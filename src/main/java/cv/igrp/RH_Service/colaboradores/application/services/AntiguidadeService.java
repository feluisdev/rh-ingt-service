package cv.igrp.RH_Service.colaboradores.application.services;

import cv.igrp.RH_Service.colaboradores.domain.models.Contrato;
import cv.igrp.RH_Service.colaboradores.domain.models.Funcionario;
import cv.igrp.RH_Service.colaboradores.domain.models.HistoricoEstadoColaborador;
import cv.igrp.RH_Service.colaboradores.domain.models.LicencaMobilidade;
import cv.igrp.RH_Service.colaboradores.domain.repository.ContratoRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.HistoricoEstadoColaboradorRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.LicencaMobilidadeRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.SubtipoLicencaMobilidadeRepository;
import cv.igrp.RH_Service.colaboradores.domain.service.CalculadoraAntiguidade;
import cv.igrp.RH_Service.colaboradores.domain.service.CalculadoraAntiguidade.PeriodoExcluido;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.SubtipoLicencaMobilidadeId;
import cv.igrp.RH_Service.parametrizacoes.domain.models.VinculoLaboral;
import cv.igrp.RH_Service.parametrizacoes.domain.models.WorkerState;
import cv.igrp.RH_Service.parametrizacoes.domain.repository.ContractTypeRepository;
import cv.igrp.RH_Service.parametrizacoes.domain.repository.VinculoLaboralRepository;
import cv.igrp.RH_Service.parametrizacoes.domain.repository.WorkerStateRepository;
import cv.igrp.RH_Service.parametrizacoes.domain.valueobject.ContractTypeId;
import cv.igrp.RH_Service.parametrizacoes.domain.valueobject.VinculoLaboralId;
import cv.igrp.RH_Service.parametrizacoes.domain.valueobject.WorkerStateId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * <b>Antiguidade / tempo de serviço.</b>
 *
 * <p>Não se calculava em lado nenhum, e isso deixava três colunas mortas: quem as
 * parametrizasse ficava convencido de que tinha feito alguma coisa. Este serviço dá-lhes o
 * primeiro consumidor:
 *
 * <ul>
 *   <li>{@code SituacaoFuncional.contaAntiguidade()} — art. 120.º n.º 2 da Lei n.º 20/X/2023: o
 *       tempo de inactividade <b>no</b> quadro não conta; a inactividade <b>fora</b> do quadro
 *       suspende o vínculo, logo também não. A disponibilidade conta (art. 122.º n.º 1).</li>
 *   <li>{@code t_leave_mobility_subtype.counts_for_seniority} — art. 47.º n.º 1 do DL n.º 3/2010:
 *       a licença sem vencimento «implica o desconto na antiguidade para todos os efeitos
 *       legais». Quais licenças o fazem é do catálogo, porque a lei faz variar com o motivo.</li>
 *   <li>{@code t_vinculo_laboral.counts_seniority} — há vínculos cujo tempo não conta. O vínculo
 *       vem do contrato, por isso o desconto aplica-se ao <b>período do contrato</b>, e não à
 *       pessoa: quem esteve dois anos num vínculo que não conta e cinco num que conta tem cinco.</li>
 * </ul>
 *
 * <p><b>O que não se desconta, e é deliberado.</b> A <b>mobilidade</b> nunca desconta: o art.
 * 137.º da Lei n.º 20/X/2023 diz que o tempo conta no lugar de origem. As <b>férias</b> não
 * gozadas contam (art. 12.º n.º 3 do DL n.º 3/2010). A <b>greve</b> perde remuneração mas não
 * desconta antiguidade (art. 16.º n.º 4) — e como não há tipo de ausência classificado para ela,
 * não fazer nada é a resposta certa.
 *
 * <p><b>O que ainda não se desconta, e é uma lacuna conhecida:</b> as faltas injustificadas, que
 * pelo art. 43.º n.º 2 não contam para antiguidade. Falta a {@code t_leave_type} uma coluna que
 * diga quais o são — hoje só o subtipo de licença tem classificação de antiguidade. Fica
 * assinalado em vez de adivinhado a partir do código do tipo.
 *
 * <p><b>A antiguidade não se guarda</b>, deriva-se. Guardá-la obrigaria a recalcular sempre que
 * uma data do passado fosse corrigida, e alguém acabaria por confiar num número velho.
 */
@Service
@RequiredArgsConstructor
public class AntiguidadeService {

    private final FuncionarioRepository funcionarioRepository;
    private final HistoricoEstadoColaboradorRepository historicoRepository;
    private final WorkerStateRepository workerStateRepository;
    private final LicencaMobilidadeRepository licencaRepository;
    private final SubtipoLicencaMobilidadeRepository subtipoRepository;
    private final ContratoRepository contratoRepository;
    private final ContractTypeRepository contractTypeRepository;
    private final VinculoLaboralRepository vinculoLaboralRepository;

