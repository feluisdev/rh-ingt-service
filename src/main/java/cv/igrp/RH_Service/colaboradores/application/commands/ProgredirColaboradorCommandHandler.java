package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.application.dto.ProgressaoResponseDTO;
import cv.igrp.RH_Service.colaboradores.application.services.AssignmentService;
import cv.igrp.RH_Service.colaboradores.application.services.VinculoLaboralService;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.parametrizacoes.domain.models.VinculoLaboral;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Progressão do colaborador para o escalão imediatamente superior da mesma categoria.
 * Aqui valida-se quem pode progredir (colaborador activo, vínculo elegível); a mecânica
 * da afectação vive em {@link AssignmentService#progredir}.
 */
@Component
@RequiredArgsConstructor
public class ProgredirColaboradorCommandHandler
        implements CommandHandler<ProgredirColaboradorCommand, ResponseEntity<ProgressaoResponseDTO>> {

    private final FuncionarioRepository funcionarioRepository;
    private final VinculoLaboralService vinculoLaboralService;
    private final AssignmentService assignmentService;

    @IgrpCommandHandler
    @Transactional
    public ResponseEntity<ProgressaoResponseDTO> handle(ProgredirColaboradorCommand command) {
        var req = command.getRequest();
        if (req == null || req.getDataEfeito() == null)
            throw IgrpResponseStatusException.badRequest("A data de efeito (dataEfeito) é obrigatória.");

        var funcionarioId = FuncionarioId.from(command.getFuncionarioId());
        var funcionario = funcionarioRepository.findById(funcionarioId)
                .orElseThrow(() -> IgrpResponseStatusException.notFound(
                        "Funcionário não encontrado: " + command.getFuncionarioId()));

        if (!Boolean.TRUE.equals(funcionario.getIsActive()))
            throw IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY,
                    "O colaborador não está activo — não é possível progredir.");

        VinculoLaboral vinculo = vinculoLaboralService.doFuncionario(funcionarioId);
        if (!vinculo.isEligibleForProgression())
            throw IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY,
                    "O vínculo laboral '" + vinculo.getDescription() + "' não permite progressão.");

        var progressao = assignmentService.progredir(
                funcionarioId, req.getDataEfeito(), notas(req.getDespachoNumero(), req.getObservacoes()));

        return ResponseEntity.status(201).body(new ProgressaoResponseDTO(
                progressao.afectacao().getId().getStringValor(),
                funcionarioId.getStringValor(),
                progressao.afectacao().getPositionId().toString(),
                progressao.escalaoAnterior().getId().getStringValor(),
                progressao.escalaoAnterior().getName(),
                progressao.escalaoNovo().getId().getStringValor(),
                progressao.escalaoNovo().getName(),
                req.getDataEfeito()));
    }

    private static String notas(String despachoNumero, String observacoes) {
        String notas = "Progressão" + (despachoNumero != null && !despachoNumero.isBlank()
                ? " (despacho " + despachoNumero + ")" : "");
        return observacoes != null && !observacoes.isBlank() ? notas + " — " + observacoes : notas;
    }
}
