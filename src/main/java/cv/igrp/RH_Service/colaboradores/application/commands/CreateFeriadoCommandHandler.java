package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.domain.models.Feriado;
import cv.igrp.RH_Service.colaboradores.domain.repository.FeriadoRepository;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component("colabsCreateFeriadoCommandHandler")
@RequiredArgsConstructor
public class CreateFeriadoCommandHandler
        implements CommandHandler<CreateFeriadoCommand, ResponseEntity<Map<String, ?>>> {

    private final FeriadoRepository feriadoRepository;

    @IgrpCommandHandler
    public ResponseEntity<Map<String, ?>> handle(CreateFeriadoCommand command) {
        var dto = command.getRequest();
        if (Boolean.TRUE.equals(dto.getIsNational()) && feriadoRepository.existsNacionalActivoByData(dto.getData()))
            throw IgrpResponseStatusException.conflict("Já existe um feriado nacional activo na data: " + dto.getData());

        var saved = feriadoRepository.save(Feriado.criar(dto.getNome(), dto.getData(), dto.getIsNational(), dto.getMunicipioCkey()));

        return ResponseEntity.status(201).body(Map.of(
                "id", saved.getId().getStringValor(),
                "message", "Criado com sucesso"));
    }
}
