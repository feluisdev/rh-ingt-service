package cv.igrp.RH_Service.colaboradores.domain.repository;

import cv.igrp.RH_Service.colaboradores.domain.models.ExameSaude;
import cv.igrp.RH_Service.colaboradores.domain.models.JuntaMedica;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.ExameSaudeId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.JuntaMedicaId;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/** Os exames de medicina do trabalho e as juntas médicas. */
public interface SaudeTrabalhoRepository {
    ExameSaude save(ExameSaude exame);
    Optional<ExameSaude> findExame(ExameSaudeId id);
    /** Do mais recente para o mais antigo. */
    List<ExameSaude> findExames(FuncionarioId funcionarioId);
    /** O último exame de cada colaborador activo cuja validade termina neste dia. */
    List<ExameSaude> findUltimosComValidadeEm(LocalDate dia);

    JuntaMedica save(JuntaMedica junta);
    Optional<JuntaMedica> findJunta(JuntaMedicaId id);
    List<JuntaMedica> findJuntas(JuntaMedica.Estado estado);
    List<JuntaMedica> findJuntas(FuncionarioId funcionarioId);
}
