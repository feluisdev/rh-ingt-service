package cv.igrp.RH_Service.colaboradores.application.services;

import cv.igrp.RH_Service.colaboradores.domain.models.AcidenteServico;
import cv.igrp.RH_Service.colaboradores.domain.models.Funcionario;
import cv.igrp.RH_Service.colaboradores.domain.models.ModalidadeAposentacao;
import cv.igrp.RH_Service.colaboradores.domain.models.ProcessoAposentacao;
import cv.igrp.RH_Service.colaboradores.domain.models.TipoFactoRh;
import cv.igrp.RH_Service.colaboradores.domain.repository.AcidenteServicoRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.AcidenteServicoId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.shared.application.services.notificacoes.Notificador;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.domain.notificacoes.TipoNotificacao;
import cv.igrp.RH_Service.shared.domain.service.Datas;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

/**
 * <b>Acidentes em serviço e doenças profissionais</b> (Lei n.º 20/X/2023, arts. 187.º–191.º; BR-SST-01..10). A participação
 * (pelo próprio ou pelo RH), a qualificação, as incapacidades temporárias e a alta, a incapacidade permanente (que abre a
 * aposentação por invalidez) e a seguradora. O salarial recebe os factos: a remuneração mantém-se (art. 189.º n.º 2).
 */
@Service
@RequiredArgsConstructor
public class AcidenteServicoService {

    static final String RECURSO = "ACIDENTE_SERVICO";

    public record Resultado(AcidenteServico acidente, List<String> alertas) {}

    private final AcidenteServicoRepository repository;
    private final FuncionarioRepository funcionarioRepository;
    private final CalendarioFeriadosService calendario;
    private final DiarioFactos diarioFactos;
    private final AposentacaoService aposentacaoService;
    private final ChefiaService chefiaService;
    private final Notificador notificador;

    @Transactional
    public Resultado participar(FuncionarioId funcionarioId, AcidenteServico.Tipo tipo, LocalDateTime dataHora, String local, String descricao,
                                String testemunhas, LocalDate dataParticipacao, boolean peloProprio) {
        var f = funcionario(funcionarioId);
        var a = repository.save(AcidenteServico.participar(funcionarioId, tipo, dataHora, local, descricao, testemunhas, dataParticipacao,
                peloProprio, hoje()));
        String titulo = "Participação de " + nome(a.getTipo()) + " de " + f.getNomeCompleto() + " (" + Datas.pt(a.getDataHora().toLocalDate()) + ")";
        notificador.paraRh().tipo(TipoNotificacao.ACIDENTE_SERVICO).titulo(titulo).recurso(RECURSO, a.getId().getStringValor()).enviar();
        notificador.para(chefiaService.chefeDirecto(funcionarioId)).tipo(TipoNotificacao.ACIDENTE_SERVICO).titulo(titulo)
                .recurso(RECURSO, a.getId().getStringValor()).enviar();
        return new Resultado(a, alertas(a));
    }

    @Transactional
    public Resultado qualificar(FuncionarioId funcionarioId, AcidenteServicoId id, boolean emServico, String despacho, String motivo) {
        var a = acidente(funcionarioId, id);
        a.qualificar(emServico, despacho, motivo);
        var gravado = repository.save(a);
        if (emServico) {
            factoDoAcidente(gravado, "QUALIFICADO", gravado.getDataHora().toLocalDate(), null);
            for (var i : gravado.getIncapacidades()) factoDaIncapacidade(gravado, i);
        }
        notificador.para(funcionarioId).tipo(TipoNotificacao.ACIDENTE_SERVICO)
                .titulo(emServico ? "O seu acidente foi qualificado como " + nome(gravado.getTipo())
                        : "O seu acidente não foi qualificado como acidente em serviço")
                .texto(gravado.getMotivo()).recurso(RECURSO, gravado.getId().getStringValor()).enviar();
        return new Resultado(gravado, alertas(gravado));
    }

    @Transactional
    public Resultado registarIncapacidade(FuncionarioId funcionarioId, AcidenteServicoId id, AcidenteServico.TipoIncapacidade tipo,
                                          LocalDate inicio, LocalDate fim) {
        var a = acidente(funcionarioId, id);
        var i = a.registarIncapacidade(tipo, inicio, fim);
        var gravado = repository.save(a);
        if (gravado.contaComoAcidente()) factoDaIncapacidade(gravado, i);
        return new Resultado(gravado, alertas(gravado));
    }

    @Transactional
    public Resultado darAlta(FuncionarioId funcionarioId, AcidenteServicoId id, LocalDate data) {
        var a = acidente(funcionarioId, id);
        a.darAlta(data);
        var gravado = repository.save(a);
        if (gravado.contaComoAcidente()) factoDoAcidente(gravado, "ALTA", data, null);
        return new Resultado(gravado, alertas(gravado));
    }

