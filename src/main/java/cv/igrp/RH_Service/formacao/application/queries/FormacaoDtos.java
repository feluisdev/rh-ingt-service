package cv.igrp.RH_Service.formacao.application.queries;

import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.formacao.application.dto.AccaoFormacaoDTO;
import cv.igrp.RH_Service.formacao.application.dto.AccaoFormacaoRequestDTO;
import cv.igrp.RH_Service.formacao.application.dto.InscricaoFormacaoDTO;
import cv.igrp.RH_Service.formacao.application.dto.NecessidadeFormacaoDTO;
import cv.igrp.RH_Service.formacao.application.dto.PlanoFormacaoDTO;
import cv.igrp.RH_Service.formacao.application.services.FormacaoService;
import cv.igrp.RH_Service.formacao.domain.models.AccaoFormacao;
import cv.igrp.RH_Service.formacao.domain.models.PlanoFormacao;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

/** Entre a formação do domínio e os DTOs, e a leitura dos valores vindos de fora. */
@Component
@RequiredArgsConstructor
public class FormacaoDtos {

    private final FormacaoService service;

    /** {@code so}: em /me, só a inscrição deste colaborador aparece. */
    public AccaoFormacaoDTO dto(AccaoFormacao a, FuncionarioId so) {
        return new AccaoFormacaoDTO(a.getId().getStringValor(), a.getPlanoId() != null ? a.getPlanoId().getStringValor() : null,
                new ArrayList<>(a.getNecessidades().stream().map(x -> x.getStringValor()).toList()), a.getTema(), a.getEntidadeFormadora(),
                a.getModalidade().name(), a.isInterna(), a.getInicio(), a.getFim(), a.getHoras(), a.getHorario(), a.getLocal(), a.getVagas(),
                a.getCustoPrevisto(), a.isCusteadaPelaAdministracao(), a.getMesesGarantia(), a.getEstado().name(), a.getMotivoCancelamento(),
                (int) a.admitidas(),
                new ArrayList<>(a.getInscricoes().stream().filter(i -> so == null || i.getFuncionarioId().equals(so))
                        .map(i -> new InscricaoFormacaoDTO(i.getId().getStringValor(), i.getFuncionarioId().getStringValor(),
                                service.nome(i.getFuncionarioId()), i.getOrigem().name(), i.getEstado().name(), i.getData(), i.getMotivo(),
                                i.getDiasPresenca(), i.getGarantiaAte())).toList()));
    }

    public PlanoFormacaoDTO dto(PlanoFormacao p) {
        return new PlanoFormacaoDTO(p.getId().getStringValor(), p.getAno(), p.getUnidadeId() != null ? p.getUnidadeId().toString() : null,
                p.getDesignacao(), p.getEstado().name(), p.getDespacho(), p.getDataAprovacao(),
                new ArrayList<>(p.getNecessidades().stream().map(n -> new NecessidadeFormacaoDTO(n.id().getStringValor(), n.tema(),
                        n.funcionarioId() != null ? n.funcionarioId().getStringValor() : null,
                        n.funcionarioId() != null ? service.nome(n.funcionarioId()) : null, n.origem().name(), n.prioridade().name(),
                        n.justificacao(), n.estado().name(), n.accaoId() != null ? n.accaoId().getStringValor() : null)).toList()));
    }

    public static FormacaoService.Dados dados(AccaoFormacaoRequestDTO r) {
        return new FormacaoService.Dados(r.getTema(), r.getEntidadeFormadora(), valor(AccaoFormacao.Modalidade.class, r.getModalidade(), "Modalidade"),
                r.getInterna(), r.getInicio(), r.getFim(), r.getHoras(), r.getHorario(), r.getLocal(), r.getVagas(), r.getCustoPrevisto(),
                r.getCusteadaPelaAdministracao(), r.getMesesGarantia());
    }

    public static <E extends Enum<E>> E valor(Class<E> tipo, String v, String oQue) {
        if (v == null || v.isBlank()) return null;
        try {
            return Enum.valueOf(tipo, v.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY, oQue + " desconhecido: " + v + ".");
        }
    }
}
