package cv.igrp.RH_Service.colaboradores.application.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cv.igrp.RH_Service.colaboradores.domain.models.Funcionario;
import cv.igrp.RH_Service.colaboradores.domain.models.LicencaMobilidade;
import cv.igrp.RH_Service.colaboradores.domain.models.SubtipoLicencaMobilidade;
import cv.igrp.RH_Service.colaboradores.domain.models.TipoFactoRh;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.LicencaMobilidadeRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.SubtipoLicencaMobilidadeId;
import cv.igrp.RH_Service.shared.application.services.notificacoes.Notificador;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.domain.notificacoes.NotificacaoRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/** BR-CMS: renovação por 3 anos, cessação com aviso prévio de 60 dias (sem aviso na pena), aviso do termo. */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ComissaoServicoServiceTest {

    private static final LocalDate HOJE = LocalDate.of(2026, 9, 1);
    private static final LocalDate INICIO = LocalDate.of(2024, 1, 1);
    private static final LocalDate FIM = LocalDate.of(2026, 12, 31);

    @Mock private LicencaMobilidadeRepository repository;
    @Mock private MobilidadeService mobilidadeService;
    @Mock private LicencaEfeitoService licencaEfeitoService;
    @Mock private FuncionarioRepository funcionarioRepository;
    @Mock private DiarioFactos diarioFactos;
    @Mock private NotificacaoRepository notificacaoRepository;
    private ComissaoServicoService service;
    private final FuncionarioId pessoa = FuncionarioId.gerarNovo();
    private final SubtipoLicencaMobilidade subtipo = mock(SubtipoLicencaMobilidade.class);

    @BeforeEach
    void setUp() {
        service = new ComissaoServicoService(repository, mobilidadeService, licencaEfeitoService, funcionarioRepository, diarioFactos,
                new Notificador(notificacaoRepository)) {
            @Override LocalDate hoje() { return HOJE; }
        };
        var f = mock(Funcionario.class);
        when(f.getNomeCompleto()).thenReturn("Rui");
        when(funcionarioRepository.findById(any())).thenReturn(Optional.of(f));
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(subtipo.regressaOuCessa()).thenReturn(true);
        when(mobilidadeService.subtipoDe(any())).thenReturn(subtipo);
    }

    private LicencaMobilidade comissao() {
        var l = LicencaMobilidade.criar(pessoa, SubtipoLicencaMobilidadeId.gerarNovo(), INICIO, FIM, null, "D-1", null, null, null, null, null);
        l.aprovar();
        when(repository.findById(l.getId())).thenReturn(Optional.of(l));
        return l;
    }

    @Test
    void renovaPorMaisTresAnosEFicaNoDiario() {
        var l = comissao();
        service.renovar(pessoa, l.getId(), "Despacho 5/2026");
        assertEquals(FIM.plusYears(3), l.getDataFim());
        assertEquals(1, l.extensoes());
        verify(diarioFactos).registar(eq(pessoa), eq(TipoFactoRh.COMISSAO_SERVICO), eq(FIM.plusDays(1)), anyString(), any(), anyString(), any());
    }

    @Test
    void naoRenovaOQueNaoEComissao() {
        var l = comissao();
        when(subtipo.regressaOuCessa()).thenReturn(false);
        assertThrows(IgrpResponseStatusException.class, () -> service.renovar(pessoa, l.getId(), null));
    }

    @Test
    void cessacaoExigeAvisoPrevioDe60Dias() {
        var l = comissao();
        var ex = assertThrows(IgrpResponseStatusException.class, () -> service.cessar(pessoa, l.getId(),
                ComissaoServicoService.Iniciativa.ENTIDADE, HOJE, HOJE.plusDays(59), null));
        assertEquals(422, ex.getStatusCode().value());
        service.cessar(pessoa, l.getId(), ComissaoServicoService.Iniciativa.NOMEADO, HOJE, null, "Motivos pessoais");
        assertEquals(HOJE.plusDays(59), l.getDataFim());
        assertTrue(l.getObservacoes().contains("iniciativa do nomeado"));
        verify(licencaEfeitoService, never()).aplicarRegresso(any());
    }

    @Test
    void penaDisciplinarCessaSemAvisoEAplicaJa() {
        var l = comissao();
        service.cessar(pessoa, l.getId(), ComissaoServicoService.Iniciativa.PENA_DISCIPLINAR, HOJE, HOJE, "Pena de suspensão");
        assertEquals(HOJE.minusDays(1), l.getDataFim());
        verify(licencaEfeitoService).aplicarRegresso(l);
    }

    @Test
    void naoCessaParaDepoisDoTermo() {
        var l = comissao();
        assertThrows(IgrpResponseStatusException.class, () -> service.cessar(pessoa, l.getId(),
                ComissaoServicoService.Iniciativa.ENTIDADE, HOJE, FIM.plusDays(10), null));
    }

    @Test
    void avisaNoventaDiasAntes() {
        var l = comissao();
        var dia = FIM.minusDays(90);
        when(repository.findComissoesEmCurso(FIM)).thenReturn(List.of(l));
        assertEquals(1, service.avisarTermos(dia));
    }
}
