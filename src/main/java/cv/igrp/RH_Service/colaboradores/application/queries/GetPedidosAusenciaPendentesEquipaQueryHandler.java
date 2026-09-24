package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.RH_Service.colaboradores.application.dto.PedidoAusenciaPendenteDTO;
import cv.igrp.RH_Service.colaboradores.application.services.DecisaoPedidoAusenciaService;
import cv.igrp.RH_Service.colaboradores.domain.models.Funcionario;
import cv.igrp.RH_Service.colaboradores.domain.models.TipoAusencia;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.TipoAusenciaRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.TipoAusenciaId;
import cv.igrp.RH_Service.shared.domain.service.CurrentEmployeeResolver;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/** A caixa da chefia: os pedidos de ausência por decidir da sua equipa directa. */
@Component
@RequiredArgsConstructor
public class GetPedidosAusenciaPendentesEquipaQueryHandler
        implements QueryHandler<GetPedidosAusenciaPendentesEquipaQuery, ResponseEntity<List<PedidoAusenciaPendenteDTO>>> {

    private final CurrentEmployeeResolver currentEmployeeResolver;
    private final DecisaoPedidoAusenciaService decisaoService;
    private final FuncionarioRepository funcionarioRepository;
    private final TipoAusenciaRepository tipoAusenciaRepository;

    @IgrpQueryHandler
    public ResponseEntity<List<PedidoAusenciaPendenteDTO>> handle(GetPedidosAusenciaPendentesEquipaQuery query) {
        var pendentes = decisaoService.pendentesDaEquipa(currentEmployeeResolver.resolve());
        Map<UUID, Funcionario> pessoas = funcionarioRepository.findAllByIds(
                        pendentes.stream().map(p -> p.getFuncionarioId().getValor()).distinct().toList()).stream()
                .collect(Collectors.toMap(f -> f.getId().getValor(), Function.identity(), (a, b) -> a));
        Map<TipoAusenciaId, Optional<TipoAusencia>> tipos = new HashMap<>();
        return ResponseEntity.ok(pendentes.stream().map(p -> {
            Funcionario f = pessoas.get(p.getFuncionarioId().getValor());
            TipoAusencia t = tipos.computeIfAbsent(p.getTipoAusenciaId(), tipoAusenciaRepository::findById).orElse(null);
            return new PedidoAusenciaPendenteDTO(p.getId().getStringValor(), p.getFuncionarioId().getStringValor(),
                    f != null ? f.getNumeroFuncionario() : null, f != null ? f.getNomeCompleto() : null,
                    t != null ? t.getCodigo() : null, t != null ? t.getNome() : null,
                    p.getDataInicio(), p.getDataFim(), p.getNumeroDias(),
                    p.getHoraInicio() != null ? p.getHoraInicio().toString() : null,
                    p.getHoraFim() != null ? p.getHoraFim().toString() : null,
                    p.minutosPorDia(), p.getMotivo());
        }).toList());
    }
}
