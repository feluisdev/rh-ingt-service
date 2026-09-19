package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.domain.repository.DadosBancariosRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.DadosBancariosId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.application.dto.SuccessResponseDTO;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;


@Component("colabsDesativarDadosBancariosCommandHandler")
@RequiredArgsConstructor
public class DesativarDadosBancariosCommandHandler
        implements CommandHandler<DesativarDadosBancariosCommand, ResponseEntity<SuccessResponseDTO>> {

    private final DadosBancariosRepository dadosBancariosRepository;

    @IgrpCommandHandler
    public ResponseEntity<SuccessResponseDTO> handle(DesativarDadosBancariosCommand command) {
        var id = DadosBancariosId.from(command.getDadosBancariosId());
        var dados = dadosBancariosRepository.findById(id)
                .orElseThrow(() -> IgrpResponseStatusException.notFound(
                        "Dados bancários não encontrados: " + command.getDadosBancariosId()));

        if (Boolean.FALSE.equals(dados.getIsActive()))
            throw IgrpResponseStatusException.badRequest("Os dados bancários já estão inactivos.");

        dados.desativar();
        dadosBancariosRepository.save(dados);
        return ResponseEntity.ok(SuccessResponseDTO.de(command.getDadosBancariosId()));
    }
}
