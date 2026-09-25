package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.RH_Service.colaboradores.application.dto.FactoRhDTO;
import cv.igrp.RH_Service.colaboradores.domain.repository.FactoRhRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

import static cv.igrp.RH_Service.colaboradores.application.queries.GetRelacaoMensalCsvQueryHandler.t;

/**
 * Os factos de um mês de processamento em CSV: separador {@code ;}, UTF-8 com BOM (como a relação
 * mensal). Os dados de cada facto vão numa coluna, {@code chave=valor} separados por vírgulas.
 */
@Component
@RequiredArgsConstructor
public class GetFactosSalariaisCsvQueryHandler implements QueryHandler<GetFactosSalariaisCsvQuery, ResponseEntity<byte[]>> {

    static final String CABECALHO = String.join(";", "versao", "mes_competencia", "numero", "nif", "nome", "tipo",
            "data_efeito", "ajuste_mes_anterior", "descricao", "referencia_tipo", "referencia_id", "dados", "registado_em");

    private final FactoRhRepository factoRepository;
    private final FuncionarioRepository funcionarioRepository;

    @IgrpQueryHandler
    public ResponseEntity<byte[]> handle(GetFactosSalariaisCsvQuery query) {
        YearMonth mes = GetFactosSalariaisQueryHandler.mes(query.getMes());
        var dtos = GetFactosSalariaisQueryHandler.dtos(factoRepository.findByMesCompetencia(mes), funcionarioRepository);
        return ResponseEntity.ok()
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"factos-salariais-" + mes + ".csv\"")
                .body(csv(dtos).getBytes(StandardCharsets.UTF_8));
    }

    public static String csv(List<FactoRhDTO> factos) {
        StringBuilder sb = new StringBuilder("﻿").append(CABECALHO).append("\r\n");
        for (FactoRhDTO f : factos) {
            String dados = f.getDados() == null ? "" : f.getDados().entrySet().stream()
                    .map(e -> e.getKey() + "=" + e.getValue()).collect(Collectors.joining(", "));
            sb.append(String.join(";", List.of(
                    Integer.toString(GetFactosSalariaisQueryHandler.VERSAO_CONTRATO), f.getMesCompetencia(),
                    t(f.getNumeroFuncionario()), t(f.getNif()), t(f.getNome()), f.getTipo(), f.getDataEfeito().toString(),
                    f.isAjusteDeMesAnterior() ? "sim" : "nao", t(f.getDescricao()), t(f.getReferenciaTipo()),
                    t(f.getReferenciaId()), t(dados), f.getRegistadoEm() != null ? f.getRegistadoEm().toString() : "")))
                    .append("\r\n");
        }
        return sb.toString();
    }
}
