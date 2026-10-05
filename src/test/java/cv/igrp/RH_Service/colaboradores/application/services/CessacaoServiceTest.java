package cv.igrp.RH_Service.colaboradores.application.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cv.igrp.RH_Service.colaboradores.domain.models.Assignment;
import cv.igrp.RH_Service.colaboradores.domain.models.TipoAfectacao;
import cv.igrp.RH_Service.colaboradores.domain.models.Contrato;
import cv.igrp.RH_Service.colaboradores.domain.models.EstadoContrato;
import cv.igrp.RH_Service.colaboradores.domain.models.Funcionario;
import cv.igrp.RH_Service.colaboradores.domain.models.HistoricoEstadoColaborador;
import cv.igrp.RH_Service.colaboradores.domain.repository.ContratoRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.HistoricoEstadoColaboradorRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.ContratoId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.parametrizacoes.domain.models.WorkerState;
import cv.igrp.RH_Service.parametrizacoes.domain.repository.WorkerStateRepository;
import cv.igrp.RH_Service.parametrizacoes.domain.valueobject.WorkerStateId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Prova do {@link CessacaoService}: a cessação é um só acontecimento com quatro efeitos —
 * contrato cessado, afectação encerrada, estado do trabalhador mudado e histórico registado.
 */
@ExtendWith(MockitoExtension.class)
class CessacaoServiceTest {

    @Mock private FuncionarioRepository funcionarioRepository;
    @Mock private ContratoRepository contratoRepository;
    @Mock private WorkerStateRepository workerStateRepository;
    @Mock private HistoricoEstadoColaboradorRepository historicoRepository;
    @Mock private AssignmentService assignmentService;
    @Mock private SubstituicaoService substituicaoService;

    @org.mockito.Mock private cv.igrp.RH_Service.colaboradores.application.services.DiarioFactos diarioFactos;
    @Mock private cv.igrp.RH_Service.colaboradores.domain.repository.ReservaLugarRepository reservaLugarRepository;
    @InjectMocks private CessacaoService service;

    private final FuncionarioId funcionarioId = FuncionarioId.gerarNovo();
    private final UUID estadoAnteriorId = UUID.randomUUID();
    private final LocalDate dataEfeito = LocalDate.of(2026, 10, 31);

    private static WorkerState estado(String code, boolean cessa) {
        return WorkerState.reconstruir(WorkerStateId.gerarNovo(), code, code, false, true, cessa);
    }

    private Funcionario funcionarioMock() {
        Funcionario funcionario = mock(Funcionario.class);
        when(funcionario.getWorkerStateId()).thenReturn(estadoAnteriorId);
        when(funcionarioRepository.findById(funcionarioId)).thenReturn(Optional.of(funcionario));
        return funcionario;
    }

    @Test
    void cessaContratoAfectacaoEstadoEHistoricoNumaSoOperacao() {
        WorkerState retired = estado("RETIRED", true);
        Funcionario funcionario = funcionarioMock();

        Contrato contrato = mock(Contrato.class);
        UUID contratoId = UUID.randomUUID();
        when(contrato.getStatus()).thenReturn(EstadoContrato.ATIVO);
        when(contrato.getId()).thenReturn(ContratoId.from(contratoId));
        when(contratoRepository.findCurrentByFuncionarioId(funcionarioId)).thenReturn(Optional.of(contrato));

        Assignment afectacao = Assignment.criar(funcionarioId, UUID.randomUUID(), null, null,
                TipoAfectacao.PRINCIPAL, Assignment.ADMISSAO, LocalDate.of(2020, 1, 1), null);
        when(assignmentService.encerrarAfectacaoCorrente(funcionarioId, dataEfeito))
                .thenReturn(Optional.of(afectacao));

        var resultado = service.cessar(funcionarioId, retired, dataEfeito, "AGE_RETIREMENT", "Nota");

        verify(contrato).encerrar(dataEfeito, "AGE_RETIREMENT");
        verify(contratoRepository).save(contrato);
        verify(assignmentService).encerrarAfectacaoCorrente(funcionarioId, dataEfeito);
        verify(funcionario).atualizarWorkerState(retired.getId().getValor(), false);
        verify(funcionarioRepository).save(funcionario);

        ArgumentCaptor<HistoricoEstadoColaborador> captor =
                ArgumentCaptor.forClass(HistoricoEstadoColaborador.class);
        verify(historicoRepository).save(captor.capture());
        assertEquals(estadoAnteriorId, captor.getValue().getEstadoAnteriorId());
        assertEquals(retired.getId().getValor(), captor.getValue().getEstadoNovoId());

        assertEquals(contratoId, resultado.contratoCessadoId());
        assertEquals(afectacao.getId().getValor(), resultado.afectacaoEncerradaId());
        assertEquals(dataEfeito, resultado.dataEfeito());
    }

