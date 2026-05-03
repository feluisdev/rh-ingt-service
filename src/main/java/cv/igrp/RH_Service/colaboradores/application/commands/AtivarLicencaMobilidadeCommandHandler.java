package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.domain.models.Colocacao;
import cv.igrp.RH_Service.colaboradores.domain.models.TipoAfectacao;
import cv.igrp.RH_Service.colaboradores.domain.repository.ColocacaoRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.LicencaMobilidadeRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.LicencaMobilidadeId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Map;

@Component("colabsAtivarLicencaMobilidadeCommandHandler")
@RequiredArgsConstructor
public class AtivarLicencaMobilidadeCommandHandler
        implements CommandHandler<AtivarLicencaMobilidadeCommand, ResponseEntity<Map<String, ?>>> {

    private final LicencaMobilidadeRepository licencaRepository;
    private final ColocacaoRepository colocacaoRepository;

    @IgrpCommandHandler
    @Transactional
    public ResponseEntity<Map<String, ?>> handle(AtivarLicencaMobilidadeCommand command) {
        var licenca = licencaRepository.findById(LicencaMobilidadeId.from(command.getLicencaId()))
                .orElseThrow(() -> IgrpResponseStatusException.notFound(
                        "Licença/mobilidade não encontrada: " + command.getLicencaId()));
        if (Boolean.TRUE.equals(licenca.getIsActive()))
            return ResponseEntity.ok(Map.of("message", "Já se encontra activo"));

        licenca.ativar();
        licencaRepository.save(licenca);

        // Create MOBILIDADE colocação — unit is null (external entity during mobility)
        var hoje = LocalDate.now();
        colocacaoRepository.fecharColocacaoAtual(licenca.getFuncionarioId(), hoje);
        var novaColocacao = Colocacao.criar(licenca.getFuncionarioId(), null, null, hoje, TipoAfectacao.MOBILIDADE, null);
        colocacaoRepository.save(novaColocacao);

        return ResponseEntity.ok(Map.of("message", "Activado com sucesso"));
    }
}
