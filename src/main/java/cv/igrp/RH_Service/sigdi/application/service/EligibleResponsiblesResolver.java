package cv.igrp.RH_Service.sigdi.application.service;

import cv.igrp.RH_Service.sigdi.application.constants.EligibilitySkipReason;
import cv.igrp.RH_Service.sigdi.application.constants.PaaLevel;
import cv.igrp.RH_Service.sigdi.application.dto.EligibleResponsibleDTO;
import cv.igrp.RH_Service.sigdi.application.dto.EligibleResponsiblesDTO;
import cv.igrp.RH_Service.sigdi.application.dto.EligibleUnitGroupDTO;
import cv.igrp.RH_Service.sigdi.application.dto.FuncionarioDTO;
import cv.igrp.RH_Service.sigdi.application.dto.OrganicaDTO;
import cv.igrp.RH_Service.sigdi.application.dto.SkippedUnitDTO;
import cv.igrp.RH_Service.sigdi.application.port.FuncionarioLookupPort;
import cv.igrp.RH_Service.sigdi.application.port.OrganicaLookupPort;
import cv.igrp.RH_Service.sigdi.domain.tatical.models.PaaSubmissionPeriod;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Fonte de elegibilidade (AUT-06, Fase 116): dado um {@link PaaSubmissionPeriod}, responde
 * quem tem de agir, agrupado por unidade orgânica. Três decisões travadas do operador
 * (116-CONTEXT.md) governam esta classe: (1) em {@code INDIVIDUAL_LEVEL} é elegível todo o
 * funcionário cujo enquadramento na unidade cobre o ano do período, e em
 * {@code UNIT_LEVEL} é elegível o {@code responsibleEmployeeId} da unidade; (2) uma unidade
 * sem responsável (ou sem enquadrados) é saltada e registada como dado visível, nunca
 * apenas em log, e nunca interrompe o varrimento das restantes; (3) esta classe devolve um
 * instantâneo do estado actual -- gravar esse instantâneo para preservar "quem era elegível
 * quando o período abriu" é trabalho da Fase 119, não desta.
 */
