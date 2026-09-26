package cv.igrp.RH_Service.colaboradores.application.services;

import cv.igrp.RH_Service.colaboradores.domain.models.ExameSaude;
import cv.igrp.RH_Service.colaboradores.domain.models.Funcionario;
import cv.igrp.RH_Service.colaboradores.domain.models.JuntaMedica;
import cv.igrp.RH_Service.colaboradores.domain.models.ModalidadeAposentacao;
import cv.igrp.RH_Service.colaboradores.domain.models.ProcessoAposentacao;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.SaudeTrabalhoRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.JuntaMedicaId;
import cv.igrp.RH_Service.shared.application.services.notificacoes.Notificador;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.domain.notificacoes.TipoNotificacao;
import cv.igrp.RH_Service.shared.domain.service.Datas;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * <b>Medicina do trabalho e junta médica</b> (BR-SST-11..18): os exames de aptidão (sem dados clínicos) com validade e
 * aviso, e a comissão de verificação de incapacidade com os efeitos do parecer.
 */
@Service
@RequiredArgsConstructor
public class SaudeTrabalhoService {

    static final String RECURSO_EXAME = "EXAME_SAUDE";
    static final String RECURSO_JUNTA = "JUNTA_MEDICA";
    /** Com que antecedência se avisa do fim da validade [ind.]. */
    static final int DIAS_AVISO = 30;

    public record Resultado<T>(T valor, List<String> alertas) {}

    private final SaudeTrabalhoRepository repository;
    private final FuncionarioRepository funcionarioRepository;
    private final AposentacaoService aposentacaoService;
    private final Notificador notificador;

    // ---------------------------------------------------------------- exames

    @Transactional
    public Resultado<ExameSaude> registarExame(FuncionarioId funcionarioId, ExameSaude.Tipo tipo, LocalDate data, String entidade,
                                               ExameSaude.Resultado resultado, String restricoes, LocalDate validade, String observacoes) {
        var f = funcionario(funcionarioId);
        Integer idade = f.getDataNascimento() != null && data != null ? Period.between(f.getDataNascimento(), data).getYears() : null;
        var e = repository.save(ExameSaude.registar(funcionarioId, tipo, data, entidade, resultado, restricoes, validade, observacoes, idade, hoje()));
        var alertas = new ArrayList<String>();
        if (e.getResultado() == ExameSaude.Resultado.INAPTO_DEFINITIVO)
            alertas.add("Inapto definitivo: peça a comissão de verificação de incapacidade (junta médica).");
        if (e.getResultado() == ExameSaude.Resultado.APTO_CONDICIONADO)
            alertas.add("Apto condicionado: a chefia deve adequar as tarefas às restrições.");
        if (!e.apto())
            notificador.paraRh().tipo(TipoNotificacao.EXAME_SAUDE)
                    .titulo(f.getNomeCompleto() + ": " + resultado(e.getResultado()) + " no exame de " + Datas.pt(e.getData()))
                    .recurso(RECURSO_EXAME, e.getId().getStringValor()).enviar();
        return new Resultado<>(e, alertas);
    }

    /** A aptidão de hoje: o último exame, se ainda vale. */
    @Transactional(readOnly = true)
    public Optional<ExameSaude> ultimoExame(FuncionarioId funcionarioId) {
        return repository.findExames(funcionarioId).stream().findFirst();
    }

    @Transactional(readOnly = true)
    public List<ExameSaude> exames(FuncionarioId funcionarioId) {
        return repository.findExames(funcionarioId);
    }

    /** Para o job: avisa 30 dias antes do fim da validade do último exame, e no dia a seguir a caducar (BR-SST-14). */
    @Transactional
    public int avisarValidades(LocalDate dia) {
        int n = 0;
        for (var e : repository.findUltimosComValidadeEm(dia.plusDays(DIAS_AVISO))) {
            aviso(e, "O exame de medicina do trabalho de " + nome(e.getFuncionarioId()) + " caduca a " + Datas.pt(e.getValidadeAte()));
            n++;
        }
        for (var e : repository.findUltimosComValidadeEm(dia.minusDays(1))) {
            aviso(e, "Caducou o exame de medicina do trabalho de " + nome(e.getFuncionarioId()) + ": marque novo exame");
            n++;
        }
        return n;
    }

    private void aviso(ExameSaude e, String titulo) {
        notificador.paraRh().tipo(TipoNotificacao.EXAME_SAUDE).titulo(titulo).recurso(RECURSO_EXAME, e.getId().getStringValor()).enviar();
        notificador.para(e.getFuncionarioId()).tipo(TipoNotificacao.EXAME_SAUDE).titulo("O seu exame de medicina do trabalho caduca a "
                + Datas.pt(e.getValidadeAte())).recurso(RECURSO_EXAME, e.getId().getStringValor()).enviar();
    }

