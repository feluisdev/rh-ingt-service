package cv.igrp.RH_Service.sigdi.infrastructure.persistence.adapters.compliance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import cv.igrp.RH_Service.sigdi.domain.compliance.valueobject.SiadapEvaluationId;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.entity.SiadapEvaluationEntity;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.repository.CompetencyItemEntityRepository;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.repository.IndividualObjectiveEntityRepository;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.repository.SiadapEvaluationEntityRepository;
import cv.igrp.RH_Service.sigdi.infrastructure.mappers.compliance.SiadapEvaluationMapper;
import java.lang.reflect.Field;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.repository.CrudRepository;

/**
 * Prova de que {@code SiadapEvaluationRepositoryImpl.deleteById} apaga ao nível da entidade
 * ({@code jpaRepository.delete(entity)}), na ordem filhas-antes-do-pai, e nunca por um
 * {@code deleteAllInBatch} ou JPQL de remoção -- T-120-07 do threat model do plano 120-02.
 *
 * <p>Os quatro comportamentos deste teste espelham, um a um, o {@code <behavior>} da Task 1 do
 * plano.
 */
@ExtendWith(MockitoExtension.class)
class SiadapEvaluationDeletionTest {

  @Mock
  private SiadapEvaluationEntityRepository jpaRepository;

  @Mock
  private IndividualObjectiveEntityRepository objectiveJpaRepository;

  @Mock
  private CompetencyItemEntityRepository competencyJpaRepository;

  @Mock
  private SiadapEvaluationMapper mapper;

  @InjectMocks
  private SiadapEvaluationRepositoryImpl adapter;

  @Test
  void deleteByIdOfExistingEvaluationDeletesChildrenBeforeEntityInOrder() {
    UUID uuid = UUID.randomUUID();
    SiadapEvaluationId id = SiadapEvaluationId.from(uuid);
    SiadapEvaluationEntity entity = mock(SiadapEvaluationEntity.class);
    when(jpaRepository.findById(uuid)).thenReturn(Optional.of(entity));

    adapter.deleteById(id);

    // Estreitamento a CrudRepository: a partir do Spring Data JPA 3.5, delete(T) (CrudRepository)
    // e delete(Specification<T>) (JpaSpecificationExecutor) tornam jpaRepository.delete(entity)
    // ambíguo em tempo de compilação, porque SiadapEvaluationEntityRepository estende os dois --
    // o mesmo estreitamento que o adaptador faz.
    CrudRepository<SiadapEvaluationEntity, UUID> crud = jpaRepository;

    InOrder inOrder = Mockito.inOrder(objectiveJpaRepository, competencyJpaRepository, crud);
    inOrder.verify(objectiveJpaRepository).deleteByEvaluationId(uuid);
    inOrder.verify(competencyJpaRepository).deleteByEvaluationId(uuid);
    inOrder.verify(crud).delete(entity);
    // Nunca uma remoção em bloco -- teria de passar ao lado do ciclo de vida da entidade e do
    // Envers em silêncio (T-120-07).
    verify(jpaRepository, never()).deleteAllInBatch(any());
    verify(jpaRepository, never()).deleteAllById(any());
  }

  @Test
  void deleteByIdOfMissingEvaluationDoesNotThrowAndReturnsFalse() {
    UUID uuid = UUID.randomUUID();
    SiadapEvaluationId id = SiadapEvaluationId.from(uuid);
    when(jpaRepository.findById(uuid)).thenReturn(Optional.empty());

    boolean result = adapter.deleteById(id);

    assertFalse(result);
    CrudRepository<SiadapEvaluationEntity, UUID> crud = jpaRepository;
    verify(crud, never()).delete(any());
    verifyNoInteractions(objectiveJpaRepository, competencyJpaRepository);
  }

  @Test
  void deleteByIdOfExistingEvaluationReturnsTrue() {
    UUID uuid = UUID.randomUUID();
    SiadapEvaluationId id = SiadapEvaluationId.from(uuid);
    SiadapEvaluationEntity entity = mock(SiadapEvaluationEntity.class);
    when(jpaRepository.findById(uuid)).thenReturn(Optional.of(entity));

    boolean result = adapter.deleteById(id);

    assertTrue(result);
  }

  /**
   * As quatro tabelas <i>interim</i> ({@code SiadapInterimFeedbackEntity},
   * {@code SiadapInterimObjectiveRevisionEntity}, {@code SiadapInterimImprovementActionEntity},
   * {@code SiadapInterimCompetencyObservationEntity}) só podem ter linhas a partir de
   * {@code IN_PROGRESS} -- uma avaliação apagável está sempre em {@code OPEN} e sem objetivos,
   * logo nunca as tem. Este teste prova, por reflexão sobre os campos declarados do adaptador, que
   * nenhum dos quatro repositórios <i>interim</i> é sequer injectado -- não há como chamá-los se
   * não existem como dependência.
   */
  @Test
  void adapterNeverInjectsAnyOfTheFourInterimRepositories() {
    String[] forbidden = {
        "SiadapInterimFeedbackEntityRepository",
        "SiadapInterimObjectiveRevisionEntityRepository",
        "SiadapInterimImprovementActionEntityRepository",
        "SiadapInterimCompetencyObservationEntityRepository"
    };

    for (Field field : SiadapEvaluationRepositoryImpl.class.getDeclaredFields()) {
      String typeName = field.getType().getSimpleName();
      for (String forbiddenType : forbidden) {
        assertFalse(typeName.equals(forbiddenType),
            "SiadapEvaluationRepositoryImpl não pode injectar " + forbiddenType);
      }
    }
  }
}
