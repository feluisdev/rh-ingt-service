package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.RH_Service.colaboradores.application.dto.JuntaMedicaDTO;
import cv.igrp.RH_Service.colaboradores.application.services.SaudeTrabalhoService;
import cv.igrp.RH_Service.colaboradores.domain.models.JuntaMedica;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.shared.domain.service.Entrada;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GetJuntasMedicasQueryHandler implements QueryHandler<GetJuntasMedicasQuery, ResponseEntity<List<JuntaMedicaDTO>>> {

    private final SaudeTrabalhoService service;
    private final SaudeTrabalhoDtos dtos;

    @IgrpQueryHandler
    public ResponseEntity<List<JuntaMedicaDTO>> handle(GetJuntasMedicasQuery q) {
        var lista = q.getFuncionarioId() != null ? service.juntasDe(FuncionarioId.from(Entrada.uuid(q.getFuncionarioId(), "o colaborador")))
                : service.juntas(ChecklistDtos.valor(JuntaMedica.Estado.class, q.getEstado(), "Estado"));
        return ResponseEntity.ok(lista.stream().map(j -> dtos.dto(j, null)).toList());
    }
}
