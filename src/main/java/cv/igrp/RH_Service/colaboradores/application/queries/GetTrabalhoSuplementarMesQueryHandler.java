package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.RH_Service.colaboradores.application.dto.TrabalhoSuplementarDTO;
import cv.igrp.RH_Service.colaboradores.application.dto.TrabalhoSuplementarMesDTO;
import cv.igrp.RH_Service.colaboradores.application.services.TrabalhoSuplementarService;
import cv.igrp.RH_Service.colaboradores.domain.models.Funcionario;
import cv.igrp.RH_Service.colaboradores.domain.models.TipoDiaSuplementar;
import cv.igrp.RH_Service.colaboradores.domain.models.TrabalhoSuplementar;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.domain.service.CurrentEmployeeResolver;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.time.YearMonth;
import java.time.format.DateTimeParseException;

@Component
@RequiredArgsConstructor
public class GetTrabalhoSuplementarMesQueryHandler
        implements QueryHandler<GetTrabalhoSuplementarMesQuery, ResponseEntity<TrabalhoSuplementarMesDTO>> {

    private final CurrentEmployeeResolver currentEmployeeResolver;
    private final TrabalhoSuplementarService trabalhoSuplementarService;

    @IgrpQueryHandler
    public ResponseEntity<TrabalhoSuplementarMesDTO> handle(GetTrabalhoSuplementarMesQuery query) {
        YearMonth mes;
        try {
            mes = YearMonth.parse(query.getMes() == null ? "" : query.getMes().trim());
        } catch (DateTimeParseException e) {
            throw IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY, "mes escreve-se yyyy-MM (ex.: 2026-09).");
        }
        FuncionarioId funcionarioId = query.getFuncionarioId() != null
                ? FuncionarioId.from(query.getFuncionarioId()) : currentEmployeeResolver.resolve();
        var m = trabalhoSuplementarService.doMes(funcionarioId, mes);

        var dto = new TrabalhoSuplementarMesDTO();
        dto.setFuncionarioId(funcionarioId.getStringValor());
        dto.setMes(mes.toString());
        dto.setTrabalhos(m.linhas().stream()
                .map(l -> dto(l.trabalho(), null, l.tipoDia(), l.minutosRealizados(), l.semRegisto())).toList());
        dto.setMinutosAutorizados(m.minutosAutorizados());
        dto.setMinutosRealizados(m.minutosRealizados());
        dto.setMinutosRealizadosDiaUtil(m.realizadosPorTipo().get(TipoDiaSuplementar.DIA_UTIL));
        dto.setMinutosRealizadosDescanso(m.realizadosPorTipo().get(TipoDiaSuplementar.DESCANSO));
        dto.setMinutosRealizadosFeriado(m.realizadosPorTipo().get(TipoDiaSuplementar.FERIADO));
        return ResponseEntity.ok(dto);
    }

    static TrabalhoSuplementarDTO dto(TrabalhoSuplementar t, Funcionario f, TipoDiaSuplementar tipo,
                                      int realizados, boolean semRegisto) {
        return new TrabalhoSuplementarDTO(t.getId().getStringValor(), t.getFuncionarioId().getStringValor(),
                f != null ? f.getNumeroFuncionario() : null, f != null ? f.getNomeCompleto() : null,
                t.getData(), t.getHoraInicio().toString(), t.getHoraFim().toString(), t.getMotivo(),
                t.getEstado().name(), t.isPedidoPeloProprio(), t.isAutorizacaoPosterior(),
                t.getDecididoPor() != null ? t.getDecididoPor().getStringValor() : null, t.getDecididoEm(),
                t.getMotivoRecusa(), t.getMotivoCancelamento(), tipo != null ? tipo.name() : null,
                t.minutosAutorizados(), realizados, semRegisto);
    }
}
