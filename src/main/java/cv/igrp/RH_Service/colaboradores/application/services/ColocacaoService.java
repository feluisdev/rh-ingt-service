package cv.igrp.RH_Service.colaboradores.application.services;

import cv.igrp.RH_Service.carreiras.domain.repository.CategoryRepository;
import cv.igrp.RH_Service.carreiras.domain.repository.GradeRepository;
import cv.igrp.RH_Service.carreiras.domain.valueobject.CategoryId;
import cv.igrp.RH_Service.carreiras.domain.valueobject.GradeId;
import cv.igrp.RH_Service.colaboradores.domain.models.Assignment;
import cv.igrp.RH_Service.colaboradores.domain.models.Contrato;
import cv.igrp.RH_Service.colaboradores.domain.models.EstadoContrato;
import cv.igrp.RH_Service.colaboradores.domain.models.Funcionario;
import cv.igrp.RH_Service.colaboradores.domain.models.HistoricoEstadoColaborador;
import cv.igrp.RH_Service.colaboradores.domain.models.TipoAfectacao;
import cv.igrp.RH_Service.colaboradores.domain.repository.AssignmentRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.ContratoRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.HistoricoEstadoColaboradorRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.estrutura.domain.models.Position;
import cv.igrp.RH_Service.estrutura.domain.repository.PositionRepository;
import cv.igrp.RH_Service.estrutura.domain.valueobject.PositionId;
import cv.igrp.RH_Service.parametrizacoes.domain.models.SituacaoFuncional;
import cv.igrp.RH_Service.parametrizacoes.domain.models.WorkerState;
import cv.igrp.RH_Service.parametrizacoes.domain.repository.WorkerStateRepository;
import cv.igrp.RH_Service.parametrizacoes.domain.valueobject.WorkerStateId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

/**
 * <b>Colocação</b>: pôr num Lugar quem não tem nenhum. É o único caso de escrita que nenhum
 * movimento cobre — a admissão de quem acabou de entrar e o <b>reingresso</b> de quem ficou sem
 * Lugar (disponibilidade depois de uma licença que abriu vaga, inactividade fora do quadro).
 *
 * <p>Não é uma porta para mover pessoas. Quem tem Lugar muda-o por um movimento, que tem as
 * suas regras (progressão, promoção, transferência, mudança de carreira); por aqui seria
 * mudar sem nenhuma delas. Regras: BR-AF-15 a BR-AF-22.
 */
@Service
@RequiredArgsConstructor
public class ColocacaoService {

    /** Mesma pessoa, mas quem já teve Lugar volta a ele: art. 122.º (disponibilidade). */
    public static final String REINGRESSO = Assignment.REINGRESSO;

    private final AssignmentService assignmentService;
    private final AssignmentRepository assignmentRepository;
    private final FuncionarioRepository funcionarioRepository;
    private final ContratoRepository contratoRepository;
    private final PositionRepository positionRepository;
    private final GradeRepository gradeRepository;
    private final CategoryRepository categoryRepository;
    private final WorkerStateRepository workerStateRepository;
    private final HistoricoEstadoColaboradorRepository historicoRepository;

    public record Resultado(Assignment afectacao, String origem, List<String> alertas) {}

