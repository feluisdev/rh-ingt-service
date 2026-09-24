package cv.igrp.RH_Service.colaboradores.application.services;

import cv.igrp.RH_Service.carreiras.domain.models.Career;
import cv.igrp.RH_Service.carreiras.domain.models.Category;
import cv.igrp.RH_Service.carreiras.domain.repository.CareerRepository;
import cv.igrp.RH_Service.carreiras.domain.repository.CategoryRepository;
import cv.igrp.RH_Service.carreiras.domain.valueobject.CareerId;
import cv.igrp.RH_Service.carreiras.domain.valueobject.CategoryId;
import cv.igrp.RH_Service.colaboradores.domain.repository.AssignmentRepository;
import cv.igrp.RH_Service.estrutura.domain.models.Job;
import cv.igrp.RH_Service.estrutura.domain.models.OrganizationalUnit;
import cv.igrp.RH_Service.estrutura.domain.models.Position;
import cv.igrp.RH_Service.estrutura.domain.repository.JobRepository;
import cv.igrp.RH_Service.estrutura.domain.repository.PositionRepository;
import cv.igrp.RH_Service.estrutura.domain.valueobject.JobId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * <b>Mapa de efectivos</b> — Lei n.º 20/X/2023, art. 4.º al. aa): o documento com as funções e o
 * número de postos de trabalho que o serviço <b>detém</b> (arts. 38.º a 41.º: face ao quadro de
 * pessoal, o que precisa). Aqui, por unidade orgânica e por cargo, os Lugares do Mapa de Pessoal:
 * activos, providos (com titular), vagos e congelados. Os extintos não entram.
 *
 * <p>É o estado <b>de hoje</b>: o provimento lê-se pelo titular corrente de cada Lugar. Só leitura.
 */
@Service
@RequiredArgsConstructor
public class MapaEfectivosService {

    public record Contagem(int lugares, int providos, int vagos, int congelados) {
        static final Contagem ZERO = new Contagem(0, 0, 0, 0);

        Contagem mais(Contagem o) {
            return new Contagem(lugares + o.lugares, providos + o.providos, vagos + o.vagos, congelados + o.congelados);
        }
    }

    public record Cargo(String carreira, String categoria, boolean foraDeGrelha, Contagem contagem) {}

    public record Unidade(OrganizationalUnit unidade, List<Cargo> cargos, Contagem totais) {}

    public record Mapa(LocalDate data, OrganizationalUnit raiz, boolean incluirSubunidades, List<Unidade> unidades, Contagem totais) {}

    private final QuemEstaNoServico quemEstaNoServico;
    private final PositionRepository positionRepository;
    private final AssignmentRepository assignmentRepository;
    private final CategoryRepository categoryRepository;
    private final CareerRepository careerRepository;
    private final JobRepository jobRepository;

    /** O cargo de um Lugar: a sua carreira/categoria ou, fora da grelha, o seu cargo (Job). */
    private record Chave(UUID careerId, UUID categoryId, UUID jobId) {}

    @Transactional(readOnly = true)
    public Mapa mapa(UUID unidadeId, boolean incluirSubunidades) {
        if (unidadeId == null) throw IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY,
                "A unidade orgânica é obrigatória: o mapa de efectivos é de cada serviço.");
        List<OrganizationalUnit> unidades = quemEstaNoServico.unidades(unidadeId, incluirSubunidades);

        List<Unidade> resultado = new ArrayList<>();
        Contagem total = Contagem.ZERO;
        for (OrganizationalUnit u : unidades) {
            List<Position> lugares = positionRepository.findByUnidade(u.getId().getValor()).stream()
                    .filter(p -> !Position.EXTINTO.equals(p.getEstado())).toList();
            if (lugares.isEmpty()) continue;
            Set<UUID> comTitular = assignmentRepository.findPositionIdsComTitular(
                    lugares.stream().map(p -> p.getId().getValor()).toList());

            Map<Chave, Contagem> porCargo = new LinkedHashMap<>();
            for (Position p : lugares) {
                boolean congelado = Position.CONGELADO.equals(p.getEstado());
                boolean activo = Position.ATIVO.equals(p.getEstado());
                boolean provido = activo && comTitular.contains(p.getId().getValor());
                Contagem c = new Contagem(activo ? 1 : 0, provido ? 1 : 0, activo && !provido ? 1 : 0, congelado ? 1 : 0);
                porCargo.merge(new Chave(p.getCareerId(), p.getCategoryId(), p.getCategoryId() == null ? p.getJobId() : null), c, Contagem::mais);
            }

            List<Object[]> ordem = new ArrayList<>();
            for (var e : porCargo.entrySet()) {
                Chave k = e.getKey();
                if (k.categoryId() != null) {
                    Category cat = categoryRepository.findById(CategoryId.from(k.categoryId())).orElse(null);
                    Career car = k.careerId() != null ? careerRepository.findById(CareerId.from(k.careerId())).orElse(null) : null;
                    Cargo cargo = new Cargo(car != null ? car.getName() : null, cat != null ? cat.getName() : null, false, e.getValue());
                    ordem.add(new Object[]{0, Objects.toString(cargo.carreira(), ""),
                            cat != null && cat.getOrdemProgressao() != null ? -cat.getOrdemProgressao() : Integer.MAX_VALUE, cargo});
                } else {
                    Job job = k.jobId() != null ? jobRepository.findById(JobId.from(k.jobId())).orElse(null) : null;
                    Cargo cargo = new Cargo("Fora de grelha", job != null ? job.getName() : null, true, e.getValue());
                    ordem.add(new Object[]{1, Objects.toString(cargo.categoria(), ""), 0, cargo});
                }
            }
            ordem.sort(Comparator.comparing((Object[] o) -> (Integer) o[0]).thenComparing(o -> (String) o[1])
                    .thenComparing(o -> (Integer) o[2]));
            List<Cargo> cargos = ordem.stream().map(o -> (Cargo) o[3]).toList();
            Contagem daUnidade = cargos.stream().map(Cargo::contagem).reduce(Contagem.ZERO, Contagem::mais);
            resultado.add(new Unidade(u, cargos, daUnidade));
            total = total.mais(daUnidade);
        }
        return new Mapa(LocalDate.now(), unidades.get(0), incluirSubunidades, resultado, total);
    }
}
