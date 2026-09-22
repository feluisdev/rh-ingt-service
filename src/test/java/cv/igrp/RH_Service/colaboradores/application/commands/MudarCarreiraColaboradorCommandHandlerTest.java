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

import cv.igrp.RH_Service.carreiras.domain.models.Career;
import cv.igrp.RH_Service.carreiras.domain.models.Category;
import cv.igrp.RH_Service.carreiras.domain.models.Grade;
import cv.igrp.RH_Service.carreiras.domain.valueobject.CareerId;
import cv.igrp.RH_Service.carreiras.domain.valueobject.CategoryId;
import cv.igrp.RH_Service.carreiras.domain.valueobject.GradeId;
import cv.igrp.RH_Service.colaboradores.application.dto.MudancaCarreiraRequestDTO;
import cv.igrp.RH_Service.colaboradores.application.services.AssignmentService;
import cv.igrp.RH_Service.colaboradores.application.services.VinculoLaboralService;
import cv.igrp.RH_Service.colaboradores.domain.models.Assignment;
import cv.igrp.RH_Service.colaboradores.domain.models.Funcionario;
import cv.igrp.RH_Service.colaboradores.domain.models.TipoAfectacao;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.estrutura.domain.models.Position;
import cv.igrp.RH_Service.estrutura.domain.valueobject.PositionId;
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
 * Prova de {@link MudarCarreiraColaboradorCommandHandler}: elegibilidade (colaborador activo,
 * vínculo) e o que sai na resposta. As regras da grelha estão provadas em
 * {@code AssignmentServiceMudancaCarreiraTest}.
 */
@ExtendWith(MockitoExtension.class)
class MudarCarreiraColaboradorCommandHandlerTest {

    @Mock private FuncionarioRepository funcionarioRepository;
    @Mock private VinculoLaboralService vinculoLaboralService;
    @Mock private AssignmentService assignmentService;

    @InjectMocks private MudarCarreiraColaboradorCommandHandler handler;

    private final UUID funcionarioUuid = UUID.randomUUID();
    private final FuncionarioId funcionarioId = FuncionarioId.from(funcionarioUuid);
    private final UUID positionId = UUID.randomUUID();
    private final LocalDate dataEfeito = LocalDate.of(2026, 10, 1);

