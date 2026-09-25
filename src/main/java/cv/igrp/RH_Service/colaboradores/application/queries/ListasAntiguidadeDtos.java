package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.RH_Service.colaboradores.application.dto.LinhaListaAntiguidadeDTO;
import cv.igrp.RH_Service.colaboradores.application.dto.ListaAntiguidadeOficialDTO;
import cv.igrp.RH_Service.colaboradores.application.dto.ReclamacaoAntiguidadeDTO;
import cv.igrp.RH_Service.colaboradores.domain.models.ListaAntiguidadeOficial;
import cv.igrp.RH_Service.colaboradores.domain.models.ReclamacaoAntiguidade;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/** Das listas de antiguidade oficiais e reclamações para os DTOs. */
public final class ListasAntiguidadeDtos {

    private ListasAntiguidadeDtos() {}

    /**
     * {@code soDe} não nulo: só a linha e as reclamações dessa pessoa ({@code /me}). {@code comLinhas} falso: o
     * cabeçalho (a listagem).
     */
    public static ListaAntiguidadeOficialDTO dto(ListaAntiguidadeOficial l, List<ReclamacaoAntiguidade> reclamacoes,
                                                 boolean comLinhas, FuncionarioId soDe, Function<FuncionarioId, String> nomes,
                                                 List<String> alertas) {
        var linhas = !comLinhas ? new ArrayList<LinhaListaAntiguidadeDTO>() : new ArrayList<>(l.getLinhas().stream()
                .filter(x -> soDe == null || x.funcionarioId().equals(soDe)).map(ListasAntiguidadeDtos::dto).toList());
        var recs = reclamacoes == null ? new ArrayList<ReclamacaoAntiguidadeDTO>() : new ArrayList<>(reclamacoes.stream()
                .filter(r -> soDe == null || r.getFuncionarioId().equals(soDe)).map(r -> dto(r, nomes, List.of())).toList());
        return new ListaAntiguidadeOficialDTO(l.getId().getStringValor(), l.getAno(), l.getReferencia(), l.getUnidadeId().toString(),
                l.isIncluirSubunidades(), l.getEstado().name(), l.getAprovadaPor(), l.getDataAprovacao(), l.getDataAfixacao(),
                l.getLocalAfixacao(), l.fimPrazoReclamacao(false), l.fimPrazoReclamacao(true), l.getDataDefinitiva(),
                l.getPublicacaoSerie(), l.getPublicacaoNumero(), l.getPublicacaoData(), l.foraDoPrazoDePublicacao(),
                l.getMotivoAnulacao(), l.getVersao(), l.getLinhas().size(), linhas, recs,
                alertas != null ? new ArrayList<>(alertas) : new ArrayList<>());
    }

    static LinhaListaAntiguidadeDTO dto(ListaAntiguidadeOficial.Linha x) {
        return new LinhaListaAntiguidadeDTO(x.grupo(), x.carreira(), x.categoria(), x.foraDeGrelha(), x.posicao(),
                x.funcionarioId().getStringValor(), x.numeroFuncionario(), x.nome(),
                x.unidadeId() != null ? x.unidadeId().toString() : null, x.escalao(), x.inicioNoCargo(), x.diasDescontados(),
                x.diasNoCargo(), x.anosNoCargo(), x.mesesNoCargo(), x.diasNoCargoResto(), x.diasTotais(), x.anosTotal(),
                x.mesesTotal(), x.diasTotalResto());
    }

    public static ReclamacaoAntiguidadeDTO dto(ReclamacaoAntiguidade r, Function<FuncionarioId, String> nomes, List<String> alertas) {
        return new ReclamacaoAntiguidadeDTO(r.getId().getStringValor(), r.getListaId().getStringValor(),
                r.getFuncionarioId().getStringValor(), nomes != null ? nomes.apply(r.getFuncionarioId()) : null,
                r.getFundamento().name(), r.getTexto(), r.isNoEstrangeiro(), r.isPeloProprio(), r.getDataApresentacao(),
                r.getEstado().name(), r.getDecisao(), r.getDataDecisao(), r.getRecursoEm(), r.getRecursoTexto(),
                r.getRecursoResultado() != null ? r.getRecursoResultado().name() : null, r.getRecursoDecisao(),
                r.getRecursoDecididoEm(), new ArrayList<>(alertas));
    }
}
