package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.application.services.LicencaService;
import cv.igrp.RH_Service.colaboradores.application.services.SubstituicaoService;
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
import cv.igrp.RH_Service.colaboradores.application.dto.LicencaEfeitoResponseDTO;

/**
 * Encerrar a licença ou a mobilidade (fim do período, ou regresso antecipado).
 *
 * <p>O <b>regresso deixou de precisar de código</b>: como a mobilidade transitória nunca tirou o
 * Lugar ao titular, encerrar é só fechar o registo. Antes, o regresso reabria a afectação de
 * origem — e falhava em silêncio se o Lugar entretanto tivesse sido ocupado por outra pessoa.
 */
@Component
@RequiredArgsConstructor
public class EncerrarLicencaMobilidadeCommandHandler
        implements CommandHandler<EncerrarLicencaMobilidadeCommand, ResponseEntity<LicencaEfeitoResponseDTO>> {

    private final LicencaMobilidadeRepository licencaRepository;
    private final MobilidadeService mobilidadeService;
    private final LicencaService licencaService;
    private final SubstituicaoService substituicaoService;

    @IgrpCommandHandler
    @Transactional
    public ResponseEntity<LicencaEfeitoResponseDTO> handle(EncerrarLicencaMobilidadeCommand command) {
        var licenca = licencaRepository.findById(LicencaMobilidadeId.from(command.getLicencaId()))
                .orElseThrow(() -> IgrpResponseStatusException.notFound(
                        "Licença/mobilidade não encontrada: " + command.getLicencaId()));

        // Encerramento no fim do período aprovado; se for antes, vale a data de hoje (regresso antecipado).
        LocalDate hoje = LocalDate.now();
        LocalDate dataFim = licenca.getDataFim() != null && licenca.getDataFim().isBefore(hoje)
                ? licenca.getDataFim() : hoje;

        licenca.encerrar(dataFim);
        licencaRepository.save(licenca);

        // Quem perdeu o Lugar não regressa a ele: fica na disponibilidade, a aguardar
        // vaga (art. 122.º). Quem o manteve não precisa de nada. Se o subtipo já não
        // existir no catálogo, encerra à mesma — fechar um registo não depende de
        // configuração.
        // Acabada a licença, acabou o impedimento: quem estava a substituir o titular sai
        // (art. 77.º n.º 2). Nada a fazer quando a licença abriu vaga -- aí o titular já não
        // tem afectação, logo não há substituição ligada a ela.
        substituicaoService.encerrarPorRegressoDoTitular(licenca.getFuncionarioId(), dataFim);

        var estadoAtribuido = mobilidadeService.subtipoSeExistir(licenca)
                .flatMap(subtipo -> licencaService.aplicarRegresso(licenca, subtipo, dataFim));
        if (estadoAtribuido.isPresent())
            return ResponseEntity.ok(new LicencaEfeitoResponseDTO(
                    licenca.getId().getStringValor(), licenca.getStatus(), true,
                    null, estadoAtribuido.get().toString()));

        return ResponseEntity.ok(new LicencaEfeitoResponseDTO(
                licenca.getId().getStringValor(), licenca.getStatus(), true, null, null));
    }
}
