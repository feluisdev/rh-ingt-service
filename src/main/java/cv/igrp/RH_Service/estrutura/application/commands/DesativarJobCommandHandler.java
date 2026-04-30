package cv.igrp.RH_Service.estrutura.application.commands;

import cv.igrp.RH_Service.estrutura.domain.repository.JobRepository;
import cv.igrp.RH_Service.estrutura.domain.valueobject.JobId;
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
public class DesativarJobCommandHandler
        implements CommandHandler<DesativarJobCommand, ResponseEntity<Map<String, ?>>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DesativarJobCommandHandler.class);

    private final JobRepository jobRepository;

    @IgrpCommandHandler
    public ResponseEntity<Map<String, ?>> handle(DesativarJobCommand command) {
        var id = JobId.from(command.getJobId());

        var job = jobRepository.findById(id)
                .orElseThrow(() -> IgrpResponseStatusException.notFound(
                        "Cargo não encontrado: " + command.getJobId()));

        job.desativar();
        jobRepository.save(job);

        return ResponseEntity.ok(Map.of("message", "Desactivado com sucesso"));
    }
}
