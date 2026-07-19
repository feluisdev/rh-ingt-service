package cv.igrp.RH_Service.sigdi.application.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import cv.igrp.RH_Service.shared.application.constants.Estado;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.sigdi.application.constants.StrategicGoalsPerspective;
import cv.igrp.RH_Service.sigdi.application.constants.StrategyMapRelationshipType;
import cv.igrp.RH_Service.sigdi.application.dto.StrategyLinkDTO;
import cv.igrp.RH_Service.sigdi.application.dto.StrategyMapLinkResponseDTO;
import cv.igrp.RH_Service.sigdi.domain.strategy.models.BscPerspectiveConfig;
import cv.igrp.RH_Service.sigdi.domain.strategy.models.InstitutionalIdentity;
import cv.igrp.RH_Service.sigdi.domain.strategy.models.StrategicGoal;
import cv.igrp.RH_Service.sigdi.domain.strategy.models.StrategyMapLink;
import cv.igrp.RH_Service.sigdi.domain.strategy.repository.BscPerspectiveConfigRepository;
import cv.igrp.RH_Service.sigdi.domain.strategy.repository.InstitutionalIdentityRepository;
import cv.igrp.RH_Service.sigdi.domain.strategy.repository.StrategicGoalRepository;
import cv.igrp.RH_Service.sigdi.domain.strategy.repository.StrategyMapLinkRepository;
import cv.igrp.RH_Service.sigdi.domain.strategy.valueobject.InstitutionalIdentityId;
import cv.igrp.RH_Service.sigdi.domain.strategy.valueobject.InstitutionalValues;
import cv.igrp.RH_Service.sigdi.domain.strategy.valueobject.StrategicGoalId;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Direction-validation rule under test (V26 seed, displayOrder): FINANCIAL=1, CUSTOMER=2,
 * PROCESS=3, LEARNING=4. ALLOW when source.displayOrder >= target.displayOrder, REJECT when
 * source.displayOrder < target.displayOrder. aprendizagemParaProcessosIsValid and
 * financeiraParaAprendizagemIsRejected are the direction-operator tripwire named cases (RESEARCH
 * Pitfall 1) -- both must exist as literal, individually-named test methods.
 */
@ExtendWith(MockitoExtension.class)
public class CreateStrategyMapLinkCommandHandlerTest {

    @Mock
    private InstitutionalIdentityRepository identityRepository;

    @Mock
    private StrategicGoalRepository goalRepository;

    @Mock
    private StrategyMapLinkRepository linkRepository;

    @Mock
    private BscPerspectiveConfigRepository perspectiveConfigRepository;

    @InjectMocks
    private CreateStrategyMapLinkCommandHandler handler;

    private InstitutionalIdentity activeIdentity() {
        return InstitutionalIdentity.create(UUID.randomUUID(), 2026, "Missão de teste",
                "Visão de teste", InstitutionalValues.of(List.of("Integridade")), "comentário de teste");
    }

    private StrategicGoal goalWithPerspective(InstitutionalIdentityId identityId,
            StrategicGoalsPerspective perspective) {
        return StrategicGoal.reconstruct(StrategicGoalId.gerarNovo(), UUID.randomUUID(), identityId,
                "Objetivo de teste", perspective, BigDecimal.ONE, Estado.A, "Descrição de teste",
                null, null, 2026, List.of());
    }

    private BscPerspectiveConfig perspectiveConfig(String code, Integer displayOrder) {
        return BscPerspectiveConfig.reconstruct(UUID.randomUUID(), code, code, displayOrder);
    }

    private StrategyLinkDTO linkDto(StrategicGoalId sourceId, StrategicGoalId targetId) {
        return new StrategyLinkDTO(sourceId.getStringValor(), targetId.getStringValor(),
                StrategyMapRelationshipType.CAUSE_EFFECT.getCode());
    }

    /** Stubs the checks every test reaching past identity-ownership needs: active identity + both goals. */
    private void stubGoals(InstitutionalIdentity identity, StrategicGoal sourceGoal, StrategicGoal targetGoal) {
        when(identityRepository.findActive()).thenReturn(Optional.of(identity));
        when(goalRepository.findById(sourceGoal.getId())).thenReturn(Optional.of(sourceGoal));
        when(goalRepository.findById(targetGoal.getId())).thenReturn(Optional.of(targetGoal));
    }

