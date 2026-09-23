package cv.igrp.RH_Service.colaboradores.application.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cv.igrp.RH_Service.colaboradores.application.dto.PedidoAusenciaRequestDTO;
import cv.igrp.RH_Service.colaboradores.application.services.CalendarioFeriadosService;
import cv.igrp.RH_Service.colaboradores.application.services.SaldoAusenciaService;
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
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

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
 * Pedido em horas (V58): as horas valem em cada dia do intervalo; so tipos sem saldo, regime FALTA e
 * sem tectos anuais/mensais; o tecto diario soma os pedidos do tipo; so colide quando as horas se cruzam.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CreatePedidoAusenciaEmHorasTest {

    private static final LocalDate INICIO = LocalDate.of(2027, 3, 1);

    @Mock private PedidoAusenciaRepository pedidoRepository;
    @Mock private FuncionarioRepository funcionarioRepository;
    @Mock private TipoAusenciaRepository tipoAusenciaRepository;
    @Mock private CalendarioFeriadosService calendarioFeriadosService;
    @Mock private DiasUteisCalculator diasUteisCalculator;
    @Mock private SaldoAusenciaService saldoAusenciaService;

    @InjectMocks private CreatePedidoAusenciaCommandHandler handler;

    private final FuncionarioId funcionario = FuncionarioId.gerarNovo();

    @BeforeEach
    void base() {
        when(funcionarioRepository.findById(funcionario)).thenReturn(Optional.of(mock(Funcionario.class)));
        when(pedidoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(pedidoRepository.findEmHorasDoTipoEntre(any(), any(), any(), any())).thenReturn(List.of());
    }

    private TipoAusencia tipo(boolean desconta, RegimeAusencia regime, Integer anual, Integer ocorrencia, Integer maxMinutos) {
        var t = TipoAusencia.reconstituir(TipoAusenciaId.gerarNovo(), "Tipo", "TIPO", desconta, true, anual, ocorrencia,
                null, null, true, regime, EfeitoNaRemuneracao.SEM_PERDA, ContagemDias.DIAS_SEGUIDOS)
                .comMaxMinutosPorDia(maxMinutos);
        when(tipoAusenciaRepository.findById(t.getId())).thenReturn(Optional.of(t));
        return t;
    }

    private PedidoAusenciaRequestDTO pedido(TipoAusencia t, LocalDate fim, String de, String ate) {
        var dto = new PedidoAusenciaRequestDTO();
        dto.setTipoAusenciaId(t.getId().getStringValor());
        dto.setDataInicio(INICIO);
        dto.setDataFim(fim);
        dto.setMotivo("teste");
        dto.setHoraInicio(de);
        dto.setHoraFim(ate);
        return dto;
    }

    private void assert422(Runnable r) {
        var e = assertThrows(IgrpResponseStatusException.class, r::run);
        assertEquals(422, e.getStatusCode().value());
    }

    @Test
    void amamentacaoUmaHoraPorDiaDuranteMeses() {
        var t = tipo(false, RegimeAusencia.FALTA, null, 183, 120);

        var r = handler.handle(new CreatePedidoAusenciaCommand(funcionario.getStringValor(),
                pedido(t, INICIO.plusDays(150), "08:00", "09:00")));

        assertEquals(201, r.getStatusCode().value());
        assertEquals(0, r.getBody().getNumeroDias());
        assertEquals(60, r.getBody().getMinutosPorDia());
        var captor = ArgumentCaptor.forClass(PedidoAusencia.class);
        verify(pedidoRepository).save(captor.capture());
        assertTrue(captor.getValue().isEmHoras());
        assertEquals(LocalTime.of(8, 0), captor.getValue().getHoraInicio());
        verify(saldoAusenciaService, never()).reservar(any());
    }

    @Test
    void oTectoDiarioSomaOsPedidosDoTipo() {
        var t = tipo(false, RegimeAusencia.FALTA, null, 183, 120);
        var primeira = PedidoAusencia.criar(funcionario, t.getId(), INICIO, INICIO.plusDays(150), 0, "manha", null);
        primeira.definirHoras(LocalTime.of(8, 0), LocalTime.of(9, 0));
        when(pedidoRepository.findEmHorasDoTipoEntre(any(), any(), any(), any())).thenReturn(List.of(primeira));

        // A segunda hora cabe (60 + 60 = 120)...
        assertEquals(201, handler.handle(new CreatePedidoAusenciaCommand(funcionario.getStringValor(),
                pedido(t, INICIO.plusDays(150), "15:00", "16:00"))).getStatusCode().value());
        // ...uma de hora e meia ja nao (60 + 90 > 120).
        assert422(() -> handler.handle(new CreatePedidoAusenciaCommand(funcionario.getStringValor(),
                pedido(t, INICIO.plusDays(150), "15:00", "16:30"))));
    }

    @Test
    void oTectoPorOcorrenciaContaOsDiasDoIntervalo() {
        var t = tipo(false, RegimeAusencia.FALTA, null, 183, 120);
        assert422(() -> handler.handle(new CreatePedidoAusenciaCommand(funcionario.getStringValor(),
                pedido(t, INICIO.plusDays(183), "08:00", "09:00"))));   // 184 dias
    }

    @Test
    void tiposQueNaoAdmitemHoras() {
        var comSaldo = tipo(true, RegimeAusencia.FALTA, null, null, null);
        var ferias = tipo(false, RegimeAusencia.FERIAS, null, null, null);
        var injustificada = tipo(false, RegimeAusencia.FALTA_INJUSTIFICADA, null, null, null);
        var comTectoAnual = tipo(false, RegimeAusencia.FALTA, 15, null, null);
        for (var t : List.of(comSaldo, ferias, injustificada, comTectoAnual))
            assert422(() -> handler.handle(new CreatePedidoAusenciaCommand(funcionario.getStringValor(),
                    pedido(t, INICIO, "10:00", "11:00"))));
        verify(pedidoRepository, never()).save(any());
    }

    @Test
    void horasMalEscritasAoContrarioOuSoUma() {
        var t = tipo(false, RegimeAusencia.FALTA, null, null, null);
        assert422(() -> handler.handle(new CreatePedidoAusenciaCommand(funcionario.getStringValor(), pedido(t, INICIO, "10h", "11:00"))));
        assert422(() -> handler.handle(new CreatePedidoAusenciaCommand(funcionario.getStringValor(), pedido(t, INICIO, "11:00", "10:00"))));
        assert422(() -> handler.handle(new CreatePedidoAusenciaCommand(funcionario.getStringValor(), pedido(t, INICIO, "10:00", null))));
    }

    @Test
    void horasQueSeCruzamComOutroPedidoSao409() {
        var t = tipo(false, RegimeAusencia.FALTA, null, null, null);
        when(pedidoRepository.existsSobreposicaoEmHoras(any(), any(), any(), any(), any())).thenReturn(true);
        var e = assertThrows(IgrpResponseStatusException.class, () -> handler.handle(new CreatePedidoAusenciaCommand(
                funcionario.getStringValor(), pedido(t, INICIO, "10:00", "11:00"))));
        assertEquals(409, e.getStatusCode().value());
    }
}
