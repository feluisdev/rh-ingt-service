package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.application.dto.RegistarColaboradorResponseDTO;
import cv.igrp.RH_Service.colaboradores.application.services.AssignmentService;
import cv.igrp.RH_Service.colaboradores.application.services.ContratoService;
import cv.igrp.RH_Service.colaboradores.application.services.DadosBancariosService;
import cv.igrp.RH_Service.colaboradores.application.services.ColaboradorDocumentoService;
import cv.igrp.RH_Service.colaboradores.application.services.FuncionarioService;
import cv.igrp.RH_Service.colaboradores.domain.models.Assignment;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RegistarColaboradorCommandHandler
        implements CommandHandler<RegistarColaboradorCommand, ResponseEntity<RegistarColaboradorResponseDTO>> {

    private final FuncionarioService funcionarioService;
    private final ContratoService contratoService;
    private final AssignmentService assignmentService;
    private final DadosBancariosService dadosBancariosService;
    private final ColaboradorDocumentoService documentoService;

    @IgrpCommandHandler
    @Transactional
    public ResponseEntity<RegistarColaboradorResponseDTO> handle(RegistarColaboradorCommand command) {
        var dto = command.getRequest();

        if (dto.getContrato() != null) {
            dto.getFuncionario().setDataAdmissao(dto.getContrato().getStartDate());
        } else if (dto.getFuncionario().getDataAdmissao() == null) {
            throw IgrpResponseStatusException.badRequest(
                    "A data de admissão é obrigatória quando não é registado contrato.");
        }

        var funcionario = funcionarioService.criarFuncionario(dto.getFuncionario());
        var funcionarioId = funcionario.getId();

        String contratoId = null;
        if (dto.getContrato() != null) {
            var contrato = contratoService.criarContrato(funcionarioId, dto.getContrato());
            contratoId = contrato.getId().getStringValor();
        }

        // Modelo de movimentos: afectação a um Lugar (Position) — funde enquadramento + colocação.
        String afectacaoId = null;
        if (dto.getAfectacao() != null) {
            var af = dto.getAfectacao();
            if (af.getPositionId() == null || af.getPositionId().isBlank())
                throw IgrpResponseStatusException.badRequest("A afectação exige um Lugar (positionId).");

            LocalDate inicio = dto.getContrato() != null
                    ? dto.getContrato().getStartDate()
                    : (af.getDataInicio() != null ? af.getDataInicio() : funcionario.getDataAdmissao());
            String origem = (af.getOrigem() == null || af.getOrigem().isBlank())
                    ? Assignment.ADMISSAO : af.getOrigem();

            var afectacao = assignmentService.afectar(
                    funcionarioId,
                    UUID.fromString(af.getPositionId()),
                    parseUuid(af.getGradeId()),
                    parseUuid(af.getFunctionId()),
                    origem,
                    af.getAssignmentType(),
                    inicio,
                    null,
                    af.getNotes());
            afectacaoId = afectacao.getId().getStringValor();
        }

        String dadosBancariosId = null;
        if (dto.getDadosBancarios() != null) {
            var dadosBancarios = dadosBancariosService.criarDadosBancarios(funcionarioId, dto.getDadosBancarios());
            dadosBancariosId = dadosBancarios.getId().getStringValor();
        }

        List<String> documentoIds = new ArrayList<>();
        if (dto.getDossier() != null) {
            for (var item : dto.getDossier()) {
                var doc = documentoService.registarDocumento(funcionarioId, item);
                documentoIds.add(doc.getId().getStringValor());
            }
        }

        return ResponseEntity.status(201).body(new RegistarColaboradorResponseDTO(
                funcionario.getId().getStringValor(),
                funcionario.getNumeroFuncionario(),
                contratoId,
                afectacaoId,
                dadosBancariosId,
                documentoIds,
                "Colaborador registado com sucesso"));
    }

    private static UUID parseUuid(String v) {
        return (v == null || v.isBlank()) ? null : UUID.fromString(v);
    }
}
