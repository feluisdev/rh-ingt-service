package cv.igrp.RH_Service.colaboradores.application.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cv.igrp.RH_Service.colaboradores.domain.models.Funcionario;
import cv.igrp.RH_Service.colaboradores.domain.models.SaldoAusencia;
import cv.igrp.RH_Service.colaboradores.domain.models.TipoAusencia;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.SaldoAusenciaRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.TipoAusenciaRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.TipoAusenciaId;
import cv.igrp.RH_Service.parametrizacoes.domain.models.RegimeAusencia;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * <b>Vencimento do direito a férias</b> — DL n.º 3/2010, cap. II.
 *
 * <p>Antes disto o saldo de férias era escrito à mão por quem instalasse a aplicação. O art. 2.º
 * n.º 4 diz que o direito vence a 1 de Janeiro, sozinho; o art. 3.º manda proporcioná-lo no ano
 * de ingresso.
 */
@ExtendWith(MockitoExtension.class)
class FeriasVencimentoTest {

    private static final FuncionarioId FUNCIONARIO = FuncionarioId.gerarNovo();
    private static final TipoAusenciaId TIPO_FERIAS = TipoAusenciaId.gerarNovo();

    @Mock private FuncionarioRepository funcionarioRepository;
    @Mock private TipoAusenciaRepository tipoAusenciaRepository;
    @Mock private SaldoAusenciaRepository saldoAusenciaRepository;

    private FeriasService servico() {
        return new FeriasService(funcionarioRepository, tipoAusenciaRepository, saldoAusenciaRepository);
    }

    private static TipoAusencia ferias(Integer diasPorAno) {
        return TipoAusencia.reconstituir(TIPO_FERIAS, "Férias", "FERIAS", true, true,
                diasPorAno, null, null, "GOZAMENTO", true, RegimeAusencia.FERIAS, null);
    }

    private static Funcionario admitidoEm(LocalDate data) {
        Funcionario f = mock(Funcionario.class);
        when(f.getDataAdmissao()).thenReturn(data);
        return f;
    }

    private void comCatalogoEColaborador(LocalDate admissao, Integer diasPorAno) {
        when(tipoAusenciaRepository.findFerias()).thenReturn(Optional.of(ferias(diasPorAno)));
        Funcionario f = mock(Funcionario.class);
        when(f.getDataAdmissao()).thenReturn(admissao);
        when(funcionarioRepository.findById(FUNCIONARIO)).thenReturn(Optional.of(f));
    }

    // -----------------------------------------------------------------------------
    @Nested
    class OsDiasVemDoCatalogoComALeiPorRecurso {

        /** Art. 2.º n.º 3: 22 dias úteis. O valor vive no catálogo, não no código. */
        @Test
        void anoInteiroDaOsDiasDoCatalogo() {
            var f = admitidoEm(LocalDate.of(2020, 3, 1));

            assertEquals(22, servico().direitoDoAno(f, ferias(22), 2026));
        }

        /** Uma instituição com diploma próprio pode ter outro número — e é respeitado. */
        @Test
        void oCatalogoManda() {
            var f = admitidoEm(LocalDate.of(2020, 3, 1));

            assertEquals(30, servico().direitoDoAno(f, ferias(30), 2026));
        }

        /** Catálogo por preencher: vale o que a lei diz, e não zero. */
        @Test
        void semValorNoCatalogoValeALei() {
            var f = admitidoEm(LocalDate.of(2020, 3, 1));

            assertEquals(22, servico().direitoDoAno(f, ferias(null), 2026));
            assertEquals(22, servico().direitoDoAno(f, ferias(0), 2026));
        }

        /** Art. 2.º n.º 1: sem relação de emprego não há direito. */
        @Test
        void antesDaAdmissaoNaoHaDireito() {
            var f = admitidoEm(LocalDate.of(2026, 3, 1));

            assertEquals(0, servico().direitoDoAno(f, ferias(22), 2025));
        }
    }

    // -----------------------------------------------------------------------------
    @Nested
    class OAnoDeIngressoEhProporcional {

        /**
         * Art. 3.º: «6 ou 5 dias úteis por cada 3 meses completos». Repartir 22 por quatro
         * trimestres dá 5,5 de cada vez — arredondando o acumulado saem exactamente 6, 11, 17 e
         * 22, que é a alternância que a lei descreve.
         */
        @Test
        void cadaTrimestreCompletoVale6ou5Dias() {
            var s = servico();

            assertEquals(6, s.direitoDoAno(admitidoEm(LocalDate.of(2026, 10, 1)), ferias(22), 2026));
            assertEquals(11, s.direitoDoAno(admitidoEm(LocalDate.of(2026, 7, 1)), ferias(22), 2026));
            assertEquals(17, s.direitoDoAno(admitidoEm(LocalDate.of(2026, 4, 1)), ferias(22), 2026));
            assertEquals(22, s.direitoDoAno(admitidoEm(LocalDate.of(2026, 1, 1)), ferias(22), 2026));
        }

        /** Art. 3.º: abaixo de 90 dias de serviço não há gozo antecipado nenhum. */
        @Test
        void abaixoDe90DiasDeServicoNaoHaDireito() {
            var s = servico();

            assertEquals(0, s.direitoDoAno(admitidoEm(LocalDate.of(2026, 11, 1)), ferias(22), 2026));
            assertEquals(0, s.direitoDoAno(admitidoEm(LocalDate.of(2026, 12, 20)), ferias(22), 2026));
        }

