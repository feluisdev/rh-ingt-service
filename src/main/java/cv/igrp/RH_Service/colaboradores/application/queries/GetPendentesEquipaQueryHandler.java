package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.RH_Service.colaboradores.application.dto.MarcacaoPendenteDTO;
import cv.igrp.RH_Service.colaboradores.application.services.AssiduidadeService;
import cv.igrp.RH_Service.colaboradores.domain.models.Funcionario;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.shared.domain.service.CurrentEmployeeResolver;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/** A caixa da chefia: os pedidos de correcção por decidir da sua equipa directa. */
@Component
@RequiredArgsConstructor
public class GetPendentesEquipaQueryHandler implements QueryHandler<GetPendentesEquipaQuery, ResponseEntity<List<MarcacaoPendenteDTO>>> {

    private final CurrentEmployeeResolver currentEmployeeResolver;
    private final AssiduidadeService assiduidadeService;
    private final FuncionarioRepository funcionarioRepository;

    @IgrpQueryHandler
    public ResponseEntity<List<MarcacaoPendenteDTO>> handle(GetPendentesEquipaQuery query) {
        var pendentes = assiduidadeService.pendentesDaEquipa(currentEmployeeResolver.resolve());
        Map<UUID, Funcionario> pessoas = funcionarioRepository.findAllByIds(
                        pendentes.stream().map(m -> m.getFuncionarioId().getValor()).distinct().toList()).stream()
                .collect(Collectors.toMap(f -> f.getId().getValor(), Function.identity(), (a, b) -> a));
        return ResponseEntity.ok(pendentes.stream().map(m -> {
            Funcionario f = pessoas.get(m.getFuncionarioId().getValor());
            return new MarcacaoPendenteDTO(m.getId().getStringValor(), m.getFuncionarioId().getStringValor(),
                    f != null ? f.getNumeroFuncionario() : null, f != null ? f.getNomeCompleto() : null,
                    m.getMomento(), m.getSentido().name(), m.getMotivo());
        }).toList());
    }
}
