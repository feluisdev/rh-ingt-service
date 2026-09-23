package cv.igrp.RH_Service.colaboradores.application.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cv.igrp.RH_Service.colaboradores.application.services.HorarioColaboradorService.Origem;
import cv.igrp.RH_Service.colaboradores.application.services.HorarioColaboradorService.Vigente;
import cv.igrp.RH_Service.colaboradores.domain.models.DiaAssiduidade;
import cv.igrp.RH_Service.colaboradores.domain.models.EstadoMarcacao;
import cv.igrp.RH_Service.colaboradores.domain.models.Funcionario;
import cv.igrp.RH_Service.colaboradores.domain.models.MarcacaoAssiduidade;
import cv.igrp.RH_Service.colaboradores.domain.models.OrigemMarcacao;
import cv.igrp.RH_Service.colaboradores.domain.models.RegimePrestacao;
import cv.igrp.RH_Service.colaboradores.domain.models.SentidoMarcacao;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.MarcacaoAssiduidadeRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.PedidoAusenciaRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/**
 * Registo pelo proprio: picagem em tempo real so em teletrabalho/misto (Lei 20/X/2023, art. 170.o);
 * pedido de correcao PENDENTE que nao conta ate a chefia directa ou o RH decidirem.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AssiduidadeProprioTest {

    @Mock private MarcacaoAssiduidadeRepository marcacaoRepository;
    @Mock private FuncionarioRepository funcionarioRepository;
    @Mock private PedidoAusenciaRepository pedidoAusenciaRepository;
    @Mock private CalendarioFeriadosService calendarioFeriadosService;
    @Mock private HorarioColaboradorService horarioColaboradorService;
    @Mock private ChefiaService chefiaService;

    @InjectMocks private AssiduidadeService service;

    private final FuncionarioId eu = FuncionarioId.gerarNovo();
    private final FuncionarioId chefe = FuncionarioId.gerarNovo();

    @BeforeEach
    void base() {
        var pessoa = mock(Funcionario.class);
        when(pessoa.getIsActive()).thenReturn(true);
        when(funcionarioRepository.findById(eu)).thenReturn(Optional.of(pessoa));
        when(marcacaoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(pedidoAusenciaRepository.findAprovadosEntre(any(), any(), any())).thenReturn(List.of());
        when(calendarioFeriadosService.feriadosDoColaborador(any(), any(), any())).thenReturn(Set.of());
    }

    private void regimeHoje(RegimePrestacao regime) {
        when(horarioColaboradorService.vigente(eq(eu), any())).thenReturn(new Vigente(Origem.COLABORADOR, null, regime, null));
    }

    @Test
    void emTeletrabalhoPicaComAHoraDoServidor() {
        regimeHoje(RegimePrestacao.TELETRABALHO);
        LocalDateTime antes = LocalDateTime.now().withSecond(0).withNano(0);

        var r = service.picarPeloProprio(eu, SentidoMarcacao.ENTRADA);

        assertEquals(OrigemMarcacao.PROPRIO, r.marcacao().getOrigem());
        assertEquals(EstadoMarcacao.VALIDA, r.marcacao().getEstado());
        assertFalse(r.marcacao().getMomento().isBefore(antes));
    }

    @Test
    void noMistoTambem() {
        regimeHoje(RegimePrestacao.MISTO);
        assertEquals(EstadoMarcacao.VALIDA, service.picarPeloProprio(eu, SentidoMarcacao.SAIDA).marcacao().getEstado());
    }

    @Test
    void emPresencialOuSemHorarioE422() {
        regimeHoje(RegimePrestacao.PRESENCIAL);
        assertEquals(422, assertThrows(IgrpResponseStatusException.class,
                () -> service.picarPeloProprio(eu, SentidoMarcacao.ENTRADA)).getStatusCode().value());
        regimeHoje(null);
        assertEquals(422, assertThrows(IgrpResponseStatusException.class,
                () -> service.picarPeloProprio(eu, SentidoMarcacao.ENTRADA)).getStatusCode().value());
        verify(marcacaoRepository, never()).save(any());
    }

    @Test
    void aCorrecaoFicaPendenteENaoContaNoDia() {
        LocalDateTime ontem8 = LocalDate.now().minusDays(1).atTime(8, 0);
        var m = service.pedirCorrecao(eu, ontem8, SentidoMarcacao.ENTRADA, "esqueci-me");

        assertEquals(EstadoMarcacao.PENDENTE, m.getEstado());
        assertTrue(m.isPendente());
        assertFalse(m.conta());
        var dia = DiaAssiduidade.calcular(ontem8.toLocalDate(), List.of(m));
        assertTrue(dia.anomalias().isEmpty());
        assertEquals(0, dia.minutosTrabalhados());
    }

    @Test
    void correcaoSemMotivoE422() {
        assertThrows(IgrpResponseStatusException.class,
                () -> service.pedirCorrecao(eu, LocalDateTime.now().minusHours(3), SentidoMarcacao.ENTRADA, " "));
    }

    private MarcacaoAssiduidade pendente() {
        var m = MarcacaoAssiduidade.pedirCorrecao(eu, LocalDateTime.now().minusDays(1), SentidoMarcacao.ENTRADA, "esqueci-me",
                LocalDateTime.now());
        when(marcacaoRepository.findById(m.getId())).thenReturn(Optional.of(m));
        return m;
    }

    @Test
    void aChefiaDirectaValida() {
        var m = pendente();
        when(chefiaService.eChefeDirecto(chefe, eu)).thenReturn(true);

        service.decidir(chefe, null, m.getId(), true, null);

        assertEquals(EstadoMarcacao.VALIDA, m.getEstado());
        assertEquals(chefe, m.getDecididaPor());
        assertTrue(m.conta());
    }

    @Test
    void quemNaoEChefiaDirectaE403EOProprioE422() {
        var m = pendente();
        when(chefiaService.eChefeDirecto(any(), any())).thenReturn(false);
        assertEquals(403, assertThrows(IgrpResponseStatusException.class,
                () -> service.decidir(chefe, null, m.getId(), true, null)).getStatusCode().value());
        assertEquals(422, assertThrows(IgrpResponseStatusException.class,
                () -> service.decidir(eu, null, m.getId(), true, null)).getStatusCode().value());
        assertEquals(EstadoMarcacao.PENDENTE, m.getEstado());
    }

    @Test
    void oRhRejeitaComMotivoEDecidirDuasVezesE409() {
        var m = pendente();
        assertEquals(422, assertThrows(IgrpResponseStatusException.class,
                () -> service.decidir(null, eu, m.getId(), false, "")).getStatusCode().value());

        service.decidir(null, eu, m.getId(), false, "estava de ferias");
        assertEquals(EstadoMarcacao.REJEITADA, m.getEstado());
        assertNull(m.getDecididaPor());
        assertEquals("estava de ferias", m.getMotivoRejeicao());

        assertEquals(409, assertThrows(IgrpResponseStatusException.class,
                () -> service.decidir(null, eu, m.getId(), true, null)).getStatusCode().value());
    }

    @Test
    void oRhNaoDecideUmaMarcacaoDeOutraPessoaPeloUrlErrado() {
        var m = pendente();
        assertEquals(404, assertThrows(IgrpResponseStatusException.class,
                () -> service.decidir(null, FuncionarioId.gerarNovo(), m.getId(), true, null)).getStatusCode().value());
    }

    @Test
    void aCaixaDaChefiaSoTemAEquipaDirecta() {
        var colega = FuncionarioId.gerarNovo();
        when(chefiaService.equipaDirecta(chefe)).thenReturn(List.of(eu, colega));
        service.pendentesDaEquipa(chefe);
        verify(marcacaoRepository).findPendentesDe(List.of(eu, colega));
    }
}
