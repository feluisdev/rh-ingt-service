package cv.igrp.RH_Service.colaboradores.application.services;

import cv.igrp.RH_Service.colaboradores.domain.filter.LicencaMobilidadeFilter;
import cv.igrp.RH_Service.colaboradores.domain.filter.PedidoAusenciaFilter;
import cv.igrp.RH_Service.colaboradores.domain.models.Assignment;
import cv.igrp.RH_Service.colaboradores.domain.models.Funcionario;
import cv.igrp.RH_Service.colaboradores.domain.models.LicencaMobilidade;
import cv.igrp.RH_Service.colaboradores.domain.models.PedidoAusencia;
import cv.igrp.RH_Service.colaboradores.domain.models.SubtipoLicencaMobilidade;
import cv.igrp.RH_Service.colaboradores.domain.models.TipoAusencia;
import cv.igrp.RH_Service.colaboradores.domain.models.TipoDiaSuplementar;
import cv.igrp.RH_Service.colaboradores.domain.repository.AssignmentRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.LicencaMobilidadeRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.PedidoAusenciaRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.TipoAusenciaRepository;
import cv.igrp.RH_Service.colaboradores.domain.service.DiasUteisCalculator;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.TipoAusenciaId;
import cv.igrp.RH_Service.estrutura.domain.models.OrganizationalUnit;
import cv.igrp.RH_Service.estrutura.domain.repository.OrganizationalUnitRepository;
import cv.igrp.RH_Service.estrutura.domain.valueobject.OrganizationalUnitId;
import cv.igrp.RH_Service.parametrizacoes.domain.models.ContagemDias;
import cv.igrp.RH_Service.parametrizacoes.domain.models.RegimeAusencia;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * <b>Relação mensal de assiduidade</b> — DL n.º 3/2010, art. 75.º n.º 1: cada serviço elabora, no fim de
 * cada mês, a relação das faltas e licenças de cada funcionário e da sua natureza, base do vencimento
 * do mês seguinte (n.º 3: e do cômputo das férias do ano seguinte).
 *
 * <p><b>Só leitura</b>: calcula-se a cada pedido, como o apuramento de faltas. O fecho (congelar,
 * bloquear, reabrir) fica para quando houver integração com o processamento salarial.
 *
 * <p>Quem entra: quem teve afectação principal num Lugar da unidade (ou das subunidades) em algum dia
 * do mês, pelas datas da afectação — quem cessou a meio também vem, com os dias fora do vínculo. Cada
 * pessoa aparece uma vez, na unidade da afectação que cobre o último dia do mês em que a teve. Quem
 * está em mobilidade interna <b>para</b> a unidade aparece na de origem, com os dias de mobilidade.
 */
@Service
@RequiredArgsConstructor
public class RelacaoMensalService {

    public enum EstadoLinha { COMPLETA, COM_PENDENCIAS }

    /** Faltas de um tipo no mês: dias (pedidos de dias inteiros, cortados ao mês) e minutos (pedidos em horas). */
    public record Rubrica(String codigo, String nome, int dias, int minutos, String efeitoRemuneracao,
                          String opcaoFaltaInjustificada) {}

    public record RubricaLicenca(String codigo, String nome, String tipoRegisto, int dias, Boolean afectaRemuneracao,
                                 Boolean contaAntiguidade) {}

    public record Linha(Funcionario funcionario, UUID unidadeId, boolean isento, int diasForaDoVinculo, int diasFerias,
                        List<Rubrica> faltasJustificadas, List<Rubrica> faltasInjustificadas,
                        int diasSemRegisto, BigDecimal faltasParciais, BigDecimal faltasPorJustificar,
                        List<RubricaLicenca> licencas, Map<TipoDiaSuplementar, Integer> suplementarPorTipo,
                        int diasPorCorrigir, int diasPorValidar, int pedidosPendentes, EstadoLinha estado) {}

    public record Unidade(OrganizationalUnit unidade, List<Linha> linhas, int comPendencias,
                          BigDecimal faltasPorJustificar) {}

    public record Relacao(YearMonth mes, boolean provisoria, OrganizationalUnit raiz, boolean incluirSubunidades,
                          List<Unidade> unidades) {}

