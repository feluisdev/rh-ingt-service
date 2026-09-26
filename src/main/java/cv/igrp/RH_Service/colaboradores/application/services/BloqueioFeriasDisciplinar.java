package cv.igrp.RH_Service.colaboradores.application.services;

import cv.igrp.RH_Service.colaboradores.domain.models.PedidoAusencia;
import cv.igrp.RH_Service.colaboradores.domain.models.TipoAusencia;
import cv.igrp.RH_Service.colaboradores.domain.repository.PedidoAusenciaRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.TipoAusenciaRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.TipoAusenciaId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.domain.service.Datas;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * <b>Férias bloqueadas por pena disciplinar</b> (Estatuto Disciplinar, art. 17.º n.º 2 b) e n.º 3; BR-DIS-30): um pedido de
 * férias não pode cair na pena de suspensão ou de inactividade, nem no ano seguinte ao seu termo — salvo os 10 dias de quem foi
 * suspenso por 90 dias ou menos. Verifica-se ao pedir e outra vez ao aprovar (a pena pode ter sido executada entretanto).
 *
 * <p>Os dias que contam para os 10 são os dos pedidos de férias aprovados ou pendentes que tocam o ano bloqueado, cada um por
 * inteiro: um pedido que atravesse o fim do ano bloqueado divide-se em dois.
 */
@Component
@RequiredArgsConstructor
public class BloqueioFeriasDisciplinar {

    private final ImpedimentosDisciplinares impedimentos;
    private final PedidoAusenciaRepository pedidoRepository;
    private final TipoAusenciaRepository tipoAusenciaRepository;

    /** 422 com a razão, se o pedido (de férias) não pode ser gozado; nada nos outros tipos. */
    @Transactional(readOnly = true)
    public void verificar(PedidoAusencia pedido) {
        motivo(pedido).ifPresent(m -> {
            throw IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY, m);
        });
    }

    @Transactional(readOnly = true)
    public Optional<String> motivo(PedidoAusencia pedido) {
        var tipos = new HashMap<TipoAusenciaId, Boolean>();
        if (!ferias(pedido, tipos)) return Optional.empty();
        var fid = pedido.getFuncionarioId();
        var inicio = pedido.getDataInicio();
        var fim = pedido.getDataFim();
        for (var j : impedimentos.janelasSemFerias(fid)) {
            if (j.tocaAPena(inicio, fim))
                return Optional.of("Entre " + Datas.pt(j.penaDe()) + " e " + Datas.pt(j.penaAte()) + " está a cumprir pena de "
                        + j.nomePena() + ": não pode marcar férias nesse período.");
            if (!j.tocaAJanela(inicio, fim)) continue;
            String periodo = " entre " + Datas.pt(j.de()) + " e " + Datas.pt(j.ate());
            String lei = " (art. 17.º do Estatuto Disciplinar).";
            if (j.diasPermitidos() == 0)
                return Optional.of("Por causa da pena de " + j.nomePena() + ", não pode gozar férias" + periodo
                        + ". Marque-as a partir de " + Datas.pt(j.ate().plusDays(1)) + lei);
            int jaMarcados = outrosPedidosDeFerias(pedido, tipos)
                    .filter(p -> j.tocaAJanela(p.getDataInicio(), p.getDataFim()))
                    .mapToInt(PedidoAusencia::getNumeroDias).sum();
            if (jaMarcados + pedido.getNumeroDias() > j.diasPermitidos())
                return Optional.of("Por causa da pena de suspensão, só pode gozar " + j.diasPermitidos() + " dias de férias"
                        + periodo + ". Já tem " + jaMarcados + " marcados e este pedido tem " + pedido.getNumeroDias()
                        + ". Reduza o pedido ou marque o resto a partir de " + Datas.pt(j.ate().plusDays(1)) + lei);
        }
        return Optional.empty();
    }

    private Stream<PedidoAusencia> outrosPedidosDeFerias(PedidoAusencia pedido, Map<TipoAusenciaId, Boolean> tipos) {
        var fid = pedido.getFuncionarioId();
        var aprovados = pedidoRepository.findAprovadosEntre(fid, pedido.getDataInicio().minusYears(2), pedido.getDataFim().plusYears(2));
        var pendentes = pedidoRepository.findPendentesDe(List.of(fid));
        return Stream.concat(aprovados.stream(), pendentes.stream())
                .filter(p -> !p.getId().equals(pedido.getId()))
                .filter(p -> !p.isEmHoras() && ferias(p, tipos));
    }

    private boolean ferias(PedidoAusencia p, Map<TipoAusenciaId, Boolean> tipos) {
        return tipos.computeIfAbsent(p.getTipoAusenciaId(),
                id -> tipoAusenciaRepository.findById(id).map(TipoAusencia::isFerias).orElse(false));
    }
}
