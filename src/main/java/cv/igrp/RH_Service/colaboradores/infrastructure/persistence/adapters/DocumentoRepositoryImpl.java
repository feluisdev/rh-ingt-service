package cv.igrp.RH_Service.colaboradores.infrastructure.persistence.adapters;

import cv.igrp.RH_Service.colaboradores.domain.filter.DocumentoFilter;
import cv.igrp.RH_Service.colaboradores.domain.models.Documento;
import cv.igrp.RH_Service.colaboradores.domain.repository.DocumentoRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.DocumentoId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.infrastructure.mappers.DocumentoMapper;
import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.repository.ColabsDocumentoEntityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Repository("colabsDocumentoRepositoryImpl")
@RequiredArgsConstructor
public class DocumentoRepositoryImpl implements DocumentoRepository {

    private final ColabsDocumentoEntityRepository entityRepository;
    private final DocumentoMapper mapper;

    @Transactional
    @Override
    public Documento save(Documento documento) {
        return mapper.toDomain(entityRepository.save(mapper.toEntity(documento)));
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<Documento> findById(DocumentoId id) {
        return entityRepository.findById(id.getValor()).map(mapper::toDomain);
    }

    @Transactional(readOnly = true)
    @Override
    public List<Documento> findAllByFuncionarioId(FuncionarioId funcionarioId, DocumentoFilter filter) {
        // Por defeito (active=null) retorna apenas activos — FR-007 clarificado
        Boolean activeFilter = filter.getActive() != null ? filter.getActive() : Boolean.TRUE;

        Stream<Documento> stream = entityRepository
                .findAllByReferenceEntityAndReferenceIdAndIsActive("FUNCIONARIO", funcionarioId.getValor(), activeFilter)
                .stream().map(mapper::toDomain);

        if (filter.getDocumentTypeId() != null)
            stream = stream.filter(d -> filter.getDocumentTypeId().equals(d.getDocumentTypeId().getValor()));

        return stream.toList();
    }
}
