package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.domain.models.TipoAusencia;
import cv.igrp.RH_Service.colaboradores.domain.repository.TipoAusenciaRepository;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component("colabsCreateTipoAusenciaCommandHandler")
@RequiredArgsConstructor
public class CreateTipoAusenciaCommandHandler
        implements CommandHandler<CreateTipoAusenciaCommand, ResponseEntity<Map<String, ?>>> {

    private final TipoAusenciaRepository tipoAusenciaRepository;

    @IgrpCommandHandler
    public ResponseEntity<Map<String, ?>> handle(CreateTipoAusenciaCommand command) {
        var dto = command.getRequest();
        if (tipoAusenciaRepository.existsByCodigo(dto.getCodigo()))
            throw IgrpResponseStatusException.conflict("Já existe um tipo de ausência com o código: " + dto.getCodigo());

        var saved = tipoAusenciaRepository.save(TipoAusencia.criar(
                dto.getNome(), dto.getCodigo(), dto.getDeductsBalance(),
                dto.getRequiresApproval(), dto.getMaxDaysPerYear(), dto.getCategoryOptionCkey()));

        return ResponseEntity.status(201).body(Map.of(
                "id", saved.getId().getStringValor(),
                "message", "Criado com sucesso"));
    }
}