    @Test
    void cessaSemContratoNemAfectacao() {
        WorkerState inactive = estado("INACTIVE", true);
        funcionarioMock();
        when(contratoRepository.findCurrentByFuncionarioId(funcionarioId)).thenReturn(Optional.empty());
        when(assignmentService.encerrarAfectacaoCorrente(funcionarioId, dataEfeito)).thenReturn(Optional.empty());

        var resultado = service.cessar(funcionarioId, inactive, dataEfeito, null, null);

        assertNull(resultado.contratoCessadoId());
        assertNull(resultado.afectacaoEncerradaId());
        verify(historicoRepository).save(any(HistoricoEstadoColaborador.class));
    }

    /** BR-AF-28: quem cessa já não vai ocupar o Lugar que lhe estava reservado. */
    @Test
    void cessarCancelaOLugarReservado() {
        WorkerState inactive = estado("INACTIVE", true);
        funcionarioMock();
        when(contratoRepository.findCurrentByFuncionarioId(funcionarioId)).thenReturn(Optional.empty());
        when(assignmentService.encerrarAfectacaoCorrente(funcionarioId, dataEfeito)).thenReturn(Optional.empty());
        var reserva = cv.igrp.RH_Service.colaboradores.domain.models.ReservaLugar.reservar(funcionarioId, UUID.randomUUID(),
                null, null, null, dataEfeito.minusMonths(1));
        when(reservaLugarRepository.findActivaByFuncionario(funcionarioId)).thenReturn(Optional.of(reserva));

        service.cessar(funcionarioId, inactive, dataEfeito, null, null);

        assertEquals(cv.igrp.RH_Service.colaboradores.domain.models.ReservaLugar.Estado.CANCELADA, reserva.getEstado());
        verify(reservaLugarRepository).save(reserva);
    }

    @Test
    void naoCessaContratoJaCessado() {
        WorkerState inactive = estado("INACTIVE", true);
        funcionarioMock();
        Contrato contrato = mock(Contrato.class);
        when(contrato.getStatus()).thenReturn(EstadoContrato.CESSADO);
        when(contrato.getId()).thenReturn(ContratoId.from(UUID.randomUUID()));
        when(contratoRepository.findCurrentByFuncionarioId(funcionarioId)).thenReturn(Optional.of(contrato));
        when(assignmentService.encerrarAfectacaoCorrente(funcionarioId, dataEfeito)).thenReturn(Optional.empty());

        service.cessar(funcionarioId, inactive, dataEfeito, null, null);

        verify(contrato, never()).encerrar(any(), any());
        verify(contratoRepository, never()).save(any());
    }

    @Test
    void recusaEstadoQueNaoCessaOVinculo() {
        WorkerState suspended = estado("SUSPENDED", false);

        var ex = assertThrows(IgrpResponseStatusException.class,
                () -> service.cessar(funcionarioId, suspended, dataEfeito, null, null));

        assertEquals(422, ex.getStatusCode().value());
        verify(funcionarioRepository, never()).save(any());
        verify(historicoRepository, never()).save(any());
    }

    @Test
    void estadoPorOmissaoPrefereInactive() {
        when(workerStateRepository.findAllEndingEmployment())
                .thenReturn(List.of(estado("RETIRED", true), estado("INACTIVE", true)));

        assertEquals("INACTIVE", service.estadoDeCessacaoPorOmissao().getCode());
    }

    @Test
    void estadoPorOmissaoUsaOPrimeiroQuandoNaoHaInactive() {
        when(workerStateRepository.findAllEndingEmployment())
                .thenReturn(List.of(estado("DEMITIDO", true)));

        assertEquals("DEMITIDO", service.estadoDeCessacaoPorOmissao().getCode());
    }

    @Test
    void semEstadoDeCessacaoConfiguradoFalha() {
        when(workerStateRepository.findAllEndingEmployment()).thenReturn(List.of());

        var ex = assertThrows(IgrpResponseStatusException.class, () -> service.estadoDeCessacaoPorOmissao());

        assertEquals(422, ex.getStatusCode().value());
    }

    @Test
    void funcionarioInexistenteFalha() {
        WorkerState inactive = estado("INACTIVE", true);
        when(funcionarioRepository.findById(funcionarioId)).thenReturn(Optional.empty());

        var ex = assertThrows(IgrpResponseStatusException.class,
                () -> service.cessar(funcionarioId, inactive, dataEfeito, null, null));

        assertEquals(404, ex.getStatusCode().value());
        verify(assignmentService, never()).encerrarAfectacaoCorrente(any(), eq(dataEfeito));
    }
}
