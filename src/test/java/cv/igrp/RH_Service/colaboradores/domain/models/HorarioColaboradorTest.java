package cv.igrp.RH_Service.colaboradores.domain.models;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.HorarioColaboradorId;
import cv.igrp.RH_Service.parametrizacoes.domain.valueobject.HorarioId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

/** O historico de horarios de uma pessoa: sem sobreposicao, e uma atribuicao nova fecha a anterior na vespera. */
class HorarioColaboradorTest {

    private final FuncionarioId funcionario = FuncionarioId.gerarNovo();

    private HorarioColaborador aberta(LocalDate inicio) {
        return HorarioColaborador.reconstruir(HorarioColaboradorId.gerarNovo(), funcionario, HorarioId.gerarNovo(),
                RegimePrestacao.PRESENCIAL, inicio, null);
    }

    @Test
    void aPrimeiraFicaAbertaEPresencialPorOmissao() {
        var h = HorarioColaborador.atribuir(funcionario, HorarioId.gerarNovo(), null, LocalDate.of(2026, 10, 1), new ArrayList<>());

        assertEquals(RegimePrestacao.PRESENCIAL, h.getRegimePrestacao());
        assertNull(h.getDataFim());
        assertTrue(h.vigoraEm(LocalDate.of(2030, 1, 1)));
        assertFalse(h.vigoraEm(LocalDate.of(2026, 9, 30)));
    }

    @Test
    void aSeguinteFechaAAnteriorNaVespera() {
        var anterior = aberta(LocalDate.of(2026, 1, 1));
        var nova = HorarioColaborador.atribuir(funcionario, HorarioId.gerarNovo(), RegimePrestacao.TELETRABALHO,
                LocalDate.of(2026, 10, 1), new ArrayList<>(List.of(anterior)));

        assertEquals(LocalDate.of(2026, 9, 30), anterior.getDataFim());
        assertEquals(RegimePrestacao.TELETRABALHO, nova.getRegimePrestacao());
    }

    @Test
    void naoSeAtribuiAntesNemNoMesmoDiaDaUltima() {
        var existentes = new ArrayList<>(List.of(aberta(LocalDate.of(2026, 10, 1))));
        for (LocalDate inicio : List.of(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 5, 1))) {
            var e = assertThrows(IgrpResponseStatusException.class, () -> HorarioColaborador.atribuir(
                    funcionario, HorarioId.gerarNovo(), null, inicio, existentes));
            assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.value(), e.getStatusCode().value());
        }
        assertNull(existentes.get(0).getDataFim());
    }

    @Test
    void dataDeInicioObrigatoria() {
        assertThrows(IgrpResponseStatusException.class, () -> HorarioColaborador.atribuir(
                funcionario, HorarioId.gerarNovo(), null, null, new ArrayList<>()));
    }
}
