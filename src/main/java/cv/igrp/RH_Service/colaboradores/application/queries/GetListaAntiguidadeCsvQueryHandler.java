package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.RH_Service.colaboradores.application.dto.LinhaAntiguidadeDTO;
import cv.igrp.RH_Service.colaboradores.application.services.ListaAntiguidadeService;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;

/** A lista de antiguidade em CSV: uma linha por funcionário, pela ordem da lista; {@code ;}, UTF-8 com BOM. */
@Component
@RequiredArgsConstructor
public class GetListaAntiguidadeCsvQueryHandler implements QueryHandler<GetListaAntiguidadeCsvQuery, ResponseEntity<byte[]>> {

    static final String CABECALHO = String.join(";", "ano", "referencia", "carreira", "categoria", "posicao", "numero",
            "nome", "unidade", "escalao", "data_inicio_no_cargo", "dias_descontados", "anos", "meses", "dias",
            "data_admissao", "anos_servico", "meses_servico", "dias_servico", "observacoes");

    private final ListaAntiguidadeService listaAntiguidadeService;

    @IgrpQueryHandler
    public ResponseEntity<byte[]> handle(GetListaAntiguidadeCsvQuery query) {
        var l = GetListaAntiguidadeQueryHandler.lista(listaAntiguidadeService, query.getAno(), query.getUnidadeId(),
                query.getIncluirSubunidades());
        String nome = "lista-antiguidade-" + l.ano() + "-" + (l.raiz().getCode() != null ? l.raiz().getCode() : "unidade") + ".csv";
        return ResponseEntity.ok()
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nome.replaceAll("[^A-Za-z0-9._-]", "_") + "\"")
                .body(csv(l).getBytes(StandardCharsets.UTF_8));
    }

    public static String csv(ListaAntiguidadeService.Lista l) {
        StringBuilder sb = new StringBuilder("\uFEFF").append(CABECALHO).append("\r\n");
        for (var g : l.grupos()) {
            int posicao = 1;
            for (var x : g.linhas()) {
                LinhaAntiguidadeDTO d = GetListaAntiguidadeQueryHandler.linha(posicao++, x, l.unidades().get(x.unidadeId()));
                List<String> c = List.of(Integer.toString(l.ano()), l.referencia().toString(), t(g.carreira()), t(g.categoria()),
                        Integer.toString(d.getPosicao()), t(d.getNumeroFuncionario()), t(d.getNome()), t(d.getUnidadeNome()),
                        t(d.getEscalao()), s(d.getDataInicioNoCargo()), Long.toString(d.getDiasDescontados()),
                        Integer.toString(d.getAnos()), Integer.toString(d.getMeses()), Integer.toString(d.getDias()),
                        s(d.getDataAdmissao()), s(d.getAnosServico()), s(d.getMesesServico()), s(d.getDiasServico()),
                        t(d.getObservacoes()));
                sb.append(String.join(";", c)).append("\r\n");
            }
        }
        return sb.toString();
    }

    private static String s(Object v) { return v == null ? "" : v.toString(); }

    private static String t(String v) { return GetRelacaoMensalCsvQueryHandler.t(v); }
}
