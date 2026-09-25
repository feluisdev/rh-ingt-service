package cv.igrp.RH_Service.colaboradores.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/** Aprovar a lista e os passos seguintes (afixar, publicar, anular). */
@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class ListaAntiguidadeRequestDTO {
    /** Aprovar (por omissão, o ano corrente). */
    private Integer ano;
    /** Aprovar. */
    private String unidadeId;
    /** Aprovar (por omissão, sim). */
    private Boolean incluirSubunidades;
    /** Aprovar: o dirigente. */
    private String aprovadaPor;
    /** Aprovar (por omissão, hoje). */
    private LocalDate dataAprovacao;
    /** Afixar (data da afixação) e publicar (data do Boletim Oficial). */
    private LocalDate data;
    /** Afixar. */
    private String local;
    /** Publicar: série do Boletim Oficial. */
    private String serie;
    /** Publicar: número do Boletim Oficial. */
    private String numero;
    /** Anular. */
    private String motivo;
}
