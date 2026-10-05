package cv.igrp.RH_Service.colaboradores.application.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cv.igrp.RH_Service.carreiras.domain.models.Career;
import cv.igrp.RH_Service.carreiras.domain.models.Category;
import cv.igrp.RH_Service.carreiras.domain.models.Grade;
import cv.igrp.RH_Service.carreiras.domain.repository.CareerRepository;
import cv.igrp.RH_Service.carreiras.domain.repository.CategoryRepository;
import cv.igrp.RH_Service.carreiras.domain.repository.GradeRepository;
import cv.igrp.RH_Service.carreiras.domain.valueobject.CareerId;
import cv.igrp.RH_Service.carreiras.domain.valueobject.CategoryId;
import cv.igrp.RH_Service.carreiras.domain.valueobject.GradeId;
import cv.igrp.RH_Service.colaboradores.domain.models.Assignment;
import cv.igrp.RH_Service.colaboradores.domain.models.TipoAfectacao;
import cv.igrp.RH_Service.colaboradores.domain.repository.AssignmentRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.AssignmentId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.estrutura.domain.models.OrgFunction;
import cv.igrp.RH_Service.estrutura.domain.models.Position;
import cv.igrp.RH_Service.estrutura.domain.repository.FunctionRepository;
import cv.igrp.RH_Service.estrutura.domain.repository.PositionRepository;
import cv.igrp.RH_Service.estrutura.domain.valueobject.FunctionId;
import cv.igrp.RH_Service.estrutura.domain.valueobject.PositionId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Prova de {@link AssignmentService#mudarCarreira}: o colaborador passa a ocupar um Lugar vago
 * de <b>outra carreira</b>.
 *
 * <p>O que estes testes fixam, e que é o cerne do movimento: a carreira <b>tem</b> de mudar —
 * é isso que o separa da promoção (mesma carreira, categoria acima) e da transferência (mesma
 * posição na grelha, outra cadeira). Sem essa guarda, este caminho seria uma promoção sem
 * nenhuma das suas regras.
 */
@ExtendWith(MockitoExtension.class)
class AssignmentServiceMudancaCarreiraTest {

    @Mock private AssignmentRepository assignmentRepository;
    @Mock private PositionRepository positionRepository;
    @Mock private GradeRepository gradeRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private CareerRepository careerRepository;
    @Mock private FunctionRepository functionRepository;

    @org.mockito.Mock private cv.igrp.RH_Service.colaboradores.application.services.DiarioFactos diarioFactos;
    @Mock private cv.igrp.RH_Service.colaboradores.domain.repository.ReservaLugarRepository reservaLugarRepository;
    @InjectMocks private AssignmentService service;

    private final FuncionarioId funcionarioId = FuncionarioId.gerarNovo();
    private final UUID origemId = UUID.randomUUID();
    private final UUID destinoId = UUID.randomUUID();
    private final UUID jobOrigem = UUID.randomUUID();
    private final UUID jobDestino = UUID.randomUUID();
    private final LocalDate inicio = LocalDate.of(2024, 1, 1);
    private final LocalDate dataEfeito = LocalDate.of(2026, 10, 1);

    private final CareerId carreiraActual = CareerId.gerarNovo();
    private final CareerId carreiraNova = CareerId.gerarNovo();
    private final CategoryId categoriaActual = CategoryId.gerarNovo();
    private final CategoryId categoriaNova = CategoryId.gerarNovo();
    private final UUID gradeActual = UUID.randomUUID();

    private Assignment afectacaoActual(UUID functionId) {
        return Assignment.reconstituir(AssignmentId.gerarNovo(), funcionarioId, origemId, gradeActual,
                functionId, TipoAfectacao.PRINCIPAL.name(), Assignment.ADMISSAO, null, inicio, null,
                true, true, null);
    }

    private Position lugar(UUID id, UUID jobId, CareerId career, CategoryId category, String estado) {
        return Position.reconstituir(PositionId.from(id), "L-" + id.toString().substring(0, 4), jobId,
                UUID.randomUUID(), career == null ? null : career.getValor(),
                category == null ? null : category.getValor(), null, null, estado, null, true);
    }

    private Career carreira(CareerId id, String nome, boolean activa) {
        return Career.reconstituir(id, "C-" + nome, nome, nome, "GERAL", activa);
    }

    private Category categoria(CategoryId id, CareerId careerId, String nome, boolean activa) {
        return Category.reconstituir(id, careerId, "K-" + nome, nome, nome, 1, activa);
    }

    private Grade escalao(CategoryId categoryId, int numero, boolean activo) {
        return Grade.reconstituir(GradeId.gerarNovo(), categoryId, numero, "E" + numero, "Escalão " + numero,
                BigDecimal.ONE, BigDecimal.TEN, activo);
    }

    /** Cenário completo e válido: destino vago noutra carreira, com escalões activos. */
    private Grade cenarioValido() {
        when(assignmentRepository.findCurrentPrincipalByFuncionario(funcionarioId))
                .thenReturn(Optional.of(afectacaoActual(null)));
        when(positionRepository.findById(PositionId.from(origemId)))
                .thenReturn(Optional.of(lugar(origemId, jobOrigem, carreiraActual, categoriaActual, Position.ATIVO)));
        when(positionRepository.findById(PositionId.from(destinoId)))
                .thenReturn(Optional.of(lugar(destinoId, jobDestino, carreiraNova, categoriaNova, Position.ATIVO)));
        when(assignmentRepository.temTitular(destinoId)).thenReturn(false);
        when(careerRepository.findById(carreiraActual))
                .thenReturn(Optional.of(carreira(carreiraActual, "Assistente Técnico", true)));
        when(careerRepository.findById(carreiraNova))
                .thenReturn(Optional.of(carreira(carreiraNova, "Técnico Superior", true)));
        when(categoryRepository.findById(categoriaActual))
                .thenReturn(Optional.of(categoria(categoriaActual, carreiraActual, "AT1", true)));
        when(categoryRepository.findById(categoriaNova))
                .thenReturn(Optional.of(categoria(categoriaNova, carreiraNova, "TS1", true)));
        Grade primeiro = escalao(categoriaNova, 1, true);
        // Lenient porque quem indica o escalão não chega a consultar a lista da categoria.
        lenient().when(gradeRepository.findByCategoryIdOrderByGradeNumber(categoriaNova))
                .thenReturn(List.of(primeiro, escalao(categoriaNova, 2, true)));
        when(assignmentRepository.save(any(Assignment.class))).thenAnswer(inv -> inv.getArgument(0));
        return primeiro;
    }

    @Test
    void mudaDeCarreiraFechandoAAfectacaoAnteriorNaVespera() {
        Grade primeiro = cenarioValido();

        var resultado = service.mudarCarreira(funcionarioId, destinoId, null, null, dataEfeito, "Mudança");

        ArgumentCaptor<Assignment> captor = ArgumentCaptor.forClass(Assignment.class);
        verify(assignmentRepository, times(2)).save(captor.capture());
        Assignment fechada = captor.getAllValues().get(0);
        Assignment nova = captor.getAllValues().get(1);

        assertFalse(fechada.getIsCurrent());
        assertEquals(dataEfeito.minusDays(1), fechada.getDataFim());
        assertEquals(destinoId, nova.getPositionId());
        assertEquals(Assignment.MUDANCA_CARREIRA, nova.getOrigem());
        assertEquals(TipoAfectacao.PRINCIPAL, nova.getAssignmentType());
        assertEquals(carreiraActual.getValor(), resultado.carreiraAnterior().getId().getValor());
        assertEquals(carreiraNova.getValor(), resultado.carreiraNova().getId().getValor());
        assertEquals(origemId, resultado.lugarAnterior().getId().getValor());
        assertEquals(destinoId, resultado.lugarNovo().getId().getValor());
        assertEquals(primeiro.getId().getValor(), resultado.escalao().getId().getValor());
    }

    /**
     * O escalão não se herda da carreira de origem: pertence à categoria do destino. Na falta de
     * indicação, entra-se pelo primeiro escalão activo — quem posiciona é o acto administrativo.
     */
    @Test
    void naoHerdaOEscalaoDaCarreiraDeOrigem() {
        Grade primeiro = cenarioValido();

        service.mudarCarreira(funcionarioId, destinoId, null, null, dataEfeito, null);

        ArgumentCaptor<Assignment> captor = ArgumentCaptor.forClass(Assignment.class);
        verify(assignmentRepository, times(2)).save(captor.capture());
        Assignment nova = captor.getAllValues().get(1);
        assertEquals(primeiro.getId().getValor(), nova.getGradeId());
        assertFalse(gradeActual.equals(nova.getGradeId()));
    }

    @Test
    void aceitaOEscalaoIndicadoQuandoPertenceACategoriaDeDestino() {
        cenarioValido();
        Grade escolhido = escalao(categoriaNova, 3, true);
        when(gradeRepository.findById(escolhido.getId())).thenReturn(Optional.of(escolhido));

        service.mudarCarreira(funcionarioId, destinoId, escolhido.getId().getValor(), null, dataEfeito, null);

        ArgumentCaptor<Assignment> captor = ArgumentCaptor.forClass(Assignment.class);
        verify(assignmentRepository, times(2)).save(captor.capture());
        assertEquals(escolhido.getId().getValor(), captor.getAllValues().get(1).getGradeId());
    }

    @Test
    void recusaEscalaoQueNaoPertenceACategoriaDeDestino() {
        when(assignmentRepository.findCurrentPrincipalByFuncionario(funcionarioId))
                .thenReturn(Optional.of(afectacaoActual(null)));
        when(positionRepository.findById(PositionId.from(origemId)))
                .thenReturn(Optional.of(lugar(origemId, jobOrigem, carreiraActual, categoriaActual, Position.ATIVO)));
        when(positionRepository.findById(PositionId.from(destinoId)))
                .thenReturn(Optional.of(lugar(destinoId, jobDestino, carreiraNova, categoriaNova, Position.ATIVO)));
        when(assignmentRepository.temTitular(destinoId)).thenReturn(false);
        when(careerRepository.findById(carreiraActual))
                .thenReturn(Optional.of(carreira(carreiraActual, "Assistente Técnico", true)));
        when(careerRepository.findById(carreiraNova))
                .thenReturn(Optional.of(carreira(carreiraNova, "Técnico Superior", true)));
        when(categoryRepository.findById(categoriaActual))
                .thenReturn(Optional.of(categoria(categoriaActual, carreiraActual, "AT1", true)));
        when(categoryRepository.findById(categoriaNova))
                .thenReturn(Optional.of(categoria(categoriaNova, carreiraNova, "TS1", true)));
        Grade doutraCategoria = escalao(categoriaActual, 4, true);
        when(gradeRepository.findById(doutraCategoria.getId())).thenReturn(Optional.of(doutraCategoria));

        var ex = assertThrows(IgrpResponseStatusException.class,
                () -> service.mudarCarreira(funcionarioId, destinoId, doutraCategoria.getId().getValor(),
                        null, dataEfeito, null));

        assertEquals(422, ex.getStatusCode().value());
        verify(assignmentRepository, never()).save(any());
    }

    /** A guarda que separa este movimento da promoção e da transferência. */
    @Test
    void recusaLugarDeDestinoDaMesmaCarreira() {
        when(assignmentRepository.findCurrentPrincipalByFuncionario(funcionarioId))
                .thenReturn(Optional.of(afectacaoActual(null)));
        when(positionRepository.findById(PositionId.from(origemId)))
                .thenReturn(Optional.of(lugar(origemId, jobOrigem, carreiraActual, categoriaActual, Position.ATIVO)));
        when(positionRepository.findById(PositionId.from(destinoId)))
                .thenReturn(Optional.of(lugar(destinoId, jobDestino, carreiraActual, categoriaNova, Position.ATIVO)));
        when(assignmentRepository.temTitular(destinoId)).thenReturn(false);

        var ex = assertThrows(IgrpResponseStatusException.class,
                () -> service.mudarCarreira(funcionarioId, destinoId, null, null, dataEfeito, null));

        assertEquals(422, ex.getStatusCode().value());
        verify(assignmentRepository, never()).save(any());
    }

    @Test
    void recusaCarreiraDeDestinoInactiva() {
        when(assignmentRepository.findCurrentPrincipalByFuncionario(funcionarioId))
                .thenReturn(Optional.of(afectacaoActual(null)));
        when(positionRepository.findById(PositionId.from(origemId)))
                .thenReturn(Optional.of(lugar(origemId, jobOrigem, carreiraActual, categoriaActual, Position.ATIVO)));
        when(positionRepository.findById(PositionId.from(destinoId)))
                .thenReturn(Optional.of(lugar(destinoId, jobDestino, carreiraNova, categoriaNova, Position.ATIVO)));
        when(assignmentRepository.temTitular(destinoId)).thenReturn(false);
        when(careerRepository.findById(carreiraActual))
                .thenReturn(Optional.of(carreira(carreiraActual, "Assistente Técnico", true)));
        when(careerRepository.findById(carreiraNova))
                .thenReturn(Optional.of(carreira(carreiraNova, "Técnico Superior", false)));

        var ex = assertThrows(IgrpResponseStatusException.class,
                () -> service.mudarCarreira(funcionarioId, destinoId, null, null, dataEfeito, null));

        assertEquals(422, ex.getStatusCode().value());
        verify(assignmentRepository, never()).save(any());
    }

    @Test
    void recusaLugarDeDestinoOcupado() {
        when(assignmentRepository.findCurrentPrincipalByFuncionario(funcionarioId))
                .thenReturn(Optional.of(afectacaoActual(null)));
        when(positionRepository.findById(PositionId.from(origemId)))
                .thenReturn(Optional.of(lugar(origemId, jobOrigem, carreiraActual, categoriaActual, Position.ATIVO)));
        when(positionRepository.findById(PositionId.from(destinoId)))
                .thenReturn(Optional.of(lugar(destinoId, jobDestino, carreiraNova, categoriaNova, Position.ATIVO)));
        when(assignmentRepository.temTitular(destinoId)).thenReturn(true);

        var ex = assertThrows(IgrpResponseStatusException.class,
                () -> service.mudarCarreira(funcionarioId, destinoId, null, null, dataEfeito, null));

        assertEquals(422, ex.getStatusCode().value());
        verify(assignmentRepository, never()).save(any());
    }

    @Test
    void recusaLugarDeDestinoCongelado() {
        when(assignmentRepository.findCurrentPrincipalByFuncionario(funcionarioId))
                .thenReturn(Optional.of(afectacaoActual(null)));
        when(positionRepository.findById(PositionId.from(origemId)))
                .thenReturn(Optional.of(lugar(origemId, jobOrigem, carreiraActual, categoriaActual, Position.ATIVO)));
        when(positionRepository.findById(PositionId.from(destinoId)))
                .thenReturn(Optional.of(lugar(destinoId, jobDestino, carreiraNova, categoriaNova, Position.CONGELADO)));

        var ex = assertThrows(IgrpResponseStatusException.class,
                () -> service.mudarCarreira(funcionarioId, destinoId, null, null, dataEfeito, null));

        assertEquals(422, ex.getStatusCode().value());
        verify(assignmentRepository, never()).save(any());
    }

    /** Fora da grelha não há carreira: nem de onde sair, nem para onde ir. */
    @Test
    void recusaQuandoOLugarActualEstaForaDaGrelha() {
        when(assignmentRepository.findCurrentPrincipalByFuncionario(funcionarioId))
                .thenReturn(Optional.of(afectacaoActual(null)));
        when(positionRepository.findById(PositionId.from(origemId)))
                .thenReturn(Optional.of(lugar(origemId, jobOrigem, null, null, Position.ATIVO)));

        var ex = assertThrows(IgrpResponseStatusException.class,
                () -> service.mudarCarreira(funcionarioId, destinoId, null, null, dataEfeito, null));

        assertEquals(422, ex.getStatusCode().value());
        verify(assignmentRepository, never()).save(any());
    }

    @Test
    void recusaQuandoOLugarDeDestinoEstaForaDaGrelha() {
        when(assignmentRepository.findCurrentPrincipalByFuncionario(funcionarioId))
                .thenReturn(Optional.of(afectacaoActual(null)));
        when(positionRepository.findById(PositionId.from(origemId)))
                .thenReturn(Optional.of(lugar(origemId, jobOrigem, carreiraActual, categoriaActual, Position.ATIVO)));
        when(positionRepository.findById(PositionId.from(destinoId)))
                .thenReturn(Optional.of(lugar(destinoId, jobDestino, null, null, Position.ATIVO)));

        var ex = assertThrows(IgrpResponseStatusException.class,
                () -> service.mudarCarreira(funcionarioId, destinoId, null, null, dataEfeito, null));

        assertEquals(422, ex.getStatusCode().value());
        verify(assignmentRepository, never()).save(any());
    }

    @Test
    void recusaMudancaParaOMesmoLugar() {
        when(assignmentRepository.findCurrentPrincipalByFuncionario(funcionarioId))
                .thenReturn(Optional.of(afectacaoActual(null)));
        when(positionRepository.findById(PositionId.from(origemId)))
                .thenReturn(Optional.of(lugar(origemId, jobOrigem, carreiraActual, categoriaActual, Position.ATIVO)));

        var ex = assertThrows(IgrpResponseStatusException.class,
                () -> service.mudarCarreira(funcionarioId, origemId, null, null, dataEfeito, null));

        assertEquals(422, ex.getStatusCode().value());
        verify(assignmentRepository, never()).save(any());
    }

    @Test
    void recusaDataDeEfeitoNaoPosteriorAoInicioDaAfectacao() {
        when(assignmentRepository.findCurrentPrincipalByFuncionario(funcionarioId))
                .thenReturn(Optional.of(afectacaoActual(null)));

        var ex = assertThrows(IgrpResponseStatusException.class,
                () -> service.mudarCarreira(funcionarioId, destinoId, null, null, inicio, null));

        assertEquals(422, ex.getStatusCode().value());
        verify(assignmentRepository, never()).save(any());
    }

    @Test
    void recusaSemAfectacaoCorrente() {
        when(assignmentRepository.findCurrentPrincipalByFuncionario(funcionarioId)).thenReturn(Optional.empty());

        var ex = assertThrows(IgrpResponseStatusException.class,
                () -> service.mudarCarreira(funcionarioId, destinoId, null, null, dataEfeito, null));

        assertEquals(422, ex.getStatusCode().value());
    }

    /** A função acompanha a pessoa quando serve o cargo do destino — como na transferência. */
    @Test
    void exigeFuncaoQuandoAActualNaoServeOCargoDoDestino() {
        UUID functionId = UUID.randomUUID();
        when(assignmentRepository.findCurrentPrincipalByFuncionario(funcionarioId))
                .thenReturn(Optional.of(afectacaoActual(functionId)));
        when(positionRepository.findById(PositionId.from(origemId)))
                .thenReturn(Optional.of(lugar(origemId, jobOrigem, carreiraActual, categoriaActual, Position.ATIVO)));
        when(positionRepository.findById(PositionId.from(destinoId)))
                .thenReturn(Optional.of(lugar(destinoId, jobDestino, carreiraNova, categoriaNova, Position.ATIVO)));
        when(assignmentRepository.temTitular(destinoId)).thenReturn(false);
        when(careerRepository.findById(carreiraActual))
                .thenReturn(Optional.of(carreira(carreiraActual, "Assistente Técnico", true)));
        when(careerRepository.findById(carreiraNova))
                .thenReturn(Optional.of(carreira(carreiraNova, "Técnico Superior", true)));
        when(categoryRepository.findById(categoriaActual))
                .thenReturn(Optional.of(categoria(categoriaActual, carreiraActual, "AT1", true)));
        when(categoryRepository.findById(categoriaNova))
                .thenReturn(Optional.of(categoria(categoriaNova, carreiraNova, "TS1", true)));
        when(gradeRepository.findByCategoryIdOrderByGradeNumber(categoriaNova))
                .thenReturn(List.of(escalao(categoriaNova, 1, true)));
        when(functionRepository.findById(FunctionId.from(functionId)))
                .thenReturn(Optional.of(OrgFunction.reconstruir(FunctionId.from(functionId), "F1",
                        "Função do cargo de origem", null, jobOrigem, true)));

        var ex = assertThrows(IgrpResponseStatusException.class,
                () -> service.mudarCarreira(funcionarioId, destinoId, null, null, dataEfeito, null));

        assertEquals(422, ex.getStatusCode().value());
        verify(assignmentRepository, never()).save(any());
    }
}
