package cv.igrp.RH_Service.colaboradores.infrastructure.persistence.adapters;

import cv.igrp.RH_Service.colaboradores.domain.models.PedidoDeclaracao;
import cv.igrp.RH_Service.colaboradores.domain.repository.PedidoDeclaracaoRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.DocumentoEmitidoId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.PedidoDeclaracaoId;
import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.DocumentoEmitidoEntity;
import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.FuncionarioEntity;
import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.PedidoDeclaracaoEntity;
import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.repository.ColabsPedidoDeclaracaoEntityRepository;
import cv.igrp.RH_Service.shared.infrastructure.persistence.JpaReferences;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PedidoDeclaracaoRepositoryImpl implements PedidoDeclaracaoRepository {

    private final ColabsPedidoDeclaracaoEntityRepository entityRepository;
    private final JpaReferences refs;

    @Transactional
    @Override
    public PedidoDeclaracao save(PedidoDeclaracao p) {
        PedidoDeclaracaoEntity e = entityRepository.findById(p.getId().getValor()).orElseGet(() -> {
            var n = new PedidoDeclaracaoEntity();
            n.setId(p.getId().getValor());
            n.setFuncionario(refs.ref(FuncionarioEntity.class, p.getFuncionarioId().getValor()));
            n.setTipo(p.getTipo().name());
            n.setFinalidade(p.getFinalidade());
            n.setPedidoPeloProprio(p.isPedidoPeloProprio());
            n.setDataPedido(p.getDataPedido());
            return n;
        });
        e.setEstado(p.getEstado().name());
        e.setDocumento(p.getDocumentoId() != null ? refs.ref(DocumentoEmitidoEntity.class, p.getDocumentoId().getValor()) : null);
        e.setMotivoRecusa(p.getMotivoRecusa());
        return toDomain(entityRepository.save(e));
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<PedidoDeclaracao> findById(PedidoDeclaracaoId id) {
        return entityRepository.findById(id.getValor()).map(this::toDomain);
    }

    @Transactional(readOnly = true)
    @Override
    public List<PedidoDeclaracao> findByFuncionario(FuncionarioId funcionarioId) {
        return entityRepository.findDoFuncionario(funcionarioId.getValor()).stream().map(this::toDomain).toList();
    }

    @Transactional(readOnly = true)
    @Override
    public List<PedidoDeclaracao> findPorEmitir() {
        return entityRepository.findPorEmitir().stream().map(this::toDomain).toList();
    }

    private PedidoDeclaracao toDomain(PedidoDeclaracaoEntity e) {
        var doc = refs.idOf(e.getDocumento(), DocumentoEmitidoEntity::getId);
        return PedidoDeclaracao.reconstruir(PedidoDeclaracaoId.from(e.getId()),
                FuncionarioId.from(refs.idOf(e.getFuncionario(), FuncionarioEntity::getId)),
                PedidoDeclaracao.Tipo.valueOf(e.getTipo()), e.getFinalidade(), PedidoDeclaracao.Estado.valueOf(e.getEstado()),
                Boolean.TRUE.equals(e.getPedidoPeloProprio()), e.getDataPedido(),
                doc != null ? DocumentoEmitidoId.from(doc) : null, e.getMotivoRecusa());
    }
}
