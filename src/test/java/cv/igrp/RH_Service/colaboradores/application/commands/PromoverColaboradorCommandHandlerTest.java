package cv.igrp.RH_Service.colaboradores.application.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import cv.igrp.RH_Service.carreiras.domain.models.Category;
import cv.igrp.RH_Service.carreiras.domain.models.Grade;
import cv.igrp.RH_Service.carreiras.domain.valueobject.CareerId;
import cv.igrp.RH_Service.carreiras.domain.valueobject.CategoryId;
import cv.igrp.RH_Service.carreiras.domain.valueobject.GradeId;
import cv.igrp.RH_Service.colaboradores.application.dto.PromocaoRequestDTO;
import cv.igrp.RH_Service.colaboradores.application.services.AssignmentService;
import cv.igrp.RH_Service.colaboradores.application.services.VinculoLaboralService;
import cv.igrp.RH_Service.colaboradores.domain.models.Assignment;
import cv.igrp.RH_Service.colaboradores.domain.models.TipoAfectacao;
import cv.igrp.RH_Service.colaboradores.domain.models.Funcionario;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.parametrizacoes.domain.models.VinculoLaboral;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Prova de {@link PromoverColaboradorCommandHandler}: elegibilidade (colaborador activo,
 * vínculo) e a inferência da forma da promoção a partir da presença do {@code positionId}.
 * As regras da grelha estão provadas em {@code AssignmentServicePromocaoTest}.
 */
@ExtendWith(MockitoExtension.class)
class PromoverColaboradorCommandHandlerTest {

    @Mock private FuncionarioRepository funcionarioRepository;
    @Mock private VinculoLaboralService vinculoLaboralService;
    @Mock private AssignmentService assignmentService;

    @InjectMocks private PromoverColaboradorCommandHandler handler;

    private final UUID funcionarioUuid = UUID.randomUUID();
    private final FuncionarioId funcionarioId = FuncionarioId.from(funcionarioUuid);
    private final UUID categoryId = UUID.randomUUID();
    private final UUID positionId = UUID.randomUUID();
    private final LocalDate dataEfeito = LocalDate.of(2026, 10, 1);

    private PromoverColaboradorCommand command(String categoryId, String positionId, LocalDate data) {
        PromocaoRequestDTO dto = new PromocaoRequestDTO();
        dto.setCategoryId(categoryId);
        dto.setPositionId(positionId);
        dto.setDataEfeito(data);
        dto.setDespachoNumero("45/2026");
        dto.setConcursoRef("CI-7/2026");
        return new PromoverColaboradorCommand(funcionarioUuid.toString(), dto);
    }

    private void funcionarioActivo(boolean activo) {
        Funcionario funcionario = mock(Funcionario.class);
        when(funcionario.getIsActive()).thenReturn(activo);
        when(funcionarioRepository.findById(funcionarioId)).thenReturn(Optional.of(funcionario));
    }

    private void vinculoElegivel(boolean elegivel) {
        VinculoLaboral vinculo = mock(VinculoLaboral.class);
        when(vinculo.isEligibleForProgression()).thenReturn(elegivel);
        if (!elegivel) when(vinculo.getDescription()).thenReturn("Prestador de Serviços");
        when(vinculoLaboralService.doFuncionario(funcionarioId)).thenReturn(vinculo);
    }

    private static Category categoria(String nome, int ordem) {
        return Category.reconstituir(CategoryId.gerarNovo(), CareerId.gerarNovo(), "C" + ordem,
                nome, null, ordem, true);
    }

    private AssignmentService.Promocao resultado(UUID positionIdFinal, boolean reclassificado) {
        Assignment nova = Assignment.criar(funcionarioId, positionIdFinal, UUID.randomUUID(), null,
                TipoAfectacao.PRINCIPAL, Assignment.PROMOCAO, dataEfeito, null, null);
        Grade escalao = Grade.reconstituir(GradeId.gerarNovo(), CategoryId.from(categoryId), 1, "E1",
                "Escalão 1", BigDecimal.TEN, null, true);
        return new AssignmentService.Promocao(nova, categoria("Técnico", 1),
                categoria("Técnico Superior", 2), escalao, reclassificado);
    }

