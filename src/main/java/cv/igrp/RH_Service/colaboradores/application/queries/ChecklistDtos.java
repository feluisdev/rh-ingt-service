package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.RH_Service.colaboradores.application.dto.ChecklistDTO;
import cv.igrp.RH_Service.colaboradores.application.dto.ItemChecklistDTO;
import cv.igrp.RH_Service.colaboradores.application.dto.ItemChecklistModeloDTO;
import cv.igrp.RH_Service.colaboradores.domain.models.Checklist;
import cv.igrp.RH_Service.colaboradores.domain.models.ItemChecklistModelo;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.util.ArrayList;

/** Entre as checklists do domínio e os DTOs. */
public final class ChecklistDtos {

    private ChecklistDtos() {}

    public static ChecklistDTO dto(Checklist c, String nome, LocalDate hoje) {
        return new ChecklistDTO(c.getId().getStringValor(), c.getFuncionarioId().getStringValor(), nome, c.getTipo().name(),
                c.getDataReferencia(), c.getEstado().name(), c.getAbertaEm(), c.getConcluidaEm(), c.getMotivoCancelamento(),
                (int) c.pendentes(), (int) c.atrasados(hoje),
                new ArrayList<>(c.getItens().stream().map(i -> new ItemChecklistDTO(i.getId().getStringValor(), i.getCodigo(),
                        i.getDescricao(), i.getResponsavel().name(), i.isObrigatorio(), i.getPrazo(), i.getEstado().name(), i.getData(),
                        i.getObservacao(), i.isAutomatico(), i.atrasado(hoje))).toList()));
    }

    public static ItemChecklistModeloDTO dto(ItemChecklistModelo m) {
        return new ItemChecklistModeloDTO(m.getId().getStringValor(), m.getTipo().name(), m.getCodigo(), m.getDescricao(),
                m.getResponsavel().name(), m.isObrigatorio(), m.getPrazoDias(), m.getOrdem(), m.isActivo());
    }

    public static <E extends Enum<E>> E valor(Class<E> tipo, String v, String oQue) {
        if (v == null || v.isBlank()) return null;
        try {
            return Enum.valueOf(tipo, v.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY, oQue + " desconhecido: " + v + ".");
        }
    }
}
