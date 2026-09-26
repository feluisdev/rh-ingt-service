package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.RH_Service.colaboradores.application.dto.ComissaoServicoDTO;
import cv.igrp.RH_Service.colaboradores.application.services.ComissaoServicoService;
import cv.igrp.RH_Service.colaboradores.application.services.MobilidadeService;
import cv.igrp.RH_Service.colaboradores.domain.models.LicencaMobilidade;
import cv.igrp.RH_Service.colaboradores.domain.models.SubtipoLicencaMobilidade;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/** Da comissão de serviço (registo de mobilidade) para o DTO. */
@Component
@RequiredArgsConstructor
public class ComissaoServicoDtos {

    private final ComissaoServicoService service;
    private final MobilidadeService mobilidadeService;

    public ComissaoServicoDTO dto(LicencaMobilidade l) {
        LocalDate hoje = LocalDate.now();
        return new ComissaoServicoDTO(l.getId().getStringValor(), l.getFuncionarioId().getStringValor(), service.nome(l.getFuncionarioId()),
                mobilidadeService.subtipoSeExistir(l).map(SubtipoLicencaMobilidade::getNome).orElse(null), l.getDataInicio(), l.getDataFim(),
                l.extensoes(), l.estadoEm(hoje).name(), texto(l.getDestinationPositionId()), texto(l.getDestinationUnitId()),
                l.getEntidadeDestino(), l.getDespachoNumero(), l.getObservacoes(),
                l.getDataFim() != null ? (int) ChronoUnit.DAYS.between(hoje, l.getDataFim()) : null);
    }

    private static String texto(UUID id) {
        return id != null ? id.toString() : null;
    }
}
