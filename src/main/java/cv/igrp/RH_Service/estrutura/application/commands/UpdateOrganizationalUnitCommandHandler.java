package cv.igrp.RH_Service.estrutura.application.commands;

import cv.igrp.RH_Service.estrutura.application.dto.OrganizationalUnitResponseDTO;
import cv.igrp.RH_Service.estrutura.application.port.FuncionarioLookupPort;
import cv.igrp.RH_Service.estrutura.domain.repository.OrganizationalUnitRepository;
import cv.igrp.RH_Service.estrutura.domain.valueobject.OrganizationalUnitId;
import cv.igrp.RH_Service.estrutura.infrastructure.mappers.OrganizationalUnitMapper;
import cv.igrp.RH_Service.parametrizacoes.application.port.OptionLookupPort;
import cv.igrp.RH_Service.parametrizacoes.domain.models.OptionCcode;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UpdateOrganizationalUnitCommandHandler
        implements CommandHandler<UpdateOrganizationalUnitCommand, ResponseEntity<OrganizationalUnitResponseDTO>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateOrganizationalUnitCommandHandler.class);

    private final OrganizationalUnitRepository unitRepository;
    private final OrganizationalUnitMapper mapper;
    private final OptionLookupPort optionLookupPort;
    private final FuncionarioLookupPort funcionarioLookupPort;
    private final HorarioDaUnidade horarioDaUnidade;
    private final cv.igrp.RH_Service.estrutura.application.services.HorarioDaUnidadeService horarioDaUnidadeService;

    @IgrpCommandHandler
    public ResponseEntity<OrganizationalUnitResponseDTO> handle(UpdateOrganizationalUnitCommand command) {
        var id = OrganizationalUnitId.from(command.getUnitId());
        var unit = unitRepository.findById(id)
                .orElseThrow(() -> IgrpResponseStatusException.notFound(
                        "Unidade orgânica não encontrada: " + command.getUnitId()));

        var dto = command.getRequest();

        if (unitRepository.existsByCodeAndIdNot(dto.getCode(), id)) {
            throw IgrpResponseStatusException.conflict(
                    "Já existe outra unidade orgânica com code='" + dto.getCode() + "'.");
        }

        OrganizationalUnitId parentId = null;
        if (dto.getParentUnitId() != null) {
            parentId = validateAndGetParentId(dto.getParentUnitId().toString(), id);
        }

        UUID responsibleEmployeeId = validateAndGetResponsibleEmployeeId(dto.getResponsibleEmployeeId());

        // PUT e substituicao total (D-10, 109-02-PLAN.md), mesma semantica de
        // parentUnitId: um pedido que omita responsibleEmployeeId limpa-o. O gume e
        // real -- este campo passa a decidir quem avalia quem -- e esta pinado por
        // teste (UpdateOrganizationalUnitCommandHandlerTest, caso 4), nao apenas
        // documentado aqui.
        // A área (V55) é a excepção à substituição total: o ecrã actual não a conhece, e um PUT
        // que a omita não a pode apagar. Limpa-se enviando-a em branco.
        String areaCkey = dto.getAreaCkey() != null ? dto.getAreaCkey() : unit.getAreaCkey();
        unit.atualizar(dto.getCode(), dto.getName(), dto.getAcronym(), dto.getUnitType(),
                dto.getDescricao(), parentId, responsibleEmployeeId, areaCkey);
        // O horário (V57) segue a mesma excepção: omisso fica, em branco limpa.
        // Com data de efeito: de hoje (ou de horarioDesde, futura); os dias passados ficam com o que vigorava.
        if (dto.getHorarioId() != null)
            horarioDaUnidadeService.definir(unit, horarioDaUnidade.validar(dto.getHorarioId()), dto.getHorarioDesde());
        var updated = unitRepository.save(unit);

        var responseDto = mapper.toDTO(updated);
        var horarioHoje = horarioDaUnidadeService.horarioEm(updated, java.time.LocalDate.now());
        responseDto.setHorarioId(horarioHoje != null ? horarioHoje.getStringValor() : null);
        if (updated.getUnitType() != null) {
            optionLookupPort.findByCcodeAndCkey(OptionCcode.UNIT_TYPE.getCode(), updated.getUnitType())
                    .ifPresent(opt -> responseDto.setUnitTypeDesc(opt.cvalue()));
        }
        if (parentId != null) {
            unitRepository.findById(parentId)
                    .ifPresent(parent -> responseDto.setParentUnitName(parent.getName()));
        }
        if (responsibleEmployeeId != null) {
            funcionarioLookupPort.findById(responsibleEmployeeId)
                    .ifPresent(f -> responseDto.setResponsibleEmployeeName(f.getNomeCompleto()));
        }
        return ResponseEntity.ok(responseDto);
    }

    private OrganizationalUnitId validateAndGetParentId(String parentUnitIdStr, OrganizationalUnitId selfId) {
        var parentId = OrganizationalUnitId.from(parentUnitIdStr);
        if (parentId.equals(selfId)) {
            throw IgrpResponseStatusException.badRequest("Uma unidade não pode ser mãe de si própria.");
        }
        var parent = unitRepository.findById(parentId)
                .orElseThrow(() -> IgrpResponseStatusException.notFound(
                        "Unidade-mãe não encontrada: " + parentUnitIdStr));
        if (!parent.isActive()) {
            throw IgrpResponseStatusException.conflict(
                    "Não é possível definir uma unidade inactiva como unidade-mãe.");
        }
        return parentId;
    }

    // Nao valida que o responsavel pertence a propria unidade -- nao-decisao
    // explicita do operador (D-06, 2026-08-24, ver 109-02-PLAN.md): os tres
    // funcionarios da base estao todos na mesma unidade, pelo que a restricao
    // nao seria exercida por dado nenhum. O que se valida e que o funcionario existe.
    private UUID validateAndGetResponsibleEmployeeId(UUID responsibleEmployeeId) {
        if (responsibleEmployeeId == null) {
            return null;
        }
        funcionarioLookupPort.findById(responsibleEmployeeId)
                .orElseThrow(() -> IgrpResponseStatusException.badRequest(
                        "Funcionário responsável não encontrado: " + responsibleEmployeeId));
        return responsibleEmployeeId;
    }
}
