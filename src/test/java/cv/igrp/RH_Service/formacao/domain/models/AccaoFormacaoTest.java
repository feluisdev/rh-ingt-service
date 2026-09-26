package cv.igrp.RH_Service.formacao.domain.models;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

/** BR-FRM: vagas, inscrições (o próprio pede, chefia e RH admitem), avaliação, garantia (art. 95.º b)) e plano. */
public class AccaoFormacaoTest {

    public static final LocalDate INICIO = LocalDate.of(2026, 10, 5);

    public static AccaoFormacao accao(Integer vagas, boolean custeada, Integer garantia) {
        return AccaoFormacao.planear("Contratação pública", "INA", AccaoFormacao.Modalidade.PRESENCIAL, false, INICIO, INICIO.plusDays(4),
                35, "09:00-16:00", "Praia", vagas, BigDecimal.valueOf(500), custeada, garantia, null, List.of());
    }

    @Test
    void garantiaSoQuandoCusteadaEEntre1e60Meses() {
        assertThrows(IgrpResponseStatusException.class, () -> accao(10, false, 12));
        assertThrows(IgrpResponseStatusException.class, () -> accao(10, true, 61));
        assertThrows(IgrpResponseStatusException.class, () -> AccaoFormacao.planear("T", null, null, false, INICIO, INICIO.minusDays(1), 7, null,
                null, null, null, false, null, null, List.of()));
    }

    @Test
    void oProprioSoPedeComInscricoesAbertasEAsVagasContam() {
        var a = accao(1, false, null);
        var ana = FuncionarioId.gerarNovo();
        var rui = FuncionarioId.gerarNovo();
        assertThrows(IgrpResponseStatusException.class, () -> a.inscrever(ana, AccaoFormacao.Origem.PROPRIO, INICIO));
        a.abrirInscricoes();
        var pedido = a.inscrever(ana, AccaoFormacao.Origem.PROPRIO, INICIO);
        assertEquals(AccaoFormacao.EstadoInscricao.PEDIDA, pedido.getEstado());
        assertThrows(IgrpResponseStatusException.class, () -> a.inscrever(ana, AccaoFormacao.Origem.RH, INICIO));
        a.inscrever(rui, AccaoFormacao.Origem.RH, INICIO);
        var ex = assertThrows(IgrpResponseStatusException.class, () -> a.decidir(pedido.getId(), true, null));
        assertEquals(409, ex.getStatusCode().value());
        assertThrows(IgrpResponseStatusException.class, () -> a.decidir(pedido.getId(), false, " "));
        a.decidir(pedido.getId(), false, "Sem vagas");
        assertEquals(AccaoFormacao.EstadoInscricao.RECUSADA, pedido.getEstado());
        // Recusado, pode voltar a pedir.
        a.inscrever(ana, AccaoFormacao.Origem.PROPRIO, INICIO);
    }

    @Test
    void iniciarDerrubaPedidosEAvaliarDaGarantia() {
        var a = accao(null, true, 24);
        a.abrirInscricoes();
        var ana = a.inscrever(FuncionarioId.gerarNovo(), AccaoFormacao.Origem.CHEFIA, INICIO);
        var rui = a.inscrever(FuncionarioId.gerarNovo(), AccaoFormacao.Origem.RH, INICIO);
        var pedido = a.inscrever(FuncionarioId.gerarNovo(), AccaoFormacao.Origem.PROPRIO, INICIO);
        a.iniciar();
        assertEquals(AccaoFormacao.EstadoInscricao.RECUSADA, pedido.getEstado());
        assertThrows(IgrpResponseStatusException.class, () -> a.avaliar(ana.getId(), AccaoFormacao.EstadoInscricao.APROVEITAMENTO, 6));
        a.avaliar(ana.getId(), AccaoFormacao.EstadoInscricao.APROVEITAMENTO, 5);
        assertEquals(INICIO.plusDays(4).plusMonths(24), ana.getGarantiaAte());
        assertThrows(IgrpResponseStatusException.class, a::concluir);
        a.avaliar(rui.getId(), AccaoFormacao.EstadoInscricao.FALTOU, null);
        assertNull(rui.getGarantiaAte());
        assertEquals(0, rui.getDiasPresenca());
        a.concluir();
        assertEquals(AccaoFormacao.Estado.CONCLUIDA, a.getEstado());
        assertThrows(IgrpResponseStatusException.class, () -> a.cancelar("tarde"));
    }

    @Test
    void naoComecaSemFormandosNemMudaDepoisDeComecar() {
        var a = accao(5, false, null);
        assertThrows(IgrpResponseStatusException.class, a::iniciar);
        a.inscrever(FuncionarioId.gerarNovo(), AccaoFormacao.Origem.RH, INICIO);
        a.iniciar();
        assertThrows(IgrpResponseStatusException.class, () -> a.definir("Outro", null, null, false, INICIO, INICIO, 7, null, null, null, null,
                false, null));
    }

    @Test
    void planoRecebeNecessidadesAteAprovarESatisfazComAAccao() {
        var p = PlanoFormacao.criar(2026, null, null, 2026);
        assertThrows(IgrpResponseStatusException.class, () -> p.aprovar("D", INICIO));
        var n = p.identificar("Excel avançado", FuncionarioId.gerarNovo(), AccaoFormacao.Origem.CHEFIA, PlanoFormacao.Prioridade.ALTA, "Relatórios");
        p.aprovar("Despacho 4/2026", INICIO);
        assertThrows(IgrpResponseStatusException.class, () -> p.identificar("Outra", null, null, null, null));
        var a = accao(null, false, null);
        p.planear(List.of(n.id()), a.getId());
        assertEquals(PlanoFormacao.EstadoNecessidade.PLANEADA, p.getNecessidades().get(0).estado());
        p.satisfazer(a.getId());
        assertEquals(PlanoFormacao.EstadoNecessidade.SATISFEITA, p.getNecessidades().get(0).estado());
        assertThrows(IgrpResponseStatusException.class, () -> PlanoFormacao.criar(2030, null, null, 2026));
    }
}
