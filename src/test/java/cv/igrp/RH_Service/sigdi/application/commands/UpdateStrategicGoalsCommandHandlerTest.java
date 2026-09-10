package cv.igrp.RH_Service.sigdi.application.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.sigdi.application.constants.StrategicGoalsPerspective;
import cv.igrp.RH_Service.sigdi.application.dto.IncoherentLinkDTO;
import cv.igrp.RH_Service.sigdi.application.dto.StategicGoalResponseDTO;
import cv.igrp.RH_Service.sigdi.application.dto.UpdateStategicGoalDTO;
import cv.igrp.RH_Service.sigdi.application.service.StrategicGoalWindowPolicy;
import cv.igrp.RH_Service.sigdi.application.service.StrategyLinkCoherencePolicy;
import cv.igrp.RH_Service.sigdi.domain.strategy.models.StrategicGoal;
import cv.igrp.RH_Service.sigdi.domain.strategy.models.StrategicIndicator;
import cv.igrp.RH_Service.sigdi.domain.strategy.repository.StrategicGoalRepository;
import cv.igrp.RH_Service.sigdi.domain.strategy.valueobject.InstitutionalIdentityId;
import cv.igrp.RH_Service.sigdi.domain.strategy.valueobject.StrategicGoalId;
import cv.igrp.RH_Service.sigdi.infrastructure.mappers.strategy.StrategicGoalMapper;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
public class UpdateStrategicGoalsCommandHandlerTest {

    @Mock
    private StrategicGoalRepository goalRepository;

    @Mock
    private StrategicGoalMapper goalMapper;

    @Mock
    private StrategicGoalWindowPolicy windowPolicy;

    // FIX-09 / A-124-02, wave 4 of Phase 130. The handler gained a fourth collaborator: the
    // read-only reporter that answers "which stored links would the rule in force no longer allow
    // to be created". Mockito's default answer returns an EMPTY list for a List-returning method,
    // so the tests that are not about the warning need no stub here and get no warning.
    @Mock
    private StrategyLinkCoherencePolicy coherencePolicy;

    @InjectMocks
    private UpdateStrategicGoalsCommandHandler updateStrategicGoalsCommandHandler;

