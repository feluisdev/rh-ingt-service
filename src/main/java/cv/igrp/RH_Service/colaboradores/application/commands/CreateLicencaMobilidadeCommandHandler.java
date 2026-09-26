package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.application.services.MobilidadeService;
import cv.igrp.RH_Service.colaboradores.application.services.ImpedimentosDisciplinares;
import cv.igrp.RH_Service.colaboradores.domain.models.FormaPrestacaoMobilidade;
import cv.igrp.RH_Service.colaboradores.domain.models.LicencaMobilidade;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.LicencaMobilidadeRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.SubtipoLicencaMobilidadeRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.SubtipoLicencaMobilidadeId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.application.dto.SuccessResponseDTO;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;


@Component("colabsCreateLicencaMobilidadeCommandHandler")
@RequiredArgsConstructor
public class CreateLicencaMobilidadeCommandHandler
        implements CommandHandler<CreateLicencaMobilidadeCommand, ResponseEntity<SuccessResponseDTO>> {

    private final LicencaMobilidadeRepository licencaRepository;
    private final FuncionarioRepository funcionarioRepository;
    private final SubtipoLicencaMobilidadeRepository subtipoRepository;
    private final MobilidadeService mobilidadeService;
    private final ImpedimentosDisciplinares impedimentos;

    @IgrpCommandHandler
    public ResponseEntity<SuccessResponseDTO> handle(CreateLicencaMobilidadeCommand command) {
        var funcionarioId = FuncionarioId.from(command.getFuncionarioId());
        funcionarioRepository.findById(funcionarioId)
                .orElseThrow(() -> IgrpResponseStatusException.notFound("Funcionário não encontrado: " + command.getFuncionarioId()));

        var dto = command.getRequest();
        var subtipoId = SubtipoLicencaMobilidadeId.from(dto.getSubtipoId());
        var subtipo = subtipoRepository.findById(subtipoId)
                .orElseThrow(() -> IgrpResponseStatusException.notFound("Subtipo não encontrado: " + dto.getSubtipoId()));

        if (subtipo.regressaOuCessa() && dto.getDataInicio() != null)
            impedimentos.impedeNomeacaoDirigente(funcionarioId, dto.getDataInicio()).ifPresent(m -> {
                throw IgrpResponseStatusException.of(org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY, m);
            });

        var formaPrestacao = FormaPrestacaoMobilidade.de(dto.getFormaPrestacao());
        mobilidadeService.validarFormaPrestacao(subtipo, formaPrestacao);

        if (dto.getDataFim() != null && dto.getDataFim().isBefore(dto.getDataInicio()))
            throw IgrpResponseStatusException.badRequest("dataFim não pode ser anterior a dataInicio.");

        var licenca = LicencaMobilidade.criar(
                funcionarioId, subtipoId,
                dto.getDataInicio(), dto.getDataFim(),
                dto.getEntidadeDestino(), dto.getDespachoNumero(), dto.getObservacoes(),
                dto.getJustification(), dto.getDestinationUnitId(), dto.getDestinationPositionId(), null);
        licenca.definirFormaPrestacao(formaPrestacao);

        var saved = licencaRepository.save(licenca);

        return ResponseEntity.status(201).body(SuccessResponseDTO.de(saved.getId().getStringValor()));
    }
}
