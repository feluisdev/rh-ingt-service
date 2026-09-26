package cv.igrp.RH_Service.colaboradores.domain.models;

import cv.igrp.RH_Service.colaboradores.domain.valueobject.ItemChecklistModeloId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * <b>Um item do modelo de checklist</b> (BR-CHK-01..03): o que cada instituição quer que se faça à entrada e à saída
 * varia, por isso é catálogo, não código. Ao abrir uma checklist, os itens activos do tipo copiam-se para ela — mudar o
 * modelo depois não mexe nas checklists já abertas.
 *
 * <p>{@link #codigo}: os itens que o sistema reconhece e marca sozinho (o cartão entregue, o provimento registado…);
 * único por tipo. {@link #prazoDias}: dias a contar da data de referência (a admissão, a cessação); negativo na saída
 * quer dizer antes do último dia.
 */
@Getter
public class ItemChecklistModelo {

    private ItemChecklistModeloId id;
    private TipoChecklist tipo;
    private String codigo;
    private String descricao;
    private ResponsavelChecklist responsavel;
    private boolean obrigatorio;
    private Integer prazoDias;
    private int ordem;
    private boolean activo;

    private ItemChecklistModelo() {}

    public static ItemChecklistModelo criar(TipoChecklist tipo, String codigo, String descricao, ResponsavelChecklist responsavel,
                                            boolean obrigatorio, Integer prazoDias, int ordem) {
        if (tipo == null) throw invalido("Diga se o item é da checklist de entrada ou de saída.");
        var m = new ItemChecklistModelo();
        m.id = ItemChecklistModeloId.gerarNovo();
        m.tipo = tipo;
        m.codigo = codigo == null || codigo.isBlank() ? null : codigo.trim().toUpperCase();
        m.activo = true;
        m.definir(descricao, responsavel, obrigatorio, prazoDias, ordem);
        return m;
    }

    public static ItemChecklistModelo reconstruir(ItemChecklistModeloId id, TipoChecklist tipo, String codigo, String descricao,
                                                  ResponsavelChecklist responsavel, boolean obrigatorio, Integer prazoDias, int ordem,
                                                  boolean activo) {
        var m = new ItemChecklistModelo();
        m.id = id;
        m.tipo = tipo;
        m.codigo = codigo;
        m.descricao = descricao;
        m.responsavel = responsavel;
        m.obrigatorio = obrigatorio;
        m.prazoDias = prazoDias;
        m.ordem = ordem;
        m.activo = activo;
        return m;
    }

    public void definir(String descricao, ResponsavelChecklist responsavel, boolean obrigatorio, Integer prazoDias, int ordem) {
        if (descricao == null || descricao.isBlank()) throw invalido("Descreva o que há a fazer.");
        if (responsavel == null) throw invalido("Indique quem trata do item.");
        if (prazoDias != null && Math.abs(prazoDias) > 365) throw invalido("O prazo do item fica dentro de um ano.");
        this.descricao = descricao.trim();
        this.responsavel = responsavel;
        this.obrigatorio = obrigatorio;
        this.prazoDias = prazoDias;
        this.ordem = ordem;
    }

    public void activar(boolean activo) {
        this.activo = activo;
    }

    static IgrpResponseStatusException invalido(String m) {
        return IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY, m);
    }
}
