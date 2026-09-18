package cv.igrp.RH_Service.colaboradores.application.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cv.igrp.RH_Service.colaboradores.domain.models.Assignment;
import cv.igrp.RH_Service.colaboradores.domain.models.Funcionario;
import cv.igrp.RH_Service.colaboradores.domain.models.HistoricoEstadoColaborador;
import cv.igrp.RH_Service.colaboradores.domain.models.LicencaMobilidade;
import cv.igrp.RH_Service.colaboradores.domain.models.SubtipoLicencaMobilidade;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.HistoricoEstadoColaboradorRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.AssignmentId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.SubtipoLicencaMobilidadeId;
import cv.igrp.RH_Service.parametrizacoes.domain.models.SituacaoFuncional;
import cv.igrp.RH_Service.parametrizacoes.domain.models.WorkerState;
import cv.igrp.RH_Service.parametrizacoes.domain.repository.WorkerStateRepository;
import cv.igrp.RH_Service.parametrizacoes.domain.valueobject.WorkerStateId;

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
 * Licenças que abrem vaga (DL n.º 3/2010) e o regresso pela disponibilidade
 * (Lei n.º 20/X/2023, art. 122.º).
 *
 * <p>Qual licença abre vaga, e a partir de que prazo, é <b>configuração do subtipo</b> —
 * a lei faz variar o prazo com o motivo. O serviço só aplica o que o catálogo diz.
 */
@ExtendWith(MockitoExtension.class)
class LicencaAbreVagaTest {

    private static final FuncionarioId FUNCIONARIO = FuncionarioId.gerarNovo();
    private static final LocalDate INICIO = LocalDate.of(2026, 10, 1);

    @Mock private AssignmentService assignmentService;
    @Mock private FuncionarioRepository funcionarioRepository;
    @Mock private WorkerStateRepository workerStateRepository;
    @Mock private HistoricoEstadoColaboradorRepository historicoRepository;

    @InjectMocks private LicencaService service;

    private static SubtipoLicencaMobilidade subtipo(String codigo, String recordType, String efeitoLugar,
                                                    Integer aposDias, String efeitoRegresso) {
        return SubtipoLicencaMobilidade.reconstituir(SubtipoLicencaMobilidadeId.gerarNovo(),
                codigo, codigo, recordType, false, false, false, true, null, null,
                efeitoLugar, aposDias, efeitoRegresso);
    }

    private static LicencaMobilidade licenca(LocalDate fim) {
        return LicencaMobilidade.criar(FUNCIONARIO, SubtipoLicencaMobilidadeId.gerarNovo(),
                INICIO, fim, null, "DESP/2026", null, null, null, null, null);
    }

    private WorkerState estadoNoCatalogo(SituacaoFuncional situacao) {
        WorkerState estado = WorkerState.reconstruir(WorkerStateId.gerarNovo(), situacao.name(),
                situacao.name(), false, true, false, situacao);
        when(workerStateRepository.findBySituacao(situacao)).thenReturn(Optional.of(estado));
        return estado;
    }

    private Funcionario funcionarioNoRepositorio() {
        Funcionario funcionario = mock(Funcionario.class);
        when(funcionario.getWorkerStateId()).thenReturn(UUID.randomUUID());
        when(funcionarioRepository.findById(FUNCIONARIO)).thenReturn(Optional.of(funcionario));
        return funcionario;
    }

    @Nested
    class OPrazoVemDoCatalogo {

        @Test
        void licencaDeLongaDuracaoAbreVagaLogo() {
            var s = subtipo("LIC_LONGA_DURACAO", "LICENCA", "ABRE_VAGA", null, "DISPONIBILIDADE");
            assertTrue(s.abreVaga(30L));
            assertTrue(s.abreVaga(null));
        }

        @Test
        void formacaoSoAbreVagaAlemDeSeisMeses() {
            // Art. 67.º n.º 3 e Lei 20/X/2023, art. 118.º n.º 2.
            var s = subtipo("LIC_FORMACAO", "LICENCA", "ABRE_VAGA", 180, "DISPONIBILIDADE");
            assertFalse(s.abreVaga(180L));
            assertTrue(s.abreVaga(181L));
        }

        @Test
        void conjugeNoEstrangeiroSoAbreVagaAlemDeUmAno() {
            // Art. 56.º n.º 2.
            var s = subtipo("LIC_ACOMP_CONJUGE", "LICENCA", "ABRE_VAGA", 365, "DISPONIBILIDADE");
            assertFalse(s.abreVaga(365L));
            assertTrue(s.abreVaga(366L));
        }

        @Test
        void licencaCurtaMantemOLugar() {
            // Art. 46.º e 48.º.
            var s = subtipo("LIC_SEM_VENCIMENTO", "LICENCA", "MANTEM", null, "REGRESSA_LUGAR");
            assertFalse(s.abreVaga(1000L));
        }

