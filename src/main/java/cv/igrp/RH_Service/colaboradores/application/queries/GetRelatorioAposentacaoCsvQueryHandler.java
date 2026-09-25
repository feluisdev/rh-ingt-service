package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.RH_Service.colaboradores.application.dto.RelatorioAposentacaoDTO;
import cv.igrp.RH_Service.colaboradores.application.services.AposentacaoService;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

import static cv.igrp.RH_Service.colaboradores.application.queries.GetRelacaoMensalCsvQueryHandler.t;

/** O relatório de aposentação em CSV (separador {@code ;}, UTF-8 com BOM). */
@Component
@RequiredArgsConstructor
public class GetRelatorioAposentacaoCsvQueryHandler
        implements QueryHandler<GetRelatorioAposentacaoCsvQuery, ResponseEntity<byte[]>> {

    static final String CABECALHO = String.join(";", "numero", "nome", "data_nascimento", "idade", "faz_65", "faz_70",
            "prorrogado_ate", "limite_efectivo", "anos_servico", "meses_servico", "dias_servico", "completa_34_anos",
            "pre_aposentacao_possivel", "processo_em_curso", "estado_processo");

    private final AposentacaoService aposentacaoService;

    @IgrpQueryHandler
    public ResponseEntity<byte[]> handle(GetRelatorioAposentacaoCsvQuery q) {
        RelatorioAposentacaoDTO r = GetRelatorioAposentacaoQueryHandler.relatorio(aposentacaoService, q.getUnidadeId(),
                q.getIncluirSubunidades(), q.getAte());
        StringBuilder sb = new StringBuilder("﻿").append(CABECALHO).append("\r\n");
        for (var l : r.getLinhas()) {
            var p = l.getProcessoEmCurso();
            sb.append(String.join(";", List.of(t(l.getNumeroFuncionario()), t(l.getNome()), d(l.getDataNascimento()),
                    Integer.toString(l.getIdade()), d(l.getFaz65()), d(l.getFaz70()), d(l.getProrrogadoAte()),
                    d(l.getLimiteEfectivo()), Integer.toString(l.getAnosServico()), Integer.toString(l.getMesesServico()),
                    Integer.toString(l.getDiasServicoResto()), d(l.getCompleta34Anos()), d(l.getPreAposentacaoPossivel()),
                    p != null ? p.getModalidade() : "", p != null ? p.getEstado() : ""))).append("\r\n");
        }
        return ResponseEntity.ok()
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"aposentacao-" + r.getAte() + ".csv\"")
                .body(sb.toString().getBytes(StandardCharsets.UTF_8));
    }

    private static String d(LocalDate x) { return x == null ? "" : x.toString(); }
}
