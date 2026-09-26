package cv.igrp.RH_Service.colaboradores.domain.repository;

import cv.igrp.RH_Service.colaboradores.domain.models.Checklist;
import cv.igrp.RH_Service.colaboradores.domain.models.ItemChecklistModelo;
import cv.igrp.RH_Service.colaboradores.domain.models.ResponsavelChecklist;
import cv.igrp.RH_Service.colaboradores.domain.models.TipoChecklist;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.ChecklistId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.ItemChecklistModeloId;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/** As checklists de entrada e saída e o modelo de itens. */
public interface ChecklistRepository {
    Checklist save(Checklist checklist);
    Optional<Checklist> findById(ChecklistId id);
    /** Da mais recente para a mais antiga. */
    List<Checklist> findByFuncionario(FuncionarioId funcionarioId);
    /** A checklist aberta (ou concluída mas não cancelada) mais recente do tipo. */
    Optional<Checklist> findCorrente(FuncionarioId funcionarioId, TipoChecklist tipo);
    /**
     * As checklists com itens pendentes — de um responsável, se indicado, e só com itens em atraso a {@code atrasadasEm},
     * se indicado.
     */
    List<Checklist> findComPendentes(TipoChecklist tipo, ResponsavelChecklist responsavel, LocalDate atrasadasEm);
    /** As abertas com algum item pendente cujo prazo é este dia. */
    List<Checklist> findComPrazoEm(LocalDate prazo);

    ItemChecklistModelo save(ItemChecklistModelo item);
    Optional<ItemChecklistModelo> findModelo(ItemChecklistModeloId id);
    List<ItemChecklistModelo> findModelos(TipoChecklist tipo);
    boolean existeCodigo(TipoChecklist tipo, String codigo, ItemChecklistModeloId excepto);
    long contarModelos();
}
