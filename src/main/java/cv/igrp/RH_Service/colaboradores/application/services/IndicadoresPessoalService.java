package cv.igrp.RH_Service.colaboradores.application.services;

import cv.igrp.RH_Service.carreiras.domain.models.Grade;
import cv.igrp.RH_Service.carreiras.domain.repository.CareerRepository;
import cv.igrp.RH_Service.carreiras.domain.repository.CategoryRepository;
import cv.igrp.RH_Service.carreiras.domain.repository.GradeRepository;
import cv.igrp.RH_Service.carreiras.domain.valueobject.GradeId;
import cv.igrp.RH_Service.colaboradores.domain.models.Funcionario;
import cv.igrp.RH_Service.colaboradores.domain.models.PedidoAusencia;
import cv.igrp.RH_Service.colaboradores.domain.models.TipoAusencia;
import cv.igrp.RH_Service.colaboradores.domain.repository.ContratoRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.PedidoAusenciaRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.TipoAusenciaRepository;
import cv.igrp.RH_Service.colaboradores.domain.service.DiasUteisCalculator;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.TipoAusenciaId;
import cv.igrp.RH_Service.estrutura.domain.models.OrganizationalUnit;
import cv.igrp.RH_Service.parametrizacoes.domain.models.ContagemDias;
import cv.igrp.RH_Service.parametrizacoes.domain.models.ContractType;
import cv.igrp.RH_Service.parametrizacoes.domain.models.RegimeAusencia;
import cv.igrp.RH_Service.parametrizacoes.domain.repository.ContractTypeRepository;
import cv.igrp.RH_Service.parametrizacoes.domain.valueobject.ContractTypeId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Period;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;

/**
 * <b>Indicadores do pessoal</b> — Lei n.º 20/X/2023, art. 38.º n.º 3: os relatórios de actividades
 * publicitam «dados e indicadores sobre o pessoal existente, independentemente da natureza do vínculo».
 * É o balanço social da indústria, para um serviço e um ano: os efectivos (por género, escalão etário,
 * tipo de contrato, carreira e unidade), as entradas e as saídas, o absentismo e as horas extras.
 *
 * <p>Os números devolvem-se; o gráfico é do front. Referência: hoje, no ano corrente; 31 de Dezembro,
 * num ano passado. O <b>absentismo</b> conta as faltas das ausências aprovadas (regime FALTA e FALTA
 * INJUSTIFICADA, sem férias) em dias úteis, sobre os dias úteis de vínculo no ano — as faltas por
 * débito do apuramento não entram (vêm da relação mensal). Só leitura.
 */
@Service
@RequiredArgsConstructor
public class IndicadoresPessoalService {

    public record Indicadores(int ano, LocalDate referencia, OrganizationalUnit raiz, boolean incluirSubunidades,
                              int efectivos, Map<String, Integer> porGenero, Map<String, Integer> porEscalaoEtario,
                              Map<String, Integer> porTipoContrato, Map<String, Integer> porCarreira,
                              Map<String, Integer> porUnidade, int entradas, int saidas,
                              int diasFalta, int diasUteisPotenciais, BigDecimal taxaAbsentismo,
                              int minutosSuplementares) {}

    private final QuemEstaNoServico quemEstaNoServico;
    private final FuncionarioRepository funcionarioRepository;
    private final ContratoRepository contratoRepository;
    private final ContractTypeRepository contractTypeRepository;
    private final GradeRepository gradeRepository;
    private final CategoryRepository categoryRepository;
    private final CareerRepository careerRepository;
    private final PedidoAusenciaRepository pedidoAusenciaRepository;
    private final TipoAusenciaRepository tipoAusenciaRepository;
    private final CalendarioFeriadosService calendarioFeriadosService;
    private final DiasUteisCalculator diasUteisCalculator;
    private final ApuramentoFaltasService apuramentoFaltasService;
    private final TrabalhoSuplementarService trabalhoSuplementarService;

