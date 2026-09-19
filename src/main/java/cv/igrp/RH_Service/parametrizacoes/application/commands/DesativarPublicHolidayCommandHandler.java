package cv.igrp.RH_Service.parametrizacoes.application.commands;

import cv.igrp.RH_Service.parametrizacoes.domain.models.PublicHoliday;
import cv.igrp.RH_Service.parametrizacoes.domain.repository.PublicHolidayRepository;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.parametrizacoes.domain.valueobject.PublicHolidayId;
import cv.igrp.RH_Service.shared.application.dto.SuccessResponseDTO;
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
public class DesativarPublicHolidayCommandHandler implements CommandHandler<DesativarPublicHolidayCommand, ResponseEntity<SuccessResponseDTO>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DesativarPublicHolidayCommandHandler.class);

    private final PublicHolidayRepository publicHolidayRepository;

    @IgrpCommandHandler
    public ResponseEntity<SuccessResponseDTO> handle(DesativarPublicHolidayCommand command) {
        var id = PublicHolidayId.from(UUID.fromString(command.getPublicHolidayId()));

        PublicHoliday holiday = publicHolidayRepository.findById(id)
            .orElseThrow(() -> IgrpResponseStatusException.notFound(
                "Não encontrado: " + command.getPublicHolidayId()));

        holiday.desativar();
        publicHolidayRepository.save(holiday);

        return ResponseEntity.ok(SuccessResponseDTO.de(command.getPublicHolidayId()));
    }
}