    /**
     * A incapacidade permanente (art. 189.º n.os 3 e 4): se é absoluta, ou parcial e não deixa exercer as funções, abre-se o
     * processo de aposentação por invalidez (pela Administração), se ainda não houver um.
     */
    @Transactional
    public Resultado registarIncapacidadePermanente(FuncionarioId funcionarioId, AcidenteServicoId id, BigDecimal percentagem, boolean absoluta,
                                                    boolean impedeFuncoes) {
        var a = acidente(funcionarioId, id);
        boolean aposenta = a.registarIncapacidadePermanente(percentagem, absoluta, impedeFuncoes);
        var gravado = repository.save(a);
        var dados = new LinkedHashMap<String, Object>();
        dados.put("incapacidadePermanente", percentagem);
        dados.put("absoluta", absoluta);
        factoDoAcidente(gravado, "INCAPACIDADE_PERMANENTE", hoje(), dados);
        var alertas = new ArrayList<>(alertas(gravado));
        if (aposenta) {
            try {
                aposentacaoService.abrir(funcionarioId, ModalidadeAposentacao.INVALIDEZ, ProcessoAposentacao.Iniciativa.ADMINISTRACAO, null,
                        "Incapacidade permanente " + (absoluta ? "absoluta" : "parcial de " + percentagem + " %") + " por " + nome(a.getTipo())
                                + " (art. 189.º da Lei n.º 20/X/2023)", true);
                alertas.add("Aberto o processo de aposentação por invalidez (art. 189.º n.º " + (absoluta ? "3" : "4") + ").");
            } catch (IgrpResponseStatusException e) {
                alertas.add("Não se abriu o processo de aposentação por invalidez: " + e.getMessage());
            }
        }
        return new Resultado(gravado, alertas);
    }

    @Transactional
    public Resultado registarSeguradora(FuncionarioId funcionarioId, AcidenteServicoId id, String seguradora, String apolice, LocalDate participacao) {
        var a = acidente(funcionarioId, id);
        a.registarSeguradora(seguradora, apolice, participacao != null ? participacao : hoje());
        return new Resultado(repository.save(a), List.of());
    }

    @Transactional
    public Resultado encerrar(FuncionarioId funcionarioId, AcidenteServicoId id) {
        var a = acidente(funcionarioId, id);
        a.encerrar();
        return new Resultado(repository.save(a), List.of());
    }

    @Transactional(readOnly = true)
    public List<AcidenteServico> listar(AcidenteServico.Estado estado) {
        return repository.find(estado);
    }

    @Transactional(readOnly = true)
    public List<AcidenteServico> doFuncionario(FuncionarioId funcionarioId) {
        return repository.findByFuncionario(funcionarioId);
    }

    /** O que o RH deve ver: participação fora do prazo [ind.], acidente por qualificar com incapacidade, sem seguradora. */
    public List<String> alertas(AcidenteServico a) {
        var l = new ArrayList<String>();
        LocalDate dia = a.getDataHora().toLocalDate();
        Set<LocalDate> feriados = calendario.feriadosDoColaborador(a.getFuncionarioId(), dia, dia.plusDays(15));
        LocalDate limite = dia;
        for (int n = 0; n < AcidenteServico.DIAS_UTEIS_PARTICIPACAO; ) {
            limite = limite.plusDays(1);
            if (limite.getDayOfWeek() != DayOfWeek.SATURDAY && limite.getDayOfWeek() != DayOfWeek.SUNDAY && !feriados.contains(limite)) n++;
        }
        if (a.getDataParticipacao().isAfter(limite))
            l.add("Participado a " + Datas.pt(a.getDataParticipacao()) + ", depois dos 2 dias úteis (até " + Datas.pt(limite) + ").");
        if (a.getEstado() == AcidenteServico.Estado.PARTICIPADO && !a.getIncapacidades().isEmpty())
            l.add("Por qualificar: as faltas da incapacidade só ficam justificadas como acidente em serviço depois da qualificação.");
        if (a.getEstado() == AcidenteServico.Estado.QUALIFICADO && a.getSeguradora() == null)
            l.add("Sem seguradora registada: se o serviço transferiu a responsabilidade, participe-lhe (art. 191.º).");
        return l;
    }

    public String nome(FuncionarioId id) {
        return funcionarioRepository.findById(id).map(Funcionario::getNomeCompleto).orElse("um colaborador");
    }

    static String nome(AcidenteServico.Tipo t) {
        return switch (t) {
            case ACIDENTE_SERVICO -> "acidente em serviço";
            case ACIDENTE_TRAJECTO -> "acidente de trajecto";
            case DOENCA_PROFISSIONAL -> "doença profissional";
        };
    }

    private void factoDaIncapacidade(AcidenteServico a, AcidenteServico.Incapacidade i) {
        var dados = new LinkedHashMap<String, Object>();
        dados.put("tipoIncapacidade", i.tipo().name());
        dados.put("inicio", i.inicio());
        dados.put("fim", i.fim());
        dados.put("remuneracaoMantida", true);
        factoDoAcidente(a, "INCAPACIDADE_TEMPORARIA", i.inicio(), dados);
    }

    private void factoDoAcidente(AcidenteServico a, String evento, LocalDate data, java.util.Map<String, Object> extra) {
        var dados = new LinkedHashMap<String, Object>();
        dados.put("evento", evento);
        dados.put("tipo", a.getTipo().name());
        if (extra != null) dados.putAll(extra);
        diarioFactos.registar(a.getFuncionarioId(), TipoFactoRh.ACIDENTE_SERVICO, data, RECURSO, a.getId().getStringValor(),
                nome(a.getTipo()) + " de " + Datas.pt(a.getDataHora().toLocalDate()) + " — " + evento.toLowerCase().replace('_', ' '), dados);
    }

    private AcidenteServico acidente(FuncionarioId funcionarioId, AcidenteServicoId id) {
        return repository.findById(id).filter(a -> a.getFuncionarioId().equals(funcionarioId))
                .orElseThrow(() -> IgrpResponseStatusException.notFound("Acidente não encontrado."));
    }

    private Funcionario funcionario(FuncionarioId id) {
        return funcionarioRepository.findById(id).orElseThrow(() -> IgrpResponseStatusException.notFound("Colaborador não encontrado."));
    }

    LocalDate hoje() { return LocalDate.now(); }
}
