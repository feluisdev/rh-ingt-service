package cv.igrp.RH_Service.colaboradores.application.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import cv.igrp.RH_Service.carreiras.domain.models.Career;
import cv.igrp.RH_Service.carreiras.domain.models.Category;
import cv.igrp.RH_Service.carreiras.domain.repository.CareerRepository;
import cv.igrp.RH_Service.carreiras.domain.repository.CategoryRepository;
import cv.igrp.RH_Service.carreiras.domain.valueobject.CareerId;
import cv.igrp.RH_Service.carreiras.domain.valueobject.CategoryId;
import cv.igrp.RH_Service.colaboradores.application.queries.GetMapaEfectivosCsvQueryHandler;
import cv.igrp.RH_Service.colaboradores.domain.repository.AssignmentRepository;
import cv.igrp.RH_Service.estrutura.domain.models.Job;
import cv.igrp.RH_Service.estrutura.domain.models.OrganizationalUnit;
import cv.igrp.RH_Service.estrutura.domain.models.Position;
import cv.igrp.RH_Service.estrutura.domain.repository.JobRepository;
import cv.igrp.RH_Service.estrutura.domain.repository.PositionRepository;
import cv.igrp.RH_Service.estrutura.domain.valueobject.JobId;
import cv.igrp.RH_Service.estrutura.domain.valueobject.OrganizationalUnitId;
import cv.igrp.RH_Service.estrutura.domain.valueobject.PositionId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/**
 * Lei n.o 20/X/2023, art. 4.o al. aa): por unidade e cargo, os Lugares activos, providos, vagos e
 * congelados; os extintos nao entram; a categoria mais alta primeiro, fora da grelha no fim.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MapaEfectivosServiceTest {

    @Mock private QuemEstaNoServico quemEstaNoServico;
    @Mock private PositionRepository positionRepository;
    @Mock private AssignmentRepository assignmentRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private CareerRepository careerRepository;
    @Mock private JobRepository jobRepository;

    private MapaEfectivosService service;
    private final UUID unidadeId = UUID.randomUUID();
    private final UUID carreira = UUID.randomUUID();
    private final UUID tecnico = UUID.randomUUID();
    private final UUID principal = UUID.randomUUID();
    private final UUID motorista = UUID.randomUUID();
    private final List<Position> lugares = new ArrayList<>();

    private Position lugar(UUID categoria, UUID job, String estado) {
        var p = mock(Position.class);
        when(p.getId()).thenReturn(PositionId.from(UUID.randomUUID()));
        when(p.getCareerId()).thenReturn(categoria != null ? carreira : null);
        when(p.getCategoryId()).thenReturn(categoria);
        when(p.getJobId()).thenReturn(job);
        when(p.getEstado()).thenReturn(estado);
        lugares.add(p);
        return p;
    }

    private void categoria(UUID id, String nome, int ordem) {
        var c = mock(Category.class);
        when(c.getName()).thenReturn(nome);
        when(c.getOrdemProgressao()).thenReturn(ordem);
        when(categoryRepository.findById(CategoryId.from(id))).thenReturn(Optional.of(c));
    }

    @BeforeEach
    void base() {
        service = new MapaEfectivosService(quemEstaNoServico, positionRepository, assignmentRepository,
                categoryRepository, careerRepository, jobRepository);
        var u = OrganizationalUnit.reconstruir(OrganizationalUnitId.from(unidadeId), "SRH", "Servico de RH", "SRH",
                null, null, null, null, null, true);
        when(quemEstaNoServico.unidades(unidadeId, true)).thenReturn(List.of(u));
        when(positionRepository.findByUnidade(unidadeId)).thenReturn(lugares);
        categoria(tecnico, "Tecnico", 1);
        categoria(principal, "Tecnico Principal", 2);
        var car = mock(Career.class);
        when(car.getName()).thenReturn("Tecnica");
        when(careerRepository.findById(CareerId.from(carreira))).thenReturn(Optional.of(car));
        var j = mock(Job.class);
        when(j.getName()).thenReturn("Motorista");
        when(jobRepository.findById(JobId.from(motorista))).thenReturn(Optional.of(j));
    }

    @Test
    void contaPorCargoEOrdena() {
        var t1 = lugar(tecnico, null, Position.ATIVO);
        lugar(tecnico, null, Position.ATIVO);
        lugar(tecnico, null, Position.CONGELADO);
        lugar(tecnico, null, Position.EXTINTO);
        var p1 = lugar(principal, null, Position.ATIVO);
        lugar(null, motorista, Position.ATIVO);
        Set<UUID> providos = Set.of(t1.getId().getValor(), p1.getId().getValor());
        when(assignmentRepository.findPositionIdsComTitular(any())).thenReturn(providos);

        var m = service.mapa(unidadeId, true);

        var cargos = m.unidades().get(0).cargos();
        assertEquals(3, cargos.size());
        assertEquals("Tecnico Principal", cargos.get(0).categoria());
        assertEquals(new MapaEfectivosService.Contagem(1, 1, 0, 0), cargos.get(0).contagem());
        assertEquals("Tecnico", cargos.get(1).categoria());
        assertEquals(new MapaEfectivosService.Contagem(2, 1, 1, 1), cargos.get(1).contagem());   // o extinto nao entra
        assertTrue(cargos.get(2).foraDeGrelha());
        assertEquals(new MapaEfectivosService.Contagem(1, 0, 1, 0), cargos.get(2).contagem());
        assertEquals(new MapaEfectivosService.Contagem(4, 2, 2, 1), m.totais());

        String csv = GetMapaEfectivosCsvQueryHandler.csv(m);
        assertEquals(4, csv.split("\r\n").length);   // cabecalho + 3 cargos
    }

    @Test
    void semUnidadeE422() {
        var e = assertThrows(IgrpResponseStatusException.class, () -> service.mapa(null, true));
        assertEquals(422, e.getStatusCode().value());
    }
}
