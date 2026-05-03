package cv.igrp.RH_Service.parametrizacoes.application.commands;

import cv.igrp.RH_Service.parametrizacoes.domain.models.PublicHoliday;
import cv.igrp.RH_Service.parametrizacoes.domain.repository.PublicHolidayRepository;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CreatePublicHolidayCommandHandler implements CommandHandler<CreatePublicHolidayCommand, ResponseEntity<Map<String, ?>>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CreatePublicHolidayCommandHandler.class);

    private final PublicHolidayRepository publicHolidayRepository;

    @IgrpCommandHandler
    public ResponseEntity<Map<String, ?>> handle(CreatePublicHolidayCommand command) {
        var dto = command.getPublicHolidayRequest();

        LocalDate holidayDate = LocalDate.parse(dto.getHolidayDate());
        boolean national = dto.getIsNational() != null && dto.getIsNational();

        if (national && publicHolidayRepository.existsByHolidayDateAndNational(holidayDate, true)) {
            throw IgrpResponseStatusException.conflict(
                "Já existe um feriado nacional activo na data '" + dto.getHolidayDate() + "'.");
        }

        PublicHoliday saved = publicHolidayRepository.save(
            PublicHoliday.criar(dto.getName(), holidayDate, national, dto.getDescription())
        );

        return ResponseEntity.status(201).body(Map.of(
            "id", saved.getId().getStringValor(),
            "message", "Criado com sucesso"
        ));
    }
}
