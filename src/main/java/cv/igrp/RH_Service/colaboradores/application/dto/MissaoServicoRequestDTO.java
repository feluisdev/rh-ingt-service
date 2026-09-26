package cv.igrp.RH_Service.colaboradores.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Pedir, autorizar, recusar, registar o regresso ou cancelar (cada acção usa os seus). */
@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class MissaoServicoRequestDTO {
    /** Pedir (em /me, vazio = o próprio). */
    private List<String> participantes = new ArrayList<>();
    /** NACIONAL, ESTRANGEIRO */
    private String destinoTipo;
    private String destino;
    private String objectivo;
    private LocalDateTime partida;
    private LocalDateTime regresso;
    private String transporte;
    private Boolean alojamentoACargo;
    private Boolean adiantamento;
    /** Autorizar. */
    private String despacho;
    /** Recusar, cancelar. */
    private String motivo;
    /** Regresso (com partida/regresso reais, se mudaram). */
    private String relatorio;
}
