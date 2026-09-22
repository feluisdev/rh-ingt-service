package cv.igrp.RH_Service.colaboradores.domain.models;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.TipoAusenciaId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

/**
 * <b>Suspensão de férias</b> — DL n.º 3/2010, art. 8.º.
 *
 * <p>As férias suspendem-se por parentalidade (n.º 1), doença e assistência a familiares (n.º 2)
 * e razões imperiosas de serviço (n.º 5). O n.º 3 diz a partir de quando: «a partir da data da
 * entrada no serviço do documento comprovativo» — logo o último dia de férias é a véspera, a
 * mesma leitura que o regresso antecipado da licença.
 *
 * <p>Até aqui um pedido de férias e um de doença não se falavam: quem adoecesse a meio perdia-as.
 */
class SuspensaoFeriasTest {

    private static final FuncionarioId FUNCIONARIO = FuncionarioId.gerarNovo();
    private static final TipoAusenciaId FERIAS = TipoAusenciaId.gerarNovo();
    private static final LocalDate HOJE = LocalDate.now();
    private static final String MOTIVO = "doenca - atestado entregue no servico";

    private static PedidoAusencia feriasAprovadas(LocalDate inicio, LocalDate fim, int dias) {
        var p = PedidoAusencia.criar(FUNCIONARIO, FERIAS, inicio, fim, dias, "ferias anuais", null);
        p.aprovar(FUNCIONARIO, inicio.minusDays(5), null);
        return p;
    }

    /** O último dia de férias é a véspera da suspensão — «a partir de» inclui o próprio dia. */
    @Test
    void oUltimoDiaDeFeriasEhAVesperaDaSuspensao() {
        var p = feriasAprovadas(HOJE.minusDays(10), HOJE.plusDays(10), 15);

        p.suspender(HOJE, MOTIVO, HOJE);

        assertEquals(HOJE, p.getSuspensoEm());
        assertEquals(HOJE.minusDays(1), p.getDataFim());
        assertTrue(p.isSuspenso());
    }

    /**
     * O estado não muda: a decisão foi tomada e não se desfaz; o que encurta é o período. É a
     * lição da V48 — não voltar a guardar o período dentro do campo que guarda a decisão.
     */
    @Test
    void oPedidoContinuaAprovado() {
        var p = feriasAprovadas(HOJE.minusDays(10), HOJE.plusDays(10), 15);

        p.suspender(HOJE, MOTIVO, HOJE);

        assertEquals(EstadoPedidoAusencia.APROVADO, p.getEstado());
    }

    @Test
    void oMotivoEhObrigatorio() {
        var p = feriasAprovadas(HOJE.minusDays(10), HOJE.plusDays(10), 15);

        var ex = assertThrows(IgrpResponseStatusException.class, () -> p.suspender(HOJE, "  ", HOJE));

        assertEquals(400, ex.getStatusCode().value());
        assertFalse(p.isSuspenso());
    }

    /**
     * Suspender no próprio dia de início deixaria o fim na véspera do começo — a mesma armadilha
     * que a V48 fechou na licença. Quem quer desfazer o pedido cancela-o.
     */
    @Test
    void naoSeSuspendeNoProprioDiaDeInicio() {
        var p = feriasAprovadas(HOJE, HOJE.plusDays(10), 8);

        var ex = assertThrows(IgrpResponseStatusException.class, () -> p.suspender(HOJE, MOTIVO, HOJE));

        assertEquals(409, ex.getStatusCode().value());
        assertFalse(p.getDataFim().isBefore(p.getDataInicio()));
    }

    @Test
    void naoSeSuspendeOQueAindaNaoComecou() {
        var p = feriasAprovadas(HOJE.plusDays(5), HOJE.plusDays(20), 11);

        var ex = assertThrows(IgrpResponseStatusException.class,
                () -> p.suspender(HOJE, MOTIVO, HOJE));

        assertEquals(409, ex.getStatusCode().value());
    }

    @Test
    void naoSeSuspendeOQueJaTerminou() {
        var p = feriasAprovadas(HOJE.minusDays(30), HOJE.minusDays(10), 15);

        var ex = assertThrows(IgrpResponseStatusException.class,
                () -> p.suspender(HOJE, MOTIVO, HOJE));

        assertEquals(409, ex.getStatusCode().value());
    }

    @Test
    void aDataDaSuspensaoNaoPodeSerFutura() {
        var p = feriasAprovadas(HOJE.minusDays(10), HOJE.plusDays(10), 15);

        var ex = assertThrows(IgrpResponseStatusException.class,
                () -> p.suspender(HOJE.plusDays(2), MOTIVO, HOJE));

        assertEquals(400, ex.getStatusCode().value());
    }

    @Test
    void naoSeSuspendeDuasVezes() {
        var p = feriasAprovadas(HOJE.minusDays(10), HOJE.plusDays(10), 15);
        p.suspender(HOJE, MOTIVO, HOJE);

        var ex = assertThrows(IgrpResponseStatusException.class,
                () -> p.suspender(HOJE, MOTIVO, HOJE));

        assertEquals(409, ex.getStatusCode().value());
    }

    /** Um pedido por decidir não tem férias a correr. */
    @Test
    void soUmPedidoAprovadoSeSuspende() {
        var p = PedidoAusencia.criar(FUNCIONARIO, FERIAS, HOJE.minusDays(10), HOJE.plusDays(10), 15, "ferias", null);

        var ex = assertThrows(IgrpResponseStatusException.class,
                () -> p.suspender(HOJE, MOTIVO, HOJE));

        assertEquals(409, ex.getStatusCode().value());
    }
}
