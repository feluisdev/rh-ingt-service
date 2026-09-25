package cv.igrp.RH_Service.colaboradores.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/** Criar um acto a publicar, registar a publicação ou cancelar. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class PublicacaoOficialRequestDTO {
    /** Criar. */
    private String tipoActo;
    /** Criar (por omissão, Boletim Oficial). */
    private String meio;
    /** Criar (opcional). */
    private String funcionarioId;
    /** Criar. */
    private String sumario;
    /** Criar (por omissão, hoje). */
    private LocalDate dataActo;
    /** Registar a publicação. */
    private String serie;
    /** Registar a publicação (obrigatório no Boletim Oficial). */
    private String numero;
    /** Registar a publicação. */
    private LocalDate dataPublicacao;
    /** Cancelar. */
    private String motivo;
}
