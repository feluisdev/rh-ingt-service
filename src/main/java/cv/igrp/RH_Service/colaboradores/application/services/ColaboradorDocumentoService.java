package cv.igrp.RH_Service.colaboradores.application.services;

import cv.igrp.RH_Service.colaboradores.application.dto.UploadDocumentoRequestDTO;
import cv.igrp.RH_Service.colaboradores.domain.models.Documento;
import cv.igrp.RH_Service.colaboradores.domain.repository.DocumentoRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.parametrizacoes.domain.repository.DocumentTypeRepository;
import cv.igrp.RH_Service.parametrizacoes.domain.valueobject.DocumentTypeId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
@RequiredArgsConstructor
public class ColaboradorDocumentoService {

    private final DocumentTypeRepository documentTypeRepository;
    private final DocumentoRepository documentoRepository;

    public Documento registarDocumento(FuncionarioId funcionarioId, UploadDocumentoRequestDTO dto) {
        var tipoId = DocumentTypeId.from(dto.getDocumentTypeId());
        var tipo = documentTypeRepository.findById(tipoId)
                .orElseThrow(() -> IgrpResponseStatusException.notFound(
                        "Tipo de documento não encontrado: " + dto.getDocumentTypeId()));

        if (!tipo.isActive())
            throw IgrpResponseStatusException.badRequest(
                    "Tipo de documento inactivo: " + tipo.getCodigo());

        var extension = FilenameUtils.getExtension(dto.getOriginalFilename()).toLowerCase();
        var allowed = Arrays.stream(tipo.getAllowedExtensions().split(","))
                .map(String::trim).map(String::toLowerCase).toList();
        if (!allowed.contains(extension))
            throw IgrpResponseStatusException.badRequest(
                    "Extensão não permitida. Aceites: " + tipo.getAllowedExtensions());

        return documentoRepository.save(Documento.criar(
                "FUNCIONARIO", funcionarioId.getValor(), tipoId,
                dto.getFileKey(),
                dto.getOriginalFilename(),
                dto.getContentType(),
                dto.getFileSize(),
                dto.getDescription()));
    }
}
