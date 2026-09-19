package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.domain.models.ReciboVencimento;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.ReciboVencimentoRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.application.dto.SuccessResponseDTO;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;


@Component("colabsCriarReciboVencimentoCommandHandler")
@RequiredArgsConstructor
public class CriarReciboVencimentoCommandHandler
        implements CommandHandler<CriarReciboVencimentoCommand, ResponseEntity<SuccessResponseDTO>> {

    private final FuncionarioRepository funcionarioRepository;
    private final ReciboVencimentoRepository reciboVencimentoRepository;

    @IgrpCommandHandler
    public ResponseEntity<SuccessResponseDTO> handle(CriarReciboVencimentoCommand command) {
        var dto = command.getRequest();
        var funcionarioId = FuncionarioId.from(command.getFuncionarioId());

        var funcionario = funcionarioRepository.findById(funcionarioId)
                .orElseThrow(() -> IgrpResponseStatusException.notFound(
                        "Funcionário não encontrado: " + command.getFuncionarioId()));

        if (!Boolean.TRUE.equals(funcionario.getIsActive()))
            throw IgrpResponseStatusException.badRequest(
                    "Não é possível emitir recibo para funcionário inactivo.");

        if (reciboVencimentoRepository.existsByFuncionarioIdAndPeriod(
                funcionarioId, dto.getPeriodMonth(), dto.getPeriodYear())) {
            throw IgrpResponseStatusException.conflict(
                    "Já existe um recibo para o período " + dto.getPeriodMonth() + "/" + dto.getPeriodYear());
        }

        if (dto.getNetSalary().compareTo(dto.getGrossSalary()) > 0)
            throw IgrpResponseStatusException.badRequest(
                    "O salário líquido não pode ser superior ao salário bruto.");

        var saved = reciboVencimentoRepository.save(ReciboVencimento.criar(
                funcionarioId, dto.getPeriodMonth(), dto.getPeriodYear(),
                dto.getIssueDate(), dto.getGrossSalary(), dto.getNetSalary(), null));

        return ResponseEntity.status(201).body(SuccessResponseDTO.de(saved.getId().getStringValor()));
    }
}
