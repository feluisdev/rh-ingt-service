package cv.igrp.RH_Service.colaboradores.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Uma missão de serviço, com os dias de ajudas de custo (sem valores). */
@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class MissaoServicoDTO {
    private String id;
    private List<String> participantes = new ArrayList<>();
    private List<String> participantesNomes = new ArrayList<>();
    /** NACIONAL, ESTRANGEIRO */
    private String destinoTipo;
    private String destino;
    private String objectivo;
    private LocalDateTime partida;
    private LocalDateTime regresso;
    /** AVIAO, BARCO, VIATURA_SERVICO, VIATURA_PROPRIA, TRANSPORTE_PUBLICO, OUTRO */
    private String transporte;
    /** O alojamento é pago pela entidade (o salarial ajusta as ajudas de custo). */
    private boolean alojamentoACargo;
    /** Pede adiantamento das ajudas de custo. */
    private boolean adiantamento;
    /** PEDIDA, AUTORIZADA, RECUSADA, REALIZADA, CANCELADA */
    private String estado;
    private String pedidoPor;
    private String despacho;
    private String motivo;
    private String relatorio;
    private LocalDate dataRelatorio;
    /** Partida até às 13:00 conta o dia; depois, meio. Regresso depois das 13:00 conta o dia; até, meio. */
    private BigDecimal diasAjudasCusto;
    private List<String> alertas = new ArrayList<>();
}
