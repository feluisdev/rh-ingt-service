package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.application.services.LicencaEfeitoService;
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
 * Aprovar = pôr em vigor a licença ou a mobilidade.
 *
 * <p><b>A mobilidade não mexe na afectação.</b> A mobilidade transitória não ocupa lugar do quadro
 * no destino (Lei n.º 20/X/2023, art. 135.º n.º 7): o colaborador continua titular do seu Lugar e o
 * regresso fica garantido. Antes, aprovar fechava a afectação de origem e ocupava um Lugar no
 * destino — o Lugar de origem ficava vago e podia ser ocupado por outra pessoa, deixando o titular
 * sem Lugar no regresso.
 *
 * <p><b>A licença pode abrir vaga</b>, conforme o subtipo (DL n.º 3/2010) — ver
 * {@link LicencaService}. Nesse caso a resposta traz a afectação encerrada.
 */
@Component
@RequiredArgsConstructor
public class AprovarLicencaMobilidadeCommandHandler
        implements CommandHandler<AprovarLicencaMobilidadeCommand, ResponseEntity<LicencaEfeitoResponseDTO>> {

    private final LicencaMobilidadeRepository licencaRepository;
    private final MobilidadeService mobilidadeService;
    private final LicencaEfeitoService licencaEfeitoService;

    @IgrpCommandHandler
    @Transactional
    public ResponseEntity<LicencaEfeitoResponseDTO> handle(AprovarLicencaMobilidadeCommand command) {
        var licenca = licencaRepository.findById(LicencaMobilidadeId.from(command.getLicencaId()))
                .orElseThrow(() -> IgrpResponseStatusException.notFound(
                        "Licença/mobilidade não encontrada: " + command.getLicencaId()));

        // Estado primeiro: um registo já decidido não chega sequer às validações do destino.
        if (!licenca.isPending())
            throw IgrpResponseStatusException.conflict(
                    "Apenas registos PENDING podem ser aprovados. Estado actual: " + licenca.getStatus());

        var subtipo = mobilidadeService.subtipoDe(licenca);
        mobilidadeService.validarParaAprovacao(licenca, subtipo);

        licenca.aprovar();
        licencaRepository.save(licenca);

        // Deferir nao e por em vigor: os efeitos pertencem ao PERIODO (art. 44.o n.o 1), nao ao
        // despacho (n.o 2). Se a licenca comeca hoje, aplicam-se ja — ninguem espera pela
        // meia-noite; se comeca mais tarde, e o job diario que os aplica na data certa.
        var efeito = licencaEfeitoService.aplicarEntradaSeDevida(licenca, java.time.LocalDate.now());
        if (efeito.afectacaoEncerradaId() != null)
            return ResponseEntity.ok(new LicencaEfeitoResponseDTO(
                    licenca.getId().getStringValor(), licenca.getStatus(), true,
                    efeito.afectacaoEncerradaId().toString(), null));

        return ResponseEntity.ok(new LicencaEfeitoResponseDTO(
                licenca.getId().getStringValor(), licenca.getStatus(), true, null, null));
    }
}
