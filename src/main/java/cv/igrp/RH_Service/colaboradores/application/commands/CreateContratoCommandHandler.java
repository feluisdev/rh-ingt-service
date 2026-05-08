package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.domain.models.Contrato;
import cv.igrp.RH_Service.colaboradores.domain.models.Funcionario;
import cv.igrp.RH_Service.shared.application.constants.RegimeTrabalho;
import cv.igrp.RH_Service.colaboradores.domain.repository.ContratoRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.parametrizacoes.domain.repository.ContractTypeRepository;
import cv.igrp.RH_Service.parametrizacoes.domain.valueobject.ContractTypeId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component("colabsCreateContratoCommandHandler")
@RequiredArgsConstructor
public class CreateContratoCommandHandler
        implements CommandHandler<CreateContratoCommand, ResponseEntity<Map<String, ?>>> {

    private final ContratoRepository contratoRepository;
    private final FuncionarioRepository funcionarioRepository;
    private final ContractTypeRepository contractTypeRepository;

    @IgrpCommandHandler
    public ResponseEntity<Map<String, ?>> handle(CreateContratoCommand command) {
        var dto = command.getRequest();

        if (dto.getFuncionarioId() == null || dto.getFuncionarioId().isBlank())
            throw IgrpResponseStatusException.badRequest("O campo funcionarioId é obrigatório.");
        if (dto.getContractTypeId() == null || dto.getContractTypeId().isBlank())
            throw IgrpResponseStatusException.badRequest("O campo contractTypeId é obrigatório.");

        var funcionarioId = FuncionarioId.from(dto.getFuncionarioId());
        Funcionario funcionario = funcionarioRepository.findById(funcionarioId)
                .orElseThrow(() -> IgrpResponseStatusException.notFound("Funcionário não encontrado: " + dto.getFuncionarioId()));

        var contractTypeId = ContractTypeId.from(UUID.fromString(dto.getContractTypeId()));
        var contractType = contractTypeRepository.findById(contractTypeId)
                .orElseThrow(() -> IgrpResponseStatusException.notFound("Tipo de contrato não encontrado: " + dto.getContractTypeId()));

        if (dto.getContractNumber() != null && !dto.getContractNumber().isBlank()
                && contratoRepository.existsByContractNumber(dto.getContractNumber()))
            throw IgrpResponseStatusException.conflict("Já existe um contrato com número '" + dto.getContractNumber() + "'.");

        if (dto.getRegimeTrabalho() != null && RegimeTrabalho.fromCode(dto.getRegimeTrabalho()).isEmpty())
            throw IgrpResponseStatusException.badRequest(
                    "Regime de trabalho inválido: '" + dto.getRegimeTrabalho() + "'. Valores aceites: TEMPO_COMPLETO, TEMPO_PARCIAL, ISENCAO_HORARIO, DEDICACAO_EXCLUSIVA");
        if ("TEMPO_PARCIAL".equals(dto.getRegimeTrabalho()) && dto.getPercentagemTempo() == null)
            throw IgrpResponseStatusException.badRequest("O campo percentagemTempo é obrigatório para regime TEMPO_PARCIAL.");
        if (!"TEMPO_PARCIAL".equals(dto.getRegimeTrabalho()) && dto.getPercentagemTempo() != null)
            throw IgrpResponseStatusException.badRequest("O campo percentagemTempo só se aplica ao regime TEMPO_PARCIAL.");

        // Encerra contrato actual se existir; calcula renewal_count
        int renewalCount = 0;
        var actual = contratoRepository.findCurrentByFuncionarioId(funcionarioId);
        if (actual.isPresent()) {
            var contratoActual = actual.get();
            // é renovação se o tipo de contrato é renovável e coincide com o anterior
            if (contractType.isRenewable()
                    && contractType.getId().getValor().equals(contratoActual.getContractTypeId())) {
                renewalCount = (contratoActual.getRenewalCount() != null ? contratoActual.getRenewalCount() : 0) + 1;
            }
            contratoActual.encerrar(dto.getStartDate().minusDays(1), "SUBSTITUICAO");
            contratoRepository.save(contratoActual);
        }

        var saved = contratoRepository.save(Contrato.criar(
                funcionarioId,
                contractType.getId().getValor(),
                dto.getContractNumber(),
                dto.getStartDate(),
                dto.getEndDate(),
                dto.getLegalBase(),
                dto.getNotes(),
                renewalCount,
                dto.getRegimeTrabalho(),
                dto.getPercentagemTempo()));

        // Actualiza vínculo do funcionário se o tipo de contrato tiver situação profissional configurada
        if (contractType.getProfessionalSituationId() != null) {
            funcionario.atualizarProfessionalSituation(contractType.getProfessionalSituationId());
            funcionarioRepository.save(funcionario);
        }

        return ResponseEntity.status(201).body(Map.of(
                "id", saved.getId().getStringValor(),
                "message", "Criado com sucesso"));
    }
}
