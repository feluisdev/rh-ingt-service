package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.domain.repository.FeriadoRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FeriadoId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component("colabsUpdateFeriadoCommandHandler")
@RequiredArgsConstructor
public class UpdateFeriadoCommandHandler
        implements CommandHandler<UpdateFeriadoCommand, ResponseEntity<Map<String, ?>>> {

    private final FeriadoRepository feriadoRepository;

    @IgrpCommandHandler
    public ResponseEntity<Map<String, ?>> handle(UpdateFeriadoCommand command) {
        var id = FeriadoId.from(command.getId());
        var feriado = feriadoRepository.findById(id)
                .orElseThrow(() -> IgrpResponseStatusException.notFound("Feriado não encontrado: " + command.getId()));

        var dto = command.getRequest();
        boolean dataOuNacionalMudou = !dto.getData().equals(feriado.getData()) || !dto.getIsNational().equals(feriado.getIsNational());
        if (Boolean.TRUE.equals(dto.getIsNational()) && dataOuNacionalMudou
                && feriadoRepository.existsNacionalActivoByData(dto.getData()))
            throw IgrpResponseStatusException.conflict("Já existe um feriado nacional activo na data: " + dto.getData());

        feriado.atualizar(dto.getNome(), dto.getData(), dto.getIsNational(), dto.getMunicipioCkey());
        feriadoRepository.save(feriado);

        return ResponseEntity.ok(Map.of("message", "Actualizado com sucesso"));
    }
}
