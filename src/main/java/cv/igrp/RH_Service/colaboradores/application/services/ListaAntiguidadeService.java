package cv.igrp.RH_Service.colaboradores.application.services;

import cv.igrp.RH_Service.carreiras.domain.models.Career;
import cv.igrp.RH_Service.carreiras.domain.models.Category;
import cv.igrp.RH_Service.carreiras.domain.models.Grade;
import cv.igrp.RH_Service.carreiras.domain.repository.CareerRepository;
import cv.igrp.RH_Service.carreiras.domain.repository.CategoryRepository;
import cv.igrp.RH_Service.carreiras.domain.repository.GradeRepository;
import cv.igrp.RH_Service.carreiras.domain.valueobject.CategoryId;
import cv.igrp.RH_Service.carreiras.domain.valueobject.GradeId;
import cv.igrp.RH_Service.colaboradores.domain.models.Assignment;
import cv.igrp.RH_Service.colaboradores.domain.models.Funcionario;
import cv.igrp.RH_Service.colaboradores.domain.repository.AssignmentRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.colaboradores.domain.service.CalculadoraAntiguidade;
import cv.igrp.RH_Service.estrutura.domain.models.Job;
import cv.igrp.RH_Service.estrutura.domain.models.OrganizationalUnit;
import cv.igrp.RH_Service.estrutura.domain.models.Position;
import cv.igrp.RH_Service.estrutura.domain.repository.JobRepository;
import cv.igrp.RH_Service.estrutura.domain.repository.PositionRepository;
import cv.igrp.RH_Service.estrutura.domain.valueobject.JobId;
import cv.igrp.RH_Service.estrutura.domain.valueobject.PositionId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * <b>Lista de antiguidade anual</b> — DL n.º 3/2010, arts. 69.º e 70.º: cada serviço organiza em cada
 * ano a lista dos seus funcionários com referência a 31 de Dezembro do ano anterior, <b>por cargos</b>
 * e, dentro deles, <b>pela antiguidade</b>, com a data de início no cargo, os dias descontados e o
 * tempo contado em anos, meses e dias (ano de 365 e mês de 30 dias), independentemente do serviço onde
 * as funções foram exercidas.
 *
 * <p>O «cargo» é a carreira e a categoria em 31 de Dezembro (fora da grelha, o cargo — Job — do Lugar).
 * A categoria de cada data sai do escalão da afectação: cada promoção e cada mudança de carreira abrem
 * uma afectação nova, mesmo quando é o próprio Lugar que é reclassificado. A data de início no cargo é
 * o começo da cadeia de afectações seguidas nessa categoria (progressões e transferências não a
 * cortam). Os descontos são os da antiguidade ({@link AntiguidadeService}), recortados a esse período.
 *
 * <p>Só se gera: a aprovação, a afixação, as reclamações e a publicação (arts. 71.º a 74.º) ficam para
 * depois.
 */
@Service
@RequiredArgsConstructor
public class ListaAntiguidadeService {

    public record Linha(Funcionario funcionario, UUID unidadeId, String escalao, LocalDate inicioNoCargo,
                        CalculadoraAntiguidade.Antiguidade noCargo, CalculadoraAntiguidade.Antiguidade total) {}

    public record Grupo(String carreira, String categoria, boolean foraDeGrelha, List<Linha> linhas) {}

    public record Lista(int ano, LocalDate referencia, OrganizationalUnit raiz, boolean incluirSubunidades,
                        Map<UUID, OrganizationalUnit> unidades, List<Grupo> grupos) {}

    private final QuemEstaNoServico quemEstaNoServico;
    private final FuncionarioRepository funcionarioRepository;
    private final AssignmentRepository assignmentRepository;
    private final PositionRepository positionRepository;
    private final JobRepository jobRepository;
    private final GradeRepository gradeRepository;
    private final CategoryRepository categoryRepository;
    private final CareerRepository careerRepository;
    private final AntiguidadeService antiguidadeService;

    /** O que define o cargo numa afectação: a categoria (pelo escalão) ou, fora da grelha, o Job do Lugar. */
    private record Cargo(UUID categoryId, UUID jobId) {}

