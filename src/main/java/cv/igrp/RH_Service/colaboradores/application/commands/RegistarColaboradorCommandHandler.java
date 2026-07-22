package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.application.dto.RegistarColaboradorResponseDTO;
import cv.igrp.RH_Service.colaboradores.application.services.ContratoService;
import cv.igrp.RH_Service.colaboradores.application.services.DadosBancariosService;
import cv.igrp.RH_Service.colaboradores.application.services.ColaboradorDocumentoService;
import cv.igrp.RH_Service.colaboradores.application.services.EnquadramentoService;
import cv.igrp.RH_Service.colaboradores.application.services.FuncionarioService;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class RegistarColaboradorCommandHandler
        implements CommandHandler<RegistarColaboradorCommand, ResponseEntity<RegistarColaboradorResponseDTO>> {

    private final FuncionarioService funcionarioService;
    private final ContratoService contratoService;
    private final EnquadramentoService enquadramentoService;
    private final DadosBancariosService dadosBancariosService;
    private final ColaboradorDocumentoService documentoService;

    @IgrpCommandHandler
    @Transactional
    public ResponseEntity<RegistarColaboradorResponseDTO> handle(RegistarColaboradorCommand command) {
        var dto = command.getRequest();

        if (dto.getEnquadramento() != null && dto.getContrato() == null)
            throw IgrpResponseStatusException.badRequest(
                    "Para registar o enquadramento é necessário incluir os dados do contrato.");

        if (dto.getContrato() != null) {
            dto.getFuncionario().setDataAdmissao(dto.getContrato().getStartDate());
            if (dto.getEnquadramento() != null)
                dto.getEnquadramento().setDataInicio(dto.getContrato().getStartDate());
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

        String enquadramentoId = null;
        if (dto.getEnquadramento() != null) {
            var enquadramento = enquadramentoService.criarEnquadramento(funcionarioId, dto.getEnquadramento());
            enquadramentoId = enquadramento.getId().getStringValor();
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
                enquadramentoId,
                dadosBancariosId,
                documentoIds,
                "Colaborador registado com sucesso"));
    }
}
