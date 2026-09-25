package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.application.dto.CartaoProfissionalDTO;
import cv.igrp.RH_Service.colaboradores.application.dto.CartaoProfissionalRequestDTO;
import cv.igrp.RH_Service.colaboradores.application.queries.CartoesDtos;
import cv.igrp.RH_Service.colaboradores.application.services.CartaoProfissionalService;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.CartaoProfissionalId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.shared.domain.service.Entrada;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccaoCartaoProfissionalCommandHandler implements CommandHandler<AccaoCartaoProfissionalCommand, ResponseEntity<CartaoProfissionalDTO>> {

    private final CartaoProfissionalService service;

    @IgrpCommandHandler
    public ResponseEntity<CartaoProfissionalDTO> handle(AccaoCartaoProfissionalCommand c) {
        var f = FuncionarioId.from(Entrada.uuid(c.getFuncionarioId(), "o colaborador"));
        var id = CartaoProfissionalId.from(Entrada.uuid(c.getCartaoId(), "o cartão"));
        CartaoProfissionalRequestDTO r = c.getRequest() != null ? c.getRequest() : new CartaoProfissionalRequestDTO();
        var cartao = switch (c.getAccao()) {
            case "ENTREGAR" -> service.entregar(f, id, r.getData());
            case "DEVOLVER" -> service.devolver(f, id, r.getData());
            case "ANULAR" -> service.anular(f, id, r.getMotivo());
            default -> throw new IllegalArgumentException("Acção desconhecida: " + c.getAccao());
        };
        // Um cartão válido tem motivo nulo: procura-se a situação, e só depois o motivo.
        var situacao = service.doFuncionario(f).stream().filter(s -> s.cartao().getId().equals(cartao.getId())).findFirst();
        return ResponseEntity.ok(CartoesDtos.dto(cartao, situacao.map(CartaoProfissionalService.Situacao::motivoInvalidade).orElse(null)));
    }
}
