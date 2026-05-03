package cv.igrp.RH_Service.colaboradores.application.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RejeitarPedidoRequestDTO {
    @NotBlank
    private String aprovadoPorId;
    private String observacoesDecisao;
}