    private final OrganizationalUnitRepository unidadeRepository;
    private final AssignmentRepository assignmentRepository;
    private final FuncionarioRepository funcionarioRepository;
    private final PedidoAusenciaRepository pedidoAusenciaRepository;
    private final TipoAusenciaRepository tipoAusenciaRepository;
    private final LicencaMobilidadeRepository licencaRepository;
    private final MobilidadeService mobilidadeService;
    private final CalendarioFeriadosService calendarioFeriadosService;
    private final ApuramentoFaltasService apuramentoFaltasService;
    private final TrabalhoSuplementarService trabalhoSuplementarService;
    private final DiasUteisCalculator diasUteisCalculator;

    @Transactional(readOnly = true)
    public Relacao relacao(YearMonth mes, UUID unidadeId, boolean incluirSubunidades) {
        if (mes == null) throw invalido("O mês é obrigatório (yyyy-MM).");
        if (unidadeId == null) throw invalido("A unidade orgânica é obrigatória: a relação é de cada serviço (art. 75.º n.º 1).");
        YearMonth corrente = YearMonth.from(hoje());
        if (mes.isAfter(corrente)) throw invalido("Não há relação de um mês que ainda não começou: " + mes + ".");
        OrganizationalUnit raiz = unidadeRepository.findById(OrganizationalUnitId.from(unidadeId))
                .orElseThrow(() -> IgrpResponseStatusException.notFound("Unidade orgânica não encontrada: " + unidadeId));

        LocalDate de = mes.atDay(1);
        LocalDate ate = mes.atEndOfMonth();
        List<OrganizationalUnit> unidades = incluirSubunidades ? comDescendentes(raiz) : List.of(raiz);

        // Uma pessoa, uma unidade: a da afectação principal que chega mais longe no mês.
        Map<UUID, Assignment> daPessoa = new LinkedHashMap<>();
        Map<UUID, UUID> unidadeDaPessoa = new HashMap<>();
        for (OrganizationalUnit u : unidades) {
            for (Assignment a : assignmentRepository.findAllByUnidadeOrganicaEntre(u.getId().getValor(), de, ate)) {
                if (!a.isPrincipal()) continue;
                UUID pessoa = a.getFuncionarioId().getValor();
                Assignment actual = daPessoa.get(pessoa);
                if (actual == null || chegaMaisLonge(a, actual, ate)) {
                    daPessoa.put(pessoa, a);
                    unidadeDaPessoa.put(pessoa, u.getId().getValor());
                }
            }
        }

        Map<UUID, Funcionario> pessoas = new HashMap<>();
        for (Funcionario f : funcionarioRepository.findAllByIds(daPessoa.keySet())) pessoas.put(f.getId().getValor(), f);
        Map<TipoAusenciaId, Optional<TipoAusencia>> tipos = new HashMap<>();

        Map<UUID, List<Linha>> porUnidade = new LinkedHashMap<>();
        for (OrganizationalUnit u : unidades) porUnidade.put(u.getId().getValor(), new ArrayList<>());
        for (var e : unidadeDaPessoa.entrySet()) {
            Funcionario f = pessoas.get(e.getKey());
            if (f == null) continue;
            porUnidade.get(e.getValue()).add(linha(f, e.getValue(), mes, de, ate, tipos));
        }

        List<Unidade> resultado = new ArrayList<>();
        for (OrganizationalUnit u : unidades) {
            List<Linha> linhas = porUnidade.get(u.getId().getValor());
            if (linhas.isEmpty() && !u.getId().equals(raiz.getId())) continue;
            linhas.sort(Comparator.comparing(l -> l.funcionario().getNomeCompleto() == null ? "" : l.funcionario().getNomeCompleto()));
            int comPendencias = (int) linhas.stream().filter(l -> l.estado() == EstadoLinha.COM_PENDENCIAS).count();
            BigDecimal porJustificar = linhas.stream().map(Linha::faltasPorJustificar).reduce(BigDecimal.ZERO, BigDecimal::add);
            resultado.add(new Unidade(u, List.copyOf(linhas), comPendencias, porJustificar));
        }
        return new Relacao(mes, !mes.isBefore(corrente), raiz, incluirSubunidades, resultado);
    }

