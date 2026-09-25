package cv.igrp.RH_Service.colaboradores.domain.models;

import cv.igrp.RH_Service.colaboradores.domain.valueobject.FactoRhId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * <b>Um facto do RH para o processamento salarial</b> (BR-FAC-01..05): o que aconteceu, a quem, com que
 * data de efeito, e em que mês de processamento entra. É <b>imutável</b> — o diário só cresce: uma
 * correcção é um facto novo, nunca a reescrita de um que a outra aplicação já pode ter lido.
 *
 * <p>O {@link #mesCompetencia} é o mês da data de efeito, excepto quando esse mês já foi fechado: então é
 * o primeiro mês aberto a seguir (ajuste no mês seguinte, como fazem os sistemas de processamento).
 * {@link #dados} leva o contexto que o salarial precisa (Lugar, escalão, estado…), por ids e códigos.
 */
@Getter
public class FactoRh {

    private FactoRhId id;
    private FuncionarioId funcionarioId;
    private TipoFactoRh tipo;
    private LocalDate dataEfeito;
    private YearMonth mesCompetencia;
    private String referenciaTipo;
    private String referenciaId;
    private String descricao;
    private Map<String, String> dados;
    private LocalDateTime registadoEm;

    private FactoRh() {}

    public static FactoRh registar(FuncionarioId funcionarioId, TipoFactoRh tipo, LocalDate dataEfeito,
                                   YearMonth mesCompetencia, String referenciaTipo, String referenciaId,
                                   String descricao, Map<String, ?> dados, LocalDateTime agora) {
        var f = new FactoRh();
        f.id = FactoRhId.gerarNovo();
        f.funcionarioId = Objects.requireNonNull(funcionarioId);
        f.tipo = Objects.requireNonNull(tipo);
        f.dataEfeito = Objects.requireNonNull(dataEfeito);
        f.mesCompetencia = mesCompetencia != null ? mesCompetencia : YearMonth.from(dataEfeito);
        f.referenciaTipo = referenciaTipo;
        f.referenciaId = referenciaId;
        f.descricao = descricao;
        f.dados = limpar(dados);
        f.registadoEm = Objects.requireNonNull(agora);
        return f;
    }

    public static FactoRh reconstruir(FactoRhId id, FuncionarioId funcionarioId, TipoFactoRh tipo, LocalDate dataEfeito,
                                      YearMonth mesCompetencia, String referenciaTipo, String referenciaId,
                                      String descricao, Map<String, String> dados, LocalDateTime registadoEm) {
        var f = new FactoRh();
        f.id = id;
        f.funcionarioId = funcionarioId;
        f.tipo = tipo;
        f.dataEfeito = dataEfeito;
        f.mesCompetencia = mesCompetencia;
        f.referenciaTipo = referenciaTipo;
        f.referenciaId = referenciaId;
        f.descricao = descricao;
        f.dados = dados != null ? Collections.unmodifiableMap(new LinkedHashMap<>(dados)) : Map.of();
        f.registadoEm = registadoEm;
        return f;
    }

    /** Entra depois do mês a que respeita (o mês da data de efeito estava fechado). */
    public boolean isAjusteDeMesAnterior() {
        return mesCompetencia.isAfter(YearMonth.from(dataEfeito));
    }

    /** Sem nulos, tudo em texto, pela ordem dada. */
    private static Map<String, String> limpar(Map<String, ?> dados) {
        if (dados == null) return Map.of();
        var m = new LinkedHashMap<String, String>();
        dados.forEach((k, v) -> { if (k != null && v != null) m.put(k, v.toString()); });
        return Collections.unmodifiableMap(m);
    }
}
