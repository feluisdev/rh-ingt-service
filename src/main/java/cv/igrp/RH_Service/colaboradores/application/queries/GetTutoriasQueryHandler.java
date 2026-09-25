package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.RH_Service.colaboradores.application.dto.PeriodoProvaDTO;
import cv.igrp.RH_Service.colaboradores.application.services.ProvimentoService;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.shared.domain.service.CurrentEmployeeResolver;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;

/** Os estágios em curso de que o utilizador é tutor. */
@Component
@RequiredArgsConstructor
public class GetTutoriasQueryHandler implements QueryHandler<GetTutoriasQuery, ResponseEntity<List<PeriodoProvaDTO>>> {
    private final ProvimentoService service;
    private final FuncionarioRepository funcionarioRepository;
    private final CurrentEmployeeResolver currentEmployeeResolver;

    @IgrpQueryHandler
    public ResponseEntity<List<PeriodoProvaDTO>> handle(GetTutoriasQuery q) {
        return ResponseEntity.ok(service.tutorias(currentEmployeeResolver.resolve()).stream()
                .map(p -> EntradaServicoDtos.dto(p, funcionarioRepository, List.of())).toList());
    }
}
