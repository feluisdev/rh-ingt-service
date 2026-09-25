package cv.igrp.RH_Service.colaboradores.infrastructure.persistence.adapters;

import cv.igrp.RH_Service.colaboradores.domain.models.DocumentoEmitido;
import cv.igrp.RH_Service.colaboradores.domain.models.TipoDocumentoEmitido;
import cv.igrp.RH_Service.colaboradores.domain.repository.DocumentoEmitidoRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.DocumentoEmitidoId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.DocumentoEmitidoEntity;
import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.FuncionarioEntity;
import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.NumeracaoDocumentoEntity;
import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.repository.ColabsDocumentoEmitidoEntityRepository;
import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.repository.ColabsNumeracaoDocumentoEntityRepository;
import cv.igrp.RH_Service.shared.infrastructure.persistence.JpaReferences;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class DocumentoEmitidoRepositoryImpl implements DocumentoEmitidoRepository {

    private final ColabsDocumentoEmitidoEntityRepository entityRepository;
    private final ColabsNumeracaoDocumentoEntityRepository numeracaoRepository;
    private final JpaReferences refs;

    @Transactional
    @Override
    public DocumentoEmitido save(DocumentoEmitido d) {
        DocumentoEmitidoEntity e = entityRepository.findById(d.getId().getValor()).orElseGet(() -> {
            var n = new DocumentoEmitidoEntity();
            n.setId(d.getId().getValor());
            n.setTipo(d.getTipo().name());
            n.setNumero(d.getNumero());
            n.setFuncionario(d.getFuncionarioId() != null ? refs.ref(FuncionarioEntity.class, d.getFuncionarioId().getValor()) : null);
            n.setTitulo(d.getTitulo());
            n.setEmitidoEm(d.getEmitidoEm());
            n.setCodigoVerificacao(d.getCodigoVerificacao());
            n.setFicheiro(d.getFicheiro());
            n.setImpressaoDigital(d.getImpressaoDigital());
            n.setReferenciaTipo(d.getReferenciaTipo());
            n.setReferenciaId(d.getReferenciaId());
            return n;
        });
        // Depois de emitido, só a anulação muda.
        e.setAnuladoEm(d.getAnuladoEm());
        e.setMotivoAnulacao(d.getMotivoAnulacao());
        return toDomain(entityRepository.save(e));
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<DocumentoEmitido> findById(DocumentoEmitidoId id) {
        return entityRepository.findById(id.getValor()).map(this::toDomain);
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<DocumentoEmitido> findByCodigo(String codigo) {
        return entityRepository.findByCodigoVerificacao(codigo).map(this::toDomain);
    }

    @Transactional(readOnly = true)
    @Override
    public boolean existsByCodigo(String codigo) {
        return entityRepository.existsByCodigoVerificacao(codigo);
    }

    @Transactional(readOnly = true)
    @Override
    public List<DocumentoEmitido> findByFuncionario(FuncionarioId funcionarioId) {
        return entityRepository.findDoFuncionario(funcionarioId.getValor()).stream().map(this::toDomain).toList();
    }

    @Transactional
    @Override
    public int proximoNumero(String serie, int ano) {
        String id = serie + "-" + ano;
        NumeracaoDocumentoEntity n = numeracaoRepository.findParaActualizar(id).orElseGet(() -> {
            var nova = new NumeracaoDocumentoEntity();
            nova.setId(id);
            nova.setSerie(serie);
            nova.setAno(ano);
            nova.setUltimo(0);
            return numeracaoRepository.saveAndFlush(nova);
        });
        n.setUltimo(n.getUltimo() + 1);
        numeracaoRepository.save(n);
        return n.getUltimo();
    }

    private DocumentoEmitido toDomain(DocumentoEmitidoEntity e) {
        return DocumentoEmitido.reconstruir(DocumentoEmitidoId.from(e.getId()), TipoDocumentoEmitido.valueOf(e.getTipo()),
                e.getNumero(),
                e.getFuncionario() != null ? FuncionarioId.from(refs.idOf(e.getFuncionario(), FuncionarioEntity::getId)) : null,
                e.getTitulo(), e.getEmitidoEm(), e.getCodigoVerificacao(), e.getFicheiro(), e.getImpressaoDigital(),
                e.getReferenciaTipo(), e.getReferenciaId(), e.getAnuladoEm(), e.getMotivoAnulacao());
    }
}
