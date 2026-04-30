package cv.igrp.RH_Service.carreiras.application.commands;

import cv.igrp.RH_Service.carreiras.domain.repository.GradeRepository;
import cv.igrp.RH_Service.carreiras.domain.valueobject.GradeId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class DesativarGradeCommandHandler
        implements CommandHandler<DesativarGradeCommand, ResponseEntity<Map<String, ?>>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DesativarGradeCommandHandler.class);

    private final GradeRepository gradeRepository;

    @IgrpCommandHandler
    public ResponseEntity<Map<String, ?>> handle(DesativarGradeCommand command) {
        var id = GradeId.from(command.getGradeId());

        var grade = gradeRepository.findById(id)
                .orElseThrow(() -> IgrpResponseStatusException.notFound(
                        "Escalão não encontrado: " + command.getGradeId()));

        if (gradeRepository.isReferencedByActiveAssignment(id)) {
            throw IgrpResponseStatusException.conflict(
                    "Não é possível desactivar o escalão: está referenciado em enquadramentos profissionais activos.");
        }

        grade.desativar();
        gradeRepository.save(grade);

        return ResponseEntity.ok(Map.of("message", "Desactivado com sucesso"));
    }
}
