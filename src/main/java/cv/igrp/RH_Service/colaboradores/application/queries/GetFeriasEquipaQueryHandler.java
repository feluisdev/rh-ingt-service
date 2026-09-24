package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.RH_Service.colaboradores.application.dto.FeriasEquipaLinhaDTO;
import cv.igrp.RH_Service.colaboradores.application.services.ChefiaService;
import cv.igrp.RH_Service.colaboradores.application.services.MapaFeriasService;
import cv.igrp.RH_Service.colaboradores.domain.models.Funcionario;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.shared.domain.service.CurrentEmployeeResolver;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

/** A chefia directa vê a preferência e a marcação de cada pessoa da equipa, para planear o serviço. */
@Component
@RequiredArgsConstructor
public class GetFeriasEquipaQueryHandler implements QueryHandler<GetFeriasEquipaQuery, ResponseEntity<List<FeriasEquipaLinhaDTO>>> {

    private final CurrentEmployeeResolver currentEmployeeResolver;
    private final ChefiaService chefiaService;
    private final FuncionarioRepository funcionarioRepository;
    private final MapaFeriasService mapaFeriasService;

    @IgrpQueryHandler
    public ResponseEntity<List<FeriasEquipaLinhaDTO>> handle(GetFeriasEquipaQuery query) {
        List<FuncionarioId> equipa = chefiaService.equipaDirecta(currentEmployeeResolver.resolve());
        List<Funcionario> pessoas = funcionarioRepository.findAllByIds(equipa.stream().map(FuncionarioId::getValor).toList());
        return ResponseEntity.ok(pessoas.stream().map(f -> {
            var l = new FeriasEquipaLinhaDTO(f.getId().getStringValor(), f.getNumeroFuncionario(), f.getNomeCompleto(),
                    List.of(), null, null, false, List.of(), 0);
            mapaFeriasService.consultar(f.getId(), query.getAno()).ifPresent(fa -> {
                l.setPreferencia(GetFeriasDoAnoQueryHandler.dtos(fa.getPreferencia()));
                l.setPreferenciaIndicadaEm(fa.getPreferenciaIndicadaEm());
                l.setPreferenciaIndicadaPor(fa.getPreferenciaIndicadaPor() != null ? fa.getPreferenciaIndicadaPor().name() : null);
                l.setPreferenciaForaDePrazo(fa.isPreferenciaForaDePrazo());
                l.setMarcacao(GetFeriasDoAnoQueryHandler.dtos(fa.getMarcacao()));
                l.setTotalMarcado(fa.totalMarcado());
            });
            return l;
        }).sorted(Comparator.comparing(l -> l.getNome() == null ? "" : l.getNome())).toList());
    }
}
