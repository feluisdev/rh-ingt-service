package cv.igrp.RH_Service.colaboradores.application.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import cv.igrp.RH_Service.colaboradores.domain.models.Assignment;
import cv.igrp.RH_Service.colaboradores.domain.models.Feriado;
import cv.igrp.RH_Service.colaboradores.domain.models.LicencaMobilidade;
import cv.igrp.RH_Service.colaboradores.domain.repository.AssignmentRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.FeriadoRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.estrutura.domain.models.OrganizationalUnit;
import cv.igrp.RH_Service.estrutura.domain.models.Position;
import cv.igrp.RH_Service.estrutura.domain.repository.OrganizationalUnitRepository;
import cv.igrp.RH_Service.estrutura.domain.repository.PositionRepository;
import cv.igrp.RH_Service.estrutura.domain.valueobject.OrganizationalUnitId;
import cv.igrp.RH_Service.estrutura.domain.valueobject.PositionId;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * O calendário que se aplica a um colaborador (V55). O calendário é da instituição; do
 * colaborador só importa onde trabalha, e só para os feriados que valem numa área.
 */
@ExtendWith(MockitoExtension.class)
class CalendarioFeriadosServiceTest {

    private static final LocalDate INICIO = LocalDate.of(2027, 1, 1);
    private static final LocalDate FIM = LocalDate.of(2027, 1, 31);

    @Mock private FeriadoRepository feriadoRepository;
    @Mock private MobilidadeService mobilidadeService;
    @Mock private AssignmentRepository assignmentRepository;
    @Mock private PositionRepository positionRepository;
    @Mock private OrganizationalUnitRepository unidadeRepository;

    @InjectMocks private CalendarioFeriadosService service;

    private final FuncionarioId funcionario = FuncionarioId.gerarNovo();

    @BeforeEach
    void semMobilidadePorOmissao() {
        when(mobilidadeService.mobilidadeEmVigor(funcionario, INICIO)).thenReturn(Optional.empty());
    }

    private static OrganizationalUnit unidade(UUID id, UUID mae, String area) {
        return OrganizationalUnit.reconstruir(OrganizationalUnitId.from(id), "C" + id, "U", "U", null, null,
                mae != null ? OrganizationalUnitId.from(mae) : null, null, area, true);
    }

    private void noLugarDaUnidade(UUID unidadeId) {
        var afectacao = mock(Assignment.class);
        var lugarId = UUID.randomUUID();
        when(afectacao.getPositionId()).thenReturn(lugarId);
        var lugar = mock(Position.class);
        when(lugar.getUnidadeOrganicaId()).thenReturn(unidadeId);
        when(assignmentRepository.findCurrentPrincipalByFuncionario(funcionario)).thenReturn(Optional.of(afectacao));
        when(positionRepository.findById(PositionId.from(lugarId))).thenReturn(Optional.of(lugar));
    }

    @Test
    void recorrentesPontuaisDaAreaTodosContam() {
        var unidadeId = UUID.randomUUID();
        noLugarDaUnidade(unidadeId);
        when(unidadeRepository.findById(OrganizationalUnitId.from(unidadeId)))
                .thenReturn(Optional.of(unidade(unidadeId, null, "MINDELO")));
        when(feriadoRepository.findAplicaveis(INICIO, FIM, "MINDELO")).thenReturn(List.of(
                new Feriado(LocalDate.of(2026, 1, 1), true),
                new Feriado(LocalDate.of(2026, 1, 22), true),
                new Feriado(LocalDate.of(2027, 1, 13), false)));

        assertEquals(Set.of(LocalDate.of(2027, 1, 1), LocalDate.of(2027, 1, 13), LocalDate.of(2027, 1, 22)),
                service.feriadosDoColaborador(funcionario, INICIO, FIM));
    }

