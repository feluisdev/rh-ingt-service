package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.domain.models.Contrato;
import cv.igrp.RH_Service.colaboradores.domain.repository.ContratoRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component("colabsCreateContratoCommandHandler")
@RequiredArgsConstructor
public class CreateContratoCommandHandler
        implements CommandHandler<CreateContratoCommand, ResponseEntity<Map<String, ?>>> {

    private final ContratoRepository contratoRepository;
    private final FuncionarioRepository funcionarioRepository;

    @IgrpCommandHandler
    public ResponseEntity<Map<String, ?>> handle(CreateContratoCommand command) {
        var dto = command.getRequest();
        if (dto.getFuncionarioId() == null || dto.getFuncionarioId().isBlank())
            throw IgrpResponseStatusException.badRequest("O campo funcionarioId é obrigatório.");

        var funcionarioId = FuncionarioId.from(dto.getFuncionarioId());
        funcionarioRepository.findById(funcionarioId)
                .orElseThrow(() -> IgrpResponseStatusException.notFound("Funcionário não encontrado: " + dto.getFuncionarioId()));

        if (contratoRepository.existsActiveByFuncionarioId(funcionarioId))
            throw IgrpResponseStatusException.conflict("Já existe um contrato activo para este funcionário. Encerre-o antes de criar um novo.");

        if (dto.getNumeroContrato() != null && !dto.getNumeroContrato().isBlank()
                && contratoRepository.existsByNumeroContrato(dto.getNumeroContrato()))
            throw IgrpResponseStatusException.conflict("Já existe um contrato com número '" + dto.getNumeroContrato() + "'.");

        var saved = contratoRepository.save(Contrato.criar(funcionarioId, dto.getTipoContrato(),
                dto.getDataInicio(), dto.getDataFim(), dto.getNumeroContrato()));

        return ResponseEntity.status(201).body(Map.of(
                "id", saved.getId().getStringValor(),
                "message", "Criado com sucesso"));
    }
}
