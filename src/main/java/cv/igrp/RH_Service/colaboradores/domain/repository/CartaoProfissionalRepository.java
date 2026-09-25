package cv.igrp.RH_Service.colaboradores.domain.repository;

import cv.igrp.RH_Service.colaboradores.domain.models.CartaoProfissional;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.CartaoProfissionalId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;

import java.util.List;
import java.util.Optional;

public interface CartaoProfissionalRepository {
    CartaoProfissional save(CartaoProfissional cartao);
    Optional<CartaoProfissional> findById(CartaoProfissionalId id);
    /** Do mais recente para o mais antigo. */
    List<CartaoProfissional> findByFuncionario(FuncionarioId funcionarioId);
}
