package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.domain.repository.ColocacaoRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.ColocacaoId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Map;

@Component("colabsEncerrarColocacaoCommandHandler")
@RequiredArgsConstructor
public class EncerrarColocacaoCommandHandler
        implements CommandHandler<EncerrarColocacaoCommand, ResponseEntity<Map<String, ?>>> {

    private final ColocacaoRepository colocacaoRepository;
    private final FuncionarioRepository funcionarioRepository;

    @IgrpCommandHandler
    public ResponseEntity<Map<String, ?>> handle(EncerrarColocacaoCommand command) {
        var funcionarioId = FuncionarioId.from(command.getFuncionarioId());

        funcionarioRepository.findById(funcionarioId)
                .orElseThrow(() -> IgrpResponseStatusException.notFound(
                        "Funcionário não encontrado: " + command.getFuncionarioId()));

        var colocacao = colocacaoRepository.findById(ColocacaoId.from(command.getColocacaoId()))
                .orElseThrow(() -> IgrpResponseStatusException.notFound(
                        "Colocação não encontrada: " + command.getColocacaoId()));

        if (!colocacao.getFuncionarioId().equals(funcionarioId))
            throw IgrpResponseStatusException.notFound("Colocação não encontrada: " + command.getColocacaoId());

        if (!Boolean.TRUE.equals(colocacao.getIsActive()))
            throw IgrpResponseStatusException.of(HttpStatus.CONFLICT, "Colocação já se encontra inactiva.");

        if (Boolean.TRUE.equals(colocacao.getIsCurrent()))
            throw IgrpResponseStatusException.of(HttpStatus.CONFLICT,
                    "Não é possível encerrar a colocação actual. Use o registo de nova colocação.");

        colocacao.fechar(LocalDate.now());
        colocacaoRepository.save(colocacao);

        return ResponseEntity.ok(Map.of("message", "Colocação encerrada com sucesso"));
    }
}
