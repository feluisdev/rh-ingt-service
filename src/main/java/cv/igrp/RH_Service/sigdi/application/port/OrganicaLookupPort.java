package cv.igrp.RH_Service.sigdi.application.port;

import cv.igrp.RH_Service.sigdi.application.dto.OrganicaDTO;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface OrganicaLookupPort {

    Optional<OrganicaDTO> findById(UUID id);

    Map<UUID, OrganicaDTO> findAllByIds(Collection<UUID> ids);

    Optional<UUID> findResponsibleEmployeeId(UUID unitId);

    /**
     * Sobe um único nível na hierarquia orgânica, devolvendo o identificador da unidade-mãe.
     * <p>
     * Devolve {@link Optional#empty()} tanto quando {@code unitId} não corresponde a nenhuma
     * unidade orgânica como quando corresponde e é a unidade de topo (sem unidade-mãe). Os
     * dois casos são indistinguíveis de propósito à saída deste método -- o chamador trata
     * ambos da mesma forma, como "não há por onde subir".
     * <p>
     * É por este método, e não por um campo novo em {@link OrganicaDTO}, que o
     * {@code parentUnitId} atravessa para o módulo {@code sigdi}: o {@code OrganicaDTO} é
     * ficheiro gerado pelo iGRP Studio ("DO NOT MODIFY") e não pode ganhar campos por mão.
     * <p>
     * Consumidor previsto: {@code EvaluatorResolver} (Fase 119).
     */
    Optional<UUID> findParentUnitId(UUID unitId);

    /**
     * Todas as unidades orgânicas activas, sem paginação.
     */
    List<OrganicaDTO> findAllActiveUnits();
}
