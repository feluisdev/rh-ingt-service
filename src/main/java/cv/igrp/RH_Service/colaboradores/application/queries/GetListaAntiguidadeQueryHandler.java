package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.RH_Service.colaboradores.application.dto.GrupoAntiguidadeDTO;
import cv.igrp.RH_Service.colaboradores.application.dto.LinhaAntiguidadeDTO;
import cv.igrp.RH_Service.colaboradores.application.dto.ListaAntiguidadeDTO;
import cv.igrp.RH_Service.colaboradores.application.services.ListaAntiguidadeService;
import cv.igrp.RH_Service.colaboradores.domain.service.CalculadoraAntiguidade;
import cv.igrp.RH_Service.estrutura.domain.models.OrganizationalUnit;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class GetListaAntiguidadeQueryHandler implements QueryHandler<GetListaAntiguidadeQuery, ResponseEntity<ListaAntiguidadeDTO>> {

    private final ListaAntiguidadeService listaAntiguidadeService;

    @IgrpQueryHandler
    public ResponseEntity<ListaAntiguidadeDTO> handle(GetListaAntiguidadeQuery query) {
        return ResponseEntity.ok(dto(lista(listaAntiguidadeService, query.getAno(), query.getUnidadeId(), query.getIncluirSubunidades())));
    }

    static ListaAntiguidadeService.Lista lista(ListaAntiguidadeService service, Integer ano, String unidadeId, Boolean subunidades) {
        UUID unidade = null;
        if (unidadeId != null && !unidadeId.isBlank()) {
            try {
                unidade = UUID.fromString(unidadeId.trim());
            } catch (IllegalArgumentException e) {
                throw IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY, "unidadeId inválido: " + unidadeId + ".");
            }
        }
        return service.lista(ano, unidade, subunidades == null || subunidades);
    }

    static ListaAntiguidadeDTO dto(ListaAntiguidadeService.Lista l) {
        List<GrupoAntiguidadeDTO> grupos = new ArrayList<>();
        for (var g : l.grupos()) {
            List<LinhaAntiguidadeDTO> linhas = new ArrayList<>();
            int posicao = 1;
            for (var x : g.linhas()) linhas.add(linha(posicao++, x, l.unidades().get(x.unidadeId())));
            grupos.add(new GrupoAntiguidadeDTO(g.carreira(), g.categoria(), g.foraDeGrelha(), linhas));
        }
        return new ListaAntiguidadeDTO(l.ano(), l.referencia(), l.raiz().getId().getStringValor(), l.raiz().getName(),
                l.incluirSubunidades(), grupos);
    }

    static LinhaAntiguidadeDTO linha(int posicao, ListaAntiguidadeService.Linha x, OrganizationalUnit u) {
        var f = x.funcionario();
        var c = x.noCargo();
        var t = x.total();
        return new LinhaAntiguidadeDTO(posicao, f.getId().getStringValor(), f.getNumeroFuncionario(), f.getNomeCompleto(),
                u != null ? u.getCode() : null, u != null ? u.getName() : null, x.escalao(), x.inicioNoCargo(),
                c.diasDescontados(), c.diasContados(), c.anos(), c.meses(), c.dias(), f.getDataAdmissao(),
                t != null ? t.anos() : null, t != null ? t.meses() : null, t != null ? t.dias() : null, observacoes(c));
    }

    static String observacoes(CalculadoraAntiguidade.Antiguidade c) {
        if (c.periodosDescontados().isEmpty()) return null;
        return c.periodosDescontados().stream()
                .map(p -> p.inicio() + " a " + p.fim() + " (" + p.dias() + " dias): " + p.motivo())
                .collect(Collectors.joining("; "));
    }
}
