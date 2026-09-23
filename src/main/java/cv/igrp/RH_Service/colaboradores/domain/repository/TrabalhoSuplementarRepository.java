package cv.igrp.RH_Service.colaboradores.domain.repository;

import cv.igrp.RH_Service.colaboradores.domain.models.TrabalhoSuplementar;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.TrabalhoSuplementarId;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface TrabalhoSuplementarRepository {
    TrabalhoSuplementar save(TrabalhoSuplementar trabalho);
    Optional<TrabalhoSuplementar> findById(TrabalhoSuplementarId id);
    /** Todos os de [{@code de}, {@code ate}], em qualquer estado, por data e hora de início. */
    List<TrabalhoSuplementar> findByFuncionarioEntre(FuncionarioId funcionarioId, LocalDate de, LocalDate ate);
    /** Os pedidos do próprio por decidir destes colaboradores. */
    List<TrabalhoSuplementar> findPedidosDe(Collection<FuncionarioId> funcionarios);
}
