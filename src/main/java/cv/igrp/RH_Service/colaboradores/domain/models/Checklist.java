package cv.igrp.RH_Service.colaboradores.domain.models;

import cv.igrp.RH_Service.colaboradores.domain.valueobject.ChecklistId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.ItemChecklistId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.ItemChecklistModeloId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * <b>A checklist de entrada ou de saída de um colaborador</b> (BR-CHK-04..12). Nasce da admissão (ou reingresso) e da
 * cessação, com os itens do modelo; cada área marca os seus. Fica <b>concluída</b> quando nenhum item está pendente, e
 * reabre-se se um item voltar a pendente. A saída não trava a cessação — é o registo de que tudo foi entregue.
 *
 * <p>Base: programas de acolhimento e integração (Lei n.º 20/X/2023, art. 141.º n.º 2); dever de conservar e usar bem os
 * bens do Estado que lhe foram confiados (deveres gerais) e devolução do cartão (art. 25.º) — o resto é prática corrente.
 */
@Getter
public class Checklist {

    public enum Estado { ABERTA, CONCLUIDA, CANCELADA }

    public enum EstadoItem { PENDENTE, FEITO, NAO_APLICAVEL }

    /** Um item copiado do modelo (ou acrescentado à mão, sem modelo). */
    @Getter
    public static class Item {
        private ItemChecklistId id;
        private ItemChecklistModeloId modeloId;
        private String codigo;
        private String descricao;
        private ResponsavelChecklist responsavel;
        private boolean obrigatorio;
        private LocalDate prazo;
        private int ordem;
        private EstadoItem estado;
        private LocalDate data;
        private String observacao;
        private boolean automatico;

        private Item() {}

        public static Item reconstruir(ItemChecklistId id, ItemChecklistModeloId modeloId, String codigo, String descricao,
                                       ResponsavelChecklist responsavel, boolean obrigatorio, LocalDate prazo, int ordem,
                                       EstadoItem estado, LocalDate data, String observacao, boolean automatico) {
            var i = new Item();
            i.id = id;
            i.modeloId = modeloId;
            i.codigo = codigo;
            i.descricao = descricao;
            i.responsavel = responsavel;
            i.obrigatorio = obrigatorio;
            i.prazo = prazo;
            i.ordem = ordem;
            i.estado = estado;
            i.data = data;
            i.observacao = observacao;
            i.automatico = automatico;
            return i;
        }

        public boolean pendente() {
            return estado == EstadoItem.PENDENTE;
        }

        public boolean atrasado(LocalDate hoje) {
            return pendente() && prazo != null && hoje.isAfter(prazo);
        }
    }

    private ChecklistId id;
    private FuncionarioId funcionarioId;
    private TipoChecklist tipo;
    private LocalDate dataReferencia;
    private Estado estado;
    private LocalDate abertaEm;
    private LocalDate concluidaEm;
    private String motivoCancelamento;
    private List<Item> itens = new ArrayList<>();

    private Checklist() {}

    /** Abre com os itens activos do modelo, pela ordem; o prazo de cada um conta da data de referência. */
    public static Checklist abrir(FuncionarioId funcionarioId, TipoChecklist tipo, LocalDate dataReferencia,
                                  List<ItemChecklistModelo> modelo, LocalDate hoje) {
        if (funcionarioId == null || tipo == null) throw invalido("Indique o colaborador e o tipo de checklist.");
        var c = new Checklist();
        c.id = ChecklistId.gerarNovo();
        c.funcionarioId = funcionarioId;
        c.tipo = tipo;
        c.dataReferencia = dataReferencia != null ? dataReferencia : hoje;
        c.estado = Estado.ABERTA;
        c.abertaEm = hoje;
        int i = 0;
        for (var m : modelo.stream().filter(ItemChecklistModelo::isActivo).filter(m -> m.getTipo() == tipo)
                .sorted(java.util.Comparator.comparingInt(ItemChecklistModelo::getOrdem)).toList()) {
            c.itens.add(Item.reconstruir(ItemChecklistId.gerarNovo(), m.getId(), m.getCodigo(), m.getDescricao(), m.getResponsavel(),
                    m.isObrigatorio(), m.getPrazoDias() != null ? c.dataReferencia.plusDays(m.getPrazoDias()) : null, i++,
                    EstadoItem.PENDENTE, null, null, false));
        }
        c.recalcular(hoje);
        return c;
    }

