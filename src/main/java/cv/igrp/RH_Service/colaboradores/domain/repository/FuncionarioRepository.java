package cv.igrp.RH_Service.colaboradores.domain.repository;

import cv.igrp.RH_Service.colaboradores.domain.filter.FuncionarioFilter;
import cv.igrp.RH_Service.colaboradores.domain.models.Funcionario;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;

import java.util.List;
import java.util.Optional;

public interface FuncionarioRepository {
    Funcionario save(Funcionario funcionario);
    Optional<Funcionario> findById(FuncionarioId id);
    List<Funcionario> findAll(FuncionarioFilter filter);
    long countAll(FuncionarioFilter filter);
    boolean existsByNif(String nif);
    boolean existsByNifAndIdNot(String nif, FuncionarioId id);
    boolean existsByBiNumero(String biNumero);
    boolean existsByBiNumeroAndIdNot(String biNumero, FuncionarioId id);
}
