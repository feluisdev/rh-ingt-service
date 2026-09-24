package cv.igrp.RH_Service.colaboradores.application.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** As férias de um colaborador num ano: preferência, marcação e alterações. */
@Data
public class FeriasAnoResponseDTO {
    private String funcionarioId;
    private int ano;
    /** Dias que se podem marcar: direito do ano + recebidos do anterior - cedidos ao seguinte. */
    private Integer direito;

    private LocalDate prazoPreferencia;
    private List<PeriodoFeriasDTO> preferencia = new ArrayList<>();
    private LocalDate preferenciaIndicadaEm;
    private boolean preferenciaForaDePrazo;
    private String preferenciaObservacoes;
    /** PROPRIO ou RH. */
    private String preferenciaIndicadaPor;

    private List<PeriodoFeriasDTO> marcacao = new ArrayList<>();
    private String origem;
    private String fundamentacao;
    private LocalDate marcadaEm;
    private int totalMarcado;

    /** Quando se deu conhecimento do mapa deste ano; nulo se ainda não se deu. */
    private LocalDate mapaPublicadoEm;
    private List<FeriasAlteracaoDTO> alteracoes = new ArrayList<>();
}
