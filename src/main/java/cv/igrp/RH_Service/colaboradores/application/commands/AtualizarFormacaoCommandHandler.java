package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.domain.repository.FormacaoRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FormacaoId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.application.dto.SuccessResponseDTO;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;


@Component("colabsAtualizarFormacaoCommandHandler")
@RequiredArgsConstructor
public class AtualizarFormacaoCommandHandler
        implements CommandHandler<AtualizarFormacaoCommand, ResponseEntity<SuccessResponseDTO>> {

    private final FuncionarioRepository funcionarioRepository;
    private final FormacaoRepository formacaoRepository;

    @IgrpCommandHandler
    public ResponseEntity<SuccessResponseDTO> handle(AtualizarFormacaoCommand command) {
        var dto = command.getRequest();
        var funcionarioId = FuncionarioId.from(command.getFuncionarioId());

        var funcionario = funcionarioRepository.findById(funcionarioId)
                .orElseThrow(() -> IgrpResponseStatusException.notFound(
                        "Funcionário não encontrado: " + command.getFuncionarioId()));

        if (!Boolean.TRUE.equals(funcionario.getIsActive()))
            throw IgrpResponseStatusException.badRequest(
                    "Não é possível actualizar formação de funcionário inactivo.");

        var formacao = formacaoRepository.findById(FormacaoId.from(command.getFormacaoId()))
                .orElseThrow(() -> IgrpResponseStatusException.notFound(
                        "Formação não encontrada: " + command.getFormacaoId()));

        if (!formacao.getFuncionarioId().equals(funcionarioId))
            throw IgrpResponseStatusException.notFound("Formação não encontrada: " + command.getFormacaoId());

        formacao.atualizar(dto.getName(), dto.getInstitution(), dto.getTrainingType(),
                dto.getStartDate(), dto.getEndDate(), dto.getDurationHours());

        formacaoRepository.save(formacao);
        return ResponseEntity.ok(SuccessResponseDTO.de(command.getFuncionarioId()));
    }
}
