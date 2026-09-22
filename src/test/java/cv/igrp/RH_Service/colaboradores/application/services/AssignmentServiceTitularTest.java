package cv.igrp.RH_Service.colaboradores.application.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cv.igrp.RH_Service.carreiras.domain.repository.CategoryRepository;
import cv.igrp.RH_Service.carreiras.domain.repository.GradeRepository;
import cv.igrp.RH_Service.colaboradores.domain.models.Assignment;
import cv.igrp.RH_Service.colaboradores.domain.models.TipoAfectacao;
import cv.igrp.RH_Service.colaboradores.domain.repository.AssignmentRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.AssignmentId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
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
import org.springframework.http.HttpStatus;

/**
 * Prova de que "uma cadeira, um ocupante" passou a ser "uma cadeira, um <b>titular</b>".
 *
 * <p>É o que autoriza a substituição do funcionário temporariamente impedido
 * (Lei n.º 20/X/2023, art. 73.º al. a) a c)): quem substitui entra num Lugar que
 * continua a ter titular, sem o desalojar. O índice único do Lugar passou a ser
 * parcial em {@code PRINCIPAL} na V45, e a regra de negócio acompanha.
 */
@ExtendWith(MockitoExtension.class)
class AssignmentServiceTitularTest {

    @Mock private AssignmentRepository assignmentRepository;
    @Mock private PositionRepository positionRepository;
    @Mock private GradeRepository gradeRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private FunctionRepository functionRepository;

    @InjectMocks private AssignmentService service;

    private final FuncionarioId funcionarioId = FuncionarioId.gerarNovo();
    private final UUID positionId = UUID.randomUUID();
    private final LocalDate inicio = LocalDate.of(2026, 3, 1);

    /** Lugar fora de grelha: não exige escalão, o que mantém o teste focado no título. */
    private Position lugarForaDeGrelha() {
        return Position.reconstituir(PositionId.from(positionId), "L-01", UUID.randomUUID(),
                UUID.randomUUID(), null, null, null, null, Position.ATIVO, null, true);
    }

    private void lugarExiste() {
        when(positionRepository.findById(PositionId.from(positionId)))
                .thenReturn(Optional.of(lugarForaDeGrelha()));
    }

    private Assignment afectar(TipoAfectacao tipo) {
        return service.afectar(funcionarioId, positionId, null, null,
                Assignment.ADMISSAO, tipo, inicio, null);
    }

    @Nested
    class OLugarComTitular {

        @Test
        void recusaUmSegundoTitular() {
            lugarExiste();
            when(assignmentRepository.temTitular(positionId)).thenReturn(true);

            var erro = assertThrows(IgrpResponseStatusException.class,
                    () -> afectar(TipoAfectacao.PRINCIPAL));

            assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, erro.getStatusCode());
            verify(assignmentRepository, never()).save(any());
        }

        /**
         * Estes dois testes afirmavam o contrário até 2026-09-22: davam por boa a criação de
         * uma afectação a outro título por esta porta. Não era uma funcionalidade — era um
         * buraco. Quem entra em substituição por aqui fica sem ligação ao titular e sem
         * nenhuma das regras do {@code SubstituicaoService}; quem entra em acumulação fica sem
         * regra nenhuma, porque ainda não há nenhuma.
         */
        @Test
        void recusaQuemTentaEntrarEmSubstituicaoPorEstaPorta() {
            var erro = assertThrows(IgrpResponseStatusException.class,
                    () -> afectar(TipoAfectacao.SUBSTITUICAO));

            assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, erro.getStatusCode());
            verify(assignmentRepository, never()).save(any());
        }

        /**
         * A {@code ACUMULACAO} deixou de existir como título: o art. 134.º n.º 2 al. b) que a
         * fundamentava trata da forma de prestação da <i>mobilidade</i>, não de ocupar um
         * segundo Lugar. Quem a enviar recebe o 422 de valor fora da lista.
         */
        @Test
        void aAcumulacaoJaNaoEUmTituloDeOcuparUmLugar() {
            var erro = assertThrows(IgrpResponseStatusException.class,
                    () -> TipoAfectacao.de("ACUMULACAO"));

            assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, erro.getStatusCode());
            assertEquals("PRINCIPAL, SUBSTITUICAO", TipoAfectacao.codigosValidos());
        }
    }

    @Nested
    class OTituloDaAfectacao {

        @Test
        void porOmissaoEPrincipal() {
            lugarExiste();
            when(assignmentRepository.temTitular(positionId)).thenReturn(false);
            when(assignmentRepository.findCurrentPrincipalByFuncionario(funcionarioId))
                    .thenReturn(Optional.empty());
            when(assignmentRepository.save(any(Assignment.class))).thenAnswer(i -> i.getArgument(0));

            afectar(null);

            ArgumentCaptor<Assignment> captor = ArgumentCaptor.forClass(Assignment.class);
            verify(assignmentRepository).save(captor.capture());
            assertEquals(TipoAfectacao.PRINCIPAL, captor.getValue().getAssignmentType());
        }

        /**
         * A invariante é a mesma de sempre; mudou a porta por onde se prova. Provava-se com
         * {@code afectar(SUBSTITUICAO)}, que deixou de ser caminho — passa a provar-se onde a
         * substituição de facto nasce.
         */
        @Test
        void aSubstituicaoNaoEncerraAAfectacaoDeQuemVaiSubstituir() {
            lugarExiste();
            when(assignmentRepository.save(any(Assignment.class))).thenAnswer(i -> i.getArgument(0));

            service.afectarSubstituicao(funcionarioId, positionId, null, null,
                    AssignmentId.gerarNovo(), inicio, null);

            // Quem vai substituir mantém o seu próprio Lugar -- o SCD Type 2 não se aplica.
            verify(assignmentRepository, never()).findCurrentPrincipalByFuncionario(any());
            verify(assignmentRepository, never()).temTitular(any());
        }
    }

    @Nested
    class OValorGuardado {

        @Test
        void vazioOuNuloLeSeComoPrincipal() {
            assertEquals(TipoAfectacao.PRINCIPAL, TipoAfectacao.de(null));
            assertEquals(TipoAfectacao.PRINCIPAL, TipoAfectacao.de("  "));
        }

        @Test
        void toleraEspacosEMinusculas() {
            assertEquals(TipoAfectacao.SUBSTITUICAO, TipoAfectacao.de(" substituicao "));
        }

        @Test
        void umTipoDesconhecidoNaoPassa() {
            var erro = assertThrows(IgrpResponseStatusException.class,
                    () -> TipoAfectacao.de("INTERINO"));

            assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, erro.getStatusCode());
        }
    }
}
