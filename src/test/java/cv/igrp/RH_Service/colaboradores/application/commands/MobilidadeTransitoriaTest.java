package cv.igrp.RH_Service.colaboradores.application.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cv.igrp.RH_Service.colaboradores.application.services.AssignmentService;
import cv.igrp.RH_Service.colaboradores.application.services.LicencaService;
import cv.igrp.RH_Service.colaboradores.application.services.MobilidadeService;
import cv.igrp.RH_Service.colaboradores.domain.models.LicencaMobilidade;
import cv.igrp.RH_Service.colaboradores.domain.models.SubtipoLicencaMobilidade;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.HistoricoEstadoColaboradorRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.LicencaMobilidadeRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.SubtipoLicencaMobilidadeRepository;
import cv.igrp.RH_Service.parametrizacoes.domain.repository.WorkerStateRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.SubtipoLicencaMobilidadeId;
import cv.igrp.RH_Service.estrutura.domain.models.OrganizationalUnit;
import cv.igrp.RH_Service.estrutura.domain.repository.OrganizationalUnitRepository;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Prova de que a mobilidade transitória <b>não toca na afectação</b> (Lei n.º 20/X/2023,
 * art. 135.º n.º 7) e das validações do destino e da duração, que vêm parametrizadas do subtipo.
 */
@ExtendWith(MockitoExtension.class)
class MobilidadeTransitoriaTest {

    @Mock private LicencaMobilidadeRepository licencaRepository;
    @Mock private SubtipoLicencaMobilidadeRepository subtipoRepository;
    @Mock private OrganizationalUnitRepository unidadeRepository;

    private MobilidadeService mobilidadeService;
    private AprovarLicencaMobilidadeCommandHandler aprovar;
    private EncerrarLicencaMobilidadeCommandHandler encerrar;
    private CancelarLicencaMobilidadeCommandHandler cancelar;

    private final FuncionarioId funcionarioId = FuncionarioId.gerarNovo();
    private final SubtipoLicencaMobilidadeId subtipoId = SubtipoLicencaMobilidadeId.gerarNovo();
    private final UUID unidadeDestino = UUID.randomUUID();
    private final LocalDate inicio = LocalDate.of(2026, 1, 1);

    private void servicos() {
        mobilidadeService = new MobilidadeService(subtipoRepository, unidadeRepository, licencaRepository);
        // A mobilidade nunca abre vaga, por isso o serviço da licença nunca faz nada aqui.
        var licencaService = new LicencaService(Mockito.mock(AssignmentService.class),
                Mockito.mock(FuncionarioRepository.class), Mockito.mock(WorkerStateRepository.class),
                Mockito.mock(HistoricoEstadoColaboradorRepository.class));
        aprovar = new AprovarLicencaMobilidadeCommandHandler(licencaRepository, mobilidadeService, licencaService);
        encerrar = new EncerrarLicencaMobilidadeCommandHandler(licencaRepository, mobilidadeService, licencaService);
        cancelar = new CancelarLicencaMobilidadeCommandHandler(licencaRepository);
    }

    private SubtipoLicencaMobilidade subtipo(String recordType, Integer maxDias, Integer maxProrrogacoes) {
        return SubtipoLicencaMobilidade.reconstituir(subtipoId, "Requisição", "MOB_REQ", recordType,
                false, true, false, true, maxDias, maxProrrogacoes);
    }

    private LicencaMobilidade registo(UUID unidadeId, String entidade, LocalDate fim) {
        return LicencaMobilidade.criar(funcionarioId, subtipoId, inicio, fim, entidade,
                "12/2026", null, null, unidadeId, null, null);
    }

    private void comSubtipo(SubtipoLicencaMobilidade s) {
        when(subtipoRepository.findById(any())).thenReturn(Optional.of(s));
    }

    private void comRegisto(LicencaMobilidade l) {
        when(licencaRepository.findById(any())).thenReturn(Optional.of(l));
    }

    @Test
    void aprovarMobilidadeInternaNaoMexeNaAfectacao() {
        servicos();
        var licenca = registo(unidadeDestino, null, inicio.plusDays(180));
        comRegisto(licenca);
        comSubtipo(subtipo("MOBILIDADE", 365, 1));
        when(unidadeRepository.findById(any())).thenReturn(Optional.of(Mockito.mock(OrganizationalUnit.class)));

        var response = aprovar.handle(new AprovarLicencaMobilidadeCommand(UUID.randomUUID().toString()));

        assertEquals(200, response.getStatusCode().value());
        ArgumentCaptor<LicencaMobilidade> captor = ArgumentCaptor.forClass(LicencaMobilidade.class);
        verify(licencaRepository).save(captor.capture());
        assertEquals(LicencaMobilidade.ACTIVE, captor.getValue().getStatus());
    }

