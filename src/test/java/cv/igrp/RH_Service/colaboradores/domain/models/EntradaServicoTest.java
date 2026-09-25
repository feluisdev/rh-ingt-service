package cv.igrp.RH_Service.colaboradores.domain.models;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.ProvimentoId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

/** Lei n.o 20/X/2023, arts. 57.o, 63.o, 72.o, 79.o-81.o: posse, estagio de 1 ano com tutor, periodo experimental 60/30 dias. */
class EntradaServicoTest {

    private static final LocalDate POSSE = LocalDate.of(2026, 1, 5);
    private final FuncionarioId pessoa = FuncionarioId.gerarNovo();
    private final FuncionarioId tutor = FuncionarioId.gerarNovo();

    private static int status(Runnable r) {
        return assertThrows(IgrpResponseStatusException.class, r::run).getStatusCode().value();
    }

    @Test
    void posseNaoAntesDoDespacho() {
        assertEquals(422, status(() -> Provimento.registar(pessoa, ModalidadeProvimento.NOMEACAO_PROVISORIA, "D-1",
                POSSE, POSSE.minusDays(1), null, false, null, null)));
        assertEquals(422, status(() -> Provimento.registar(pessoa, null, "D-1", POSSE, POSSE, null, false, null, null)));
        assertTrue(ModalidadeProvimento.NOMEACAO_PROVISORIA.temEstagioProbatorio());
        assertEquals(ModalidadeProvimento.CONTRATO_INDETERMINADO, ModalidadeProvimento.CONTRATO_ESTAGIO.depoisDoEstagio());
    }

    @Test
    void estagioDuraUmAnoETemTutor() {
        assertEquals(422, status(() -> PeriodoProva.estagio(ProvimentoId.gerarNovo(), pessoa, POSSE, null)));
        assertEquals(422, status(() -> PeriodoProva.estagio(ProvimentoId.gerarNovo(), pessoa, POSSE, pessoa)));
        var e = PeriodoProva.estagio(ProvimentoId.gerarNovo(), pessoa, POSSE, tutor);
        assertEquals(LocalDate.of(2027, 1, 4), e.getFimPrevisto());
    }

    @Test
    void periodoExperimental60Ou30Dias() {
        var longo = PeriodoProva.experimental(ProvimentoId.gerarNovo(), pessoa, POSSE, POSSE.plusMonths(6).minusDays(1), null);
        assertEquals(POSSE.plusDays(59), longo.getFimPrevisto());
        var curto = PeriodoProva.experimental(ProvimentoId.gerarNovo(), pessoa, POSSE, POSSE.plusMonths(5), null);
        assertEquals(POSSE.plusDays(29), curto.getFimPrevisto());
        var incerto = PeriodoProva.experimental(ProvimentoId.gerarNovo(), pessoa, POSSE, null, 8);
        assertEquals(POSSE.plusDays(59), incerto.getFimPrevisto());
        assertEquals(422, status(() -> PeriodoProva.experimental(ProvimentoId.gerarNovo(), pessoa, POSSE, null, null)));
    }

    @Test
    void relatorioDoTutorEDecisaoNoFim() {
        var e = PeriodoProva.estagio(ProvimentoId.gerarNovo(), pessoa, POSSE, tutor);
        assertEquals(403, status(() -> e.registarRelatorio(pessoa, PeriodoProva.Avaliacao.POSITIVA, "Bom", POSSE.plusMonths(11))));
        e.registarRelatorio(tutor, PeriodoProva.Avaliacao.POSITIVA, "Revelou as competencias", POSSE.plusMonths(11));
        assertEquals(422, status(() -> e.concluir(null, null, POSSE.plusMonths(11))));   // antes do fim
        e.concluir(null, null, LocalDate.of(2027, 1, 5));                                // vale o relatorio
        assertEquals(PeriodoProva.Estado.CONCLUIDO_COM_SUCESSO, e.getEstado());
        assertEquals(409, status(() -> e.cessarAntecipadamente("x", POSSE)));
    }

    @Test
    void cessacaoAntecipadaFundamentadaEDenunciaSoNoExperimental() {
        var e = PeriodoProva.estagio(ProvimentoId.gerarNovo(), pessoa, POSSE, tutor);
        assertEquals(422, status(() -> e.cessarAntecipadamente(" ", POSSE.plusMonths(2))));
        assertEquals(422, status(() -> e.denunciar(POSSE.plusDays(10))));
        e.cessarAntecipadamente("Nao revela as competencias exigidas", POSSE.plusMonths(2));
        assertTrue(e.terminouSemSucesso());
        var x = PeriodoProva.experimental(ProvimentoId.gerarNovo(), pessoa, POSSE, POSSE.plusYears(1), null);
        x.denunciar(POSSE.plusDays(10));
        assertEquals(PeriodoProva.Estado.DENUNCIADO, x.getEstado());
    }
}
