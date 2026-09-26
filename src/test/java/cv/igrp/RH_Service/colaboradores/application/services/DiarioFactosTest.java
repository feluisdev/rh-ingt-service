package cv.igrp.RH_Service.colaboradores.application.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import cv.igrp.RH_Service.colaboradores.application.dto.FactoRhDTO;
import cv.igrp.RH_Service.colaboradores.domain.models.Assignment;
import cv.igrp.RH_Service.colaboradores.domain.models.TipoAfectacao;
import cv.igrp.RH_Service.colaboradores.domain.models.TipoFactoRh;
import cv.igrp.RH_Service.colaboradores.domain.repository.FactoRhRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** BR-FAC-02..04: registar, movimento com os dados da afectacao, CSV do contrato. */
@ExtendWith(MockitoExtension.class)
class DiarioFactosTest {

    @Mock private FactoRhRepository repository;
    @Mock private cv.igrp.RH_Service.carreiras.domain.repository.GradeRepository gradeRepository;
    private DiarioFactos diario;
    private final FuncionarioId pessoa = FuncionarioId.gerarNovo();

    @BeforeEach
    void setUp() {
        diario = new DiarioFactos(repository, new CompetenciaSalarial(null), org.mockito.Mockito.mock(org.springframework.context.ApplicationEventPublisher.class),
                gradeRepository);
    }

    @Test
    void movimentoLevaLugarEscalaoEOrigem() {
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));
        UUID lugar = UUID.randomUUID(), escalao = UUID.randomUUID();
        var a = Assignment.criar(pessoa, lugar, escalao, null, TipoAfectacao.PRINCIPAL, Assignment.PROGRESSAO,
                LocalDate.of(2026, 10, 1), null);
        var f = diario.movimento(TipoFactoRh.PROGRESSAO, a, "Progressao para o escalao 3");
        assertEquals(TipoFactoRh.PROGRESSAO, f.getTipo());
        assertEquals(YearMonth.of(2026, 10), f.getMesCompetencia());
        assertEquals(lugar.toString(), f.getDados().get("lugarId"));
        assertEquals(escalao.toString(), f.getDados().get("escalaoId"));
        assertEquals(Assignment.PROGRESSAO, f.getDados().get("origem"));
        assertEquals("AFECTACAO", f.getReferenciaTipo());
    }

    @Test
    void movimentoLevaOBrutoDoEscalao() {
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));
        UUID escalao = UUID.randomUUID();
        var grade = org.mockito.Mockito.mock(cv.igrp.RH_Service.carreiras.domain.models.Grade.class);
        when(grade.getSalaryBase()).thenReturn(new java.math.BigDecimal("85000.00"));
        when(gradeRepository.findById(cv.igrp.RH_Service.carreiras.domain.valueobject.GradeId.from(escalao))).thenReturn(java.util.Optional.of(grade));
        var a = Assignment.criar(pessoa, UUID.randomUUID(), escalao, null, TipoAfectacao.PRINCIPAL, Assignment.PROGRESSAO,
                LocalDate.of(2026, 10, 1), null);
        var f = diario.movimento(TipoFactoRh.PROGRESSAO, a, "Progressao");
        assertEquals("85000.00", f.getDados().get("remuneracaoBase"));
    }

    @Test
    void csvTemBomSeparadorPontoEVirgulaEOsDados() {
        var dto = new FactoRhDTO("id", pessoa.getStringValor(), "0000001", "123456789", "Maria; Silva", "CESSACAO",
                LocalDate.of(2026, 9, 30), "2026-09", false, "WORKER_STATE", "x", "Cessação", Map.of("estado", "EXONERADO"),
                LocalDateTime.of(2026, 9, 30, 12, 0));
        String csv = cv.igrp.RH_Service.colaboradores.application.queries.GetFactosSalariaisCsvQueryHandler.csv(List.of(dto));
        assertTrue(csv.startsWith("﻿versao;mes_competencia;"));
        assertTrue(csv.contains("\"Maria; Silva\""), csv);
        assertTrue(csv.contains("estado=EXONERADO"), csv);
    }
}