@Component
@RequiredArgsConstructor
public class EligibleResponsiblesResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(EligibleResponsiblesResolver.class);

    private final OrganicaLookupPort organicaLookupPort;
    private final FuncionarioLookupPort funcionarioLookupPort;

    public EligibleResponsiblesDTO resolve(PaaSubmissionPeriod period) {
        List<EligibleUnitGroupDTO> groups = new ArrayList<>();
        List<SkippedUnitDTO> skipped = new ArrayList<>();

        for (OrganicaDTO unit : organicaLookupPort.findAllActiveUnits()) {
            Resolution resolution = switch (period.getType()) {
                case UNIT_LEVEL -> resolveUnitLevel(unit);
                case INDIVIDUAL_LEVEL -> resolveIndividualLevel(unit, period.getYear());
            };

            if (resolution.group() != null) {
                groups.add(resolution.group());
            } else {
                // A lista tipada `skipped` é o que cumpre a decisão do operador ("registada
                // de forma visível na aplicação"); o LOGGER.warn é diagnóstico complementar.
                // Quem apagar a lista e ficar só com o log quebra a decisão.
                skipped.add(resolution.skipped());
                LOGGER.warn("Unidade orgânica saltada da elegibilidade: id={}, nome={}, motivo={}, período={}",
                        unit.getId(), unit.getName(), resolution.skipped().getReason(), period.getId());
            }
        }

        int totalEligible = groups.stream().mapToInt(g -> g.getResponsibles().size()).sum();

        EligibleResponsiblesDTO result = new EligibleResponsiblesDTO();
        result.setPeriodId(period.getId() != null ? period.getId().toString() : null);
        result.setPurpose(period.getPurpose().getCode());
        result.setType(period.getType().getCode());
        result.setYear(period.getYear());
        result.setTotalEligible(totalEligible);
        result.setGroups(groups);
        result.setSkipped(skipped);
        return result;
    }

    /**
     * Ramo UNIT_LEVEL: elegível é o {@code responsibleEmployeeId} da unidade. Não sobe à
     * unidade-mãe em circunstância nenhuma -- o {@code parentUnitId} não é consultado.
     */
    private Resolution resolveUnitLevel(OrganicaDTO unit) {
        String rawResponsibleId = unit.getResponsibleEmployeeId();

        if (rawResponsibleId == null) {
            return Resolution.skip(skippedUnit(unit, EligibilitySkipReason.UNIT_WITHOUT_RESPONSIBLE, null));
        }
        if (rawResponsibleId.isBlank()) {
            return Resolution.skip(skippedUnit(unit, EligibilitySkipReason.UNIT_WITHOUT_RESPONSIBLE, rawResponsibleId));
        }

        UUID responsibleId;
        try {
            responsibleId = UUID.fromString(rawResponsibleId.trim());
        } catch (IllegalArgumentException e) {
            // O valor não tem chave estrangeira (V32, Fase 109) e pode ser lixo de
            // digitação -- uma linha suja não pode derrubar o lote inteiro.
            return Resolution.skip(skippedUnit(unit, EligibilitySkipReason.UNIT_WITHOUT_RESPONSIBLE, rawResponsibleId));
        }

        return funcionarioLookupPort.findById(responsibleId)
                .map(responsible -> {
                    EligibleUnitGroupDTO group = newGroup(unit);
                    group.getResponsibles().add(new EligibleResponsibleDTO(responsible.getId(), responsible.getNomeCompleto()));
                    return Resolution.group(group);
                })
                .orElseGet(() -> Resolution.skip(
                        skippedUnit(unit, EligibilitySkipReason.RESPONSIBLE_NOT_FOUND, responsibleId.toString())));
    }

    /**
     * Ramo INDIVIDUAL_LEVEL: elegível é todo o funcionário cujo enquadramento na unidade
     * cobre {@code year}. O {@code responsibleEmployeeId} da unidade não é consultado.
     */
    private Resolution resolveIndividualLevel(OrganicaDTO unit, int year) {
        UUID unitId = UUID.fromString(unit.getId());
        List<UUID> assignedIds = funcionarioLookupPort.findEmployeeIdsAssignedToUnitInYear(unitId, year);

        if (assignedIds.isEmpty()) {
            return Resolution.skip(skippedUnit(unit, EligibilitySkipReason.UNIT_WITHOUT_ASSIGNED_EMPLOYEES, null));
        }

        Map<UUID, FuncionarioDTO> resolved = funcionarioLookupPort.findAllByIds(assignedIds);

        EligibleUnitGroupDTO group = newGroup(unit);
        List<UUID> orphanIds = new ArrayList<>();
        for (UUID employeeId : assignedIds) {
            FuncionarioDTO employee = resolved.get(employeeId);
            if (employee == null) {
                orphanIds.add(employeeId);
                continue;
            }
            group.getResponsibles().add(new EligibleResponsibleDTO(employee.getId(), employee.getNomeCompleto()));
        }

        if (group.getResponsibles().isEmpty()) {
            String detail = "Identificadores de enquadramento sem funcionário correspondente: " + orphanIds;
            return Resolution.skip(skippedUnit(unit, EligibilitySkipReason.UNIT_WITHOUT_ASSIGNED_EMPLOYEES, detail));
        }
        return Resolution.group(group);
    }

    private EligibleUnitGroupDTO newGroup(OrganicaDTO unit) {
        EligibleUnitGroupDTO group = new EligibleUnitGroupDTO();
        group.setUnitId(unit.getId());
        group.setUnitName(unit.getName());
        group.setUnitAcronym(unit.getAcronym());
        return group;
    }

    private SkippedUnitDTO skippedUnit(OrganicaDTO unit, EligibilitySkipReason reason, String detail) {
        SkippedUnitDTO skipped = new SkippedUnitDTO();
        skipped.setUnitId(unit.getId());
        skipped.setUnitName(unit.getName());
        skipped.setReason(reason.getCode());
        skipped.setReasonDescription(reason.getDescription());
        skipped.setDetail(detail);
        return skipped;
    }

    /** Resultado de resolver uma unidade: exactamente um de grupo ou salto, nunca ambos. */
    private record Resolution(EligibleUnitGroupDTO group, SkippedUnitDTO skipped) {
        static Resolution group(EligibleUnitGroupDTO group) {
            return new Resolution(group, null);
        }

        static Resolution skip(SkippedUnitDTO skipped) {
            return new Resolution(null, skipped);
        }
    }
}