    // ---------------------------------------------------------------- junta médica

    @Transactional
    public JuntaMedica pedirJunta(FuncionarioId funcionarioId, JuntaMedica.Motivo motivo, String fundamentacao, LocalDate data) {
        funcionario(funcionarioId);
        if (repository.findJuntas(funcionarioId).stream().anyMatch(j -> j.getEstado() == JuntaMedica.Estado.PEDIDA))
            throw IgrpResponseStatusException.conflict("Já há um pedido de junta médica em curso para este colaborador.");
        return repository.save(JuntaMedica.pedir(funcionarioId, motivo, fundamentacao, data != null ? data : hoje()));
    }

    /**
     * O parecer (BR-SST-17): incapaz permanente abre a aposentação por invalidez; incapaz temporário por mais de 30 dias passa
     * à situação de inactividade fora do quadro (Lei n.º 20/X/2023, art. 121.º) — o RH regista a mudança de estado; apto para
     * outras funções pede a reafectação.
     */
    @Transactional
    public Resultado<JuntaMedica> registarParecer(FuncionarioId funcionarioId, JuntaMedicaId id, LocalDate data, JuntaMedica.Parecer parecer,
                                                  Integer dias, String observacoes) {
        var j = junta(funcionarioId, id);
        j.registarParecer(data != null ? data : hoje(), parecer, dias, observacoes, hoje());
        var gravada = repository.save(j);
        var alertas = new ArrayList<String>();
        switch (parecer) {
            case INCAPAZ_PERMANENTE -> {
                try {
                    aposentacaoService.abrir(funcionarioId, ModalidadeAposentacao.INVALIDEZ, ProcessoAposentacao.Iniciativa.ADMINISTRACAO, null,
                            "Parecer de incapacidade permanente da comissão de verificação de " + Datas.pt(gravada.getDataJunta()), true);
                    alertas.add("Aberto o processo de aposentação por invalidez.");
                } catch (IgrpResponseStatusException e) {
                    alertas.add("Não se abriu o processo de aposentação por invalidez: " + e.getMessage());
                }
            }
            case INCAPAZ_TEMPORARIO -> {
                if (dias > 30)
                    alertas.add("Incapacidade por mais de 30 dias: passa à situação de inactividade fora do quadro (art. 121.º) — registe a mudança de estado.");
            }
            case APTO_OUTRAS_FUNCOES -> alertas.add("Apto para outras funções: estude a reafectação a funções compatíveis.");
            default -> { }
        }
        notificador.paraRh().tipo(TipoNotificacao.EXAME_SAUDE)
                .titulo("Parecer da junta médica de " + nome(funcionarioId) + ": " + parecer(parecer))
                .recurso(RECURSO_JUNTA, gravada.getId().getStringValor()).enviar();
        return new Resultado<>(gravada, alertas);
    }

    @Transactional
    public JuntaMedica cancelarJunta(FuncionarioId funcionarioId, JuntaMedicaId id) {
        var j = junta(funcionarioId, id);
        j.cancelar();
        return repository.save(j);
    }

    @Transactional(readOnly = true)
    public List<JuntaMedica> juntas(JuntaMedica.Estado estado) {
        return repository.findJuntas(estado);
    }

    @Transactional(readOnly = true)
    public List<JuntaMedica> juntasDe(FuncionarioId funcionarioId) {
        return repository.findJuntas(funcionarioId);
    }

    public String nome(FuncionarioId id) {
        return funcionarioRepository.findById(id).map(Funcionario::getNomeCompleto).orElse("um colaborador");
    }

    static String resultado(ExameSaude.Resultado r) {
        return switch (r) {
            case APTO -> "apto";
            case APTO_CONDICIONADO -> "apto condicionado";
            case INAPTO_TEMPORARIO -> "inapto temporário";
            case INAPTO_DEFINITIVO -> "inapto definitivo";
        };
    }

    static String parecer(JuntaMedica.Parecer p) {
        return switch (p) {
            case APTO -> "apto";
            case APTO_OUTRAS_FUNCOES -> "apto para outras funções";
            case INCAPAZ_TEMPORARIO -> "incapaz temporário";
            case INCAPAZ_PERMANENTE -> "incapaz permanente";
        };
    }

    private JuntaMedica junta(FuncionarioId funcionarioId, JuntaMedicaId id) {
        return repository.findJunta(id).filter(j -> j.getFuncionarioId().equals(funcionarioId))
                .orElseThrow(() -> IgrpResponseStatusException.notFound("Pedido de junta médica não encontrado."));
    }

    private Funcionario funcionario(FuncionarioId id) {
        return funcionarioRepository.findById(id).orElseThrow(() -> IgrpResponseStatusException.notFound("Colaborador não encontrado."));
    }

    LocalDate hoje() { return LocalDate.now(); }
}
