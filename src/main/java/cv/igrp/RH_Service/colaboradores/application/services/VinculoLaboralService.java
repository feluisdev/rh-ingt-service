package cv.igrp.RH_Service.colaboradores.application.services;

import cv.igrp.RH_Service.colaboradores.domain.repository.ContratoRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.parametrizacoes.domain.models.VinculoLaboral;
import cv.igrp.RH_Service.parametrizacoes.domain.repository.ContractTypeRepository;
import cv.igrp.RH_Service.parametrizacoes.domain.repository.VinculoLaboralRepository;
import cv.igrp.RH_Service.parametrizacoes.domain.valueobject.ContractTypeId;
import cv.igrp.RH_Service.parametrizacoes.domain.valueobject.VinculoLaboralId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Vínculo laboral do colaborador. O vínculo é da <b>relação de trabalho</b>, não da pessoa:
 * chega-se a ele pelo contrato corrente → tipo de contrato → vínculo. Não se guarda no
 * funcionário de propósito — muda quando o contrato muda, e o histórico de contratos é o
 * histórico de vínculos.
 *
 * <p>Porta única para as regras que dependem do vínculo (progressão, promoção, transferência)
 * e para as leituras que o mostram.
 */
@Service
@RequiredArgsConstructor
public class VinculoLaboralService {

    private final ContratoRepository contratoRepository;
    private final ContractTypeRepository contractTypeRepository;
    private final VinculoLaboralRepository vinculoLaboralRepository;

    /**
     * Vínculo do contrato corrente, ou vazio se o colaborador não tiver contrato corrente,
     * se o tipo de contrato não tiver vínculo configurado, ou se o vínculo não existir.
     * Para leituras — não lança.
     */
    public Optional<VinculoLaboral> procurarDoFuncionario(FuncionarioId funcionarioId) {
        return contratoRepository.findCurrentByFuncionarioId(funcionarioId)
                .map(contrato -> contrato.getContractTypeId())
                .flatMap(tipoId -> contractTypeRepository.findById(ContractTypeId.from(tipoId)))
                .map(tipo -> tipo.getVinculoLaboralId())
                .flatMap(vinculoId -> vinculoLaboralRepository.findById(VinculoLaboralId.from(vinculoId)));
    }

    /**
     * Vínculo do contrato corrente para quem <b>exige</b> um — os movimentos de carreira.
     * Sem contrato corrente ou sem vínculo configurado, a operação não pode prosseguir.
     *
     * @throws IgrpResponseStatusException 422 quando não há vínculo determinável
     */
    public VinculoLaboral doFuncionario(FuncionarioId funcionarioId) {
        var contrato = contratoRepository.findCurrentByFuncionarioId(funcionarioId)
                .orElseThrow(() -> IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY,
                        "O colaborador não tem contrato corrente — não é possível determinar o vínculo laboral."));

        var tipo = contrato.getContractTypeId() == null ? null
                : contractTypeRepository.findById(ContractTypeId.from(contrato.getContractTypeId())).orElse(null);
        if (tipo == null || tipo.getVinculoLaboralId() == null)
            throw IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY,
                    "O tipo de contrato do colaborador não tem vínculo laboral configurado.");

        return vinculoLaboralRepository.findById(VinculoLaboralId.from(tipo.getVinculoLaboralId()))
                .orElseThrow(() -> IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY,
                        "Vínculo laboral não encontrado: " + tipo.getVinculoLaboralId()));
    }
}
