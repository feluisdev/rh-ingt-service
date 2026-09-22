package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.application.services.LicencaEfeitoService;
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
 * <b>Regresso antecipado ao serviço</b> — art. 46.º n.º 4 do DL n.º 3/2010: «O funcionário a quem
 * tenha sido concedida licença pode requerer o regresso antecipado ao serviço.»
 *
 * <p>O que isto muda é a <b>data de fim</b>, e mais nada. O despacho continua a ser o que foi, e
 * por isso o registo fica deferido ({@code APPROVED}): passa a {@code TERMINADA} sozinho, porque
 * o estado do período deriva das datas. Até à V48 este caminho escrevia um estado {@code CLOSED}
 * e fixava o fim em <i>hoje</i> sem olhar ao início — o que, numa licença que ainda não tinha
 * começado, gravava um fim anterior ao início e falseava as contagens de dias em que assentam o
 * desconto na antiguidade e as férias proporcionais (art. 47.º n.os 1 a 3).
 *
 * <p><b>Só quem partiu regressa.</b> Uma licença por iniciar não se encerra: desiste-se dela pelo
 * cancelamento, porque pelo art. 44.º n.º 1 não chegou a haver ausência. Uma já terminada não
 * precisa de nada — terminou no dia em que terminou, sem ninguém carregar em nada (art. 46.º
 * n.º 3). Nos dois casos o agregado recusa com 409 e diz qual é o caminho.
 *
 * <p>O <b>regresso não precisa de reabrir nada</b>: como a mobilidade transitória nunca tirou o
 * Lugar ao titular, e a licença que abre vaga devolve o funcionário à disponibilidade (art. 122.º),
 * não há afectação de origem a restaurar.
 */
@Component
@RequiredArgsConstructor
public class EncerrarLicencaMobilidadeCommandHandler
        implements CommandHandler<EncerrarLicencaMobilidadeCommand, ResponseEntity<LicencaEfeitoResponseDTO>> {

    private final LicencaMobilidadeRepository licencaRepository;
    private final LicencaEfeitoService licencaEfeitoService;

    @IgrpCommandHandler
    @Transactional
    public ResponseEntity<LicencaEfeitoResponseDTO> handle(EncerrarLicencaMobilidadeCommand command) {
        var licenca = licencaRepository.findById(LicencaMobilidadeId.from(command.getLicencaId()))
                .orElseThrow(() -> IgrpResponseStatusException.notFound(
                        "Licença/mobilidade não encontrada: " + command.getLicencaId()));

        LocalDate hoje = LocalDate.now();

        // O regresso é hoje: é isso que "antecipado" quer dizer. A data não vem do pedido porque
        // um regresso futuro não é um regresso — é uma prorrogação ao contrário, e o registo do
        // que ainda não aconteceu não pertence a este caminho.
        licenca.registarRegressoAntecipado(hoje, hoje);
        licencaRepository.save(licenca);

        // O funcionário está de volta agora: os efeitos aplicam-se já, sem esperar pelo job da
        // noite, que deixaria o substituto no Lugar mais um dia. Pelo mesmo caminho que o job
        // usaria — e marcados como aplicados, para ele não lhes tocar outra vez.
        var efeito = licencaEfeitoService.aplicarRegresso(licenca);

        return ResponseEntity.ok(new LicencaEfeitoResponseDTO(
                licenca.getId().getStringValor(), licenca.getStatus(), true, null,
                efeito.estadoAtribuidoId() != null ? efeito.estadoAtribuidoId().toString() : null));
    }
}
