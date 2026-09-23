package cv.igrp.RH_Service.colaboradores.application.services;

import cv.igrp.RH_Service.colaboradores.domain.models.Feriado;
import cv.igrp.RH_Service.colaboradores.domain.repository.AssignmentRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.FeriadoRepository;
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
import java.util.Set;
import java.util.UUID;

/**
 * <b>O calendário de feriados que se aplica a um colaborador</b> (V55).
 *
 * <p>O calendário é da <b>instituição</b>, não da pessoa: ela cataloga os feriados nacionais e os
 * do sítio onde tem serviços, e conta-se tudo o que estiver activo. A única coisa que depende
 * do colaborador é <b>onde trabalha</b>, e só para as instituições que limitam feriados a uma
 * área:
 *
 * <ul>
 *   <li>a unidade é a de onde <b>exerce funções</b> no início do período — numa mobilidade
 *       interna, a de destino; sem mobilidade, a do seu Lugar;</li>
 *   <li>a área é a da unidade ou, se ela não a tiver, a da unidade-mãe mais próxima que tenha;</li>
 *   <li>numa mobilidade <b>externa</b>, ou sem Lugar, ou sem área em lado nenhum, a área é
 *       desconhecida e só contam os feriados sem área.</li>
 * </ul>
 *
 * <p><b>A direcção segura quando falta configuração:</b> conta-se só o que se sabe. Nunca se
 * desconta um dia que ninguém declarou feriado para aquele sítio.
 */
@Service
@RequiredArgsConstructor
public class CalendarioFeriadosService {

    /** Uma árvore orgânica mais funda do que isto é um ciclo nos dados, não uma organização. */
    private static final int PROFUNDIDADE_MAXIMA = 50;

    private final FeriadoRepository feriadoRepository;
    private final MobilidadeService mobilidadeService;
    private final AssignmentRepository assignmentRepository;
    private final PositionRepository positionRepository;
    private final OrganizationalUnitRepository unidadeRepository;

    @Transactional(readOnly = true)
    public Set<LocalDate> feriadosDoColaborador(FuncionarioId funcionarioId, LocalDate inicio, LocalDate fim) {
        String area = areaDaUnidade(unidadeOndeExerceFuncoes(funcionarioId, inicio));
        Set<LocalDate> datas = new HashSet<>();
        for (Feriado feriado : feriadoRepository.findAplicaveis(inicio, fim, area))
            feriado.ocorrenciasEntre(inicio, fim).forEach(datas::add);
        return datas;
    }

    private UUID unidadeOndeExerceFuncoes(FuncionarioId funcionarioId, LocalDate data) {
        var mobilidade = mobilidadeService.mobilidadeEmVigor(funcionarioId, data);
        if (mobilidade.isPresent())
            // Externa: trabalha noutra entidade, cujo calendário não é nosso.
            return mobilidade.get().isDestinoInterno() ? mobilidade.get().getDestinationUnitId() : null;

        return assignmentRepository.findCurrentPrincipalByFuncionario(funcionarioId)
                .flatMap(a -> positionRepository.findById(PositionId.from(a.getPositionId())))
                .map(Position::getUnidadeOrganicaId)
                .orElse(null);
    }

    private String areaDaUnidade(UUID unidadeId) {
        UUID atual = unidadeId;
        Set<UUID> vistas = new HashSet<>();
        while (atual != null && vistas.add(atual) && vistas.size() <= PROFUNDIDADE_MAXIMA) {
            OrganizationalUnit unidade = unidadeRepository.findById(OrganizationalUnitId.from(atual)).orElse(null);
            if (unidade == null) return null;
            if (unidade.getAreaCkey() != null) return unidade.getAreaCkey();
            atual = unidade.getParentUnitId() != null ? unidade.getParentUnitId().getValor() : null;
        }
        return null;
    }
}
