package cv.igrp.RH_Service.colaboradores.application.services;

import cv.igrp.RH_Service.colaboradores.domain.models.ListaAntiguidadeOficial;
import cv.igrp.RH_Service.colaboradores.domain.models.ReclamacaoAntiguidade;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.ListaAntiguidadeOficialRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.ListaAntiguidadeOficialId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.ReclamacaoAntiguidadeId;
import cv.igrp.RH_Service.shared.application.services.notificacoes.Notificador;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.domain.notificacoes.TipoNotificacao;
import cv.igrp.RH_Service.shared.domain.service.Datas;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * <b>O ciclo da lista de antiguidade</b> (DL n.º 3/2010, arts. 69.º–74.º; BR-LAN-06..15): aprovar (congelar a
 * lista gerada), afixar (avisa cada pessoa da lista do prazo para reclamar), reclamações e recursos, lista
 * definitiva e publicação no Boletim Oficial.
 */
@Service
@RequiredArgsConstructor
public class CicloListaAntiguidadeService {

    static final String RECURSO = "LISTA_ANTIGUIDADE";

    /** Uma operação que correu, com o que o utilizador deve saber (ex.: publicada depois de 30 de Abril). */
    public record Resultado<T>(T valor, List<String> alertas) {}

    private final ListaAntiguidadeOficialRepository repository;
    private final ListaAntiguidadeService listaAntiguidadeService;
    private final FuncionarioRepository funcionarioRepository;
    private final Notificador notificador;

    @Transactional
    public ListaAntiguidadeOficial aprovar(Integer ano, UUID unidadeId, boolean incluirSubunidades, String aprovadaPor,
                                           LocalDate dataAprovacao) {
        int a = ano != null ? ano : hoje().getYear();
        if (repository.existeActiva(a, unidadeId))
            throw IgrpResponseStatusException.conflict("Já há uma lista de antiguidade de " + a
                    + " para este serviço. Anule-a antes de aprovar outra.");
        return repository.save(ListaAntiguidadeOficial.aprovar(a, unidadeId, incluirSubunidades, aprovadaPor,
                dataAprovacao != null ? dataAprovacao : hoje(), linhas(a, unidadeId, incluirSubunidades)));
    }

    @Transactional
    public ListaAntiguidadeOficial afixar(ListaAntiguidadeOficialId id, LocalDate data, String local) {
        var l = lista(id);
        l.afixar(data != null ? data : hoje(), local);
        var gravada = repository.save(l);
        for (var linha : l.getLinhas())
            notificador.para(linha.funcionarioId()).tipo(TipoNotificacao.LISTA_ANTIGUIDADE_AFIXADA)
                    .titulo("Foi afixada a lista de antiguidade de " + l.getAno())
                    .texto("Afixada em " + l.getLocalAfixacao() + ". Pode reclamar até " + Datas.pt(l.fimPrazoReclamacao(false))
                            + " (até " + Datas.pt(l.fimPrazoReclamacao(true)) + " se presta serviço no estrangeiro).")
                    .recurso(RECURSO, l.getId().getStringValor()).enviar();
        return gravada;
    }

    @Transactional
    public ListaAntiguidadeOficial recalcular(ListaAntiguidadeOficialId id) {
        var l = lista(id);
        l.recalcular(linhas(l.getAno(), l.getUnidadeId(), l.isIncluirSubunidades()));
        return repository.save(l);
    }

    @Transactional
    public ListaAntiguidadeOficial tornarDefinitiva(ListaAntiguidadeOficialId id) {
        var l = lista(id);
        boolean porDecidir = repository.findReclamacoes(id).stream().anyMatch(ReclamacaoAntiguidade::porDecidir);
        l.tornarDefinitiva(hoje(), porDecidir);
        return repository.save(l);
    }

    @Transactional
    public Resultado<ListaAntiguidadeOficial> publicar(ListaAntiguidadeOficialId id, String serie, String numero, LocalDate data) {
        var l = lista(id);
        List<String> alertas = new ArrayList<>();
        if (l.publicar(serie, numero, data))
            alertas.add("A lista foi publicada depois de 30 de Abril, o prazo do art. 71.º n.º 2.");
        return new Resultado<>(repository.save(l), alertas);
    }

    @Transactional
    public ListaAntiguidadeOficial anular(ListaAntiguidadeOficialId id, String motivo) {
        var l = lista(id);
        l.anular(motivo);
        return repository.save(l);
    }

    @Transactional
    public ReclamacaoAntiguidade reclamar(ListaAntiguidadeOficialId listaId, FuncionarioId funcionarioId,
                                          ReclamacaoAntiguidade.Fundamento fundamento, String texto, boolean noEstrangeiro,
                                          boolean peloProprio) {
        var l = lista(listaId);
        var f = funcionarioRepository.findById(funcionarioId)
                .orElseThrow(() -> IgrpResponseStatusException.notFound("Colaborador não encontrado."));
        var r = repository.save(ReclamacaoAntiguidade.apresentar(l, funcionarioId, fundamento, texto, noEstrangeiro, peloProprio, hoje()));
        notificador.paraRh().tipo(TipoNotificacao.RECLAMACAO_ANTIGUIDADE)
                .titulo(f.getNomeCompleto() + " reclamou da lista de antiguidade de " + l.getAno())
                .texto("A decisão deve ser notificada até " + Datas.pt(hoje().plusDays(ReclamacaoAntiguidade.PRAZO_DECISAO)) + ".")
                .recurso(RECURSO, l.getId().getStringValor()).enviar();
        return r;
    }

