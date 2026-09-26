package cv.igrp.RH_Service.colaboradores.domain.repository;

import cv.igrp.RH_Service.colaboradores.domain.models.FechoMensal;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/** Os fechos dos meses de processamento. */
public interface FechoMensalRepository {
    FechoMensal save(FechoMensal fecho);
    Optional<FechoMensal> findByMes(YearMonth mes);
    /** Dos mais recentes para os mais antigos. */
    List<FechoMensal> findAll();
    /** Os meses fechados (não reabertos) a partir deste. */
    Set<YearMonth> mesesFechadosDesde(YearMonth mes);
}
