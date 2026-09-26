package cv.igrp.RH_Service.formacao.infrastructure.persistence.adapters;

import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.FuncionarioEntity;
import cv.igrp.RH_Service.formacao.domain.models.AccaoFormacao;
import cv.igrp.RH_Service.formacao.domain.models.PlanoFormacao;
import cv.igrp.RH_Service.formacao.domain.repository.FormacaoRepositorio;
import cv.igrp.RH_Service.formacao.domain.valueobject.AccaoFormacaoId;
import cv.igrp.RH_Service.formacao.domain.valueobject.InscricaoFormacaoId;
import cv.igrp.RH_Service.formacao.domain.valueobject.NecessidadeFormacaoId;
import cv.igrp.RH_Service.formacao.domain.valueobject.PlanoFormacaoId;
import cv.igrp.RH_Service.formacao.infrastructure.persistence.entity.AccaoFormacaoEntity;
import cv.igrp.RH_Service.formacao.infrastructure.persistence.entity.InscricaoFormacaoEntity;
import cv.igrp.RH_Service.formacao.infrastructure.persistence.entity.NecessidadeFormacaoEntity;
import cv.igrp.RH_Service.formacao.infrastructure.persistence.entity.PlanoFormacaoEntity;
import cv.igrp.RH_Service.formacao.infrastructure.persistence.repository.FormAccaoFormacaoEntityRepository;
import cv.igrp.RH_Service.formacao.infrastructure.persistence.repository.FormPlanoFormacaoEntityRepository;
import cv.igrp.RH_Service.shared.infrastructure.persistence.JpaReferences;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class FormacaoRepositorioImpl implements FormacaoRepositorio {

    private final FormAccaoFormacaoEntityRepository accaoRepository;
    private final FormPlanoFormacaoEntityRepository planoRepository;
    private final JpaReferences refs;

    @Transactional
    @Override
    public AccaoFormacao save(AccaoFormacao a) {
        AccaoFormacaoEntity e = accaoRepository.findById(a.getId().getValor()).orElseGet(() -> {
            var n = new AccaoFormacaoEntity();
            n.setId(a.getId().getValor());
            return n;
        });
        e.setPlanoId(a.getPlanoId() != null ? a.getPlanoId().getValor() : null);
        e.getNecessidades().clear();
        a.getNecessidades().forEach(x -> e.getNecessidades().add(x.getValor()));
        e.setTema(a.getTema());
        e.setEntidadeFormadora(a.getEntidadeFormadora());
        e.setModalidade(a.getModalidade().name());
        e.setInterna(a.isInterna());
        e.setInicio(a.getInicio());
        e.setFim(a.getFim());
        e.setHoras(a.getHoras());
        e.setHorario(a.getHorario());
        e.setLocal(a.getLocal());
        e.setVagas(a.getVagas());
        e.setCustoPrevisto(a.getCustoPrevisto());
        e.setCusteadaPelaAdministracao(a.isCusteadaPelaAdministracao());
        e.setMesesGarantia(a.getMesesGarantia());
        e.setEstado(a.getEstado().name());
        e.setMotivoCancelamento(a.getMotivoCancelamento());
        Map<UUID, InscricaoFormacaoEntity> existentes = e.getInscricoes().stream()
                .collect(Collectors.toMap(InscricaoFormacaoEntity::getId, Function.identity()));
        for (var i : a.getInscricoes()) {
            var x = existentes.get(i.getId().getValor());
            if (x == null) {
                x = new InscricaoFormacaoEntity();
                x.setId(i.getId().getValor());
                x.setAccao(e);
                x.setFuncionario(refs.ref(FuncionarioEntity.class, i.getFuncionarioId().getValor()));
                x.setOrigem(i.getOrigem().name());
                x.setData(i.getData());
                e.getInscricoes().add(x);
            }
            x.setEstado(i.getEstado().name());
            x.setMotivo(i.getMotivo());
            x.setDiasPresenca(i.getDiasPresenca());
            x.setGarantiaAte(i.getGarantiaAte());
        }
        return toDomain(accaoRepository.save(e));
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<AccaoFormacao> findById(AccaoFormacaoId id) {
        return accaoRepository.findById(id.getValor()).map(this::toDomain);
    }

    @Transactional(readOnly = true)
    @Override
    public List<AccaoFormacao> find(AccaoFormacao.Estado estado, Integer ano) {
        return accaoRepository.find(estado != null ? estado.name() : null, ano).stream().map(this::toDomain).toList();
    }

    @Transactional(readOnly = true)
    @Override
    public List<AccaoFormacao> findDoFuncionario(FuncionarioId funcionarioId) {
        return accaoRepository.findDoFuncionario(funcionarioId.getValor()).stream().map(this::toDomain).toList();
    }

    @Transactional(readOnly = true)
    @Override
    public List<AccaoFormacao> findComFormandoEntre(FuncionarioId funcionarioId, LocalDate de, LocalDate ate) {
        return accaoRepository.findComFormandoEntre(funcionarioId.getValor(), de, ate).stream().map(this::toDomain).toList();
    }

    @Transactional(readOnly = true)
    @Override
    public List<AccaoFormacao> findComGarantiaEm(FuncionarioId funcionarioId, LocalDate em) {
        return accaoRepository.findComGarantiaEm(funcionarioId.getValor(), em).stream().map(this::toDomain).toList();
    }

    @Transactional
    @Override
    public PlanoFormacao save(PlanoFormacao p) {
        PlanoFormacaoEntity e = planoRepository.findById(p.getId().getValor()).orElseGet(() -> {
            var n = new PlanoFormacaoEntity();
            n.setId(p.getId().getValor());
            return n;
        });
        e.setAno(p.getAno());
        e.setUnidadeId(p.getUnidadeId());
        e.setDesignacao(p.getDesignacao());
        e.setEstado(p.getEstado().name());
        e.setDespacho(p.getDespacho());
        e.setDataAprovacao(p.getDataAprovacao());
        Map<UUID, NecessidadeFormacaoEntity> existentes = e.getNecessidades().stream()
                .collect(Collectors.toMap(NecessidadeFormacaoEntity::getId, Function.identity()));
        int ordem = 0;
        for (var n : p.getNecessidades()) {
            var x = existentes.get(n.id().getValor());
            if (x == null) {
                x = new NecessidadeFormacaoEntity();
                x.setId(n.id().getValor());
                x.setPlano(e);
                e.getNecessidades().add(x);
            }
            x.setOrdem(ordem++);
            x.setTema(n.tema());
            x.setFuncionarioId(n.funcionarioId() != null ? n.funcionarioId().getValor() : null);
            x.setOrigem(n.origem().name());
            x.setPrioridade(n.prioridade().name());
            x.setJustificacao(n.justificacao());
            x.setEstado(n.estado().name());
            x.setAccaoId(n.accaoId() != null ? n.accaoId().getValor() : null);
        }
        return toDomain(planoRepository.save(e));
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<PlanoFormacao> findPlano(PlanoFormacaoId id) {
        return planoRepository.findById(id.getValor()).map(this::toDomain);
    }

    @Transactional(readOnly = true)
    @Override
    public List<PlanoFormacao> findPlanos(Integer ano) {
        return planoRepository.find(ano).stream().map(this::toDomain).toList();
    }

    private AccaoFormacao toDomain(AccaoFormacaoEntity e) {
        var inscricoes = e.getInscricoes().stream().map(x -> AccaoFormacao.Inscricao.reconstruir(InscricaoFormacaoId.from(x.getId()),
                FuncionarioId.from(refs.idOf(x.getFuncionario(), FuncionarioEntity::getId)), AccaoFormacao.Origem.valueOf(x.getOrigem()),
                AccaoFormacao.EstadoInscricao.valueOf(x.getEstado()), x.getData(), x.getMotivo(), x.getDiasPresenca(), x.getGarantiaAte())).toList();
        return AccaoFormacao.reconstruir(AccaoFormacaoId.from(e.getId()), e.getPlanoId() != null ? PlanoFormacaoId.from(e.getPlanoId()) : null,
                e.getNecessidades().stream().map(NecessidadeFormacaoId::from).toList(), e.getTema(), e.getEntidadeFormadora(),
                AccaoFormacao.Modalidade.valueOf(e.getModalidade()), Boolean.TRUE.equals(e.getInterna()), e.getInicio(), e.getFim(),
                e.getHoras(), e.getHorario(), e.getLocal(), e.getVagas(), e.getCustoPrevisto(), Boolean.TRUE.equals(e.getCusteadaPelaAdministracao()),
                e.getMesesGarantia(), AccaoFormacao.Estado.valueOf(e.getEstado()), e.getMotivoCancelamento(), inscricoes);
    }

    private PlanoFormacao toDomain(PlanoFormacaoEntity e) {
        var necessidades = e.getNecessidades().stream().map(x -> new PlanoFormacao.Necessidade(NecessidadeFormacaoId.from(x.getId()), x.getTema(),
                x.getFuncionarioId() != null ? FuncionarioId.from(x.getFuncionarioId()) : null, AccaoFormacao.Origem.valueOf(x.getOrigem()),
                PlanoFormacao.Prioridade.valueOf(x.getPrioridade()), x.getJustificacao(), PlanoFormacao.EstadoNecessidade.valueOf(x.getEstado()),
                x.getAccaoId() != null ? AccaoFormacaoId.from(x.getAccaoId()) : null)).toList();
        return PlanoFormacao.reconstruir(PlanoFormacaoId.from(e.getId()), e.getAno(), e.getUnidadeId(), e.getDesignacao(),
                PlanoFormacao.Estado.valueOf(e.getEstado()), e.getDespacho(), e.getDataAprovacao(), necessidades);
    }
}
