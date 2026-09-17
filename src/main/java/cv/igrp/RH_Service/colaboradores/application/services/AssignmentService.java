package cv.igrp.RH_Service.colaboradores.application.services;

import cv.igrp.RH_Service.colaboradores.domain.models.Assignment;
import cv.igrp.RH_Service.colaboradores.domain.repository.AssignmentRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.AssignmentId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.carreiras.domain.models.Grade;
import cv.igrp.RH_Service.carreiras.domain.repository.CategoryRepository;
import cv.igrp.RH_Service.carreiras.domain.repository.GradeRepository;
import cv.igrp.RH_Service.carreiras.domain.valueobject.CategoryId;
import cv.igrp.RH_Service.carreiras.domain.valueobject.GradeId;
import cv.igrp.RH_Service.estrutura.domain.models.Position;
import cv.igrp.RH_Service.estrutura.domain.repository.FunctionRepository;
import cv.igrp.RH_Service.estrutura.domain.repository.PositionRepository;
import cv.igrp.RH_Service.estrutura.domain.valueobject.FunctionId;
import cv.igrp.RH_Service.estrutura.domain.valueobject.PositionId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

/**
 * Serviço de afectação — centraliza a criação/versionamento de Afectações (SCD Type 2)
 * e as regras de negócio (Lugar ocupável, uma cadeira um ocupante, coerência de grelha).
 * Reutilizado pelo registo de colaborador e pela mobilidade.
 */
