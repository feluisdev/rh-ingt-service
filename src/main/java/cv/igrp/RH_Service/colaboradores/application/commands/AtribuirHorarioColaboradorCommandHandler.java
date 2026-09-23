package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.application.services.HorarioColaboradorService;
import cv.igrp.RH_Service.colaboradores.domain.models.RegimePrestacao;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.shared.application.dto.SuccessResponseDTO;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AtribuirHorarioColaboradorCommandHandler
        implements CommandHandler<AtribuirHorarioColaboradorCommand, ResponseEntity<SuccessResponseDTO>> {

    private final HorarioColaboradorService horarioColaboradorService;

    @IgrpCommandHandler
    public ResponseEntity<SuccessResponseDTO> handle(AtribuirHorarioColaboradorCommand command) {
        var dto = command.getRequest();
        var resultado = horarioColaboradorService.atribuir(FuncionarioId.from(command.getFuncionarioId()),
                dto.getHorarioId(), regime(dto.getRegimePrestacao()), dto.getDataInicio());
        return ResponseEntity.status(201).body(SuccessResponseDTO.de(resultado.atribuicao().getId().getStringValor(),
                resultado.alertas().toArray(String[]::new)));
    }

    private static RegimePrestacao regime(String valor) {
        if (valor == null || valor.isBlank()) return null;
        try {
            return RegimePrestacao.valueOf(valor.trim());
        } catch (IllegalArgumentException e) {
            throw IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY,
                    "regimePrestacao inválido: '" + valor + "'. Valores: PRESENCIAL, TELETRABALHO, MISTO.");
        }
    }
}
