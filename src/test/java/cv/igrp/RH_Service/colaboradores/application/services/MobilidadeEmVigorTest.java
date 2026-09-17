package cv.igrp.RH_Service.colaboradores.application.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import cv.igrp.RH_Service.colaboradores.domain.models.LicencaMobilidade;
import cv.igrp.RH_Service.colaboradores.domain.models.SubtipoLicencaMobilidade;
import cv.igrp.RH_Service.colaboradores.domain.repository.LicencaMobilidadeRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.SubtipoLicencaMobilidadeRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.SubtipoLicencaMobilidadeId;
import cv.igrp.RH_Service.estrutura.domain.repository.OrganizationalUnitRepository;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Prova de {@link MobilidadeService#mobilidadeEmVigor}: é isto que responde a "onde a pessoa
 * exerce funções hoje", já que a afectação continua a apontar para o Lugar de origem. Uma licença
 * (que não é mobilidade) não conta — não muda o local de trabalho.
 */
@ExtendWith(MockitoExtension.class)
class MobilidadeEmVigorTest {

    @Mock private SubtipoLicencaMobilidadeRepository subtipoRepository;
    @Mock private OrganizationalUnitRepository unidadeRepository;
    @Mock private LicencaMobilidadeRepository licencaRepository;

    @InjectMocks private MobilidadeService service;

    private final FuncionarioId funcionarioId = FuncionarioId.gerarNovo();
    private final SubtipoLicencaMobilidadeId subtipoId = SubtipoLicencaMobilidadeId.gerarNovo();
    private final LocalDate hoje = LocalDate.of(2026, 6, 1);

    private SubtipoLicencaMobilidade subtipo(String recordType) {
        return SubtipoLicencaMobilidade.reconstituir(subtipoId, "Requisição", "MOB_REQ", recordType,
                false, true, false, true, 365, 1);
    }

    private LicencaMobilidade registoActivo(UUID unidadeDestino, String entidade) {
        var l = LicencaMobilidade.criar(funcionarioId, subtipoId, hoje.minusDays(30), hoje.plusDays(30),
                entidade, "7/2026", null, null, unidadeDestino, null, null);
        l.aprovar();
        return l;
    }

    @Test
    void devolveAMobilidadeInternaEmVigor() {
        UUID unidade = UUID.randomUUID();
        when(licencaRepository.findActiveByFuncionarioIdAt(funcionarioId, hoje))
                .thenReturn(List.of(registoActivo(unidade, null)));
        when(subtipoRepository.findById(any())).thenReturn(Optional.of(subtipo("MOBILIDADE")));

        var mobilidade = service.mobilidadeEmVigor(funcionarioId, hoje);

        assertTrue(mobilidade.isPresent());
        assertTrue(mobilidade.get().isDestinoInterno());
        assertEquals(unidade, mobilidade.get().getDestinationUnitId());
    }

    @Test
    void devolveAMobilidadeExternaEmVigor() {
        when(licencaRepository.findActiveByFuncionarioIdAt(funcionarioId, hoje))
                .thenReturn(List.of(registoActivo(null, "Ministério da Saúde")));
        when(subtipoRepository.findById(any())).thenReturn(Optional.of(subtipo("MOBILIDADE")));

        var mobilidade = service.mobilidadeEmVigor(funcionarioId, hoje);

        assertTrue(mobilidade.isPresent());
        assertEquals("Ministério da Saúde", mobilidade.get().getEntidadeDestino());
    }

    @Test
    void licencaNaoContaComoMobilidade() {
        when(licencaRepository.findActiveByFuncionarioIdAt(funcionarioId, hoje))
                .thenReturn(List.of(registoActivo(null, null)));
        when(subtipoRepository.findById(any())).thenReturn(Optional.of(subtipo("LICENCA")));

        assertTrue(service.mobilidadeEmVigor(funcionarioId, hoje).isEmpty());
    }

    @Test
    void semRegistosEmVigorDevolveVazio() {
        when(licencaRepository.findActiveByFuncionarioIdAt(funcionarioId, hoje)).thenReturn(List.of());

        assertTrue(service.mobilidadeEmVigor(funcionarioId, hoje).isEmpty());
    }

    @Test
    void prorrogacaoAcimaDoPeriodoMaximoEhRecusada() {
        var licenca = registoActivo(null, "Câmara Municipal");

        var ex = assertThrows(IgrpResponseStatusException.class,
                () -> service.validarPeriodoDeProrrogacao(licenca, subtipo("MOBILIDADE"),
                        licenca.getDataFim().plusDays(400)));

        assertEquals(422, ex.getStatusCode().value());
    }

    @Test
    void prorrogacaoPorIgualPeriodoEhAceite() {
        var licenca = registoActivo(null, "Câmara Municipal");

        service.validarPeriodoDeProrrogacao(licenca, subtipo("MOBILIDADE"),
                licenca.getDataFim().plusDays(300));

        licenca.prorrogar(licenca.getDataFim().plusDays(300), 1);
        assertEquals(1, licenca.extensoes());
    }
}
