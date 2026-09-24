package cv.igrp.RH_Service.colaboradores.application.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import cv.igrp.RH_Service.colaboradores.domain.models.FeriasDoAno;
import cv.igrp.RH_Service.colaboradores.domain.models.Funcionario;
import cv.igrp.RH_Service.colaboradores.domain.models.MapaFerias;
import cv.igrp.RH_Service.colaboradores.domain.models.OrigemMarcacaoFerias;
import cv.igrp.RH_Service.colaboradores.domain.models.OrigemPreferenciaFerias;
import cv.igrp.RH_Service.colaboradores.domain.models.PeriodoFerias;
import cv.igrp.RH_Service.colaboradores.domain.repository.FeriasDoAnoRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.MapaFeriasRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FeriasDoAnoId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.parametrizacoes.application.services.ParametrosFeriasService;
import cv.igrp.RH_Service.parametrizacoes.domain.models.ParametroFerias;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/**
 * DL n.o 3/2010: a preferencia indicada pelo proprio (art. 5.o n.o 4) fica com quem a indicou; um pedido
 * de ferias fora da marcacao do mapa (art. 6.o n.o 2) da aviso -- mais forte depois de o mapa ser
 * conhecido --, e nunca recusa.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MapaFeriasAvisoEPreferenciaTest {

    /** Um ano cujo prazo do mapa (31 de Marco) ja passou, e um cujo prazo ainda nao chegou. */
    private static final int PASSADO = 2025;
    private static final int FUTURO = 2030;

    @Mock private FeriasDoAnoRepository feriasDoAnoRepository;
    @Mock private MapaFeriasRepository mapaFeriasRepository;
    @Mock private FuncionarioRepository funcionarioRepository;
    @Mock private ParametrosFeriasService parametrosFerias;
    @InjectMocks private MapaFeriasService service;

    private final FuncionarioId maria = FuncionarioId.gerarNovo();
    private Funcionario pessoa;

    @BeforeEach
    void base() {
        pessoa = mock(Funcionario.class);
        when(pessoa.getId()).thenReturn(maria);
        when(pessoa.getIsActive()).thenReturn(true);
        when(funcionarioRepository.findById(maria)).thenReturn(Optional.of(pessoa));
        when(parametrosFerias.vigenteEm(anyInt())).thenReturn(ParametroFerias.daLei());
        when(mapaFeriasRepository.findByAno(anyInt())).thenReturn(Optional.empty());
        when(feriasDoAnoRepository.findByFuncionarioIdAndAno(any(), anyInt())).thenReturn(Optional.empty());
        when(feriasDoAnoRepository.save(any())).thenAnswer(i -> i.getArgument(0));
    }

    private void marcadas(int ano, LocalDate de, LocalDate ate) {
        var f = FeriasDoAno.reconstituir(FeriasDoAnoId.gerarNovo(), maria, ano, List.of(), null, false, null,
                List.of(new PeriodoFerias(de, ate, 10)), OrigemMarcacaoFerias.ACORDO, null, LocalDate.of(ano, 2, 1), List.of());
        when(feriasDoAnoRepository.findByFuncionarioIdAndAno(maria, ano)).thenReturn(Optional.of(f));
    }

    @Test
    void dentroDaMarcacaoNaoAvisa() {
        marcadas(PASSADO, LocalDate.of(PASSADO, 8, 1), LocalDate.of(PASSADO, 8, 22));
        assertTrue(service.avisoForaDaMarcacao(maria, LocalDate.of(PASSADO, 8, 4), LocalDate.of(PASSADO, 8, 8)).isEmpty());
    }

    @Test
    void foraDaMarcacaoDepoisDeOMapaSerConhecidoAvisaQueEAlteracao() {
        marcadas(PASSADO, LocalDate.of(PASSADO, 8, 1), LocalDate.of(PASSADO, 8, 22));
        var aviso = service.avisoForaDaMarcacao(maria, LocalDate.of(PASSADO, 9, 1), LocalDate.of(PASSADO, 9, 5));
        assertTrue(aviso.orElseThrow().contains("art. 6.º n.º 2"), aviso.get());
    }

    @Test
    void foraDaMarcacaoAntesDoPrazoDoMapaSoDizQueNaoCoincide() {
        marcadas(FUTURO, LocalDate.of(FUTURO, 8, 1), LocalDate.of(FUTURO, 8, 22));
        var aviso = service.avisoForaDaMarcacao(maria, LocalDate.of(FUTURO, 9, 2), LocalDate.of(FUTURO, 9, 6));
        assertTrue(aviso.orElseThrow().contains("não coincide"), aviso.get());
    }

    @Test
    void semMarcacaoSoAvisaComOMapaConhecido() {
        assertTrue(service.avisoForaDaMarcacao(maria, LocalDate.of(FUTURO, 9, 2), LocalDate.of(FUTURO, 9, 6)).isEmpty());
        when(mapaFeriasRepository.findByAno(FUTURO)).thenReturn(Optional.of(mock(MapaFerias.class)));
        assertTrue(service.avisoForaDaMarcacao(maria, LocalDate.of(FUTURO, 9, 2), LocalDate.of(FUTURO, 9, 6)).isPresent());
    }

    @Test
    void aPreferenciaDoProprioFicaComQuemAIndicou() {
        var r = service.indicarPreferencia(maria, FUTURO, List.of(new PeriodoFerias(LocalDate.of(FUTURO, 7, 1),
                LocalDate.of(FUTURO, 7, 22), null)), "praia", OrigemPreferenciaFerias.PROPRIO);
        assertEquals(OrigemPreferenciaFerias.PROPRIO, r.ferias().getPreferenciaIndicadaPor());
        var rh = service.indicarPreferencia(maria, FUTURO, List.of(new PeriodoFerias(LocalDate.of(FUTURO, 7, 1),
                LocalDate.of(FUTURO, 7, 22), null)), null);
        assertEquals(OrigemPreferenciaFerias.RH, rh.ferias().getPreferenciaIndicadaPor());
    }

    @Test
    void oProprioInactivoNaoIndica403() {
        when(pessoa.getIsActive()).thenReturn(false);
        var e = assertThrows(IgrpResponseStatusException.class, () -> service.indicarPreferencia(maria, FUTURO,
                List.of(new PeriodoFerias(LocalDate.of(FUTURO, 7, 1), LocalDate.of(FUTURO, 7, 22), null)), null,
                OrigemPreferenciaFerias.PROPRIO));
        assertEquals(403, e.getStatusCode().value());
    }
}
