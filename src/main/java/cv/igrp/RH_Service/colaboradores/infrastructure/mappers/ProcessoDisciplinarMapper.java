package cv.igrp.RH_Service.colaboradores.infrastructure.mappers;

import cv.igrp.RH_Service.colaboradores.application.dto.ProcessoDisciplinarDTO;
import cv.igrp.RH_Service.colaboradores.domain.models.ActoDisciplinar;
import cv.igrp.RH_Service.colaboradores.domain.models.EspecieProcessoDisciplinar;
import cv.igrp.RH_Service.colaboradores.domain.models.FaseProcessoDisciplinar;
import cv.igrp.RH_Service.colaboradores.domain.models.PenaDisciplinar;
import cv.igrp.RH_Service.colaboradores.domain.models.ProcessoDisciplinar;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.ProcessoDisciplinarId;
import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.FuncionarioEntity;
import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.ProcessoDisciplinarActoEntity;
import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.ProcessoDisciplinarEntity;
import cv.igrp.RH_Service.shared.infrastructure.persistence.JpaReferences;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component("colabsProcessoDisciplinarMapper")
@RequiredArgsConstructor
public class ProcessoDisciplinarMapper {

    private final JpaReferences refs;

    public ProcessoDisciplinar toDomain(ProcessoDisciplinarEntity e) {
        var actos = e.getActos().stream().map(a -> new ActoDisciplinar(a.getId(), ActoDisciplinar.Tipo.valueOf(a.getTipo()), a.getData(),
                a.getDataFim(), a.getDias(), a.getPena() != null ? PenaDisciplinar.valueOf(a.getPena()) : null, a.getDuracao(), a.getTexto())).toList();
        return ProcessoDisciplinar.reconstituir(
                ProcessoDisciplinarId.from(e.getId()),
                FuncionarioId.from(refs.idOf(e.getFuncionario(), FuncionarioEntity::getId)),
                e.getProcessNumber(), e.getStartDate(), e.getEndDate(),
                e.getPenalty(), e.getPenaltyStartDate(), e.getPenaltyEndDate(),
                e.getOfficialBulletin(), e.getNotes(),
                e.getEspecie() != null ? EspecieProcessoDisciplinar.valueOf(e.getEspecie()) : null,
                e.getFase() != null ? FaseProcessoDisciplinar.valueOf(e.getFase()) : null,
                e.getDataInfraccao(), e.getPenaPrevista() != null ? PenaDisciplinar.valueOf(e.getPenaPrevista()) : null,
                e.getInstrutorId() != null ? FuncionarioId.from(e.getInstrutorId()) : null, e.getInstrutorNome(),
                e.getPena() != null ? PenaDisciplinar.valueOf(e.getPena()) : null, e.getPenaDuracao(), e.getEfeitosAplicadosEm(), actos);
    }

    /** Preenche a entidade (nova ou a gravada) com o agregado; os actos actualizam-se no lugar pelo id. */
    public ProcessoDisciplinarEntity toEntity(ProcessoDisciplinar p, ProcessoDisciplinarEntity e) {
        if (e == null) {
            e = new ProcessoDisciplinarEntity();
            e.setId(p.getId().getValor());
            e.setFuncionario(refs.ref(FuncionarioEntity.class, p.getFuncionarioId().getValor()));
        }
        e.setProcessNumber(p.getProcessNumber());
        e.setStartDate(p.getStartDate());
        e.setEndDate(p.getEndDate());
        e.setPenalty(p.getPenalty());
        e.setPenaltyStartDate(p.getPenaltyStartDate());
        e.setPenaltyEndDate(p.getPenaltyEndDate());
        e.setOfficialBulletin(p.getOfficialBulletin());
        e.setNotes(p.getNotes());
        e.setEspecie(p.getEspecie() != null ? p.getEspecie().name() : null);
        e.setFase(p.getFase() != null ? p.getFase().name() : null);
        e.setDataInfraccao(p.getDataInfraccao());
        e.setPenaPrevista(p.getPenaPrevista() != null ? p.getPenaPrevista().name() : null);
        e.setInstrutorId(p.getInstrutorId() != null ? p.getInstrutorId().getValor() : null);
        e.setInstrutorNome(p.getInstrutorNome());
        e.setPena(p.getPena() != null ? p.getPena().name() : null);
        e.setPenaDuracao(p.getPenaDuracao());
        e.setEfeitosAplicadosEm(p.getEfeitosAplicadosEm());
        Map<UUID, ProcessoDisciplinarActoEntity> existentes = e.getActos().stream()
                .collect(Collectors.toMap(ProcessoDisciplinarActoEntity::getId, Function.identity()));
        for (var a : p.getActos()) {
            var x = existentes.get(a.id());
            if (x == null) {
                x = new ProcessoDisciplinarActoEntity();
                x.setId(a.id());
                x.setProcesso(e);
                e.getActos().add(x);
            }
            x.setTipo(a.tipo().name());
            x.setData(a.data());
            x.setDataFim(a.dataFim());
            x.setDias(a.dias());
            x.setPena(a.pena() != null ? a.pena().name() : null);
            x.setDuracao(a.duracao());
            x.setTexto(a.texto());
        }
        return e;
    }

    public ProcessoDisciplinarDTO toDTO(ProcessoDisciplinar p) {
        ProcessoDisciplinarDTO dto = new ProcessoDisciplinarDTO();
        dto.setId(p.getId().getStringValor());
        dto.setFuncionarioId(p.getFuncionarioId().getStringValor());
        dto.setProcessNumber(p.getProcessNumber());
        dto.setStartDate(p.getStartDate());
        dto.setEndDate(p.getEndDate());
        dto.setPenalty(p.getPenalty());
        dto.setPenaltyStartDate(p.getPenaltyStartDate());
        dto.setPenaltyEndDate(p.getPenaltyEndDate());
        dto.setOfficialBulletin(p.getOfficialBulletin());
        dto.setNotes(p.getNotes());
        return dto;
    }
}
