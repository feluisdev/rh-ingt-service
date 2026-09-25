package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.domain.models.Funcionario;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;

/** O nome de um colaborador para as respostas (nulo se não existir). */
final class Nomes {
    private Nomes() {}

    static String de(FuncionarioRepository repo, FuncionarioId id) {
        return repo.findById(id).map(Funcionario::getNomeCompleto).orElse(null);
    }
}
