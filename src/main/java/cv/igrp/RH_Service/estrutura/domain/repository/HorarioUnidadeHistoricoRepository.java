package cv.igrp.RH_Service.estrutura.domain.repository;

import cv.igrp.RH_Service.estrutura.domain.models.VigenciaHorarioUnidade;
import cv.igrp.RH_Service.estrutura.domain.valueobject.OrganizationalUnitId;
import cv.igrp.RH_Service.parametrizacoes.domain.valueobject.HorarioId;

import java.util.List;

/** O histórico do horário de cada unidade orgânica: uma linha por mudança, com a data de efeito. */
public interface HorarioUnidadeHistoricoRepository {
    List<VigenciaHorarioUnidade> findByUnidade(OrganizationalUnitId unidadeId);
    List<VigenciaHorarioUnidade> findByHorario(HorarioId horarioId);
    /** Grava; uma vigência da mesma unidade com o mesmo {@code desde} é substituída. */
    void registar(VigenciaHorarioUnidade vigencia);
}