    @Test
    void semPositionIdPedeReclassificacaoDoLugarActual() {
        funcionarioActivo(true);
        vinculoElegivel(true);
        when(assignmentService.promover(eq(funcionarioId), eq(categoryId), isNull(), isNull(),
                eq(dataEfeito), any())).thenReturn(resultado(positionId, true));

        var response = handler.handle(command(categoryId.toString(), null, dataEfeito));

        assertEquals(201, response.getStatusCode().value());
        var body = response.getBody();
        assertTrue(body.getLugarReclassificado());
        assertEquals("Técnico", body.getCategoriaAnterior());
        assertEquals("Técnico Superior", body.getCategoriaNova());
        assertEquals("Escalão 1", body.getEscalao());
        assertEquals(dataEfeito, body.getDataEfeito());
        verify(assignmentService).promover(funcionarioId, categoryId, null, null, dataEfeito,
                "Promoção (despacho 45/2026) [concurso CI-7/2026]");
    }

    @Test
    void comPositionIdPedeMudancaDeLugar() {
        funcionarioActivo(true);
        vinculoElegivel(true);
        when(assignmentService.promover(eq(funcionarioId), eq(categoryId), eq(positionId), isNull(),
                eq(dataEfeito), any())).thenReturn(resultado(positionId, false));

        var response = handler.handle(command(categoryId.toString(), positionId.toString(), dataEfeito));

        assertEquals(201, response.getStatusCode().value());
        assertEquals(false, response.getBody().getLugarReclassificado());
        assertEquals(positionId.toString(), response.getBody().getPositionId());
    }

    @Test
    void recusaColaboradorInactivo() {
        funcionarioActivo(false);

        var ex = assertThrows(IgrpResponseStatusException.class,
                () -> handler.handle(command(categoryId.toString(), null, dataEfeito)));

        assertEquals(422, ex.getStatusCode().value());
        verifyNoInteractions(vinculoLaboralService, assignmentService);
    }

    @Test
    void recusaVinculoNaoElegivel() {
        funcionarioActivo(true);
        vinculoElegivel(false);

        var ex = assertThrows(IgrpResponseStatusException.class,
                () -> handler.handle(command(categoryId.toString(), null, dataEfeito)));

        assertEquals(422, ex.getStatusCode().value());
        verifyNoInteractions(assignmentService);
    }

    @Test
    void recusaPedidoSemCategoriaDeDestino() {
        var ex = assertThrows(IgrpResponseStatusException.class,
                () -> handler.handle(command(null, null, dataEfeito)));

        assertEquals(400, ex.getStatusCode().value());
        verifyNoInteractions(funcionarioRepository, vinculoLaboralService, assignmentService);
    }

    @Test
    void recusaPedidoSemDataDeEfeito() {
        var ex = assertThrows(IgrpResponseStatusException.class,
                () -> handler.handle(command(categoryId.toString(), null, null)));

        assertEquals(400, ex.getStatusCode().value());
        verifyNoInteractions(funcionarioRepository, vinculoLaboralService, assignmentService);
    }

    @Test
    void recusaUuidInvalido() {
        funcionarioActivo(true);
        vinculoElegivel(true);

        var ex = assertThrows(IgrpResponseStatusException.class,
                () -> handler.handle(command("nao-e-uuid", null, dataEfeito)));

        assertEquals(400, ex.getStatusCode().value());
        verifyNoInteractions(assignmentService);
    }

    @Test
    void recusaFuncionarioInexistente() {
        when(funcionarioRepository.findById(funcionarioId)).thenReturn(Optional.empty());

        var ex = assertThrows(IgrpResponseStatusException.class,
                () -> handler.handle(command(categoryId.toString(), null, dataEfeito)));

        assertEquals(404, ex.getStatusCode().value());
    }
}
