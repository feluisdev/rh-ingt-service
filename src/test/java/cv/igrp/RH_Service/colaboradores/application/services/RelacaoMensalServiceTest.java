package cv.igrp.RH_Service.colaboradores.application.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import cv.igrp.RH_Service.colaboradores.application.queries.GetRelacaoMensalCsvQueryHandler;
import cv.igrp.RH_Service.colaboradores.domain.models.Assignment;
import cv.igrp.RH_Service.colaboradores.domain.models.Funcionario;
import cv.igrp.RH_Service.colaboradores.domain.models.LicencaMobilidade;
import cv.igrp.RH_Service.colaboradores.domain.models.OpcaoFaltaInjustificada;
import cv.igrp.RH_Service.colaboradores.domain.models.PedidoAusencia;
import cv.igrp.RH_Service.colaboradores.domain.models.SubtipoLicencaMobilidade;
import cv.igrp.RH_Service.colaboradores.domain.models.TipoAusencia;
import cv.igrp.RH_Service.colaboradores.domain.models.TipoDiaSuplementar;
import cv.igrp.RH_Service.colaboradores.domain.repository.AssignmentRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.LicencaMobilidadeRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.PedidoAusenciaRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.TipoAusenciaRepository;
import cv.igrp.RH_Service.colaboradores.domain.service.ApuramentoFaltas;
import cv.igrp.RH_Service.colaboradores.domain.service.DiasUteisCalculator;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.TipoAusenciaId;
import cv.igrp.RH_Service.estrutura.domain.models.OrganizationalUnit;
import cv.igrp.RH_Service.estrutura.domain.repository.OrganizationalUnitRepository;
import cv.igrp.RH_Service.estrutura.domain.valueobject.OrganizationalUnitId;
import cv.igrp.RH_Service.parametrizacoes.domain.models.ContagemDias;
import cv.igrp.RH_Service.parametrizacoes.domain.models.EfeitoNaRemuneracao;
import cv.igrp.RH_Service.parametrizacoes.domain.models.RegimeAusencia;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/**
 * DL n.o 3/2010, art. 75.o: quem entra (afectacoes do mes, cessados incluidos, uma unidade por pessoa),
 * o que conta (ferias, faltas por natureza cortadas ao mes, licencas em dias de calendario, apuramento,
 * trabalho suplementar) e as pendencias.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RelacaoMensalServiceTest {

    private static final YearMonth AGOSTO = YearMonth.of(2026, 8);   // 1 de Agosto e um sabado
    private static final LocalDate DE = AGOSTO.atDay(1);
    private static final LocalDate ATE = AGOSTO.atEndOfMonth();

    @Mock private OrganizationalUnitRepository unidadeRepository;
    @Mock private AssignmentRepository assignmentRepository;
    @Mock private FuncionarioRepository funcionarioRepository;
    @Mock private PedidoAusenciaRepository pedidoAusenciaRepository;
    @Mock private TipoAusenciaRepository tipoAusenciaRepository;
    @Mock private LicencaMobilidadeRepository licencaRepository;
    @Mock private MobilidadeService mobilidadeService;
    @Mock private CalendarioFeriadosService calendarioFeriadosService;
    @Mock private ApuramentoFaltasService apuramentoFaltasService;
    @Mock private TrabalhoSuplementarService trabalhoSuplementarService;

    private RelacaoMensalService service;

    private final UUID raizId = UUID.randomUUID();
    private final UUID filhaId = UUID.randomUUID();
    private OrganizationalUnit raiz;
    private final FuncionarioId maria = FuncionarioId.gerarNovo();
    private final FuncionarioId francisco = FuncionarioId.gerarNovo();
    private final FuncionarioId joana = FuncionarioId.gerarNovo();
    private Funcionario fFrancisco;

    private OrganizationalUnit unidade(UUID id, UUID pai, String nome) {
        var u = mock(OrganizationalUnit.class);
        when(u.getId()).thenReturn(OrganizationalUnitId.from(id));
        when(u.getParentUnitId()).thenReturn(pai != null ? OrganizationalUnitId.from(pai) : null);
        when(u.getName()).thenReturn(nome);
        when(u.getCode()).thenReturn(nome.toUpperCase());
        return u;
    }

    private Assignment afectacao(FuncionarioId quem, LocalDate inicio, LocalDate fim) {
        var a = mock(Assignment.class);
        when(a.getFuncionarioId()).thenReturn(quem);
        when(a.isPrincipal()).thenReturn(true);
        when(a.getDataInicio()).thenReturn(inicio);
        when(a.getDataFim()).thenReturn(fim);
        return a;
    }

    private Funcionario pessoa(FuncionarioId id, String nome) {
        var f = mock(Funcionario.class);
        when(f.getId()).thenReturn(id);
        when(f.getNomeCompleto()).thenReturn(nome);
        when(f.getNumeroFuncionario()).thenReturn(nome.substring(0, 3));
        when(f.getDataAdmissao()).thenReturn(LocalDate.of(2020, 1, 1));
        return f;
    }

    private TipoAusencia tipo(String codigo, RegimeAusencia regime, ContagemDias contagem, boolean ferias) {
        var t = mock(TipoAusencia.class);
        var id = TipoAusenciaId.gerarNovo();
        when(t.getId()).thenReturn(id);
        when(t.getCodigo()).thenReturn(codigo);
        when(t.getNome()).thenReturn(codigo.toLowerCase());
        when(t.getRegime()).thenReturn(regime);
        when(t.getContagem()).thenReturn(contagem);
        when(t.isFerias()).thenReturn(ferias);
        when(t.getEfeitoRemuneracao()).thenReturn(EfeitoNaRemuneracao.SEM_PERDA);
        when(tipoAusenciaRepository.findById(id)).thenReturn(Optional.of(t));
        return t;
    }

    private PedidoAusencia pedido(TipoAusencia t, LocalDate de, LocalDate ate) {
        var p = mock(PedidoAusencia.class);
        var id = t.getId();
        when(p.getTipoAusenciaId()).thenReturn(id);
        when(p.getDataInicio()).thenReturn(de);
        when(p.getDataFim()).thenReturn(ate);
        when(p.ultimoDiaEmVigor()).thenReturn(ate);
        return p;
    }

    private static ApuramentoFaltasService.Apuramento apuramento(int semRegisto, String parciais, int porCorrigir) {
        BigDecimal p = new BigDecimal(parciais);
        return new ApuramentoFaltasService.Apuramento(AGOSTO, false, new ApuramentoFaltas.Resultado(List.of(), List.of(),
                semRegisto, 0, 480, p, p.add(BigDecimal.valueOf(semRegisto)), porCorrigir, 0));
    }

    @BeforeEach
    void base() {
        service = new RelacaoMensalService(unidadeRepository, assignmentRepository, funcionarioRepository,
                pedidoAusenciaRepository, tipoAusenciaRepository, licencaRepository, mobilidadeService,
                calendarioFeriadosService, apuramentoFaltasService, trabalhoSuplementarService, new DiasUteisCalculator()) {
            @Override LocalDate hoje() { return LocalDate.of(2026, 9, 23); }
        };
        raiz = unidade(raizId, null, "Direccao");
        var filha = unidade(filhaId, raizId, "Servico");
        when(unidadeRepository.findById(OrganizationalUnitId.from(raizId))).thenReturn(Optional.of(raiz));
        when(unidadeRepository.findAllActive()).thenReturn(List.of(raiz, filha));

        // Francisco na direccao, cessou a 15; a Joana mudou da direccao para o servico a 11; a Maria no servico.
        var aFrancisco = afectacao(francisco, LocalDate.of(2020, 1, 1), LocalDate.of(2026, 8, 15));
        var aJoana1 = afectacao(joana, LocalDate.of(2021, 1, 1), LocalDate.of(2026, 8, 10));
        var aJoana2 = afectacao(joana, LocalDate.of(2026, 8, 11), null);
        var aMaria = afectacao(maria, LocalDate.of(2022, 1, 1), null);
        when(assignmentRepository.findAllByUnidadeOrganicaEntre(raizId, DE, ATE)).thenReturn(List.of(aFrancisco, aJoana1));
        when(assignmentRepository.findAllByUnidadeOrganicaEntre(filhaId, DE, ATE)).thenReturn(List.of(aJoana2, aMaria));

        fFrancisco = pessoa(francisco, "Francisco");
        var fMaria = pessoa(maria, "Maria");
        var fJoana = pessoa(joana, "Joana");
        when(funcionarioRepository.findAllByIds(any())).thenReturn(List.of(fFrancisco, fMaria, fJoana));
        when(apuramentoFaltasService.fimDoVinculo(fFrancisco)).thenReturn(LocalDate.of(2026, 8, 15));

        when(calendarioFeriadosService.feriadosDoColaborador(any(), any(), any())).thenReturn(Set.of());
        when(apuramentoFaltasService.apurar(any(), eq(AGOSTO))).thenReturn(apuramento(0, "0", 0));
        Map<TipoDiaSuplementar, Integer> vazio = new EnumMap<>(TipoDiaSuplementar.class);
        when(trabalhoSuplementarService.doMes(any(), eq(AGOSTO)))
                .thenReturn(new TrabalhoSuplementarService.Mes(AGOSTO, List.of(), vazio, 0, 0));
    }

    private RelacaoMensalService.Linha linha(RelacaoMensalService.Relacao r, FuncionarioId quem) {
        return r.unidades().stream().flatMap(u -> u.linhas().stream())
                .filter(l -> l.funcionario().getId().equals(quem)).findFirst().orElseThrow();
    }

    @Test
    void cadaPessoaUmaVezNaUnidadeQueChegaMaisLonge() {
        var r = service.relacao(AGOSTO, raizId, true);
        assertFalse(r.provisoria());
        assertEquals(2, r.unidades().size());
        assertEquals(raizId, r.unidades().get(0).unidade().getId().getValor());
        assertEquals(List.of(francisco), r.unidades().get(0).linhas().stream().map(l -> l.funcionario().getId()).toList());
        // Joana (J) antes de Maria (M): por nome.
        assertEquals(List.of(joana, maria), r.unidades().get(1).linhas().stream().map(l -> l.funcionario().getId()).toList());
    }

    @Test
    void semSubunidadesSoAUnidade() {
        var r = service.relacao(AGOSTO, raizId, false);
        assertEquals(1, r.unidades().size());
        assertEquals(2, r.unidades().get(0).linhas().size());   // o Francisco e a Joana, que la esteve ate dia 10
    }

    @Test
    void quemCessouAMeioTemOsDiasForaDoVinculoEALicencaCortada() {
        var licenca = mock(LicencaMobilidade.class);
        when(licenca.isApproved()).thenReturn(true);
        when(licenca.getDataInicio()).thenReturn(LocalDate.of(2026, 7, 20));
        when(licenca.getDataFim()).thenReturn(null);
        var subtipo = mock(SubtipoLicencaMobilidade.class);
        when(subtipo.getId()).thenReturn(cv.igrp.RH_Service.colaboradores.domain.valueobject.SubtipoLicencaMobilidadeId.gerarNovo());
        when(subtipo.getCodigo()).thenReturn("LIC_SV");
        when(subtipo.getAffectsPay()).thenReturn(true);
        when(mobilidadeService.subtipoSeExistir(licenca)).thenReturn(Optional.of(subtipo));
        when(licencaRepository.findAllByFuncionarioId(eq(francisco), any())).thenReturn(List.of(licenca));

        var l = linha(service.relacao(AGOSTO, raizId, true), francisco);
        assertEquals(16, l.diasForaDoVinculo());        // 16 a 31
        assertEquals(1, l.licencas().size());
        assertEquals(15, l.licencas().get(0).dias());   // 1 a 15, dias de calendario
        assertEquals("LICENCA", l.licencas().get(0).tipoRegisto());
        assertTrue(l.licencas().get(0).afectaRemuneracao());
    }

    @Test
    void feriasEFaltasPorNaturezaCortadasAoMes() {
        var ferias = tipo("FERIAS", RegimeAusencia.FERIAS, ContagemDias.DIAS_UTEIS, true);
        var luto = tipo("LUTO", RegimeAusencia.FALTA, ContagemDias.DIAS_SEGUIDOS, false);
        var injust = tipo("FALTA_INJ", RegimeAusencia.FALTA_INJUSTIFICADA, ContagemDias.DIAS_UTEIS, false);
        var trat = tipo("TRAT", RegimeAusencia.FALTA, ContagemDias.DIAS_UTEIS, false);
        var pFerias = pedido(ferias, LocalDate.of(2026, 8, 10), LocalDate.of(2026, 8, 12));      // seg-qua: 3
        var pLuto = pedido(luto, LocalDate.of(2026, 7, 30), LocalDate.of(2026, 8, 4));          // corte 1-4: seguidos de seg 3 a ter 4 = 2
        var pInj = pedido(injust, LocalDate.of(2026, 8, 17), LocalDate.of(2026, 8, 17));
        when(pInj.getOpcaoFaltaInjustificada()).thenReturn(OpcaoFaltaInjustificada.PERDA_REMUNERACAO);
        var pTrat = pedido(trat, LocalDate.of(2026, 8, 18), LocalDate.of(2026, 8, 19));
        when(pTrat.isEmHoras()).thenReturn(true);
        when(pTrat.minutosPorDia()).thenReturn(60);
        when(pedidoAusenciaRepository.findAprovadosEntre(eq(maria), any(), any())).thenReturn(List.of(pFerias, pLuto, pInj, pTrat));

        var l = linha(service.relacao(AGOSTO, raizId, true), maria);
        assertEquals(3, l.diasFerias());
        assertEquals(2, l.faltasJustificadas().size());
        var rLuto = l.faltasJustificadas().stream().filter(x -> x.codigo().equals("LUTO")).findFirst().orElseThrow();
        assertEquals(2, rLuto.dias());
        var rTrat = l.faltasJustificadas().stream().filter(x -> x.codigo().equals("TRAT")).findFirst().orElseThrow();
        assertEquals(0, rTrat.dias());
        assertEquals(120, rTrat.minutos());
        assertEquals(1, l.faltasInjustificadas().size());
        assertEquals("PERDA_REMUNERACAO", l.faltasInjustificadas().get(0).opcaoFaltaInjustificada());
        assertEquals(1, l.faltasInjustificadas().get(0).dias());
        assertEquals(0, l.diasForaDoVinculo());
    }

    @Test
    void pendenciasDoApuramentoOuPedidosPorDecidir() {
        when(apuramentoFaltasService.apurar(eq(maria), eq(AGOSTO))).thenReturn(apuramento(2, "0.5", 1));
        var pendente = mock(PedidoAusencia.class);
        when(pendente.getDataInicio()).thenReturn(LocalDate.of(2026, 8, 30));
        when(pendente.getDataFim()).thenReturn(LocalDate.of(2026, 9, 2));
        when(pedidoAusenciaRepository.findAllByFuncionarioId(eq(joana), any())).thenReturn(List.of(pendente));
        Map<TipoDiaSuplementar, Integer> sup = new EnumMap<>(TipoDiaSuplementar.class);
        sup.put(TipoDiaSuplementar.DIA_UTIL, 90);
        when(trabalhoSuplementarService.doMes(eq(maria), eq(AGOSTO))).thenReturn(new TrabalhoSuplementarService.Mes(AGOSTO, List.of(), sup, 120, 90));

        var r = service.relacao(AGOSTO, raizId, true);
        var lMaria = linha(r, maria);
        assertEquals(RelacaoMensalService.EstadoLinha.COM_PENDENCIAS, lMaria.estado());
        assertEquals(new BigDecimal("2.5"), lMaria.faltasPorJustificar());
        assertEquals(90, lMaria.suplementarPorTipo().get(TipoDiaSuplementar.DIA_UTIL));
        var lJoana = linha(r, joana);
        assertEquals(1, lJoana.pedidosPendentes());
        assertEquals(RelacaoMensalService.EstadoLinha.COM_PENDENCIAS, lJoana.estado());
        assertEquals(RelacaoMensalService.EstadoLinha.COMPLETA, linha(r, francisco).estado());
        assertEquals(2, r.unidades().get(1).comPendencias());
        assertEquals(new BigDecimal("2.5"), r.unidades().get(1).faltasPorJustificar());
    }

    @Test
    void mesCorrenteEProvisorioEFuturoOuSemUnidadeE422() {
        assertTrue(service.relacao(YearMonth.of(2026, 9), raizId, true).provisoria());
        var futuro = assertThrows(IgrpResponseStatusException.class, () -> service.relacao(YearMonth.of(2026, 10), raizId, true));
        assertEquals(422, futuro.getStatusCode().value());
        var semUnidade = assertThrows(IgrpResponseStatusException.class, () -> service.relacao(AGOSTO, null, true));
        assertEquals(422, semUnidade.getStatusCode().value());
        var inexistente = assertThrows(IgrpResponseStatusException.class, () -> service.relacao(AGOSTO, UUID.randomUUID(), true));
        assertEquals(404, inexistente.getStatusCode().value());
    }

    @Test
    void csvComCabecalhoUmaLinhaPorPessoaEDecimaisComVirgula() {
        when(apuramentoFaltasService.apurar(eq(maria), eq(AGOSTO))).thenReturn(apuramento(1, "0.5", 0));
        String csv = GetRelacaoMensalCsvQueryHandler.csv(service.relacao(AGOSTO, raizId, true));
        String[] linhas = csv.split("\r\n");
        assertTrue(linhas[0].startsWith("﻿mes;provisoria;unidade_codigo"));
        assertEquals(4, linhas.length);   // cabecalho + 3 pessoas
        String daMaria = java.util.Arrays.stream(linhas).filter(x -> x.contains(";Maria;")).findFirst().orElseThrow();
        assertTrue(daMaria.contains(";0,5;1,5;"), daMaria);
        assertEquals("\"a;b\"", GetRelacaoMensalCsvQueryHandler.t("a;b"));
    }
}
