package cv.igrp.RH_Service.colaboradores.domain.repository;

import cv.igrp.RH_Service.colaboradores.domain.models.EnquadramentoProfissional;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.EnquadramentoId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EnquadramentoRepository {
    EnquadramentoProfissional save(EnquadramentoProfissional enquadramento);
    Optional<EnquadramentoProfissional> findById(EnquadramentoId id);
    Optional<EnquadramentoProfissional> findCurrentByFuncionarioId(FuncionarioId funcionarioId);
    List<EnquadramentoProfissional> findAllByFuncionarioIdOrderByDataInicioDesc(FuncionarioId funcionarioId);

    /**
     * Quem esteve na unidade orgânica {@code unidadeOrganicaId} durante o ano {@code year},
     * resolvido pelas datas do enquadramento (dataInicio/dataFim), não pelo {@code isCurrent}.
     *
     * <p>Decisão do operador ({@code 116-CONTEXT.md}): o {@code isCurrent} responde a "onde
     * está hoje"; esta consulta responde a "onde estava no ano do período". São perguntas
     * diferentes -- um enquadramento já encerrado (isCurrent = false) que cobriu o ano do
     * período tem de ser devolvido, e um enquadramento actual (isCurrent = true) que só
     * começou depois desse ano não. Sem esta distinção, um período de 2027 planeado em 2029
     * devolveria a unidade errada.
     *
     * <p>A porta é year-shaped de propósito: quem a chama raciocina em anos; a tradução
     * para {@link java.time.LocalDate} é detalhe de infra-estrutura.
     */
    List<EnquadramentoProfissional> findAllByUnidadeOrganicaIdCoveringYear(UUID unidadeOrganicaId, int year);
}