    public Resultado colocar(FuncionarioId funcionarioId, UUID positionId, UUID gradeId, UUID functionId,
                             String origemPedida, TipoAfectacao tipo, LocalDate dataInicio, String notes) {
        List<String> alertas = new ArrayList<>();

        // BR-AF-13: a substituição tem endpoint e regras próprias
        if (tipo == TipoAfectacao.SUBSTITUICAO)
            throw recusa("Para pôr alguém a substituir o titular de um Lugar, use a acção «Substituição».");

        // BR-AF-15: o colaborador existe e está activo
        Funcionario funcionario = funcionarioRepository.findById(funcionarioId)
                .orElseThrow(() -> IgrpResponseStatusException.notFound("O colaborador indicado não existe."));
        if (!Boolean.TRUE.equals(funcionario.getIsActive()))
            throw recusa("Este colaborador não está activo e não pode ser colocado num Lugar.");

        // BR-AF-16: quem tem Lugar muda por um movimento
        if (assignmentRepository.findCurrentPrincipalByFuncionario(funcionarioId).isPresent())
            throw recusa("Este colaborador já ocupa um Lugar. Para o mudar de Lugar ou de categoria, "
                    + "use a progressão, a promoção, a transferência ou a mudança de carreira.");

        // BR-AF-17: a licença que lhe tirou o Lugar ainda está em curso
        WorkerState estado = funcionario.getWorkerStateId() == null ? null
                : workerStateRepository.findById(WorkerStateId.from(funcionario.getWorkerStateId())).orElse(null);
        SituacaoFuncional situacao = estado != null ? estado.getSituacaoFuncional() : null;
        if (situacao == SituacaoFuncional.INACTIVIDADE_FORA_QUADRO)
            throw recusa("Este colaborador está de licença e ficou sem Lugar. Só pode ser colocado num Lugar quando regressar.");

        // BR-AF-18: com contrato corrente activo, e não antes dele
        Contrato contrato = contratoRepository.findCurrentByFuncionarioId(funcionarioId)
                .orElseThrow(() -> recusa("Este colaborador não tem contrato em vigor. Registe o contrato antes de o colocar num Lugar."));
        if (contrato.getStatus() != EstadoContrato.ATIVO)
            throw recusa("O contrato deste colaborador está " + (contrato.getStatus() == EstadoContrato.SUSPENSO ? "suspenso" : "cessado")
                    + ". Só é possível colocá-lo num Lugar com o contrato activo.");
        if (contrato.getStartDate() != null && dataInicio.isBefore(contrato.getStartDate()))
            throw recusa("A data de início não pode ser anterior ao início do contrato (" + data(contrato.getStartDate()) + ").");

        // BR-AF-19: a origem é do sistema — ADMISSAO na primeira vez, REINGRESSO depois
        List<Assignment> anteriores = assignmentRepository.findAllByFuncionarioOrderByDataInicioDesc(funcionarioId)
                .stream().filter(Assignment::isPrincipal).toList();
        String origem = anteriores.isEmpty() ? Assignment.ADMISSAO : REINGRESSO;
        if (origemPedida != null && !origemPedida.isBlank() && !origem.equals(origemPedida)) {
            if (!Assignment.ADMISSAO.equals(origemPedida) && !REINGRESSO.equals(origemPedida))
                throw recusa("Aqui só se admite um colaborador ou se reintegra quem ficou sem Lugar. Para progressões, "
                        + "promoções, transferências ou mudanças de carreira, use a acção própria.");
            throw recusa(anteriores.isEmpty()
                    ? "Este colaborador nunca teve um Lugar: a colocação é uma admissão, não um reingresso."
                    : "Este colaborador já teve um Lugar: a colocação é um reingresso, não uma admissão.");
        }

        // BR-AF-20: nunca antes da admissão, nem a sobrepor-se à última afectação
        if (funcionario.getDataAdmissao() != null && dataInicio.isBefore(funcionario.getDataAdmissao()))
            throw recusa("A data de início não pode ser anterior à data de admissão (" + data(funcionario.getDataAdmissao()) + ").");
        Optional<Assignment> ultima = anteriores.stream().findFirst();
        if (ultima.isPresent() && ultima.get().getDataFim() != null && !dataInicio.isAfter(ultima.get().getDataFim()))
            throw recusa("A data de início tem de ser posterior a " + data(ultima.get().getDataFim())
                    + ", o dia em que este colaborador deixou o último Lugar.");

        // BR-AF-21: no reingresso, a mesma categoria (art. 122.º) e, por omissão, o mesmo escalão
        UUID escalao = gradeId;
        if (ultima.isPresent() && ultima.get().getGradeId() != null) {
            UUID categoriaAnterior = categoriaDoEscalao(ultima.get().getGradeId());
            Position lugar = positionRepository.findById(PositionId.from(positionId)).orElse(null);
            if (lugar != null && categoriaAnterior != null && !categoriaAnterior.equals(lugar.getCategoryId()))
                throw recusa("No reingresso, o colaborador volta a um Lugar da sua categoria"
                        + nomeCategoria(categoriaAnterior) + ". Escolha um Lugar vago dessa categoria; "
                        + "a mudança de categoria faz-se depois, pela promoção ou pela mudança de carreira.");
            if (escalao == null && lugar != null && !lugar.isForaDeGrelha())
                escalao = ultima.get().getGradeId();
        }

        Assignment afectacao = assignmentService.afectar(funcionarioId, positionId, escalao, functionId,
                origem, tipo, dataInicio, notes);

        // BR-AF-22: quem estava em disponibilidade volta à actividade no quadro
        if (situacao == SituacaoFuncional.DISPONIBILIDADE) {
            Optional<WorkerState> activo = workerStateRepository.findBySituacao(SituacaoFuncional.ACTIVIDADE_NO_QUADRO);
            if (activo.isPresent()) {
                UUID anterior = funcionario.getWorkerStateId();
                funcionario.atualizarWorkerState(activo.get().getId().getValor(), true);
                funcionarioRepository.save(funcionario);
                historicoRepository.save(HistoricoEstadoColaborador.criar(funcionarioId, anterior,
                        activo.get().getId().getValor(), "REINGRESSO", dataInicio,
                        "Reingresso num Lugar vago da sua categoria (art. 122.º)"));
            } else {
                alertas.add("O colaborador foi colocado, mas o estado dele continua «em disponibilidade»: não há no catálogo "
                        + "um estado de actividade no quadro para o qual o passar.");
            }
        }
        return new Resultado(afectacao, origem, alertas);
    }

    private UUID categoriaDoEscalao(UUID gradeId) {
        return gradeRepository.findById(GradeId.from(gradeId))
                .map(g -> g.getCategoryId() != null ? g.getCategoryId().getValor() : null).orElse(null);
    }

    private String nomeCategoria(UUID categoriaId) {
        return categoryRepository.findById(CategoryId.from(categoriaId))
                .map(c -> " (" + c.getName() + ")").orElse("");
    }

    private static String data(LocalDate d) {
        return d == null ? "" : d.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    private static IgrpResponseStatusException recusa(String motivo) {
        return IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY, motivo);
    }
}
