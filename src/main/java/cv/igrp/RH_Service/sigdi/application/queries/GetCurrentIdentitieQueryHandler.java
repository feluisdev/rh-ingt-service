package cv.igrp.RH_Service.sigdi.application.queries;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import org.springframework.context.event.EventListener;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import cv.igrp.RH_Service.sigdi.application.dto.IdentityResponseDTO;
import cv.igrp.RH_Service.shared.security.SecurityContextHelper;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.sigdi.domain.strategy.repository.InstitutionalIdentityRepository;
import cv.igrp.RH_Service.sigdi.infrastructure.mappers.strategy.InstitutionalIdentityMapper;

@Component
public class GetCurrentIdentitieQueryHandler implements QueryHandler<GetCurrentIdentitieQuery, ResponseEntity<IdentityResponseDTO>>{


  private static final Logger LOGGER = LoggerFactory.getLogger(GetCurrentIdentitieQueryHandler.class);


  private final InstitutionalIdentityRepository identityRepository;
  private final InstitutionalIdentityMapper identityMapper;
  private final SecurityContextHelper securityContextHelper;

  public GetCurrentIdentitieQueryHandler(InstitutionalIdentityRepository identityRepository,
                                        InstitutionalIdentityMapper identityMapper,
                                        SecurityContextHelper securityContextHelper) {
    this.identityRepository = identityRepository;
    this.identityMapper = identityMapper;
    this.securityContextHelper = securityContextHelper;
  }

   @IgrpQueryHandler
  @Transactional(readOnly = true)
  public ResponseEntity<IdentityResponseDTO> handle(GetCurrentIdentitieQuery query) {
    var instId = securityContextHelper.getCurrentInstitutionId();
    LOGGER.debug("GetCurrentIdentitieQuery: institutionId={}", instId);

    var current = identityRepository.findActive()
        .orElseThrow(() -> IgrpResponseStatusException.notFound(
            "Identidade Institucional ativa não encontrada para a instituição: " + instId));

    return ResponseEntity.ok(identityMapper.toResponse(current));
  }

}
