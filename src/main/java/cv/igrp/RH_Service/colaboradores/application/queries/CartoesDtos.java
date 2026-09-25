package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.RH_Service.colaboradores.application.dto.CartaoProfissionalDTO;
import cv.igrp.RH_Service.colaboradores.application.services.CartaoProfissionalService;
import cv.igrp.RH_Service.colaboradores.domain.models.CartaoProfissional;

/** Dos cartões profissionais para os DTOs. */
public final class CartoesDtos {

    private CartoesDtos() {}

    public static CartaoProfissionalDTO dto(CartaoProfissional c, String motivoInvalidade) {
        return new CartaoProfissionalDTO(c.getId().getStringValor(), c.getFuncionarioId().getStringValor(),
                c.getDocumentoId().getStringValor(), c.getNumero(), c.getEmitidoEm(), c.getCategoria(), c.getFuncao(), c.getCargo(),
                c.getEstado().name(), c.getDataEntrega(), c.getDataDevolucao(), c.getMotivoAnulacao(), motivoInvalidade == null,
                motivoInvalidade);
    }

    public static CartaoProfissionalDTO dto(CartaoProfissionalService.Situacao s) {
        return dto(s.cartao(), s.motivoInvalidade());
    }
}
