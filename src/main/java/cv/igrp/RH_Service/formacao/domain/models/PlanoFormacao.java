package cv.igrp.RH_Service.formacao.domain.models;

import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.formacao.domain.valueobject.AccaoFormacaoId;
import cv.igrp.RH_Service.formacao.domain.valueobject.NecessidadeFormacaoId;
import cv.igrp.RH_Service.formacao.domain.valueobject.PlanoFormacaoId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * <b>O plano anual de formação</b> (BR-FRM-01..02): as necessidades identificadas (pela chefia, pelo próprio ou pelo RH),
 * com prioridade; aprovado por despacho. As acções respondem às necessidades; uma acção concluída satisfá-las.
 */
@Getter
public class PlanoFormacao {

    public enum Estado { RASCUNHO, APROVADO }
    public enum Prioridade { ALTA, MEDIA, BAIXA }
    public enum EstadoNecessidade { IDENTIFICADA, PLANEADA, SATISFEITA }

    public record Necessidade(NecessidadeFormacaoId id, String tema, FuncionarioId funcionarioId, AccaoFormacao.Origem origem,
                              Prioridade prioridade, String justificacao, EstadoNecessidade estado, AccaoFormacaoId accaoId) {}

    private PlanoFormacaoId id;
    private int ano;
    private UUID unidadeId;
    private String designacao;
    private Estado estado;
    private String despacho;
    private LocalDate dataAprovacao;
    private List<Necessidade> necessidades = new ArrayList<>();

    private PlanoFormacao() {}

    public static PlanoFormacao criar(int ano, UUID unidadeId, String designacao, int anoCorrente) {
        if (ano < anoCorrente - 1 || ano > anoCorrente + 2) throw invalido("O plano é deste ano ou dos próximos dois.");
        var p = new PlanoFormacao();
        p.id = PlanoFormacaoId.gerarNovo();
        p.ano = ano;
        p.unidadeId = unidadeId;
        p.designacao = designacao != null && !designacao.isBlank() ? designacao.trim() : "Plano de formação " + ano;
        p.estado = Estado.RASCUNHO;
        return p;
    }

    public static PlanoFormacao reconstruir(PlanoFormacaoId id, int ano, UUID unidadeId, String designacao, Estado estado, String despacho,
                                            LocalDate dataAprovacao, List<Necessidade> necessidades) {
        var p = new PlanoFormacao();
        p.id = id;
        p.ano = ano;
        p.unidadeId = unidadeId;
        p.designacao = designacao;
        p.estado = estado;
        p.despacho = despacho;
        p.dataAprovacao = dataAprovacao;
        p.necessidades = new ArrayList<>(necessidades);
        return p;
    }

    public List<Necessidade> getNecessidades() {
        return Collections.unmodifiableList(necessidades);
    }

    /** Uma necessidade, enquanto o plano está em rascunho. */
    public Necessidade identificar(String tema, FuncionarioId funcionarioId, AccaoFormacao.Origem origem, Prioridade prioridade, String justificacao) {
        if (estado != Estado.RASCUNHO) throw IgrpResponseStatusException.conflict("O plano já foi aprovado: não recebe necessidades novas.");
        if (tema == null || tema.isBlank()) throw invalido("Indique o tema da formação necessária.");
        var n = new Necessidade(NecessidadeFormacaoId.gerarNovo(), tema.trim(), funcionarioId, origem != null ? origem : AccaoFormacao.Origem.RH,
                prioridade != null ? prioridade : Prioridade.MEDIA, justificacao != null && !justificacao.isBlank() ? justificacao.trim() : null,
                EstadoNecessidade.IDENTIFICADA, null);
        necessidades.add(n);
        return n;
    }

    public void aprovar(String despacho, LocalDate data) {
        if (estado != Estado.RASCUNHO) throw IgrpResponseStatusException.conflict("O plano já foi aprovado.");
        if (despacho == null || despacho.isBlank()) throw invalido("Indique o despacho que aprova o plano.");
        if (necessidades.isEmpty()) throw invalido("O plano não tem necessidades identificadas.");
        this.despacho = despacho.trim();
        this.dataAprovacao = data;
        this.estado = Estado.APROVADO;
    }

    /** Uma acção responde a estas necessidades (ficam PLANEADAS). */
    public void planear(List<NecessidadeFormacaoId> ids, AccaoFormacaoId accaoId) {
        for (var id : ids) substituir(id, EstadoNecessidade.PLANEADA, accaoId);
    }

    /** A acção concluiu: as necessidades a que respondia ficam SATISFEITAS. */
    public void satisfazer(AccaoFormacaoId accaoId) {
        for (var n : List.copyOf(necessidades))
            if (accaoId.equals(n.accaoId())) substituir(n.id(), EstadoNecessidade.SATISFEITA, accaoId);
    }

    private void substituir(NecessidadeFormacaoId id, EstadoNecessidade estado, AccaoFormacaoId accaoId) {
        var n = necessidades.stream().filter(x -> x.id().equals(id)).findFirst()
                .orElseThrow(() -> invalido("Uma das necessidades não é deste plano."));
        necessidades.set(necessidades.indexOf(n), new Necessidade(n.id(), n.tema(), n.funcionarioId(), n.origem(), n.prioridade(),
                n.justificacao(), estado, accaoId));
    }

    static IgrpResponseStatusException invalido(String m) {
        return IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY, m);
    }
}
