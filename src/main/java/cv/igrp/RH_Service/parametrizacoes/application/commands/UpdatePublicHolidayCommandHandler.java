package cv.igrp.RH_Service.parametrizacoes.application.commands;

import cv.igrp.RH_Service.parametrizacoes.application.dto.PublicHolidayResponseDTO;
import cv.igrp.RH_Service.parametrizacoes.domain.models.PublicHoliday;
import cv.igrp.RH_Service.parametrizacoes.domain.repository.PublicHolidayRepository;
import cv.igrp.RH_Service.parametrizacoes.infrastructure.mappers.PublicHolidayMapper;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.parametrizacoes.domain.valueobject.PublicHolidayId;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UpdatePublicHolidayCommandHandler implements CommandHandler<UpdatePublicHolidayCommand, ResponseEntity<PublicHolidayResponseDTO>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpdatePublicHolidayCommandHandler.class);

    private final PublicHolidayRepository publicHolidayRepository;
    private final PublicHolidayMapper publicHolidayMapper;

    @IgrpCommandHandler
    public ResponseEntity<PublicHolidayResponseDTO> handle(UpdatePublicHolidayCommand command) {
        var id = PublicHolidayId.from(UUID.fromString(command.getPublicHolidayId()));
        var dto = command.getPublicHolidayRequest();

        PublicHoliday holiday = publicHolidayRepository.findById(id)
            .orElseThrow(() -> IgrpResponseStatusException.notFound(
                "Não encontrado: " + command.getPublicHolidayId()));

        boolean national = dto.getIsNational() != null && dto.getIsNational();

        holiday.atualizar(dto.getName(), dto.getHolidayDate(), national, dto.getDescription());
        PublicHoliday saved = publicHolidayRepository.save(holiday);

        return ResponseEntity.ok(publicHolidayMapper.toDTO(saved));
    }
}
