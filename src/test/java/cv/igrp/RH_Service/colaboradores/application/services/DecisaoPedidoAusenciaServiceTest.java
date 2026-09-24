package cv.igrp.RH_Service.colaboradores.application.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cv.igrp.RH_Service.colaboradores.domain.models.EstadoPedidoAusencia;
import cv.igrp.RH_Service.colaboradores.domain.models.PedidoAusencia;
import cv.igrp.RH_Service.colaboradores.domain.repository.PedidoAusenciaRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.TipoAusenciaId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/**
 * Quem decide um pedido de ausencia: a chefia directa (pela sua caixa) ou o RH (sempre); um so nivel;
 * os mesmos efeitos no saldo pelos dois caminhos.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DecisaoPedidoAusenciaServiceTest {

    @Mock private PedidoAusenciaRepository pedidoRepository;
    @Mock private SaldoAusenciaService saldoAusenciaService;
    @Mock private ChefiaService chefiaService;
    @InjectMocks private DecisaoPedidoAusenciaService service;

    private final FuncionarioId maria = FuncionarioId.gerarNovo();
    private final FuncionarioId chefe = FuncionarioId.gerarNovo();
    private final FuncionarioId rh = FuncionarioId.gerarNovo();
    private PedidoAusencia pedido;

    @BeforeEach
    void base() {
        pedido = PedidoAusencia.criar(maria, TipoAusenciaId.gerarNovo(), LocalDate.of(2027, 3, 1),
                LocalDate.of(2027, 3, 5), 5, "ferias", null);
        when(pedidoRepository.findById(pedido.getId())).thenReturn(Optional.of(pedido));
        when(pedidoRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(chefiaService.eChefeDirecto(chefe, maria)).thenReturn(true);
    }

    private static int status(Runnable r) {
        return assertThrows(IgrpResponseStatusException.class, r::run).getStatusCode().value();
    }

    @Test
    void aChefiaDirectaAprovaEFicaComoDecisora() {
        service.aprovar(chefe, null, pedido.getId(), null, "boas ferias");
        assertEquals(EstadoPedidoAusencia.APROVADO, pedido.getEstado());
        assertEquals(chefe, pedido.getAprovadoPor());
        verify(saldoAusenciaService).confirmarGozo(pedido);
    }

    @Test
    void aChefiaRejeitaComMotivoESemMotivoE422() {
        assertEquals(422, status(() -> service.rejeitar(chefe, null, pedido.getId(), null, " ")));
        service.rejeitar(chefe, null, pedido.getId(), null, "servico nessa semana");
        assertEquals(EstadoPedidoAusencia.REJEITADO, pedido.getEstado());
        assertEquals("servico nessa semana", pedido.getObservacoesDecisao());
        verify(saldoAusenciaService).libertarReserva(pedido);
    }

    @Test
    void quemNaoEChefiaDirecta403ENinguemDecideOsSeus422() {
        assertEquals(403, status(() -> service.aprovar(FuncionarioId.gerarNovo(), null, pedido.getId(), null, null)));
        assertEquals(422, status(() -> service.aprovar(maria, null, pedido.getId(), null, null)));
        verify(saldoAusenciaService, never()).confirmarGozo(any());
    }

    @Test
    void soSeDecideUmPendente409() {
        service.aprovar(null, maria, pedido.getId(), rh, null);
        assertEquals(409, status(() -> service.rejeitar(chefe, null, pedido.getId(), null, "tarde")));
    }

    @Test
    void oRhDecideSempreMasSoPeloDonoDoPedido() {
        assertEquals(404, status(() -> service.aprovar(null, FuncionarioId.gerarNovo(), pedido.getId(), rh, null)));
        // Pelo RH, rejeitar sem motivo continua a poder-se (o front ja envia assim).
        service.rejeitar(null, maria, pedido.getId(), rh, null);
        assertEquals(rh, pedido.getAprovadoPor());
        assertEquals(EstadoPedidoAusencia.REJEITADO, pedido.getEstado());
    }

    @Test
    void pendentesDaEquipa() {
        when(chefiaService.equipaDirecta(chefe)).thenReturn(List.of(maria));
        when(pedidoRepository.findPendentesDe(List.of(maria))).thenReturn(List.of(pedido));
        assertEquals(List.of(pedido), service.pendentesDaEquipa(chefe));
    }
}
