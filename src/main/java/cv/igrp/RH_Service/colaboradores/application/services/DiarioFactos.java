package cv.igrp.RH_Service.colaboradores.application.services;

import cv.igrp.RH_Service.colaboradores.domain.models.Assignment;
import cv.igrp.RH_Service.colaboradores.domain.models.FactoRh;
import cv.igrp.RH_Service.colaboradores.domain.models.TipoFactoRh;
import cv.igrp.RH_Service.colaboradores.domain.repository.FactoRhRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * <b>O diário de factos para o processamento salarial</b> (BR-FAC-01..05). Cada serviço que muda algo
 * que o salarial precisa de saber regista aqui o facto, na mesma transacção. O diário só cresce; a
 * leitura por mês é o contrato com a outra aplicação (e a base da exportação do fecho, F5.1).
 *
 * <pre>{@code
 * diarioFactos.registar(funcionarioId, TipoFactoRh.CESSACAO, dataEfeito, "HISTORICO_ESTADO", id,
 *         "Cessação: exoneração", Map.of("estado", "EXONERADO"));
 * diarioFactos.movimento(TipoFactoRh.PROGRESSAO, afectacaoNova, "Progressão para o escalão 3");
 * }</pre>
 */
@Service
@RequiredArgsConstructor
public class DiarioFactos {

    private final FactoRhRepository repository;
    private final CompetenciaSalarial competenciaSalarial;
    /** Quem reage a um facto na mesma transacção (ex.: os actos sujeitos a publicação, BR-PUB-02). */
    private final org.springframework.context.ApplicationEventPublisher eventos;
    /** O bruto base do escalão (t_grade.salary_base) vai nos factos de movimento: o RH parametriza-o, o salarial usa-o. */
    private final cv.igrp.RH_Service.carreiras.domain.repository.GradeRepository gradeRepository;

    /** Um facto acabou de ser registado. */
    public record FactoRegistado(FactoRh facto) {}

    @Transactional
    public FactoRh registar(FuncionarioId funcionarioId, TipoFactoRh tipo, LocalDate dataEfeito,
                            String referenciaTipo, Object referenciaId, String descricao, Map<String, ?> dados) {
        FactoRh facto = repository.save(FactoRh.registar(funcionarioId, tipo, dataEfeito,
                competenciaSalarial.competencia(dataEfeito), referenciaTipo,
                referenciaId != null ? referenciaId.toString() : null, descricao, dados, agora()));
        if (facto != null) eventos.publishEvent(new FactoRegistado(facto));
        return facto;
    }

    /** Um movimento que abre uma afectação: o Lugar, o escalão (com o bruto base), a função e a origem vão nos dados. */
    @Transactional
    public FactoRh movimento(TipoFactoRh tipo, Assignment afectacao, String descricao) {
        var dados = new LinkedHashMap<String, Object>();
        dados.put("lugarId", afectacao.getPositionId());
        dados.put("escalaoId", afectacao.getGradeId());
        // O bruto do escalão à data do registo (BR-FAC-06): as remunerações e os descontos calcula-os o salarial.
        if (afectacao.getGradeId() != null && gradeRepository != null)
            gradeRepository.findById(cv.igrp.RH_Service.carreiras.domain.valueobject.GradeId.from(afectacao.getGradeId()))
                    .ifPresent(g -> dados.put("remuneracaoBase", g.getSalaryBase()));
        dados.put("funcaoId", afectacao.getFunctionId());
        dados.put("origem", afectacao.getOrigem());
        return registar(afectacao.getFuncionarioId(), tipo, afectacao.getDataInicio(), "AFECTACAO",
                afectacao.getId().getStringValor(), descricao, dados);
    }

    LocalDateTime agora() { return LocalDateTime.now(); }
}
