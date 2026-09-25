package cv.igrp.RH_Service.colaboradores.application.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cv.igrp.RH_Service.carreiras.domain.repository.CareerRepository;
import cv.igrp.RH_Service.carreiras.domain.repository.CategoryRepository;
import cv.igrp.RH_Service.carreiras.domain.repository.GradeRepository;
import cv.igrp.RH_Service.colaboradores.domain.models.Assignment;
import cv.igrp.RH_Service.colaboradores.domain.models.LicencaMobilidade;
import cv.igrp.RH_Service.colaboradores.domain.models.TipoAfectacao;
import cv.igrp.RH_Service.colaboradores.domain.repository.AssignmentRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.AssignmentId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.SubtipoLicencaMobilidadeId;
import cv.igrp.RH_Service.estrutura.domain.models.Position;
import cv.igrp.RH_Service.estrutura.domain.repository.FunctionRepository;
import cv.igrp.RH_Service.estrutura.domain.repository.PositionRepository;
import cv.igrp.RH_Service.estrutura.domain.valueobject.PositionId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Prova da consolidação da mobilidade — Lei n.º 20/X/2023, art. 132.º n.º 4: «A mobilidade
 * definitiva ocorre nos casos de consolidação da mobilidade transitória, <b>na mesma função e
 * categoria</b>.»
 *
 * <p>É a única via pela qual uma mobilidade toca na afectação, e não contradiz o art. 135.º
 * n.º 7: o que esse número diz é que a mobilidade <b>transitória</b> não ocupa Lugar. A
 * definitiva, pelo n.º 8, é precisamente a que é feita «com ocupação do lugar do quadro» — e a
 * consolidação é a passagem de uma à outra.
 */
@ExtendWith(MockitoExtension.class)
class ConsolidacaoMobilidadeTest {

    @Mock private AssignmentRepository assignmentRepository;
    @Mock private PositionRepository positionRepository;
    @Mock private GradeRepository gradeRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private CareerRepository careerRepository;
    @Mock private FunctionRepository functionRepository;

    @org.mockito.Mock private cv.igrp.RH_Service.colaboradores.application.services.DiarioFactos diarioFactos;
    @InjectMocks private AssignmentService service;

    private final FuncionarioId funcionarioId = FuncionarioId.gerarNovo();
    private final UUID origemId = UUID.randomUUID();
    private final UUID destinoId = UUID.randomUUID();
    private final UUID job = UUID.randomUUID();
    private final UUID categoria = UUID.randomUUID();
    private final UUID carreira = UUID.randomUUID();
    private final UUID grade = UUID.randomUUID();
    private final UUID unidadeOrigem = UUID.randomUUID();
    private final UUID unidadeDestino = UUID.randomUUID();
    private final LocalDate inicio = LocalDate.of(2026, 1, 1);
    private final LocalDate dataEfeito = LocalDate.of(2026, 10, 1);

    private Assignment afectacaoActual() {
        return Assignment.reconstituir(AssignmentId.gerarNovo(), funcionarioId, origemId, grade,
                null, TipoAfectacao.PRINCIPAL.name(), Assignment.ADMISSAO, null, inicio, null,
                true, true, null);
    }

    private Position lugar(UUID id, UUID jobId, UUID categoryId, UUID unidade, String estado) {
        return Position.reconstituir(PositionId.from(id), "L-" + id.toString().substring(0, 4), jobId,
                unidade, carreira, categoryId, null, null, estado, null, true);
    }

    private void cenario(Position destino) {
        when(assignmentRepository.findCurrentPrincipalByFuncionario(funcionarioId))
                .thenReturn(Optional.of(afectacaoActual()));
        when(positionRepository.findById(PositionId.from(origemId)))
                .thenReturn(Optional.of(lugar(origemId, job, categoria, unidadeOrigem, Position.ATIVO)));
        when(positionRepository.findById(PositionId.from(destinoId))).thenReturn(Optional.of(destino));
    }

    @Nested
    class OMovimento {