    private Linha linha(Funcionario f, UUID unidadeId, YearMonth mes, LocalDate de, LocalDate ate,
                        Map<TipoAusenciaId, Optional<TipoAusencia>> tipos) {
        FuncionarioId id = f.getId();
        LocalDate fimDoVinculo = apuramentoFaltasService.fimDoVinculo(f);
        LocalDate inicio = f.getDataAdmissao() != null && f.getDataAdmissao().isAfter(de) ? f.getDataAdmissao() : de;
        LocalDate fim = fimDoVinculo != null && fimDoVinculo.isBefore(ate) ? fimDoVinculo : ate;
        int diasNoMes = ate.getDayOfMonth();
        int foraDoVinculo = inicio.isAfter(fim) ? diasNoMes : diasNoMes - (int) ChronoUnit.DAYS.between(inicio, fim) - 1;

        Set<LocalDate> feriados = calendarioFeriadosService.feriadosDoColaborador(id, de, ate);

        // Férias e faltas: os pedidos aprovados, cortados ao mês e ao vínculo.
        int diasFerias = 0;
        Map<String, int[]> justificadas = new LinkedHashMap<>();
        Map<String, int[]> injustificadas = new LinkedHashMap<>();
        Map<String, TipoAusencia> tipoDaChave = new HashMap<>();
        Map<String, String> opcaoDaChave = new HashMap<>();
        if (!inicio.isAfter(fim)) {
            for (PedidoAusencia p : pedidoAusenciaRepository.findAprovadosEntre(id, inicio, fim)) {
                TipoAusencia tipo = tipos.computeIfAbsent(p.getTipoAusenciaId(), tipoAusenciaRepository::findById).orElse(null);
                if (tipo == null) continue;
                LocalDate pi = p.getDataInicio().isBefore(inicio) ? inicio : p.getDataInicio();
                LocalDate ultimo = p.ultimoDiaEmVigor();
                LocalDate pf = ultimo.isAfter(fim) ? fim : ultimo;
                if (pi.isAfter(pf)) continue;
                int dias = 0, minutos = 0;
                if (p.isEmHoras()) minutos = p.minutosPorDia() * contar(pi, pf, feriados, ContagemDias.DIAS_UTEIS);
                else dias = contar(pi, pf, feriados, tipo.getContagem());

                if (tipo.isFerias()) {
                    diasFerias += dias;
                    continue;
                }
                boolean injustificada = tipo.getRegime() == RegimeAusencia.FALTA_INJUSTIFICADA;
                String opcao = injustificada && p.getOpcaoFaltaInjustificada() != null ? p.getOpcaoFaltaInjustificada().name() : null;
                String chave = tipo.getId().getStringValor() + "|" + opcao;
                tipoDaChave.put(chave, tipo);
                opcaoDaChave.put(chave, opcao);
                int[] soma = (injustificada ? injustificadas : justificadas).computeIfAbsent(chave, k -> new int[2]);
                soma[0] += dias;
                soma[1] += minutos;
            }
        }

        // Licenças e mobilidade: dias de calendário no mês (art. 76.º), dentro do vínculo.
        Map<String, int[]> licencas = new LinkedHashMap<>();
        Map<String, SubtipoLicencaMobilidade> subtipoDaChave = new HashMap<>();
        if (!inicio.isAfter(fim)) {
            for (LicencaMobilidade l : licencaRepository.findAllByFuncionarioId(id, new LicencaMobilidadeFilter())) {
                if (!l.isApproved() || l.getDataInicio() == null) continue;
                LocalDate li = l.getDataInicio().isBefore(inicio) ? inicio : l.getDataInicio();
                LocalDate lf = l.getDataFim() == null || l.getDataFim().isAfter(fim) ? fim : l.getDataFim();
                if (li.isAfter(lf)) continue;
                SubtipoLicencaMobilidade subtipo = mobilidadeService.subtipoSeExistir(l).orElse(null);
                String chave = subtipo != null ? subtipo.getId().getStringValor() : "?";
                subtipoDaChave.putIfAbsent(chave, subtipo);
                licencas.computeIfAbsent(chave, k -> new int[1])[0] += (int) ChronoUnit.DAYS.between(li, lf) + 1;
            }
        }

        var apuramento = apuramentoFaltasService.apurar(id, mes);
        var r = apuramento.resultado();
        var suplementar = trabalhoSuplementarService.doMes(id, mes).realizadosPorTipo();

        PedidoAusenciaFilter pendentesFiltro = new PedidoAusenciaFilter();
        pendentesFiltro.setEstado("PENDENTE");
        int pendentes = (int) pedidoAusenciaRepository.findAllByFuncionarioId(id, pendentesFiltro).stream()
                .filter(p -> !p.getDataInicio().isAfter(ate) && !p.getDataFim().isBefore(de)).count();

        EstadoLinha estado = r.diasPorCorrigir() > 0 || r.diasPorValidar() > 0 || pendentes > 0
                ? EstadoLinha.COM_PENDENCIAS : EstadoLinha.COMPLETA;

        return new Linha(f, unidadeId, apuramento.isento(), foraDoVinculo, diasFerias,
                rubricas(justificadas, tipoDaChave, opcaoDaChave), rubricas(injustificadas, tipoDaChave, opcaoDaChave),
                r.diasSemRegisto(), r.faltasParciais(), r.totalFaltas(),
                licencas.entrySet().stream().map(e -> {
                    SubtipoLicencaMobilidade s = subtipoDaChave.get(e.getKey());
                    return new RubricaLicenca(s != null ? s.getCodigo() : null, s != null ? s.getNome() : null,
                            s != null ? (s.isMobilidade() ? "MOBILIDADE" : "LICENCA") : null, e.getValue()[0],
                            s != null ? s.getAffectsPay() : null, s != null ? s.getCountsForSeniority() : null);
                }).toList(),
                suplementar, r.diasPorCorrigir(), r.diasPorValidar(), pendentes, estado);
    }

