package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.RH_Service.colaboradores.application.dto.DebitoAfericaoDTO;
import cv.igrp.RH_Service.colaboradores.application.dto.DiaApuradoDTO;
import cv.igrp.RH_Service.colaboradores.application.dto.FaltasApuradasResponseDTO;
import cv.igrp.RH_Service.colaboradores.application.services.ApuramentoFaltasService;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.time.YearMonth;
import java.time.format.DateTimeParseException;

/** As faltas por débito do mês (DL n.º 3/2010, art. 13.º), calculadas até ao fecho do mês. */
@Component
@RequiredArgsConstructor
public class GetFaltasApuradasQueryHandler implements QueryHandler<GetFaltasApuradasQuery, ResponseEntity<FaltasApuradasResponseDTO>> {

    private final ApuramentoFaltasService apuramentoFaltasService;

    @IgrpQueryHandler
    public ResponseEntity<FaltasApuradasResponseDTO> handle(GetFaltasApuradasQuery query) {
        YearMonth mes;
        try {
            mes = YearMonth.parse(query.getMes() == null ? "" : query.getMes().trim());
        } catch (DateTimeParseException e) {
            throw IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY, "mes escreve-se yyyy-MM (ex.: 2026-09).");
        }
        var funcionarioId = FuncionarioId.from(query.getFuncionarioId());
        var a = apuramentoFaltasService.apurar(funcionarioId, mes);
        var r = a.resultado();

        var dto = new FaltasApuradasResponseDTO();
        dto.setFuncionarioId(funcionarioId.getStringValor());
        dto.setMes(mes.toString());
        dto.setIsento(a.isento());
        dto.setDias(r.dias().stream().map(d -> new DiaApuradoDTO(d.data(), d.estado().name(),
                d.motivo() != null ? d.motivo().name() : null, d.minutosEsperados(), d.minutosTrabalhados(),
                d.minutosEmFalta())).toList());
        dto.setDebitos(r.debitos().stream().map(b -> new DebitoAfericaoDTO(b.horarioNome(), b.periodo().name(),
                b.inicio(), b.fim(), b.minutosEsperados(), b.minutosTrabalhados(), b.minutosJaEmFalta(),
                b.minutosDebito())).toList());
        dto.setDiasSemRegisto(r.diasSemRegisto());
        dto.setMinutosParciais(r.minutosParciais());
        dto.setPeriodoNormalMinutos(r.periodoNormalMinutos());
        dto.setFaltasParciais(r.faltasParciais());
        dto.setTotalFaltas(r.totalFaltas());
        dto.setDiasPorCorrigir(r.diasPorCorrigir());
        return ResponseEntity.ok(dto);
    }
}
