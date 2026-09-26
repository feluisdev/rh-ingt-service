package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.RH_Service.colaboradores.application.dto.SugestaoJuntaMedicaDTO;
import cv.igrp.RH_Service.colaboradores.application.services.SaudeTrabalhoService;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class GetSugestoesJuntaMedicaQueryHandler
        implements QueryHandler<GetSugestoesJuntaMedicaQuery, ResponseEntity<List<SugestaoJuntaMedicaDTO>>> {

    private final SaudeTrabalhoService service;

    @IgrpQueryHandler
    public ResponseEntity<List<SugestaoJuntaMedicaDTO>> handle(GetSugestoesJuntaMedicaQuery q) {
        LocalDate dia;
        try {
            dia = q.getData() == null || q.getData().isBlank() ? LocalDate.now() : LocalDate.parse(q.getData().trim());
        } catch (DateTimeParseException e) {
            throw IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY, "Indique a data no formato ano-mês-dia, por exemplo 2026-09-26.");
        }
        return ResponseEntity.ok(service.sugestoesJunta(dia).stream()
                .map(s -> new SugestaoJuntaMedicaDTO(s.funcionarioId().getStringValor(), s.nome(), s.desde(), s.ate(), s.dias()))
                .toList());
    }
}
