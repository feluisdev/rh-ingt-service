package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.domain.models.Dependente;
import cv.igrp.RH_Service.colaboradores.domain.repository.DependenteRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component("colabsCreateDependenteCommandHandler")
@RequiredArgsConstructor
public class CreateDependenteCommandHandler
        implements CommandHandler<CreateDependenteCommand, ResponseEntity<Map<String, ?>>> {

    private final DependenteRepository dependenteRepository;
    private final FuncionarioRepository funcionarioRepository;

    @IgrpCommandHandler
    public ResponseEntity<Map<String, ?>> handle(CreateDependenteCommand command) {
        var dto = command.getRequest();
        if (dto.getFuncionarioId() == null || dto.getFuncionarioId().isBlank())
            throw IgrpResponseStatusException.badRequest("O campo funcionarioId é obrigatório.");

        var funcionarioId = FuncionarioId.from(dto.getFuncionarioId());
        funcionarioRepository.findById(funcionarioId)
                .orElseThrow(() -> IgrpResponseStatusException.notFound("Funcionário não encontrado: " + dto.getFuncionarioId()));

        var saved = dependenteRepository.save(Dependente.criar(
                funcionarioId, dto.getNome(), dto.getParentesco(),
                dto.getDataNascimento(), dto.getNif()));

        return ResponseEntity.status(201).body(Map.of(
                "id", saved.getId().getStringValor(),
                "message", "Criado com sucesso"));
    }
}
