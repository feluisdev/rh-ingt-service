package cv.igrp.RH_Service.colaboradores.infrastructure.persistence.adapters;

import cv.igrp.RH_Service.colaboradores.domain.models.Checklist;
import cv.igrp.RH_Service.colaboradores.domain.models.ItemChecklistModelo;
import cv.igrp.RH_Service.colaboradores.domain.models.ResponsavelChecklist;
import cv.igrp.RH_Service.colaboradores.domain.models.TipoChecklist;
import cv.igrp.RH_Service.colaboradores.domain.repository.ChecklistRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.ChecklistId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.ItemChecklistId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.ItemChecklistModeloId;
import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.ChecklistEntity;
import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.ChecklistItemEntity;
import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.FuncionarioEntity;
import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.ItemChecklistModeloEntity;
import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.repository.ColabsChecklistEntityRepository;
import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.repository.ColabsItemChecklistModeloEntityRepository;
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
public class ChecklistRepositoryImpl implements ChecklistRepository {

    private final ColabsChecklistEntityRepository entityRepository;
    private final ColabsItemChecklistModeloEntityRepository modeloRepository;
    private final JpaReferences refs;

    @Transactional
    @Override
    public Checklist save(Checklist c) {
        ChecklistEntity e = entityRepository.findById(c.getId().getValor()).orElseGet(() -> {
            var n = new ChecklistEntity();
            n.setId(c.getId().getValor());
            n.setFuncionario(refs.ref(FuncionarioEntity.class, c.getFuncionarioId().getValor()));
            n.setTipo(c.getTipo().name());
            n.setAbertaEm(c.getAbertaEm());
            return n;
        });
        e.setDataReferencia(c.getDataReferencia());
        e.setEstado(c.getEstado().name());
        e.setConcluidaEm(c.getConcluidaEm());
        e.setMotivoCancelamento(c.getMotivoCancelamento());
        // Os itens guardam a identidade (marcam-se pelo id): actualizam-se no lugar, os novos acrescentam-se.
        Map<UUID, ChecklistItemEntity> existentes = e.getItens().stream()
                .collect(Collectors.toMap(ChecklistItemEntity::getId, Function.identity()));
        for (var i : c.getItens()) {
            var x = existentes.get(i.getId().getValor());
            if (x == null) {
                x = new ChecklistItemEntity();
                x.setId(i.getId().getValor());
                x.setChecklist(e);
                x.setModeloItemId(i.getModeloId() != null ? i.getModeloId().getValor() : null);
                x.setCodigo(i.getCodigo());
                e.getItens().add(x);
            }
            x.setDescricao(i.getDescricao());
            x.setResponsavel(i.getResponsavel().name());
            x.setObrigatorio(i.isObrigatorio());
            x.setPrazo(i.getPrazo());
            x.setOrdem(i.getOrdem());
            x.setEstado(i.getEstado().name());
            x.setData(i.getData());
            x.setObservacao(i.getObservacao());
            x.setAutomatico(i.isAutomatico());
        }
        return toDomain(entityRepository.save(e));
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<Checklist> findById(ChecklistId id) {
        return entityRepository.findById(id.getValor()).map(this::toDomain);
    }

    @Transactional(readOnly = true)
    @Override
    public List<Checklist> findByFuncionario(FuncionarioId funcionarioId) {
        return entityRepository.findDoFuncionario(funcionarioId.getValor()).stream().map(this::toDomain).toList();
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<Checklist> findCorrente(FuncionarioId funcionarioId, TipoChecklist tipo) {
        return entityRepository.findCorrentes(funcionarioId.getValor(), tipo.name()).stream().findFirst().map(this::toDomain);
    }

    @Transactional(readOnly = true)
    @Override
    public List<Checklist> findComPendentes(TipoChecklist tipo, ResponsavelChecklist responsavel, LocalDate atrasadasEm) {
        String t = tipo != null ? tipo.name() : null;
        String r = responsavel != null ? responsavel.name() : null;
        var lista = atrasadasEm == null ? entityRepository.findComPendentes(t, r) : entityRepository.findComAtrasados(t, r, atrasadasEm);
        return lista.stream().map(this::toDomain).toList();
    }

    @Transactional(readOnly = true)
    @Override
    public List<Checklist> findComPrazoEm(LocalDate prazo) {
        return entityRepository.findComPrazoEm(prazo).stream().map(this::toDomain).toList();
    }

    @Transactional
    @Override
    public ItemChecklistModelo save(ItemChecklistModelo m) {
        var e = modeloRepository.findById(m.getId().getValor()).orElseGet(() -> {
            var n = new ItemChecklistModeloEntity();
            n.setId(m.getId().getValor());
            n.setTipo(m.getTipo().name());
            n.setCodigo(m.getCodigo());
            return n;
        });
        e.setDescricao(m.getDescricao());
        e.setResponsavel(m.getResponsavel().name());
        e.setObrigatorio(m.isObrigatorio());
        e.setPrazoDias(m.getPrazoDias());
        e.setOrdem(m.getOrdem());
        e.setActivo(m.isActivo());
        return toDomain(modeloRepository.save(e));
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<ItemChecklistModelo> findModelo(ItemChecklistModeloId id) {
        return modeloRepository.findById(id.getValor()).map(this::toDomain);
    }

    @Transactional(readOnly = true)
    @Override
    public List<ItemChecklistModelo> findModelos(TipoChecklist tipo) {
        return modeloRepository.find(tipo != null ? tipo.name() : null).stream().map(this::toDomain).toList();
    }

    @Transactional(readOnly = true)
    @Override
    public boolean existeCodigo(TipoChecklist tipo, String codigo, ItemChecklistModeloId excepto) {
        return modeloRepository.existeCodigo(tipo.name(), codigo, excepto != null ? excepto.getValor() : null);
    }

    @Transactional(readOnly = true)
    @Override
    public long contarModelos() {
        return modeloRepository.count();
    }

    private Checklist toDomain(ChecklistEntity e) {
        var itens = e.getItens().stream().map(x -> Checklist.Item.reconstruir(ItemChecklistId.from(x.getId()),
                x.getModeloItemId() != null ? ItemChecklistModeloId.from(x.getModeloItemId()) : null, x.getCodigo(), x.getDescricao(),
                ResponsavelChecklist.valueOf(x.getResponsavel()), Boolean.TRUE.equals(x.getObrigatorio()), x.getPrazo(),
                x.getOrdem() != null ? x.getOrdem() : 0, Checklist.EstadoItem.valueOf(x.getEstado()), x.getData(), x.getObservacao(),
                Boolean.TRUE.equals(x.getAutomatico()))).toList();
        return Checklist.reconstruir(ChecklistId.from(e.getId()),
                FuncionarioId.from(refs.idOf(e.getFuncionario(), FuncionarioEntity::getId)), TipoChecklist.valueOf(e.getTipo()),
                e.getDataReferencia(), Checklist.Estado.valueOf(e.getEstado()), e.getAbertaEm(), e.getConcluidaEm(),
                e.getMotivoCancelamento(), itens);
    }

    private ItemChecklistModelo toDomain(ItemChecklistModeloEntity e) {
        return ItemChecklistModelo.reconstruir(ItemChecklistModeloId.from(e.getId()), TipoChecklist.valueOf(e.getTipo()), e.getCodigo(),
                e.getDescricao(), ResponsavelChecklist.valueOf(e.getResponsavel()), Boolean.TRUE.equals(e.getObrigatorio()),
                e.getPrazoDias(), e.getOrdem() != null ? e.getOrdem() : 0, Boolean.TRUE.equals(e.getActivo()));
    }
}
