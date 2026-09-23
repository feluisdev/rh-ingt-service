package cv.igrp.RH_Service.colaboradores.domain.repository;

import cv.igrp.RH_Service.colaboradores.domain.models.MarcacaoAssiduidade;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.MarcacaoAssiduidadeId;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MarcacaoAssiduidadeRepository {
    MarcacaoAssiduidade save(MarcacaoAssiduidade marcacao);
    Optional<MarcacaoAssiduidade> findById(MarcacaoAssiduidadeId id);
    /** Todas as de [{@code de}, {@code ate}], anuladas incluídas, por ordem de momento. */
    List<MarcacaoAssiduidade> findByFuncionarioEntre(FuncionarioId funcionarioId, LocalDate de, LocalDate ate);
    /** Há alguma marcação válida (não anulada) nesse dia. */
    boolean existeValidaNoDia(FuncionarioId funcionarioId, LocalDate data);
    boolean existsByReferenciaExterna(String referenciaExterna);
    /** Os pedidos de correcção por decidir destes colaboradores. */
    List<MarcacaoAssiduidade> findPendentesDe(java.util.Collection<FuncionarioId> funcionarios);
}