        @Test
        void periodoAbertoUltrapassaQualquerPrazo() {
            var s = subtipo("LIC_FORMACAO", "LICENCA", "ABRE_VAGA", 180, "DISPONIBILIDADE");
            assertTrue(s.abreVaga(null));
        }
    }

    @Nested
    class EntradaEmVigor {

        @Test
        void licencaQueAbreVagaEncerraAAfectacaoEPassaAInactividadeForaDoQuadro() {
            var s = subtipo("LIC_LONGA_DURACAO", "LICENCA", "ABRE_VAGA", null, "DISPONIBILIDADE");
            var l = licenca(LocalDate.of(2027, 10, 1));

            Assignment afectacao = mock(Assignment.class);
            UUID afectacaoId = UUID.randomUUID();
            when(afectacao.getId()).thenReturn(AssignmentId.from(afectacaoId));
            when(assignmentService.encerrarAfectacaoCorrente(FUNCIONARIO, INICIO))
                    .thenReturn(Optional.of(afectacao));
            var estado = estadoNoCatalogo(SituacaoFuncional.INACTIVIDADE_FORA_QUADRO);
            Funcionario funcionario = funcionarioNoRepositorio();

            var efeito = service.aplicarEntradaEmVigor(l, s);

            assertEquals(afectacaoId, efeito.afectacaoEncerradaId());
            assertEquals(estado.getId().getValor(), efeito.estadoAtribuidoId());
            verify(funcionario).atualizarWorkerState(estado.getId().getValor(), true);
            verify(historicoRepository).save(any(HistoricoEstadoColaborador.class));
        }

        @Test
        void licencaQueMantemOLugarNaoTocaEmNada() {
            var s = subtipo("LIC_SEM_VENCIMENTO", "LICENCA", "MANTEM", null, "REGRESSA_LUGAR");

            var efeito = service.aplicarEntradaEmVigor(licenca(LocalDate.of(2026, 12, 1)), s);

            assertEquals(null, efeito.afectacaoEncerradaId());
            verify(assignmentService, never()).encerrarAfectacaoCorrente(any(), any());
        }

        @Test
        void mobilidadeNuncaAbreVaga() {
            // Art. 135.º n.º 7: sem ocupação do lugar do quadro.
            var s = subtipo("MOB_COMISSAO", "MOBILIDADE", "ABRE_VAGA", null, "REGRESSA_LUGAR");

            var efeito = service.aplicarEntradaEmVigor(licenca(LocalDate.of(2027, 1, 1)), s);

            assertEquals(null, efeito.afectacaoEncerradaId());
            verify(assignmentService, never()).encerrarAfectacaoCorrente(any(), any());
        }

        @Test
        void semEstadoConfiguradoNoCatalogoAVagaAbreNaMesma() {
            var s = subtipo("LIC_LONGA_DURACAO", "LICENCA", "ABRE_VAGA", null, "DISPONIBILIDADE");
            Assignment afectacao = mock(Assignment.class);
            when(afectacao.getId()).thenReturn(AssignmentId.from(UUID.randomUUID()));
            when(assignmentService.encerrarAfectacaoCorrente(any(), any())).thenReturn(Optional.of(afectacao));
            when(workerStateRepository.findBySituacao(SituacaoFuncional.INACTIVIDADE_FORA_QUADRO))
                    .thenReturn(Optional.empty());

            var efeito = service.aplicarEntradaEmVigor(licenca(null), s);

            assertEquals(null, efeito.estadoAtribuidoId());
            verify(historicoRepository, never()).save(any());
        }
    }

    @Nested
    class Regresso {

        @Test
        void quemPerdeuOLugarFicaNaDisponibilidade() {
            var s = subtipo("LIC_LONGA_DURACAO", "LICENCA", "ABRE_VAGA", null, "DISPONIBILIDADE");
            var estado = estadoNoCatalogo(SituacaoFuncional.DISPONIBILIDADE);
            Funcionario funcionario = funcionarioNoRepositorio();

            var atribuido = service.aplicarRegresso(licenca(null), s, LocalDate.of(2027, 10, 1));

            assertTrue(atribuido.isPresent());
            assertEquals(estado.getId().getValor(), atribuido.get());
            verify(funcionario).atualizarWorkerState(estado.getId().getValor(), true);
        }

        @Test
        void quemManteveOLugarRegressaSemMudarDeEstado() {
            var s = subtipo("LIC_SEM_VENCIMENTO", "LICENCA", "MANTEM", null, "REGRESSA_LUGAR");

            var atribuido = service.aplicarRegresso(licenca(null), s, LocalDate.of(2026, 12, 1));

            assertTrue(atribuido.isEmpty());
            verify(historicoRepository, never()).save(any());
        }
    }
}
