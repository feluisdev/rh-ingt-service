package cv.igrp.RH_Service.colaboradores.application.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cv.igrp.RH_Service.colaboradores.application.services.HorarioColaboradorService.Origem;
import cv.igrp.RH_Service.colaboradores.application.services.HorarioColaboradorService.Vigente;
import cv.igrp.RH_Service.colaboradores.domain.models.Contrato;
import cv.igrp.RH_Service.colaboradores.domain.models.EstadoTrabalhoSuplementar;
import cv.igrp.RH_Service.colaboradores.domain.models.Funcionario;
import cv.igrp.RH_Service.colaboradores.domain.models.MarcacaoAssiduidade;
import cv.igrp.RH_Service.colaboradores.domain.models.OrigemMarcacao;
import cv.igrp.RH_Service.colaboradores.domain.models.RegimePrestacao;
import cv.igrp.RH_Service.colaboradores.domain.models.SentidoMarcacao;
import cv.igrp.RH_Service.colaboradores.domain.models.TipoDiaSuplementar;
import cv.igrp.RH_Service.colaboradores.domain.models.TrabalhoSuplementar;
import cv.igrp.RH_Service.colaboradores.domain.repository.ContratoRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.MarcacaoAssiduidadeRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.TrabalhoSuplementarRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.MarcacaoAssiduidadeId;
import cv.igrp.RH_Service.parametrizacoes.domain.models.BlocoHorario;
import cv.igrp.RH_Service.parametrizacoes.domain.models.ControloHorario;
import cv.igrp.RH_Service.parametrizacoes.domain.models.Horario;
import cv.igrp.RH_Service.shared.application.constants.RegimeTrabalho;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/**
 * Trabalho suplementar: quem lanca e quem decide, isencao, fora do horario num dia util, sobreposicao,
 * e as horas realizadas do mes pelas marcacoes, por tipo de dia.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TrabalhoSuplementarServiceTest {

    /** Quarta-feira. */
    private static final LocalDateTime AGORA = LocalDateTime.of(2026, 9, 23, 10, 0);
    private static final LocalDate HOJE = AGORA.toLocalDate();

    @Mock private TrabalhoSuplementarRepository trabalhoRepository;
    @Mock private FuncionarioRepository funcionarioRepository;
    @Mock private ContratoRepository contratoRepository;
    @Mock private MarcacaoAssiduidadeRepository marcacaoRepository;
    @Mock private CalendarioFeriadosService calendarioFeriadosService;
    @Mock private HorarioColaboradorService horarioColaboradorService;
    @Mock private ChefiaService chefiaService;

    private TrabalhoSuplementarService service;
    private final FuncionarioId pessoa = FuncionarioId.gerarNovo();
    private final FuncionarioId chefe = FuncionarioId.gerarNovo();
    private final List<TrabalhoSuplementar> gravados = new ArrayList<>();

    /** 08:00-12:00 e 13:00-17:00, segunda a sexta. */
    private static Horario fixo() {
        List<BlocoHorario> blocos = new ArrayList<>();
        for (DayOfWeek d : List.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)) {
            blocos.add(new BlocoHorario(d, LocalTime.of(8, 0), LocalTime.of(12, 0), true));
            blocos.add(new BlocoHorario(d, LocalTime.of(13, 0), LocalTime.of(17, 0), true));
        }
        return Horario.criar("Fixo", ControloHorario.FIXO, null, null, blocos);
    }

    @BeforeEach
    void base() {
        service = new TrabalhoSuplementarService(trabalhoRepository, funcionarioRepository, contratoRepository,
                marcacaoRepository, calendarioFeriadosService, horarioColaboradorService, chefiaService) {
            @Override LocalDateTime agora() { return AGORA; }
        };
        var f = mock(Funcionario.class);
        when(f.getIsActive()).thenReturn(true);
        when(funcionarioRepository.findById(pessoa)).thenReturn(Optional.of(f));
        when(contratoRepository.findCurrentByFuncionarioId(pessoa)).thenReturn(Optional.empty());
        when(horarioColaboradorService.vigente(eq(pessoa), any()))
                .thenReturn(new Vigente(Origem.BASE, fixo(), RegimePrestacao.PRESENCIAL, null));
        when(calendarioFeriadosService.feriadosDoColaborador(eq(pessoa), any(), any())).thenReturn(Set.of());
        when(trabalhoRepository.findByFuncionarioEntre(eq(pessoa), any(), any())).thenAnswer(i -> List.copyOf(gravados));
        when(trabalhoRepository.save(any())).thenAnswer(i -> {
            TrabalhoSuplementar t = i.getArgument(0);
            gravados.removeIf(g -> g.getId().equals(t.getId()));
            gravados.add(t);
            return t;
        });
        when(trabalhoRepository.findById(any())).thenAnswer(i -> gravados.stream()
                .filter(g -> g.getId().equals(i.getArgument(0))).findFirst());
        when(chefiaService.eChefeDirecto(chefe, pessoa)).thenReturn(true);
    }

    private static int status(Runnable r) {
        return assertThrows(IgrpResponseStatusException.class, r::run).getStatusCode().value();
    }

    private static LocalTime h(String hhmm) { return LocalTime.parse(hhmm); }

    @Test
    void oRhLancaAutorizadoForaDoHorario() {
        var t = service.lancar(null, pessoa, HOJE, h("17:00"), h("19:00"), "Fecho de contas");
        assertEquals(EstadoTrabalhoSuplementar.AUTORIZADO, t.getEstado());
    }

    @Test
    void numDiaUtilNaoPodeTocarNoHorario() {
        assertEquals(422, status(() -> service.lancar(null, pessoa, HOJE, h("16:00"), h("18:00"), "x")));
        verify(trabalhoRepository, never()).save(any());
    }

    @Test
    void emDiaDeDescansoOuFeriadoQualquerHoraServe() {
        LocalDate sabado = HOJE.plusDays(3);
        service.lancar(null, pessoa, sabado, h("09:00"), h("13:00"), "Inventario");
        LocalDate feriado = HOJE.plusDays(1);
        when(calendarioFeriadosService.feriadosDoColaborador(eq(pessoa), any(), any())).thenReturn(Set.of(feriado));
        service.lancar(null, pessoa, feriado, h("09:00"), h("12:00"), "Eleicoes");
        assertEquals(2, gravados.size());
    }

    @Test
    void isentoDeHorarioNaoFazTrabalhoSuplementar() {
        var c = mock(Contrato.class);
        when(c.getRegimeTrabalho()).thenReturn(RegimeTrabalho.ISENCAO_HORARIO.getCode());
        when(contratoRepository.findCurrentByFuncionarioId(pessoa)).thenReturn(Optional.of(c));
        assertEquals(422, status(() -> service.lancar(null, pessoa, HOJE, h("17:00"), h("19:00"), "x")));
    }

    @Test
    void semHorarioNaoSeDistingueOTempoNormal() {
        when(horarioColaboradorService.vigente(eq(pessoa), any())).thenReturn(new Vigente(Origem.NENHUM, null, null, null));
        assertEquals(422, status(() -> service.lancar(null, pessoa, HOJE, h("17:00"), h("19:00"), "x")));
    }

    @Test
    void colaboradorInactivo403() {
        var f = mock(Funcionario.class);
        when(f.getIsActive()).thenReturn(false);
        when(funcionarioRepository.findById(pessoa)).thenReturn(Optional.of(f));
        assertEquals(403, status(() -> service.lancar(null, pessoa, HOJE, h("17:00"), h("19:00"), "x")));
    }

    @Test
    void sobreposicaoComOutroEmVigor409MasNaoComUmCancelado() {
        var primeiro = service.lancar(null, pessoa, HOJE, h("17:00"), h("19:00"), "x");
        assertEquals(409, status(() -> service.pedir(pessoa, HOJE, h("18:00"), h("20:00"), "y")));
        service.cancelar(pessoa, primeiro.getId(), "Afinal nao");
        service.pedir(pessoa, HOJE, h("18:00"), h("20:00"), "y");
        assertEquals(2, gravados.size());
    }

    @Test
    void aChefiaDirectaLancaEDecideSoParaASuaEquipa() {
        var t = service.lancar(chefe, pessoa, HOJE, h("17:00"), h("18:00"), "x");
        assertEquals(chefe, t.getDecididoPor());

        var outroChefe = FuncionarioId.gerarNovo();
        assertEquals(403, status(() -> service.lancar(outroChefe, pessoa, HOJE, h("18:00"), h("19:00"), "x")));
        assertEquals(422, status(() -> service.lancar(pessoa, pessoa, HOJE, h("18:00"), h("19:00"), "x")));

        var pedido = service.pedir(pessoa, HOJE.plusDays(1), h("17:00"), h("18:00"), "y");
        when(trabalhoRepository.findById(pedido.getId())).thenReturn(Optional.of(pedido));
        assertEquals(403, status(() -> service.decidir(outroChefe, null, pedido.getId(), true, null)));
        service.decidir(chefe, null, pedido.getId(), true, null);
        assertEquals(EstadoTrabalhoSuplementar.AUTORIZADO, pedido.getEstado());
    }

    @Test
    void oRhDecideSemprePeloCaminhoDoColaborador() {
        var pedido = service.pedir(pessoa, HOJE.plusDays(1), h("17:00"), h("18:00"), "y");
        when(trabalhoRepository.findById(pedido.getId())).thenReturn(Optional.of(pedido));
        assertEquals(404, status(() -> service.decidir(null, FuncionarioId.gerarNovo(), pedido.getId(), true, null)));
        service.decidir(null, pessoa, pedido.getId(), false, "Sem orcamento");
        assertEquals(EstadoTrabalhoSuplementar.RECUSADO, pedido.getEstado());
    }

    private MarcacaoAssiduidade marca(LocalDate dia, String hora, SentidoMarcacao s) {
        return MarcacaoAssiduidade.reconstruir(MarcacaoAssiduidadeId.gerarNovo(), pessoa, LocalDateTime.of(dia, h(hora)), s,
                OrigemMarcacao.IMPORTADO, null, null, false, null, null);
    }

    @Test
    void oMesContaAsRealizadasPelasMarcacoesPorTipoDeDia() {
        LocalDate segunda = LocalDate.of(2026, 9, 21);
        LocalDate sabado = LocalDate.of(2026, 9, 19);
        LocalDate terca = LocalDate.of(2026, 9, 22);
        service.lancar(null, pessoa, segunda, h("17:00"), h("19:00"), "Fecho");     // saiu as 18:30 -> 90 min
        service.lancar(null, pessoa, sabado, h("09:00"), h("13:00"), "Inventario"); // 09:10-13:00 -> 230 min
        service.lancar(null, pessoa, terca, h("17:00"), h("18:00"), "Reuniao");     // sem marcacoes -> sem registo
        var recusado = service.pedir(pessoa, HOJE.plusDays(2), h("17:00"), h("18:00"), "y");
        recusado.recusar(null, "nao", AGORA);

        when(marcacaoRepository.findByFuncionarioEntre(eq(pessoa), any(), any())).thenReturn(List.of(
                marca(sabado, "09:10", SentidoMarcacao.ENTRADA), marca(sabado, "13:00", SentidoMarcacao.SAIDA),
                marca(segunda, "08:00", SentidoMarcacao.ENTRADA), marca(segunda, "12:00", SentidoMarcacao.SAIDA),
                marca(segunda, "13:00", SentidoMarcacao.ENTRADA), marca(segunda, "18:30", SentidoMarcacao.SAIDA)));

        var m = service.doMes(pessoa, YearMonth.of(2026, 9));
        assertEquals(4, m.linhas().size());
        assertEquals(90, m.realizadosPorTipo().get(TipoDiaSuplementar.DIA_UTIL));
        assertEquals(230, m.realizadosPorTipo().get(TipoDiaSuplementar.DESCANSO));
        assertEquals(0, m.realizadosPorTipo().get(TipoDiaSuplementar.FERIADO));
        assertEquals(320, m.minutosRealizados());
        assertEquals(120 + 240 + 60, m.minutosAutorizados());   // o recusado nao conta

        var daTerca = m.linhas().stream().filter(l -> l.trabalho().getData().equals(terca)).findFirst().orElseThrow();
        assertTrue(daTerca.semRegisto());
        var doRecusado = m.linhas().stream().filter(l -> l.trabalho().getId().equals(recusado.getId())).findFirst().orElseThrow();
        assertEquals(0, doRecusado.minutosRealizados());
        assertFalse(doRecusado.semRegisto());
    }

    @Test
    void autorizadosPorDiaSoOsAutorizados() {
        LocalDate segunda = LocalDate.of(2026, 9, 21);
        service.lancar(null, pessoa, segunda, h("17:00"), h("19:00"), "x");
        service.pedir(pessoa, HOJE.plusDays(1), h("17:00"), h("18:00"), "y");
        var porDia = service.autorizadosPorDia(pessoa, segunda, HOJE.plusDays(1));
        assertEquals(1, porDia.size());
        assertEquals(h("17:00"), porDia.get(segunda).get(0).entrada());
    }
}