        @Test
        void consolidaParaUmLugarVagoDoServicoDeDestino() {
            cenario(lugar(destinoId, job, categoria, unidadeDestino, Position.ATIVO));
            when(assignmentRepository.temTitular(destinoId)).thenReturn(false);
            when(assignmentRepository.save(any(Assignment.class))).thenAnswer(i -> i.getArgument(0));

            var resultado = service.consolidarMobilidade(funcionarioId, destinoId, unidadeDestino,
                    dataEfeito, "Consolidação");

            ArgumentCaptor<Assignment> captor = ArgumentCaptor.forClass(Assignment.class);
            verify(assignmentRepository, times(2)).save(captor.capture());
            Assignment fechada = captor.getAllValues().get(0);
            Assignment nova = captor.getAllValues().get(1);

            assertFalse(fechada.getIsCurrent());
            assertEquals(dataEfeito.minusDays(1), fechada.getDataFim());
            assertEquals(destinoId, nova.getPositionId());
            assertEquals(Assignment.CONSOLIDACAO, nova.getOrigem());
            assertEquals(origemId, resultado.lugarAnterior().getId().getValor());
            assertEquals(destinoId, resultado.lugarNovo().getId().getValor());
        }

        /** Não há evolução na grelha numa consolidação: é a mesma categoria, logo o mesmo escalão. */
        @Test
        void oEscalaoMantemSe() {
            cenario(lugar(destinoId, job, categoria, unidadeDestino, Position.ATIVO));
            when(assignmentRepository.temTitular(destinoId)).thenReturn(false);
            when(assignmentRepository.save(any(Assignment.class))).thenAnswer(i -> i.getArgument(0));

            service.consolidarMobilidade(funcionarioId, destinoId, unidadeDestino, dataEfeito, null);

            ArgumentCaptor<Assignment> captor = ArgumentCaptor.forClass(Assignment.class);
            verify(assignmentRepository, times(2)).save(captor.capture());
            assertEquals(grade, captor.getAllValues().get(1).getGradeId());
        }
    }

    @Nested
    class NaMesmaFuncaoECategoria {

        /** Art. 132.º n.º 4. Mudar de categoria exigiria concurso comum interno (art. 135.º n.º 6). */
        @Test
        void recusaCategoriaDiferente() {
            cenario(lugar(destinoId, job, UUID.randomUUID(), unidadeDestino, Position.ATIVO));
            when(assignmentRepository.temTitular(destinoId)).thenReturn(false);

            var erro = assertThrows(IgrpResponseStatusException.class,
                    () -> service.consolidarMobilidade(funcionarioId, destinoId, unidadeDestino, dataEfeito, null));

            assertEquals(422, erro.getStatusCode().value());
            verify(assignmentRepository, never()).save(any());
        }

        @Test
        void recusaCargoDiferente() {
            cenario(lugar(destinoId, UUID.randomUUID(), categoria, unidadeDestino, Position.ATIVO));
            when(assignmentRepository.temTitular(destinoId)).thenReturn(false);

            var erro = assertThrows(IgrpResponseStatusException.class,
                    () -> service.consolidarMobilidade(funcionarioId, destinoId, unidadeDestino, dataEfeito, null));

            assertEquals(422, erro.getStatusCode().value());
            verify(assignmentRepository, never()).save(any());
        }
    }

    @Nested
    class OLugarDeDestino {

        /** Consolida-se onde se esteve em mobilidade, não num serviço qualquer. */
        @Test
        void temDeSerDaUnidadeOndeEsteveEmMobilidade() {
            cenario(lugar(destinoId, job, categoria, UUID.randomUUID(), Position.ATIVO));
            when(assignmentRepository.temTitular(destinoId)).thenReturn(false);

            var erro = assertThrows(IgrpResponseStatusException.class,
                    () -> service.consolidarMobilidade(funcionarioId, destinoId, unidadeDestino, dataEfeito, null));

            assertEquals(422, erro.getStatusCode().value());
            verify(assignmentRepository, never()).save(any());
        }

        /** Art. 134.º n.º 1 al. a): «para outro lugar vago do quadro de outro serviço». */
        @Test
        void temDeEstarVago() {
            cenario(lugar(destinoId, job, categoria, unidadeDestino, Position.ATIVO));
            when(assignmentRepository.temTitular(destinoId)).thenReturn(true);

            var erro = assertThrows(IgrpResponseStatusException.class,
                    () -> service.consolidarMobilidade(funcionarioId, destinoId, unidadeDestino, dataEfeito, null));

            assertEquals(422, erro.getStatusCode().value());
        }

