package cv.igrp.RH_Service.colaboradores.domain.models;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

/** BR-CHK: cópia do modelo com prazos, marcação, conclusão automática e reabertura, cumprimento pelo código. */
public class ChecklistTest {

    public static final LocalDate ENTRADA = LocalDate.of(2026, 9, 1);

    public static List<ItemChecklistModelo> modelo() {
        var inactivo = ItemChecklistModelo.criar(TipoChecklist.ENTRADA, "VELHO", "Já não se faz", ResponsavelChecklist.RH, true, 0, 0);
        inactivo.activar(false);
        return List.of(
                ItemChecklistModelo.criar(TipoChecklist.ENTRADA, "CARTAO_PROFISSIONAL", "Entregar o cartão", ResponsavelChecklist.RH, true, 30, 2),
                ItemChecklistModelo.criar(TipoChecklist.ENTRADA, null, "Acolhimento", ResponsavelChecklist.CHEFIA, false, null, 1),
                ItemChecklistModelo.criar(TipoChecklist.SAIDA, null, "Devolver bens", ResponsavelChecklist.PATRIMONIO, true, 0, 1),
                inactivo);
    }

    @Test
    void copiaOsActivosDoTipoPelaOrdemComPrazo() {
        var c = Checklist.abrir(FuncionarioId.gerarNovo(), TipoChecklist.ENTRADA, ENTRADA, modelo(), ENTRADA);
        assertEquals(2, c.getItens().size());
        assertEquals("Acolhimento", c.getItens().get(0).getDescricao());
        assertNull(c.getItens().get(0).getPrazo());
        assertEquals(ENTRADA.plusDays(30), c.getItens().get(1).getPrazo());
        assertEquals(Checklist.Estado.ABERTA, c.getEstado());
    }

    @Test
    void concluiSemPendentesEReabre() {
        var c = Checklist.abrir(FuncionarioId.gerarNovo(), TipoChecklist.ENTRADA, ENTRADA, modelo(), ENTRADA);
        var a = c.getItens().get(0).getId();
        var b = c.getItens().get(1).getId();
        c.marcar(a, Checklist.EstadoItem.NAO_APLICAVEL, null, null, ENTRADA);
        assertEquals(Checklist.Estado.ABERTA, c.getEstado());
        c.marcar(b, Checklist.EstadoItem.FEITO, null, null, ENTRADA.plusDays(3));
        assertEquals(Checklist.Estado.CONCLUIDA, c.getEstado());
        assertEquals(ENTRADA.plusDays(3), c.getConcluidaEm());
        c.marcar(b, Checklist.EstadoItem.PENDENTE, null, null, ENTRADA.plusDays(4));
        assertEquals(Checklist.Estado.ABERTA, c.getEstado());
        assertNull(c.getConcluidaEm());
        assertNull(c.getItens().get(1).getData());
    }

    @Test
    void obrigatorioNaoAplicavelPedeARazao() {
        var c = Checklist.abrir(FuncionarioId.gerarNovo(), TipoChecklist.ENTRADA, ENTRADA, modelo(), ENTRADA);
        var cartao = c.getItens().get(1).getId();
        assertThrows(IgrpResponseStatusException.class, () -> c.marcar(cartao, Checklist.EstadoItem.NAO_APLICAVEL, " ", null, ENTRADA));
        c.marcar(cartao, Checklist.EstadoItem.NAO_APLICAVEL, "Já tem cartão de outra entidade", null, ENTRADA);
        assertThrows(IgrpResponseStatusException.class,
                () -> c.marcar(cartao, Checklist.EstadoItem.FEITO, null, ENTRADA.plusDays(1), ENTRADA));
    }

    @Test
    void cumprePeloCodigoSoSePendente() {
        var c = Checklist.abrir(FuncionarioId.gerarNovo(), TipoChecklist.ENTRADA, ENTRADA, modelo(), ENTRADA);
        assertTrue(c.cumprirPorCodigo("CARTAO_PROFISSIONAL", "Cartão n.º 1", ENTRADA.plusDays(5)).isPresent());
        var item = c.getItens().get(1);
        assertTrue(item.isAutomatico());
        assertEquals(Checklist.EstadoItem.FEITO, item.getEstado());
        assertTrue(c.cumprirPorCodigo("CARTAO_PROFISSIONAL", "outra vez", ENTRADA.plusDays(6)).isEmpty());
        assertTrue(c.cumprirPorCodigo("NAO_EXISTE", null, ENTRADA).isEmpty());
    }

    @Test
    void atrasoEItemAcrescentado() {
        var c = Checklist.abrir(FuncionarioId.gerarNovo(), TipoChecklist.ENTRADA, ENTRADA, modelo(), ENTRADA);
        assertEquals(0, c.atrasados(ENTRADA.plusDays(30)));
        assertEquals(1, c.atrasados(ENTRADA.plusDays(31)));
        var extra = c.acrescentar("Formação em segurança", ResponsavelChecklist.RH, false, ENTRADA.plusDays(10), ENTRADA);
        assertEquals(2, extra.getOrdem());
        assertEquals(3, c.pendentes());
    }

    @Test
    void canceladaNaoSeMexe() {
        var c = Checklist.abrir(FuncionarioId.gerarNovo(), TipoChecklist.SAIDA, ENTRADA, modelo(), ENTRADA);
        assertThrows(IgrpResponseStatusException.class, () -> c.cancelar(null));
        c.cancelar("Cessação anulada");
        assertEquals(Checklist.Estado.CANCELADA, c.getEstado());
        var item = c.getItens().get(0).getId();
        assertThrows(IgrpResponseStatusException.class, () -> c.marcar(item, Checklist.EstadoItem.FEITO, null, null, ENTRADA));
        assertFalse(c.cumprirPorCodigo("X", null, ENTRADA).isPresent());
    }
}
