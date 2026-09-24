package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.application.dto.AfectacaoRequestDTO;
import cv.igrp.RH_Service.colaboradores.application.services.ColocacaoService;
import cv.igrp.RH_Service.colaboradores.domain.models.TipoAfectacao;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.application.dto.SuccessResponseDTO;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AfectarColaboradorCommandHandler
        implements CommandHandler<AfectarColaboradorCommand, ResponseEntity<SuccessResponseDTO>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AfectarColaboradorCommandHandler.class);

    private final ColocacaoService colocacaoService;

    @IgrpCommandHandler
    @Transactional
    public ResponseEntity<SuccessResponseDTO> handle(AfectarColaboradorCommand command) {
        AfectacaoRequestDTO dto = command.getRequest();

        if (dto.getFuncionarioId() == null || dto.getFuncionarioId().isBlank())
            throw IgrpResponseStatusException.badRequest("O funcionário é obrigatório.");
        if (dto.getPositionId() == null || dto.getPositionId().isBlank())
            throw IgrpResponseStatusException.badRequest("O Lugar (positionId) é obrigatório.");

        LocalDate dataInicio = dto.getDataInicio() != null ? dto.getDataInicio() : LocalDate.now();
        // Colocacao de quem nao tem Lugar (BR-AF-15 a BR-AF-22): a origem e do sistema, e quem
        // ja tem Lugar muda-o por um movimento, nao por aqui.
        ColocacaoService.Resultado r = colocacaoService.colocar(
                FuncionarioId.from(dto.getFuncionarioId()),
                UUID.fromString(dto.getPositionId()),
                parse(dto.getGradeId()),
                parse(dto.getFunctionId()),
                dto.getOrigem(),
                TipoAfectacao.de(dto.getAssignmentType()),
                dataInicio,
                dto.getNotes());

        return ResponseEntity.status(201).body(
                SuccessResponseDTO.de(r.afectacao().getId().getStringValor(), r.alertas().toArray(String[]::new)));
    }

    private static UUID parse(String v) {
        return (v == null || v.isBlank()) ? null : UUID.fromString(v);
    }
}
