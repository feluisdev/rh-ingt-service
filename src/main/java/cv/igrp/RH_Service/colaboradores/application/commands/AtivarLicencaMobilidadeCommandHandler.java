package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.application.services.LicencaService;
import cv.igrp.RH_Service.colaboradores.application.services.MobilidadeService;
import cv.igrp.RH_Service.colaboradores.domain.repository.LicencaMobilidadeRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.LicencaMobilidadeId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import cv.igrp.RH_Service.colaboradores.application.dto.LicencaEfeitoResponseDTO;

/**
 * Activar é o mesmo que aprovar — mantido por compatibilidade do endpoint
 * {@code PATCH /licencas-mobilidade/{licencaId}/ativar}. Passa pelas mesmas validações e exige que
 * o registo esteja PENDING; antes activava a partir de qualquer estado, incluindo REJECTED e
 * CANCELLED, e gerava a afectação de mobilidade.
 */
@Component
@RequiredArgsConstructor
public class AtivarLicencaMobilidadeCommandHandler
        implements CommandHandler<AtivarLicencaMobilidadeCommand, ResponseEntity<LicencaEfeitoResponseDTO>> {

    private final LicencaMobilidadeRepository licencaRepository;
    private final MobilidadeService mobilidadeService;
    private final LicencaService licencaService;

    @IgrpCommandHandler
    @Transactional
    public ResponseEntity<LicencaEfeitoResponseDTO> handle(AtivarLicencaMobilidadeCommand command) {
        var licenca = licencaRepository.findById(LicencaMobilidadeId.from(command.getLicencaId()))
                .orElseThrow(() -> IgrpResponseStatusException.notFound(
                        "Licença/mobilidade não encontrada: " + command.getLicencaId()));

        if (licenca.isApproved())
            return ResponseEntity.ok(new LicencaEfeitoResponseDTO(
                    licenca.getId().getStringValor(), licenca.getStatus(), false, null, null));

        // Estado primeiro: um registo já decidido não chega sequer às validações do destino.
        if (!licenca.isPending())
            throw IgrpResponseStatusException.conflict(
                    "Apenas registos PENDING podem ser aprovados. Estado actual: " + licenca.getStatus());

        var subtipo = mobilidadeService.subtipoDe(licenca);
        mobilidadeService.validarParaAprovacao(licenca, subtipo);

        licenca.aprovar();
        licencaRepository.save(licenca);

        // A licença pode abrir vaga; a mobilidade nunca o faz. Quem decide é o subtipo.
        var efeito = licencaService.aplicarEntradaEmVigor(licenca, subtipo);
        if (efeito.afectacaoEncerradaId() != null)
            return ResponseEntity.ok(new LicencaEfeitoResponseDTO(
                    licenca.getId().getStringValor(), licenca.getStatus(), true,
                    efeito.afectacaoEncerradaId().toString(), null));

        return ResponseEntity.ok(new LicencaEfeitoResponseDTO(
                licenca.getId().getStringValor(), licenca.getStatus(), true, null, null));
    }
}
