package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.domain.models.LicencaMobilidade;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.LicencaMobilidadeRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.SubtipoLicencaMobilidadeRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.SubtipoLicencaMobilidadeId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.domain.service.CurrentEmployeeResolver;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Component("colabsSelfServiceCriarLicencaMobilidadeCommandHandler")
@RequiredArgsConstructor
public class SelfServiceCriarLicencaMobilidadeCommandHandler
        implements CommandHandler<SelfServiceCriarLicencaMobilidadeCommand, ResponseEntity<Map<String, ?>>> {

    private final CurrentEmployeeResolver currentEmployeeResolver;
    private final FuncionarioRepository funcionarioRepository;
    private final LicencaMobilidadeRepository licencaRepository;
    private final SubtipoLicencaMobilidadeRepository subtipoRepository;

    @IgrpCommandHandler
    @Transactional
    public ResponseEntity<Map<String, ?>> handle(SelfServiceCriarLicencaMobilidadeCommand command) {
        var dto = command.getRequest();
        var funcionarioId = currentEmployeeResolver.resolve();

        var funcionario = funcionarioRepository.findById(funcionarioId)
                .orElseThrow(() -> IgrpResponseStatusException.notFound(
                        "Funcionário não encontrado: " + funcionarioId.getStringValor()));

        if (!Boolean.TRUE.equals(funcionario.getIsActive()))
            throw IgrpResponseStatusException.of(HttpStatus.FORBIDDEN, "Acesso negado: colaborador inactivo.");

        var subtipoId = SubtipoLicencaMobilidadeId.from(dto.getSubtipoId());
        var subtipo = subtipoRepository.findById(subtipoId)
                .orElseThrow(() -> IgrpResponseStatusException.notFound(
                        "Subtipo de licença/mobilidade não encontrado: " + dto.getSubtipoId()));

        if (!Boolean.TRUE.equals(subtipo.getCanSelfSubmit()))
            throw IgrpResponseStatusException.of(HttpStatus.FORBIDDEN,
                    "Este tipo de licença/mobilidade não permite auto-submissão pelo colaborador.");

        var licenca = LicencaMobilidade.criar(funcionarioId, subtipoId,
                dto.getDataInicio(), dto.getDataFim(),
                dto.getEntidadeDestino(), dto.getDespachoNumero(), dto.getObservacoes(),
                dto.getJustification(), null, null);
        var saved = licencaRepository.save(licenca);

        return ResponseEntity.status(201).body(Map.of(
                "id", saved.getId().getStringValor(),
                "message", "Licença/mobilidade submetida com sucesso"));
    }
}
