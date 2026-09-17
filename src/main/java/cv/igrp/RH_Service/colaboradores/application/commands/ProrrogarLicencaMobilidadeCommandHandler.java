package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.application.dto.ProrrogacaoMobilidadeResponseDTO;
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

import java.time.LocalDate;

/**
 * Prorrogar a licença ou a mobilidade em vigor. A mobilidade transitória é, em regra, prorrogável
 * uma única vez por igual período (Lei n.º 20/X/2023, art. 132.º n.º 5) — mas tanto o número de
 * prorrogações como a duração de cada período vêm parametrizados no subtipo, não do código.
 *
 * <p>Não toca na afectação, como o resto do processo.
 */
@Component
@RequiredArgsConstructor
public class ProrrogarLicencaMobilidadeCommandHandler
        implements CommandHandler<ProrrogarLicencaMobilidadeCommand, ResponseEntity<ProrrogacaoMobilidadeResponseDTO>> {

    private final LicencaMobilidadeRepository licencaRepository;
    private final MobilidadeService mobilidadeService;

    @IgrpCommandHandler
    @Transactional
    public ResponseEntity<ProrrogacaoMobilidadeResponseDTO> handle(ProrrogarLicencaMobilidadeCommand command) {
        var req = command.getRequest();
        if (req == null || req.getNovaDataFim() == null)
            throw IgrpResponseStatusException.badRequest("A nova data de fim (novaDataFim) é obrigatória.");

        var licenca = licencaRepository.findById(LicencaMobilidadeId.from(command.getLicencaId()))
                .orElseThrow(() -> IgrpResponseStatusException.notFound(
                        "Licença/mobilidade não encontrada: " + command.getLicencaId()));

        var subtipo = mobilidadeService.subtipoDe(licenca);
        LocalDate dataFimAnterior = licenca.getDataFim();

        mobilidadeService.validarPeriodoDeProrrogacao(licenca, subtipo, req.getNovaDataFim());
        licenca.prorrogar(req.getNovaDataFim(), subtipo.getMaxExtensions());
        licencaRepository.save(licenca);

        return ResponseEntity.ok(new ProrrogacaoMobilidadeResponseDTO(
                licenca.getId().getStringValor(),
                licenca.getDataInicio(),
                dataFimAnterior,
                licenca.getDataFim(),
                licenca.extensoes(),
                subtipo.getMaxExtensions()));
    }
}
