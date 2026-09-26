package cv.igrp.RH_Service.colaboradores.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/** Pedir, autorizar, indeferir ou cessar. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class AcumulacaoFuncoesRequestDTO {
    /** Pedir: PUBLICA, PRIVADA. */
    private String tipo;
    /** Pedir: nas públicas remuneradas, o caso do art. 21.º n.º 2. */
    private String casoPublico;
    private Boolean remunerada;
    private String entidade;
    private String funcoes;
    private String horario;
    /** Obrigatório na docência ou investigação (até um terço do horário principal). */
    private Integer horasSemanais;
    private LocalDate inicio;
    private LocalDate fim;
    /** Nas privadas: não concorrentes, não conflituantes, fora do horário, sem risco para a isenção. */
    private Boolean declaracaoSemConflito;
    /** Autorizar. */
    private String despacho;
    /** Indeferir, cessar. */
    private String motivo;
    /** Autorizar, indeferir, cessar. */
    private LocalDate data;
}
