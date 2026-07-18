package cv.igrp.RH_Service.sigdi.infrastructure.persistence.adapters.strategy;

import cv.igrp.RH_Service.sigdi.infrastructure.persistence.entity.BscPerspectiveConfigEntity;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.repository.BscPerspectiveConfigEntityRepository;
import cv.igrp.RH_Service.sigdi.domain.strategy.models.BscPerspectiveConfig;
import cv.igrp.RH_Service.sigdi.domain.strategy.repository.BscPerspectiveConfigRepository;
import cv.igrp.RH_Service.sigdi.infrastructure.mappers.strategy.BscPerspectiveConfigMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Adapter for the {@link BscPerspectiveConfigRepository} port. Reads/writes ONLY the
 * perspective-config table via {@link BscPerspectiveConfigEntityRepository} -- no other
 * aggregate or table is ever touched here (PERSP-03 structural isolation).
 */
@Component
@RequiredArgsConstructor
public class BscPerspectiveConfigRepositoryImpl implements BscPerspectiveConfigRepository {

  private final BscPerspectiveConfigEntityRepository jpaRepository;
  private final BscPerspectiveConfigMapper mapper;

  @Transactional(readOnly = true)
  @Override
  public List<BscPerspectiveConfig> findAll() {
    return jpaRepository.findAllByOrderByDisplayOrderAsc().stream()
        .map(mapper::toDomain)
        .toList();
  }

  @Transactional(readOnly = true)
  @Override
  public Optional<BscPerspectiveConfig> findByCode(String code) {
    return jpaRepository.findByCode(code)
        .map(mapper::toDomain);
  }

  @Transactional
  @Override
  public List<BscPerspectiveConfig> saveAll(List<BscPerspectiveConfig> configs) {
    // Two-phase write: display_order carries a non-deferrable DB-level UNIQUE constraint (V26).
    // Persisting a genuine swap (two rows exchanging already-in-use order values) in a single
    // pass can send an UPDATE that transiently duplicates a value still held by another row --
    // Postgres enforces UNIQUE constraints immediately per statement, not at commit. Phase 1
    // parks every row on a distinct value outside the valid 1-4 range (guaranteed collision-free,
    // since exactly 4 rows ever exist) and flushes; phase 2 then applies the real requested
    // order, always safe because no row holds an in-range value at that point. Callers (the
    // command handler) still see a single logical save -- this is purely a persistence-layer
    // concern, so the port's one-call contract is unchanged.
    //
    // Audit trade-off (73-REVIEW.md WR-02): the entity is @Audited (Hibernate Envers), so this
    // two-phase write creates 8 Envers revision rows per call (4 parked + 4 real) in
    // t_bsc_perspective_config_aud for what is, from the admin's point of view, a single logical
    // "update the 4 perspectives" action -- 4 of which show a transient, meaningless
    // display_order in {-1,-2,-3,-4}. This is a deliberate, known trade-off, not a bug: it is the
    // price paid for dodging the non-deferrable UNIQUE constraint without bypassing the JPA
    // persistence context. Anyone auditing t_bsc_perspective_config_aud should disregard revision
    // rows with a negative display_order -- they never represent real business data. A future
    // optimization could replace the parking phase with a bulk JPQL/native UPDATE (which Envers
    // does not track, since bulk updates bypass the persistence context), but that requires an
    // explicit entityManager.clear()/refresh before phase 2 reads/writes the same rows and is
    // therefore a deliberate design decision, not a drop-in change -- left for a future phase.
    List<BscPerspectiveConfigEntity> parked = configs.stream()
        .map(mapper::toEntity)
        .toList();
    for (int i = 0; i < parked.size(); i++) {
      parked.get(i).setDisplayOrder(-(i + 1));
    }
    jpaRepository.saveAllAndFlush(parked);

    List<BscPerspectiveConfigEntity> entities = configs.stream()
        .map(mapper::toEntity)
        .toList();
    return jpaRepository.saveAllAndFlush(entities).stream()
        .map(mapper::toDomain)
        .toList();
  }
}