    private static List<Rubrica> rubricas(Map<String, int[]> somas, Map<String, TipoAusencia> tipos, Map<String, String> opcoes) {
        return somas.entrySet().stream().map(e -> {
            TipoAusencia t = tipos.get(e.getKey());
            return new Rubrica(t.getCodigo(), t.getNome(), e.getValue()[0], e.getValue()[1],
                    t.getEfeitoRemuneracao() != null ? t.getEfeitoRemuneracao().name() : null, opcoes.get(e.getKey()));
        }).toList();
    }

    /** Os dias do pedido dentro do corte; um corte sem dias úteis (fim-de-semana, feriado) vale zero. */
    private int contar(LocalDate de, LocalDate ate, Set<LocalDate> feriados, ContagemDias contagem) {
        try {
            return diasUteisCalculator.calcular(de, ate, feriados, contagem != null ? contagem : ContagemDias.DIAS_UTEIS);
        } catch (IgrpResponseStatusException e) {
            return 0;
        }
    }

    /** A afectação que chega mais longe no mês; em empate, a que começou depois. */
    private static boolean chegaMaisLonge(Assignment a, Assignment b, LocalDate ate) {
        LocalDate fa = a.getDataFim() == null || a.getDataFim().isAfter(ate) ? ate : a.getDataFim();
        LocalDate fb = b.getDataFim() == null || b.getDataFim().isAfter(ate) ? ate : b.getDataFim();
        if (!fa.equals(fb)) return fa.isAfter(fb);
        return a.getDataInicio() != null && b.getDataInicio() != null && a.getDataInicio().isAfter(b.getDataInicio());
    }

    /** A unidade e as suas descendentes activas, pela árvore de {@code parentUnitId}, a raiz primeiro. */
    private List<OrganizationalUnit> comDescendentes(OrganizationalUnit raiz) {
        Map<UUID, List<OrganizationalUnit>> filhos = new HashMap<>();
        for (OrganizationalUnit u : unidadeRepository.findAllActive())
            if (u.getParentUnitId() != null)
                filhos.computeIfAbsent(u.getParentUnitId().getValor(), k -> new ArrayList<>()).add(u);
        List<OrganizationalUnit> todas = new ArrayList<>();
        List<OrganizationalUnit> fila = new ArrayList<>(List.of(raiz));
        Set<UUID> vistas = new java.util.HashSet<>();
        while (!fila.isEmpty()) {
            OrganizationalUnit u = fila.remove(0);
            if (!vistas.add(u.getId().getValor())) continue;
            todas.add(u);
            List<OrganizationalUnit> deU = new ArrayList<>(filhos.getOrDefault(u.getId().getValor(), List.of()));
            deU.sort(Comparator.comparing(x -> x.getName() == null ? "" : x.getName()));
            fila.addAll(deU);
        }
        return todas;
    }

    LocalDate hoje() { return LocalDate.now(); }

    private static IgrpResponseStatusException invalido(String mensagem) {
        return IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY, mensagem);
    }
}
