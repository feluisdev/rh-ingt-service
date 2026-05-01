package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.domain.repository.SubtipoLicencaMobilidadeRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.SubtipoLicencaMobilidadeId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component("colabsUpdateSubtipoLicencaMobilidadeCommandHandler")
@RequiredArgsConstructor
public class UpdateSubtipoLicencaMobilidadeCommandHandler
        implements CommandHandler<UpdateSubtipoLicencaMobilidadeCommand, ResponseEntity<Map<String, ?>>> {

    private static final List<String> VALID_RECORD_TYPES = List.of("LICENCA", "MOBILIDADE", "AMBOS");
    private final SubtipoLicencaMobilidadeRepository subtipoRepository;

    @IgrpCommandHandler
    public ResponseEntity<Map<String, ?>> handle(UpdateSubtipoLicencaMobilidadeCommand command) {
        var id = SubtipoLicencaMobilidadeId.from(command.getId());
        var subtipo = subtipoRepository.findById(id)
                .orElseThrow(() -> IgrpResponseStatusException.notFound("Subtipo não encontrado: " + command.getId()));

        var dto = command.getRequest();
        if (!VALID_RECORD_TYPES.contains(dto.getRecordType()))
            throw IgrpResponseStatusException.badRequest("recordType inválido. Valores aceites: LICENCA, MOBILIDADE, AMBOS.");
        if (subtipoRepository.existsByCodigoAndIdNot(dto.getCodigo(), id))
            throw IgrpResponseStatusException.conflict("Já existe um subtipo com o código: " + dto.getCodigo());

        subtipo.atualizar(dto.getNome(), dto.getCodigo(), dto.getRecordType(),
                dto.getAffectsPay(), dto.getCountsForSeniority(), dto.getCanSelfSubmit());
        subtipoRepository.save(subtipo);

        return ResponseEntity.ok(Map.of("message", "Actualizado com sucesso"));
    }
}