    @Transactional
    public Resultado<ReclamacaoAntiguidade> decidir(ListaAntiguidadeOficialId listaId, ReclamacaoAntiguidadeId id,
                                                    boolean deferida, String decisao) {
        var r = reclamacao(listaId, id);
        List<String> alertas = new ArrayList<>();
        if (r.decidir(deferida, decisao, hoje()))
            alertas.add("A decisão saiu depois dos 30 dias do art. 72.º n.º 5.");
        if (deferida)
            alertas.add("Corrija os dados do colaborador e recalcule a lista antes de a tornar definitiva.");
        notificador.para(r.getFuncionarioId()).tipo(TipoNotificacao.RECLAMACAO_ANTIGUIDADE)
                .titulo("A sua reclamação da lista de antiguidade foi " + (deferida ? "deferida" : "indeferida"))
                .texto(r.getDecisao() + " Pode recorrer até " + Datas.pt(hoje().plusDays(r.isNoEstrangeiro()
                        ? ReclamacaoAntiguidade.PRAZO_RECURSO_ESTRANGEIRO : ReclamacaoAntiguidade.PRAZO_RECURSO)) + ".")
                .recurso(RECURSO, listaId.getStringValor()).enviar();
        return new Resultado<>(repository.save(r), alertas);
    }

    @Transactional
    public ReclamacaoAntiguidade recorrer(ListaAntiguidadeOficialId listaId, ReclamacaoAntiguidadeId id, String texto) {
        var r = reclamacao(listaId, id);
        r.recorrer(texto, hoje());
        return repository.save(r);
    }

    @Transactional
    public ReclamacaoAntiguidade decidirRecurso(ListaAntiguidadeOficialId listaId, ReclamacaoAntiguidadeId id,
                                                boolean provido, String decisao) {
        var r = reclamacao(listaId, id);
        r.decidirRecurso(provido, decisao, hoje());
        notificador.para(r.getFuncionarioId()).tipo(TipoNotificacao.RECLAMACAO_ANTIGUIDADE)
                .titulo("O seu recurso sobre a lista de antiguidade foi " + (provido ? "provido" : "não provido"))
                .texto(r.getRecursoDecisao()).recurso(RECURSO, listaId.getStringValor()).enviar();
        return repository.save(r);
    }

    @Transactional(readOnly = true)
    public List<ListaAntiguidadeOficial> listar(Integer ano, UUID unidadeId) {
        return repository.find(ano, unidadeId);
    }

    @Transactional(readOnly = true)
    public ListaAntiguidadeOficial lista(ListaAntiguidadeOficialId id) {
        return repository.findById(id).orElseThrow(() -> IgrpResponseStatusException.notFound("Lista de antiguidade não encontrada."));
    }

    @Transactional(readOnly = true)
    public List<ReclamacaoAntiguidade> reclamacoes(ListaAntiguidadeOficialId id) {
        return repository.findReclamacoes(id);
    }

    @Transactional(readOnly = true)
    public List<ListaAntiguidadeOficial> visiveisPara(FuncionarioId funcionarioId) {
        return repository.findVisiveisPara(funcionarioId);
    }

    /** A lista gerada hoje (BR-LAN-01..05), em linhas para congelar. */
    List<ListaAntiguidadeOficial.Linha> linhas(int ano, UUID unidadeId, boolean incluirSubunidades) {
        var lista = listaAntiguidadeService.lista(ano, unidadeId, incluirSubunidades);
        List<ListaAntiguidadeOficial.Linha> out = new ArrayList<>();
        int g = 0;
        for (var grupo : lista.grupos()) {
            g++;
            int p = 0;
            for (var x : grupo.linhas()) {
                p++;
                var c = x.noCargo();
                var t = x.total();
                out.add(new ListaAntiguidadeOficial.Linha(g, grupo.carreira(), grupo.categoria(), grupo.foraDeGrelha(), p,
                        x.funcionario().getId(), x.funcionario().getNumeroFuncionario(), x.funcionario().getNomeCompleto(),
                        x.unidadeId(), x.escalao(), x.inicioNoCargo(), c.diasDescontados(), c.diasContados(), c.anos(), c.meses(),
                        c.dias(), t != null ? t.diasContados() : 0, t != null ? t.anos() : 0, t != null ? t.meses() : 0,
                        t != null ? t.dias() : 0));
            }
        }
        return out;
    }

    private ReclamacaoAntiguidade reclamacao(ListaAntiguidadeOficialId listaId, ReclamacaoAntiguidadeId id) {
        return repository.findReclamacao(id).filter(r -> r.getListaId().equals(listaId))
                .orElseThrow(() -> IgrpResponseStatusException.notFound("Reclamação não encontrada."));
    }

    LocalDate hoje() { return LocalDate.now(); }
}
