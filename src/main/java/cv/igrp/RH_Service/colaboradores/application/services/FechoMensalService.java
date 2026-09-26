package cv.igrp.RH_Service.colaboradores.application.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import cv.igrp.RH_Service.colaboradores.domain.models.FechoMensal;
import cv.igrp.RH_Service.colaboradores.domain.repository.FactoRhRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.FechoMensalRepository;
import cv.igrp.RH_Service.shared.application.services.notificacoes.Notificador;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.domain.notificacoes.TipoNotificacao;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * <b>Fecho mensal e exportação para o salarial</b> (DL n.º 3/2010, art. 75.º; BR-FEC-01..08). O RH é a fonte dos factos (quem
 * está em que escalão — com o bruto base —, situação, dias, horas, ausências, missões, penas); o salarial calcula as
 * remunerações, os suplementos, os descontos e o líquido. Fechar fotografa a relação mensal e fixa o mês: os factos que
 * depois tenham efeito nele entram no mês seguinte.
 */
@Service
@RequiredArgsConstructor
public class FechoMensalService {

    /** A versão do contrato de exportação. */
    public static final int VERSAO = 1;

    public record Resultado(FechoMensal fecho, int linhas, List<String> alertas) {}

    /** O que o salarial recebe de um mês. */
    public record Exportacao(YearMonth mes, String estado, LocalDateTime fechadoEm, boolean provisoria, List<Map<String, Object>> relacao) {}

    private final FechoMensalRepository repository;
    private final FactoRhRepository factoRepository;
    private final RelacaoMensalService relacaoMensalService;
    private final cv.igrp.RH_Service.estrutura.domain.repository.OrganizationalUnitRepository unidadeRepository;
    private final ObjectMapper objectMapper;
    private final Notificador notificador;

    /** Fechar (ou fechar de novo, se foi reaberto). As pendências da relação mensal avisam, não impedem. */
    @Transactional
    public Resultado fechar(YearMonth mes) {
        var linhas = linhas(mes);
        String json = json(linhas);
        int factos = factoRepository.findByMesCompetencia(mes).size();
        var existente = repository.findByMes(mes);
        FechoMensal f;
        if (existente.isPresent()) {
            f = existente.get();
            f.fecharDeNovo(json, factos, agora());
        } else {
            f = FechoMensal.fechar(mes, YearMonth.from(agora()), json, factos, agora());
        }
        var gravado = repository.save(f);
        var alertas = new ArrayList<String>();
        long pendentes = linhas.stream().filter(l -> "COM_PENDENCIAS".equals(String.valueOf(l.get("estado")))).count();
        if (pendentes > 0)
            alertas.add(pendentes + " colaborador(es) com pendências na relação mensal (faltas por justificar, dias por validar ou por corrigir): "
                    + "o que se resolver agora entra no mês seguinte.");
        notificador.paraRh().tipo(TipoNotificacao.AVISO).titulo("Mês " + mes + " fechado para o processamento salarial")
                .texto(factos + " facto(s); " + linhas.size() + " linha(s) na relação mensal.").recurso("FECHO_MENSAL", mes.toString()).enviar();
        return new Resultado(gravado, linhas.size(), alertas);
    }

    @Transactional
    public Resultado reabrir(YearMonth mes, String motivo) {
        var f = repository.findByMes(mes).orElseThrow(() -> IgrpResponseStatusException.notFound("Este mês não foi fechado."));
        f.reabrir(motivo, agora());
        return new Resultado(repository.save(f), 0, List.of("O salarial pode já ter processado este mês: combine o acerto com a equipa do salarial."));
    }

    @Transactional(readOnly = true)
    public List<FechoMensal> fechos() {
        return repository.findAll();
    }

    /** A exportação de um mês: a relação congelada (se fechado) ou a de hoje (provisória). Os factos, pelo mês de competência. */
    @Transactional(readOnly = true)
    public Exportacao exportar(YearMonth mes) {
        Optional<FechoMensal> f = repository.findByMes(mes);
        if (f.isPresent() && f.get().fechado())
            return new Exportacao(mes, FechoMensal.Estado.FECHADO.name(), f.get().getFechadoEm(), false, ler(f.get().getRelacao()));
        return new Exportacao(mes, f.map(x -> x.getEstado().name()).orElse("ABERTO"), f.map(FechoMensal::getFechadoEm).orElse(null), true, linhas(mes));
    }

    /**
     * A relação mensal de toda a entidade — a de cada unidade de topo com as subunidades (art. 75.º n.º 1) —, em linhas simples
     * (identificadores e números, sem objectos de domínio).
     */
    List<Map<String, Object>> linhas(YearMonth mes) {
        var l = new ArrayList<Map<String, Object>>();
        for (var raiz : unidadeRepository.findAllActive()) {
            if (raiz.getParentUnitId() != null) continue;
            var r = relacaoMensalService.relacao(mes, raiz.getId().getValor(), true);
            acrescentar(l, r);
        }
        return l;
    }

    private void acrescentar(List<Map<String, Object>> l, RelacaoMensalService.Relacao r) {
        for (var u : r.unidades())
            for (var x : u.linhas()) {
                var m = new LinkedHashMap<String, Object>();
                m.put("funcionarioId", x.funcionario().getId() != null ? x.funcionario().getId().getStringValor() : null);
                m.put("numeroFuncionario", x.funcionario().getNumeroFuncionario());
                m.put("nif", x.funcionario().getNif());
                m.put("nome", x.funcionario().getNomeCompleto());
                m.put("unidadeId", x.unidadeId() != null ? x.unidadeId().toString() : null);
                m.put("isento", x.isento());
                m.put("diasForaDoVinculo", x.diasForaDoVinculo());
                m.put("diasFerias", x.diasFerias());
                m.put("faltasJustificadas", x.faltasJustificadas());
                m.put("faltasInjustificadas", x.faltasInjustificadas());
                m.put("diasSemRegisto", x.diasSemRegisto());
                m.put("faltasParciais", x.faltasParciais());
                m.put("faltasPorJustificar", x.faltasPorJustificar());
                m.put("licencas", x.licencas());
                m.put("suplementarPorTipo", x.suplementarPorTipo());
                m.put("estado", x.estado().name());
                l.add(m);
            }
    }

    private String json(List<Map<String, Object>> linhas) {
        try {
            return objectMapper.writeValueAsString(linhas);
        } catch (Exception e) {
            throw new IllegalStateException("Não foi possível guardar a relação mensal do fecho.", e);
        }
    }

    private List<Map<String, Object>> ler(String json) {
        if (json == null || json.isBlank()) return List.of();
        try {
            return objectMapper.readValue(json, new TypeReference<List<Map<String, Object>>>() {});
        } catch (Exception e) {
            throw new IllegalStateException("A relação mensal guardada no fecho não se pode ler.", e);
        }
    }

    LocalDateTime agora() { return LocalDateTime.now(); }
}
