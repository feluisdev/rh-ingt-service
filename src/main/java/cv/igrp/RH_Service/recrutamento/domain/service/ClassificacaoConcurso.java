package cv.igrp.RH_Service.recrutamento.domain.service;

import cv.igrp.RH_Service.recrutamento.domain.models.Candidatura;
import cv.igrp.RH_Service.recrutamento.domain.models.Concurso;
import cv.igrp.RH_Service.recrutamento.domain.models.MetodoSelecao;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * <b>A lista de classificação</b> (BR-CNC-16..18): os aprovados ordenam-se pela classificação final; em empate, pela
 * nota do método de maior ponderação, e depois pela data da candidatura [ind.]. A <b>ordem de provimento</b> respeita a
 * quota de deficiência (Lei n.º 20/X/2023, art. 127.º n.º 4): dos Lugares a prover, os da quota vão primeiro aos
 * candidatos com deficiência aprovados, pela ordem deles; os restantes, pela ordem geral.
 */
public final class ClassificacaoConcurso {

    private ClassificacaoConcurso() {}

    /** Classifica as admitidas com todas as notas, ordena as aprovadas e dá-lhes a posição (1, 2, …). */
    public static List<Candidatura> classificar(Concurso concurso, List<Candidatura> candidaturas) {
        MetodoSelecao maisPesado = concurso.getMetodos().stream().max(Comparator.comparingInt(Concurso.Metodo::ponderacao))
                .map(Concurso.Metodo::metodo).orElse(null);
        candidaturas.forEach(c -> c.classificar(concurso.getMetodos()));
        List<Candidatura> aprovadas = new ArrayList<>(candidaturas.stream().filter(Candidatura::aprovada).toList());
        aprovadas.sort(Comparator.comparing(Candidatura::getClassificacaoFinal, Comparator.reverseOrder())
                .thenComparing(c -> maisPesado == null ? BigDecimal.ZERO : c.getNotas().getOrDefault(maisPesado, BigDecimal.ZERO),
                        Comparator.reverseOrder())
                .thenComparing(Candidatura::getDataApresentacao));
        for (int i = 0; i < aprovadas.size(); i++) aprovadas.get(i).posicionar(i + 1);
        return aprovadas;
    }

    /**
     * A ordem por que os aprovados ainda por prover (já ordenados) são chamados para os Lugares, com a quota à frente.
     * {@code jaProvidosComDeficiencia}: os da quota já providos, que a descontam.
     */
    public static List<Candidatura> ordemDeProvimento(Concurso concurso, List<Candidatura> aprovadasOrdenadas, long jaProvidosComDeficiencia) {
        long quota = Math.max(0, (concurso.getQuotaDeficiencia() != null ? concurso.getQuotaDeficiencia() : 0) - jaProvidosComDeficiencia);
        List<Candidatura> ordem = new ArrayList<>();
        aprovadasOrdenadas.stream().filter(Candidatura::isDeficiencia).limit(quota).forEach(ordem::add);
        aprovadasOrdenadas.stream().filter(c -> !ordem.contains(c)).forEach(ordem::add);
        return ordem;
    }
}
