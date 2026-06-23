package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.application.dto.ContratoResponseDTO;
import cv.igrp.RH_Service.colaboradores.domain.repository.ContratoRepository;
import cv.igrp.RH_Service.shared.application.constants.RegimeTrabalho;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.ContratoId;
import cv.igrp.RH_Service.colaboradores.infrastructure.mappers.ContratoMapper;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component("colabsUpdateContratoCommandHandler")
@RequiredArgsConstructor
public class UpdateContratoCommandHandler
        implements CommandHandler<UpdateContratoCommand, ResponseEntity<ContratoResponseDTO>> {

    private final ContratoRepository contratoRepository;
    private final ContratoMapper mapper;

    @IgrpCommandHandler
    public ResponseEntity<ContratoResponseDTO> handle(UpdateContratoCommand command) {
        var dto = command.getRequest();
        var id = ContratoId.from(command.getContratoId());

        var contrato = contratoRepository.findById(id)
                .orElseThrow(() -> IgrpResponseStatusException.notFound("Contrato não encontrado: " + command.getContratoId()));

        if (dto.getContractNumber() != null && !dto.getContractNumber().isBlank()
                && contratoRepository.existsByContractNumberAndIdNot(dto.getContractNumber(), id))
            throw IgrpResponseStatusException.conflict("Já existe um contrato com número '" + dto.getContractNumber() + "'.");

        if (dto.getRegimeTrabalho() != null && RegimeTrabalho.fromCode(dto.getRegimeTrabalho()).isEmpty())
            throw IgrpResponseStatusException.badRequest(
                    "Regime de trabalho inválido: '" + dto.getRegimeTrabalho() + "'. Valores aceites: " + RegimeTrabalho.codigosValidos());

        String regimeEfectivo = dto.getRegimeTrabalho() != null ? dto.getRegimeTrabalho() : contrato.getRegimeTrabalho();
        if ("TEMPO_PARCIAL".equals(regimeEfectivo) && dto.getPercentagemTempo() == null && contrato.getPercentagemTempo() == null)
            throw IgrpResponseStatusException.badRequest("O campo percentagemTempo é obrigatório para regime TEMPO_PARCIAL.");
        if (!"TEMPO_PARCIAL".equals(regimeEfectivo)
                && dto.getPercentagemTempo() != null
                && dto.getPercentagemTempo().compareTo(java.math.BigDecimal.ZERO) != 0)
            throw IgrpResponseStatusException.badRequest("O campo percentagemTempo só se aplica ao regime TEMPO_PARCIAL.");

        contrato.atualizar(
                dto.getEndDate() != null ? dto.getEndDate() : contrato.getEndDate(),
                dto.getLegalBase() != null ? dto.getLegalBase() : contrato.getLegalBase(),
                dto.getNotes() != null ? dto.getNotes() : contrato.getNotes(),
                dto.getRegimeTrabalho() != null ? dto.getRegimeTrabalho() : contrato.getRegimeTrabalho(),
                dto.getPercentagemTempo() != null ? dto.getPercentagemTempo() : contrato.getPercentagemTempo()
        );

        return ResponseEntity.ok(mapper.toDTO(contratoRepository.save(contrato)));
    }
}
