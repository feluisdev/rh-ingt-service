package cv.igrp.RH_Service.colaboradores.domain.repository;

import cv.igrp.RH_Service.colaboradores.domain.models.ListaAntiguidadeOficial;
import cv.igrp.RH_Service.colaboradores.domain.models.ReclamacaoAntiguidade;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.ListaAntiguidadeOficialId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.ReclamacaoAntiguidadeId;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** As listas de antiguidade oficiais e as suas reclamações. */
public interface ListaAntiguidadeOficialRepository {
    ListaAntiguidadeOficial save(ListaAntiguidadeOficial lista);
    Optional<ListaAntiguidadeOficial> findById(ListaAntiguidadeOficialId id);
    /** Filtros opcionais; das mais recentes para as mais antigas. */
    List<ListaAntiguidadeOficial> find(Integer ano, UUID unidadeId);
    /** Há uma lista não anulada deste serviço neste ano? */
    boolean existeActiva(int ano, UUID unidadeId);
    /** As afixadas, definitivas ou publicadas onde a pessoa aparece. */
    List<ListaAntiguidadeOficial> findVisiveisPara(FuncionarioId funcionarioId);

    ReclamacaoAntiguidade save(ReclamacaoAntiguidade reclamacao);
    Optional<ReclamacaoAntiguidade> findReclamacao(ReclamacaoAntiguidadeId id);
    List<ReclamacaoAntiguidade> findReclamacoes(ListaAntiguidadeOficialId listaId);
}
