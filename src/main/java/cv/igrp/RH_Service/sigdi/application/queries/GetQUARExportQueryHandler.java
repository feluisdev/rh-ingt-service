package cv.igrp.RH_Service.sigdi.application.queries;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.repository.InstitutionalIdentityEntityRepository;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.repository.OkrEntityRepository;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class GetQUARExportQueryHandler
    implements QueryHandler<GetQUARExportQuery, ResponseEntity<byte[]>> {

  private static final Logger LOGGER = LoggerFactory.getLogger(GetQUARExportQueryHandler.class);

  private final InstitutionalIdentityEntityRepository identityRepository;
  private final OkrEntityRepository okrRepository;

  public GetQUARExportQueryHandler(InstitutionalIdentityEntityRepository identityRepository,
                                    OkrEntityRepository okrRepository) {
    this.identityRepository = identityRepository;
    this.okrRepository = okrRepository;
  }

  @IgrpQueryHandler
  @Transactional(readOnly = true)
  public ResponseEntity<byte[]> handle(GetQUARExportQuery query) {
    LOGGER.debug("GetQUARExportQuery: {}", query);

    identityRepository.findFirstByIsActiveTrue()
        .orElseThrow(() -> IgrpResponseStatusException.notFound("No active institution found"));

    // Validate completeness unless force=true
    if (!Boolean.TRUE.equals(query.getForce())) {
      String cycle = query.getYear().toString();
      long total = okrRepository.findAllByInstitutionIdAndCycle(
          identityRepository.findFirstByIsActiveTrue().get().getInstitutionId(), cycle).size();
      // If checks needed, they would go here — stub: always allow
    }

    String format = query.getFormat() != null ? query.getFormat().toUpperCase() : "PDF";
    String filename = "QUAR_" + query.getYear();

    if ("EXCEL".equals(format)) {
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.parseMediaType(
          "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
      headers.setContentDispositionFormData("attachment", filename + ".xlsx");
      return ResponseEntity.ok().headers(headers).body(new byte[0]);
    }

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_PDF);
    headers.setContentDispositionFormData("attachment", filename + ".pdf");
    return ResponseEntity.ok().headers(headers).body(new byte[0]);
  }
}
