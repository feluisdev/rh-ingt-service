package cv.igrp.RH_Service.sigdi.application.queries;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import org.springframework.context.event.EventListener;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import cv.igrp.RH_Service.sigdi.application.dto.IdentityResponseDTO;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.sigdi.domain.strategy.repository.InstitutionalIdentityRepository;
import cv.igrp.RH_Service.sigdi.infrastructure.mappers.strategy.InstitutionalIdentityMapper;

@Component
public class GetCurrentIdentitieQueryHandler implements QueryHandler<GetCurrentIdentitieQuery, ResponseEntity<IdentityResponseDTO>>{


  private static final Logger LOGGER = LoggerFactory.getLogger(GetCurrentIdentitieQueryHandler.class);


  private final InstitutionalIdentityRepository identityRepository;
  private final InstitutionalIdentityMapper identityMapper;

  public GetCurrentIdentitieQueryHandler(InstitutionalIdentityRepository identityRepository,
                                        InstitutionalIdentityMapper identityMapper) {
    this.identityRepository = identityRepository;
    this.identityMapper = identityMapper;
  }

   @IgrpQueryHandler
  public ResponseEntity<IdentityResponseDTO> handle(GetCurrentIdentitieQuery query) {

    LOGGER.debug("GetCurrentIdentitieQuery: {}", query);

    var current = identityRepository.findActive()
        .orElseThrow(() -> IgrpResponseStatusException.notFound("Identidade Institucional ativa não encontrada"));

    return ResponseEntity.ok(identityMapper.toResponse(current));
  }

}