    @Transactional(readOnly = true)
    public Indicadores indicadores(UUID unidadeId, boolean incluirSubunidades, Integer ano) {
        if (unidadeId == null) throw invalido("A unidade orgânica é obrigatória: os indicadores são de cada serviço.");
        LocalDate hoje = hoje();
        int anoRef = ano != null ? ano : hoje.getYear();
        if (anoRef > hoje.getYear()) throw invalido("Não há indicadores de um ano que ainda não começou: " + anoRef + ".");
        LocalDate referencia = anoRef < hoje.getYear() ? LocalDate.of(anoRef, 12, 31) : hoje;
        LocalDate inicioAno = LocalDate.of(anoRef, 1, 1);

        List<OrganizationalUnit> unidades = quemEstaNoServico.unidades(unidadeId, incluirSubunidades);
        Map<UUID, String> nomeUnidade = new HashMap<>();
        unidades.forEach(u -> nomeUnidade.put(u.getId().getValor(), u.getName()));

        var naReferencia = quemEstaNoServico.colocacoes(unidades, referencia, referencia);
        var noAno = quemEstaNoServico.colocacoes(unidades, inicioAno, referencia);
        Map<UUID, Funcionario> pessoas = new HashMap<>();
        for (Funcionario f : funcionarioRepository.findAllByIds(noAno.keySet())) pessoas.put(f.getId().getValor(), f);

        Map<String, Integer> genero = new TreeMap<>(), etario = new LinkedHashMap<>(), contrato = new TreeMap<>(),
                carreira = new TreeMap<>(), unidade = new TreeMap<>();
        for (String escalao : List.of("<30", "30-39", "40-49", "50-59", "60+")) etario.put(escalao, 0);
        Map<TipoAusenciaId, Optional<TipoAusencia>> tipos = new HashMap<>();
        int efectivos = 0, diasFalta = 0, potenciais = 0, minutosSuplementares = 0;

        for (var e : naReferencia.entrySet()) {
            Funcionario f = pessoas.get(e.getKey());
            if (f == null) continue;
            efectivos++;
            genero.merge(genero(f.getGenero()), 1, Integer::sum);
            etario.merge(escalaoEtario(f.getDataNascimento(), referencia), 1, Integer::sum);
            contrato.merge(tipoDeContrato(f), 1, Integer::sum);
            carreira.merge(carreiraDe(e.getValue().afectacao().getGradeId()), 1, Integer::sum);
            unidade.merge(texto(nomeUnidade.get(e.getValue().unidadeId()), "?"), 1, Integer::sum);

            // Absentismo: dias úteis de falta aprovada sobre os dias úteis de vínculo no ano.
            LocalDate de = f.getDataAdmissao() != null && f.getDataAdmissao().isAfter(inicioAno) ? f.getDataAdmissao() : inicioAno;
            if (!de.isAfter(referencia)) {
                Set<LocalDate> feriados = calendarioFeriadosService.feriadosDoColaborador(f.getId(), de, referencia);
                potenciais += contar(de, referencia, feriados);
                for (PedidoAusencia p : pedidoAusenciaRepository.findAprovadosEntre(f.getId(), de, referencia)) {
                    if (p.isEmHoras()) continue;
                    TipoAusencia t = tipos.computeIfAbsent(p.getTipoAusenciaId(), tipoAusenciaRepository::findById).orElse(null);
                    if (t == null || t.isFerias()
                            || (t.getRegime() != RegimeAusencia.FALTA && t.getRegime() != RegimeAusencia.FALTA_INJUSTIFICADA)) continue;
                    LocalDate pi = p.getDataInicio().isBefore(de) ? de : p.getDataInicio();
                    LocalDate pf = p.ultimoDiaEmVigor().isAfter(referencia) ? referencia : p.ultimoDiaEmVigor();
                    if (!pi.isAfter(pf)) diasFalta += contar(pi, pf, feriados);
                }
            }
            for (YearMonth m = YearMonth.from(inicioAno); !m.isAfter(YearMonth.from(referencia)); m = m.plusMonths(1))
                minutosSuplementares += trabalhoSuplementarService.doMes(f.getId(), m).minutosRealizados();
        }

        int entradas = 0, saidas = 0;
        for (var e : noAno.entrySet()) {
            Funcionario f = pessoas.get(e.getKey());
            if (f == null) continue;
            if (f.getDataAdmissao() != null && !f.getDataAdmissao().isBefore(inicioAno) && !f.getDataAdmissao().isAfter(referencia))
                entradas++;
            LocalDate fim = apuramentoFaltasService.fimDoVinculo(f);
            if (fim != null && !fim.isBefore(inicioAno) && !fim.isAfter(referencia)) saidas++;
        }

        BigDecimal taxa = potenciais == 0 ? BigDecimal.ZERO
                : BigDecimal.valueOf(diasFalta * 100L).divide(BigDecimal.valueOf(potenciais), 2, RoundingMode.HALF_UP);
        return new Indicadores(anoRef, referencia, unidades.get(0), incluirSubunidades, efectivos, genero, etario, contrato,
                carreira, unidade, entradas, saidas, diasFalta, potenciais, taxa, minutosSuplementares);
    }

    /** O catálogo SEX guarda «F»/«M»; o enum {@code Sexo} e o seed, «FEMININO»/«MASCULINO»: contam juntos. */
    static String genero(String g) {
        if (g == null || g.isBlank()) return "Não indicado";
        String v = g.trim().toUpperCase();
        if (v.equals("F") || v.equals("FEMININO")) return "F";
        if (v.equals("M") || v.equals("MASCULINO")) return "M";
        return g.trim();
    }

    static String escalaoEtario(LocalDate nascimento, LocalDate referencia) {
        if (nascimento == null) return "Sem data";
        int idade = Period.between(nascimento, referencia).getYears();
        if (idade < 30) return "<30";
        if (idade < 40) return "30-39";
        if (idade < 50) return "40-49";
        if (idade < 60) return "50-59";
        return "60+";
    }

    private String tipoDeContrato(Funcionario f) {
        return contratoRepository.findCurrentByFuncionarioId(f.getId())
                .map(c -> c.getContractTypeId() == null ? null
                        : contractTypeRepository.findById(ContractTypeId.from(c.getContractTypeId())).map(ContractType::getDescription).orElse(null))
                .map(d -> texto(d, "Sem tipo"))
                .orElse("Sem contrato");
    }

    private String carreiraDe(UUID gradeId) {
        if (gradeId == null) return "Fora de grelha";
        return gradeRepository.findById(GradeId.from(gradeId)).map(Grade::getCategoryId)
                .flatMap(categoryRepository::findById)
                .flatMap(c -> c.getCareerId() != null ? careerRepository.findById(c.getCareerId()) : Optional.empty())
                .map(c -> texto(c.getName(), "?"))
                .orElse("Fora de grelha");
    }

    private int contar(LocalDate de, LocalDate ate, Set<LocalDate> feriados) {
        try {
            return diasUteisCalculator.calcular(de, ate, feriados, ContagemDias.DIAS_UTEIS);
        } catch (IgrpResponseStatusException e) {
            return 0;
        }
    }

    private static String texto(String s, String omissao) {
        return s == null || s.isBlank() ? omissao : s;
    }

    protected LocalDate hoje() { return LocalDate.now(); }

    private static IgrpResponseStatusException invalido(String mensagem) {
        return IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY, mensagem);
    }
}