    @Test
    void handleChangesExistingGoalsYearToTheSuppliedValue() {
        StrategicGoal existingGoal = StrategicGoal.create(
                UUID.randomUUID(), InstitutionalIdentityId.gerarNovo(), "Objetivo Original",
                StrategicGoalsPerspective.FINANCIAL, BigDecimal.ONE, "Descrição original",
                2020, new ArrayList<>());

        when(goalRepository.findById(any(StrategicGoalId.class))).thenReturn(Optional.of(existingGoal));
        when(goalRepository.save(any(StrategicGoal.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        stubMapperResponse();

        UpdateStategicGoalDTO dto = new UpdateStategicGoalDTO();
        dto.setTitle("Objetivo Original");
        dto.setDescription("Descrição original");
        dto.setWeight(BigDecimal.ONE);
        dto.setYear(2027);

        ResponseEntity<StategicGoalResponseDTO> response = updateStrategicGoalsCommandHandler.handle(
                new UpdateStrategicGoalsCommand(dto, UUID.randomUUID().toString()));

        ArgumentCaptor<StrategicGoal> savedCaptor = ArgumentCaptor.forClass(StrategicGoal.class);
        verify(goalRepository).save(savedCaptor.capture());
        assertEquals(2027, savedCaptor.getValue().getYear());
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void handleWithNullYearPreservesExistingGoalsYear() {
        StrategicGoal existingGoal = StrategicGoal.create(
                UUID.randomUUID(), InstitutionalIdentityId.gerarNovo(), "Objetivo Original",
                StrategicGoalsPerspective.FINANCIAL, BigDecimal.ONE, "Descrição original",
                2020, new ArrayList<>());

        when(goalRepository.findById(any(StrategicGoalId.class))).thenReturn(Optional.of(existingGoal));
        when(goalRepository.save(any(StrategicGoal.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        stubMapperResponse();

        UpdateStategicGoalDTO dto = new UpdateStategicGoalDTO();
        dto.setTitle("Objetivo Original");
        dto.setDescription("Descrição original");
        dto.setWeight(BigDecimal.ONE);
        dto.setYear(null); // omitted on the edit form -- must NOT clear the existing year

        updateStrategicGoalsCommandHandler.handle(
                new UpdateStrategicGoalsCommand(dto, UUID.randomUUID().toString()));

        ArgumentCaptor<StrategicGoal> savedCaptor = ArgumentCaptor.forClass(StrategicGoal.class);
        verify(goalRepository).save(savedCaptor.capture());
        assertEquals(2020, savedCaptor.getValue().getYear());
    }

    @Test
    void handleWithLegacyNullYearGoalAndOmittedYearThrowsBadRequest() {
        StrategicGoal existingGoal = StrategicGoal.create(
                UUID.randomUUID(), InstitutionalIdentityId.gerarNovo(), "Objetivo Original",
                StrategicGoalsPerspective.FINANCIAL, BigDecimal.ONE, "Descrição original",
                null, new ArrayList<>());

        when(goalRepository.findById(any(StrategicGoalId.class))).thenReturn(Optional.of(existingGoal));

        UpdateStategicGoalDTO dto = new UpdateStategicGoalDTO();
        dto.setTitle("Objetivo Original");
        dto.setDescription("Descrição original");
        dto.setWeight(BigDecimal.ONE);
        dto.setYear(null); // legacy goal + edit that never touches year -- effective year stays null

        IgrpResponseStatusException ex = assertThrows(IgrpResponseStatusException.class,
                () -> updateStrategicGoalsCommandHandler.handle(
                        new UpdateStrategicGoalsCommand(dto, UUID.randomUUID().toString())));

        assertEquals("O ano é obrigatório para a submissão de objetivos estratégicos PAA/BSC.",
                ex.getBody().getTitle());
    }

    @Test
    void handleWithSuppliedYearButNoActivePeriodThrowsBadRequest() {
        StrategicGoal existingGoal = StrategicGoal.create(
                UUID.randomUUID(), InstitutionalIdentityId.gerarNovo(), "Objetivo Original",
                StrategicGoalsPerspective.FINANCIAL, BigDecimal.ONE, "Descrição original",
                2020, new ArrayList<>());

        when(goalRepository.findById(any(StrategicGoalId.class))).thenReturn(Optional.of(existingGoal));
        doThrow(IgrpResponseStatusException.badRequest(
                "Prazo não configurado para a submissão de objetivos estratégicos PAA/BSC"))
                .when(windowPolicy).requireOpenFor(2026);

        UpdateStategicGoalDTO dto = new UpdateStategicGoalDTO();
        dto.setTitle("Objetivo Original");
        dto.setDescription("Descrição original");
        dto.setWeight(BigDecimal.ONE);
        dto.setYear(2026);

        IgrpResponseStatusException ex = assertThrows(IgrpResponseStatusException.class,
                () -> updateStrategicGoalsCommandHandler.handle(
                        new UpdateStrategicGoalsCommand(dto, UUID.randomUUID().toString())));

        assertEquals("Prazo não configurado para a submissão de objetivos estratégicos PAA/BSC",
                ex.getBody().getTitle());
    }

    /**
     * FIX-01 / A-124-01, first half: an ABSENT "indicators" key must preserve what the goal
     * already has. Jackson leaves the DTO field null when the key is missing, and a null list
     * means "do not touch". Before this fix the DTO initialised the field to an empty list, the
     * handler could never see null, and every edit silently wiped the goal's KPIs.
     */
    @Test
    void handleWithAbsentIndicatorsPreservesTheExistingOnes() {
        StrategicIndicator existingIndicator = StrategicIndicator.create(
                "Técnicos certificados em cadastro predial", "certificados / inscritos",
                BigDecimal.valueOf(40), null, "Registo interno", BigDecimal.ONE,
                null, null, null, null);

        List<StrategicIndicator> existingIndicators = new ArrayList<>();
        existingIndicators.add(existingIndicator);

        StrategicGoal existingGoal = StrategicGoal.create(
                UUID.randomUUID(), InstitutionalIdentityId.gerarNovo(), "Objetivo com KPI",
                StrategicGoalsPerspective.LEARNING, BigDecimal.ONE, "Descrição original",
                2026, existingIndicators);

        when(goalRepository.findById(any(StrategicGoalId.class))).thenReturn(Optional.of(existingGoal));
        when(goalRepository.save(any(StrategicGoal.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        stubMapperResponse();

        UpdateStategicGoalDTO dto = new UpdateStategicGoalDTO();
        dto.setTitle("Objetivo com KPI");
        dto.setDescription("Descrição original");
        dto.setWeight(BigDecimal.ONE);
        dto.setYear(2026);
        // setIndicators is deliberately NOT called: this is the absent-key case.

        assertNull(dto.getIndicators(),
                "The DTO must leave indicators null when the key is absent, otherwise the handler "
                        + "cannot tell absent from empty and the KPIs are wiped (A-124-01)");

        updateStrategicGoalsCommandHandler.handle(
                new UpdateStrategicGoalsCommand(dto, UUID.randomUUID().toString()));

        ArgumentCaptor<StrategicGoal> savedCaptor = ArgumentCaptor.forClass(StrategicGoal.class);
        verify(goalRepository).save(savedCaptor.capture());

        List<StrategicIndicator> savedIndicators = savedCaptor.getValue().getIndicators();
        assertEquals(1, savedIndicators.size());
        assertEquals("Técnicos certificados em cadastro predial", savedIndicators.get(0).getTitle());
        assertEquals(existingIndicator.getId(), savedIndicators.get(0).getId());
    }

    /**
     * FIX-01 / A-124-01, second half: an EXPLICIT empty list must replace the existing one, so the
     * goal ends up with zero indicators. This removal is INTENTIONAL and it is not a defect. There
     * is no dedicated indicator endpoint in this service, so updating the goal is the only route
     * the product offers to delete the last KPI; a guard that ignored [] would make that
     * impossible by any route, which would be new debt of our own making.
     */
    @Test
    void handleWithExplicitEmptyIndicatorsRemovesThemAllOnPurpose() {
        List<StrategicIndicator> existingIndicators = new ArrayList<>();
        existingIndicators.add(StrategicIndicator.create(
                "KPI a remover", "processos digitais / total", BigDecimal.valueOf(80),
                null, "Registo interno", BigDecimal.ONE, null, null, null, null));

        StrategicGoal existingGoal = StrategicGoal.create(
                UUID.randomUUID(), InstitutionalIdentityId.gerarNovo(), "Objetivo com KPI",
                StrategicGoalsPerspective.PROCESS, BigDecimal.ONE, "Descrição original",
                2026, existingIndicators);

        when(goalRepository.findById(any(StrategicGoalId.class))).thenReturn(Optional.of(existingGoal));
        when(goalRepository.save(any(StrategicGoal.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        stubMapperResponse();

        UpdateStategicGoalDTO dto = new UpdateStategicGoalDTO();
        dto.setTitle("Objetivo com KPI");
        dto.setDescription("Descrição original");
        dto.setWeight(BigDecimal.ONE);
        dto.setYear(2026);
        dto.setIndicators(new ArrayList<>()); // explicit [] -- the only way to drop the last KPI

        updateStrategicGoalsCommandHandler.handle(
                new UpdateStrategicGoalsCommand(dto, UUID.randomUUID().toString()));

        ArgumentCaptor<StrategicGoal> savedCaptor = ArgumentCaptor.forClass(StrategicGoal.class);
        verify(goalRepository).save(savedCaptor.capture());
        assertEquals(0, savedCaptor.getValue().getIndicators().size());
    }

    /**
     * Stubs {@code goalMapper.toResponse(...)} for the tests that reach the end of the handler.
     *
     * <p><b>Why the four existing tests above changed, written instead of being quietly
     * rewritten.</b> Until wave 4 of Phase 130 the handler ended with
     * {@code return ResponseEntity.ok(goalMapper.toResponse(saved))} and never looked at the
     * result, so an unstubbed mapper mock returning {@code null} was harmless. The handler now
     * CONSUMES that response -- it sets the incoherent-link list on it before returning -- so a
     * {@code null} would raise a NullPointerException coming from the mock and from no defect at
     * all. This stub restores what the real mapper does. Nothing about the behaviour those four
     * tests assert has changed: they still assert the same year, the same indicators and the same
     * status code.
     *
     * <p>The change that DID break call sites -- the new arity of
     * {@code StrategicGoal.update(...)} -- broke none of these: no test in this repository calls
     * that method (docs/qa/130-PRECONDICOES.md, 4.3), only the production handler does.
     */
    private StategicGoalResponseDTO stubMapperResponse() {
        StategicGoalResponseDTO mapped = new StategicGoalResponseDTO();
        when(goalMapper.toResponse(any(StrategicGoal.class))).thenReturn(mapped);
        return mapped;
    }

    private IncoherentLinkDTO anIncoherentLink(String sourceTitle, String targetTitle) {
        IncoherentLinkDTO link = new IncoherentLinkDTO();
        link.setLinkId(UUID.randomUUID());
        link.setSourceGoalId(UUID.randomUUID());
        link.setSourceGoalTitle(sourceTitle);
        link.setSourcePerspective("FINANCIAL");
        link.setSourcePerspectiveOrder(1);
        link.setTargetGoalId(UUID.randomUUID());
        link.setTargetGoalTitle(targetTitle);
        link.setTargetPerspective("LEARNING");
        link.setTargetPerspectiveOrder(4);
        link.setReason(IncoherentLinkDTO.Reason.CAUSE_EFFECT_ORDER_INVERTED);
        link.setReasonDesc("A perspetiva de origem esta numa ordem inferior a da perspetiva de destino no fluxo causa-efeito do BSC.");
        return link;
    }

    /**
     * FIX-09 / A-124-02, case 1: an ABSENT perspective key preserves the one the goal already has.
     * Jackson leaves the DTO field null when the key is missing, and null means "do not touch" --
     * the same semantics every other field of this update carries.
     */
    @Test
    void handleWithAbsentPerspectivePreservesTheExistingOne() {
        StrategicGoal existingGoal = StrategicGoal.create(
                UUID.randomUUID(), InstitutionalIdentityId.gerarNovo(), "Objetivo Original",
                StrategicGoalsPerspective.PROCESS, BigDecimal.ONE, "Descrição original",
                2026, new ArrayList<>());

        when(goalRepository.findById(any(StrategicGoalId.class))).thenReturn(Optional.of(existingGoal));
        when(goalRepository.save(any(StrategicGoal.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        stubMapperResponse();

        UpdateStategicGoalDTO dto = new UpdateStategicGoalDTO();
        dto.setTitle("Objetivo Original");
        dto.setYear(2026);
        // setPerspective is deliberately NOT called: this is the absent-key case.

        assertNull(dto.getPerspective(),
                "The DTO must leave perspective null when the key is absent, otherwise the handler "
                        + "cannot tell absent from sent and an edit that never touched the field would "
                        + "rewrite it");

        updateStrategicGoalsCommandHandler.handle(
                new UpdateStrategicGoalsCommand(dto, UUID.randomUUID().toString()));

        ArgumentCaptor<StrategicGoal> savedCaptor = ArgumentCaptor.forClass(StrategicGoal.class);
        verify(goalRepository).save(savedCaptor.capture());
        assertEquals(StrategicGoalsPerspective.PROCESS, savedCaptor.getValue().getPerspective());
    }

    /**
     * FIX-09 / A-124-02, case 2 -- the finding itself, inverted. A perspective that IS sent and IS
     * different reaches the repository. Before wave 4 of Phase 130 the field did not exist on the
     * DTO, {@code goal.update(...)} took five arguments and copied {@code this.perspective}, and
     * the value measured on the wire came back unchanged with a 200 and a success toast
     * (docs/qa/130-EVIDENCIA-LACUNA-124.md, 2.5).
     */
    @Test
    void handleWithSuppliedPerspectiveSavesTheNewOne() {
        StrategicGoal existingGoal = StrategicGoal.create(
                UUID.randomUUID(), InstitutionalIdentityId.gerarNovo(), "Objetivo Original",
                StrategicGoalsPerspective.CUSTOMER, BigDecimal.ONE, "Descrição original",
                2026, new ArrayList<>());

        when(goalRepository.findById(any(StrategicGoalId.class))).thenReturn(Optional.of(existingGoal));
        when(goalRepository.save(any(StrategicGoal.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        stubMapperResponse();

        UpdateStategicGoalDTO dto = new UpdateStategicGoalDTO();
        dto.setTitle("Objetivo Original");
        dto.setYear(2026);
        dto.setPerspective("FINANCIAL");

        ResponseEntity<StategicGoalResponseDTO> response = updateStrategicGoalsCommandHandler.handle(
                new UpdateStrategicGoalsCommand(dto, UUID.randomUUID().toString()));

        ArgumentCaptor<StrategicGoal> savedCaptor = ArgumentCaptor.forClass(StrategicGoal.class);
        verify(goalRepository).save(savedCaptor.capture());
        assertEquals(StrategicGoalsPerspective.FINANCIAL, savedCaptor.getValue().getPerspective());
        assertEquals(200, response.getStatusCode().value());
    }

    /**
     * FIX-09 / A-124-02, case 3: an UNKNOWN perspective code is refused with 400 and nothing is
     * saved. This is an invalid REQUEST, not an incoherence to warn about -- the asymmetry between
     * decision 2 and decision 3 of 130-CONTEXT.md is deliberate and this test fixes which side of
     * it a bad code falls on.
     */
    @Test
    void handleWithUnknownPerspectiveCodeThrowsBadRequestAndNeverSaves() {
        StrategicGoal existingGoal = StrategicGoal.create(
                UUID.randomUUID(), InstitutionalIdentityId.gerarNovo(), "Objetivo Original",
                StrategicGoalsPerspective.PROCESS, BigDecimal.ONE, "Descrição original",
                2026, new ArrayList<>());

        when(goalRepository.findById(any(StrategicGoalId.class))).thenReturn(Optional.of(existingGoal));

        UpdateStategicGoalDTO dto = new UpdateStategicGoalDTO();
        dto.setTitle("Objetivo Original");
        dto.setYear(2026);
        dto.setPerspective("PERSPETIVA_QUE_NAO_EXISTE");

        IgrpResponseStatusException ex = assertThrows(IgrpResponseStatusException.class,
                () -> updateStrategicGoalsCommandHandler.handle(
                        new UpdateStrategicGoalsCommand(dto, UUID.randomUUID().toString())));

        assertEquals("Perspetiva inválida: PERSPETIVA_QUE_NAO_EXISTE", ex.getBody().getTitle());
        assertEquals(400, ex.getBody().getStatus());
        verify(goalRepository, never()).save(any(StrategicGoal.class));
    }

    /**
     * FIX-09 / A-124-02, case 4 -- decision 2 of 130-CONTEXT.md, WARN AND SAVE. When the reporter
     * comes back with links the rule in force would no longer allow to be created, the answer is
     * still 200, the goal is still saved, and the list travels WITH the saved goal.
     *
     * <p>This is the case that proves the list is not an error signal. Routing it through the
     * error path would make the product look like it failed when it saved -- the mirror image of
     * the defect this wave removes -- and would leave the user in a deadlock the product explains
     * nowhere.
     */
    @Test
    void handleReturnsOkAndSavesWhenThePolicyReportsIncoherentLinks() {
        StrategicGoal existingGoal = StrategicGoal.create(
                UUID.randomUUID(), InstitutionalIdentityId.gerarNovo(), "Objetivo Original",
                StrategicGoalsPerspective.LEARNING, BigDecimal.ONE, "Descrição original",
                2026, new ArrayList<>());

        when(goalRepository.findById(any(StrategicGoalId.class))).thenReturn(Optional.of(existingGoal));
        when(goalRepository.save(any(StrategicGoal.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        stubMapperResponse();
        when(coherencePolicy.findIncoherentLinksForGoal(any(StrategicGoalId.class)))
                .thenReturn(List.of(anIncoherentLink("Objetivo Original", "Objetivo de destino")));

        UpdateStategicGoalDTO dto = new UpdateStategicGoalDTO();
        dto.setTitle("Objetivo Original");
        dto.setYear(2026);
        dto.setPerspective("FINANCIAL");

        ResponseEntity<StategicGoalResponseDTO> response = updateStrategicGoalsCommandHandler.handle(
                new UpdateStrategicGoalsCommand(dto, UUID.randomUUID().toString()));

        assertEquals(200, response.getStatusCode().value());

        ArgumentCaptor<StrategicGoal> savedCaptor = ArgumentCaptor.forClass(StrategicGoal.class);
        verify(goalRepository).save(savedCaptor.capture());
        assertEquals(StrategicGoalsPerspective.FINANCIAL, savedCaptor.getValue().getPerspective());

        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getIncoherentLinks());
        assertEquals(1, response.getBody().getIncoherentLinks().size());
        assertEquals(IncoherentLinkDTO.Reason.CAUSE_EFFECT_ORDER_INVERTED,
                response.getBody().getIncoherentLinks().get(0).getReason());
    }

    /**
     * The other half of case 4, and it is not redundant: when the reporter has nothing to say the
     * field stays NULL rather than becoming an empty list. "Nothing to warn about" is then a state
     * of its own, which is what keeps a caller from having to guess whether an empty list means
     * "no incoherence" or "the reading failed" -- rule 5 of the project CLAUDE.md.
     */
    @Test
    void handleLeavesTheIncoherentLinkListAbsentWhenThereIsNothingToWarnAbout() {
        StrategicGoal existingGoal = StrategicGoal.create(
                UUID.randomUUID(), InstitutionalIdentityId.gerarNovo(), "Objetivo Original",
                StrategicGoalsPerspective.LEARNING, BigDecimal.ONE, "Descrição original",
                2026, new ArrayList<>());

        when(goalRepository.findById(any(StrategicGoalId.class))).thenReturn(Optional.of(existingGoal));
        when(goalRepository.save(any(StrategicGoal.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        stubMapperResponse();
        // coherencePolicy is left unstubbed on purpose: Mockito answers an empty list.

        UpdateStategicGoalDTO dto = new UpdateStategicGoalDTO();
        dto.setTitle("Objetivo Original");
        dto.setYear(2026);
        dto.setPerspective("FINANCIAL");

        ResponseEntity<StategicGoalResponseDTO> response = updateStrategicGoalsCommandHandler.handle(
                new UpdateStrategicGoalsCommand(dto, UUID.randomUUID().toString()));

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertNull(response.getBody().getIncoherentLinks());
    }
}
