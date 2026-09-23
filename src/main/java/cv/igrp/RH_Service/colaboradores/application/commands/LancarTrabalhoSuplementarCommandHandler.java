package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.application.dto.TrabalhoSuplementarRequestDTO;
import cv.igrp.RH_Service.colaboradores.application.services.TrabalhoSuplementarService;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.shared.application.dto.SuccessResponseDTO;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.domain.service.CurrentEmployeeResolver;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.time.format.DateTimeParseException;

@Component
@RequiredArgsConstructor
public class LancarTrabalhoSuplementarCommandHandler
        implements CommandHandler<LancarTrabalhoSuplementarCommand, ResponseEntity<SuccessResponseDTO>> {

    private final CurrentEmployeeResolver currentEmployeeResolver;
    private final TrabalhoSuplementarService trabalhoSuplementarService;

    @IgrpCommandHandler
    public ResponseEntity<SuccessResponseDTO> handle(LancarTrabalhoSuplementarCommand command) {
        TrabalhoSuplementarRequestDTO dto = pedido(command.getRequest());
        FuncionarioId chefe = command.isPelaChefia() ? currentEmployeeResolver.resolve() : null;
        String funcionario = command.isPelaChefia() ? dto.getFuncionarioId() : command.getFuncionarioId();
        var t = trabalhoSuplementarService.lancar(chefe, funcionarioId(funcionario), dto.getData(),
                hora(dto.getHoraInicio(), "horaInicio"), hora(dto.getHoraFim(), "horaFim"), dto.getMotivo());
        return ResponseEntity.status(201).body(SuccessResponseDTO.de(t.getId().getStringValor()));
    }

    static TrabalhoSuplementarRequestDTO pedido(TrabalhoSuplementarRequestDTO dto) {
        if (dto == null) throw invalido("O pedido não traz dados: data, horaInicio, horaFim e motivo.");
        return dto;
    }

    static FuncionarioId funcionarioId(String valor) {
        if (valor == null || valor.isBlank()) throw invalido("O funcionarioId é obrigatório.");
        try {
            return FuncionarioId.from(valor.trim());
        } catch (IllegalArgumentException e) {
            throw invalido("funcionarioId inválido: " + valor + ".");
        }
    }

    static LocalTime hora(String valor, String campo) {
        if (valor == null || valor.isBlank()) return null;
        try {
            return LocalTime.parse(valor.trim());
        } catch (DateTimeParseException e) {
            throw invalido(campo + " escreve-se HH:mm (ex.: 18:30): '" + valor + "'.");
        }
    }

    private static IgrpResponseStatusException invalido(String mensagem) {
        return IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY, mensagem);
    }
}
