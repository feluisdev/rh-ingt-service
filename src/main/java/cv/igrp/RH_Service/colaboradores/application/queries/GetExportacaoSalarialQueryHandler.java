package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.RH_Service.colaboradores.application.dto.ExportacaoSalarialDTO;
import cv.igrp.RH_Service.colaboradores.application.services.FechoMensalService;
import cv.igrp.RH_Service.colaboradores.domain.repository.FactoRhRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
@RequiredArgsConstructor
public class GetExportacaoSalarialQueryHandler implements QueryHandler<GetExportacaoSalarialQuery, ResponseEntity<ExportacaoSalarialDTO>> {

    private final FechoMensalService service;
    private final FactoRhRepository factoRepository;
    private final FuncionarioRepository funcionarioRepository;

    @IgrpQueryHandler
    public ResponseEntity<ExportacaoSalarialDTO> handle(GetExportacaoSalarialQuery q) {
        var mes = FechoMensalDtos.mes(q.getMes());
        var e = service.exportar(mes);
        var factos = GetFactosSalariaisQueryHandler.dtos(factoRepository.findByMesCompetencia(mes), funcionarioRepository);
        return ResponseEntity.ok(new ExportacaoSalarialDTO(FechoMensalService.VERSAO, mes.toString(), e.estado(), e.fechadoEm(), e.provisoria(),
                factos.size(), new ArrayList<>(factos), new ArrayList<>(e.relacao())));
    }
}
