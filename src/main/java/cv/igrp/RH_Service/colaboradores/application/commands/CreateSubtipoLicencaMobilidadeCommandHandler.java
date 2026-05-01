package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.domain.models.SubtipoLicencaMobilidade;
import cv.igrp.RH_Service.colaboradores.domain.repository.SubtipoLicencaMobilidadeRepository;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component("colabsCreateSubtipoLicencaMobilidadeCommandHandler")
@RequiredArgsConstructor
public class CreateSubtipoLicencaMobilidadeCommandHandler
        implements CommandHandler<CreateSubtipoLicencaMobilidadeCommand, ResponseEntity<Map<String, ?>>> {

    private static final List<String> VALID_RECORD_TYPES = List.of("LICENCA", "MOBILIDADE", "AMBOS");
    private final SubtipoLicencaMobilidadeRepository subtipoRepository;

    @IgrpCommandHandler
    public ResponseEntity<Map<String, ?>> handle(CreateSubtipoLicencaMobilidadeCommand command) {
        var dto = command.getRequest();
        if (!VALID_RECORD_TYPES.contains(dto.getRecordType()))
            throw IgrpResponseStatusException.badRequest("recordType inválido. Valores aceites: LICENCA, MOBILIDADE, AMBOS.");
        if (subtipoRepository.existsByCodigo(dto.getCodigo()))
            throw IgrpResponseStatusException.conflict("Já existe um subtipo com o código: " + dto.getCodigo());

        var saved = subtipoRepository.save(SubtipoLicencaMobilidade.criar(
                dto.getNome(), dto.getCodigo(), dto.getRecordType(),
                dto.getAffectsPay(), dto.getCountsForSeniority(), dto.getCanSelfSubmit()));

        return ResponseEntity.status(201).body(Map.of(
                "id", saved.getId().getStringValor(),
                "message", "Criado com sucesso"));
    }
}
