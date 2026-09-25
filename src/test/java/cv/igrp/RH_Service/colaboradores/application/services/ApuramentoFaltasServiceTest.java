package cv.igrp.RH_Service.colaboradores.application.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import cv.igrp.RH_Service.colaboradores.application.services.HorarioColaboradorService.Origem;
import cv.igrp.RH_Service.colaboradores.application.services.HorarioColaboradorService.Vigente;
import cv.igrp.RH_Service.colaboradores.domain.models.Contrato;
import cv.igrp.RH_Service.colaboradores.domain.models.EstadoDiaApurado;
import cv.igrp.RH_Service.colaboradores.domain.models.Funcionario;
import cv.igrp.RH_Service.colaboradores.domain.models.LicencaMobilidade;
import cv.igrp.RH_Service.colaboradores.domain.models.PedidoAusencia;
import cv.igrp.RH_Service.colaboradores.domain.models.RegimePrestacao;
import cv.igrp.RH_Service.colaboradores.domain.models.SubtipoLicencaMobilidade;
import cv.igrp.RH_Service.colaboradores.domain.repository.ContratoRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.LicencaMobilidadeRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.MarcacaoAssiduidadeRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.PedidoAusenciaRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.parametrizacoes.domain.models.BlocoHorario;
import cv.igrp.RH_Service.parametrizacoes.domain.models.ControloHorario;
import cv.igrp.RH_Service.parametrizacoes.domain.models.Horario;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
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