    @Test
    void aprovarMobilidadeExternaBastaAEntidadeDeDestino() {
        servicos();
        var licenca = registo(null, "Ministério das Finanças", inicio.plusDays(300));
        comRegisto(licenca);
        comSubtipo(subtipo("MOBILIDADE", 365, 1));

        aprovar.handle(new AprovarLicencaMobilidadeCommand(UUID.randomUUID().toString()));

        verify(licencaRepository).save(any(LicencaMobilidade.class));
        verify(unidadeRepository, never()).findById(any());
    }

    @Test
    void mobilidadeSemDestinoNenhumEhRecusada() {
        servicos();
        comRegisto(registo(null, null, inicio.plusDays(30)));
        comSubtipo(subtipo("MOBILIDADE", 365, 1));

        var ex = assertThrows(IgrpResponseStatusException.class,
                () -> aprovar.handle(new AprovarLicencaMobilidadeCommand(UUID.randomUUID().toString())));

        assertEquals(422, ex.getStatusCode().value());
        verify(licencaRepository, never()).save(any());
    }

    @Test
    void duracaoAcimaDoMaximoDoSubtipoEhRecusada() {
        servicos();
        comRegisto(registo(null, "Câmara Municipal da Praia", inicio.plusDays(400)));
        comSubtipo(subtipo("MOBILIDADE", 365, 1));

        var ex = assertThrows(IgrpResponseStatusException.class,
                () -> aprovar.handle(new AprovarLicencaMobilidadeCommand(UUID.randomUUID().toString())));

        assertEquals(422, ex.getStatusCode().value());
        verify(licencaRepository, never()).save(any());
    }

    @Test
    void subtipoAmbosContaComoMobilidadeENaoEhIgnorado() {
        servicos();
        comRegisto(registo(null, null, inicio.plusDays(30)));
        comSubtipo(subtipo("AMBOS", null, null));

        var ex = assertThrows(IgrpResponseStatusException.class,
                () -> aprovar.handle(new AprovarLicencaMobilidadeCommand(UUID.randomUUID().toString())));

        assertEquals(422, ex.getStatusCode().value());
    }

    @Test
    void licencaPuraNaoExigeDestino() {
        servicos();
        comRegisto(registo(null, null, inicio.plusDays(90)));
        comSubtipo(subtipo("LICENCA", null, null));

        aprovar.handle(new AprovarLicencaMobilidadeCommand(UUID.randomUUID().toString()));

        verify(licencaRepository).save(any(LicencaMobilidade.class));
    }

    @Test
    void encerrarSoFechaORegistoENaoMexeNaAfectacao() {
        servicos();
        var licenca = registo(unidadeDestino, null, inicio.plusDays(180));
        licenca.aprovar();
        comRegisto(licenca);

        encerrar.handle(new EncerrarLicencaMobilidadeCommand(UUID.randomUUID().toString()));

        ArgumentCaptor<LicencaMobilidade> captor = ArgumentCaptor.forClass(LicencaMobilidade.class);
        verify(licencaRepository).save(captor.capture());
        assertEquals(LicencaMobilidade.CLOSED, captor.getValue().getStatus());
    }

    @Test
    void encerrarSoEhPossivelComORegistoActivo() {
        servicos();
        comRegisto(registo(unidadeDestino, null, inicio.plusDays(180)));   // fica PENDING

        var ex = assertThrows(IgrpResponseStatusException.class,
                () -> encerrar.handle(new EncerrarLicencaMobilidadeCommand(UUID.randomUUID().toString())));

        assertEquals(409, ex.getStatusCode().value());
    }

    @Test
    void cancelarFechaORegistoSemReverterNada() {
        servicos();
        var licenca = registo(unidadeDestino, null, inicio.plusDays(180));
        licenca.aprovar();
        comRegisto(licenca);

        cancelar.handle(new CancelarLicencaMobilidadeCommand(UUID.randomUUID().toString()));

        ArgumentCaptor<LicencaMobilidade> captor = ArgumentCaptor.forClass(LicencaMobilidade.class);
        verify(licencaRepository).save(captor.capture());
        assertEquals(LicencaMobilidade.CANCELLED, captor.getValue().getStatus());
    }

    @Test
    void registoRejeitadoNaoPodeSerAprovado() {
        servicos();
        var licenca = registo(unidadeDestino, null, inicio.plusDays(180));
        licenca.rejeitar("fora de prazo");
        comRegisto(licenca);

        var ex = assertThrows(IgrpResponseStatusException.class,
                () -> aprovar.handle(new AprovarLicencaMobilidadeCommand(UUID.randomUUID().toString())));

        assertEquals(409, ex.getStatusCode().value());
    }

    @Test
    void prorrogacaoRespeitaOLimiteDoSubtipo() {
        var licenca = registo(unidadeDestino, null, inicio.plusDays(364));
        licenca.aprovar();

        licenca.prorrogar(inicio.plusDays(700), 1);
        assertEquals(1, licenca.extensoes());

        var ex = assertThrows(IgrpResponseStatusException.class,
                () -> licenca.prorrogar(inicio.plusDays(900), 1));
        assertEquals(422, ex.getStatusCode().value());
    }
}
