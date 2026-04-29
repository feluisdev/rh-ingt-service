package cv.igrp.RH_Service.sigdi.application.queries;

import cv.igrp.RH_Service.sigdi.application.dto.DelegationResponseDTO;
import cv.igrp.RH_Service.sigdi.domain.admin.models.Delegation;
import cv.igrp.RH_Service.sigdi.domain.admin.repository.UserDelegationRepository;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class GetListDelegationsQueryHandler
    implements QueryHandler<GetListDelegationsQuery, ResponseEntity<List<DelegationResponseDTO>>> {

  private static final Logger LOGGER = LoggerFactory.getLogger(GetListDelegationsQueryHandler.class);

  private final UserDelegationRepository userDelegationRepository;

  public GetListDelegationsQueryHandler(UserDelegationRepository userDelegationRepository) {
    this.userDelegationRepository = userDelegationRepository;
  }

  @IgrpQueryHandler
  @Transactional(readOnly = true)
  public ResponseEntity<List<DelegationResponseDTO>> handle(GetListDelegationsQuery query) {
    LOGGER.debug("GetListDelegationsQuery: {}", query);

    List<Delegation> delegations = userDelegationRepository.findAll();

    List<DelegationResponseDTO> result = delegations.stream()
        .filter(d -> {
          if (query.getDelegatorUserId() != null && !query.getDelegatorUserId().isBlank()) {
            if (!d.getDelegatorId().toString().equals(query.getDelegatorUserId())) return false;
          }
          if (query.getScope() != null && !query.getScope().isBlank()) {
            if (!d.getScope().equalsIgnoreCase(query.getScope())) return false;
          }
          if (query.getIsActive() != null) {
            if (d.isActive() != query.getIsActive()) return false;
          }
          return true;
        })
        .map(d -> {
          DelegationResponseDTO dto = new DelegationResponseDTO();
          dto.setId(d.getId().getStringValor());
          dto.setDelegatorId(d.getDelegatorId().toString());
          dto.setDelegateId(d.getDelegateId().toString());
          dto.setScope(d.getScope());
          dto.setStartDate(d.getStartDate().toString());
          dto.setEndDate(d.getEndDate().toString());
          dto.setReason(d.getReason());
          dto.setIsActive(d.isActive());
          dto.setCreatedAt(null);
          return dto;
        })
        .collect(Collectors.toList());

    return ResponseEntity.ok(result);
  }
}
