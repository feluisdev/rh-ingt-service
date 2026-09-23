package cv.igrp.RH_Service.colaboradores.application.services;

import cv.igrp.RH_Service.colaboradores.domain.models.DiaAssiduidade;
import cv.igrp.RH_Service.colaboradores.domain.models.EstadoDiaApurado;
import cv.igrp.RH_Service.colaboradores.domain.models.Funcionario;
import cv.igrp.RH_Service.colaboradores.domain.models.LicencaMobilidade;
import cv.igrp.RH_Service.colaboradores.domain.models.MarcacaoAssiduidade;
import cv.igrp.RH_Service.colaboradores.domain.models.PedidoAusencia;
import cv.igrp.RH_Service.colaboradores.domain.models.SubtipoLicencaMobilidade;
import cv.igrp.RH_Service.colaboradores.domain.repository.ContratoRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.LicencaMobilidadeRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.MarcacaoAssiduidadeRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.PedidoAusenciaRepository;
import cv.igrp.RH_Service.colaboradores.domain.service.ApuramentoFaltas;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.shared.application.constants.RegimeTrabalho;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.List;
import java.util.Set;

/**
 * <b>Faltas por débito de um colaborador num mês</b> — DL n.º 3/2010, art. 13.º; Lei n.º 20/X/2023,
 * art. 170.º. Classifica cada dia (o que não é de trabalho a apurar sai logo: futuro, antes da
 * admissão, isenção de horário, feriado, ausência aprovada, licença, mobilidade externa, sem horário)
 * e entrega o resto ao {@link ApuramentoFaltas}.
 *
 * <p>Calcula-se, não se guarda: até ao fecho do mês (art. 75.º, passo seguinte), a justificação de um
 * dia — um pedido de ausência aprovado — tira-o do apuramento. O que ficar por justificar no fecho
 * passa a injustificado (art. 43.º n.º 1 b)).
 */
@Service
@RequiredArgsConstructor
public class ApuramentoFaltasService {

    public record Apuramento(YearMonth mes, boolean isento, ApuramentoFaltas.Resultado resultado) {}

    private final FuncionarioRepository funcionarioRepository;
    private final ContratoRepository contratoRepository;
    private final MarcacaoAssiduidadeRepository marcacaoRepository;
    private final PedidoAusenciaRepository pedidoAusenciaRepository;
    private final LicencaMobilidadeRepository licencaRepository;
    private final MobilidadeService mobilidadeService;
    private final CalendarioFeriadosService calendarioFeriadosService;
    private final HorarioColaboradorService horarioColaboradorService;
    private final TrabalhoSuplementarService trabalhoSuplementarService;

    @Transactional(readOnly = true)
    public Apuramento apurar(FuncionarioId funcionarioId, YearMonth mes) {
        Funcionario funcionario = funcionarioRepository.findById(funcionarioId)
                .orElseThrow(() -> IgrpResponseStatusException.notFound("Funcionário não encontrado: " + funcionarioId.getStringValor()));
        LocalDate de = mes.atDay(1);
        LocalDate ate = mes.atEndOfMonth();
        LocalDate hoje = hoje();

        // Isenção de horário: sem horário obrigatório, não há débito (o regime é do diploma de
        // desenvolvimento, art. 165.º n.º 4 da Lei 20/X/2023).
        boolean isento = contratoRepository.findCurrentByFuncionarioId(funcionarioId)
                .map(c -> RegimeTrabalho.ISENCAO_HORARIO.getCode().equals(c.getRegimeTrabalho()))
                .orElse(false);

        List<MarcacaoAssiduidade> marcacoes = marcacaoRepository.findByFuncionarioEntre(funcionarioId, de, ate);
        Set<LocalDate> feriados = calendarioFeriadosService.feriadosDoColaborador(funcionarioId, de, ate);
        List<PedidoAusencia> aprovados = pedidoAusenciaRepository.findAprovadosEntre(funcionarioId, de, ate);
        Set<LocalDate> justificados = diasCobertos(aprovados.stream().filter(p -> !p.isEmHoras()).toList(), de, ate);
        Map<LocalDate, List<DiaAssiduidade.Periodo>> horasJustificadas = horasJustificadas(
                aprovados.stream().filter(PedidoAusencia::isEmHoras).toList(), de, ate);
        Map<LocalDate, List<DiaAssiduidade.Periodo>> suplementares = trabalhoSuplementarService.autorizadosPorDia(funcionarioId, de, ate);

        List<ApuramentoFaltas.Dia> dias = new ArrayList<>();
        for (LocalDate d = de; !d.isAfter(ate); d = d.plusDays(1)) {
            final LocalDate data = d;
            List<MarcacaoAssiduidade> doDia = marcacoes.stream().filter(m -> m.getMomento().toLocalDate().equals(data)).toList();
            DiaAssiduidade assiduidade = DiaAssiduidade.calcular(data, doDia);
            boolean temValidas = doDia.stream().anyMatch(MarcacaoAssiduidade::conta);

            EstadoDiaApurado previo = estadoPrevio(funcionarioId, funcionario, data, hoje, isento, feriados, justificados);
            // Um dia com correcções por decidir não se apura: o que conta ainda não está assente.
            if (previo == null && doDia.stream().anyMatch(MarcacaoAssiduidade::isPendente))
                previo = EstadoDiaApurado.POR_VALIDAR;
            var horario = previo == null ? horarioColaboradorService.vigente(funcionarioId, data).horario() : null;
            dias.add(new ApuramentoFaltas.Dia(data, previo, horario, assiduidade, temValidas,
                    horasJustificadas.getOrDefault(data, List.of()), suplementares.getOrDefault(data, List.of())));
        }
        return new Apuramento(mes, isento, ApuramentoFaltas.apurar(dias));
    }

