package cv.igrp.RH_Service.sigdi.application.service;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.sigdi.application.constants.PaaLevel;
import cv.igrp.RH_Service.sigdi.application.constants.Purpose;
import cv.igrp.RH_Service.sigdi.application.dto.EligibleResponsiblesDTO;
import cv.igrp.RH_Service.sigdi.domain.tatical.models.PaaSubmissionPeriod;
import cv.igrp.RH_Service.sigdi.domain.tatical.repository.PaaSubmissionPeriodRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Fonte do AUT-06 (Fase 116, plano 04): traduz o triplo {@code (purpose, type, ano)} do
 * critério 1 da fase num {@link PaaSubmissionPeriod} e delega no
 * {@link EligibleResponsiblesResolver}. Esta fase não expõe HTTP nenhum -- decisão do
 * operador de 2026-08-26 -- porque o resultado é o levantamento nominal de quem trabalha em
 * que unidade orgânica, e a única guarda disponível hoje (o papel {@code "RH"} de
 * {@code PaaSecurityProperties}) nunca foi confirmado contra o realm IAM e vai ser apagado
 * pela Fase 115 ao migrar para a stack de permissões IGRP. O consumidor previsto é a
 * Fase 119, por injecção; a exposição decide-se no ecrã do {@code PRZ-05}, aí sim guardado
 * por permissão própria e verificada.
 */
@Component
@RequiredArgsConstructor
public class EligibleResponsiblesSource {

    private final PaaSubmissionPeriodRepository periodRepository;
    private final EligibleResponsiblesResolver resolver;

    public EligibleResponsiblesDTO findBy(Purpose purpose, PaaLevel type, Integer year) {
        if (purpose == null) {
            throw IgrpResponseStatusException.badRequest("purpose é obrigatório");
        }
        if (type == null) {
            throw IgrpResponseStatusException.badRequest("type é obrigatório");
        }
        if (year == null) {
            throw IgrpResponseStatusException.badRequest("year é obrigatório");
        }

        Optional<PaaSubmissionPeriod> period = periodRepository.findByTypeAndYearAndPurpose(type, year, purpose);
        return period
                .map(resolver::resolve)
                .orElseThrow(() -> IgrpResponseStatusException.notFound(
                        "Não existe período de submissão para " + purpose.getDescription()
                                + " / " + type.getDescription() + " / " + year));
    }
}
