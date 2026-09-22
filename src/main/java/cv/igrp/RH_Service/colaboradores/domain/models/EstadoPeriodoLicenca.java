package cv.igrp.RH_Service.colaboradores.domain.models;

/**
 * Onde a licença ou mobilidade está <b>no seu período</b>, numa data.
 *
 * <p>Este é o segundo eixo do registo, e não se guarda: <b>deriva das datas</b>. O primeiro eixo
 * é a decisão — o despacho — e vive no {@code status} ({@code PENDING}, {@code APPROVED},
 * {@code REJECTED}, {@code CANCELLED}).
 *
 * <p>A separação é a do próprio art. 44.º do DL n.º 3/2010: o n.º 1 define a licença como
 * «ausência prolongada do serviço» — um <i>período</i> —, e o n.º 2 faz a concessão depender «do
 * pedido do interessado e do despacho da autoridade competente» — um <i>acto</i>. Guardar os dois
 * no mesmo campo, como se fazia até à V48, é que tornava possível aprovar em Setembro uma licença
 * de Outubro e pôr a pessoa de licença em Setembro.
 *
 * <p>Só faz sentido perguntar isto a um registo deferido: um pedido por decidir, indeferido ou
 * cancelado não tem período nenhum a decorrer.
 */
public enum EstadoPeriodoLicenca {

    /** Deferida, mas a data de início ainda não chegou. */
    POR_INICIAR,

    /** A decorrer na data perguntada. É o que quer dizer «estar de licença». */
    EM_CURSO,

    /**
     * A data de fim já passou. Pelo art. 46.º n.º 3 o que dependia da licença «caduca
     * automaticamente» — não é preciso ninguém encerrar nada para o período acabar.
     */
    TERMINADA
}