    /**
     * Tempo de serviço à data indicada.
     *
     * @param ate data de referência; por omissão, hoje. Serve para responder a «quanta
     *            antiguidade tinha à data da promoção», que é uma pergunta corrente do RH
     */
    public CalculadoraAntiguidade.Antiguidade calcular(FuncionarioId funcionarioId, LocalDate ate) {
        Funcionario funcionario = funcionarioRepository.findById(funcionarioId)
                .orElseThrow(() -> IgrpResponseStatusException.notFound(
                        "Funcionário não encontrado: " + funcionarioId.getStringValor()));

        if (funcionario.getDataAdmissao() == null)
            throw IgrpResponseStatusException.of(org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY,
                    "O colaborador não tem data de admissão — não há por onde começar a contagem.");

        LocalDate referencia = ate != null ? ate : LocalDate.now();

        List<PeriodoExcluido> exclusoes = new ArrayList<>();
        exclusoes.addAll(periodosEmSituacaoQueNaoConta(funcionarioId, referencia));
        exclusoes.addAll(periodosDeLicencaQueNaoConta(funcionarioId));
        exclusoes.addAll(periodosDeVinculoQueNaoConta(funcionarioId));

        return CalculadoraAntiguidade.calcular(funcionario.getDataAdmissao(), referencia, exclusoes);
    }

    /**
     * Percorre o histórico de estados e devolve os intervalos em que o colaborador esteve numa
     * situação que não conta.
     *
     * <p>O histórico guarda <b>transições</b>, não intervalos: cada linha diz a partir de quando
     * passou a estar num estado. O intervalo de cada estado vai da sua data de efectividade até
     * à véspera da transição seguinte — ou até à data de referência, se for a última.
     */
    private List<PeriodoExcluido> periodosEmSituacaoQueNaoConta(FuncionarioId funcionarioId,
                                                               LocalDate referencia) {
        List<HistoricoEstadoColaborador> historico =
                new ArrayList<>(historicoRepository.findAllByFuncionarioId(funcionarioId));
        historico.sort(Comparator.comparing(HistoricoEstadoColaborador::getDataEfectividade));

        List<PeriodoExcluido> periodos = new ArrayList<>();
        for (int i = 0; i < historico.size(); i++) {
            HistoricoEstadoColaborador linha = historico.get(i);
            Optional<WorkerState> estado = estado(linha.getEstadoNovoId());
            if (estado.isEmpty()) continue;

            var situacao = estado.get().getSituacaoFuncional();
            // Sem situação classificada não se adivinha: um estado por classificar é
            // configuração em falta, e descontar por omissão tiraria tempo a quem o tem.
            if (situacao == null || situacao.contaAntiguidade()) continue;

            LocalDate inicio = linha.getDataEfectividade();
            LocalDate fim = i + 1 < historico.size()
                    ? historico.get(i + 1).getDataEfectividade().minusDays(1)
                    : referencia;

            periodos.add(new PeriodoExcluido(inicio, fim,
                    "Situação " + situacao.name() + " (" + estado.get().getCode() + ")"));
        }
        return periodos;
    }

    /**
     * Licenças cujo subtipo diz que o tempo não conta (art. 47.º n.º 1). Só as <b>deferidas</b>
     * entram: um pedido por decidir, indeferido ou cancelado não produziu ausência nenhuma.
     *
     * <p>A mobilidade nunca entra, mesmo que o subtipo esteja mal classificado: o art. 137.º diz
     * que o tempo conta no lugar de origem, e isso não é configurável.
     */
    private List<PeriodoExcluido> periodosDeLicencaQueNaoConta(FuncionarioId funcionarioId) {
        List<PeriodoExcluido> periodos = new ArrayList<>();

        for (LicencaMobilidade licenca : licencaRepository.findAllByFuncionarioId(
                funcionarioId, new cv.igrp.RH_Service.colaboradores.domain.filter.LicencaMobilidadeFilter())) {

            if (!licenca.isApproved()) continue;

            var subtipo = subtipoRepository.findById(
                    SubtipoLicencaMobilidadeId.from(licenca.getSubtipoId().getValor()));
            if (subtipo.isEmpty()) continue;
            if (subtipo.get().isMobilidade()) continue;
            if (!Boolean.FALSE.equals(subtipo.get().getCountsForSeniority())) continue;

            periodos.add(new PeriodoExcluido(licenca.getDataInicio(), licenca.getDataFim(),
                    "Licença " + subtipo.get().getCodigo() + " (art. 47.º n.º 1)"));
        }
        return periodos;
    }

    /**
     * Contratos cujo vínculo diz que o tempo não conta. O vínculo é da relação de trabalho e
     * chega-se a ele pelo contrato → tipo de contrato → vínculo, por isso o desconto vale pelo
     * período do contrato.
     *
     * <p>Um contrato ainda em vigor não tem fim: o período fica em aberto e o calculador corta-o
     * na data de referência.
     */
    private List<PeriodoExcluido> periodosDeVinculoQueNaoConta(FuncionarioId funcionarioId) {
        List<PeriodoExcluido> periodos = new ArrayList<>();

        for (Contrato contrato : contratoRepository.findAllByFuncionarioIdOrderByStartDateDesc(funcionarioId)) {
            if (contrato.getStartDate() == null || contrato.getContractTypeId() == null) continue;

            Optional<VinculoLaboral> vinculo = contractTypeRepository
                    .findById(ContractTypeId.from(contrato.getContractTypeId()))
                    .map(tipo -> tipo.getVinculoLaboralId())
                    .flatMap(id -> id == null ? Optional.empty()
                            : vinculoLaboralRepository.findById(VinculoLaboralId.from(id)));

            if (vinculo.isEmpty() || vinculo.get().isCountsSeniority()) continue;

            periodos.add(new PeriodoExcluido(contrato.getStartDate(), contrato.getEndDate(),
                    "Vínculo " + vinculo.get().getCode() + " não conta antiguidade"));
        }
        return periodos;
    }

    private Optional<WorkerState> estado(UUID estadoId) {
        return estadoId == null ? Optional.empty()
                : workerStateRepository.findById(WorkerStateId.from(estadoId));
    }
}
