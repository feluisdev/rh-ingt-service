package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.domain.models.Formacao;
import cv.igrp.RH_Service.colaboradores.domain.repository.DocumentoRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.FormacaoRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.DocumentoId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component("colabsCriarFormacaoCommandHandler")
@RequiredArgsConstructor
public class CriarFormacaoCommandHandler
        implements CommandHandler<CriarFormacaoCommand, ResponseEntity<Map<String, ?>>> {

    private final FuncionarioRepository funcionarioRepository;
    private final FormacaoRepository formacaoRepository;
    private final DocumentoRepository documentoRepository;

    @IgrpCommandHandler
    public ResponseEntity<Map<String, ?>> handle(CriarFormacaoCommand command) {
        var dto = command.getRequest();
        var funcionarioId = FuncionarioId.from(command.getFuncionarioId());

        var funcionario = funcionarioRepository.findById(funcionarioId)
                .orElseThrow(() -> IgrpResponseStatusException.notFound(
                        "Funcionário não encontrado: " + command.getFuncionarioId()));

        if (!Boolean.TRUE.equals(funcionario.getIsActive()))
            throw IgrpResponseStatusException.badRequest(
                    "Não é possível registar formação para funcionário inactivo.");

        if (dto.getDocumentId() != null) {
            documentoRepository.findById(DocumentoId.from(dto.getDocumentId()))
                    .orElseThrow(() -> IgrpResponseStatusException.badRequest(
                            "Documento não encontrado: " + dto.getDocumentId()));
        }

        var saved = formacaoRepository.save(Formacao.criar(
                funcionarioId, dto.getName(), dto.getInstitution(),
                dto.getTypeOptionKey(), dto.getStartDate(), dto.getEndDate(),
                dto.getDurationHours(), dto.getDocumentId()));

        return ResponseEntity.status(201).body(Map.of("id", saved.getId().getStringValor()));
    }
}
