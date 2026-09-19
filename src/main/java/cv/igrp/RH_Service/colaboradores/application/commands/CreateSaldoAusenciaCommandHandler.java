package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.domain.models.SaldoAusencia;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.SaldoAusenciaRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.TipoAusenciaRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.TipoAusenciaId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.application.dto.SuccessResponseDTO;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;


@Component("colabsCreateSaldoAusenciaCommandHandler")
@RequiredArgsConstructor
public class CreateSaldoAusenciaCommandHandler
        implements CommandHandler<CreateSaldoAusenciaCommand, ResponseEntity<SuccessResponseDTO>> {

    private final SaldoAusenciaRepository saldoRepository;
    private final FuncionarioRepository funcionarioRepository;
    private final TipoAusenciaRepository tipoAusenciaRepository;

    @IgrpCommandHandler
    public ResponseEntity<SuccessResponseDTO> handle(CreateSaldoAusenciaCommand command) {
        var funcionarioId = FuncionarioId.from(command.getFuncionarioId());
        funcionarioRepository.findById(funcionarioId)
                .orElseThrow(() -> IgrpResponseStatusException.notFound("Funcionário não encontrado: " + command.getFuncionarioId()));

        var dto = command.getRequest();
        var tipoId = TipoAusenciaId.from(dto.getTipoAusenciaId());
        tipoAusenciaRepository.findById(tipoId)
                .orElseThrow(() -> IgrpResponseStatusException.notFound("Tipo de ausência não encontrado: " + dto.getTipoAusenciaId()));

        if (saldoRepository.existsByFuncionarioIdAndTipoAusenciaIdAndAno(funcionarioId, tipoId, dto.getAno()))
            throw IgrpResponseStatusException.conflict(
                    "Já existe saldo para este funcionário, tipo e ano: " + dto.getAno());

        var saved = saldoRepository.save(
                SaldoAusencia.criar(funcionarioId, tipoId, dto.getAno(), dto.getDiasDireito()));

        return ResponseEntity.status(201).body(SuccessResponseDTO.de(saved.getId().getStringValor()));
    }
}
