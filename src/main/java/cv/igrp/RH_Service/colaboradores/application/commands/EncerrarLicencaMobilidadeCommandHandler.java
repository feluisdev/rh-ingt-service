package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.domain.repository.ColocacaoRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.LicencaMobilidadeRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.SubtipoLicencaMobilidadeRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.LicencaMobilidadeId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.SubtipoLicencaMobilidadeId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Map;

@Component("colabsEncerrarLicencaMobilidadeCommandHandler")
@RequiredArgsConstructor
public class EncerrarLicencaMobilidadeCommandHandler
        implements CommandHandler<EncerrarLicencaMobilidadeCommand, ResponseEntity<Map<String, ?>>> {

    private final LicencaMobilidadeRepository licencaRepository;
    private final ColocacaoRepository colocacaoRepository;
    private final SubtipoLicencaMobilidadeRepository subtipoRepository;

    @IgrpCommandHandler
    @Transactional
    public ResponseEntity<Map<String, ?>> handle(EncerrarLicencaMobilidadeCommand command) {
        var licenca = licencaRepository.findById(LicencaMobilidadeId.from(command.getLicencaId()))
                .orElseThrow(() -> IgrpResponseStatusException.notFound(
                        "Licença/mobilidade não encontrada: " + command.getLicencaId()));

        if (!licenca.isApproved())
            throw IgrpResponseStatusException.of(HttpStatus.CONFLICT,
                    "Apenas registos ACTIVE podem ser encerrados. Estado actual: " + licenca.getStatus());

        licenca.encerrar();
        licencaRepository.save(licenca);

        var subtipo = subtipoRepository.findById(SubtipoLicencaMobilidadeId.from(licenca.getSubtipoId().getValor()))
                .orElse(null);
        boolean isMobilidade = subtipo != null && "MOBILIDADE".equals(subtipo.getRecordType());

        if (isMobilidade) {
            // Close the MOBILIDADE colocação
            colocacaoRepository.fecharColocacaoAtual(licenca.getFuncionarioId(), LocalDate.now());
            // Restore the most recent non-mobilidade colocação
            colocacaoRepository.findMostRecentNonMobilidadeByFuncionarioId(licenca.getFuncionarioId())
                    .ifPresent(anterior -> {
                        anterior.reabrir();
                        colocacaoRepository.save(anterior);
                    });
        }

        return ResponseEntity.ok(Map.of("message", "Encerrado com sucesso"));
    }
}
