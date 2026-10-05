package cv.igrp.RH_Service.colaboradores.application.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cv.igrp.RH_Service.carreiras.domain.models.Category;
import cv.igrp.RH_Service.carreiras.domain.models.Grade;
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
import cv.igrp.RH_Service.estrutura.domain.models.Position;
import cv.igrp.RH_Service.estrutura.domain.repository.FunctionRepository;
import cv.igrp.RH_Service.estrutura.domain.repository.PositionRepository;
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
 * Prova de {@link AssignmentService#promover}: as duas formas da promoção (mudar de Lugar ou
 * reclassificar o Lugar actual), inferidas da presença do Lugar de destino, e as regras da
 * grelha (mesma carreira, categoria imediatamente superior, escalão da categoria de destino).
 */
@ExtendWith(MockitoExtension.class)
class AssignmentServicePromocaoTest {

    @Mock private AssignmentRepository assignmentRepository;
    @Mock private PositionRepository positionRepository;
    @Mock private GradeRepository gradeRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private FunctionRepository functionRepository;

    @org.mockito.Mock private cv.igrp.RH_Service.colaboradores.application.services.DiarioFactos diarioFactos;
    @Mock private cv.igrp.RH_Service.colaboradores.domain.repository.ReservaLugarRepository reservaLugarRepository;
    @InjectMocks private AssignmentService service;

    private final FuncionarioId funcionarioId = FuncionarioId.gerarNovo();
    private final CareerId carreira = CareerId.gerarNovo();
    private final UUID positionActualId = UUID.randomUUID();
    private final UUID positionDestinoId = UUID.randomUUID();
    private final LocalDate inicio = LocalDate.of(2024, 1, 1);
    private final LocalDate dataEfeito = LocalDate.of(2026, 10, 1);

    private final Category catActual = Category.reconstituir(CategoryId.gerarNovo(), carreira,
            "TEC", "Técnico", null, 1, true);
    private final Category catDestino = Category.reconstituir(CategoryId.gerarNovo(), carreira,
            "TEC_SUP", "Técnico Superior", null, 2, true);

    private Grade escalao(CategoryId categoryId, int numero, boolean activo) {
        return Grade.reconstituir(GradeId.gerarNovo(), categoryId, numero, "E" + numero,
                "Escalão " + numero, BigDecimal.valueOf(100 + numero), null, activo);
    }

    private Assignment afectacaoActual() {
        return Assignment.reconstituir(AssignmentId.gerarNovo(), funcionarioId, positionActualId,
                escalao(catActual.getId(), 2, true).getId().getValor(), UUID.randomUUID(),
                TipoAfectacao.PRINCIPAL.name(), Assignment.ADMISSAO, null, inicio, null, true, true, null);
    }

    private Position lugar(UUID id, CategoryId categoryId, String estado) {
        return Position.reconstituir(PositionId.from(id), "L-" + id.toString().substring(0, 4),
                UUID.randomUUID(), UUID.randomUUID(), carreira.getValor(),
                categoryId == null ? null : categoryId.getValor(), null, null, estado, null, true);
    }

    private void cenarioBase() {
        when(assignmentRepository.findCurrentPrincipalByFuncionario(funcionarioId))
                .thenReturn(Optional.of(afectacaoActual()));
        when(positionRepository.findById(PositionId.from(positionActualId)))
                .thenReturn(Optional.of(lugar(positionActualId, catActual.getId(), Position.ATIVO)));
        when(categoryRepository.findById(catActual.getId())).thenReturn(Optional.of(catActual));
        when(categoryRepository.findById(catDestino.getId())).thenReturn(Optional.of(catDestino));
    }

