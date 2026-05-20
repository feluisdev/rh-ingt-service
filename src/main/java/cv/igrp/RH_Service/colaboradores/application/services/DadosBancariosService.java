package cv.igrp.RH_Service.colaboradores.application.services;

import cv.igrp.RH_Service.colaboradores.application.dto.DadosBancariosRequestDTO;
import cv.igrp.RH_Service.colaboradores.domain.models.DadosBancarios;
import cv.igrp.RH_Service.colaboradores.domain.repository.DadosBancariosRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DadosBancariosService {

    private final DadosBancariosRepository dadosBancariosRepository;
    private final FuncionarioRepository funcionarioRepository;

    @Transactional
    public DadosBancarios criarDadosBancarios(FuncionarioId funcionarioId, DadosBancariosRequestDTO dto) {
        funcionarioRepository.findById(funcionarioId)
                .orElseThrow(() -> IgrpResponseStatusException.notFound(
                        "Funcionário não encontrado: " + funcionarioId.getStringValor()));

        return dadosBancariosRepository.save(DadosBancarios.criar(
                funcionarioId, dto.getBanco(), dto.getNumeroConta(),
                dto.getIban(), dto.getNumeroSegurancaSocial()));
    }
}
