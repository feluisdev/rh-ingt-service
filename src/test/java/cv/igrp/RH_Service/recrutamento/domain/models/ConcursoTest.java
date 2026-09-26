package cv.igrp.RH_Service.recrutamento.domain.models;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cv.igrp.RH_Service.recrutamento.domain.service.ClassificacaoConcurso;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

/** BR-CNC: abertura (métodos obrigatórios, quota, júri), nota eliminatória, classificação e ordem de provimento. */
public class ConcursoTest {

    public static final LocalDate HOJE = LocalDate.of(2026, 9, 1);

    public static List<Concurso.Metodo> metodosCompletos() {
        return List.of(new Concurso.Metodo(MetodoSelecao.TRIAGEM_CURRICULAR, 20, false, null),
                new Concurso.Metodo(MetodoSelecao.PROVA_CONHECIMENTOS, 40, true, BigDecimal.valueOf(10)),
                new Concurso.Metodo(MetodoSelecao.AVALIACAO_COMPETENCIAS, 20, false, null),
                new Concurso.Metodo(MetodoSelecao.ENTREVISTA, 20, false, null));
    }

    public static List<Concurso.MembroJuri> juri() {
        return List.of(new Concurso.MembroJuri(Concurso.PapelJuri.PRESIDENTE, "P", null),
                new Concurso.MembroJuri(Concurso.PapelJuri.VOGAL, "V1", null), new Concurso.MembroJuri(Concurso.PapelJuri.VOGAL, "V2", null),
                new Concurso.MembroJuri(Concurso.PapelJuri.SUPLENTE, "S1", null), new Concurso.MembroJuri(Concurso.PapelJuri.SUPLENTE, "S2", null));
    }

    public static Concurso pronto(Concurso.Modalidade modalidade, String vinculo, int lugares, Integer quota) {
        var c = Concurso.criar("C-1/2026", Concurso.Finalidade.INGRESSO, null, modalidade, vinculo, UUID.randomUUID());
        var ls = new ArrayList<UUID>();
        for (int i = 0; i < lugares; i++) ls.add(UUID.randomUUID());
        c.definirLugares(ls);
        c.definirRequisitos(null, null, quota);
        c.definirMetodos(metodosCompletos(), null);
        c.definirJuri(juri());
        c.definirPrazo(HOJE, HOJE, HOJE.plusDays(15));
        return c;
    }

    @Test
    void abreComTudoDefinido() {
        var c = pronto(Concurso.Modalidade.EXTERNO, null, 2, 0);
        c.abrir(HOJE);
        assertEquals(Concurso.Estado.ABERTO, c.getEstado());
        assertTrue(c.aceitaCandidatura(HOJE.plusDays(15)));
        assertTrue(!c.aceitaCandidatura(HOJE.plusDays(16)));
    }

    @Test
    void externoExigeQuota() {
        var c = pronto(Concurso.Modalidade.EXTERNO, null, 2, null);
        assertThrows(IgrpResponseStatusException.class, () -> c.abrir(HOJE));
        var i = pronto(Concurso.Modalidade.INTERNO, null, 2, null);
        i.abrir(HOJE);
        assertEquals(Concurso.Estado.ABERTO, i.getEstado());
    }

    @Test
    void quotaNaoPassaOsLugares() {
        var c = pronto(Concurso.Modalidade.EXTERNO, null, 1, 2);
        assertThrows(IgrpResponseStatusException.class, () -> c.abrir(HOJE));
    }

    @Test
    void faltaMetodoObrigatorioSalvoTermoOuDespacho() {
        var soDois = List.of(new Concurso.Metodo(MetodoSelecao.TRIAGEM_CURRICULAR, 50, false, null),
                new Concurso.Metodo(MetodoSelecao.ENTREVISTA, 50, false, null));
        var c = pronto(Concurso.Modalidade.INTERNO, "NOMEACAO_PROVISORIA", 1, null);
        c.definirMetodos(soDois, null);
        assertThrows(IgrpResponseStatusException.class, () -> c.abrir(HOJE));

        var termo = pronto(Concurso.Modalidade.INTERNO, "CONTRATO_TERMO_CERTO", 1, null);
        termo.definirMetodos(soDois, null);
        termo.abrir(HOJE);
        assertEquals(Concurso.Estado.ABERTO, termo.getEstado());

        var dispensa = pronto(Concurso.Modalidade.INTERNO, null, 1, null);
        dispensa.definirMetodos(soDois, "Despacho 3/2026");
        dispensa.abrir(HOJE);
        assertEquals(Concurso.Estado.ABERTO, dispensa.getEstado());
    }

