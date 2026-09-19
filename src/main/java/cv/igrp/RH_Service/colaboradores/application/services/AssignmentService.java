package cv.igrp.RH_Service.colaboradores.application.services;

import cv.igrp.RH_Service.colaboradores.domain.models.Assignment;
import cv.igrp.RH_Service.colaboradores.domain.models.TipoAfectacao;
import cv.igrp.RH_Service.colaboradores.domain.repository.AssignmentRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.AssignmentId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.carreiras.domain.models.Category;
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
 * Reutilizado pelo registo de colaborador e pelos movimentos de carreira.
 *
 * <p>A <b>mobilidade transitória não passa por aqui</b>: não ocupa lugar do quadro no destino
 * (Lei n.º 20/X/2023, art. 135.º n.º 7), logo não cria nem fecha afectações — ver
 * {@link MobilidadeService}. A mudança definitiva de Lugar é a {@link #transferir transferência}.
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
                              String origem, TipoAfectacao assignmentType, LocalDate dataInicio,
                              UUID originAssignmentId, String notes) {

        TipoAfectacao tipo = assignmentType != null ? assignmentType : TipoAfectacao.PRINCIPAL;

        Position position = validarLugarParaAfectacao(positionId, gradeId, functionId);

        // Uma cadeira, um titular corrente. Quem entra a outro título (substituição,
        // acumulação) não desaloja o titular nem exige que o Lugar esteja vago -- é para
        // isso que o índice do Lugar é parcial (V45).
        if (tipo.isPrincipal() && assignmentRepository.temTitular(positionId))
            throw IgrpResponseStatusException.of(org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY,
                    "O Lugar '" + position.getNumeroLugar() + "' já tem titular.");

        // SCD Type 2: encerrar a afectação PRINCIPAL corrente antes de abrir a nova
        if (tipo.isPrincipal()) {
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
     * Afectação em substituição de um titular impedido. Passa pelas mesmas validações de
     * Lugar que a {@link #afectar} — Lugar ocupável, coerência de grelha, função compatível
     * com o cargo — e só não passa pela do titular, porque o Lugar <b>tem</b> titular: é
     * essa a razão de existir da substituição.
     *
     * <p>Quem decide se o titular pode ser substituído é o {@code SubstituicaoService};
     * aqui trata-se só da mecânica da afectação.
     */
    public Assignment afectarSubstituicao(FuncionarioId funcionarioId, UUID positionId, UUID gradeId,
                                          UUID functionId, AssignmentId titularAssignmentId,
                                          LocalDate dataInicio, String notes) {

        Position position = validarLugarParaAfectacao(positionId, gradeId, functionId);

        // A afectação PRINCIPAL do substituto não se toca: quem vai substituir mantém o seu
        // próprio Lugar, se o tiver (art. 91.º n.º 1 al. a), nomeação em substituição).
        return assignmentRepository.save(Assignment.criarSubstituicao(
                funcionarioId, position.getId().getValor(), gradeId, functionId,
                titularAssignmentId, dataInicio, notes));
    }

    /**
     * Validações do Lugar que valem para qualquer título de ocupação: o Lugar existe e está
     * ocupável, a grelha é coerente com ele, e a função pertence ao seu cargo. O que <b>não</b>
     * está aqui é a regra do titular único, porque essa depende do título — ver {@link #afectar}.
     */
    private Position validarLugarParaAfectacao(UUID positionId, UUID gradeId, UUID functionId) {
        Position position = positionRepository.findById(PositionId.from(positionId))
                .orElseThrow(() -> IgrpResponseStatusException.notFound(
                        "Lugar não encontrado: " + positionId));

        if (!position.podeSerOcupado())
            throw IgrpResponseStatusException.of(org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY,
                    "O Lugar '" + position.getNumeroLugar() + "' não está disponível (estado="
                            + position.getEstado() + ").");

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
        // decisão vive em OrgFunction.validarCompatibilidadeComCargo.
        if (functionId != null) {
            functionRepository.findById(FunctionId.from(functionId))
                    .orElseThrow(() -> IgrpResponseStatusException.notFound(
                            "Função não encontrada: " + functionId))
                    .validarCompatibilidadeComCargo(position.getJobId());
        }

        return position;
    }

    /** Resultado de uma progressão: a nova afectação e os escalões de partida e de chegada. */
    public record Progressao(Assignment afectacao, Grade escalaoAnterior, Grade escalaoNovo) {}

    /**
     * Resultado de uma promoção: a nova afectação, as categorias de partida e de chegada, o
     * escalão atribuído e se o Lugar foi reclassificado (promoção na própria cadeira) ou se
     * houve mudança de Lugar.
     */
    public record Promocao(Assignment afectacao, Category categoriaAnterior, Category categoriaNova,
                           Grade escalao, boolean lugarReclassificado) {}

    /**
     * Promoção (evolução horizontal por mudança de categoria — Lei 20/X/2023, art. 140.º n.º 4).
     * Duas formas, inferidas do pedido e nunca persistidas — deduzem-se do histórico comparando
     * o Lugar da afectação anterior com o da nova:
     * <ul>
     *   <li>{@code positionIdDestino != null} — a pessoa muda para um Lugar vago da categoria de
     *       destino; o Lugar antigo fica vago.</li>
     *   <li>{@code positionIdDestino == null} — a pessoa fica na mesma cadeira e é o <b>Lugar que
     *       sobe de categoria</b> (reclassificação), ficando nela depois de o ocupante sair.</li>
     * </ul>
     */
    public Promocao promover(FuncionarioId funcionarioId, UUID categoryIdDestino, UUID positionIdDestino,
                             UUID gradeIdEscolhido, LocalDate dataEfeito, String notes) {

        Assignment atual = assignmentRepository.findCurrentPrincipalByFuncionario(funcionarioId)
                .orElseThrow(() -> IgrpResponseStatusException.of(org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY,
                        "O colaborador não tem afectação principal corrente — não é possível promover."));

        if (!dataEfeito.isAfter(atual.getDataInicio()))
            throw IgrpResponseStatusException.of(org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY,
                    "A data de efeito tem de ser posterior ao início da afectação corrente ("
                            + atual.getDataInicio() + ").");

        Position lugarActual = positionRepository.findById(PositionId.from(atual.getPositionId()))
                .orElseThrow(() -> IgrpResponseStatusException.notFound(
                        "Lugar não encontrado: " + atual.getPositionId()));

        if (lugarActual.isForaDeGrelha())
            throw IgrpResponseStatusException.of(org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY,
                    "O Lugar '" + lugarActual.getNumeroLugar()
                            + "' está fora da grelha (sem carreira/categoria) — não há categoria de onde promover.");

        Category categoriaAtual = categoriaOuFalha(lugarActual.getCategoryId());
        Category categoriaDestino = categoriaOuFalha(categoryIdDestino);

        if (!Boolean.TRUE.equals(categoriaDestino.getIsActive()))
            throw IgrpResponseStatusException.of(org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY,
                    "A categoria de destino '" + categoriaDestino.getName() + "' está inactiva.");

        if (!categoriaDestino.getCareerId().equals(categoriaAtual.getCareerId()))
            throw IgrpResponseStatusException.of(org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY,
                    "A categoria de destino pertence a outra carreira — a promoção é dentro da mesma carreira.");

        if (categoriaAtual.getOrdemProgressao() == null || categoriaDestino.getOrdemProgressao() == null
                || categoriaDestino.getOrdemProgressao() != categoriaAtual.getOrdemProgressao() + 1)
            throw IgrpResponseStatusException.of(org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY,
                    "A promoção é para a categoria imediatamente superior: de '" + categoriaAtual.getName()
                            + "' a seguinte é a de ordem " + (categoriaAtual.getOrdemProgressao() == null
                            ? "(não definida)" : categoriaAtual.getOrdemProgressao() + 1) + ".");

        Grade escalao = escalaoDaPromocao(gradeIdEscolhido, categoriaDestino);

        boolean reclassificado = positionIdDestino == null;
        UUID positionIdFinal;

        if (reclassificado) {
            lugarActual.reclassificarPara(categoriaDestino.getCareerId().getValor(), categoryIdDestino);
            positionRepository.save(lugarActual);
            positionIdFinal = lugarActual.getId().getValor();
        } else {
            Position destino = positionRepository.findById(PositionId.from(positionIdDestino))
                    .orElseThrow(() -> IgrpResponseStatusException.notFound(
                            "Lugar de destino não encontrado: " + positionIdDestino));

            if (!destino.podeSerOcupado())
                throw IgrpResponseStatusException.of(org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY,
                        "O Lugar '" + destino.getNumeroLugar() + "' não está disponível (estado="
                                + destino.getEstado() + ").");

            if (assignmentRepository.temTitular(positionIdDestino))
                throw IgrpResponseStatusException.of(org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY,
                        "O Lugar '" + destino.getNumeroLugar() + "' já tem titular.");

            if (!categoryIdDestino.equals(destino.getCategoryId()))
                throw IgrpResponseStatusException.of(org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY,
                        "O Lugar '" + destino.getNumeroLugar() + "' não é da categoria de destino '"
                                + categoriaDestino.getName() + "'.");

            positionIdFinal = positionIdDestino;
        }

        atual.encerrar(dataEfeito.minusDays(1));
        assignmentRepository.save(atual);

        Assignment nova = assignmentRepository.save(Assignment.criar(
                funcionarioId, positionIdFinal, escalao.getId().getValor(), atual.getFunctionId(),
                TipoAfectacao.PRINCIPAL, Assignment.PROMOCAO, dataEfeito, null, notes));

        return new Promocao(nova, categoriaAtual, categoriaDestino, escalao, reclassificado);
    }

    /** Resultado de uma transferência: a nova afectação e os Lugares de partida e de chegada. */
    public record Transferencia(Assignment afectacao, Position lugarAnterior, Position lugarNovo) {}

    /**
     * Transferência: mudança <b>definitiva</b> de Lugar sem subir na grelha. A pessoa mantém
     * carreira, categoria e escalão — muda de cadeira e, por consequência, de unidade orgânica
     * (que é derivada do Lugar). Se a categoria mudasse seria uma promoção, não uma transferência.
     *
     * <p>A função exercida acompanha a pessoa quando é compatível com o cargo do Lugar de destino;
     * quando não é, exige-se que seja indicada, em vez de se perder em silêncio.
     */
    public Transferencia transferir(FuncionarioId funcionarioId, UUID positionIdDestino, UUID functionIdEscolhido,
                                    LocalDate dataEfeito, String notes) {

        Assignment atual = assignmentRepository.findCurrentPrincipalByFuncionario(funcionarioId)
                .orElseThrow(() -> IgrpResponseStatusException.of(org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY,
                        "O colaborador não tem afectação principal corrente — não é possível transferir."));

        if (!dataEfeito.isAfter(atual.getDataInicio()))
            throw IgrpResponseStatusException.of(org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY,
                    "A data de efeito tem de ser posterior ao início da afectação corrente ("
                            + atual.getDataInicio() + ").");

        Position origem = positionRepository.findById(PositionId.from(atual.getPositionId()))
                .orElseThrow(() -> IgrpResponseStatusException.notFound(
                        "Lugar não encontrado: " + atual.getPositionId()));

        if (positionIdDestino.equals(atual.getPositionId()))
            throw IgrpResponseStatusException.of(org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY,
                    "O Lugar de destino é o mesmo em que o colaborador já está.");

        Position destino = positionRepository.findById(PositionId.from(positionIdDestino))
                .orElseThrow(() -> IgrpResponseStatusException.notFound(
                        "Lugar de destino não encontrado: " + positionIdDestino));

        if (!destino.podeSerOcupado())
            throw IgrpResponseStatusException.of(org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY,
                    "O Lugar '" + destino.getNumeroLugar() + "' não está disponível (estado="
                            + destino.getEstado() + ").");

        if (assignmentRepository.temTitular(positionIdDestino))
            throw IgrpResponseStatusException.of(org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY,
                    "O Lugar '" + destino.getNumeroLugar() + "' já tem titular.");

        // A transferência não mexe na grelha: mesma carreira, mesma categoria, mesmo escalão.
        if (origem.isForaDeGrelha() != destino.isForaDeGrelha()
                || !java.util.Objects.equals(origem.getCareerId(), destino.getCareerId())
                || !java.util.Objects.equals(origem.getCategoryId(), destino.getCategoryId()))
            throw IgrpResponseStatusException.of(org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY,
                    "A transferência mantém a posição na grelha: o Lugar '" + destino.getNumeroLugar()
                            + "' tem carreira/categoria diferente do Lugar actual. Para subir de categoria use a promoção.");

        UUID functionId = funcaoDaTransferencia(atual.getFunctionId(), functionIdEscolhido, destino);

        atual.encerrar(dataEfeito.minusDays(1));
        assignmentRepository.save(atual);

        Assignment nova = assignmentRepository.save(Assignment.criar(
                funcionarioId, positionIdDestino, atual.getGradeId(), functionId,
                TipoAfectacao.PRINCIPAL, Assignment.TRANSFERENCIA, dataEfeito, null, notes));

        return new Transferencia(nova, origem, destino);
    }

    /**
     * Função da afectação de destino: a escolhida (validada contra o cargo do Lugar) ou a actual,
     * se continuar compatível. Uma função actual incompatível com o novo cargo é um erro explícito
     * — não se deixa cair em silêncio.
     */
    private UUID funcaoDaTransferencia(UUID functionIdActual, UUID functionIdEscolhido, Position destino) {
        if (functionIdEscolhido != null) {
            functionRepository.findById(FunctionId.from(functionIdEscolhido))
                    .orElseThrow(() -> IgrpResponseStatusException.notFound(
                            "Função não encontrada: " + functionIdEscolhido))
                    .validarCompatibilidadeComCargo(destino.getJobId());
            return functionIdEscolhido;
        }

        if (functionIdActual == null) return null;

        var funcaoActual = functionRepository.findById(FunctionId.from(functionIdActual))
                .orElseThrow(() -> IgrpResponseStatusException.notFound(
                        "Função não encontrada: " + functionIdActual));

        if (!funcaoActual.isCompativelComCargo(destino.getJobId()))
            throw IgrpResponseStatusException.of(org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY,
                    "A função actual '" + funcaoActual.getName() + "' não pertence ao cargo do Lugar '"
                            + destino.getNumeroLugar() + "' — indique a função (functionId) a exercer no destino.");

        return functionIdActual;
    }

    /** Escalão da promoção: o escolhido (tem de ser da categoria de destino) ou o primeiro activo. */
    private Grade escalaoDaPromocao(UUID gradeIdEscolhido, Category categoriaDestino) {
        CategoryId destinoId = categoriaDestino.getId();

        if (gradeIdEscolhido != null) {
            Grade escolhido = gradeRepository.findById(GradeId.from(gradeIdEscolhido))
                    .orElseThrow(() -> IgrpResponseStatusException.notFound(
                            "Escalão não encontrado: " + gradeIdEscolhido));
            if (!escolhido.getCategoryId().equals(destinoId))
                throw IgrpResponseStatusException.of(org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY,
                        "O escalão '" + escolhido.getName() + "' não pertence à categoria de destino '"
                                + categoriaDestino.getName() + "'.");
            return escolhido;
        }

        return gradeRepository.findByCategoryIdOrderByGradeNumber(destinoId).stream()
                .filter(g -> Boolean.TRUE.equals(g.getIsActive()))
                .findFirst()
                .orElseThrow(() -> IgrpResponseStatusException.of(org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY,
                        "A categoria '" + categoriaDestino.getName() + "' não tem escalões activos."));
    }

    private Category categoriaOuFalha(UUID categoryId) {
        if (categoryId == null)
            throw IgrpResponseStatusException.badRequest("A categoria é obrigatória.");
        return categoryRepository.findById(CategoryId.from(categoryId))
                .orElseThrow(() -> IgrpResponseStatusException.notFound("Categoria não encontrada: " + categoryId));
    }

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
                atual.getFunctionId(), TipoAfectacao.PRINCIPAL, Assignment.PROGRESSAO,
                dataEfeito, null, notes));

        return new Progressao(nova, escalaoAtual, escalaoSeguinte);
    }

    /**
     * Encerra a afectação PRINCIPAL corrente do colaborador (cessação/reforma).
     * O Lugar volta a estar VAGO (derivado). Idempotente: se não houver afectação
     * corrente, não faz nada.
     */
    public Optional<Assignment> encerrarAfectacaoCorrente(FuncionarioId funcionarioId, LocalDate dataFim) {
        return assignmentRepository.findCurrentPrincipalByFuncionario(funcionarioId)
                .map(a -> {
                    a.encerrar(dataFim);
                    return assignmentRepository.save(a);
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
