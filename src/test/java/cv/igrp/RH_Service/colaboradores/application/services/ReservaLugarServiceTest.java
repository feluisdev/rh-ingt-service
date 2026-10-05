package cv.igrp.RH_Service.colaboradores.application.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cv.igrp.RH_Service.colaboradores.domain.models.Assignment;
import cv.igrp.RH_Service.colaboradores.domain.models.Contrato;
import cv.igrp.RH_Service.colaboradores.domain.models.Funcionario;
import cv.igrp.RH_Service.colaboradores.domain.models.ReservaLugar;
import cv.igrp.RH_Service.colaboradores.domain.models.TipoAfectacao;
import cv.igrp.RH_Service.colaboradores.domain.repository.AssignmentRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.ContratoRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.ReservaLugarRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.ContratoId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.estrutura.domain.models.Position;
import cv.igrp.RH_Service.estrutura.domain.repository.PositionRepository;
import cv.igrp.RH_Service.estrutura.domain.valueobject.PositionId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;

/** BR-AF-23 a BR-AF-26: o Lugar reservado para quem foi registado sem contrato. */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ReservaLugarServiceTest {

    @Mock ReservaLugarRepository reservaRepository;
    @Mock FuncionarioRepository funcionarioRepository;
    @Mock AssignmentRepository assignmentRepository;
    @Mock ContratoRepository contratoRepository;
    @Mock PositionRepository positionRepository;
    @Mock AssignmentService assignmentService;
    @Mock ColocacaoService colocacaoService;
    @InjectMocks ReservaLugarService service;

    private final FuncionarioId funcionarioId = FuncionarioId.gerarNovo();
    private final UUID lugarId = UUID.randomUUID();
    private final LocalDate admissao = LocalDate.of(2026, 11, 1);

    @BeforeEach
    void colaboradorRegistadoSemContrato() {
        funcionario(true);
        when(contratoRepository.findCurrentByFuncionarioId(funcionarioId)).thenReturn(Optional.empty());
        when(assignmentRepository.findCurrentPrincipalByFuncionario(funcionarioId)).thenReturn(Optional.empty());
        when(reservaRepository.findActivaByFuncionario(funcionarioId)).thenReturn(Optional.empty());
        when(reservaRepository.findActivaByPosition(lugarId)).thenReturn(Optional.empty());
        when(assignmentRepository.temTitular(lugarId)).thenReturn(false);
        when(assignmentService.validarLugarParaAfectacao(eq(lugarId), any(), any())).thenReturn(lugar());
        when(positionRepository.findById(PositionId.from(lugarId))).thenReturn(Optional.of(lugar()));
        when(reservaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    // ------------------------------------------------------------------ reservar

    @Test
    void reservaOLugarSemOOcupar() {
        ReservaLugar r = reservar();

        assertTrue(r.activa());
        assertEquals(lugarId, r.getPositionId());
        verify(reservaRepository).save(any());
        verify(assignmentService, never()).afectar(any(), any(), any(), any(), any(), any(), any(), any());
        verify(colocacaoService, never()).colocar(any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void naoSeReservaUmLugarComTitular() {
        when(assignmentRepository.temTitular(lugarId)).thenReturn(true);
        assert422(this::reservar);
        verify(reservaRepository, never()).save(any());
    }

    @Test
    void naoSeReservaUmLugarJaReservado() {
        when(reservaRepository.findActivaByPosition(lugarId))
                .thenReturn(Optional.of(ReservaLugar.reservar(FuncionarioId.gerarNovo(), lugarId, null, null, null, admissao)));
        assert422(this::reservar);
        verify(reservaRepository, never()).save(any());
    }

    @Test
    void oLugarPassaPelasValidacoesDaColocacao() {
        // congelado, escalão de outra categoria, função de outro cargo: a mesma validação de sempre
        when(assignmentService.validarLugarParaAfectacao(eq(lugarId), any(), any()))
                .thenThrow(IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY, "Lugar indisponível."));
        assert422(this::reservar);
        verify(reservaRepository, never()).save(any());
    }

    @Test
    void comContratoEmVigorColocaSeDirectamente() {
        when(contratoRepository.findCurrentByFuncionarioId(funcionarioId)).thenReturn(Optional.of(contrato(admissao)));
        assert422(this::reservar);
    }

    @Test
    void quemJaTemLugarNaoReserva() {
        when(assignmentRepository.findCurrentPrincipalByFuncionario(funcionarioId)).thenReturn(Optional.of(
                Assignment.criar(funcionarioId, UUID.randomUUID(), null, null, TipoAfectacao.PRINCIPAL,
                        Assignment.ADMISSAO, admissao, null)));
        assert422(this::reservar);
    }

    @Test
    void umaReservaDeCadaVez() {
        when(reservaRepository.findActivaByFuncionario(funcionarioId))
                .thenReturn(Optional.of(ReservaLugar.reservar(funcionarioId, UUID.randomUUID(), null, null, null, admissao)));
        assert422(this::reservar);
    }

    @Test
    void inactivoNaoReserva() {
        funcionario(false);
        assert422(this::reservar);
    }

    @Test
    void aSubstituicaoNaoSeReserva() {
        assert422(() -> service.reservar(funcionarioId, lugarId, null, null, TipoAfectacao.SUBSTITUICAO, null));
    }

    // ------------------------------------------------------------------ concretizar

    @Test
    void semReservaOContratoNaoFazMaisNada() {
        assertTrue(service.concretizar(funcionarioId, contrato(admissao)).isEmpty());
        verify(colocacaoService, never()).colocar(any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void oContratoOcupaOLugarReservadoAPartirDoInicioDoContrato() {
        reservaActiva();
        LocalDate inicioContrato = admissao.plusMonths(1);
        when(colocacaoService.colocar(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(new ColocacaoService.Resultado(null, Assignment.ADMISSAO, List.of()));

        List<String> alertas = service.concretizar(funcionarioId, contrato(inicioContrato));

        verify(colocacaoService).colocar(eq(funcionarioId), eq(lugarId), any(), any(), eq(null),
                eq(TipoAfectacao.PRINCIPAL), eq(inicioContrato), any());
        assertEquals(1, alertas.size());
    }

    @Test
    void contratoAnteriorAAdmissaoColocaNaAdmissao() {
        reservaActiva();
        when(colocacaoService.colocar(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(new ColocacaoService.Resultado(null, Assignment.ADMISSAO, List.of()));

        service.concretizar(funcionarioId, contrato(admissao.minusMonths(3)));

        ArgumentCaptor<LocalDate> inicio = ArgumentCaptor.forClass(LocalDate.class);
        verify(colocacaoService).colocar(any(), any(), any(), any(), any(), any(), inicio.capture(), any());
        assertEquals(admissao, inicio.getValue());
    }

    @Test
    void seAColocacaoFalharOContratoFicaEAvisa() {
        reservaActiva();
        when(colocacaoService.colocar(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenThrow(IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY, "O Lugar foi congelado."));

        List<String> alertas = service.concretizar(funcionarioId, contrato(admissao));

        assertEquals(1, alertas.size());
        assertTrue(alertas.get(0).contains("O Lugar foi congelado."));
    }

    // ------------------------------------------------------------------ cancelar

    @Test
    void cancelarLibertaOLugar() {
        ReservaLugar r = reservaActiva();
        service.cancelar(funcionarioId, "Desistiu.");
        assertEquals(ReservaLugar.Estado.CANCELADA, r.getEstado());
        assertEquals("Desistiu.", r.getMotivo());
        verify(reservaRepository).save(r);
    }

    @Test
    void cancelarExigeReservaEMotivo() {
        assert422(() -> service.cancelar(funcionarioId, "x"));
        reservaActiva();
        IgrpResponseStatusException ex = assertThrows(IgrpResponseStatusException.class,
                () -> service.cancelar(funcionarioId, " "));
        assertEquals(HttpStatus.BAD_REQUEST.value(), ex.getStatusCode().value());
    }

    @Test
    void umaReservaFechadaNaoMudaMais() {
        ReservaLugar r = ReservaLugar.reservar(funcionarioId, lugarId, null, null, null, admissao);
        r.concluir(UUID.randomUUID(), admissao);
        IgrpResponseStatusException ex = assertThrows(IgrpResponseStatusException.class, () -> r.cancelar("x", admissao));
        assertEquals(HttpStatus.CONFLICT.value(), ex.getStatusCode().value());
    }

    // ------------------------------------------------------------------ apoio

    private ReservaLugar reservar() {
        return service.reservar(funcionarioId, lugarId, null, null, TipoAfectacao.PRINCIPAL, null);
    }

    private ReservaLugar reservaActiva() {
        ReservaLugar r = ReservaLugar.reservar(funcionarioId, lugarId, null, null, null, admissao);
        when(reservaRepository.findActivaByFuncionario(funcionarioId)).thenReturn(Optional.of(r));
        return r;
    }

    private static void assert422(org.junit.jupiter.api.function.Executable e) {
        IgrpResponseStatusException ex = assertThrows(IgrpResponseStatusException.class, e);
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.value(), ex.getStatusCode().value());
    }

    private void funcionario(boolean activo) {
        when(funcionarioRepository.findById(funcionarioId)).thenReturn(Optional.of(Funcionario.reconstituir(
                funcionarioId, "F000001", "Teste", LocalDate.of(1990, 1, 1), "F", "SOLTEIRO", "123", null, null,
                null, null, "CV", null, null, null, null, null, null, UUID.randomUUID(), admissao, activo)));
    }

    private Contrato contrato(LocalDate inicio) {
        return Contrato.reconstituir(ContratoId.gerarNovo(), funcionarioId, UUID.randomUUID(), "C-1", inicio, null, null,
                true, "ATIVO", 0, "TEMPO_COMPLETO", null, null, null);
    }

    private Position lugar() {
        return Position.reconstituir(PositionId.from(lugarId), "L-01", UUID.randomUUID(), UUID.randomUUID(),
                null, null, null, null, Position.ATIVO, null, true);
    }
}
