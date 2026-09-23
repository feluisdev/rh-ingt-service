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

        // Os feriados do PERÍODO, não do ano de início: um pedido de 28 de Dezembro a 5 de
        // Janeiro atravessa o 1 de Janeiro do ano seguinte.
        var feriados = calendarioFeriadosService.feriadosDoColaborador(
                funcionarioId, dto.getDataInicio(), dto.getDataFim());
        int numeroDias = diasUteisCalculator.calcular(dto.getDataInicio(), dto.getDataFim(), feriados);

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
                saved.getEstadoTexto()));
    }
}
