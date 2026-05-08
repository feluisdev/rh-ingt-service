package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.application.dto.DependenteResponseDTO;
import cv.igrp.RH_Service.colaboradores.domain.repository.DependenteRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.DependenteId;
import cv.igrp.RH_Service.colaboradores.infrastructure.mappers.DependenteMapper;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component("colabsUpdateDependenteCommandHandler")
@RequiredArgsConstructor
public class UpdateDependenteCommandHandler
        implements CommandHandler<UpdateDependenteCommand, ResponseEntity<DependenteResponseDTO>> {

    private final DependenteRepository dependenteRepository;
    private final DependenteMapper mapper;

    @IgrpCommandHandler
    public ResponseEntity<DependenteResponseDTO> handle(UpdateDependenteCommand command) {
        var dto = command.getRequest();
        var id = DependenteId.from(command.getDependenteId());

        var dependente = dependenteRepository.findById(id)
                .orElseThrow(() -> IgrpResponseStatusException.notFound(
                        "Dependente não encontrado: " + command.getDependenteId()));

        dependente.atualizar(
                dto.getFullName() != null ? dto.getFullName() : dependente.getFullName(),
                dto.getRelationshipType() != null ? dto.getRelationshipType() : dependente.getRelationshipType(),
                dto.getBirthDate() != null ? dto.getBirthDate() : dependente.getBirthDate(),
                dto.getNif() != null ? dto.getNif() : dependente.getNif()
        );

        return ResponseEntity.ok(mapper.toDTO(dependenteRepository.save(dependente)));
    }
}
