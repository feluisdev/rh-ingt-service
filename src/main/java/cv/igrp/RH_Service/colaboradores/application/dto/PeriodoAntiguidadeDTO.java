package cv.igrp.RH_Service.colaboradores.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/** Um período que não contou para a antiguidade, e porquê. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PeriodoAntiguidadeDTO {
    private LocalDate inicio;
    private LocalDate fim;
    private long dias;
    /**
     * Porque não contou. Quando dois motivos se sobrepõem — a situação funcional e a licença que
     * a causou —, o período é um só e os motivos aparecem juntos: descontar duas vezes os mesmos
     * dias seria tirar o dobro.
     */
    private String motivo;
}
