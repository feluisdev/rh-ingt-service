package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.application.dto.ColocacaoResponseDTO;
import cv.igrp.RH_Service.colaboradores.domain.repository.ColocacaoRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.ColocacaoId;
import cv.igrp.RH_Service.colaboradores.infrastructure.mappers.ColocacaoMapper;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component("colabsAtualizarColocacaoCommandHandler")
@RequiredArgsConstructor
public class AtualizarColocacaoCommandHandler
        implements CommandHandler<AtualizarColocacaoCommand, ResponseEntity<ColocacaoResponseDTO>> {

    private final ColocacaoRepository colocacaoRepository;
    private final ColocacaoMapper mapper;

    @IgrpCommandHandler
    public ResponseEntity<ColocacaoResponseDTO> handle(AtualizarColocacaoCommand command) {
        var dto = command.getRequest();
        var colocacao = colocacaoRepository.findById(ColocacaoId.from(command.getColocacaoId()))
                .orElseThrow(() -> IgrpResponseStatusException.notFound(
                        "Colocação não encontrada: " + command.getColocacaoId()));

        if (dto.getEndDate() != null && colocacao.getStartDate() != null
                && dto.getEndDate().isBefore(colocacao.getStartDate()))
            throw IgrpResponseStatusException.badRequest(
                    "A data de fim não pode ser anterior à data de início.");

        colocacao.atualizar(dto.getEndDate(), dto.getNotes());
        var saved = colocacaoRepository.save(colocacao);
        return ResponseEntity.ok(mapper.toDTO(saved));
    }
}
