package cv.igrp.RH_Service.colaboradores.application.services;

import cv.igrp.RH_Service.colaboradores.domain.models.Assignment;
import cv.igrp.RH_Service.colaboradores.domain.repository.AssignmentRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.estrutura.domain.models.OrganizationalUnit;
import cv.igrp.RH_Service.estrutura.domain.models.Position;
import cv.igrp.RH_Service.estrutura.domain.repository.OrganizationalUnitRepository;
import cv.igrp.RH_Service.estrutura.domain.repository.PositionRepository;
import cv.igrp.RH_Service.estrutura.domain.valueobject.OrganizationalUnitId;
import cv.igrp.RH_Service.estrutura.domain.valueobject.PositionId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

/**
 * <b>A unidade onde um colaborador exerce funções</b>, e o que ela herda da unidade-mãe.
 *
 * <p>Serve o calendário de feriados (a área, V55) e o horário (V57), que perguntam o mesmo:
 * numa mobilidade interna, a unidade de destino; sem mobilidade, a do seu Lugar; numa mobilidade
 * <b>externa</b>, ou sem Lugar, nenhuma — trabalha noutra entidade, ou não se sabe onde.
 *
 * <p>O Lugar é o <b>dessa data</b>: o da afectação principal que a cobria, e não o de hoje — quem
 * mudou de unidade não vê os meses passados recalculados com a unidade nova. Sem afectação que cubra
 * a data (um intervalo entre afectações, dados antigos sem datas), vale a afectação actual, como antes.
 */
@Service
@RequiredArgsConstructor
public class UnidadeDeExercicioService {

    /** Uma árvore orgânica mais funda do que isto é um ciclo nos dados, não uma organização. */
    private static final int PROFUNDIDADE_MAXIMA = 50;

    private final MobilidadeService mobilidadeService;
    private final AssignmentRepository assignmentRepository;
    private final PositionRepository positionRepository;
    private final OrganizationalUnitRepository unidadeRepository;

    /** Nula quando não se sabe onde exerce: mobilidade externa ou sem Lugar. */
    @Transactional(readOnly = true)
    public UUID unidadeOndeExerceFuncoes(FuncionarioId funcionarioId, LocalDate data) {
        var mobilidade = mobilidadeService.mobilidadeEmVigor(funcionarioId, data);
        if (mobilidade.isPresent())
            // Externa: trabalha noutra entidade, cujo calendário e horário não são nossos.
            return mobilidade.get().isDestinoInterno() ? mobilidade.get().getDestinationUnitId() : null;

        Optional<Assignment> daData = assignmentRepository.findAllByFuncionarioOrderByDataInicioDesc(funcionarioId).stream()
                .filter(a -> a.isPrincipal() && cobre(a, data))
                .findFirst();
        return daData.or(() -> assignmentRepository.findCurrentPrincipalByFuncionario(funcionarioId))
                .flatMap(a -> positionRepository.findById(PositionId.from(a.getPositionId())))
                .map(Position::getUnidadeOrganicaId)
                .orElse(null);
    }

    private static boolean cobre(Assignment a, LocalDate data) {
        return data != null && a.getDataInicio() != null && !a.getDataInicio().isAfter(data)
                && (a.getDataFim() == null || !a.getDataFim().isBefore(data));
    }

    /**
     * O primeiro valor não nulo de {@code campo}, na unidade ou, subindo, na unidade-mãe mais
     * próxima que o tenha. Nulo se ninguém na cadeia o tiver.
     */
    @Transactional(readOnly = true)
    public <T> T herdado(UUID unidadeId, Function<OrganizationalUnit, T> campo) {
        UUID atual = unidadeId;
        Set<UUID> vistas = new HashSet<>();
        while (atual != null && vistas.add(atual) && vistas.size() <= PROFUNDIDADE_MAXIMA) {
            OrganizationalUnit unidade = unidadeRepository.findById(OrganizationalUnitId.from(atual)).orElse(null);
            if (unidade == null) return null;
            T valor = campo.apply(unidade);
            if (valor != null) return valor;
            atual = unidade.getParentUnitId() != null ? unidade.getParentUnitId().getValor() : null;
        }
        return null;
    }
}