    private EstadoDiaApurado estadoPrevio(FuncionarioId funcionarioId, Funcionario funcionario, LocalDate data,
                                          LocalDate hoje, boolean isento, Set<LocalDate> feriados,
                                          Set<LocalDate> justificados) {
        // O dia de hoje ainda não acabou.
        if (!data.isBefore(hoje)) return EstadoDiaApurado.FUTURO;
        if (funcionario.getDataAdmissao() != null && data.isBefore(funcionario.getDataAdmissao()))
            return EstadoDiaApurado.FORA_DO_VINCULO;
        if (isento) return EstadoDiaApurado.ISENTO;
        if (feriados.contains(data)) return EstadoDiaApurado.FERIADO;
        if (justificados.contains(data)) return EstadoDiaApurado.AUSENCIA_JUSTIFICADA;
        for (LicencaMobilidade l : licencaRepository.findActiveByFuncionarioIdAt(funcionarioId, data)) {
            boolean mobilidade = mobilidadeService.subtipoSeExistir(l).map(SubtipoLicencaMobilidade::isMobilidade).orElse(false);
            if (!mobilidade) return EstadoDiaApurado.LICENCA;
            // Na mobilidade interna trabalha cá, com o horário da unidade de destino: apura-se.
            if (!l.isDestinoInterno()) return EstadoDiaApurado.MOBILIDADE_EXTERNA;
        }
        return null;
    }

    /** Os dias do mês cobertos por pedidos de dias inteiros aprovados; um pedido suspenso deixa de cobrir desde a suspensão. */
    private static Set<LocalDate> diasCobertos(List<PedidoAusencia> pedidos, LocalDate de, LocalDate ate) {
        Set<LocalDate> dias = new HashSet<>();
        for (PedidoAusencia p : pedidos) {
            LocalDate fim = p.ultimoDiaEmVigor();
            for (LocalDate d = p.getDataInicio().isBefore(de) ? de : p.getDataInicio(); !d.isAfter(fim) && !d.isAfter(ate); d = d.plusDays(1))
                dias.add(d);
        }
        return dias;
    }

    /** V58: os intervalos justificados de cada dia, dos pedidos em horas aprovados (até ao fim antecipado). */
    private static Map<LocalDate, List<DiaAssiduidade.Periodo>> horasJustificadas(List<PedidoAusencia> pedidos,
                                                                              LocalDate de, LocalDate ate) {
        Map<LocalDate, List<DiaAssiduidade.Periodo>> porDia = new HashMap<>();
        for (PedidoAusencia p : pedidos) {
            LocalDate fim = p.ultimoDiaEmVigor();
            for (LocalDate d = p.getDataInicio().isBefore(de) ? de : p.getDataInicio(); !d.isAfter(fim) && !d.isAfter(ate); d = d.plusDays(1))
                porDia.computeIfAbsent(d, k -> new ArrayList<>()).add(new DiaAssiduidade.Periodo(p.getHoraInicio(), p.getHoraFim()));
        }
        return porDia;
    }

    LocalDate hoje() { return LocalDate.now(); }
}
