package cv.igrp.RH_Service.parametrizacoes.application.commands;

import cv.igrp.RH_Service.parametrizacoes.domain.models.PublicHoliday;
import cv.igrp.RH_Service.parametrizacoes.domain.repository.PublicHolidayRepository;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.application.dto.SuccessResponseDTO;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class CreatePublicHolidayCommandHandler implements CommandHandler<CreatePublicHolidayCommand, ResponseEntity<SuccessResponseDTO>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CreatePublicHolidayCommandHandler.class);

    private final PublicHolidayRepository publicHolidayRepository;

    @IgrpCommandHandler
    public ResponseEntity<SuccessResponseDTO> handle(CreatePublicHolidayCommand command) {
        var dto = command.getPublicHolidayRequest();

        var holidayDate = dto.getHolidayDate();
        boolean national = Boolean.TRUE.equals(dto.getIsNational());
        boolean recurring = Boolean.TRUE.equals(dto.getIsRecurring());

        var holiday = PublicHoliday.criar(dto.getName(), holidayDate, national, dto.getDescription(),
                recurring, dto.getAreaCkey());

        if (national && publicHolidayRepository.existeNacionalActivoNoDia(holidayDate, recurring)) {
            throw IgrpResponseStatusException.conflict(
                "Já existe um feriado nacional activo na data '" + dto.getHolidayDate() + "'.");
        }

        PublicHoliday saved = publicHolidayRepository.save(holiday);

        return ResponseEntity.status(201).body(SuccessResponseDTO.de(saved.getId().getStringValor()));
    }
}