    /** Só para os casos que chegam a gravar — evita stubs desnecessários nos que falham antes. */
    private void gravaAfectacoes() {
        when(assignmentRepository.save(any(Assignment.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void semLugarDeDestinoReclassificaOLugarActual() {
        cenarioBase();
        gravaAfectacoes();
        Grade primeiroDestino = escalao(catDestino.getId(), 1, true);
        when(gradeRepository.findByCategoryIdOrderByGradeNumber(catDestino.getId()))
                .thenReturn(List.of(escalao(catDestino.getId(), 0, false), primeiroDestino));
        when(positionRepository.save(any(Position.class))).thenAnswer(inv -> inv.getArgument(0));

        var resultado = service.promover(funcionarioId, catDestino.getId().getValor(), null, null,
                dataEfeito, "Promoção");

        assertTrue(resultado.lugarReclassificado());
        ArgumentCaptor<Position> lugarCaptor = ArgumentCaptor.forClass(Position.class);
        verify(positionRepository).save(lugarCaptor.capture());
        assertEquals(catDestino.getId().getValor(), lugarCaptor.getValue().getCategoryId());

        ArgumentCaptor<Assignment> captor = ArgumentCaptor.forClass(Assignment.class);
        verify(assignmentRepository, times(2)).save(captor.capture());
        assertEquals(dataEfeito.minusDays(1), captor.getAllValues().get(0).getDataFim());
        Assignment nova = captor.getAllValues().get(1);
        assertEquals(positionActualId, nova.getPositionId());
        assertEquals(primeiroDestino.getId().getValor(), nova.getGradeId());
        assertEquals(Assignment.PROMOCAO, nova.getOrigem());
        assertEquals(catDestino, resultado.categoriaNova());
        assertEquals(catActual, resultado.categoriaAnterior());
    }

    @Test
    void comLugarDeDestinoVagoMudaDeLugarSemReclassificar() {
        cenarioBase();
        gravaAfectacoes();
        Grade escolhido = escalao(catDestino.getId(), 3, true);
        when(gradeRepository.findById(escolhido.getId())).thenReturn(Optional.of(escolhido));
        when(positionRepository.findById(PositionId.from(positionDestinoId)))
                .thenReturn(Optional.of(lugar(positionDestinoId, catDestino.getId(), Position.ATIVO)));
        when(assignmentRepository.temTitular(positionDestinoId)).thenReturn(false);

        var resultado = service.promover(funcionarioId, catDestino.getId().getValor(), positionDestinoId,
                escolhido.getId().getValor(), dataEfeito, "Promoção");

        assertFalse(resultado.lugarReclassificado());
        verify(positionRepository, never()).save(any());
        ArgumentCaptor<Assignment> captor = ArgumentCaptor.forClass(Assignment.class);
        verify(assignmentRepository, times(2)).save(captor.capture());
        Assignment nova = captor.getAllValues().get(1);
        assertEquals(positionDestinoId, nova.getPositionId());
        assertEquals(escolhido.getId().getValor(), nova.getGradeId());
    }

    @Test
    void recusaLugarDeDestinoJaOcupado() {
        cenarioBase();
        when(positionRepository.findById(PositionId.from(positionDestinoId)))
                .thenReturn(Optional.of(lugar(positionDestinoId, catDestino.getId(), Position.ATIVO)));
        when(assignmentRepository.temTitular(positionDestinoId)).thenReturn(true);
        Grade escolhido = escalao(catDestino.getId(), 1, true);
        when(gradeRepository.findById(escolhido.getId())).thenReturn(Optional.of(escolhido));

        var ex = assertThrows(IgrpResponseStatusException.class,
                () -> service.promover(funcionarioId, catDestino.getId().getValor(), positionDestinoId,
                        escolhido.getId().getValor(), dataEfeito, null));

        assertEquals(422, ex.getStatusCode().value());
        verify(assignmentRepository, never()).save(any());
    }

    @Test
    void recusaLugarDeDestinoDeOutraCategoria() {
        cenarioBase();
        when(positionRepository.findById(PositionId.from(positionDestinoId)))
                .thenReturn(Optional.of(lugar(positionDestinoId, catActual.getId(), Position.ATIVO)));
        when(assignmentRepository.temTitular(positionDestinoId)).thenReturn(false);
        Grade escolhido = escalao(catDestino.getId(), 1, true);
        when(gradeRepository.findById(escolhido.getId())).thenReturn(Optional.of(escolhido));

        var ex = assertThrows(IgrpResponseStatusException.class,
                () -> service.promover(funcionarioId, catDestino.getId().getValor(), positionDestinoId,
                        escolhido.getId().getValor(), dataEfeito, null));

        assertEquals(422, ex.getStatusCode().value());
        verify(assignmentRepository, never()).save(any());
    }

    @Test
    void recusaCategoriaQueNaoEAImediatamenteSuperior() {
        Category catSalto = Category.reconstituir(CategoryId.gerarNovo(), carreira, "TEC_ESP",
                "Técnico Especialista", null, 3, true);
        when(assignmentRepository.findCurrentPrincipalByFuncionario(funcionarioId))
                .thenReturn(Optional.of(afectacaoActual()));
        when(positionRepository.findById(PositionId.from(positionActualId)))
                .thenReturn(Optional.of(lugar(positionActualId, catActual.getId(), Position.ATIVO)));
        when(categoryRepository.findById(catActual.getId())).thenReturn(Optional.of(catActual));
        when(categoryRepository.findById(catSalto.getId())).thenReturn(Optional.of(catSalto));

        var ex = assertThrows(IgrpResponseStatusException.class,
                () -> service.promover(funcionarioId, catSalto.getId().getValor(), null, null, dataEfeito, null));

        assertEquals(422, ex.getStatusCode().value());
        verify(assignmentRepository, never()).save(any());
    }

    @Test
    void recusaCategoriaDeOutraCarreira() {
        Category outraCarreira = Category.reconstituir(CategoryId.gerarNovo(), CareerId.gerarNovo(),
                "ASS", "Assistente", null, 2, true);
        when(assignmentRepository.findCurrentPrincipalByFuncionario(funcionarioId))
                .thenReturn(Optional.of(afectacaoActual()));
        when(positionRepository.findById(PositionId.from(positionActualId)))
                .thenReturn(Optional.of(lugar(positionActualId, catActual.getId(), Position.ATIVO)));
        when(categoryRepository.findById(catActual.getId())).thenReturn(Optional.of(catActual));
        when(categoryRepository.findById(outraCarreira.getId())).thenReturn(Optional.of(outraCarreira));

        var ex = assertThrows(IgrpResponseStatusException.class,
                () -> service.promover(funcionarioId, outraCarreira.getId().getValor(), null, null,
                        dataEfeito, null));

        assertEquals(422, ex.getStatusCode().value());
        verify(assignmentRepository, never()).save(any());
    }

    @Test
    void recusaEscalaoQueNaoPertenceACategoriaDeDestino() {
        cenarioBase();
        Grade escalaoDaOutraCategoria = escalao(catActual.getId(), 1, true);
        when(gradeRepository.findById(escalaoDaOutraCategoria.getId()))
                .thenReturn(Optional.of(escalaoDaOutraCategoria));

        var ex = assertThrows(IgrpResponseStatusException.class,
                () -> service.promover(funcionarioId, catDestino.getId().getValor(), null,
                        escalaoDaOutraCategoria.getId().getValor(), dataEfeito, null));

        assertEquals(422, ex.getStatusCode().value());
        verify(assignmentRepository, never()).save(any());
    }

    @Test
    void recusaLugarActualForaDaGrelha() {
        when(assignmentRepository.findCurrentPrincipalByFuncionario(funcionarioId))
                .thenReturn(Optional.of(afectacaoActual()));
        when(positionRepository.findById(PositionId.from(positionActualId)))
                .thenReturn(Optional.of(Position.reconstituir(PositionId.from(positionActualId), "L-X",
                        UUID.randomUUID(), UUID.randomUUID(), null, null, null, null,
                        Position.ATIVO, null, true)));

        var ex = assertThrows(IgrpResponseStatusException.class,
                () -> service.promover(funcionarioId, catDestino.getId().getValor(), null, null, dataEfeito, null));

        assertEquals(422, ex.getStatusCode().value());
        verify(assignmentRepository, never()).save(any());
    }

    @Test
    void recusaDataDeEfeitoNaoPosteriorAoInicioDaAfectacao() {
        when(assignmentRepository.findCurrentPrincipalByFuncionario(funcionarioId))
                .thenReturn(Optional.of(afectacaoActual()));

        var ex = assertThrows(IgrpResponseStatusException.class,
                () -> service.promover(funcionarioId, catDestino.getId().getValor(), null, null, inicio, null));

        assertEquals(422, ex.getStatusCode().value());
        verify(assignmentRepository, never()).save(any());
    }
}