    @Transactional(readOnly = true)
    public Lista lista(Integer ano, UUID unidadeId, boolean incluirSubunidades) {
        if (unidadeId == null) throw invalido("A unidade orgânica é obrigatória: a lista é de cada serviço (art. 69.º n.º 1).");
        int anoLista = ano != null ? ano : hoje().getYear();
        if (anoLista > hoje().getYear())
            throw invalido("A lista de " + anoLista + " refere-se a 31 de Dezembro de " + (anoLista - 1) + ", que ainda não chegou.");
        LocalDate referencia = LocalDate.of(anoLista - 1, 12, 31);

        List<OrganizationalUnit> unidades = quemEstaNoServico.unidades(unidadeId, incluirSubunidades);
        Map<UUID, OrganizationalUnit> porId = new LinkedHashMap<>();
        unidades.forEach(u -> porId.put(u.getId().getValor(), u));
        var colocacoes = quemEstaNoServico.colocacoes(unidades, referencia, referencia);

        Map<UUID, Funcionario> pessoas = new HashMap<>();
        for (Funcionario f : funcionarioRepository.findAllByIds(colocacoes.keySet())) pessoas.put(f.getId().getValor(), f);

        Map<UUID, Optional<Grade>> escaloes = new HashMap<>();
        Map<UUID, Optional<Position>> lugares = new HashMap<>();
        Map<Cargo, List<Linha>> porCargo = new LinkedHashMap<>();
        for (var e : colocacoes.entrySet()) {
            Funcionario f = pessoas.get(e.getKey());
            if (f == null) continue;
            Assignment actual = e.getValue().afectacao();
            Cargo cargo = cargo(actual, escaloes, lugares);
            LocalDate inicio = inicioNoCargo(f, actual, cargo, referencia, escaloes, lugares);
            var noCargo = antiguidadeService.calcularDesde(f.getId(), inicio, referencia);
            CalculadoraAntiguidade.Antiguidade total = null;
            try {
                total = antiguidadeService.calcular(f.getId(), referencia);
            } catch (IgrpResponseStatusException semAdmissao) {
                // Sem data de admissão não há tempo total; a linha fica, com o tempo no cargo.
            }
            String escalao = actual.getGradeId() != null
                    ? escaloes.computeIfAbsent(actual.getGradeId(), this::escalao).map(Grade::getName).orElse(null) : null;
            porCargo.computeIfAbsent(cargo, k -> new ArrayList<>())
                    .add(new Linha(f, e.getValue().unidadeId(), escalao, inicio, noCargo, total));
        }

        List<Grupo> grupos = new ArrayList<>();
        List<Object[]> ordenacao = new ArrayList<>();
        for (var e : porCargo.entrySet()) {
            List<Linha> linhas = new ArrayList<>(e.getValue());
            linhas.sort(Comparator.comparingLong((Linha l) -> l.noCargo().diasContados()).reversed()
                    .thenComparing(Comparator.comparingLong((Linha l) -> l.total() != null ? l.total().diasContados() : 0).reversed())
                    .thenComparing(l -> l.funcionario().getNomeCompleto() == null ? "" : l.funcionario().getNomeCompleto()));
            Cargo c = e.getKey();
            if (c.categoryId() != null) {
                Category categoria = categoryRepository.findById(CategoryId.from(c.categoryId())).orElse(null);
                Career carreira = categoria != null && categoria.getCareerId() != null
                        ? careerRepository.findById(categoria.getCareerId()).orElse(null) : null;
                Grupo g = new Grupo(carreira != null ? carreira.getName() : null, categoria != null ? categoria.getName() : null,
                        false, List.copyOf(linhas));
                ordenacao.add(new Object[]{0, g.carreira() == null ? "" : g.carreira(),
                        categoria != null && categoria.getOrdemProgressao() != null ? -categoria.getOrdemProgressao() : Integer.MAX_VALUE, g});
            } else {
                Job job = c.jobId() != null ? jobRepository.findById(JobId.from(c.jobId())).orElse(null) : null;
                Grupo g = new Grupo("Fora de grelha", job != null ? job.getName() : null, true, List.copyOf(linhas));
                ordenacao.add(new Object[]{1, g.categoria() == null ? "" : g.categoria(), 0, g});
            }
        }
        ordenacao.sort(Comparator.comparing((Object[] o) -> (Integer) o[0])
                .thenComparing(o -> (String) o[1]).thenComparing(o -> (Integer) o[2]));
        ordenacao.forEach(o -> grupos.add((Grupo) o[3]));
        return new Lista(anoLista, referencia, unidades.get(0), incluirSubunidades, porId, grupos);
    }

    /**
     * O início da cadeia de afectações principais seguidas no mesmo cargo, a partir da que vale na
     * referência. Uma interrupção (um dia sem afectação) corta a cadeia.
     */
    private LocalDate inicioNoCargo(Funcionario f, Assignment actual, Cargo cargo, LocalDate referencia,
                                    Map<UUID, Optional<Grade>> escaloes, Map<UUID, Optional<Position>> lugares) {
        List<Assignment> historico = assignmentRepository.findAllByFuncionarioOrderByDataInicioDesc(f.getId()).stream()
                .filter(Assignment::isPrincipal)
                .filter(a -> a.getDataInicio() != null && !a.getDataInicio().isAfter(referencia))
                .sorted(Comparator.comparing(Assignment::getDataInicio).reversed())
                .toList();
        LocalDate inicio = actual.getDataInicio();
        LocalDate limite = actual.getDataInicio();
        for (Assignment a : historico) {
            if (a.getId().equals(actual.getId()) || !a.getDataInicio().isBefore(limite)) continue;
            boolean seguida = a.getDataFim() == null || !a.getDataFim().plusDays(1).isBefore(limite);
            if (!seguida || !cargo.equals(cargo(a, escaloes, lugares))) break;
            inicio = a.getDataInicio();
            limite = a.getDataInicio();
        }
        return inicio;
    }

    private Cargo cargo(Assignment a, Map<UUID, Optional<Grade>> escaloes, Map<UUID, Optional<Position>> lugares) {
        if (a.getGradeId() != null) {
            UUID categoria = escaloes.computeIfAbsent(a.getGradeId(), this::escalao)
                    .map(g -> g.getCategoryId() != null ? g.getCategoryId().getValor() : null).orElse(null);
            if (categoria != null) return new Cargo(categoria, null);
        }
        Position p = lugares.computeIfAbsent(a.getPositionId(),
                id -> id != null ? positionRepository.findById(PositionId.from(id)) : Optional.empty()).orElse(null);
        return new Cargo(null, p != null ? p.getJobId() : null);
    }

    private Optional<Grade> escalao(UUID id) {
        return gradeRepository.findById(GradeId.from(id));
    }

    protected LocalDate hoje() { return LocalDate.now(); }

    private static IgrpResponseStatusException invalido(String mensagem) {
        return IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY, mensagem);
    }
}