    public static Checklist reconstruir(ChecklistId id, FuncionarioId funcionarioId, TipoChecklist tipo, LocalDate dataReferencia,
                                        Estado estado, LocalDate abertaEm, LocalDate concluidaEm, String motivoCancelamento,
                                        List<Item> itens) {
        var c = new Checklist();
        c.id = id;
        c.funcionarioId = funcionarioId;
        c.tipo = tipo;
        c.dataReferencia = dataReferencia;
        c.estado = estado;
        c.abertaEm = abertaEm;
        c.concluidaEm = concluidaEm;
        c.motivoCancelamento = motivoCancelamento;
        c.itens = new ArrayList<>(itens);
        c.itens.sort(java.util.Comparator.comparingInt(Item::getOrdem));
        return c;
    }

    public List<Item> getItens() {
        return Collections.unmodifiableList(itens);
    }

    /**
     * Marca um item: feito, não aplicável (num obrigatório, com a razão), ou de volta a pendente. A checklist conclui-se
     * quando nada fica pendente; reabre se um item voltar a pendente.
     */
    public Item marcar(ItemChecklistId itemId, EstadoItem novo, String observacao, LocalDate data, LocalDate hoje) {
        exigirNaoCancelada();
        if (novo == null) throw invalido("Diga se o item está feito, não se aplica ou volta a pendente.");
        var item = item(itemId);
        String obs = observacao == null || observacao.isBlank() ? null : observacao.trim();
        if (novo == EstadoItem.NAO_APLICAVEL && item.obrigatorio && obs == null)
            throw invalido("Este item é obrigatório: explique porque não se aplica.");
        if (data != null && data.isAfter(hoje)) throw invalido("A data do item não pode ser no futuro.");
        item.estado = novo;
        item.observacao = obs;
        item.data = novo == EstadoItem.PENDENTE ? null : (data != null ? data : hoje);
        item.automatico = false;
        recalcular(hoje);
        return item;
    }

    /** O sistema cumpriu o item com este código (o cartão entregue, o provimento registado…). */
    public Optional<Item> cumprirPorCodigo(String codigo, String observacao, LocalDate data) {
        if (estado == Estado.CANCELADA || codigo == null) return Optional.empty();
        var item = itens.stream().filter(i -> codigo.equals(i.codigo) && i.pendente()).findFirst();
        item.ifPresent(i -> {
            i.estado = EstadoItem.FEITO;
            i.data = data;
            i.observacao = observacao;
            i.automatico = true;
            recalcular(data);
        });
        return item;
    }

    /** Um item a mais, só desta checklist. */
    public Item acrescentar(String descricao, ResponsavelChecklist responsavel, boolean obrigatorio, LocalDate prazo, LocalDate hoje) {
        exigirNaoCancelada();
        if (descricao == null || descricao.isBlank()) throw invalido("Descreva o que há a fazer.");
        if (responsavel == null) throw invalido("Indique quem trata do item.");
        var item = Item.reconstruir(ItemChecklistId.gerarNovo(), null, null, descricao.trim(), responsavel, obrigatorio, prazo,
                itens.stream().mapToInt(Item::getOrdem).max().orElse(-1) + 1, EstadoItem.PENDENTE, null, null, false);
        itens.add(item);
        recalcular(hoje);
        return item;
    }

    public void cancelar(String motivo) {
        exigirNaoCancelada();
        if (motivo == null || motivo.isBlank()) throw invalido("Cancelar a checklist exige o motivo.");
        this.motivoCancelamento = motivo.trim();
        this.estado = Estado.CANCELADA;
    }

    public long pendentes() {
        return itens.stream().filter(Item::pendente).count();
    }

    public long atrasados(LocalDate hoje) {
        return itens.stream().filter(i -> i.atrasado(hoje)).count();
    }

    private void recalcular(LocalDate hoje) {
        if (estado == Estado.CANCELADA) return;
        if (pendentes() == 0) {
            if (estado != Estado.CONCLUIDA) concluidaEm = hoje;
            estado = Estado.CONCLUIDA;
        } else {
            estado = Estado.ABERTA;
            concluidaEm = null;
        }
    }

    private Item item(ItemChecklistId itemId) {
        return itens.stream().filter(i -> i.id.equals(itemId)).findFirst()
                .orElseThrow(() -> IgrpResponseStatusException.notFound("Item não encontrado nesta checklist."));
    }

    private void exigirNaoCancelada() {
        if (estado == Estado.CANCELADA) throw IgrpResponseStatusException.conflict("Esta checklist foi cancelada.");
    }

    static IgrpResponseStatusException invalido(String m) {
        return IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY, m);
    }
}
