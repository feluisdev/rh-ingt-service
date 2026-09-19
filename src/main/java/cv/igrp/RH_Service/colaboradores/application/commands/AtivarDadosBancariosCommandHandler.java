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


@Component("colabsAtivarDadosBancariosCommandHandler")
@RequiredArgsConstructor
public class AtivarDadosBancariosCommandHandler
        implements CommandHandler<AtivarDadosBancariosCommand, ResponseEntity<SuccessResponseDTO>> {

    private final DadosBancariosRepository dadosBancariosRepository;

    @IgrpCommandHandler
    public ResponseEntity<SuccessResponseDTO> handle(AtivarDadosBancariosCommand command) {
        var id = DadosBancariosId.from(command.getDadosBancariosId());
        var dados = dadosBancariosRepository.findById(id)
                .orElseThrow(() -> IgrpResponseStatusException.notFound(
                        "Dados bancários não encontrados: " + command.getDadosBancariosId()));

        if (Boolean.TRUE.equals(dados.getIsActive()))
            return ResponseEntity.ok(SuccessResponseDTO.semEfeito(command.getDadosBancariosId(), "Dados bancários já estão activos."));

        dados.ativar();
        dadosBancariosRepository.save(dados);
        return ResponseEntity.ok(SuccessResponseDTO.de(command.getDadosBancariosId()));
    }
}
