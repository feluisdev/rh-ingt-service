package cv.igrp.RH_Service.colaboradores.domain.models;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cv.igrp.RH_Service.colaboradores.domain.valueobject.TipoAusenciaId;
import cv.igrp.RH_Service.parametrizacoes.domain.models.RegimeAusencia;

import org.junit.jupiter.api.Test;

/**
 * Os três limites do art. 15.º n.º 1 do DL n.º 3/2010 — e sobretudo o que os distingue.
 *
 * <p>Até à V53 o catálogo só sabia dizer «X dias por ano», e o limite era sempre somado ao ano
 * civil. Mas o artigo quase nunca fala em anos: «até 6, <b>por ocasião</b> do casamento», «até 8,
 * por motivo de <b>falecimento</b> do cônjuge», «duas por <b>cada</b> prova». Escrever isso no
 * limite anual errava nos dois sentidos ao mesmo tempo — recusava o segundo funeral do ano e
 * deixava passar oito dias seguidos de uma só vez.
 */
class LimitesDeDiasTipoAusenciaTest {

    private static TipoAusencia tipo(Integer porAno, Integer porOcorrencia, Integer porMes) {
        return TipoAusencia.reconstituir(TipoAusenciaId.gerarNovo(), "Tipo", "TIPO",
                false, false, porAno, porOcorrencia, porMes, "PESSOAL", true, RegimeAusencia.FALTA, null, null);
    }

    /** Art. 15.º n.º 1 al. b): oito dias por falecimento do cônjuge. Oito cabem. */
    @Test
    void oLimitePorOcorrenciaDeixaPassarOQueACabeNele() {
        assertFalse(tipo(null, 8, null).excedeLimitePorOcorrencia(8));
    }

    @Test
    void eRecusaOPedidoQueOExcede() {
        assertTrue(tipo(null, 8, null).excedeLimitePorOcorrencia(9));
    }

    /**
     * A razão de tudo isto: quem perde dois familiares no mesmo ano tem direito às duas
     * ausências. O limite por ocorrência olha só para o pedido que tem à frente — não sabe, nem
     * quer saber, quantos dias já foram dados este ano.
     */
    @Test
    void oSegundoLutoDoMesmoAnoNaoEsbarraNoPrimeiro() {
        var luto = tipo(null, 8, null);

        assertFalse(luto.excedeLimitePorOcorrencia(8));
        assertFalse(luto.excedeLimitePorOcorrencia(8));
    }

    /** Sem limite desta natureza — que é o caso da greve, da prisão preventiva, da calamidade. */
    @Test
    void semLimiteNenhumNadaSeRecusa() {
        var semTecto = tipo(null, null, null);

        assertFalse(semTecto.excedeLimitePorOcorrencia(365));
        assertFalse(semTecto.excedeLimiteAnual(300, 100));
        assertFalse(semTecto.excedeLimiteMensal(28, 3));
    }

    /** Art. 15.º n.º 1 al. j): quinze por ano. Aqui, sim, soma-se o que já foi dado. */
    @Test
    void oLimiteAnualSomaOQueJaFoiDadoNoAno() {
        var assistencia = tipo(15, null, null);

        assertFalse(assistencia.excedeLimiteAnual(10, 5));
        assertTrue(assistencia.excedeLimiteAnual(10, 6));
    }

    /** Art. 15.º n.º 1 al. o): um por mês. O mês tem contagem própria. */
    @Test
    void oLimiteMensalSomaOQueJaFoiDadoNoMes() {
        var porContaDeFerias = tipo(null, null, 1);

        assertFalse(porContaDeFerias.excedeLimiteMensal(0, 1));
        assertTrue(porContaDeFerias.excedeLimiteMensal(1, 1));
    }

    /**
     * Al. q): «não podendo em caso algum ultrapassar 6 dias em cada ano civil <b>e um dia por
     * mês</b>». É esta alínea que obriga a ter três valores independentes em vez de um valor com
     * uma classificação ao lado: os dois tectos valem <b>ao mesmo tempo</b>, e um pedido pode
     * caber no ano e não caber no mês.
     */
    @Test
    void aMesmaLinhaPodeTerTectoAnualEMensalAoMesmoTempo() {
        var autorizada = tipo(6, null, 1);

        assertFalse(autorizada.excedeLimiteAnual(2, 1));
        assertTrue(autorizada.excedeLimiteMensal(1, 1));
    }
}
