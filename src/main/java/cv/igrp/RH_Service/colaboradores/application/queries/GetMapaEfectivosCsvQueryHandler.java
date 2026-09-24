package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.RH_Service.colaboradores.application.services.MapaEfectivosService;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;

/** O mapa de efectivos em CSV: uma linha por unidade e cargo; {@code ;}, UTF-8 com BOM. */
@Component
@RequiredArgsConstructor
public class GetMapaEfectivosCsvQueryHandler implements QueryHandler<GetMapaEfectivosCsvQuery, ResponseEntity<byte[]>> {

    static final String CABECALHO = String.join(";", "data", "unidade_codigo", "unidade", "carreira", "categoria",
            "fora_de_grelha", "lugares", "providos", "vagos", "congelados");

    private final MapaEfectivosService mapaEfectivosService;

    @IgrpQueryHandler
    public ResponseEntity<byte[]> handle(GetMapaEfectivosCsvQuery query) {
        var m = GetMapaEfectivosQueryHandler.mapa(mapaEfectivosService, query.getUnidadeId(), query.getIncluirSubunidades());
        String nome = "mapa-efectivos-" + m.data() + "-" + (m.raiz().getCode() != null ? m.raiz().getCode() : "unidade") + ".csv";
        return ResponseEntity.ok()
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nome.replaceAll("[^A-Za-z0-9._-]", "_") + "\"")
                .body(csv(m).getBytes(StandardCharsets.UTF_8));
    }

    public static String csv(MapaEfectivosService.Mapa m) {
        StringBuilder sb = new StringBuilder("\uFEFF").append(CABECALHO).append("\r\n");
        for (var u : m.unidades())
            for (var c : u.cargos()) {
                var k = c.contagem();
                sb.append(String.join(";", List.of(m.data().toString(), t(u.unidade().getCode()), t(u.unidade().getName()),
                        t(c.carreira()), t(c.categoria()), c.foraDeGrelha() ? "sim" : "nao", Integer.toString(k.lugares()),
                        Integer.toString(k.providos()), Integer.toString(k.vagos()), Integer.toString(k.congelados())))).append("\r\n");
            }
        return sb.toString();
    }

    private static String t(String v) { return GetRelacaoMensalCsvQueryHandler.t(v); }
}
