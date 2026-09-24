package cv.igrp.RH_Service.colaboradores.application.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import cv.igrp.RH_Service.carreiras.domain.repository.CareerRepository;
import cv.igrp.RH_Service.carreiras.domain.repository.CategoryRepository;
import cv.igrp.RH_Service.carreiras.domain.repository.GradeRepository;
import cv.igrp.RH_Service.colaboradores.domain.models.Assignment;
import cv.igrp.RH_Service.colaboradores.domain.models.Funcionario;
import cv.igrp.RH_Service.colaboradores.domain.models.PedidoAusencia;
import cv.igrp.RH_Service.colaboradores.domain.models.TipoAusencia;
import cv.igrp.RH_Service.colaboradores.domain.models.TipoDiaSuplementar;
import cv.igrp.RH_Service.colaboradores.domain.repository.ContratoRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.PedidoAusenciaRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.TipoAusenciaRepository;
import cv.igrp.RH_Service.colaboradores.domain.service.DiasUteisCalculator;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.TipoAusenciaId;
import cv.igrp.RH_Service.estrutura.domain.models.OrganizationalUnit;
import cv.igrp.RH_Service.estrutura.domain.valueobject.OrganizationalUnitId;
import cv.igrp.RH_Service.parametrizacoes.domain.models.RegimeAusencia;
import cv.igrp.RH_Service.parametrizacoes.domain.repository.ContractTypeRepository;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
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
 * Lei n.o 20/X/2023, art. 38.o n.o 3: efectivos por genero e escalao etario, entradas e saidas no ano,
 * absentismo (faltas aprovadas em dias uteis sobre os dias uteis de vinculo) e horas extras.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class IndicadoresPessoalServiceTest {

    private static final int ANO = 2025;   // um ano passado: referencia a 31/12/2025

    @Mock private QuemEstaNoServico quemEstaNoServico;
    @Mock private FuncionarioRepository funcionarioRepository;
    @Mock private ContratoRepository contratoRepository;
    @Mock private ContractTypeRepository contractTypeRepository;
    @Mock private GradeRepository gradeRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private CareerRepository careerRepository;
    @Mock private PedidoAusenciaRepository pedidoAusenciaRepository;
    @Mock private TipoAusenciaRepository tipoAusenciaRepository;
    @Mock private CalendarioFeriadosService calendarioFeriadosService;
    @Mock private ApuramentoFaltasService apuramentoFaltasService;
    @Mock private TrabalhoSuplementarService trabalhoSuplementarService;

    private IndicadoresPessoalService service;
    private final UUID unidadeId = UUID.randomUUID();
    private final Map<UUID, QuemEstaNoServico.Colocacao> naRef = new LinkedHashMap<>();
    private final Map<UUID, QuemEstaNoServico.Colocacao> noAno = new LinkedHashMap<>();
    private final List<Funcionario> pessoas = new ArrayList<>();

    private Funcionario pessoa(String genero, LocalDate nascimento, LocalDate admissao, boolean aindaLa) {
        var id = FuncionarioId.gerarNovo();
        var f = mock(Funcionario.class);
        when(f.getId()).thenReturn(id);
        when(f.getGenero()).thenReturn(genero);
        when(f.getDataNascimento()).thenReturn(nascimento);
        when(f.getDataAdmissao()).thenReturn(admissao);
        pessoas.add(f);
        var a = mock(Assignment.class);
        var c = new QuemEstaNoServico.Colocacao(a, unidadeId);
        noAno.put(id.getValor(), c);
        if (aindaLa) naRef.put(id.getValor(), c);
        return f;
    }

    @BeforeEach
    void base() {
        service = new IndicadoresPessoalService(quemEstaNoServico, funcionarioRepository, contratoRepository, contractTypeRepository,
                gradeRepository, categoryRepository, careerRepository, pedidoAusenciaRepository, tipoAusenciaRepository,
                calendarioFeriadosService, new DiasUteisCalculator(), apuramentoFaltasService, trabalhoSuplementarService) {
            @Override protected LocalDate hoje() { return LocalDate.of(2026, 9, 24); }
        };
        var u = OrganizationalUnit.reconstruir(OrganizationalUnitId.from(unidadeId), "SRH", "Servico de RH", "SRH",
                null, null, null, null, null, true);
        when(quemEstaNoServico.unidades(unidadeId, true)).thenReturn(List.of(u));
        LocalDate ref = LocalDate.of(ANO, 12, 31);
        when(quemEstaNoServico.colocacoes(any(), eq(ref), eq(ref))).thenReturn(naRef);
        when(quemEstaNoServico.colocacoes(any(), eq(LocalDate.of(ANO, 1, 1)), eq(ref))).thenReturn(noAno);
        when(funcionarioRepository.findAllByIds(any())).thenReturn(pessoas);
        when(calendarioFeriadosService.feriadosDoColaborador(any(), any(), any())).thenReturn(Set.of());
        when(contratoRepository.findCurrentByFuncionarioId(any())).thenReturn(Optional.empty());
        Map<TipoDiaSuplementar, Integer> vazio = new EnumMap<>(TipoDiaSuplementar.class);
        when(trabalhoSuplementarService.doMes(any(), any())).thenReturn(new TrabalhoSuplementarService.Mes(YearMonth.of(ANO, 1), List.of(), vazio, 0, 0));
    }

    @Test
    void efectivosEntradasSaidasEAbsentismo() {
        var maria = pessoa("F", LocalDate.of(1990, 5, 1), LocalDate.of(2015, 6, 1), true);   // 35 anos
        pessoa("M", LocalDate.of(1970, 1, 1), LocalDate.of(2025, 3, 1), true);              // 55, entrou em 2025
        var joana = pessoa("F", LocalDate.of(2000, 1, 1), LocalDate.of(2020, 1, 1), false);  // saiu em 2025
        when(apuramentoFaltasService.fimDoVinculo(joana)).thenReturn(LocalDate.of(ANO, 6, 30));
        // A Maria teve 5 dias uteis de falta justificada aprovada (e ferias, que nao contam).
        var falta = mock(TipoAusencia.class);
        when(falta.getRegime()).thenReturn(RegimeAusencia.FALTA);
        var ferias = mock(TipoAusencia.class);
        when(ferias.isFerias()).thenReturn(true);
        var tFalta = TipoAusenciaId.gerarNovo();
        var tFerias = TipoAusenciaId.gerarNovo();
        when(tipoAusenciaRepository.findById(tFalta)).thenReturn(Optional.of(falta));
        when(tipoAusenciaRepository.findById(tFerias)).thenReturn(Optional.of(ferias));
        var p1 = mock(PedidoAusencia.class);
        when(p1.getTipoAusenciaId()).thenReturn(tFalta);
        when(p1.getDataInicio()).thenReturn(LocalDate.of(ANO, 3, 3));      // segunda
        when(p1.ultimoDiaEmVigor()).thenReturn(LocalDate.of(ANO, 3, 7));   // sexta
        var p2 = mock(PedidoAusencia.class);
        when(p2.getTipoAusenciaId()).thenReturn(tFerias);
        when(p2.getDataInicio()).thenReturn(LocalDate.of(ANO, 8, 4));
        when(p2.ultimoDiaEmVigor()).thenReturn(LocalDate.of(ANO, 8, 22));
        when(pedidoAusenciaRepository.findAprovadosEntre(eq(maria.getId()), any(), any())).thenReturn(List.of(p1, p2));

        var i = service.indicadores(unidadeId, true, ANO);

        assertEquals(2, i.efectivos());
        assertEquals(Map.of("F", 1, "M", 1), i.porGenero());
        assertEquals(1, i.porEscalaoEtario().get("30-39"));
        assertEquals(1, i.porEscalaoEtario().get("50-59"));
        assertEquals(2, i.porTipoContrato().get("Sem contrato"));   // os dois sem contrato
        assertEquals(1, i.entradas());
        assertEquals(1, i.saidas());
        assertEquals(5, i.diasFalta());
        // 261 dias uteis em 2025 (Maria) + de 3 de Marco ao fim do ano (o outro).
        assertEquals(261 + 218, i.diasUteisPotenciais());
        assertEquals(new BigDecimal("1.04"), i.taxaAbsentismo());
    }

    @Test
    void anoFuturoOuSemUnidadeE422() {
        assertEquals(422, assertThrows(IgrpResponseStatusException.class, () -> service.indicadores(unidadeId, true, 2027)).getStatusCode().value());
        assertEquals(422, assertThrows(IgrpResponseStatusException.class, () -> service.indicadores(null, true, ANO)).getStatusCode().value());
    }

    @Test
    void escalaoEtario() {
        assertEquals("<30", IndicadoresPessoalService.escalaoEtario(LocalDate.of(2000, 1, 1), LocalDate.of(2025, 12, 31)));
        assertEquals("60+", IndicadoresPessoalService.escalaoEtario(LocalDate.of(1960, 1, 1), LocalDate.of(2025, 12, 31)));
        assertEquals("Sem data", IndicadoresPessoalService.escalaoEtario(null, LocalDate.of(2025, 12, 31)));
    }
}
