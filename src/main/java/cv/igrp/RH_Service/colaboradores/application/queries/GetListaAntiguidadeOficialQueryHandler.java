package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.RH_Service.colaboradores.application.dto.ListaAntiguidadeOficialDTO;
import cv.igrp.RH_Service.colaboradores.application.services.CicloListaAntiguidadeService;
import cv.igrp.RH_Service.colaboradores.domain.models.Funcionario;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.ListaAntiguidadeOficialId;
import cv.igrp.RH_Service.shared.domain.service.Entrada;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GetListaAntiguidadeOficialQueryHandler
        implements QueryHandler<GetListaAntiguidadeOficialQuery, ResponseEntity<ListaAntiguidadeOficialDTO>> {

    private final CicloListaAntiguidadeService service;
    private final FuncionarioRepository funcionarioRepository;

    @IgrpQueryHandler
    public ResponseEntity<ListaAntiguidadeOficialDTO> handle(GetListaAntiguidadeOficialQuery q) {
        var id = ListaAntiguidadeOficialId.from(Entrada.uuid(q.getListaId(), "a lista de antiguidade"));
        return ResponseEntity.ok(ListasAntiguidadeDtos.dto(service.lista(id), service.reclamacoes(id), true, null,
                f -> funcionarioRepository.findById(f).map(Funcionario::getNomeCompleto).orElse(null), List.of()));
    }
}
