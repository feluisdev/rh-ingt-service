package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.domain.models.LicencaMobilidade;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.LicencaMobilidadeRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.SubtipoLicencaMobilidadeRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.SubtipoLicencaMobilidadeId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component("colabsCreateLicencaMobilidadeCommandHandler")
@RequiredArgsConstructor
public class CreateLicencaMobilidadeCommandHandler
        implements CommandHandler<CreateLicencaMobilidadeCommand, ResponseEntity<Map<String, ?>>> {

    private final LicencaMobilidadeRepository licencaRepository;
    private final FuncionarioRepository funcionarioRepository;
    private final SubtipoLicencaMobilidadeRepository subtipoRepository;

    @IgrpCommandHandler
    public ResponseEntity<Map<String, ?>> handle(CreateLicencaMobilidadeCommand command) {
        var funcionarioId = FuncionarioId.from(command.getFuncionarioId());
        funcionarioRepository.findById(funcionarioId)
                .orElseThrow(() -> IgrpResponseStatusException.notFound("Funcionário não encontrado: " + command.getFuncionarioId()));

        var dto = command.getRequest();
        var subtipoId = SubtipoLicencaMobilidadeId.from(dto.getSubtipoId());
        subtipoRepository.findById(subtipoId)
                .orElseThrow(() -> IgrpResponseStatusException.notFound("Subtipo não encontrado: " + dto.getSubtipoId()));

        if (dto.getDataFim() != null && dto.getDataFim().isBefore(dto.getDataInicio()))
            throw IgrpResponseStatusException.badRequest("dataFim não pode ser anterior a dataInicio.");

        var saved = licencaRepository.save(LicencaMobilidade.criar(
                funcionarioId, subtipoId,
                dto.getDataInicio(), dto.getDataFim(),
                dto.getEntidadeDestino(), dto.getDespachoNumero(), dto.getObservacoes()));

        return ResponseEntity.status(201).body(Map.of(
                "id", saved.getId().getStringValor(),
                "message", "Criado com sucesso"));
    }
}
