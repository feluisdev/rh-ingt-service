package cv.igrp.RH_Service.shared.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/** Uma página de notificações, com o total por ler da caixa. */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@IgrpDTO
public class WrapperListaNotificacoesDTO extends PageDTO {

    private long naoLidas;
    private List<NotificacaoDTO> content = new ArrayList<>();
}
