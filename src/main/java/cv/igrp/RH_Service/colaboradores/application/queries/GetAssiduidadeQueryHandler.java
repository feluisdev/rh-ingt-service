package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.RH_Service.colaboradores.application.dto.AssiduidadeResponseDTO;
import cv.igrp.RH_Service.colaboradores.application.dto.DiaAssiduidadeDTO;
import cv.igrp.RH_Service.colaboradores.application.dto.MarcacaoDTO;
import cv.igrp.RH_Service.colaboradores.application.dto.PeriodoPresencaDTO;
import cv.igrp.RH_Service.colaboradores.application.dto.SemanaAssiduidadeDTO;
import cv.igrp.RH_Service.colaboradores.application.services.AssiduidadeService;
import cv.igrp.RH_Service.colaboradores.domain.models.MarcacaoAssiduidade;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

/** Por dia: períodos, intervalos, horas trabalhadas e esperadas, anomalias; por semana, os totais. */
@Component
@RequiredArgsConstructor
public class GetAssiduidadeQueryHandler implements QueryHandler<GetAssiduidadeQuery, ResponseEntity<AssiduidadeResponseDTO>> {

    private final AssiduidadeService assiduidadeService;

    @IgrpQueryHandler
    public ResponseEntity<AssiduidadeResponseDTO> handle(GetAssiduidadeQuery query) {
        var funcionarioId = FuncionarioId.from(query.getFuncionarioId());
        var consulta = assiduidadeService.consultar(funcionarioId, query.getDe(), query.getAte());

        var r = new AssiduidadeResponseDTO();
        r.setFuncionarioId(funcionarioId.getStringValor());
        r.setDe(query.getDe());
        r.setAte(query.getAte());
        r.setDias(consulta.dias().stream().map(d -> new DiaAssiduidadeDTO(
                d.dia().data(),
                d.dia().periodos().stream().map(p -> new PeriodoPresencaDTO(p.entrada().toString(), p.saida().toString(), p.minutos())).toList(),
                d.dia().intervalosMinutos(),
                d.dia().minutosTrabalhados(),
                d.minutosEsperados(),
                d.horarioNome(),
                d.feriado(),
                d.dia().anomalias().stream().map(Enum::name).sorted().toList(),
                d.marcacoes().stream().map(GetAssiduidadeQueryHandler::dto).toList())).toList());
        r.setSemanas(consulta.semanas().stream().map(s -> new SemanaAssiduidadeDTO(
                s.ano(), s.semana(), s.inicio(), s.minutosTrabalhados(), s.minutosEsperados())).toList());
        return ResponseEntity.ok(r);
    }

    private static MarcacaoDTO dto(MarcacaoAssiduidade m) {
        return new MarcacaoDTO(m.getId().getStringValor(), m.getMomento(), m.getSentido().name(), m.getOrigem().name(),
                m.getMotivo(), m.getReferenciaExterna(), m.isAnulada(), m.getMotivoAnulacao(), m.getAnuladaEm(),
                m.getEstado().name(), m.getMotivoRejeicao());
    }
}