    private MudarCarreiraColaboradorCommand command(String positionId, LocalDate data) {
        MudancaCarreiraRequestDTO dto = new MudancaCarreiraRequestDTO();
        dto.setPositionId(positionId);
        dto.setDataEfeito(data);
        dto.setDespachoNumero("45/2026");
        dto.setConcursoRef("CI-7/2026");
        return new MudarCarreiraColaboradorCommand(funcionarioUuid.toString(), dto);
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

    private static Position lugar(UUID id, String numero) {
        return Position.reconstituir(PositionId.from(id), numero, UUID.randomUUID(), UUID.randomUUID(),
                UUID.randomUUID(), UUID.randomUUID(), null, null, Position.ATIVO, null, true);
    }

    private AssignmentService.MudancaCarreira resultado() {
        UUID origemId = UUID.randomUUID();
        CareerId carreiraAntiga = CareerId.gerarNovo();
        CareerId carreiraNova = CareerId.gerarNovo();
        CategoryId categoriaAntiga = CategoryId.gerarNovo();
        CategoryId categoriaNova = CategoryId.gerarNovo();
        Grade escalao = Grade.reconstituir(GradeId.gerarNovo(), categoriaNova, 1, "E1", "Escalão 1",
                BigDecimal.ONE, BigDecimal.TEN, true);
        Assignment nova = Assignment.criar(funcionarioId, positionId, escalao.getId().getValor(), null,
                TipoAfectacao.PRINCIPAL, Assignment.MUDANCA_CARREIRA, dataEfeito, null);
        return new AssignmentService.MudancaCarreira(nova,
                Career.reconstituir(carreiraAntiga, "AT", "Assistente Técnico", null, "GERAL", true),
                Career.reconstituir(carreiraNova, "TS", "Técnico Superior", null, "GERAL", true),
                Category.reconstituir(categoriaAntiga, carreiraAntiga, "AT1", "Assistente Técnico 1", null, 1, true),
                Category.reconstituir(categoriaNova, carreiraNova, "TS1", "Técnico Superior 1", null, 1, true),
                escalao, lugar(origemId, "LUG-0002"), lugar(positionId, "LUG-0004"));
    }

    @Test
    void mudaDeCarreiraEDevolveAsDuasPontasDaGrelha() {
        funcionarioActivo(true);
        vinculoElegivel(true);
        when(assignmentService.mudarCarreira(eq(funcionarioId), eq(positionId), isNull(), isNull(),
                eq(dataEfeito), any())).thenReturn(resultado());

        var resposta = handler.handle(command(positionId.toString(), dataEfeito));

        assertEquals(201, resposta.getStatusCode().value());
        var corpo = resposta.getBody();
        assertEquals("Assistente Técnico", corpo.getCarreiraAnterior());
        assertEquals("Técnico Superior", corpo.getCarreiraNova());
        assertEquals("Assistente Técnico 1", corpo.getCategoriaAnterior());
        assertEquals("Técnico Superior 1", corpo.getCategoriaNova());
        assertEquals("LUG-0002", corpo.getNumeroLugarAnterior());
        assertEquals("LUG-0004", corpo.getNumeroLugar());
        assertEquals("Escalão 1", corpo.getEscalao());
        assertEquals(dataEfeito, corpo.getDataEfeito());
    }

    /** O despacho e o concurso não se validam — registam-se nas notas da afectação. */
    @Test
    void registaODespachoEOConcursoNasNotas() {
        funcionarioActivo(true);
        vinculoElegivel(true);
        when(assignmentService.mudarCarreira(any(), any(), any(), any(), any(), any()))
                .thenReturn(resultado());

        handler.handle(command(positionId.toString(), dataEfeito));

        var notas = org.mockito.ArgumentCaptor.forClass(String.class);
        verify(assignmentService).mudarCarreira(any(), any(), any(), any(), any(), notas.capture());
        assertTrue(notas.getValue().contains("Mudança de carreira"));
        assertTrue(notas.getValue().contains("45/2026"));
        assertTrue(notas.getValue().contains("CI-7/2026"));
    }

    @Test
    void recusaColaboradorInactivo() {
        funcionarioActivo(false);

        var ex = assertThrows(IgrpResponseStatusException.class,
                () -> handler.handle(command(positionId.toString(), dataEfeito)));

        assertEquals(422, ex.getStatusCode().value());
        verifyNoInteractions(assignmentService);
    }

    /**
     * O vínculo pesa aqui, ao contrário da transferência: mudar de carreira é evolução na
     * carreira, não uma mudança de cadeira.
     */
    @Test
    void recusaVinculoQueNaoPermiteEvoluirNaCarreira() {
        funcionarioActivo(true);
        vinculoElegivel(false);

        var ex = assertThrows(IgrpResponseStatusException.class,
                () -> handler.handle(command(positionId.toString(), dataEfeito)));

        assertEquals(422, ex.getStatusCode().value());
        verifyNoInteractions(assignmentService);
    }

    @Test
    void recusaFuncionarioInexistente() {
        when(funcionarioRepository.findById(funcionarioId)).thenReturn(Optional.empty());

        var ex = assertThrows(IgrpResponseStatusException.class,
                () -> handler.handle(command(positionId.toString(), dataEfeito)));

        assertEquals(404, ex.getStatusCode().value());
        verifyNoInteractions(assignmentService);
    }

    @Test
    void exigeOLugarDeDestino() {
        var ex = assertThrows(IgrpResponseStatusException.class,
                () -> handler.handle(command(null, dataEfeito)));

        assertEquals(400, ex.getStatusCode().value());
        verifyNoInteractions(assignmentService);
    }

    @Test
    void exigeADataDeEfeito() {
        var ex = assertThrows(IgrpResponseStatusException.class,
                () -> handler.handle(command(positionId.toString(), null)));

        assertEquals(400, ex.getStatusCode().value());
        verifyNoInteractions(assignmentService);
    }

    @Test
    void recusaPositionIdQueNaoEUuid() {
        funcionarioActivo(true);
        vinculoElegivel(true);

        var ex = assertThrows(IgrpResponseStatusException.class,
                () -> handler.handle(command("nao-e-uuid", dataEfeito)));

        assertEquals(400, ex.getStatusCode().value());
        verifyNoInteractions(assignmentService);
    }
}