        @Test
        void naoPodeSerOLugarActual() {
            when(assignmentRepository.findCurrentPrincipalByFuncionario(funcionarioId))
                    .thenReturn(Optional.of(afectacaoActual()));
            when(positionRepository.findById(PositionId.from(origemId)))
                    .thenReturn(Optional.of(lugar(origemId, job, categoria, unidadeOrigem, Position.ATIVO)));

            var erro = assertThrows(IgrpResponseStatusException.class,
                    () -> service.consolidarMobilidade(funcionarioId, origemId, unidadeOrigem, dataEfeito, null));

            assertEquals(422, erro.getStatusCode().value());
        }
    }

    @Nested
    class OPeriodoDaMobilidade {

        private LicencaMobilidade mobilidade(LocalDate de, LocalDate ate) {
            var m = LicencaMobilidade.criar(funcionarioId, SubtipoLicencaMobilidadeId.gerarNovo(),
                    de, ate, null, null, null, null, unidadeDestino, null, null);
            m.aprovar();
            return m;
        }

        /**
         * A partir da data de efeito a pessoa é titular do Lugar de destino, logo o último dia em
         * mobilidade é a véspera — a mesma regra do regresso antecipado e da suspensão de férias.
         */
        @Test
        void oUltimoDiaEmMobilidadeEAVespera() {
            var m = mobilidade(LocalDate.of(2026, 5, 1), LocalDate.of(2027, 4, 30));

            m.consolidar(dataEfeito, dataEfeito);

            assertEquals(dataEfeito.minusDays(1), m.getDataFim());
        }

        /** Não é um regresso: marca-se, para o job nocturno não devolver ninguém a lado nenhum. */
        @Test
        void marcaOEfeitoDeRegressoParaOJobNaoLheTocar() {
            var m = mobilidade(LocalDate.of(2026, 5, 1), LocalDate.of(2027, 4, 30));

            m.consolidar(dataEfeito, dataEfeito);

            assertNotNull(m.getEfeitoRegressoAplicadoEm());
            assertEquals(LicencaMobilidade.APPROVED, m.getStatus());
        }

        @Test
        void naoSeConsolidaOQueAindaNaoComecou() {
            var m = mobilidade(LocalDate.of(2027, 1, 1), LocalDate.of(2027, 12, 31));

            var erro = assertThrows(IgrpResponseStatusException.class,
                    () -> m.consolidar(LocalDate.of(2027, 2, 1), dataEfeito));

            assertEquals(409, erro.getStatusCode().value());
        }

        /** Consolidada uma vez, o periodo transitorio acabou: nao ha segundo a consolidar. */
        @Test
        void naoSeConsolidaDuasVezes() {
            var m = mobilidade(LocalDate.of(2026, 5, 1), LocalDate.of(2027, 4, 30));
            m.consolidar(dataEfeito, dataEfeito);

            var erro = assertThrows(IgrpResponseStatusException.class,
                    () -> m.consolidar(dataEfeito.plusDays(10), dataEfeito.plusDays(10)));

            assertEquals(409, erro.getStatusCode().value());
        }

        @Test
        void naoSeConsolidaOQueNaoFoiDeferido() {
            var m = LicencaMobilidade.criar(funcionarioId, SubtipoLicencaMobilidadeId.gerarNovo(),
                    LocalDate.of(2026, 5, 1), LocalDate.of(2027, 4, 30),
                    null, null, null, null, unidadeDestino, null, null);

            var erro = assertThrows(IgrpResponseStatusException.class,
                    () -> m.consolidar(dataEfeito, dataEfeito));

            assertEquals(409, erro.getStatusCode().value());
        }

        @Test
        void aDataDeEfeitoNaoPodeSerAnteriorAoInicioDaMobilidade() {
            var m = mobilidade(LocalDate.of(2026, 5, 1), LocalDate.of(2027, 4, 30));

            var erro = assertThrows(IgrpResponseStatusException.class,
                    () -> m.consolidar(LocalDate.of(2026, 4, 1), dataEfeito));

            assertEquals(400, erro.getStatusCode().value());
        }
    }
}
