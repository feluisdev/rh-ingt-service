package cv.igrp.RH_Service.colaboradores.application.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import cv.igrp.RH_Service.colaboradores.domain.models.Assignment;
import cv.igrp.RH_Service.colaboradores.domain.repository.AssignmentRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.estrutura.domain.models.Position;
import cv.igrp.RH_Service.estrutura.domain.repository.PositionRepository;
import cv.igrp.RH_Service.estrutura.domain.valueobject.PositionId;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/** A chefia directa pelo Mapa de Pessoal: o titular do Lugar-pai; a equipa, os titulares dos Lugares que reportam. */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ChefiaServiceTest {

    @Mock private AssignmentRepository assignmentRepository;
    @Mock private PositionRepository positionRepository;
    @InjectMocks private ChefiaService service;

    private final UUID lugarChefe = UUID.randomUUID();
    private final UUID lugarA = UUID.randomUUID();
    private final UUID lugarB = UUID.randomUUID();
    private final FuncionarioId chefe = FuncionarioId.gerarNovo();
    private final FuncionarioId a = FuncionarioId.gerarNovo();
    private final FuncionarioId b = FuncionarioId.gerarNovo();

    private Assignment afectacao(FuncionarioId quem, UUID lugar) {
        var x = mock(Assignment.class);
        when(x.getFuncionarioId()).thenReturn(quem);
        when(x.getPositionId()).thenReturn(lugar);
        when(assignmentRepository.findCurrentPrincipalByFuncionario(quem)).thenReturn(Optional.of(x));
        when(assignmentRepository.findTitularByPosition(lugar)).thenReturn(Optional.of(x));
        return x;
    }

    private Position lugar(UUID id, UUID pai) {
        var p = mock(Position.class);
        when(p.getId()).thenReturn(PositionId.from(id));
        when(p.getParentPositionId()).thenReturn(pai);
        when(positionRepository.findById(PositionId.from(id))).thenReturn(Optional.of(p));
        return p;
    }

    @Test
    void chefiaDirectaEEquipa() {
        afectacao(chefe, lugarChefe);
        afectacao(a, lugarA);
        afectacao(b, lugarB);
        lugar(lugarChefe, null);
        var pa = lugar(lugarA, lugarChefe);
        var pb = lugar(lugarB, lugarChefe);
        when(positionRepository.findSubordinados(lugarChefe)).thenReturn(List.of(pa, pb));

        assertEquals(Optional.of(chefe), service.chefeDirecto(a));
        assertTrue(service.eChefeDirecto(chefe, b));
        assertFalse(service.eChefeDirecto(a, b));
        assertEquals(List.of(a, b), service.equipaDirecta(chefe));
    }

    @Test
    void semLugarPaiOuChefiaVagaNaoHaChefia() {
        afectacao(a, lugarA);
        lugar(lugarA, null);
        assertEquals(Optional.empty(), service.chefeDirecto(a));

        afectacao(b, lugarB);
        lugar(lugarB, lugarChefe);
        when(assignmentRepository.findTitularByPosition(lugarChefe)).thenReturn(Optional.empty());
        assertEquals(Optional.empty(), service.chefeDirecto(b));
    }

    @Test
    void semLugarAEquipaEVazia() {
        when(assignmentRepository.findCurrentPrincipalByFuncionario(any())).thenReturn(Optional.empty());
        assertTrue(service.equipaDirecta(chefe).isEmpty());
    }
}
