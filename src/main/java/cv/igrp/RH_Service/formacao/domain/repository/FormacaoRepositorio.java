package cv.igrp.RH_Service.formacao.domain.repository;

import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.formacao.domain.models.AccaoFormacao;
import cv.igrp.RH_Service.formacao.domain.models.PlanoFormacao;
import cv.igrp.RH_Service.formacao.domain.valueobject.AccaoFormacaoId;
import cv.igrp.RH_Service.formacao.domain.valueobject.PlanoFormacaoId;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/** As acções e os planos de formação. (O histórico por colaborador é o FormacaoRepository de colaboradores.) */
public interface FormacaoRepositorio {
    AccaoFormacao save(AccaoFormacao accao);
    Optional<AccaoFormacao> findById(AccaoFormacaoId id);
    /** Filtros opcionais: estado e ano do início; pelo início, dos mais recentes. */
    List<AccaoFormacao> find(AccaoFormacao.Estado estado, Integer ano);
    /** As acções em que o colaborador está (ou esteve) inscrito. */
    List<AccaoFormacao> findDoFuncionario(FuncionarioId funcionarioId);
    /** As acções em curso ou concluídas, que tocam {@code [de, ate]}, em que o colaborador foi admitido. */
    List<AccaoFormacao> findComFormandoEntre(FuncionarioId funcionarioId, LocalDate de, LocalDate ate);
    /** As acções em que o colaborador deve permanência (garantia) a {@code em} ou depois. */
    List<AccaoFormacao> findComGarantiaEm(FuncionarioId funcionarioId, LocalDate em);

    PlanoFormacao save(PlanoFormacao plano);
    Optional<PlanoFormacao> findPlano(PlanoFormacaoId id);
    List<PlanoFormacao> findPlanos(Integer ano);
}
