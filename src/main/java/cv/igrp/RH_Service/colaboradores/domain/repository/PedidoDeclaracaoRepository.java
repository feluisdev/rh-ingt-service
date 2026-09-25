package cv.igrp.RH_Service.colaboradores.domain.repository;

import cv.igrp.RH_Service.colaboradores.domain.models.PedidoDeclaracao;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.PedidoDeclaracaoId;

import java.util.List;
import java.util.Optional;

public interface PedidoDeclaracaoRepository {
    PedidoDeclaracao save(PedidoDeclaracao pedido);
    Optional<PedidoDeclaracao> findById(PedidoDeclaracaoId id);
    /** Do mais recente para o mais antigo. */
    List<PedidoDeclaracao> findByFuncionario(FuncionarioId funcionarioId);
    /** Os pedidos por emitir de todos, dos mais antigos para os mais recentes (a caixa do RH). */
    List<PedidoDeclaracao> findPorEmitir();
}
