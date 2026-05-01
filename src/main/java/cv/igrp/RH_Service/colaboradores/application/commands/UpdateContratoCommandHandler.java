package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.application.dto.ContratoResponse;
import cv.igrp.RH_Service.colaboradores.domain.repository.ContratoRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.ContratoId;
import cv.igrp.RH_Service.colaboradores.infrastructure.mappers.ContratoMapper;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component("colabsUpdateContratoCommandHandler")
@RequiredArgsConstructor
public class UpdateContratoCommandHandler
        implements CommandHandler<UpdateContratoCommand, ResponseEntity<ContratoResponse>> {

    private final ContratoRepository contratoRepository;
    private final ContratoMapper mapper;

    @IgrpCommandHandler
    public ResponseEntity<ContratoResponse> handle(UpdateContratoCommand command) {
        var dto = command.getRequest();
        var id = ContratoId.from(command.getContratoId());

        var contrato = contratoRepository.findById(id)
                .orElseThrow(() -> IgrpResponseStatusException.notFound("Contrato não encontrado: " + command.getContratoId()));

        if (dto.getNumeroContrato() != null && !dto.getNumeroContrato().isBlank()
                && contratoRepository.existsByNumeroContratoAndIdNot(dto.getNumeroContrato(), id))
            throw IgrpResponseStatusException.conflict("Já existe um contrato com número '" + dto.getNumeroContrato() + "'.");

        contrato.atualizar(
                dto.getTipoContrato() != null ? dto.getTipoContrato() : contrato.getTipoContrato(),
                dto.getDataInicio() != null ? dto.getDataInicio() : contrato.getDataInicio(),
                dto.getDataFim() != null ? dto.getDataFim() : contrato.getDataFim(),
                dto.getNumeroContrato() != null ? dto.getNumeroContrato() : contrato.getNumeroContrato()
        );

        return ResponseEntity.ok(mapper.toDTO(contratoRepository.save(contrato)));
    }
}
