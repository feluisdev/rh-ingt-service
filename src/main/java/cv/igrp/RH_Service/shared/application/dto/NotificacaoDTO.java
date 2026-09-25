package cv.igrp.RH_Service.shared.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** Uma notificação, tal como o ecrã a mostra. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class NotificacaoDTO {
    private String id;
    /** O que anuncia (ex.: PEDIDO_AUSENCIA_DECIDIDO) — para o ícone e a ligação. */
    private String tipo;
    private String titulo;
    private String texto;
    /** A que se refere (ex.: PEDIDO_AUSENCIA) e o seu id — para abrir o sítio certo. */
    private String recursoTipo;
    private String recursoId;
    /** Caixa partilhada (ex.: RH); nulo quando é pessoal. */
    private String perfil;
    private LocalDateTime criadaEm;
    private LocalDateTime lidaEm;
    private boolean lida;
}
