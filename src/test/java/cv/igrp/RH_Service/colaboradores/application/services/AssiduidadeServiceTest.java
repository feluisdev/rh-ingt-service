package cv.igrp.RH_Service.colaboradores.application.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cv.igrp.RH_Service.colaboradores.application.services.AssiduidadeService.Picagem;
import cv.igrp.RH_Service.colaboradores.application.services.HorarioColaboradorService.Origem;
import cv.igrp.RH_Service.colaboradores.application.services.HorarioColaboradorService.Vigente;
import cv.igrp.RH_Service.colaboradores.domain.models.Funcionario;
import cv.igrp.RH_Service.colaboradores.domain.models.MarcacaoAssiduidade;
import cv.igrp.RH_Service.colaboradores.domain.models.OrigemMarcacao;
import cv.igrp.RH_Service.colaboradores.domain.models.RegimePrestacao;
import cv.igrp.RH_Service.colaboradores.domain.models.SentidoMarcacao;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.MarcacaoAssiduidadeRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.PedidoAusenciaRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.MarcacaoAssiduidadeId;
import cv.igrp.RH_Service.parametrizacoes.domain.models.BlocoHorario;
import cv.igrp.RH_Service.parametrizacoes.domain.models.ControloHorario;
import cv.igrp.RH_Service.parametrizacoes.domain.models.Horario;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/**
 * Registo diario: a correccao exige motivo; os alertas nao recusam; a importacao e repetivel e uma
 * picagem ma nao trava as outras; a consulta da as horas esperadas do horario do dia.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AssiduidadeServiceTest {

    /** Uma segunda-feira no passado. */
    private static final LocalDate SEGUNDA = LocalDate.of(2026, 9, 21);

    @Mock private MarcacaoAssiduidadeRepository marcacaoRepository;
    @Mock private FuncionarioRepository funcionarioRepository;
    @Mock private PedidoAusenciaRepository pedidoAusenciaRepository;
    @Mock private CalendarioFeriadosService calendarioFeriadosService;
    @Mock private HorarioColaboradorService horarioColaboradorService;

    @InjectMocks private AssiduidadeService service;

    private final FuncionarioId funcionario = FuncionarioId.gerarNovo();
    private Funcionario pessoa;

    @BeforeEach
    void base() {
        pessoa = mock(Funcionario.class);
        when(pessoa.getId()).thenReturn(funcionario);
        when(funcionarioRepository.findById(funcionario)).thenReturn(Optional.of(pessoa));
        when(funcionarioRepository.findByNumeroFuncionario("0000002")).thenReturn(Optional.of(pessoa));
        when(marcacaoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(pedidoAusenciaRepository.findAprovadosEntre(any(), any(), any())).thenReturn(List.of());
        when(calendarioFeriadosService.feriadosDoColaborador(any(), any(), any())).thenReturn(Set.of());
    }

    @Test
    void aPrimeiraDoDiaNaoPedeMotivo() {
        when(marcacaoRepository.existeValidaNoDia(funcionario, SEGUNDA)).thenReturn(false);

        var r = service.lancar(funcionario, SEGUNDA.atTime(8, 0), SentidoMarcacao.ENTRADA, null);

        assertEquals(OrigemMarcacao.MANUAL, r.marcacao().getOrigem());
        assertTrue(r.alertas().isEmpty());
    }

    @Test
    void numDiaComMarcacoesEUmaCorreccaoEExigeMotivo() {
        when(marcacaoRepository.existeValidaNoDia(funcionario, SEGUNDA)).thenReturn(true);

        var e = assertThrows(IgrpResponseStatusException.class,
                () -> service.lancar(funcionario, SEGUNDA.atTime(17, 0), SentidoMarcacao.SAIDA, null));
        assertEquals(422, e.getStatusCode().value());
        verify(marcacaoRepository, never()).save(any());

        var r = service.lancar(funcionario, SEGUNDA.atTime(17, 0), SentidoMarcacao.SAIDA, "esqueceu-se de picar");
        assertEquals("esqueceu-se de picar", r.marcacao().getMotivo());
    }

    @Test
    void fimDeSemanaEFeriadoDaoAlertaNaoRecusa() {
        LocalDate sabado = SEGUNDA.minusDays(2);
        when(calendarioFeriadosService.feriadosDoColaborador(funcionario, sabado, sabado)).thenReturn(Set.of(sabado));

        var r = service.lancar(funcionario, sabado.atTime(9, 0), SentidoMarcacao.ENTRADA, null);

        assertEquals(2, r.alertas().size());
    }

    @Test
    void importacaoRepetivelEUmaPicagemMaNaoTravaAsOutras() {
        when(marcacaoRepository.existsByReferenciaExterna("R1")).thenReturn(true);
        when(marcacaoRepository.existsByReferenciaExterna("R2")).thenReturn(false);
        when(marcacaoRepository.existsByReferenciaExterna("R3")).thenReturn(false);
        when(marcacaoRepository.existsByReferenciaExterna("R4")).thenReturn(false);
        when(funcionarioRepository.findByNumeroFuncionario("9999999")).thenReturn(Optional.empty());

        var rel = service.importar(List.of(
                new Picagem(null, "0000002", SEGUNDA.atTime(8, 0), SentidoMarcacao.ENTRADA, "R1"),   // ja importada
                new Picagem(null, "0000002", SEGUNDA.atTime(12, 0), SentidoMarcacao.SAIDA, "R2"),
                new Picagem(null, "0000002", SEGUNDA.atTime(12, 0), SentidoMarcacao.SAIDA, "R2"),    // repetida no lote
                new Picagem(null, "9999999", SEGUNDA.atTime(8, 0), SentidoMarcacao.ENTRADA, "R3"),   // numero desconhecido
                new Picagem(null, "0000002", SEGUNDA.atTime(13, 0), null, "R4"),                    // sem sentido
                new Picagem(null, "0000002", SEGUNDA.atTime(14, 0), SentidoMarcacao.ENTRADA, null)));  // sem referencia

        assertEquals(1, rel.importadas());
        assertEquals(2, rel.duplicadas());
        assertEquals(3, rel.rejeitadas().size());
        verify(marcacaoRepository, times(1)).save(any());
    }

    @Test
    void importacaoVaziaOuGrandeDemaisE422() {
        assertThrows(IgrpResponseStatusException.class, () -> service.importar(List.of()));
    }

    @Test
    void consultaDaAsHorasEsperadasDoHorarioEZeroNoFeriado() {
        var horario = Horario.criar("Normal", ControloHorario.FIXO, null, null, List.of(DayOfWeek.values()).stream()
                .limit(5).map(d -> new BlocoHorario(d, LocalTime.of(8, 0), LocalTime.of(16, 0), true)).toList());
        when(horarioColaboradorService.vigente(eq(funcionario), any()))
                .thenReturn(new Vigente(Origem.BASE, horario, RegimePrestacao.PRESENCIAL, null));
        LocalDate terca = SEGUNDA.plusDays(1);
        when(calendarioFeriadosService.feriadosDoColaborador(funcionario, SEGUNDA, SEGUNDA.plusDays(6))).thenReturn(Set.of(terca));
        when(marcacaoRepository.findByFuncionarioEntre(funcionario, SEGUNDA, SEGUNDA.plusDays(6))).thenReturn(List.of(
                marcacao(SEGUNDA.atTime(8, 0), SentidoMarcacao.ENTRADA), marcacao(SEGUNDA.atTime(15, 0), SentidoMarcacao.SAIDA)));

        var c = service.consultar(funcionario, SEGUNDA, SEGUNDA.plusDays(6));

        assertEquals(7, c.dias().size());
        assertEquals(7 * 60, c.dias().get(0).dia().minutosTrabalhados());
        assertEquals(8 * 60, c.dias().get(0).minutosEsperados());
        assertTrue(c.dias().get(1).feriado());
        assertEquals(0, c.dias().get(1).minutosEsperados());
        assertEquals(1, c.semanas().size());
        assertEquals(4 * 8 * 60, c.semanas().get(0).minutosEsperados());
        assertEquals(7 * 60, c.semanas().get(0).minutosTrabalhados());
    }

    @Test
    void consultaDeMaisDeDoisMesesOuAoContrarioE422() {
        assertThrows(IgrpResponseStatusException.class, () -> service.consultar(funcionario, SEGUNDA, SEGUNDA.plusDays(62)));
        assertThrows(IgrpResponseStatusException.class, () -> service.consultar(funcionario, SEGUNDA, SEGUNDA.minusDays(1)));
    }

    @Test
    void anularUmaMarcacaoDeOutraPessoaE404() {
        var alheia = MarcacaoAssiduidade.registar(FuncionarioId.gerarNovo(), SEGUNDA.atTime(8, 0), SentidoMarcacao.ENTRADA,
                OrigemMarcacao.MANUAL, null, null, LocalDateTime.now());
        when(marcacaoRepository.findById(alheia.getId())).thenReturn(Optional.of(alheia));

        var e = assertThrows(IgrpResponseStatusException.class, () -> service.anular(funcionario, alheia.getId(), "erro"));
        assertEquals(404, e.getStatusCode().value());
    }

    private MarcacaoAssiduidade marcacao(LocalDateTime momento, SentidoMarcacao sentido) {
        return MarcacaoAssiduidade.reconstruir(MarcacaoAssiduidadeId.gerarNovo(), funcionario, momento, sentido,
                OrigemMarcacao.IMPORTADO, null, null, false, null, null);
    }
}
