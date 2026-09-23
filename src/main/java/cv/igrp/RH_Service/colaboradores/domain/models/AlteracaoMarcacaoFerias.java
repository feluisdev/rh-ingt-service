package cv.igrp.RH_Service.colaboradores.domain.models;

import java.time.LocalDate;

/**
 * Registo de uma alteração a uma marcação depois de o mapa ter sido dado a conhecer (art. 6.º
 * n.º 2). Guarda o antes e o depois por extenso: é a prova de que a alteração teve o motivo que a
 * lei exige, e tem de se ler sem reconstruir versões antigas.
 */
public record AlteracaoMarcacaoFerias(
        MotivoAlteracaoMapaFerias motivo,
        String fundamentacao,
        String periodosAnteriores,
        String periodosNovos,
        LocalDate alteradaEm) {
}
