package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.RH_Service.colaboradores.application.dto.EntradaServicoDTO;
import cv.igrp.RH_Service.colaboradores.application.services.ProvimentoService;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.shared.domain.service.Entrada;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class GetEntradaServicoQueryHandler implements QueryHandler<GetEntradaServicoQuery, ResponseEntity<EntradaServicoDTO>> {

    private final ProvimentoService service;
    private final FuncionarioRepository funcionarioRepository;

    @IgrpQueryHandler
    public ResponseEntity<EntradaServicoDTO> handle(GetEntradaServicoQuery q) {
        var f = FuncionarioId.from(Entrada.uuid(q.getFuncionarioId(), "o colaborador"));
        return ResponseEntity.ok(new EntradaServicoDTO(f.getStringValor(),
                new ArrayList<>(service.provimentos(f).stream().map(p -> EntradaServicoDtos.dto(p, List.of())).toList()),
                new ArrayList<>(service.periodos(f).stream().map(p -> EntradaServicoDtos.dto(p, funcionarioRepository, List.of())).toList())));
    }
}
