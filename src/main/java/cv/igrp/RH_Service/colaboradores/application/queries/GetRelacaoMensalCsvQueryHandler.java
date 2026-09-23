package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.RH_Service.colaboradores.application.services.RelacaoMensalService;
import cv.igrp.RH_Service.colaboradores.domain.models.TipoDiaSuplementar;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A relação mensal em CSV: uma linha por colaborador, separador {@code ;} e UTF-8 com BOM — é o que o
 * Excel em português abre sem assistente. Decimais com vírgula. O detalhe por tipo vai numa coluna,
 * {@code CODIGO=3d} ou {@code CODIGO=90min}, separado por vírgulas.
 */
@Component
@RequiredArgsConstructor
public class GetRelacaoMensalCsvQueryHandler implements QueryHandler<GetRelacaoMensalCsvQuery, ResponseEntity<byte[]>> {

    static final String CABECALHO = String.join(";", "mes", "provisoria", "unidade_codigo", "unidade", "numero", "nome",
            "estado", "isento", "dias_fora_do_vinculo", "dias_ferias", "faltas_justificadas_dias",
            "faltas_justificadas_minutos", "faltas_justificadas_detalhe", "faltas_injustificadas_dias",
            "faltas_injustificadas_detalhe", "dias_sem_registo", "faltas_parciais", "faltas_por_justificar",
            "licencas_dias", "licencas_detalhe", "suplementar_min_dia_util", "suplementar_min_descanso",
            "suplementar_min_feriado", "dias_por_corrigir", "dias_por_validar", "pedidos_pendentes");

    private final RelacaoMensalService relacaoMensalService;

    @IgrpQueryHandler
    public ResponseEntity<byte[]> handle(GetRelacaoMensalCsvQuery query) {
        var r = GetRelacaoMensalQueryHandler.relacao(relacaoMensalService, query.getMes(), query.getUnidadeId(),
                query.getIncluirSubunidades());
        byte[] corpo = csv(r).getBytes(StandardCharsets.UTF_8);
        String nome = "relacao-mensal-" + r.mes() + "-" + (r.raiz().getCode() != null ? r.raiz().getCode() : "unidade") + ".csv";
        return ResponseEntity.ok()
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nome.replaceAll("[^A-Za-z0-9._-]", "_") + "\"")
                .body(corpo);
    }

    public static String csv(RelacaoMensalService.Relacao r) {
        StringBuilder sb = new StringBuilder("\uFEFF").append(CABECALHO).append("\r\n");
        for (var u : r.unidades()) {
            for (var l : u.linhas()) {
                var f = l.funcionario();
                List<String> c = List.of(r.mes().toString(), r.provisoria() ? "sim" : "nao", t(u.unidade().getCode()),
                        t(u.unidade().getName()), t(f.getNumeroFuncionario()), t(f.getNomeCompleto()), l.estado().name(),
                        l.isento() ? "sim" : "nao", n(l.diasForaDoVinculo()), n(l.diasFerias()),
                        n(l.faltasJustificadas().stream().mapToInt(RelacaoMensalService.Rubrica::dias).sum()),
                        n(l.faltasJustificadas().stream().mapToInt(RelacaoMensalService.Rubrica::minutos).sum()),
                        t(detalhe(l.faltasJustificadas())),
                        n(l.faltasInjustificadas().stream().mapToInt(RelacaoMensalService.Rubrica::dias).sum()),
                        t(detalhe(l.faltasInjustificadas())),
                        n(l.diasSemRegisto()), d(l.faltasParciais()), d(l.faltasPorJustificar()),
                        n(l.licencas().stream().mapToInt(RelacaoMensalService.RubricaLicenca::dias).sum()),
                        t(l.licencas().stream().map(x -> x.codigo() + "=" + x.dias() + "d").collect(Collectors.joining(", "))),
                        n(l.suplementarPorTipo().getOrDefault(TipoDiaSuplementar.DIA_UTIL, 0)),
                        n(l.suplementarPorTipo().getOrDefault(TipoDiaSuplementar.DESCANSO, 0)),
                        n(l.suplementarPorTipo().getOrDefault(TipoDiaSuplementar.FERIADO, 0)),
                        n(l.diasPorCorrigir()), n(l.diasPorValidar()), n(l.pedidosPendentes()));
                sb.append(String.join(";", c)).append("\r\n");
            }
        }
        return sb.toString();
    }

    private static String detalhe(List<RelacaoMensalService.Rubrica> rubricas) {
        return rubricas.stream().map(x -> x.codigo() + "=" + (x.minutos() > 0 && x.dias() == 0 ? x.minutos() + "min" : x.dias() + "d")
                + (x.opcaoFaltaInjustificada() != null ? " (" + x.opcaoFaltaInjustificada() + ")" : ""))
                .collect(Collectors.joining(", "));
    }

    private static String n(int v) { return Integer.toString(v); }

    private static String d(BigDecimal v) { return v == null ? "0" : v.stripTrailingZeros().toPlainString().replace('.', ','); }

    /** Texto entre aspas quando precisa: separador, aspas ou mudança de linha. */
    public static String t(String v) {
        if (v == null) return "";
        if (v.contains(";") || v.contains("\"") || v.contains("\n") || v.contains("\r"))
            return "\"" + v.replace("\"", "\"\"") + "\"";
        return v;
    }
}
