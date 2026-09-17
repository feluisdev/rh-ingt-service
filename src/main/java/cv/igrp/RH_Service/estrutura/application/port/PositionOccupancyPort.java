package cv.igrp.RH_Service.estrutura.application.port;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

/**
 * Quem ocupa os Lugares -- a pergunta pertence a colaboradores/ (a Afectacao e dona da
 * ocupacao), mas quem lista o Mapa de Pessoal esta em estrutura/. A porta inverte a
 * dependencia: estrutura declara o que precisa, colaboradores implementa.
 *
 * <p>Nao se usou um adapter em estrutura/infrastructure/lookup/ (como o
 * FuncionarioLookupAdapter) de proposito: esse e, por decisao documentada (D-08), o unico
 * ficheiro de estrutura autorizado a importar colaboradores, e o portao automatico
 * {@code grep -rln "colaboradores\." src/main/java/cv/igrp/RH_Service/estrutura/} tem de
 * continuar a devolver exactamente esse ficheiro. Como colaboradores -> estrutura nao tem
 * restricao, o adapter vive do lado de colaboradores e o portao mantem-se intacto.
 */
public interface PositionOccupancyPort {

    /**
     * Quais dos {@code positionIds} tem afectacao corrente. Os ids ausentes do resultado
     * estao vagos. Resolve-se numa consulta so, para nao trazer N+1 a quem lista.
     */
    Set<UUID> ocupados(Collection<UUID> positionIds);
}
