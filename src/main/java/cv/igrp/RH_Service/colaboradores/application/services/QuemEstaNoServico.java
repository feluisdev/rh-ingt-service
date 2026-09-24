package cv.igrp.RH_Service.colaboradores.application.services;

import cv.igrp.RH_Service.colaboradores.domain.models.Assignment;
import cv.igrp.RH_Service.colaboradores.domain.repository.AssignmentRepository;
import cv.igrp.RH_Service.estrutura.domain.models.OrganizationalUnit;
import cv.igrp.RH_Service.estrutura.domain.repository.OrganizationalUnitRepository;
import cv.igrp.RH_Service.estrutura.domain.valueobject.OrganizationalUnitId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * <b>Quem esteve num serviço num período</b> — o que os relatórios «de cada serviço» (a relação
 * mensal do art. 75.º, a lista de antiguidade do art. 69.º do DL n.º 3/2010) perguntam: a unidade e,
 * se se quiser, as subunidades; e quem teve afectação principal num Lugar delas em algum dia do
 * período, <b>pelas datas da afectação</b> (quem cessou a meio também vem). Cada pessoa fica numa
 * unidade só: a da afectação que chega mais longe no período.
 */
@Component
@RequiredArgsConstructor
public class QuemEstaNoServico {

    /** Uma pessoa no serviço: a afectação que conta e a unidade onde fica. */
    public record Colocacao(Assignment afectacao, UUID unidadeId) {}

    private final OrganizationalUnitRepository unidadeRepository;
    private final AssignmentRepository assignmentRepository;

    /** A unidade (404 se não existir) e, com {@code subunidades}, as descendentes activas; a raiz primeiro. */
    @Transactional(readOnly = true)
    public List<OrganizationalUnit> unidades(UUID raizId, boolean subunidades) {
        OrganizationalUnit raiz = unidadeRepository.findById(OrganizationalUnitId.from(raizId))
                .orElseThrow(() -> IgrpResponseStatusException.notFound("Unidade orgânica não encontrada: " + raizId));
        if (!subunidades) return List.of(raiz);
        Map<UUID, List<OrganizationalUnit>> filhos = new HashMap<>();
        for (OrganizationalUnit u : unidadeRepository.findAllActive())
            if (u.getParentUnitId() != null)
                filhos.computeIfAbsent(u.getParentUnitId().getValor(), k -> new ArrayList<>()).add(u);
        List<OrganizationalUnit> todas = new ArrayList<>();
        List<OrganizationalUnit> fila = new ArrayList<>(List.of(raiz));
        Set<UUID> vistas = new HashSet<>();
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

    /** Quem esteve nas {@code unidades} em [{@code de}, {@code ate}], por id do funcionário, pela ordem das unidades. */
    @Transactional(readOnly = true)
    public Map<UUID, Colocacao> colocacoes(List<OrganizationalUnit> unidades, LocalDate de, LocalDate ate) {
        Map<UUID, Colocacao> porPessoa = new LinkedHashMap<>();
        for (OrganizationalUnit u : unidades) {
            for (Assignment a : assignmentRepository.findAllByUnidadeOrganicaEntre(u.getId().getValor(), de, ate)) {
                if (!a.isPrincipal()) continue;
                UUID pessoa = a.getFuncionarioId().getValor();
                Colocacao actual = porPessoa.get(pessoa);
                if (actual == null || chegaMaisLonge(a, actual.afectacao(), ate))
                    porPessoa.put(pessoa, new Colocacao(a, u.getId().getValor()));
            }
        }
        return porPessoa;
    }

    /** A afectação que chega mais longe no período; em empate, a que começou depois. */
    static boolean chegaMaisLonge(Assignment a, Assignment b, LocalDate ate) {
        LocalDate fa = a.getDataFim() == null || a.getDataFim().isAfter(ate) ? ate : a.getDataFim();
        LocalDate fb = b.getDataFim() == null || b.getDataFim().isAfter(ate) ? ate : b.getDataFim();
        if (!fa.equals(fb)) return fa.isAfter(fb);
        return a.getDataInicio() != null && b.getDataInicio() != null && a.getDataInicio().isAfter(b.getDataInicio());
    }
}
