package cv.igrp.RH_Service.colaboradores.application.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cv.igrp.RH_Service.colaboradores.application.dto.MudarEstadoColaboradorRequestDTO;
import cv.igrp.RH_Service.colaboradores.application.services.CessacaoService;
import cv.igrp.RH_Service.colaboradores.domain.models.Contrato;
import cv.igrp.RH_Service.colaboradores.domain.models.EstadoContrato;
import cv.igrp.RH_Service.colaboradores.domain.models.Funcionario;
import cv.igrp.RH_Service.colaboradores.domain.models.HistoricoEstadoColaborador;
import cv.igrp.RH_Service.colaboradores.domain.repository.ContratoRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.HistoricoEstadoColaboradorRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.ContratoId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.application.services.AssignmentService;
import cv.igrp.RH_Service.parametrizacoes.domain.models.SituacaoFuncional;
import cv.igrp.RH_Service.parametrizacoes.domain.models.WorkerState;
import cv.igrp.RH_Service.parametrizacoes.domain.repository.WorkerStateRepository;
import cv.igrp.RH_Service.parametrizacoes.domain.valueobject.WorkerStateId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Prova de que os <b>dois caminhos</b> da cessação — mudar para um estado de cessação e encerrar
 * o contrato — passam pelo mesmo {@link CessacaoService} e devolvem o mesmo resultado. Antes,
 * o encerramento de contrato deixava o trabalhador ACTIVE, sem contrato nem Lugar, e sem
 * histórico.
 */
@ExtendWith(MockitoExtension.class)
class CessacaoPelosDoisCaminhosTest {

    private static final UUID FUNCIONARIO = UUID.randomUUID();
    private static final LocalDate DATA = LocalDate.of(2026, 10, 31);
    private static final FuncionarioId FUNCIONARIO_ID = FuncionarioId.from(FUNCIONARIO);

    private static WorkerState estado(String code, boolean cessa) {
        return WorkerState.reconstruir(WorkerStateId.gerarNovo(), code, code, false, true, cessa);
    }

    private static WorkerState estado(String code, boolean cessa, SituacaoFuncional situacao) {
        return WorkerState.reconstruir(WorkerStateId.gerarNovo(), code, code, false, true, cessa, situacao);
    }

    private static CessacaoService.Cessacao resultado(UUID estadoAnteriorId) {
        return new CessacaoService.Cessacao(mock(Funcionario.class), estado("INACTIVE", true),
                estadoAnteriorId, UUID.randomUUID(), UUID.randomUUID(), DATA);
    }

    @Nested
    @ExtendWith(MockitoExtension.class)
    class PorMudancaDeEstado {

        @Mock private FuncionarioRepository funcionarioRepository;
        @Mock private WorkerStateRepository workerStateRepository;
        @Mock private ContratoRepository contratoRepository;
        @Mock private HistoricoEstadoColaboradorRepository historicoRepository;
        @Mock private CessacaoService cessacaoService;
        @Mock private AssignmentService assignmentService;

        @InjectMocks private MudarEstadoColaboradorCommandHandler handler;

        private MudarEstadoColaboradorCommand command(WorkerState novoEstado) {
            var dto = new MudarEstadoColaboradorRequestDTO();
            dto.setWorkerStateId(novoEstado.getId().getStringValor());
            dto.setDataEfectividade(DATA);
            dto.setMotivoCkey("AGE_RETIREMENT");
            return new MudarEstadoColaboradorCommand(FUNCIONARIO.toString(), dto);
        }

        private void cenario(WorkerState novoEstado, UUID estadoAnteriorId) {
            Funcionario funcionario = mock(Funcionario.class);
            when(funcionario.getWorkerStateId()).thenReturn(estadoAnteriorId);
            when(funcionarioRepository.findById(FUNCIONARIO_ID)).thenReturn(Optional.of(funcionario));
            when(workerStateRepository.findById(any())).thenReturn(Optional.of(novoEstado));
        }

