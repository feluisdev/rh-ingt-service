package cv.igrp.RH_Service.colaboradores.domain.repository;

import cv.igrp.RH_Service.colaboradores.domain.filter.LicencaMobilidadeFilter;
import cv.igrp.RH_Service.colaboradores.domain.models.LicencaMobilidade;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.LicencaMobilidadeId;

import java.util.List;
import java.util.Optional;

public interface LicencaMobilidadeRepository {

    LicencaMobilidade save(LicencaMobilidade licenca);

    Optional<LicencaMobilidade> findById(LicencaMobilidadeId id);

    List<LicencaMobilidade> findAllByFuncionarioId(FuncionarioId funcionarioId, LicencaMobilidadeFilter filter);

    /**
     * Registos em vigor (deferidos e a decorrer) do colaborador numa data — é por aqui que se sabe onde a pessoa
     * exerce funções quando está em mobilidade, já que a afectação continua no Lugar de origem.
     */
    List<LicencaMobilidade> findActiveByFuncionarioIdAt(FuncionarioId funcionarioId, java.time.LocalDate data);

    /** Deferidas que já começaram e cujos efeitos no Lugar continuam por aplicar. */
    List<LicencaMobilidade> findEntradaPorAplicar(java.time.LocalDate data);

    /** Deferidas cujo período já terminou e cujo regresso continua por aplicar. */
    List<LicencaMobilidade> findRegressoPorAplicar(java.time.LocalDate data);

    /**
     * As comissões de serviço (subtipos que regressam ou cessam, art. 64.º n.º 2) deferidas e ainda sem regresso
     * aplicado — todas, ou só as que terminam até {@code terminaAte}; pelo fim.
     */
    List<LicencaMobilidade> findComissoesEmCurso(java.time.LocalDate terminaAte);
}
