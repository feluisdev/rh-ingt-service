package cv.igrp.RH_Service.colaboradores.domain.models;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cv.igrp.RH_Service.colaboradores.domain.service.RegrasAposentacao;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

/** Lei n.o 20/X/2023, arts. 48.o, 175.o-179.o: as regras fixadas e o ciclo do processo (BR-APO). */
class AposentacaoDominioTest {

    private static final LocalDate HOJE = LocalDate.of(2026, 9, 25);
    private final FuncionarioId pessoa = FuncionarioId.gerarNovo();

    private static int status(Runnable r) {
        return assertThrows(IgrpResponseStatusException.class, r::run).getStatusCode().value();
    }

    @Test
    void limitesDeIdadeEProjeccaoDoTempoDeServico() {
        LocalDate nasc = LocalDate.of(1962, 3, 10);
        assertEquals(LocalDate.of(2027, 3, 10), RegrasAposentacao.faz(nasc, RegrasAposentacao.IDADE_LIMITE));
        assertEquals(64, RegrasAposentacao.idade(nasc, HOJE));
        // 33 anos de 365 dias contados hoje: faltam 365 dias para os 34
        assertEquals(HOJE.plusDays(365), RegrasAposentacao.completaAnosDeServico(34, 33L * 365, HOJE));
        // ja passou: devolve o dia (aproximado) em que chegou
        assertEquals(HOJE.minusDays(10), RegrasAposentacao.completaAnosDeServico(34, 34L * 365 + 10, HOJE));
    }

    @Test
    void preAposentacaoNoDiaEmQueAsDuasCondicoesSeVerificam() {
        LocalDate nasc = LocalDate.of(1970, 1, 1);             // faz 58 em 2028-01-01
        assertEquals(LocalDate.of(2028, 1, 1), RegrasAposentacao.preAposentacaoPossivel(nasc, 31L * 365, HOJE));
        LocalDate servico = HOJE.plusDays(2L * 365);            // 30 anos so daqui a 2 anos
        assertEquals(servico, RegrasAposentacao.preAposentacaoPossivel(LocalDate.of(1960, 1, 1), 28L * 365, HOJE));
    }

    @Test
    void prestacaoDaPreAposentacaoEntre70e80() {
        assertTrue(RegrasAposentacao.prestacaoValida(BigDecimal.valueOf(75)));
        assertTrue(RegrasAposentacao.prestacaoValida(BigDecimal.valueOf(70)));
        assertFalse(RegrasAposentacao.prestacaoValida(BigDecimal.valueOf(69.99)));
        assertFalse(RegrasAposentacao.prestacaoValida(BigDecimal.valueOf(81)));
        assertFalse(RegrasAposentacao.prestacaoValida(null));
    }

    @Test
    void antecipadaNoInteresseDaAdministracaoExigeAcordo() {
        assertEquals(422, status(() -> ProcessoAposentacao.abrir(pessoa, ModalidadeAposentacao.ANTECIPADA_INTERESSE_ADMINISTRACAO,
                ProcessoAposentacao.Iniciativa.ADMINISTRACAO, HOJE, null, null, false)));
        assertEquals(422, status(() -> ProcessoAposentacao.abrir(pessoa, null, null, HOJE, null, null, false)));
    }

    @Test
    void cicloCompletoPedidoDespachoDesligacaoAposentacao() {
        var p = ProcessoAposentacao.abrir(pessoa, ModalidadeAposentacao.LIMITE_IDADE, null, HOJE, null, null, false);
        assertEquals(EstadoProcessoAposentacao.PEDIDO, p.getEstado());
        assertEquals(422, status(() -> p.deferir(" ", null, null)));
        p.deferir("D-12/2026", HOJE, HOJE.plusMonths(2));
        assertEquals(409, status(() -> p.indeferir("x")));
        p.desligar(HOJE.plusMonths(1), null);
        assertEquals(409, status(() -> p.cancelar("desisto")));   // ja desligado
        assertEquals(422, status(() -> p.concluir(HOJE)));         // antes da desligacao
        p.concluir(HOJE.plusMonths(2));
        assertEquals(EstadoProcessoAposentacao.CONCLUIDO, p.getEstado());
        assertFalse(p.getEstado().emCurso());
    }

    @Test
    void preAposentacaoExigePercentagemNaDesligacao() {
        var p = ProcessoAposentacao.abrir(pessoa, ModalidadeAposentacao.PRE_APOSENTACAO,
                ProcessoAposentacao.Iniciativa.FUNCIONARIO, HOJE, null, null, false);
        p.deferir("D-1", HOJE, null);
        assertEquals(422, status(() -> p.desligar(HOJE, BigDecimal.valueOf(60))));
        p.desligar(HOJE, BigDecimal.valueOf(80));
        assertEquals(BigDecimal.valueOf(80), p.getPercentagemPrestacao());
    }

    @Test
    void soAAntecipadaExtingueOLugar() {
        assertTrue(ModalidadeAposentacao.ANTECIPADA_PEDIDO.extingueLugar());
        assertTrue(ModalidadeAposentacao.ANTECIPADA_INTERESSE_ADMINISTRACAO.extingueLugar());
        assertFalse(ModalidadeAposentacao.LIMITE_IDADE.extingueLugar());
        assertFalse(ModalidadeAposentacao.PRE_APOSENTACAO.extingueLugar());
    }

    @Test
    void prorrogacaoSoDepoisDos65ENuncaDepoisDos70() {
        LocalDate nasc = LocalDate.of(1961, 6, 1);   // 65 em 2026-06-01, 70 em 2031-06-01
        assertEquals(422, status(() -> ProrrogacaoPermanencia.pedir(pessoa, nasc, HOJE, false, "Interesse", LocalDate.of(2027, 1, 1))));
        assertEquals(422, status(() -> ProrrogacaoPermanencia.pedir(pessoa, nasc, HOJE, true, " ", LocalDate.of(2027, 1, 1))));
        assertEquals(422, status(() -> ProrrogacaoPermanencia.pedir(pessoa, nasc, HOJE, true, "Interesse", LocalDate.of(2031, 6, 2))));
        assertEquals(422, status(() -> ProrrogacaoPermanencia.pedir(pessoa, nasc, HOJE, true, "Interesse", LocalDate.of(2026, 5, 1))));
        var p = ProrrogacaoPermanencia.pedir(pessoa, nasc, HOJE, true, "Interesse publico excepcional", LocalDate.of(2031, 6, 1));
        p.autorizar("D-7/2026", HOJE);
        assertTrue(p.isAutorizada());
        assertEquals(409, status(() -> p.indeferir("x")));
    }
}
