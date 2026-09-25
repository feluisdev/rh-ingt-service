package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.RH_Service.colaboradores.application.dto.FactoRhDTO;
import cv.igrp.RH_Service.colaboradores.application.dto.FactosSalariaisDTO;
import cv.igrp.RH_Service.colaboradores.domain.models.FactoRh;
import cv.igrp.RH_Service.colaboradores.domain.models.Funcionario;
import cv.igrp.RH_Service.colaboradores.domain.repository.FactoRhRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class GetFactosSalariaisQueryHandler
        implements QueryHandler<GetFactosSalariaisQuery, ResponseEntity<FactosSalariaisDTO>> {

    public static final int VERSAO_CONTRATO = 1;

    private final FactoRhRepository factoRepository;
    private final FuncionarioRepository funcionarioRepository;

    @IgrpQueryHandler
    public ResponseEntity<FactosSalariaisDTO> handle(GetFactosSalariaisQuery query) {
        List<FactoRh> factos;
        String mes = null;
        if (query.getFuncionarioId() != null) {
            factos = factoRepository.findByFuncionario(funcionarioId(query.getFuncionarioId()));
        } else {
            YearMonth m = mes(query.getMes());
            mes = m.toString();
            factos = factoRepository.findByMesCompetencia(m);
        }
        List<FactoRhDTO> dtos = dtos(factos, funcionarioRepository);
        return ResponseEntity.ok(new FactosSalariaisDTO(VERSAO_CONTRATO, mes, dtos.size(), dtos));
    }

    static List<FactoRhDTO> dtos(List<FactoRh> factos, FuncionarioRepository funcionarios) {
        Map<FuncionarioId, Optional<Funcionario>> cache = new HashMap<>();
        return factos.stream().map(f -> {
            Funcionario p = cache.computeIfAbsent(f.getFuncionarioId(), funcionarios::findById).orElse(null);
            return new FactoRhDTO(f.getId().getStringValor(), f.getFuncionarioId().getStringValor(),
                    p != null ? p.getNumeroFuncionario() : null, p != null ? p.getNif() : null,
                    p != null ? p.getNomeCompleto() : null, f.getTipo().name(), f.getDataEfeito(),
                    f.getMesCompetencia().toString(), f.isAjusteDeMesAnterior(), f.getReferenciaTipo(),
                    f.getReferenciaId(), f.getDescricao(), f.getDados(), f.getRegistadoEm());
        }).toList();
    }

    static YearMonth mes(String valor) {
        try {
            return YearMonth.parse(valor == null ? "" : valor.trim());
        } catch (DateTimeParseException e) {
            throw IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Indique o mês no formato ano-mês, por exemplo 2026-09.");
        }
    }

    private static FuncionarioId funcionarioId(String valor) {
        try {
            return FuncionarioId.from(valor.trim());
        } catch (IllegalArgumentException e) {
            throw IgrpResponseStatusException.notFound("Colaborador não encontrado.");
        }
    }
}