    @Test
    void aprendizagemParaProcessosIsValid() {
        InstitutionalIdentity identity = activeIdentity();
        StrategicGoal sourceGoal = goalWithPerspective(identity.getId(), StrategicGoalsPerspective.LEARNING);
        StrategicGoal targetGoal = goalWithPerspective(identity.getId(), StrategicGoalsPerspective.PROCESS);
        stubGoals(identity, sourceGoal, targetGoal);
        when(perspectiveConfigRepository.findByCode("LEARNING"))
                .thenReturn(Optional.of(perspectiveConfig("LEARNING", 4)));
        when(perspectiveConfigRepository.findByCode("PROCESS"))
                .thenReturn(Optional.of(perspectiveConfig("PROCESS", 3)));
        when(linkRepository.findBySourceAndTarget(sourceGoal.getId(), targetGoal.getId()))
                .thenReturn(Optional.empty());
        when(linkRepository.save(any(StrategyMapLink.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CreateStrategyMapLinkCommand command =
                new CreateStrategyMapLinkCommand(linkDto(sourceGoal.getId(), targetGoal.getId()));

        ResponseEntity<StrategyMapLinkResponseDTO> response = handler.handle(command);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void clienteParaFinanceiraIsValid() {
        InstitutionalIdentity identity = activeIdentity();
        StrategicGoal sourceGoal = goalWithPerspective(identity.getId(), StrategicGoalsPerspective.CUSTOMER);
        StrategicGoal targetGoal = goalWithPerspective(identity.getId(), StrategicGoalsPerspective.FINANCIAL);
        stubGoals(identity, sourceGoal, targetGoal);
        when(perspectiveConfigRepository.findByCode("CUSTOMER"))
                .thenReturn(Optional.of(perspectiveConfig("CUSTOMER", 2)));
        when(perspectiveConfigRepository.findByCode("FINANCIAL"))
                .thenReturn(Optional.of(perspectiveConfig("FINANCIAL", 1)));
        when(linkRepository.findBySourceAndTarget(sourceGoal.getId(), targetGoal.getId()))
                .thenReturn(Optional.empty());
        when(linkRepository.save(any(StrategyMapLink.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CreateStrategyMapLinkCommand command =
                new CreateStrategyMapLinkCommand(linkDto(sourceGoal.getId(), targetGoal.getId()));

        ResponseEntity<StrategyMapLinkResponseDTO> response = handler.handle(command);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void financeiraParaAprendizagemIsRejected() {
        InstitutionalIdentity identity = activeIdentity();
        StrategicGoal sourceGoal = goalWithPerspective(identity.getId(), StrategicGoalsPerspective.FINANCIAL);
        StrategicGoal targetGoal = goalWithPerspective(identity.getId(), StrategicGoalsPerspective.LEARNING);
        stubGoals(identity, sourceGoal, targetGoal);
        when(perspectiveConfigRepository.findByCode("FINANCIAL"))
                .thenReturn(Optional.of(perspectiveConfig("FINANCIAL", 1)));
        when(perspectiveConfigRepository.findByCode("LEARNING"))
                .thenReturn(Optional.of(perspectiveConfig("LEARNING", 4)));

        CreateStrategyMapLinkCommand command =
                new CreateStrategyMapLinkCommand(linkDto(sourceGoal.getId(), targetGoal.getId()));

        IgrpResponseStatusException ex =
                assertThrows(IgrpResponseStatusException.class, () -> handler.handle(command));

        assertEquals(
                "Ligação inválida: a perspetiva de origem não pode estar numa ordem inferior à da perspetiva de destino no fluxo causa-efeito do BSC",
                ex.getBody().getTitle());
    }

    @Test
    void sameOrderDifferentGoalsIsAllowed() {
        InstitutionalIdentity identity = activeIdentity();
        StrategicGoal sourceGoal = goalWithPerspective(identity.getId(), StrategicGoalsPerspective.PROCESS);
        StrategicGoal targetGoal = goalWithPerspective(identity.getId(), StrategicGoalsPerspective.PROCESS);
        stubGoals(identity, sourceGoal, targetGoal);
        when(perspectiveConfigRepository.findByCode("PROCESS"))
                .thenReturn(Optional.of(perspectiveConfig("PROCESS", 3)));
        when(linkRepository.findBySourceAndTarget(sourceGoal.getId(), targetGoal.getId()))
                .thenReturn(Optional.empty());
        when(linkRepository.save(any(StrategyMapLink.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CreateStrategyMapLinkCommand command =
                new CreateStrategyMapLinkCommand(linkDto(sourceGoal.getId(), targetGoal.getId()));

        ResponseEntity<StrategyMapLinkResponseDTO> response = handler.handle(command);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void nullPerspectiveYieldsBadRequest() {
        InstitutionalIdentity identity = activeIdentity();
        StrategicGoal sourceGoal = goalWithPerspective(identity.getId(), null);
        StrategicGoal targetGoal = goalWithPerspective(identity.getId(), StrategicGoalsPerspective.PROCESS);
        stubGoals(identity, sourceGoal, targetGoal);

        CreateStrategyMapLinkCommand command =
                new CreateStrategyMapLinkCommand(linkDto(sourceGoal.getId(), targetGoal.getId()));

        IgrpResponseStatusException ex =
                assertThrows(IgrpResponseStatusException.class, () -> handler.handle(command));

        assertEquals("Perspetiva do objetivo não definida", ex.getBody().getTitle());
    }

    @Test
    void unconfiguredPerspectiveYieldsBadRequest() {
        InstitutionalIdentity identity = activeIdentity();
        StrategicGoal sourceGoal = goalWithPerspective(identity.getId(), StrategicGoalsPerspective.LEARNING);
        StrategicGoal targetGoal = goalWithPerspective(identity.getId(), StrategicGoalsPerspective.PROCESS);
        stubGoals(identity, sourceGoal, targetGoal);
        when(perspectiveConfigRepository.findByCode("LEARNING")).thenReturn(Optional.empty());

        CreateStrategyMapLinkCommand command =
                new CreateStrategyMapLinkCommand(linkDto(sourceGoal.getId(), targetGoal.getId()));

        IgrpResponseStatusException ex =
                assertThrows(IgrpResponseStatusException.class, () -> handler.handle(command));

        assertEquals("Perspetiva não configurada: LEARNING", ex.getBody().getTitle());
    }

    @Test
    void selfLinkIsRejected() {
        String goalId = UUID.randomUUID().toString();
        StrategyLinkDTO dto = new StrategyLinkDTO(goalId, goalId, StrategyMapRelationshipType.CAUSE_EFFECT.getCode());
        CreateStrategyMapLinkCommand command = new CreateStrategyMapLinkCommand(dto);

        IgrpResponseStatusException ex =
                assertThrows(IgrpResponseStatusException.class, () -> handler.handle(command));

        assertEquals("sourceGoalId deve ser diferente de targetGoalId", ex.getBody().getTitle());
    }

    @Test
    void duplicateLinkIsRejected() {
        InstitutionalIdentity identity = activeIdentity();
        StrategicGoal sourceGoal = goalWithPerspective(identity.getId(), StrategicGoalsPerspective.LEARNING);
        StrategicGoal targetGoal = goalWithPerspective(identity.getId(), StrategicGoalsPerspective.PROCESS);
        stubGoals(identity, sourceGoal, targetGoal);
        when(perspectiveConfigRepository.findByCode("LEARNING"))
                .thenReturn(Optional.of(perspectiveConfig("LEARNING", 4)));
        when(perspectiveConfigRepository.findByCode("PROCESS"))
                .thenReturn(Optional.of(perspectiveConfig("PROCESS", 3)));
        StrategyMapLink existingLink = StrategyMapLink.create(identity.getInstitutionId(),
                sourceGoal.getId(), targetGoal.getId(), StrategyMapRelationshipType.CAUSE_EFFECT);
        when(linkRepository.findBySourceAndTarget(sourceGoal.getId(), targetGoal.getId()))
                .thenReturn(Optional.of(existingLink));

        CreateStrategyMapLinkCommand command =
                new CreateStrategyMapLinkCommand(linkDto(sourceGoal.getId(), targetGoal.getId()));

        IgrpResponseStatusException ex =
                assertThrows(IgrpResponseStatusException.class, () -> handler.handle(command));

        assertEquals("Já existe um link com os mesmos goals", ex.getBody().getTitle());
    }

    @Test
    void crossIdentityGoalIsRejected() {
        InstitutionalIdentity identity = activeIdentity();
        StrategicGoal sourceGoal =
                goalWithPerspective(InstitutionalIdentityId.gerarNovo(), StrategicGoalsPerspective.LEARNING);
        StrategicGoal targetGoal = goalWithPerspective(identity.getId(), StrategicGoalsPerspective.PROCESS);
        stubGoals(identity, sourceGoal, targetGoal);

        CreateStrategyMapLinkCommand command =
                new CreateStrategyMapLinkCommand(linkDto(sourceGoal.getId(), targetGoal.getId()));

        IgrpResponseStatusException ex =
                assertThrows(IgrpResponseStatusException.class, () -> handler.handle(command));

        assertEquals("Os goals devem pertencer à identity ativa", ex.getBody().getTitle());
    }
}
