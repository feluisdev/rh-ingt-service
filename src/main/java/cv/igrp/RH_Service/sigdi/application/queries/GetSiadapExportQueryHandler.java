package cv.igrp.RH_Service.sigdi.application.queries;

import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class GetSiadapExportQueryHandler
    implements QueryHandler<GetSiadapExportQuery, ResponseEntity<byte[]>> {

  private static final Logger LOGGER = LoggerFactory.getLogger(GetSiadapExportQueryHandler.class);

  @IgrpQueryHandler
  public ResponseEntity<byte[]> handle(GetSiadapExportQuery query) {
    LOGGER.debug("GetSiadapExportQuery: {}", query);

    String format = query.getFormat() != null ? query.getFormat().toUpperCase() : "EXCEL";
    String filename = "SIADAP_" + query.getYear();

    if ("PDF_BATCH".equals(format)) {
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.parseMediaType("application/zip"));
      headers.setContentDispositionFormData("attachment", filename + ".zip");
      return ResponseEntity.ok().headers(headers).body(new byte[0]);
    }

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.parseMediaType(
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
    headers.setContentDispositionFormData("attachment", filename + ".xlsx");
    return ResponseEntity.ok().headers(headers).body(new byte[0]);
  }
}