/** A classificacao dos dias antes de apurar: o que nao e dia de trabalho sai logo, e nao conta. */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ApuramentoFaltasServiceTest {

    private static final YearMonth SETEMBRO = YearMonth.of(2026, 9);

    @Mock private FuncionarioRepository funcionarioRepository;
    @Mock private ContratoRepository contratoRepository;
    @Mock private MarcacaoAssiduidadeRepository marcacaoRepository;
    @Mock private PedidoAusenciaRepository pedidoAusenciaRepository;
    @Mock private LicencaMobilidadeRepository licencaRepository;
    @Mock private MobilidadeService mobilidadeService;
    @Mock private CalendarioFeriadosService calendarioFeriadosService;
    @Mock private HorarioColaboradorService horarioColaboradorService;
    @Mock private TrabalhoSuplementarService trabalhoSuplementarService;
    @Mock private org.springframework.beans.factory.ObjectProvider<DiasEspeciaisProvider> especiais;

    private ApuramentoFaltasService service;
    private final FuncionarioId funcionario = FuncionarioId.gerarNovo();

    @BeforeEach
    void base() {
        service = new ApuramentoFaltasService(funcionarioRepository, contratoRepository, marcacaoRepository,
                pedidoAusenciaRepository, licencaRepository, mobilidadeService, calendarioFeriadosService,
                horarioColaboradorService, trabalhoSuplementarService, especiais) {
            @Override LocalDate hoje() { return LocalDate.of(2026, 9, 21); }
        };
        var pessoa = mock(Funcionario.class);
        when(pessoa.getDataAdmissao()).thenReturn(LocalDate.of(2026, 9, 3));
        when(funcionarioRepository.findById(funcionario)).thenReturn(Optional.of(pessoa));
        when(contratoRepository.findCurrentByFuncionarioId(funcionario)).thenReturn(Optional.empty());
        when(marcacaoRepository.findByFuncionarioEntre(any(), any(), any())).thenReturn(List.of());
        when(calendarioFeriadosService.feriadosDoColaborador(any(), any(), any())).thenReturn(Set.of(LocalDate.of(2026, 9, 12)));
        when(pedidoAusenciaRepository.findAprovadosEntre(any(), any(), any())).thenReturn(List.of());
        when(licencaRepository.findActiveByFuncionarioIdAt(any(), any())).thenReturn(List.of());
        var horario = Horario.criar("Normal", ControloHorario.FIXO, null, null, List.of(DayOfWeek.values()).stream().limit(5)
                .map(d -> new BlocoHorario(d, LocalTime.of(8, 0), LocalTime.of(16, 0), true)).toList());
        when(horarioColaboradorService.vigente(eq(funcionario), any()))
                .thenReturn(new Vigente(Origem.BASE, horario, RegimePrestacao.PRESENCIAL, null));
    }

    @Test
    void diaCobertoPorOutroProcessoNaoEFalta() {
        // BR-FAL-09: missao, formacao, suspensao ou acidente tiram o dia do apuramento
        DiasEspeciaisProvider missao = (f, de, ate) -> java.util.Map.of(LocalDate.of(2026, 9, 8), EstadoDiaApurado.MISSAO_SERVICO);
        DiasEspeciaisProvider acidente = (f, de, ate) -> java.util.Map.of(LocalDate.of(2026, 9, 8), EstadoDiaApurado.ACIDENTE_SERVICO,
                LocalDate.of(2026, 9, 9), EstadoDiaApurado.ACIDENTE_SERVICO);
        when(especiais.orderedStream()).thenAnswer(i -> java.util.stream.Stream.of(missao, acidente));
        var a = service.apurar(funcionario, SETEMBRO);
        assertEquals(EstadoDiaApurado.MISSAO_SERVICO, estado(a, 8));      // a primeira razao fica
        assertEquals(EstadoDiaApurado.ACIDENTE_SERVICO, estado(a, 9));
        assertEquals(EstadoDiaApurado.COM_FALTA, estado(a, 10));
    }

    private EstadoDiaApurado estado(ApuramentoFaltasService.Apuramento a, int dia) {
        return a.resultado().dias().get(dia - 1).estado();
    }

    @Test
    void classificaAntesDaAdmissaoFeriadoEFuturo() {
        var a = service.apurar(funcionario, SETEMBRO);
        assertEquals(30, a.resultado().dias().size());
        assertEquals(EstadoDiaApurado.FORA_DO_VINCULO, estado(a, 2));
        assertEquals(EstadoDiaApurado.COM_FALTA, estado(a, 3));      // quinta, sem registo
        assertEquals(EstadoDiaApurado.DESCANSO, estado(a, 6));       // domingo
        assertEquals(EstadoDiaApurado.FERIADO, estado(a, 12));
        assertEquals(EstadoDiaApurado.FUTURO, estado(a, 21));        // hoje ainda nao acabou
    }

    @Test
    void pedidoAprovadoJustificaAteAVesperaDaSuspensao() {
        var pedido = PedidoAusencia.reconstituir(
                cv.igrp.RH_Service.colaboradores.domain.valueobject.PedidoAusenciaId.gerarNovo(), funcionario,
                cv.igrp.RH_Service.colaboradores.domain.valueobject.TipoAusenciaId.gerarNovo(),
                LocalDate.of(2026, 9, 7), LocalDate.of(2026, 9, 11), 5, "ferias", "APROVADO", null, null, null,
                true, LocalDate.of(2026, 9, 10), "doenca", null);
        when(pedidoAusenciaRepository.findAprovadosEntre(any(), any(), any())).thenReturn(List.of(pedido));

        var a = service.apurar(funcionario, SETEMBRO);
        assertEquals(EstadoDiaApurado.AUSENCIA_JUSTIFICADA, estado(a, 9));
        assertEquals(EstadoDiaApurado.COM_FALTA, estado(a, 10));
    }

    @Test
    void licencaEMobilidadeExternaNaoSaoFaltas() {
        var licenca = mock(LicencaMobilidade.class);
        var mobilidade = mock(LicencaMobilidade.class);
        when(mobilidade.isDestinoInterno()).thenReturn(false);
        var subLic = mock(SubtipoLicencaMobilidade.class);
        var subMob = mock(SubtipoLicencaMobilidade.class);
        when(subMob.isMobilidade()).thenReturn(true);
        when(mobilidadeService.subtipoSeExistir(licenca)).thenReturn(Optional.of(subLic));
        when(mobilidadeService.subtipoSeExistir(mobilidade)).thenReturn(Optional.of(subMob));
        when(licencaRepository.findActiveByFuncionarioIdAt(funcionario, LocalDate.of(2026, 9, 14))).thenReturn(List.of(licenca));
        when(licencaRepository.findActiveByFuncionarioIdAt(funcionario, LocalDate.of(2026, 9, 15))).thenReturn(List.of(mobilidade));

        var a = service.apurar(funcionario, SETEMBRO);
        assertEquals(EstadoDiaApurado.LICENCA, estado(a, 14));
        assertEquals(EstadoDiaApurado.MOBILIDADE_EXTERNA, estado(a, 15));
    }

    @Test
    void isencaoDeHorarioNaoTemDebito() {
        var contrato = mock(Contrato.class);
        when(contrato.getRegimeTrabalho()).thenReturn("ISENCAO_HORARIO");
        when(contratoRepository.findCurrentByFuncionarioId(funcionario)).thenReturn(Optional.of(contrato));

        var a = service.apurar(funcionario, SETEMBRO);
        assertTrue(a.isento());
        assertEquals(EstadoDiaApurado.ISENTO, estado(a, 3));
        assertEquals(0, a.resultado().totalFaltas().signum());
    }

    @Test
    void depoisDoFimDoVinculoNaoHaFaltas() {
        // Cessou a 8: o contrato mais recente acabou nesse dia, e a pessoa ficou inactiva.
        var pessoa = mock(Funcionario.class);
        when(pessoa.getId()).thenReturn(funcionario);
        when(pessoa.getIsActive()).thenReturn(false);
        when(pessoa.getDataAdmissao()).thenReturn(LocalDate.of(2026, 9, 3));
        when(funcionarioRepository.findById(funcionario)).thenReturn(Optional.of(pessoa));
        var contrato = mock(Contrato.class);
        when(contrato.getEndDate()).thenReturn(LocalDate.of(2026, 9, 8));
        when(contratoRepository.findAllByFuncionarioIdOrderByStartDateDesc(funcionario)).thenReturn(List.of(contrato));

        var a = service.apurar(funcionario, SETEMBRO);
        assertEquals(EstadoDiaApurado.COM_FALTA, estado(a, 8));          // ultimo dia do vinculo, sem registo
        assertEquals(EstadoDiaApurado.FORA_DO_VINCULO, estado(a, 9));
        assertEquals(EstadoDiaApurado.FORA_DO_VINCULO, estado(a, 18));
        assertEquals(LocalDate.of(2026, 9, 8), service.fimDoVinculo(pessoa));
    }
}
