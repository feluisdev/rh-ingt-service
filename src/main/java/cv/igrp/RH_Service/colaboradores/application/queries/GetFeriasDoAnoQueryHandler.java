package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.RH_Service.colaboradores.application.dto.FeriasAlteracaoDTO;
import cv.igrp.RH_Service.colaboradores.application.dto.FeriasAnoResponseDTO;
import cv.igrp.RH_Service.colaboradores.application.dto.PeriodoFeriasDTO;
import cv.igrp.RH_Service.colaboradores.application.services.MapaFeriasService;
import cv.igrp.RH_Service.parametrizacoes.application.services.ParametrosFeriasService;
import cv.igrp.RH_Service.colaboradores.domain.models.MapaFerias;
import cv.igrp.RH_Service.colaboradores.domain.models.PeriodoFerias;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.MapaFeriasRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;

/** As férias de um colaborador num ano. Sem nada indicado nem marcado, vem vazio — não é 404. */
@Component
@RequiredArgsConstructor
public class GetFeriasDoAnoQueryHandler implements QueryHandler<GetFeriasDoAnoQuery, ResponseEntity<FeriasAnoResponseDTO>> {

    private final MapaFeriasService mapaFeriasService;
    private final MapaFeriasRepository mapaFeriasRepository;
    private final FuncionarioRepository funcionarioRepository;
    private final ParametrosFeriasService parametrosFerias;

    @IgrpQueryHandler
    public ResponseEntity<FeriasAnoResponseDTO> handle(GetFeriasDoAnoQuery query) {
        var funcionarioId = FuncionarioId.from(query.getFuncionarioId());
        funcionarioRepository.findById(funcionarioId)
                .orElseThrow(() -> IgrpResponseStatusException.notFound("Funcionário não encontrado: " + query.getFuncionarioId()));
        int ano = query.getAno();

        var r = new FeriasAnoResponseDTO();
        r.setFuncionarioId(funcionarioId.getStringValor());
        r.setAno(ano);
        r.setDireito(mapaFeriasService.direitoParaMarcar(funcionarioId, ano));
        r.setPrazoPreferencia(parametrosFerias.vigenteEm(ano).prazoPreferencia(ano));
        r.setMapaPublicadoEm(mapaFeriasRepository.findByAno(ano).map(MapaFerias::getPublicadoEm).orElse(null));

        mapaFeriasService.consultar(funcionarioId, ano).ifPresent(f -> {
            r.setPreferencia(dtos(f.getPreferencia()));
            r.setPreferenciaIndicadaEm(f.getPreferenciaIndicadaEm());
            r.setPreferenciaForaDePrazo(f.isPreferenciaForaDePrazo());
            r.setPreferenciaObservacoes(f.getPreferenciaObservacoes());
            r.setMarcacao(dtos(f.getMarcacao()));
            r.setOrigem(f.getOrigem() != null ? f.getOrigem().name() : null);
            r.setFundamentacao(f.getFundamentacao());
            r.setMarcadaEm(f.getMarcadaEm());
            r.setTotalMarcado(f.totalMarcado());
            r.setAlteracoes(f.getAlteracoes().stream()
                    .map(a -> new FeriasAlteracaoDTO(a.motivo().name(), a.fundamentacao(),
                            a.periodosAnteriores(), a.periodosNovos(), a.alteradaEm()))
                    .toList());
        });
        return ResponseEntity.ok(r);
    }

    static List<PeriodoFeriasDTO> dtos(List<PeriodoFerias> periodos) {
        return periodos.stream().map(p -> new PeriodoFeriasDTO(p.inicio(), p.fim(), p.diasUteis())).toList();
    }
}