        @Test
        void estadoDeCessacaoDelegaNoServicoUnico() {
            WorkerState retired = estado("RETIRED", true);
            UUID anterior = UUID.randomUUID();
            cenario(retired, anterior);
            when(cessacaoService.cessar(eq(FUNCIONARIO_ID), eq(retired), eq(DATA), eq("AGE_RETIREMENT"), isNull()))
                    .thenReturn(resultado(anterior));

            var response = handler.handle(command(retired));

            assertEquals(200, response.getStatusCode().value());
            assertTrue(response.getBody().getCessouVinculo());
            assertEquals("RETIRED", response.getBody().getEstadoNovoCode());
            // O handler não duplica os efeitos: quem os aplica é o serviço.
            verify(historicoRepository, never()).save(any());
            verify(funcionarioRepository, never()).save(any());
        }

        @Test
        void estadoQueNaoCessaSuspendeOContratoENaoChamaOServico() {
            WorkerState suspended = estado("SUSPENDED", false, SituacaoFuncional.INACTIVIDADE_NO_QUADRO);
            cenario(suspended, UUID.randomUUID());
            Contrato contrato = mock(Contrato.class);
            when(contrato.getStatus()).thenReturn(EstadoContrato.ATIVO);
            when(contrato.getId()).thenReturn(ContratoId.from(UUID.randomUUID()));
            when(contratoRepository.findCurrentByFuncionarioId(FUNCIONARIO_ID)).thenReturn(Optional.of(contrato));

            var response = handler.handle(command(suspended));

            assertFalse(response.getBody().getCessouVinculo());
            verify(contrato).suspender();
            verify(historicoRepository).save(any(HistoricoEstadoColaborador.class));
            verify(cessacaoService, never()).cessar(any(), any(), any(), any(), any());
        }

        @Test
        void recusaEstadoInactivoNoCatalogo() {
            WorkerState inactivoNoCatalogo = WorkerState.reconstruir(WorkerStateId.gerarNovo(),
                    "OBSOLETO", "Obsoleto", false, false, false);
            when(funcionarioRepository.findById(FUNCIONARIO_ID)).thenReturn(Optional.of(mock(Funcionario.class)));
            when(workerStateRepository.findById(any())).thenReturn(Optional.of(inactivoNoCatalogo));

            var ex = assertThrows(IgrpResponseStatusException.class,
                    () -> handler.handle(command(inactivoNoCatalogo)));

            assertEquals(409, ex.getStatusCode().value());
        }
    }

    @Nested
    @ExtendWith(MockitoExtension.class)
    class PorEncerramentoDeContrato {

        @Mock private ContratoRepository contratoRepository;
        @Mock private CessacaoService cessacaoService;

        @InjectMocks private CloseContratoCommandHandler handler;

        private CloseContratoCommand command(LocalDate endDate, String motivo) {
            return new CloseContratoCommand(UUID.randomUUID().toString(), endDate, motivo, null);
        }

        @Test
        void encerrarContratoCessaOVinculoPeloMesmoServico() {
            WorkerState inactive = estado("INACTIVE", true);
            Contrato contrato = mock(Contrato.class);
            when(contrato.getFuncionarioId()).thenReturn(FUNCIONARIO_ID);
            when(contratoRepository.findById(any())).thenReturn(Optional.of(contrato));
            when(cessacaoService.estadoDeCessacaoPorOmissao()).thenReturn(inactive);
            UUID anterior = UUID.randomUUID();
            when(cessacaoService.cessar(eq(FUNCIONARIO_ID), eq(inactive), eq(DATA), eq("CONTRACT_TERMINATION"), isNull()))
                    .thenReturn(resultado(anterior));

            var response = handler.handle(command(DATA, "CONTRACT_TERMINATION"));

            assertEquals(200, response.getStatusCode().value());
            assertTrue(response.getBody().getCessouVinculo());
            assertEquals("INACTIVE", response.getBody().getEstadoNovoCode());
            assertEquals(FUNCIONARIO.toString(), response.getBody().getFuncionarioId());
        }

        @Test
        void recusaSemDataDeFim() {
            var ex = assertThrows(IgrpResponseStatusException.class,
                    () -> handler.handle(command(null, "MOTIVO")));

            assertEquals(400, ex.getStatusCode().value());
        }

        @Test
        void recusaSemMotivo() {
            var ex = assertThrows(IgrpResponseStatusException.class,
                    () -> handler.handle(command(DATA, "  ")));

            assertEquals(400, ex.getStatusCode().value());
        }
    }
}
