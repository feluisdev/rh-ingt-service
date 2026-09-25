package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.RH_Service.colaboradores.application.dto.RelatorioAposentacaoDTO;
import cv.igrp.RH_Service.colaboradores.application.services.AposentacaoService;
import cv.igrp.RH_Service.shared.domain.service.Entrada;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class GetRelatorioAposentacaoQueryHandler
        implements QueryHandler<GetRelatorioAposentacaoQuery, ResponseEntity<RelatorioAposentacaoDTO>> {

    private final AposentacaoService aposentacaoService;

    @IgrpQueryHandler
    public ResponseEntity<RelatorioAposentacaoDTO> handle(GetRelatorioAposentacaoQuery q) {
        return ResponseEntity.ok(relatorio(aposentacaoService, q.getUnidadeId(), q.getIncluirSubunidades(), q.getAte()));
    }

    static RelatorioAposentacaoDTO relatorio(AposentacaoService service, String unidadeId, Boolean subunidades, LocalDate ate) {
        var unidade = Entrada.uuid(unidadeId, "a unidade orgânica");
        LocalDate hoje = LocalDate.now();
        LocalDate limite = ate != null ? ate : hoje.plusMonths(12);
        var linhas = AposentacaoDtos.linhas(service.relatorio(unidade, !Boolean.FALSE.equals(subunidades), limite));
        return new RelatorioAposentacaoDTO(hoje, limite, unidade.toString(), linhas.size(), linhas);
    }
}
