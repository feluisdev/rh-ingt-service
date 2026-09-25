package cv.igrp.RH_Service.colaboradores.infrastructure.persistence.adapters;

import cv.igrp.RH_Service.colaboradores.domain.models.PublicacaoOficial;
import cv.igrp.RH_Service.colaboradores.domain.repository.PublicacaoOficialRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.DocumentoEmitidoId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.PublicacaoOficialId;
import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.DocumentoEmitidoEntity;
import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.FuncionarioEntity;
import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.PublicacaoOficialEntity;
import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.repository.ColabsPublicacaoOficialEntityRepository;
import cv.igrp.RH_Service.shared.infrastructure.persistence.JpaReferences;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PublicacaoOficialRepositoryImpl implements PublicacaoOficialRepository {

    private final ColabsPublicacaoOficialEntityRepository entityRepository;
    private final JpaReferences refs;

    @Transactional
    @Override
    public PublicacaoOficial save(PublicacaoOficial p) {
        PublicacaoOficialEntity e = entityRepository.findById(p.getId().getValor()).orElseGet(() -> {
            var n = new PublicacaoOficialEntity();
            n.setId(p.getId().getValor());
            n.setTipoActo(p.getTipoActo().name());
            n.setMeio(p.getMeio().name());
            n.setFuncionario(p.getFuncionarioId() != null ? refs.ref(FuncionarioEntity.class, p.getFuncionarioId().getValor()) : null);
            n.setReferenciaTipo(p.getReferenciaTipo());
            n.setReferenciaId(p.getReferenciaId());
            n.setSumario(p.getSumario());
            n.setDataActo(p.getDataActo());
            return n;
        });
        e.setEstado(p.getEstado().name());
        e.setExtracto(p.getExtractoId() != null ? refs.ref(DocumentoEmitidoEntity.class, p.getExtractoId().getValor()) : null);
        e.setSerie(p.getSerie());
        e.setNumero(p.getNumero());
        e.setDataPublicacao(p.getDataPublicacao());
        e.setMotivoCancelamento(p.getMotivoCancelamento());
        return toDomain(entityRepository.save(e));
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<PublicacaoOficial> findById(PublicacaoOficialId id) {
        return entityRepository.findById(id.getValor()).map(this::toDomain);
    }

    @Transactional(readOnly = true)
    @Override
    public List<PublicacaoOficial> find(PublicacaoOficial.Estado estado) {
        return entityRepository.find(estado != null ? estado.name() : null).stream().map(this::toDomain).toList();
    }

    @Transactional(readOnly = true)
    @Override
    public List<PublicacaoOficial> findByFuncionario(FuncionarioId funcionarioId) {
        return entityRepository.findDoFuncionario(funcionarioId.getValor()).stream().map(this::toDomain).toList();
    }

    @Transactional(readOnly = true)
    @Override
    public boolean existeParaReferencia(String referenciaTipo, String referenciaId) {
        return referenciaTipo != null && referenciaId != null && entityRepository.existeParaReferencia(referenciaTipo, referenciaId);
    }

    private PublicacaoOficial toDomain(PublicacaoOficialEntity e) {
        var f = refs.idOf(e.getFuncionario(), FuncionarioEntity::getId);
        var x = refs.idOf(e.getExtracto(), DocumentoEmitidoEntity::getId);
        return PublicacaoOficial.reconstruir(PublicacaoOficialId.from(e.getId()), PublicacaoOficial.TipoActo.valueOf(e.getTipoActo()),
                PublicacaoOficial.Meio.valueOf(e.getMeio()), f != null ? FuncionarioId.from(f) : null, e.getReferenciaTipo(),
                e.getReferenciaId(), e.getSumario(), e.getDataActo(), PublicacaoOficial.Estado.valueOf(e.getEstado()),
                x != null ? DocumentoEmitidoId.from(x) : null, e.getSerie(), e.getNumero(), e.getDataPublicacao(),
                e.getMotivoCancelamento());
    }
}
