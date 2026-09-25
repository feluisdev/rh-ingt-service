package cv.igrp.RH_Service.colaboradores.application.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cv.igrp.RH_Service.colaboradores.application.dto.PedidoAusenciaRequestDTO;
import cv.igrp.RH_Service.colaboradores.application.services.CalendarioFeriadosService;
import cv.igrp.RH_Service.colaboradores.application.services.SaldoAusenciaService;
import cv.igrp.RH_Service.colaboradores.domain.models.EstadoPedidoAusencia;
import cv.igrp.RH_Service.colaboradores.domain.models.Funcionario;
import cv.igrp.RH_Service.colaboradores.domain.models.PedidoAusencia;
import cv.igrp.RH_Service.colaboradores.domain.models.TipoAusencia;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.PedidoAusenciaRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.TipoAusenciaRepository;
import cv.igrp.RH_Service.colaboradores.domain.service.DiasUteisCalculator;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.TipoAusenciaId;
import cv.igrp.RH_Service.parametrizacoes.domain.models.ContagemDias;
import cv.igrp.RH_Service.parametrizacoes.domain.models.EfeitoNaRemuneracao;
import cv.igrp.RH_Service.parametrizacoes.domain.models.RegimeAusencia;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/**
 * DL n.o 3/2010, art. 15.o: as faltas que sao um direito (luto, casamento, doenca...) nao se aprovam --
 * o tipo com requires_approval = false nasce APROVADO, sem decisor; os outros nascem PENDENTE.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CreatePedidoAusenciaAprovacaoAutomaticaTest {

    private static final LocalDate INICIO = LocalDate.of(2027, 3, 1);

    @Mock private PedidoAusenciaRepository pedidoRepository;
    @Mock private FuncionarioRepository funcionarioRepository;
    @Mock private TipoAusenciaRepository tipoAusenciaRepository;
    @Mock private CalendarioFeriadosService calendarioFeriadosService;
    @Mock private DiasUteisCalculator diasUteisCalculator;
    @Mock private SaldoAusenciaService saldoAusenciaService;
    @Mock private cv.igrp.RH_Service.colaboradores.application.services.AvisosAusencia avisosAusencia;

    @InjectMocks private CreatePedidoAusenciaCommandHandler handler;

    private final FuncionarioId funcionario = FuncionarioId.gerarNovo();

    @BeforeEach
    void base() {
        when(funcionarioRepository.findById(funcionario)).thenReturn(Optional.of(mock(Funcionario.class)));
        when(pedidoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(pedidoRepository.findEmHorasDoTipoEntre(any(), any(), any(), any())).thenReturn(List.of());
        when(calendarioFeriadosService.feriadosDoColaborador(any(), any(), any())).thenReturn(Set.of());
        when(diasUteisCalculator.calcular(any(), any(), any(), any())).thenReturn(3);
    }

    private TipoAusencia tipo(Boolean requerAprovacao) {
        var t = TipoAusencia.reconstituir(TipoAusenciaId.gerarNovo(), "Luto", "LUTO", false, requerAprovacao, null, null,
                null, null, true, RegimeAusencia.FALTA, EfeitoNaRemuneracao.SEM_PERDA, ContagemDias.DIAS_SEGUIDOS);
        when(tipoAusenciaRepository.findById(t.getId())).thenReturn(Optional.of(t));
        return t;
    }

    private PedidoAusencia criar(TipoAusencia t, String horaInicio, String horaFim) {
        var dto = new PedidoAusenciaRequestDTO();
        dto.setTipoAusenciaId(t.getId().getStringValor());
        dto.setDataInicio(INICIO);
        dto.setDataFim(INICIO.plusDays(2));
        dto.setMotivo("teste");
        dto.setHoraInicio(horaInicio);
        dto.setHoraFim(horaFim);
        handler.handle(new CreatePedidoAusenciaCommand(funcionario.getStringValor(), dto));
        var captor = ArgumentCaptor.forClass(PedidoAusencia.class);
        verify(pedidoRepository).save(captor.capture());
        return captor.getValue();
    }

    @Test
    void tipoQueNaoRequerAprovacaoNasceAprovadoSemDecisor() {
        var p = criar(tipo(false), null, null);
        assertEquals(EstadoPedidoAusencia.APROVADO, p.getEstado());
        assertNull(p.getAprovadoPor());
        assertTrue(p.isAprovacaoAutomatica());
        verify(saldoAusenciaService).confirmarGozo(p);
    }

    @Test
    void tambemNoPedidoEmHoras() {
        var p = criar(tipo(false), "08:00", "09:00");
        assertEquals(EstadoPedidoAusencia.APROVADO, p.getEstado());
        assertTrue(p.isAprovacaoAutomatica());
    }

    @Test
    void tipoQueRequerAprovacaoNascePendente() {
        var p = criar(tipo(true), null, null);
        assertEquals(EstadoPedidoAusencia.PENDENTE, p.getEstado());
        assertFalse(p.isAprovacaoAutomatica());
        verify(saldoAusenciaService, never()).confirmarGozo(any());
    }

    @Test
    void semValorNoCatalogoRequerAprovacao() {
        assertEquals(EstadoPedidoAusencia.PENDENTE, criar(tipo(null), null, null).getEstado());
    }
}