    @Test
    void ponderacoesSomam100EJuriCompleto() {
        var c = Concurso.criar("X", Concurso.Finalidade.ACESSO, null, Concurso.Modalidade.INTERNO, null, UUID.randomUUID());
        assertThrows(IgrpResponseStatusException.class, () -> c.definirMetodos(
                List.of(new Concurso.Metodo(MetodoSelecao.ENTREVISTA, 60, false, null)), null));
        assertThrows(IgrpResponseStatusException.class, () -> c.definirJuri(juri().subList(0, 3)));
    }

    @Test
    void abertoNaoSeEdita() {
        var c = pronto(Concurso.Modalidade.INTERNO, null, 1, null);
        c.abrir(HOJE);
        assertThrows(IgrpResponseStatusException.class, () -> c.definirLugares(List.of(UUID.randomUUID())));
    }

    @Test
    void homologacaoDaReservaDe18Meses() {
        var c = pronto(Concurso.Modalidade.INTERNO, null, 1, null);
        c.abrir(HOJE);
        c.encerrarCandidaturas(HOJE.plusDays(16));
        c.iniciarAvaliacao(false);
        c.listaProvisoria(false);
        c.homologar("Despacho 9/2026", LocalDate.of(2026, 11, 2));
        assertEquals(LocalDate.of(2028, 5, 2), c.getReservaAte());
        assertTrue(c.reservaValida(LocalDate.of(2028, 5, 2)));
        assertTrue(!c.reservaValida(LocalDate.of(2028, 5, 3)));
    }

    @Test
    void naoEncerraAntesDoFimDoPrazo() {
        var c = pronto(Concurso.Modalidade.INTERNO, null, 1, null);
        c.abrir(HOJE);
        assertThrows(IgrpResponseStatusException.class, () -> c.encerrarCandidaturas(HOJE.plusDays(15)));
    }

    public static Candidatura candidato(Concurso c, String nome, boolean deficiencia, LocalDate data, int... notas) {
        var x = Candidatura.apresentar(c, nome, "D" + nome, null, null, null, null, deficiencia, null, false, data);
        x.admitir();
        var ms = c.getMetodos();
        for (int i = 0; i < ms.size(); i++) x.registarNota(ms, ms.get(i).metodo(), BigDecimal.valueOf(notas[i]));
        return x;
    }

    @Test
    void notaEliminatoriaReprovaEMediaPonderadaOrdena() {
        var c = pronto(Concurso.Modalidade.EXTERNO, null, 2, 1);
        c.abrir(HOJE);
        var a = candidato(c, "A", false, HOJE, 14, 16, 14, 14);          // 14,80
        var b = candidato(c, "B", false, HOJE, 20, 9, 20, 20);           // eliminado na prova
        var d = candidato(c, "D", true, HOJE.plusDays(1), 12, 12, 12, 12); // 12,00
        var e = candidato(c, "E", false, HOJE.plusDays(1), 12, 12, 12, 12); // 12,00, mas depois
        assertEquals(Candidatura.Estado.REPROVADA, b.getEstado());

        var ordem = ClassificacaoConcurso.classificar(c, new ArrayList<>(List.of(a, b, d, e)));
        assertEquals(List.of(a, d, e), ordem);
        assertEquals(new BigDecimal("14.80"), a.getClassificacaoFinal());
        assertNull(b.getPosicao());
        assertEquals(3, e.getPosicao());

        // A quota (1) põe D à frente; esgotada, volta a ordem geral.
        assertEquals(List.of(d, a, e), ClassificacaoConcurso.ordemDeProvimento(c, ordem, 0));
        assertEquals(List.of(a, d, e), ClassificacaoConcurso.ordemDeProvimento(c, ordem, 1));
    }

    @Test
    void exclusaoPassaPelaAudiencia() {
        var c = pronto(Concurso.Modalidade.INTERNO, null, 1, null);
        c.abrir(HOJE);
        var x = Candidatura.apresentar(c, "Z", "DZ", null, null, null, null, false, null, true, HOJE);
        x.proporExclusao("Sem habilitação", HOJE);
        assertEquals(Candidatura.Estado.EM_AUDIENCIA, x.getEstado());
        assertEquals(HOJE.plusDays(Candidatura.DIAS_AUDIENCIA), x.getAudienciaAte());
        x.decidirAudiencia(false, "Juntou o diploma");
        assertEquals(Candidatura.Estado.ADMITIDA, x.getEstado());
        assertNull(x.getMotivoExclusao());
    }

    @Test
    void candidaturaForaDoPrazo() {
        var c = pronto(Concurso.Modalidade.INTERNO, null, 1, null);
        c.abrir(HOJE);
        assertThrows(IgrpResponseStatusException.class,
                () -> Candidatura.apresentar(c, "Z", "DZ", null, null, null, null, false, null, true, HOJE.plusDays(20)));
    }
}
