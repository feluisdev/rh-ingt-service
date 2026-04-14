package cv.igrp.RH_Service.sigdi.application.queries;

import cv.igrp.RH_Service.sigdi.application.dto.WrapperListIdentitieDTO;
import cv.igrp.RH_Service.sigdi.domain.strategy.repository.InstitutionalIdentityRepository;
import cv.igrp.RH_Service.sigdi.infrastructure.mappers.strategy.InstitutionalIdentityMapper;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class GetListIdentitieQueryHandler implements QueryHandler<GetListIdentitieQuery, ResponseEntity<WrapperListIdentitieDTO>> {

  private static final Logger LOGGER = LoggerFactory.getLogger(GetListIdentitieQueryHandler.class);

  private final InstitutionalIdentityRepository identityRepository;
  private final InstitutionalIdentityMapper identityMapper;

  public GetListIdentitieQueryHandler(InstitutionalIdentityRepository identityRepository,
                                      InstitutionalIdentityMapper identityMapper) {
    this.identityRepository = identityRepository;
    this.identityMapper = identityMapper;
  }

  @IgrpQueryHandler
  public ResponseEntity<WrapperListIdentitieDTO> handle(GetListIdentitieQuery query) {
    LOGGER.debug("GetListIdentitieQuery: {}", query);

    int page = query.getPageNumber() != null ? Integer.parseInt(query.getPageNumber()) : 0;
    int size = query.getPageSize() != null ? Integer.parseInt(query.getPageSize()) : 20;
    Integer cycleYear = query.getCicleYear() != null && !query.getCicleYear().isBlank()
        ? Integer.parseInt(query.getCicleYear()) : null;

    var identities = identityRepository.findAll(cycleYear, page, size);
    long total = identityRepository.countAll(cycleYear);

    var data = identities.stream()
        .map(identityMapper::toResponse)
        .toList();

    WrapperListIdentitieDTO response = new WrapperListIdentitieDTO();
    response.setData(data);
    response.setPageNumber(page);
    response.setPageSize(size);
    response.setTotalElements(total);
    response.setTotalPages((int) Math.ceil((double) total / size));

    return ResponseEntity.ok(response);
  }
}