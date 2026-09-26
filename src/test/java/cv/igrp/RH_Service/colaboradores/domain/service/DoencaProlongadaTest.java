package cv.igrp.RH_Service.colaboradores.domain.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cv.igrp.RH_Service.colaboradores.domain.models.PedidoAusencia;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.TipoAusenciaId;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.junit.jupiter.api.Test;

/** BR-SST-19: 30 dias seguidos de doença, ainda em curso, sugerem a junta; fins-de-semana entre atestados não interrompem. */
class DoencaProlongadaTest {

    private final FuncionarioId f = FuncionarioId.gerarNovo();
    private final TipoAusenciaId doenca = TipoAusenciaId.gerarNovo();

    private PedidoAusencia p(FuncionarioId quem, LocalDate de, LocalDate ate) {
        return PedidoAusencia.criar(quem, doenca, de, ate, (int) ChronoUnit.DAYS.between(de, ate) + 1, null, null);
    }

    @Test
    void atestadosSeguidosSomamEAtingemOsTrintaDias() {
        var pedidos = List.of(p(f, LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 15)),
                p(f, LocalDate.of(2026, 8, 16), LocalDate.of(2026, 9, 15)));
        assertTrue(DoencaProlongada.atingidos(pedidos, LocalDate.of(2026, 8, 29)).isEmpty());
        var r = DoencaProlongada.atingidos(pedidos, LocalDate.of(2026, 8, 30));
        assertEquals(1, r.size());
        assertEquals(30, r.getFirst().dias());
        assertEquals(LocalDate.of(2026, 8, 1), r.getFirst().de());
        assertEquals(LocalDate.of(2026, 9, 15), r.getFirst().ate());
    }

    @Test
    void fimDeSemanaNaoInterrompeMasUmDiaUtilSim() {
        // 2026-08-07 é sexta; recomeça na segunda 10
        var comFimDeSemana = List.of(p(f, LocalDate.of(2026, 7, 13), LocalDate.of(2026, 8, 7)),
                p(f, LocalDate.of(2026, 8, 10), LocalDate.of(2026, 8, 31)));
        assertEquals(LocalDate.of(2026, 7, 13), DoencaProlongada.atingidos(comFimDeSemana, LocalDate.of(2026, 8, 12)).getFirst().de());
        // regressou na segunda 10 e adoeceu de novo na terça 11
        var comRegresso = List.of(p(f, LocalDate.of(2026, 7, 13), LocalDate.of(2026, 8, 7)),
                p(f, LocalDate.of(2026, 8, 11), LocalDate.of(2026, 8, 31)));
        assertTrue(DoencaProlongada.atingidos(comRegresso, LocalDate.of(2026, 8, 20)).isEmpty());
    }

    @Test
    void quemJaRegressouNaoVaiAJunta() {
        var pedidos = List.of(p(f, LocalDate.of(2026, 6, 1), LocalDate.of(2026, 7, 31)));
        assertTrue(DoencaProlongada.atingidos(pedidos, LocalDate.of(2026, 8, 3)).isEmpty());
        assertEquals(1, DoencaProlongada.atingidos(pedidos, LocalDate.of(2026, 7, 31)).size());
    }

    @Test
    void cadaColaboradorComOSeuPeriodo() {
        var g = FuncionarioId.gerarNovo();
        var pedidos = List.of(p(f, LocalDate.of(2026, 8, 1), LocalDate.of(2026, 9, 30)),
                p(g, LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30)));
        var r = DoencaProlongada.atingidos(pedidos, LocalDate.of(2026, 9, 15));
        assertEquals(1, r.size());
        assertEquals(f, r.getFirst().funcionarioId());
    }
}
