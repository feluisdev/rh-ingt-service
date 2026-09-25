package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.RH_Service.colaboradores.application.dto.PedidoDeclaracaoDTO;
import cv.igrp.RH_Service.colaboradores.application.services.DeclaracoesService;
import cv.igrp.RH_Service.colaboradores.domain.models.Funcionario;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.shared.domain.service.CurrentEmployeeResolver;
import cv.igrp.RH_Service.shared.domain.service.Entrada;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class GetDeclaracoesQueryHandler implements QueryHandler<GetDeclaracoesQuery, ResponseEntity<List<PedidoDeclaracaoDTO>>> {

    private final DeclaracoesService declaracoesService;
    private final CurrentEmployeeResolver currentEmployeeResolver;
    private final FuncionarioRepository funcionarioRepository;

    @IgrpQueryHandler
    public ResponseEntity<List<PedidoDeclaracaoDTO>> handle(GetDeclaracoesQuery q) {
        List<DeclaracoesService.Pedido> pedidos;
        if (q.isPorEmitir()) pedidos = declaracoesService.porEmitir();
        else {
            FuncionarioId f = q.getFuncionarioId() == null ? currentEmployeeResolver.resolve()
                    : FuncionarioId.from(Entrada.uuid(q.getFuncionarioId(), "o colaborador"));
            pedidos = declaracoesService.doFuncionario(f);
        }
        var cache = new HashMap<FuncionarioId, Optional<Funcionario>>();
        return ResponseEntity.ok(pedidos.stream().map(p -> DocumentosDtos.dto(p,
                cache.computeIfAbsent(p.pedido().getFuncionarioId(), funcionarioRepository::findById).orElse(null))).toList());
    }
}
