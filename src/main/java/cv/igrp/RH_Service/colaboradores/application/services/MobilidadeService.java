package cv.igrp.RH_Service.colaboradores.application.services;

import cv.igrp.RH_Service.colaboradores.domain.models.LicencaMobilidade;
import cv.igrp.RH_Service.colaboradores.domain.models.SubtipoLicencaMobilidade;
import cv.igrp.RH_Service.colaboradores.domain.repository.SubtipoLicencaMobilidadeRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.SubtipoLicencaMobilidadeId;
import cv.igrp.RH_Service.estrutura.domain.repository.OrganizationalUnitRepository;
import cv.igrp.RH_Service.estrutura.domain.valueobject.OrganizationalUnitId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

/**
 * Regras da mobilidade <b>transitória</b> (Lei n.º 20/X/2023, art. 132.º a 137.º).
 *
 * <p>A mobilidade transitória <b>não ocupa lugar do quadro no destino</b> (art. 135.º n.º 7) e o
 * tempo conta no lugar de origem (art. 137.º): por isso <b>não toca na afectação</b> — a pessoa
 * continua titular do seu Lugar e o regresso está sempre garantido. O que este processo regista é
 * <i>onde a pessoa exerce funções</i> e até quando.
 *
 * <p>O destino é <b>interno</b> (unidade nossa) ou <b>externo</b> (entidade de fora, art. 133.º:
 * autarquias, sector empresarial, privado, organismos internacionais). O comportamento é o mesmo.
 *
 * <p>A mudança <b>definitiva</b> de Lugar não passa por aqui: é a transferência
 * ({@link AssignmentService#transferir}).
 */
@Service
@RequiredArgsConstructor
public class MobilidadeService {

    private final SubtipoLicencaMobilidadeRepository subtipoRepository;
    private final OrganizationalUnitRepository unidadeRepository;

    public SubtipoLicencaMobilidade subtipoDe(LicencaMobilidade licenca) {
        return subtipoRepository.findById(
                        SubtipoLicencaMobilidadeId.from(licenca.getSubtipoId().getValor()))
                .orElseThrow(() -> IgrpResponseStatusException.notFound(
                        "Subtipo de licença/mobilidade não encontrado: " + licenca.getSubtipoId().getStringValor()));
    }

    /**
     * Valida um processo de mobilidade antes de entrar em vigor: destino indicado (interno ou
     * externo) e duração dentro do limite parametrizado no subtipo.
     * Para uma licença (não mobilidade) não há nada a validar aqui.
     */
    public void validarParaAprovacao(LicencaMobilidade licenca, SubtipoLicencaMobilidade subtipo) {
        if (!subtipo.isMobilidade()) return;

        boolean temDestinoExterno = licenca.getEntidadeDestino() != null
                && !licenca.getEntidadeDestino().isBlank();

        if (!licenca.isDestinoInterno() && !temDestinoExterno)
            throw IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY,
                    "A mobilidade exige um destino: uma unidade orgânica (destinationUnitId) para "
                            + "mobilidade interna, ou a entidade de destino (entidadeDestino) para mobilidade externa.");

        if (licenca.isDestinoInterno()
                && unidadeRepository.findById(OrganizationalUnitId.from(licenca.getDestinationUnitId())).isEmpty())
            throw IgrpResponseStatusException.notFound(
                    "Unidade orgânica de destino não encontrada: " + licenca.getDestinationUnitId());

        validarDuracao(licenca, subtipo);
    }

    /**
     * Duração máxima da mobilidade transitória — um ano, em regra (art. 132.º n.º 5), mas o valor
     * vive no catálogo ({@code max_duration_days}) e não no código.
     */
    public void validarDuracao(LicencaMobilidade licenca, SubtipoLicencaMobilidade subtipo) {
        Integer maxDias = subtipo.getMaxDurationDays();
        if (maxDias == null) return;

        Long duracao = licenca.duracaoEmDias();
        if (duracao == null)
            throw IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY,
                    "O subtipo '" + subtipo.getNome() + "' tem duração máxima de " + maxDias
                            + " dias — a data de fim é obrigatória.");

        if (duracao > maxDias)
            throw IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY,
                    "A duração pedida (" + duracao + " dias) excede o máximo do subtipo '"
                            + subtipo.getNome() + "' (" + maxDias + " dias).");
    }
}
