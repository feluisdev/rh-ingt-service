package cv.igrp.RH_Service.colaboradores.infrastructure.persistence.adapters;

import cv.igrp.RH_Service.colaboradores.domain.models.ListaAntiguidadeOficial;
import cv.igrp.RH_Service.colaboradores.domain.models.ReclamacaoAntiguidade;
import cv.igrp.RH_Service.colaboradores.domain.repository.ListaAntiguidadeOficialRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.ListaAntiguidadeOficialId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.ReclamacaoAntiguidadeId;
import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.FuncionarioEntity;
import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.ListaAntiguidadeEntity;
import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.ListaAntiguidadeLinhaEntity;
import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.ReclamacaoAntiguidadeEntity;
import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.repository.ColabsListaAntiguidadeEntityRepository;
import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.repository.ColabsReclamacaoAntiguidadeEntityRepository;
import cv.igrp.RH_Service.shared.infrastructure.persistence.JpaReferences;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class ListaAntiguidadeOficialRepositoryImpl implements ListaAntiguidadeOficialRepository {

    private final ColabsListaAntiguidadeEntityRepository listas;
    private final ColabsReclamacaoAntiguidadeEntityRepository reclamacoes;
    private final JpaReferences refs;

    @Transactional
    @Override
    public ListaAntiguidadeOficial save(ListaAntiguidadeOficial l) {
        ListaAntiguidadeEntity e = listas.findById(l.getId().getValor()).orElseGet(() -> {
            var n = new ListaAntiguidadeEntity();
            n.setId(l.getId().getValor());
            n.setAno(l.getAno());
            n.setReferencia(l.getReferencia());
            n.setUnidadeId(l.getUnidadeId());
            n.setIncluirSubunidades(l.isIncluirSubunidades());
            n.setAprovadaPor(l.getAprovadaPor());
            n.setDataAprovacao(l.getDataAprovacao());
            return n;
        });
        e.setEstado(l.getEstado().name());
        e.setDataAfixacao(l.getDataAfixacao());
        e.setLocalAfixacao(l.getLocalAfixacao());
        e.setDataDefinitiva(l.getDataDefinitiva());
        e.setPublicacaoSerie(l.getPublicacaoSerie());
        e.setPublicacaoNumero(l.getPublicacaoNumero());
        e.setPublicacaoData(l.getPublicacaoData());
        e.setMotivoAnulacao(l.getMotivoAnulacao());
        // As linhas só mudam quando a versão muda (recalcular); reescrevem-se inteiras.
        if (e.getVersao() == null || e.getVersao() != l.getVersao()) {
            e.getLinhas().clear();
            for (var x : l.getLinhas()) e.getLinhas().add(linha(e, x));
            e.setVersao(l.getVersao());
        }
        return toDomain(listas.save(e));
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<ListaAntiguidadeOficial> findById(ListaAntiguidadeOficialId id) {
        return listas.findById(id.getValor()).map(this::toDomain);
    }

    @Transactional(readOnly = true)
    @Override
    public List<ListaAntiguidadeOficial> find(Integer ano, UUID unidadeId) {
        return listas.find(ano, unidadeId).stream().map(this::toDomain).toList();
    }

    @Transactional(readOnly = true)
    @Override
    public boolean existeActiva(int ano, UUID unidadeId) {
        return listas.existeActiva(ano, unidadeId);
    }

    @Transactional(readOnly = true)
    @Override
    public List<ListaAntiguidadeOficial> findVisiveisPara(FuncionarioId funcionarioId) {
        return listas.findVisiveisPara(funcionarioId.getValor()).stream().map(this::toDomain).toList();
    }

    @Transactional
    @Override
    public ReclamacaoAntiguidade save(ReclamacaoAntiguidade r) {
        ReclamacaoAntiguidadeEntity e = reclamacoes.findById(r.getId().getValor()).orElseGet(() -> {
            var n = new ReclamacaoAntiguidadeEntity();
            n.setId(r.getId().getValor());
            n.setLista(refs.ref(ListaAntiguidadeEntity.class, r.getListaId().getValor()));
            n.setFuncionario(refs.ref(FuncionarioEntity.class, r.getFuncionarioId().getValor()));
            n.setFundamento(r.getFundamento().name());
            n.setTexto(r.getTexto());
            n.setNoEstrangeiro(r.isNoEstrangeiro());
            n.setPeloProprio(r.isPeloProprio());
            n.setDataApresentacao(r.getDataApresentacao());
            return n;
        });
        e.setEstado(r.getEstado().name());
        e.setDecisao(r.getDecisao());
        e.setDataDecisao(r.getDataDecisao());
        e.setRecursoEm(r.getRecursoEm());
        e.setRecursoTexto(r.getRecursoTexto());
        e.setRecursoResultado(r.getRecursoResultado() != null ? r.getRecursoResultado().name() : null);
        e.setRecursoDecisao(r.getRecursoDecisao());
        e.setRecursoDecididoEm(r.getRecursoDecididoEm());
        return toDomain(reclamacoes.save(e));
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<ReclamacaoAntiguidade> findReclamacao(ReclamacaoAntiguidadeId id) {
        return reclamacoes.findById(id.getValor()).map(this::toDomain);
    }

    @Transactional(readOnly = true)
    @Override
    public List<ReclamacaoAntiguidade> findReclamacoes(ListaAntiguidadeOficialId listaId) {
        return reclamacoes.findDaLista(listaId.getValor()).stream().map(this::toDomain).toList();
    }

    private static ListaAntiguidadeLinhaEntity linha(ListaAntiguidadeEntity lista, ListaAntiguidadeOficial.Linha x) {
        var e = new ListaAntiguidadeLinhaEntity();
        e.setId(UUID.randomUUID());
        e.setLista(lista);
        e.setGrupo(x.grupo());
        e.setCarreira(x.carreira());
        e.setCategoria(x.categoria());
        e.setForaDeGrelha(x.foraDeGrelha());
        e.setPosicao(x.posicao());
        e.setFuncionarioId(x.funcionarioId().getValor());
        e.setNumeroFuncionario(x.numeroFuncionario());
        e.setNome(x.nome());
        e.setUnidadeId(x.unidadeId());
        e.setEscalao(x.escalao());
        e.setInicioNoCargo(x.inicioNoCargo());
        e.setDiasDescontados(x.diasDescontados());
        e.setDiasNoCargo(x.diasNoCargo());
        e.setAnosNoCargo(x.anosNoCargo());
        e.setMesesNoCargo(x.mesesNoCargo());
        e.setDiasNoCargoResto(x.diasNoCargoResto());
        e.setDiasTotais(x.diasTotais());
        e.setAnosTotal(x.anosTotal());
        e.setMesesTotal(x.mesesTotal());
        e.setDiasTotalResto(x.diasTotalResto());
        return e;
    }

    private ListaAntiguidadeOficial toDomain(ListaAntiguidadeEntity e) {
        var linhas = e.getLinhas().stream().map(x -> new ListaAntiguidadeOficial.Linha(x.getGrupo(), x.getCarreira(),
                x.getCategoria(), Boolean.TRUE.equals(x.getForaDeGrelha()), x.getPosicao(), FuncionarioId.from(x.getFuncionarioId()),
                x.getNumeroFuncionario(), x.getNome(), x.getUnidadeId(), x.getEscalao(), x.getInicioNoCargo(),
                x.getDiasDescontados(), x.getDiasNoCargo(), x.getAnosNoCargo(), x.getMesesNoCargo(), x.getDiasNoCargoResto(),
                x.getDiasTotais(), x.getAnosTotal(), x.getMesesTotal(), x.getDiasTotalResto())).toList();
        return ListaAntiguidadeOficial.reconstruir(ListaAntiguidadeOficialId.from(e.getId()), e.getAno(), e.getReferencia(),
                e.getUnidadeId(), Boolean.TRUE.equals(e.getIncluirSubunidades()), ListaAntiguidadeOficial.Estado.valueOf(e.getEstado()),
                e.getAprovadaPor(), e.getDataAprovacao(), e.getDataAfixacao(), e.getLocalAfixacao(), e.getDataDefinitiva(),
                e.getPublicacaoSerie(), e.getPublicacaoNumero(), e.getPublicacaoData(), e.getMotivoAnulacao(),
                e.getVersao() != null ? e.getVersao() : 1, linhas);
    }

    private ReclamacaoAntiguidade toDomain(ReclamacaoAntiguidadeEntity e) {
        return ReclamacaoAntiguidade.reconstruir(ReclamacaoAntiguidadeId.from(e.getId()),
                ListaAntiguidadeOficialId.from(refs.idOf(e.getLista(), ListaAntiguidadeEntity::getId)),
                FuncionarioId.from(refs.idOf(e.getFuncionario(), FuncionarioEntity::getId)),
                ReclamacaoAntiguidade.Fundamento.valueOf(e.getFundamento()), e.getTexto(),
                Boolean.TRUE.equals(e.getNoEstrangeiro()), Boolean.TRUE.equals(e.getPeloProprio()), e.getDataApresentacao(),
                ReclamacaoAntiguidade.Estado.valueOf(e.getEstado()), e.getDecisao(), e.getDataDecisao(), e.getRecursoEm(),
                e.getRecursoTexto(), e.getRecursoResultado() != null ? ReclamacaoAntiguidade.ResultadoRecurso.valueOf(e.getRecursoResultado()) : null,
                e.getRecursoDecisao(), e.getRecursoDecididoEm());
    }
}
