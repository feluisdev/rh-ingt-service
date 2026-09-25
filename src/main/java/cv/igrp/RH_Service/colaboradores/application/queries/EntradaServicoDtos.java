package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.RH_Service.colaboradores.application.dto.PeriodoProvaDTO;
import cv.igrp.RH_Service.colaboradores.application.dto.ProvimentoDTO;
import cv.igrp.RH_Service.colaboradores.domain.models.Funcionario;
import cv.igrp.RH_Service.colaboradores.domain.models.PeriodoProva;
import cv.igrp.RH_Service.colaboradores.domain.models.Provimento;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;

import java.util.ArrayList;
import java.util.List;

/** Dos provimentos e períodos de prova para os DTOs. */
public final class EntradaServicoDtos {

    private EntradaServicoDtos() {}

    public static ProvimentoDTO dto(Provimento p, List<String> alertas) {
        return new ProvimentoDTO(p.getId().getStringValor(), p.getFuncionarioId().getStringValor(), p.getModalidade().name(),
                p.getDespachoNumero(), p.getDespachoData(), p.getDataPosse(), p.getConcursoRef(), p.isVemDeOutraCarreira(),
                p.getPeriodoProvaId() != null ? p.getPeriodoProvaId().getStringValor() : null,
                p.getAnteriorId() != null ? p.getAnteriorId().getStringValor() : null, p.getObservacoes(),
                new ArrayList<>(alertas != null ? alertas : List.of()));
    }

    public static PeriodoProvaDTO dto(PeriodoProva p, FuncionarioRepository funcionarios, List<String> alertas) {
        return new PeriodoProvaDTO(p.getId().getStringValor(), p.getProvimentoId().getStringValor(),
                p.getFuncionarioId().getStringValor(), nome(funcionarios, p.getFuncionarioId()), p.getTipo().name(), p.getInicio(),
                p.getFimPrevisto(), p.getTutorId() != null ? p.getTutorId().getStringValor() : null,
                p.getTutorId() != null ? nome(funcionarios, p.getTutorId()) : null, p.getEstado().name(), p.getDataRelatorio(),
                p.getAvaliacao() != null ? p.getAvaliacao().name() : null, p.getFundamentacao(), p.getDataFim(),
                new ArrayList<>(alertas != null ? alertas : List.of()));
    }

    private static String nome(FuncionarioRepository funcionarios, FuncionarioId id) {
        return funcionarios.findById(id).map(Funcionario::getNomeCompleto).orElse(null);
    }
}
