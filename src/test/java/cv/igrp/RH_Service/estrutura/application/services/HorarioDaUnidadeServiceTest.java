package cv.igrp.RH_Service.estrutura.application.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

import cv.igrp.RH_Service.estrutura.domain.models.OrganizationalUnit;
import cv.igrp.RH_Service.estrutura.domain.models.VigenciaHorarioUnidade;
import cv.igrp.RH_Service.estrutura.domain.repository.HorarioUnidadeHistoricoRepository;
import cv.igrp.RH_Service.estrutura.domain.repository.OrganizationalUnitRepository;
import cv.igrp.RH_Service.estrutura.domain.valueobject.OrganizationalUnitId;
import cv.igrp.RH_Service.parametrizacoes.domain.valueobject.HorarioId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/**
 * O horario da unidade tem data de efeito: muda de hoje (ou de uma data futura), o passado fica com o
 * que vigorava; sem historico vale a coluna; uma data passada da 422.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class HorarioDaUnidadeServiceTest {

    private static final LocalDate HOJE = LocalDate.of(2026, 9, 24);

    @Mock private HorarioUnidadeHistoricoRepository historico;
    @Mock private OrganizationalUnitRepository unidadeRepository;

    private HorarioDaUnidadeService service;
    private final List<VigenciaHorarioUnidade> linhas = new ArrayList<>();
    private final HorarioId antigo = HorarioId.gerarNovo();
    private final HorarioId novo = HorarioId.gerarNovo();
    private OrganizationalUnit unidade;

    @BeforeEach
    void base() {
        service = new HorarioDaUnidadeService(historico, unidadeRepository) {
            @Override protected LocalDate hoje() { return HOJE; }
        };
        unidade = OrganizationalUnit.reconstruir(OrganizationalUnitId.gerarNovo(), "U", "Unidade", "U",
                null, null, null, null, null, antigo, true);
        when(historico.findByUnidade(any())).thenAnswer(i -> linhas.stream()
                .filter(l -> l.unidadeId().equals(i.getArgument(0))).toList());
        when(historico.findByHorario(any())).thenAnswer(i -> linhas.stream()
                .filter(l -> i.getArgument(0).equals(l.horarioId())).toList());
        doAnswer(i -> {
            VigenciaHorarioUnidade v = i.getArgument(0);
            linhas.removeIf(l -> l.unidadeId().equals(v.unidadeId()) && v.desde() != null && v.desde().equals(l.desde()));
            linhas.add(v);
            return null;
        }).when(historico).registar(any());
    }

    @Test
    void semHistoricoValeAColunaEmQualquerData() {
        assertEquals(antigo, service.horarioEm(unidade, LocalDate.of(2020, 1, 1)));
        assertEquals(antigo, service.horarioEm(unidade, HOJE.plusYears(1)));
    }

    @Test
    void mudarDeHojeDeixaOPassadoComOAntigo() {
        service.definir(unidade, novo, null);
        assertEquals(antigo, service.horarioEm(unidade, HOJE.minusDays(1)));
        assertEquals(novo, service.horarioEm(unidade, HOJE));
        assertEquals(novo, unidade.getHorarioId());   // a coluna guarda o de hoje
    }

    @Test
    void mudarNumaDataFuturaSoValeDessaData() {
        service.definir(unidade, novo, HOJE.plusDays(10));
        assertEquals(antigo, service.horarioEm(unidade, HOJE));
        assertEquals(novo, service.horarioEm(unidade, HOJE.plusDays(10)));
        assertEquals(antigo, unidade.getHorarioId());  // ainda nao
    }

    @Test
    void limparOHorarioTambemTemData() {
        service.definir(unidade, null, null);
        assertEquals(antigo, service.horarioEm(unidade, HOJE.minusDays(1)));
        assertNull(service.horarioEm(unidade, HOJE));
    }

    @Test
    void umaDataPassadaE422() {
        var e = assertThrows(IgrpResponseStatusException.class, () -> service.definir(unidade, novo, HOJE.minusDays(1)));
        assertEquals(422, e.getStatusCode().value());
    }

    @Test
    void vigorouPeloHistoricoOuPelaColunaDeUmaUnidadeSemHistorico() {
        when(unidadeRepository.findAllByHorario(antigo)).thenReturn(List.of(unidade));
        assertTrue(service.vigorouAntesDe(antigo, HOJE));      // coluna, desde sempre
        service.definir(unidade, novo, null);
        assertTrue(service.vigorouAntesDe(antigo, HOJE));      // agora pelo historico (desde sempre ate ontem)
        assertFalse(service.vigorouAntesDe(novo, HOJE));       // so vale de hoje: ainda nao vigorou num dia passado
    }
}
