package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.application.services.CalendarioFeriadosService;
import cv.igrp.RH_Service.colaboradores.application.services.SaldoAusenciaService;
import cv.igrp.RH_Service.colaboradores.domain.models.OpcaoFaltaInjustificada;
import cv.igrp.RH_Service.colaboradores.domain.models.PedidoAusencia;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.PedidoAusenciaRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.TipoAusenciaRepository;
import cv.igrp.RH_Service.colaboradores.domain.service.DiasUteisCalculator;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.TipoAusenciaId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;

import cv.igrp.RH_Service.colaboradores.application.dto.PedidoAusenciaCriadoResponseDTO;

@Component("colabsCreatePedidoAusenciaCommandHandler")
@RequiredArgsConstructor
public class CreatePedidoAusenciaCommandHandler
        implements CommandHandler<CreatePedidoAusenciaCommand, ResponseEntity<PedidoAusenciaCriadoResponseDTO>> {

    private final PedidoAusenciaRepository pedidoRepository;
    private final FuncionarioRepository funcionarioRepository;
    private final TipoAusenciaRepository tipoAusenciaRepository;
    private final CalendarioFeriadosService calendarioFeriadosService;
    private final DiasUteisCalculator diasUteisCalculator;
    private final SaldoAusenciaService saldoAusenciaService;

    @IgrpCommandHandler
    @Transactional
    public ResponseEntity<PedidoAusenciaCriadoResponseDTO> handle(CreatePedidoAusenciaCommand command) {
        var funcionarioId = FuncionarioId.from(command.getFuncionarioId());
        funcionarioRepository.findById(funcionarioId)
                .orElseThrow(() -> IgrpResponseStatusException.notFound("Funcionário não encontrado: " + command.getFuncionarioId()));

        var dto = command.getRequest();
        var tipoId = TipoAusenciaId.from(dto.getTipoAusenciaId());
        var tipo = tipoAusenciaRepository.findById(tipoId)
                .orElseThrow(() -> IgrpResponseStatusException.notFound("Tipo de ausência não encontrado: " + dto.getTipoAusenciaId()));
        if (!Boolean.TRUE.equals(tipo.getIsActive()))
            throw IgrpResponseStatusException.badRequest("Tipo de ausência inactivo: " + dto.getTipoAusenciaId());

        // V58: com horaInicio/horaFim é um pedido em horas, e segue o seu caminho. Sem elas, tudo
        // como sempre foi -- os campos são novos e opcionais.
        LocalTime horaInicio = hora(dto.getHoraInicio(), "horaInicio");
        LocalTime horaFim = hora(dto.getHoraFim(), "horaFim");
        if (horaInicio != null || horaFim != null)
            return criarEmHoras(funcionarioId, tipo, dto, horaInicio, horaFim);

        // Os feriados do PERÍODO, não do ano de início: um pedido de 28 de Dezembro a 5 de
        // Janeiro atravessa o 1 de Janeiro do ano seguinte.
        var feriados = calendarioFeriadosService.feriadosDoColaborador(
                funcionarioId, dto.getDataInicio(), dto.getDataFim());
        // Art. 76.º: dias úteis só onde a lei o diz; nas outras faltas contam os fins-de-semana
        // e feriados intercalados. Qual é o caso di-lo a linha do catálogo (V56).
        int numeroDias = diasUteisCalculator.calcular(dto.getDataInicio(), dto.getDataFim(), feriados,
                tipo.getContagem());

        if (pedidoRepository.existsOverlapForFuncionario(funcionarioId, dto.getDataInicio(), dto.getDataFim()))
            throw IgrpResponseStatusException.conflict("Existe sobreposição de datas com um pedido APROVADO ou PENDENTE do mesmo funcionário.");

        // Os três limites do art. 15.º n.º 1, cada um com a sua natureza (V53). Vêm por esta
        // ordem de propósito: a recusa mais útil é a que fala do próprio pedido.
        //
        // O limite por OCORRÊNCIA olha só para este pedido. É o que a lei diz na maioria das
        // alíneas — «até 6, por ocasião do casamento», «até 8, por falecimento do cônjuge»,
        // «duas por cada prova» —, e é por isso que não se soma nada: quem perde dois
        // familiares no mesmo ano tem direito às duas ausências.
        if (tipo.excedeLimitePorOcorrencia(numeroDias))
            throw IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Este tipo de ausência admite no máximo " + tipo.getMaxDaysPerOccurrence()
                            + " dias de cada vez, e foram pedidos " + numeroDias + ".");

        int ano = dto.getDataInicio().getYear();

        if (tipo.getMaxDaysPerMonth() != null) {
            int mes = dto.getDataInicio().getMonthValue();
            int usadosNoMes = pedidoRepository.somarDiasNoMes(funcionarioId, tipoId, ano, mes);
            if (tipo.excedeLimiteMensal(usadosNoMes, numeroDias))
                throw IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY,
                        "Limite mensal de dias excedido. Disponíveis neste mês: "
                                + (tipo.getMaxDaysPerMonth() - usadosNoMes) + ", solicitados: " + numeroDias + ".");
        }

        if (tipo.getMaxDaysPerYear() != null) {
            int diasUsados = pedidoRepository.somarDiasNoAno(funcionarioId, tipoId, ano);
            if (tipo.excedeLimiteAnual(diasUsados, numeroDias))
                throw IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY,
                        "Limite anual de dias excedido. Disponíveis: " + (tipo.getMaxDaysPerYear() - diasUsados) + ", solicitados: " + numeroDias);
        }

        // Art. 43.º n.º 2: a falta injustificada «implica a opção entre a perda das remunerações
        // correspondentes aos dias de ausência, ou o seu desconto nas férias». É a única escolha
        // que a lei dá — o desconto na antiguidade, no mesmo número, é imperativo — e é de cada
        // caso. Exige-se no acto de registar: deixá-la para depois criava linhas que ninguém
        // voltaria a abrir, e quem processa vencimentos ficaria sem saber o que fazer com elas.
        var opcao = OpcaoFaltaInjustificada.de(dto.getOpcaoFaltaInjustificada());

        if (tipo.isFaltaInjustificada() && opcao == null)
            throw IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Uma falta injustificada tem de dizer o que se faz aos dias (art. 43.º n.º 2): "
                            + "PERDA_REMUNERACAO ou DESCONTO_FERIAS.");

        if (!tipo.isFaltaInjustificada() && opcao != null)
            throw IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY,
                    "A opção do art. 43.º n.º 2 só existe nas faltas injustificadas, e o tipo '"
                            + tipo.getCodigo() + "' não está classificado como tal.");

        var pedido = PedidoAusencia.criar(
                funcionarioId, tipoId, dto.getDataInicio(), dto.getDataFim(), numeroDias,
                dto.getMotivo(), opcao);

        // Os dias ficam reservados desde a submissão: dois pedidos em simultâneo já não
        // podem esgotar duas vezes o mesmo saldo. Sem saldo suficiente, é 422 já aqui.
        saldoAusenciaService.reservar(pedido);

        var saved = pedidoRepository.save(pedido);

        return ResponseEntity.status(201).body(new PedidoAusenciaCriadoResponseDTO(
                saved.getId().getStringValor(),
                saved.getNumeroDias(),
                saved.getEstadoTexto(),
                0));
    }

    /**
     * <b>Pedido em horas</b> (V58) — o que a lei dá em horas: o tratamento ambulatório «durante o tempo
     * necessário» (DL n.º 3/2010, art. 37.º), as consultas pré-natais, a doação de sangue, o crédito
     * sindical (art. 15.º), a amamentação (Lei n.º 20/X/2023, art. 172.º n.º 3). As horas valem em
     * cada dia do intervalo. Não conta dias ({@code numeroDias} = 0): o apuramento de faltas desconta
     * as horas justificadas, e o art. 38.º n.º 2 converte-as pelo art. 13.º.
     */
    private ResponseEntity<PedidoAusenciaCriadoResponseDTO> criarEmHoras(
            FuncionarioId funcionarioId, cv.igrp.RH_Service.colaboradores.domain.models.TipoAusencia tipo,
            cv.igrp.RH_Service.colaboradores.application.dto.PedidoAusenciaRequestDTO dto,
            LocalTime horaInicio, LocalTime horaFim) {
        String recusa = tipo.motivoParaRecusarHoras();
        if (recusa != null)
            throw IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY,
                    "O tipo '" + tipo.getCodigo() + "' não admite pedidos em horas: " + recusa + ".");
        if (dto.getDataFim().isBefore(dto.getDataInicio()))
            throw IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY,
                    "A data de fim é anterior à de início.");
        if (OpcaoFaltaInjustificada.de(dto.getOpcaoFaltaInjustificada()) != null)
            throw IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY,
                    "A opção do art. 43.º n.º 2 só existe nas faltas injustificadas.");

        var pedido = PedidoAusencia.criar(funcionarioId, tipo.getId(), dto.getDataInicio(), dto.getDataFim(),
                0, dto.getMotivo(), null);
        pedido.definirHoras(horaInicio, horaFim);

        // O tecto por ocorrência conta os dias do intervalo: é assim que se limitam os meses da
        // amamentação (183 dias no seed, os 6 meses do art. 20.º do DL n.º 3/2010).
        long diasDoIntervalo = java.time.temporal.ChronoUnit.DAYS.between(dto.getDataInicio(), dto.getDataFim()) + 1;
        if (tipo.getMaxDaysPerOccurrence() != null && diasDoIntervalo > tipo.getMaxDaysPerOccurrence())
            throw IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Este tipo de ausência admite no máximo " + tipo.getMaxDaysPerOccurrence()
                            + " dias de cada vez, e o intervalo tem " + diasDoIntervalo + ".");

        if (pedidoRepository.existsSobreposicaoEmHoras(funcionarioId, dto.getDataInicio(), dto.getDataFim(), horaInicio, horaFim))
            throw IgrpResponseStatusException.conflict(
                    "Existe sobreposição com um pedido APROVADO ou PENDENTE do mesmo funcionário nessas datas e horas.");

        // As 2 horas da amamentação podem ir em dois pedidos de 1 hora; um terceiro já não cabe.
        if (tipo.getMaxMinutosPorDia() != null) {
            int jaPedidos = pedidoRepository.findEmHorasDoTipoEntre(funcionarioId, tipo.getId(), dto.getDataInicio(), dto.getDataFim())
                    .stream().mapToInt(PedidoAusencia::minutosPorDia).sum();
            if (jaPedidos + pedido.minutosPorDia() > tipo.getMaxMinutosPorDia())
                throw IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY,
                        "Este tipo admite no máximo " + tipo.getMaxMinutosPorDia() + " minutos por dia; já há "
                                + jaPedidos + " pedidos nessas datas e este pede " + pedido.minutosPorDia() + ".");
        }

        var saved = pedidoRepository.save(pedido);
        return ResponseEntity.status(201).body(new PedidoAusenciaCriadoResponseDTO(
                saved.getId().getStringValor(), 0, saved.getEstadoTexto(), saved.minutosPorDia()));
    }

    private static LocalTime hora(String valor, String campo) {
        if (valor == null || valor.isBlank()) return null;
        try {
            return LocalTime.parse(valor.trim());
        } catch (java.time.format.DateTimeParseException e) {
            throw IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY,
                    campo + " escreve-se HH:mm (ex.: 08:30): " + valor + ".");
        }
    }
}
