package cv.igrp.RH_Service.colaboradores.application.services;

import cv.igrp.RH_Service.colaboradores.application.dto.ContratoRequestDTO;
import cv.igrp.RH_Service.colaboradores.domain.models.Contrato;
import cv.igrp.RH_Service.colaboradores.domain.repository.ContratoRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.parametrizacoes.domain.repository.ContractTypeRepository;
import cv.igrp.RH_Service.parametrizacoes.domain.valueobject.ContractTypeId;
import cv.igrp.RH_Service.shared.application.constants.RegimeTrabalho;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ContratoService {

    private final ContratoRepository contratoRepository;
    private final FuncionarioRepository funcionarioRepository;
    private final ContractTypeRepository contractTypeRepository;

    @Transactional
    public Contrato criarContrato(FuncionarioId funcionarioId, ContratoRequestDTO dto) {
        if (dto.getContractTypeId() == null || dto.getContractTypeId().isBlank())
            throw IgrpResponseStatusException.badRequest("O campo contractTypeId é obrigatório.");

        funcionarioRepository.findById(funcionarioId)
                .orElseThrow(() -> IgrpResponseStatusException.notFound(
                        "Funcionário não encontrado: " + funcionarioId.getStringValor()));

        var contractTypeId = ContractTypeId.from(UUID.fromString(dto.getContractTypeId()));
        var contractType = contractTypeRepository.findById(contractTypeId)
                .orElseThrow(() -> IgrpResponseStatusException.notFound(
                        "Tipo de contrato não encontrado: " + dto.getContractTypeId()));

        if (dto.getContractNumber() != null && !dto.getContractNumber().isBlank()
                && contratoRepository.existsByContractNumber(dto.getContractNumber()))
            throw IgrpResponseStatusException.conflict(
                    "Já existe um contrato com número '" + dto.getContractNumber() + "'.");

        if (dto.getRegimeTrabalho() != null && RegimeTrabalho.fromCode(dto.getRegimeTrabalho()).isEmpty())
            throw IgrpResponseStatusException.badRequest(
                    "Regime de trabalho inválido: '" + dto.getRegimeTrabalho() + "'. Valores aceites: "
                            + RegimeTrabalho.codigosValidos());
        if ("TEMPO_PARCIAL".equals(dto.getRegimeTrabalho()) && dto.getPercentagemTempo() == null)
            throw IgrpResponseStatusException.badRequest(
                    "O campo percentagemTempo é obrigatório para regime TEMPO_PARCIAL.");
        if (!"TEMPO_PARCIAL".equals(dto.getRegimeTrabalho()) && dto.getPercentagemTempo() != null)
            throw IgrpResponseStatusException.badRequest(
                    "O campo percentagemTempo só se aplica ao regime TEMPO_PARCIAL.");

        int renewalCount = 0;
        var actual = contratoRepository.findCurrentByFuncionarioId(funcionarioId);
        if (actual.isPresent()) {
            var contratoActual = actual.get();
            if (contractType.isRenewable()
                    && contractType.getId().getValor().equals(contratoActual.getContractTypeId())) {
                renewalCount = (contratoActual.getRenewalCount() != null ? contratoActual.getRenewalCount() : 0) + 1;
            }
            contratoActual.encerrar(dto.getStartDate().minusDays(1), "SUBSTITUICAO");
            contratoRepository.save(contratoActual);
        }

        return contratoRepository.save(Contrato.criar(
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
    }
}
