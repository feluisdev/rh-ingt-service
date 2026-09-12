package cv.igrp.RH_Service.sigdi.application.queries;

import cv.igrp.RH_Service.sigdi.infrastructure.persistence.entity.StrategicGoalEntity;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.entity.StrategicIndicatorEntity;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.entity.TacticalActivitiesEntity;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.entity.KeyResultsEntity;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.repository.StrategicGoalEntityRepository;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.repository.TacticalActivitiesEntityRepository;
import cv.igrp.RH_Service.sigdi.application.service.ExportDocumentHelper;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.entity.InstitutionalIdentityEntity;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.entity.OkrEntity;
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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class GetQUARExportQueryHandler
    implements QueryHandler<GetQUARExportQuery, ResponseEntity<byte[]>> {

  private static final Logger LOGGER = LoggerFactory.getLogger(GetQUARExportQueryHandler.class);

  private final InstitutionalIdentityEntityRepository identityRepository;
  private final OkrEntityRepository okrRepository;
  private final StrategicGoalEntityRepository strategicGoalRepository;
  private final TacticalActivitiesEntityRepository tacticalActivitiesRepository;

  public GetQUARExportQueryHandler(InstitutionalIdentityEntityRepository identityRepository,
                                     OkrEntityRepository okrRepository,
                                     StrategicGoalEntityRepository strategicGoalRepository,
                                     TacticalActivitiesEntityRepository tacticalActivitiesRepository) {
    this.identityRepository = identityRepository;
    this.okrRepository = okrRepository;
    this.strategicGoalRepository = strategicGoalRepository;
    this.tacticalActivitiesRepository = tacticalActivitiesRepository;
  }

  @IgrpQueryHandler
  @Transactional(readOnly = true)
  public ResponseEntity<byte[]> handle(GetQUARExportQuery query) {
    LOGGER.debug("GetQUARExportQuery: {}", query);

    InstitutionalIdentityEntity identity = identityRepository.findFirstByIsActiveTrue()
        .orElseThrow(() -> IgrpResponseStatusException.notFound("No active institution found"));

    String cycle = query.getYear().toString();
    List<OkrEntity> okrs = okrRepository.findAllByInstitutionIdAndCycle(identity.getInstitutionId(), cycle);

    String format = query.getFormat() != null ? query.getFormat().toUpperCase() : "PDF";
    String filename = "QUAR_" + query.getYear();

    if ("EXCEL".equals(format) || "CSV".equals(format)) {
      List<String[]> rows = new ArrayList<>();
      rows.add(new String[]{"QUADRO DE AVALIACAO E RESPONSABILIZACAO (QUAR) - " + query.getYear()});
      rows.add(new String[]{"Missao", identity.getMission() != null ? identity.getMission() : ""});
      rows.add(new String[]{"Visao", identity.getVision() != null ? identity.getVision() : ""});
      rows.add(new String[]{""});
      rows.add(new String[]{"Objetivo Estrategico", "Resultado-Chave", "Meta", "Valor Atual", "Unidade", "Progresso (%)"});

      if (!okrs.isEmpty()) {
        for (OkrEntity okr : okrs) {
          if (okr.getKeyResults() == null || okr.getKeyResults().isEmpty()) {
            rows.add(new String[]{okr.getTitle(), "Sem KRs associados", "-", "-", "-", "0%"});
          } else {
            for (var kr : okr.getKeyResults()) {
              BigDecimal target = kr.getTargetValue() != null ? kr.getTargetValue() : BigDecimal.ZERO;
              BigDecimal current = kr.getCurrentValue() != null ? kr.getCurrentValue() : BigDecimal.ZERO;
              double progress = target.compareTo(BigDecimal.ZERO) > 0
                  ? Math.min(100.0, current.divide(target, 4, RoundingMode.HALF_UP).doubleValue() * 100.0)
                  : 0.0;
              rows.add(new String[]{
                  okr.getTitle() != null ? okr.getTitle() : "",
                  kr.getTitle() != null ? kr.getTitle() : "",
                  String.valueOf(target),
                  String.valueOf(current),
                  kr.getMetricUnit() != null ? kr.getMetricUnit() : "",
                  String.format("%.1f%%", progress)
              });
            }
          }
        }
      } else {
        // Milestone v33.0 (QUA-01): Export from active strategic goals and PAA KRs
        List<StrategicGoalEntity> goals = strategicGoalRepository.findAll().stream()
            .filter(g -> query.getYear() == null || g.getYear() == null || g.getYear().equals(query.getYear()))
            .collect(Collectors.toList());

        List<TacticalActivitiesEntity> activities = query.getYear() != null
            ? tacticalActivitiesRepository.findAllByFiscalYear(query.getYear())
            : tacticalActivitiesRepository.findAll();

        Map<UUID, List<TacticalActivitiesEntity>> actByGoal = new HashMap<>();
        for (var act : activities) {
          if (act.getStrategicGoalId() != null) {
            actByGoal.computeIfAbsent(act.getStrategicGoalId(), k -> new ArrayList<>()).add(act);
          }
        }

        for (StrategicGoalEntity goal : goals) {
          boolean hasRows = false;
          if (goal.getIndicators() != null && !goal.getIndicators().isEmpty()) {
            for (StrategicIndicatorEntity ind : goal.getIndicators()) {
              rows.add(new String[]{
                  goal.getTitle(),
                  ind.getTitle(),
                  String.valueOf(ind.getTarget() != null ? ind.getTarget() : BigDecimal.ZERO),
                  "0",
                  "%",
                  "0.0%"
              });
              hasRows = true;
            }
          }
          List<TacticalActivitiesEntity> linkedActs = actByGoal.get(goal.getId());
          if (linkedActs != null) {
            for (var act : linkedActs) {
              if (act.getKeyResults() != null) {
                for (KeyResultsEntity kr : act.getKeyResults()) {
                  BigDecimal target = kr.getTargetValue() != null ? kr.getTargetValue() : BigDecimal.ZERO;
                  BigDecimal current = kr.getCurrentValue() != null ? kr.getCurrentValue() : BigDecimal.ZERO;
                  double progress = target.compareTo(BigDecimal.ZERO) > 0
                      ? Math.min(100.0, current.divide(target, 4, RoundingMode.HALF_UP).doubleValue() * 100.0)
                      : 0.0;
                  rows.add(new String[]{
                      goal.getTitle(),
                      kr.getTitle(),
                      String.valueOf(target),
                      String.valueOf(current),
                      kr.getMetricUnit() != null ? kr.getMetricUnit() : "",
                      String.format("%.1f%%", progress)
                  });
                  hasRows = true;
                }
              }
            }
          }
          if (!hasRows) {
            rows.add(new String[]{goal.getTitle(), "Sem KRs associados", "-", "-", "-", "0%"});
          }
        }
      }

      byte[] content = ExportDocumentHelper.createCsv(rows);
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.parseMediaType(
          "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
      headers.setContentDispositionFormData("attachment", filename + ".xlsx");
      return ResponseEntity.ok().headers(headers).body(content);
    }

    // PDF format
    List<String> lines = new ArrayList<>();
    lines.add("Missao: " + (identity.getMission() != null ? identity.getMission() : ""));
    lines.add("Ciclo / Ano: " + query.getYear());

    if (!okrs.isEmpty()) {
      lines.add("Total de Objetivos: " + okrs.size());
      lines.add("----------------------------------------------------------------------");
      lines.add(String.format("%-32s | %-25s | %-8s | %-8s", "Objetivo", "Resultado-Chave", "Meta", "Atual"));
      lines.add("----------------------------------------------------------------------");

      for (OkrEntity okr : okrs) {
        if (okr.getKeyResults() == null || okr.getKeyResults().isEmpty()) {
          lines.add(String.format("%-32s | %-25s | %-8s | %-8s", truncate(okr.getTitle(), 32), "Sem KRs", "-", "-"));
        } else {
          for (var kr : okr.getKeyResults()) {
            BigDecimal target = kr.getTargetValue() != null ? kr.getTargetValue() : BigDecimal.ZERO;
            BigDecimal current = kr.getCurrentValue() != null ? kr.getCurrentValue() : BigDecimal.ZERO;
            lines.add(String.format("%-32s | %-25s | %-8s | %-8s",
                truncate(okr.getTitle(), 32),
                truncate(kr.getTitle(), 25),
                String.valueOf(target),
                String.valueOf(current)));
          }
        }
      }
    } else {
      List<StrategicGoalEntity> goals = strategicGoalRepository.findAll().stream()
          .filter(g -> query.getYear() == null || g.getYear() == null || g.getYear().equals(query.getYear()))
          .collect(Collectors.toList());

      List<TacticalActivitiesEntity> activities = query.getYear() != null
          ? tacticalActivitiesRepository.findAllByFiscalYear(query.getYear())
          : tacticalActivitiesRepository.findAll();

      Map<UUID, List<TacticalActivitiesEntity>> actByGoal = new HashMap<>();
      for (var act : activities) {
        if (act.getStrategicGoalId() != null) {
          actByGoal.computeIfAbsent(act.getStrategicGoalId(), k -> new ArrayList<>()).add(act);
        }
      }

      lines.add("Total de Objetivos Estrategicos: " + goals.size());
      lines.add("----------------------------------------------------------------------");
      lines.add(String.format("%-32s | %-25s | %-8s | %-8s", "Objetivo", "Resultado-Chave", "Meta", "Atual"));
      lines.add("----------------------------------------------------------------------");

      for (StrategicGoalEntity goal : goals) {
        boolean hasItems = false;
        if (goal.getIndicators() != null) {
          for (StrategicIndicatorEntity ind : goal.getIndicators()) {
            lines.add(String.format("%-32s | %-25s | %-8s | %-8s",
                truncate(goal.getTitle(), 32),
                truncate(ind.getTitle(), 25),
                String.valueOf(ind.getTarget() != null ? ind.getTarget() : BigDecimal.ZERO),
                "0"));
            hasItems = true;
          }
        }
        List<TacticalActivitiesEntity> linkedActs = actByGoal.get(goal.getId());
        if (linkedActs != null) {
          for (var act : linkedActs) {
            if (act.getKeyResults() != null) {
              for (KeyResultsEntity kr : act.getKeyResults()) {
                BigDecimal target = kr.getTargetValue() != null ? kr.getTargetValue() : BigDecimal.ZERO;
                BigDecimal current = kr.getCurrentValue() != null ? kr.getCurrentValue() : BigDecimal.ZERO;
                lines.add(String.format("%-32s | %-25s | %-8s | %-8s",
                    truncate(goal.getTitle(), 32),
                    truncate(kr.getTitle(), 25),
                    String.valueOf(target),
                    String.valueOf(current)));
                hasItems = true;
              }
            }
          }
        }
        if (!hasItems) {
          lines.add(String.format("%-32s | %-25s | %-8s | %-8s", truncate(goal.getTitle(), 32), "Sem KRs", "-", "-"));
        }
      }
    }

    byte[] content = ExportDocumentHelper.createSimplePdf("QUADRO DE AVALIACAO E RESPONSABILIZACAO (QUAR) - " + query.getYear(), lines);
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_PDF);
    headers.setContentDispositionFormData("attachment", filename + ".pdf");
    return ResponseEntity.ok().headers(headers).body(content);
  }

  private String truncate(String text, int max) {
    if (text == null) return "";
    return text.length() > max ? text.substring(0, max - 3) + "..." : text;
  }
}
