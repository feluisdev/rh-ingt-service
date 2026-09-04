package cv.igrp.RH_Service.sigdi.domain.tatical.repository;

import cv.igrp.RH_Service.shared.domain.pagination.PageResult;
import cv.igrp.RH_Service.sigdi.application.constants.PaaLevel;
import cv.igrp.RH_Service.sigdi.domain.tatical.filter.TaticalActivityFilter;
import cv.igrp.RH_Service.sigdi.domain.tatical.models.TacticalActivity;
import cv.igrp.RH_Service.sigdi.domain.tatical.valueobject.TacticalActivityId;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TacticalActivityRepository {

  TacticalActivity save(TacticalActivity activity);

  Optional<TacticalActivity> findById(TacticalActivityId id);

  Optional<TacticalActivity> findByIdFull(TacticalActivityId id);

  PageResult<TacticalActivity> findAll(TaticalActivityFilter filter);

  long countByStatuses(List<String> statuses);

  /**
   * Read projection of pending activities for the workflow inbox, ordered oldest-first
   * by {@code createdDate} (D-S, {@code 110-01-PLAN.md}).
   */
  List<PendingActivityRow> findPendingRows(List<String> statuses, int page, int size);

  /**
   * Fase 119 (PRZ-05): as unidades orgânicas que já têm pelo menos uma
   * {@code TacticalActivity} no ano e nível dados -- consumido por
   * {@code GetPeriodGenerationQueryHandler} para cruzar as unidades elegíveis de uma finalidade
   * PAA com quem já submeteu. Não filtra por estado da actividade: uma actividade em
   * {@code PENDING_TACTICAL} já foi submetida por quem a criou; o que se pergunta é se a
   * unidade agiu, não se a actividade foi aprovada. {@code year} nulo ou {@code paaLevel} nulo
   * devolvem lista vazia sem tocar no JPA.
   */
  List<UUID> findOrganicUnitIdsWithActivitiesInYear(Integer year, PaaLevel paaLevel);
}
