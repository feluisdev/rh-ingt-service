package cv.igrp.RH_Service.colaboradores.infrastructure.persistence.adapters;

import cv.igrp.RH_Service.colaboradores.domain.models.CartaoProfissional;
import cv.igrp.RH_Service.colaboradores.domain.repository.CartaoProfissionalRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.CartaoProfissionalId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.DocumentoEmitidoId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.CartaoProfissionalEntity;
import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.DocumentoEmitidoEntity;
import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.FuncionarioEntity;
import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.repository.ColabsCartaoProfissionalEntityRepository;
import cv.igrp.RH_Service.shared.infrastructure.persistence.JpaReferences;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CartaoProfissionalRepositoryImpl implements CartaoProfissionalRepository {

    private final ColabsCartaoProfissionalEntityRepository entityRepository;
    private final JpaReferences refs;

    @Transactional
    @Override
    public CartaoProfissional save(CartaoProfissional c) {
        CartaoProfissionalEntity e = entityRepository.findById(c.getId().getValor()).orElseGet(() -> {
            var n = new CartaoProfissionalEntity();
            n.setId(c.getId().getValor());
            n.setFuncionario(refs.ref(FuncionarioEntity.class, c.getFuncionarioId().getValor()));
            n.setDocumento(refs.ref(DocumentoEmitidoEntity.class, c.getDocumentoId().getValor()));
            n.setNumero(c.getNumero());
            n.setEmitidoEm(c.getEmitidoEm());
            n.setCategoriaId(c.getCategoriaId());
            n.setCategoria(c.getCategoria());
            n.setFuncaoId(c.getFuncaoId());
            n.setFuncao(c.getFuncao());
            n.setCargo(c.getCargo());
            return n;
        });
        e.setEstado(c.getEstado().name());
        e.setDataEntrega(c.getDataEntrega());
        e.setDataDevolucao(c.getDataDevolucao());
        e.setMotivoAnulacao(c.getMotivoAnulacao());
        return toDomain(entityRepository.save(e));
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<CartaoProfissional> findById(CartaoProfissionalId id) {
        return entityRepository.findById(id.getValor()).map(this::toDomain);
    }

    @Transactional(readOnly = true)
    @Override
    public List<CartaoProfissional> findByFuncionario(FuncionarioId funcionarioId) {
        return entityRepository.findDoFuncionario(funcionarioId.getValor()).stream().map(this::toDomain).toList();
    }

    private CartaoProfissional toDomain(CartaoProfissionalEntity e) {
        return CartaoProfissional.reconstruir(CartaoProfissionalId.from(e.getId()),
                FuncionarioId.from(refs.idOf(e.getFuncionario(), FuncionarioEntity::getId)),
                DocumentoEmitidoId.from(refs.idOf(e.getDocumento(), DocumentoEmitidoEntity::getId)), e.getNumero(), e.getEmitidoEm(),
                e.getCategoriaId(), e.getCategoria(), e.getFuncaoId(), e.getFuncao(), e.getCargo(),
                CartaoProfissional.Estado.valueOf(e.getEstado()), e.getDataEntrega(), e.getDataDevolucao(), e.getMotivoAnulacao());
    }
}