@Service
@RequiredArgsConstructor
public class AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final PositionRepository positionRepository;
    private final GradeRepository gradeRepository;
    private final CategoryRepository categoryRepository;
    private final FunctionRepository functionRepository;

    /**
     * Afecta um colaborador a um Lugar. Se já houver afectação PRINCIPAL corrente e a nova
     * também for PRINCIPAL, a corrente é encerrada (SCD Type 2) antes de abrir a nova.
     */
    public Assignment afectar(FuncionarioId funcionarioId, UUID positionId, UUID gradeId, UUID functionId,
                              String origem, String assignmentType, LocalDate dataInicio,
                              UUID originAssignmentId, String notes) {

        String tipo = (assignmentType == null || assignmentType.isBlank())
                ? Assignment.PRINCIPAL : assignmentType;

        Position position = positionRepository.findById(PositionId.from(positionId))
                .orElseThrow(() -> IgrpResponseStatusException.notFound(
                        "Lugar não encontrado: " + positionId));

        if (!position.podeSerOcupado())
            throw IgrpResponseStatusException.of(org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY,
                    "O Lugar '" + position.getNumeroLugar() + "' não está disponível (estado="
                            + position.getEstado() + ").");

        // Uma cadeira, um ocupante corrente
        if (assignmentRepository.isPositionOccupied(positionId))
            throw IgrpResponseStatusException.of(org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY,
                    "O Lugar '" + position.getNumeroLugar() + "' já está ocupado.");

        // Coerência com a grelha PCFR
        if (position.isForaDeGrelha() && gradeId != null)
            throw IgrpResponseStatusException.of(org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY,
                    "O Lugar está fora da grelha (sem carreira/categoria) — não pode ter escalão.");
        if (!position.isForaDeGrelha() && gradeId == null)
            throw IgrpResponseStatusException.of(org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY,
                    "O Lugar é de carreira — o escalão (gradeId) é obrigatório.");

        // PCFR: o escalão escolhido tem de pertencer à categoria do Lugar
        if (gradeId != null) {
            Grade grade = gradeRepository.findById(GradeId.from(gradeId))
                    .orElseThrow(() -> IgrpResponseStatusException.notFound(
                            "Escalão não encontrado: " + gradeId));
            if (!grade.getCategoryId().getValor().equals(position.getCategoryId()))
                throw IgrpResponseStatusException.of(org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY,
                        "O escalão '" + grade.getName() + "' pertence à categoria '"
                                + nomeCategoria(grade.getCategoryId().getValor())
                                + "', mas o Lugar '" + position.getNumeroLugar()
                                + "' exige um escalão da categoria '"
                                + nomeCategoria(position.getCategoryId())
                                + "'. Escolha um escalão dessa categoria.");
        }

        // Coerência cargo↔função (BR-FUN-02): a função escolhida tem de pertencer ao
        // cargo do Lugar. Funções genéricas (jobId nulo) servem qualquer cargo -- essa
        // decisão vive em OrgFunction.validarCompatibilidadeComCargo, que até aqui
        // existia sem nenhum chamador.
        if (functionId != null) {
            functionRepository.findById(FunctionId.from(functionId))
                    .orElseThrow(() -> IgrpResponseStatusException.notFound(
                            "Função não encontrada: " + functionId))
                    .validarCompatibilidadeComCargo(position.getJobId());
        }

        // SCD Type 2: encerrar a afectação PRINCIPAL corrente antes de abrir a nova
        if (Assignment.PRINCIPAL.equals(tipo)) {
            Optional<Assignment> atual = assignmentRepository.findCurrentPrincipalByFuncionario(funcionarioId);
            atual.ifPresent(a -> {
                a.encerrar(dataInicio.minusDays(1));
                assignmentRepository.save(a);
            });
        }

        return assignmentRepository.save(Assignment.criar(
                funcionarioId, positionId, gradeId, functionId, tipo, origem,
                dataInicio, originAssignmentId, notes));
    }

    /**
     * Afecta por MOBILIDADE ao Lugar de destino, herdando o escalão (grade) da afectação
     * corrente — a mobilidade muda a cadeira mas mantém a posição-na-grelha da pessoa.
     * A afectação corrente é encerrada (SCD Type 2) dentro de {@link #afectar}.
     */
    public Assignment afectarMobilidade(FuncionarioId funcionarioId, UUID positionId,
                                        LocalDate dataInicio, String notes) {
        Optional<Assignment> atual = assignmentRepository.findCurrentPrincipalByFuncionario(funcionarioId);
        UUID gradeAtual = atual.map(Assignment::getGradeId).orElse(null);
        // Guarda a afectação de origem para permitir o regresso (mobilidade temporária).
        UUID origemId = atual.map(a -> a.getId().getValor()).orElse(null);
        return afectar(funcionarioId, positionId, gradeAtual, null,
                Assignment.MOBILIDADE, Assignment.PRINCIPAL, dataInicio, origemId, notes);
    }

    /** Resultado de uma progressão: a nova afectação e os escalões de partida e de chegada. */
    public record Progressao(Assignment afectacao, Grade escalaoAnterior, Grade escalaoNovo) {}

    /**
     * Progressão (evolução horizontal dentro da mesma categoria — Lei 20/X/2023, art. 140.º):
     * sobe o colaborador para o escalão activo imediatamente superior da categoria do seu Lugar.
     * Mantém o Lugar e a função; não exige vaga. Fecha a afectação PRINCIPAL corrente na véspera
     * da data de efeito e abre uma nova no mesmo Lugar com origem PROGRESSAO (SCD Type 2).
     * Não passa por {@link #afectar}: o Lugar já está ocupado — pelo próprio colaborador.
     */
    public Progressao progredir(FuncionarioId funcionarioId, LocalDate dataEfeito, String notes) {
        Assignment atual = assignmentRepository.findCurrentPrincipalByFuncionario(funcionarioId)
                .orElseThrow(() -> IgrpResponseStatusException.of(org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY,
                        "O colaborador não tem afectação principal corrente — não é possível progredir."));

        if (!dataEfeito.isAfter(atual.getDataInicio()))
            throw IgrpResponseStatusException.of(org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY,
                    "A data de efeito tem de ser posterior ao início da afectação corrente ("
                            + atual.getDataInicio() + ").");

        Position position = positionRepository.findById(PositionId.from(atual.getPositionId()))
                .orElseThrow(() -> IgrpResponseStatusException.notFound(
                        "Lugar não encontrado: " + atual.getPositionId()));

        if (position.isForaDeGrelha() || atual.getGradeId() == null)
            throw IgrpResponseStatusException.of(org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY,
                    "O Lugar '" + position.getNumeroLugar()
                            + "' está fora da grelha (sem carreira/categoria) — não há escalões para progredir.");

        Grade escalaoAtual = gradeRepository.findById(GradeId.from(atual.getGradeId()))
                .orElseThrow(() -> IgrpResponseStatusException.notFound(
                        "Escalão não encontrado: " + atual.getGradeId()));

        Grade escalaoSeguinte = gradeRepository
                .findByCategoryIdOrderByGradeNumber(CategoryId.from(position.getCategoryId()))
                .stream()
                .filter(g -> Boolean.TRUE.equals(g.getIsActive()))
                .filter(g -> g.getGradeNumber() > escalaoAtual.getGradeNumber())
                .findFirst()
                .orElseThrow(() -> IgrpResponseStatusException.of(org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY,
                        "O colaborador já está no último escalão da categoria '"
                                + nomeCategoria(position.getCategoryId()) + "'."));

        atual.encerrar(dataEfeito.minusDays(1));
        assignmentRepository.save(atual);

        Assignment nova = assignmentRepository.save(Assignment.criar(
                funcionarioId, atual.getPositionId(), escalaoSeguinte.getId().getValor(),
                atual.getFunctionId(), Assignment.PRINCIPAL, Assignment.PROGRESSAO,
                dataEfeito, null, notes));

        return new Progressao(nova, escalaoAtual, escalaoSeguinte);
    }

    /**
     * Regresso de mobilidade (temporária): fecha a afectação de MOBILIDADE corrente e
     * reabre a afectação de origem ({@code origin_assignment_id}) num novo período corrente,
     * no mesmo Lugar de origem, herdando escalão/função. Se a mobilidade não registou origem
     * (permanente) ou se o Lugar de origem já foi reafectado/extinto, apenas fecha a corrente.
     * Idempotente: sem afectação corrente, não faz nada.
     */
    public void regressarDeMobilidade(FuncionarioId funcionarioId, LocalDate dataRegresso) {
        Optional<Assignment> correnteOpt = assignmentRepository.findCurrentPrincipalByFuncionario(funcionarioId);
        if (correnteOpt.isEmpty()) return;

        Assignment corrente = correnteOpt.get();
        corrente.encerrar(dataRegresso);
        assignmentRepository.save(corrente);

        UUID origemId = corrente.getOriginAssignmentId();
        if (origemId == null) return; // mobilidade permanente — nada a reabrir

        Assignment origem = assignmentRepository.findById(AssignmentId.from(origemId)).orElse(null);
        if (origem == null) return;

        // Só reabre se o Lugar de origem ainda estiver vago (não foi reafectado entretanto).
        if (assignmentRepository.isPositionOccupied(origem.getPositionId())) return;

        assignmentRepository.save(Assignment.criar(
                funcionarioId, origem.getPositionId(), origem.getGradeId(), origem.getFunctionId(),
                Assignment.PRINCIPAL, Assignment.MOBILIDADE, dataRegresso, null,
                "Regresso de mobilidade ao Lugar de origem"));
    }

    /**
     * Encerra a afectação PRINCIPAL corrente do colaborador (cessação/reforma).
     * O Lugar volta a estar VAGO (derivado). Idempotente: se não houver afectação
     * corrente, não faz nada.
     */
    public void encerrarAfectacaoCorrente(FuncionarioId funcionarioId, LocalDate dataFim) {
        assignmentRepository.findCurrentPrincipalByFuncionario(funcionarioId)
                .ifPresent(a -> {
                    a.encerrar(dataFim);
                    assignmentRepository.save(a);
                });
    }

    /** Nome legível da categoria para mensagens de erro; devolve o UUID se não for encontrada. */
    private String nomeCategoria(UUID categoryId) {
        if (categoryId == null) return "(sem categoria)";
        return categoryRepository.findById(CategoryId.from(categoryId))
                .map(c -> c.getName())
                .orElse(categoryId.toString());
    }
}
