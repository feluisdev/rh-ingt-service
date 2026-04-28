package cv.igrp.RH_Service.sigdi.application.queries;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.sigdi.application.dto.InstitutionResponseDTO;
import cv.igrp.RH_Service.sigdi.domain.admin.models.Institution;
import cv.igrp.RH_Service.sigdi.domain.admin.repository.InstitutionRepository;
import cv.igrp.RH_Service.sigdi.domain.admin.valueobject.InstitutionId;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class GetInstitutionByIdQueryHandler
    implements QueryHandler<GetInstitutionByIdQuery, ResponseEntity<InstitutionResponseDTO>> {

  private static final Logger LOGGER = LoggerFactory.getLogger(GetInstitutionByIdQueryHandler.class);

  private final InstitutionRepository institutionRepository;

  public GetInstitutionByIdQueryHandler(InstitutionRepository institutionRepository) {
    this.institutionRepository = institutionRepository;
  }

  @IgrpQueryHandler
  @Transactional(readOnly = true)
  public ResponseEntity<InstitutionResponseDTO> handle(GetInstitutionByIdQuery query) {
    LOGGER.debug("GetInstitutionByIdQuery: {}", query);

    Institution institution = institutionRepository.findById(InstitutionId.from(query.getId()))
        .orElseThrow(() -> IgrpResponseStatusException.notFound(
            "Institution with id '" + query.getId() + "' not found."));

    InstitutionResponseDTO response = new InstitutionResponseDTO();
    response.setId(institution.getId().getStringValor());
    response.setCode(institution.getCode());
    response.setName(institution.getName());
    response.setType(institution.getType());
    response.setIsActive(institution.isActive());
    response.setContactEmail(institution.getContactEmail());
    response.setCreatedAt(null);

    return ResponseEntity.ok(response);
  }
}
