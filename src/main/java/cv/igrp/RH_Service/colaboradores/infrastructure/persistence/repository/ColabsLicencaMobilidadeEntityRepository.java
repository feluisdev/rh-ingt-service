package cv.igrp.RH_Service.colaboradores.infrastructure.persistence.repository;

import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.LicencaMobilidadeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

import java.util.List;
import java.util.UUID;

public interface ColabsLicencaMobilidadeEntityRepository extends JpaRepository<LicencaMobilidadeEntity, UUID> {

    List<LicencaMobilidadeEntity> findAllByFuncionario_Id(UUID funcionarioId);

    List<LicencaMobilidadeEntity> findAllByFuncionario_IdAndIsActiveTrue(UUID funcionarioId);

    @Query("""
            SELECT l FROM ColabsLicencaMobilidadeEntity l
             WHERE l.funcionario.id = :funcionarioId
               AND l.status = 'APPROVED'
               AND l.dataInicio <= :data
               AND (l.dataFim IS NULL OR l.dataFim >= :data)
             ORDER BY l.dataInicio DESC
            """)
    List<LicencaMobilidadeEntity> findActiveAt(@Param("funcionarioId") UUID funcionarioId,
                                               @Param("data") LocalDate data);

    /**
     * Deferidas que ja comecaram e cujos efeitos no Lugar continuam por aplicar. E a pergunta que
     * o job diario faz, e e feita ao estado actual — nao a um intervalo desde a ultima execucao.
     * Por isso um dia falhado nao perde nada: o dia seguinte apanha o atraso.
     */
    @Query("""
            SELECT l FROM ColabsLicencaMobilidadeEntity l
             WHERE l.status = 'APPROVED'
               AND l.dataInicio <= :data
               AND l.efeitoEntradaAplicadoEm IS NULL
             ORDER BY l.dataInicio
            """)
    List<LicencaMobilidadeEntity> findEntradaPorAplicar(@Param("data") LocalDate data);

    /**
     * Deferidas cujo periodo ja terminou e cujo regresso continua por aplicar — o «caduca
     * automaticamente» do art. 46.o n.o 3. Um periodo sem fim nunca entra aqui: o regresso da
     * licenca de longa duracao depende de despacho (art. 53.o), nao do calendario.
     */
    @Query("""
            SELECT l FROM ColabsLicencaMobilidadeEntity l
             WHERE l.status = 'APPROVED'
               AND l.dataFim IS NOT NULL
               AND l.dataFim < :data
               AND l.efeitoRegressoAplicadoEm IS NULL
             ORDER BY l.dataFim
            """)
    List<LicencaMobilidadeEntity> findRegressoPorAplicar(@Param("data") LocalDate data);

    @Query("""
            SELECT l FROM ColabsLicencaMobilidadeEntity l
             WHERE l.status = 'APPROVED' AND l.isActive = true
               AND l.subtipo.returnEffect = 'REGRESSA_OU_CESSA'
               AND l.efeitoRegressoAplicadoEm IS NULL
             ORDER BY l.dataFim
            """)
    List<LicencaMobilidadeEntity> findComissoesEmCurso();

    @Query("""
            SELECT l FROM ColabsLicencaMobilidadeEntity l
             WHERE l.status = 'APPROVED' AND l.isActive = true
               AND l.subtipo.returnEffect = 'REGRESSA_OU_CESSA'
               AND l.efeitoRegressoAplicadoEm IS NULL
               AND l.dataFim <= :terminaAte
             ORDER BY l.dataFim
            """)
    List<LicencaMobilidadeEntity> findComissoesQueTerminamAte(@Param("terminaAte") LocalDate terminaAte);
}
