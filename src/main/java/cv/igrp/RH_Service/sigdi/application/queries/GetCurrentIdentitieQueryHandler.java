package cv.igrp.RH_Service.sigdi.application.queries;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import cv.igrp.RH_Service.sigdi.application.dto.IdentityResponseDTO;
import cv.igrp.RH_Service.shared.security.SecurityContextHelper;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.sigdi.domain.strategy.repository.InstitutionalIdentityRepository;
import cv.igrp.RH_Service.sigdi.infrastructure.mappers.strategy.InstitutionalIdentityMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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

    Optional<cv.igrp.RH_Service.sigdi.domain.strategy.models.InstitutionalIdentity> result = identityRepository.findActive();

    if (result.isEmpty() && securityContextHelper.isNonProduction()) {
      LOGGER.warn("No active identity found for institution: {}. Returning MOCK data for non-production environment.", instId);
      return ResponseEntity.ok(buildMockIdentity(instId));
    }

    var current = result.orElseThrow(() -> IgrpResponseStatusException.notFound(
        "Identidade Institucional ativa não encontrada para a instituição: " + instId));

    return ResponseEntity.ok(identityMapper.toResponse(current));
  }

  private IdentityResponseDTO buildMockIdentity(UUID instId) {
    IdentityResponseDTO mock = new IdentityResponseDTO();
    mock.setId(UUID.fromString("00000000-0000-0000-0000-000000000099"));
    mock.setInstitutionId(instId != null ? instId : UUID.fromString("00000000-0000-0000-0000-000000000001"));
    mock.setMission("[MOCK] Promover o desenvolvimento sustentável e eficiente dos recursos humanos do Estado.");
    mock.setVision("[MOCK] Ser referência em gestão de recursos humanos da Administração Pública de Cabo Verde.");
    mock.setValues(List.of("Integridade", "Excelência", "Transparência", "Inovação"));
    mock.setVersionComment("Dados de demonstração — ambiente de desenvolvimento/staging");
    mock.setActive(true);
    mock.setCycleYear(2025);
    mock.setCreatedAt(LocalDateTime.now());
    return mock;
  }

}
