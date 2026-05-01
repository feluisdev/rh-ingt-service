package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.domain.repository.TipoAusenciaRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.TipoAusenciaId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component("colabsUpdateTipoAusenciaCommandHandler")
@RequiredArgsConstructor
public class UpdateTipoAusenciaCommandHandler
        implements CommandHandler<UpdateTipoAusenciaCommand, ResponseEntity<Map<String, ?>>> {

    private final TipoAusenciaRepository tipoAusenciaRepository;

    @IgrpCommandHandler
    public ResponseEntity<Map<String, ?>> handle(UpdateTipoAusenciaCommand command) {
        var id = TipoAusenciaId.from(command.getId());
        var tipo = tipoAusenciaRepository.findById(id)
                .orElseThrow(() -> IgrpResponseStatusException.notFound("Tipo de ausência não encontrado: " + command.getId()));

        var dto = command.getRequest();
        if (tipoAusenciaRepository.existsByCodigoAndIdNot(dto.getCodigo(), id))
            throw IgrpResponseStatusException.conflict("Já existe um tipo de ausência com o código: " + dto.getCodigo());

        tipo.atualizar(dto.getNome(), dto.getCodigo(), dto.getDeductsBalance(),
                dto.getRequiresApproval(), dto.getMaxDaysPerYear(), dto.getCategoryOptionCkey());
        tipoAusenciaRepository.save(tipo);

        return ResponseEntity.ok(Map.of("message", "Actualizado com sucesso"));
    }
}
