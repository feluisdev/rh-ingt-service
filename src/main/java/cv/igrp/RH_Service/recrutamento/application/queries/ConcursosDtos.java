package cv.igrp.RH_Service.recrutamento.application.queries;

import cv.igrp.RH_Service.recrutamento.application.dto.CandidaturaDTO;
import cv.igrp.RH_Service.recrutamento.application.dto.ConcursoDTO;
import cv.igrp.RH_Service.recrutamento.application.dto.ConcursoRequestDTO;
import cv.igrp.RH_Service.recrutamento.application.dto.MembroJuriDTO;
import cv.igrp.RH_Service.recrutamento.application.dto.MetodoConcursoDTO;
import cv.igrp.RH_Service.recrutamento.application.dto.NotaCandidaturaDTO;
import cv.igrp.RH_Service.recrutamento.application.services.ConcursoService;
import cv.igrp.RH_Service.recrutamento.domain.models.Candidatura;
import cv.igrp.RH_Service.recrutamento.domain.models.Concurso;
import cv.igrp.RH_Service.recrutamento.domain.models.MetodoSelecao;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.domain.service.Entrada;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Entre o domínio do recrutamento e os DTOs, e a leitura dos valores vindos de fora. */
public final class ConcursosDtos {

    private ConcursosDtos() {}

    public static ConcursoDTO dto(Concurso c, List<Candidatura> candidaturas) {
        return new ConcursoDTO(c.getId().getStringValor(), c.getReferencia(), c.getFinalidade().name(), c.getTipo().name(),
                c.getModalidade().name(), c.getVinculo(), c.getCategoriaId().toString(),
                new ArrayList<>(c.getLugares().stream().map(UUID::toString).toList()), c.getRequisitos(), c.getHabilitacaoMinima(),
                c.getQuotaDeficiencia(), new ArrayList<>(c.getMetodos().stream().map(m -> new MetodoConcursoDTO(m.metodo().name(),
                m.ponderacao(), m.eliminatorio(), m.notaMinima())).toList()), c.getDispensaMetodosDespacho(),
                new ArrayList<>(c.getJuri().stream().map(m -> new MembroJuriDTO(m.papel().name(), m.nome(),
                        m.funcionarioId() != null ? m.funcionarioId().toString() : null)).toList()),
                c.getDataAviso(), c.getCandidaturasDe(), c.getCandidaturasAte(), c.getEstado().name(), c.getHomologacaoDespacho(),
                c.getHomologacaoData(), c.getReservaAte(), c.getMotivoAnulacao(), candidaturas != null ? candidaturas.size() : 0,
                candidaturas == null ? new ArrayList<>() : new ArrayList<>(candidaturas.stream().map(ConcursosDtos::dto).toList()));
    }

    public static CandidaturaDTO dto(Candidatura x) {
        return new CandidaturaDTO(x.getId().getStringValor(), x.getConcursoId().getStringValor(), x.getNome(), x.getDocumento(),
                x.getNif(), x.getEmail(), x.getTelefone(), x.getHabilitacao(), x.isDeficiencia(),
                x.getFuncionarioId() != null ? x.getFuncionarioId().toString() : null, x.isVinculadoAdministracao(),
                x.getDataApresentacao(), x.getEstado().name(), x.getMotivoExclusao(), x.getAudienciaAte(), x.getRespostaAudiencia(),
                new ArrayList<>(x.getNotas().entrySet().stream().map(e -> new NotaCandidaturaDTO(e.getKey().name(), e.getValue())).toList()),
                x.getClassificacaoFinal(), x.getPosicao(), x.getLugarProvidoId() != null ? x.getLugarProvidoId().toString() : null,
                x.getDataDesistencia());
    }

    public static ConcursoService.Dados dados(ConcursoRequestDTO r) {
        return new ConcursoService.Dados(r.getReferencia(), valor(Concurso.Finalidade.class, r.getFinalidade(), "Finalidade"),
                valor(Concurso.Tipo.class, r.getTipo(), "Tipo de concurso"), valor(Concurso.Modalidade.class, r.getModalidade(), "Modalidade"),
                r.getVinculo(), Entrada.uuidOpcional(r.getCategoriaId(), "a categoria"),
                r.getRequisitos(), r.getHabilitacaoMinima(), r.getQuotaDeficiencia(),
                r.getLugares() == null ? null : r.getLugares().stream().map(l -> Entrada.uuid(l, "o Lugar")).toList(),
                r.getMetodos() == null ? null : r.getMetodos().stream().map(m -> new Concurso.Metodo(
                        valor(MetodoSelecao.class, m.getMetodo(), "Método de selecção"), m.getPonderacao() != null ? m.getPonderacao() : 0,
                        Boolean.TRUE.equals(m.getEliminatorio()), m.getNotaMinima())).toList(),
                r.getDispensaMetodosDespacho(),
                r.getJuri() == null ? null : r.getJuri().stream().map(m -> new Concurso.MembroJuri(
                        valor(Concurso.PapelJuri.class, m.getPapel(), "Papel no júri"), m.getNome(),
                        Entrada.uuidOpcional(m.getFuncionarioId(), "o membro do júri"))).toList(),
                r.getDataAviso(), r.getCandidaturasDe(), r.getCandidaturasAte());
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
