package cv.igrp.RH_Service.colaboradores.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/** Alteração a uma marcação depois de o mapa ter sido dado a conhecer (art. 6.º n.º 2). */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FeriasAlteracaoDTO {
    private String motivo;
    private String fundamentacao;
    private String periodosAnteriores;
    private String periodosNovos;
    private LocalDate alteradaEm;
}
