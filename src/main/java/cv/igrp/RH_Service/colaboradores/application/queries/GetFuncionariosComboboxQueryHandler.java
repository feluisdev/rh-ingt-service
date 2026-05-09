package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.RH_Service.colaboradores.domain.filter.FuncionarioFilter;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.shared.application.dto.ComboboxItemDTO;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GetFuncionariosComboboxQueryHandler
        implements QueryHandler<GetFuncionariosComboboxQuery, ResponseEntity<List<ComboboxItemDTO>>> {

    private final FuncionarioRepository funcionarioRepository;

    @IgrpQueryHandler
    public ResponseEntity<List<ComboboxItemDTO>> handle(GetFuncionariosComboboxQuery query) {
        var filter = new FuncionarioFilter();
        filter.setIsActive(true);
        filter.setPage(0);
        filter.setSize(500);
        if (query.getQ() != null && !query.getQ().isBlank()) {
            filter.setNome(query.getQ().trim());
        }
        var items = funcionarioRepository.findAll(filter).stream()
                .map(f -> new ComboboxItemDTO(
                        f.getId().getStringValor(),
                        f.getNomeCompleto() + " — " + f.getNumeroFuncionario()))
                .toList();
        return ResponseEntity.ok(items);
    }
}
