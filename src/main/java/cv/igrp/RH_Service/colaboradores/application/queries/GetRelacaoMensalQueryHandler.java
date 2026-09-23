package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.RH_Service.colaboradores.application.dto.RelacaoMensalDTO;
import cv.igrp.RH_Service.colaboradores.application.dto.RelacaoMensalLinhaDTO;
import cv.igrp.RH_Service.colaboradores.application.dto.RelacaoMensalUnidadeDTO;
import cv.igrp.RH_Service.colaboradores.application.dto.RubricaFaltaDTO;
import cv.igrp.RH_Service.colaboradores.application.dto.RubricaLicencaDTO;
import cv.igrp.RH_Service.colaboradores.application.services.RelacaoMensalService;
import cv.igrp.RH_Service.colaboradores.domain.models.TipoDiaSuplementar;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class GetRelacaoMensalQueryHandler implements QueryHandler<GetRelacaoMensalQuery, ResponseEntity<RelacaoMensalDTO>> {

    private final RelacaoMensalService relacaoMensalService;

    @IgrpQueryHandler
    public ResponseEntity<RelacaoMensalDTO> handle(GetRelacaoMensalQuery query) {
        var r = relacao(relacaoMensalService, query.getMes(), query.getUnidadeId(), query.getIncluirSubunidades());
        return ResponseEntity.ok(dto(r));
    }

    static RelacaoMensalService.Relacao relacao(RelacaoMensalService service, String mes, String unidadeId, Boolean subunidades) {
        YearMonth m;
        try {
            m = YearMonth.parse(mes == null ? "" : mes.trim());
        } catch (DateTimeParseException e) {
            throw IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY, "mes escreve-se yyyy-MM (ex.: 2026-09).");
        }
        UUID unidade = null;
        if (unidadeId != null && !unidadeId.isBlank()) {
            try {
                unidade = UUID.fromString(unidadeId.trim());
            } catch (IllegalArgumentException e) {
                throw IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY, "unidadeId inválido: " + unidadeId + ".");
            }
        }
        return service.relacao(m, unidade, subunidades == null || subunidades);
    }

    static RelacaoMensalDTO dto(RelacaoMensalService.Relacao r) {
        List<RelacaoMensalUnidadeDTO> unidades = r.unidades().stream().map(u -> new RelacaoMensalUnidadeDTO(
                u.unidade().getId().getStringValor(), u.unidade().getCode(), u.unidade().getName(), u.linhas().size(),
                u.comPendencias(), u.faltasPorJustificar(), u.linhas().stream().map(GetRelacaoMensalQueryHandler::linha).toList()
        )).toList();
        return new RelacaoMensalDTO(r.mes().toString(), r.provisoria(), r.raiz().getId().getStringValor(), r.raiz().getName(),
                r.incluirSubunidades(), unidades);
    }

    private static RelacaoMensalLinhaDTO linha(RelacaoMensalService.Linha l) {
        var f = l.funcionario();
        return new RelacaoMensalLinhaDTO(f.getId().getStringValor(), f.getNumeroFuncionario(), f.getNomeCompleto(),
                l.isento(), l.diasForaDoVinculo(), l.diasFerias(),
                l.faltasJustificadas().stream().map(GetRelacaoMensalQueryHandler::rubrica).toList(),
                l.faltasInjustificadas().stream().map(GetRelacaoMensalQueryHandler::rubrica).toList(),
                l.diasSemRegisto(), l.faltasParciais(), l.faltasPorJustificar(),
                l.licencas().stream().map(x -> new RubricaLicencaDTO(x.codigo(), x.nome(), x.tipoRegisto(), x.dias(),
                        x.afectaRemuneracao(), x.contaAntiguidade())).toList(),
                l.suplementarPorTipo().getOrDefault(TipoDiaSuplementar.DIA_UTIL, 0),
                l.suplementarPorTipo().getOrDefault(TipoDiaSuplementar.DESCANSO, 0),
                l.suplementarPorTipo().getOrDefault(TipoDiaSuplementar.FERIADO, 0),
                l.diasPorCorrigir(), l.diasPorValidar(), l.pedidosPendentes(), l.estado().name());
    }

    private static RubricaFaltaDTO rubrica(RelacaoMensalService.Rubrica x) {
        return new RubricaFaltaDTO(x.codigo(), x.nome(), x.dias(), x.minutos(), x.efeitoRemuneracao(), x.opcaoFaltaInjustificada());
    }
}
