package cv.igrp.RH_Service.colaboradores.application.commands;

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
        implements CommandHandler<EncerrarLicencaMobilidadeCommand, ResponseEntity<Map<String, ?>>> {

    private final LicencaMobilidadeRepository licencaRepository;

    @IgrpCommandHandler
    @Transactional
    public ResponseEntity<Map<String, ?>> handle(EncerrarLicencaMobilidadeCommand command) {
        var licenca = licencaRepository.findById(LicencaMobilidadeId.from(command.getLicencaId()))
                .orElseThrow(() -> IgrpResponseStatusException.notFound(
                        "Licença/mobilidade não encontrada: " + command.getLicencaId()));

        // Encerramento no fim do período aprovado; se for antes, vale a data de hoje (regresso antecipado).
        LocalDate hoje = LocalDate.now();
        LocalDate dataFim = licenca.getDataFim() != null && licenca.getDataFim().isBefore(hoje)
                ? licenca.getDataFim() : hoje;

        licenca.encerrar(dataFim);
        licencaRepository.save(licenca);

        return ResponseEntity.ok(Map.of("message", "Encerrado com sucesso"));
    }
}
