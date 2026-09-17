package cv.igrp.RH_Service.colaboradores.application.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import cv.igrp.RH_Service.colaboradores.domain.models.Contrato;
import cv.igrp.RH_Service.colaboradores.domain.repository.ContratoRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.parametrizacoes.domain.models.ContractType;
import cv.igrp.RH_Service.parametrizacoes.domain.models.VinculoLaboral;
import cv.igrp.RH_Service.parametrizacoes.domain.repository.ContractTypeRepository;
import cv.igrp.RH_Service.parametrizacoes.domain.repository.VinculoLaboralRepository;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Prova da travessia contrato corrente → tipo de contrato → vínculo laboral: o vínculo é da
 * relação de trabalho e não da pessoa, por isso não vive no funcionário.
 */
@ExtendWith(MockitoExtension.class)
class VinculoLaboralServiceTest {

    @Mock private ContratoRepository contratoRepository;
    @Mock private ContractTypeRepository contractTypeRepository;
    @Mock private VinculoLaboralRepository vinculoLaboralRepository;

    @InjectMocks private VinculoLaboralService service;

    private final FuncionarioId funcionarioId = FuncionarioId.gerarNovo();

    private VinculoLaboral cadeiaCompleta() {
        Contrato contrato = mock(Contrato.class);
        when(contrato.getContractTypeId()).thenReturn(UUID.randomUUID());
        when(contratoRepository.findCurrentByFuncionarioId(funcionarioId)).thenReturn(Optional.of(contrato));

        ContractType tipo = mock(ContractType.class);
        when(tipo.getVinculoLaboralId()).thenReturn(UUID.randomUUID());
        when(contractTypeRepository.findById(any())).thenReturn(Optional.of(tipo));

        VinculoLaboral vinculo = mock(VinculoLaboral.class);
        when(vinculoLaboralRepository.findById(any())).thenReturn(Optional.of(vinculo));
        return vinculo;
    }

    @Test
    void devolveOVinculoDoContratoCorrente() {
        VinculoLaboral vinculo = cadeiaCompleta();

        assertSame(vinculo, service.doFuncionario(funcionarioId));
        assertSame(vinculo, service.procurarDoFuncionario(funcionarioId).orElseThrow());
    }

    @Test
    void semContratoCorrenteLancaEmDoFuncionarioEDevolveVazioEmProcurar() {
        when(contratoRepository.findCurrentByFuncionarioId(funcionarioId)).thenReturn(Optional.empty());

        var ex = assertThrows(IgrpResponseStatusException.class, () -> service.doFuncionario(funcionarioId));
        assertEquals(422, ex.getStatusCode().value());
        assertTrue(service.procurarDoFuncionario(funcionarioId).isEmpty());
    }

    @Test
    void tipoDeContratoSemVinculoConfiguradoLanca() {
        Contrato contrato = mock(Contrato.class);
        when(contrato.getContractTypeId()).thenReturn(UUID.randomUUID());
        when(contratoRepository.findCurrentByFuncionarioId(funcionarioId)).thenReturn(Optional.of(contrato));

        ContractType tipo = mock(ContractType.class);
        when(tipo.getVinculoLaboralId()).thenReturn(null);
        when(contractTypeRepository.findById(any())).thenReturn(Optional.of(tipo));

        var ex = assertThrows(IgrpResponseStatusException.class, () -> service.doFuncionario(funcionarioId));
        assertEquals(422, ex.getStatusCode().value());
        assertTrue(service.procurarDoFuncionario(funcionarioId).isEmpty());
    }
}