    @Test
    void unidadeSemAreaHerdaADaMae() {
        var mae = UUID.randomUUID();
        var filha = UUID.randomUUID();
        noLugarDaUnidade(filha);
        when(unidadeRepository.findById(OrganizationalUnitId.from(filha))).thenReturn(Optional.of(unidade(filha, mae, null)));
        when(unidadeRepository.findById(OrganizationalUnitId.from(mae))).thenReturn(Optional.of(unidade(mae, null, "SAL")));

        service.feriadosDoColaborador(funcionario, INICIO, FIM);

        verify(feriadoRepository).findAplicaveis(INICIO, FIM, "SAL");
    }

    @Test
    void semAreaAteAoTopoSoContamOsSemArea() {
        var unidadeId = UUID.randomUUID();
        noLugarDaUnidade(unidadeId);
        when(unidadeRepository.findById(OrganizationalUnitId.from(unidadeId)))
                .thenReturn(Optional.of(unidade(unidadeId, null, null)));

        service.feriadosDoColaborador(funcionario, INICIO, FIM);

        verify(feriadoRepository).findAplicaveis(eq(INICIO), eq(FIM), isNull());
    }

    @Test
    void cicloNaArvoreNaoPrendeOCalculo() {
        var a = UUID.randomUUID();
        var b = UUID.randomUUID();
        noLugarDaUnidade(a);
        when(unidadeRepository.findById(OrganizationalUnitId.from(a))).thenReturn(Optional.of(unidade(a, b, null)));
        when(unidadeRepository.findById(OrganizationalUnitId.from(b))).thenReturn(Optional.of(unidade(b, a, null)));

        service.feriadosDoColaborador(funcionario, INICIO, FIM);

        verify(feriadoRepository).findAplicaveis(eq(INICIO), eq(FIM), isNull());
    }

    @Test
    void semLugarSoContamOsSemArea() {
        when(assignmentRepository.findCurrentPrincipalByFuncionario(funcionario)).thenReturn(Optional.empty());

        service.feriadosDoColaborador(funcionario, INICIO, FIM);

        verify(feriadoRepository).findAplicaveis(eq(INICIO), eq(FIM), isNull());
        verifyNoInteractions(unidadeRepository);
    }

    @Test
    void emMobilidadeInternaContaAUnidadeDeDestino() {
        var destino = UUID.randomUUID();
        var mobilidade = mock(LicencaMobilidade.class);
        when(mobilidade.isDestinoInterno()).thenReturn(true);
        when(mobilidade.getDestinationUnitId()).thenReturn(destino);
        when(mobilidadeService.mobilidadeEmVigor(funcionario, INICIO)).thenReturn(Optional.of(mobilidade));
        when(unidadeRepository.findById(OrganizationalUnitId.from(destino)))
                .thenReturn(Optional.of(unidade(destino, null, "PRAIA")));

        service.feriadosDoColaborador(funcionario, INICIO, FIM);

        verify(feriadoRepository).findAplicaveis(INICIO, FIM, "PRAIA");
        verifyNoInteractions(assignmentRepository);
    }

    @Test
    void emMobilidadeExternaSoContamOsSemArea_oCalendarioDoDestinoNaoENosso() {
        var mobilidade = mock(LicencaMobilidade.class);
        when(mobilidade.isDestinoInterno()).thenReturn(false);
        when(mobilidadeService.mobilidadeEmVigor(funcionario, INICIO)).thenReturn(Optional.of(mobilidade));

        service.feriadosDoColaborador(funcionario, INICIO, FIM);

        verify(feriadoRepository).findAplicaveis(eq(INICIO), eq(FIM), isNull());
        verifyNoInteractions(assignmentRepository, unidadeRepository);
    }

    @Test
    void semFeriadosCarregadosContamTodosOsDiasUteis() {
        when(assignmentRepository.findCurrentPrincipalByFuncionario(funcionario)).thenReturn(Optional.empty());
        when(feriadoRepository.findAplicaveis(any(), any(), any())).thenReturn(List.of());

        assertEquals(Set.of(), service.feriadosDoColaborador(funcionario, INICIO, FIM));
    }
}
