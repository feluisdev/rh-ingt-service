package cv.igrp.RH_Service.colaboradores.domain.repository;

import cv.igrp.RH_Service.colaboradores.domain.models.FeriasDoAno;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FeriasDoAnoRepository {

    FeriasDoAno save(FeriasDoAno ferias);

    Optional<FeriasDoAno> findByFuncionarioIdAndAno(FuncionarioId funcionarioId, int ano);

    /** As marcações do ano: o conteúdo do mapa (art. 6.º n.º 1). */
    List<FeriasDoAno> findAllComMarcacaoByAno(int ano);

    /**
     * Colaboradores activos sem marcação no ano — os que o mapa ainda não cobre. Sem acordo, é a
     * estes que o dirigente fixa as férias (art. 5.º n.º 5).
     */
    List<UUID> findFuncionariosActivosSemMarcacao(int ano);

    /** Quem indicou preferência para o ano (art. 5.º n.º 4). */
    List<UUID> findFuncionariosComPreferencia(int ano);
}
