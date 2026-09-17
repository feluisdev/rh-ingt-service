package cv.igrp.RH_Service.colaboradores.application.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Mobilidade em vigor — onde o colaborador exerce funções hoje. Não substitui o Lugar: a
 * mobilidade transitória não ocupa lugar do quadro no destino (Lei n.º 20/X/2023, art. 135.º n.º 7).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MobilidadeEmVigorDTO {
    private String id;
    /** INTERNO (unidade nossa) ou EXTERNO (entidade de fora). */
    private String destinoTipo;
    private String destinoUnidadeId;
    private String destinoNome;
    private LocalDate dataInicio;
    private LocalDate dataFim;
    private String despachoNumero;
}