        /** Trimestres <b>completos</b>: dois meses e meio não fazem um. */
        @Test
        void soContaOTrimestreCompleto() {
            // 15 de Outubro a 31 de Dezembro: 78 dias, nem 90 nem um trimestre.
            assertEquals(0, servico().direitoDoAno(admitidoEm(LocalDate.of(2026, 10, 15)), ferias(22), 2026));
            // 15 de Setembro: 108 dias, passa os 90, mas só um trimestre completo.
            assertEquals(6, servico().direitoDoAno(admitidoEm(LocalDate.of(2026, 9, 15)), ferias(22), 2026));
        }

        /** No ano seguinte ao da admissão o direito é inteiro (art. 2.º n.º 4). */
        @Test
        void noAnoSeguinteODireitoEhInteiro() {
            assertEquals(22, servico().direitoDoAno(admitidoEm(LocalDate.of(2026, 11, 1)), ferias(22), 2027));
        }

        /** A proporcionalidade acompanha um direito anual diferente de 22. */
        @Test
        void aProporcaoSegueODireitoAnualDaInstituicao() {
            assertEquals(15, servico().direitoDoAno(admitidoEm(LocalDate.of(2026, 7, 1)), ferias(30), 2026));
        }
    }

    // -----------------------------------------------------------------------------
    @Nested
    class OSaldoNasceSozinho {

        @Test
        void criaOSaldoQuandoNaoExiste() {
            comCatalogoEColaborador(LocalDate.of(2020, 3, 1), 22);
            when(saldoAusenciaRepository.findByFuncionarioIdAndTipoAusenciaIdAndAno(any(), any(), anyInt()))
                    .thenReturn(Optional.empty());
            when(saldoAusenciaRepository.save(any())).thenAnswer(i -> i.getArgument(0));

            servico().garantirSaldoDoAno(FUNCIONARIO, 2026);

            ArgumentCaptor<SaldoAusencia> captor = ArgumentCaptor.forClass(SaldoAusencia.class);
            verify(saldoAusenciaRepository).save(captor.capture());
            assertEquals(22, captor.getValue().getDiasDireito());
            assertEquals(2026, captor.getValue().getAno());
        }

        /** Chamado todos os dias pelo job: sem nada a mudar, não escreve. */
        @Test
        void naoEscreveQuandoNadaMuda() {
            comCatalogoEColaborador(LocalDate.of(2020, 3, 1), 22);
            var existente = SaldoAusencia.criar(FUNCIONARIO, TIPO_FERIAS, 2026, 22);
            when(saldoAusenciaRepository.findByFuncionarioIdAndTipoAusenciaIdAndAno(any(), any(), anyInt()))
                    .thenReturn(Optional.of(existente));

            servico().garantirSaldoDoAno(FUNCIONARIO, 2026);

            verify(saldoAusenciaRepository, never()).save(any());
        }

        /** No ano de ingresso o direito cresce a cada trimestre — o job actualiza-o. */
        @Test
        void oDireitoCresceNoAnoDeIngresso() {
            comCatalogoEColaborador(LocalDate.of(2026, 4, 1), 22);
            var existente = SaldoAusencia.criar(FUNCIONARIO, TIPO_FERIAS, 2026, 11);
            when(saldoAusenciaRepository.findByFuncionarioIdAndTipoAusenciaIdAndAno(any(), any(), anyInt()))
                    .thenReturn(Optional.of(existente));
            when(saldoAusenciaRepository.save(any())).thenAnswer(i -> i.getArgument(0));

            servico().garantirSaldoDoAno(FUNCIONARIO, 2026);

            assertEquals(17, existente.getDiasDireito());
        }

        /**
         * O direito não encolhe abaixo do que já foi gozado ou reservado. O art. 2.º n.º 5 diz
         * que é irrenunciável, e tirar dias já gozados não é coisa que um job faça sozinho.
         */
        @Test
        void oDireitoNaoEncolheAbaixoDoQueJaFoiUsado() {
            comCatalogoEColaborador(LocalDate.of(2026, 10, 1), 22);   // recalculado daria 6
            var existente = SaldoAusencia.criar(FUNCIONARIO, TIPO_FERIAS, 2026, 22);
            existente.reservar(4);
            existente.confirmarGozo(4);
            existente.reservar(5);                                    // 4 gozados + 5 reservados
            when(saldoAusenciaRepository.findByFuncionarioIdAndTipoAusenciaIdAndAno(any(), any(), anyInt()))
                    .thenReturn(Optional.of(existente));
            when(saldoAusenciaRepository.save(any())).thenAnswer(i -> i.getArgument(0));

            servico().garantirSaldoDoAno(FUNCIONARIO, 2026);

            assertEquals(9, existente.getDiasDireito());
            assertTrue(existente.saldoDisponivel() >= 0);
        }

        /** Sem tipo de férias no catálogo não há nada a fazer — e não se rebenta. */
        @Test
        void semTipoDeFeriasNoCatalogoNaoFazNada() {
            when(tipoAusenciaRepository.findFerias()).thenReturn(Optional.empty());

            assertFalse(servico().garantirSaldoDoAno(FUNCIONARIO, 2026).isPresent());
            verify(saldoAusenciaRepository, never()).save(any());
        }
    }
}
