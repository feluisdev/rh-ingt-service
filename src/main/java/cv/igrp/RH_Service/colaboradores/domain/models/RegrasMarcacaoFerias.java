package cv.igrp.RH_Service.colaboradores.domain.models;

import java.time.LocalDate;

/**
 * O que é preciso saber para validar uma marcação (DL n.º 3/2010, art. 5.º). Tudo vem de fora do
 * agregado — do saldo, do catálogo, dos parâmetros —, e é por isso que viaja num objecto: o
 * agregado aplica as regras sem saber de onde vieram os números.
 *
 * @param direitoTotal      dias que se podem marcar no ano: o direito vencido mais os recebidos do
 *                          ano anterior, menos os cedidos ao seguinte. Nulo se a instituição não
 *                          tiver tipo de férias no catálogo — aí não se valida o total.
 * @param maximoSeguidos    art. 5.º n.º 1: não se gozam seguidamente mais dias úteis do que os do
 *                          art. 2.º n.º 3 (o direito anual do catálogo; 22 por lei).
 * @param minimoInterpolado art. 5.º n.º 1: em gozo interpolado, um dos períodos tem pelo menos
 *                          estes dias (11 por lei; parâmetro).
 * @param anoDeIngresso     art. 3.º: no ano de ingresso não vale o mínimo do gozo interpolado.
 * @param janelaInicio      art. 5.º n.º 5: início da janela em que o dirigente fixa (1 de Maio).
 * @param janelaFim         fim da mesma janela (31 de Outubro).
 */
public record RegrasMarcacaoFerias(
        Integer direitoTotal,
        int maximoSeguidos,
        int minimoInterpolado,
        boolean anoDeIngresso,
        LocalDate janelaInicio,
        LocalDate janelaFim) {
}
