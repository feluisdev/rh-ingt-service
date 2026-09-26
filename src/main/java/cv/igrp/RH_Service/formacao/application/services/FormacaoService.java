package cv.igrp.RH_Service.formacao.application.services;

import cv.igrp.RH_Service.colaboradores.application.services.ChefiaService;
import cv.igrp.RH_Service.colaboradores.domain.models.Formacao;
import cv.igrp.RH_Service.colaboradores.domain.models.Funcionario;
import cv.igrp.RH_Service.colaboradores.domain.repository.FormacaoRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.formacao.domain.models.AccaoFormacao;
import cv.igrp.RH_Service.formacao.domain.models.PlanoFormacao;
import cv.igrp.RH_Service.formacao.domain.repository.FormacaoRepositorio;
import cv.igrp.RH_Service.formacao.domain.valueobject.AccaoFormacaoId;
import cv.igrp.RH_Service.formacao.domain.valueobject.InscricaoFormacaoId;
import cv.igrp.RH_Service.formacao.domain.valueobject.NecessidadeFormacaoId;
import cv.igrp.RH_Service.formacao.domain.valueobject.PlanoFormacaoId;
import cv.igrp.RH_Service.shared.application.services.notificacoes.Notificador;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.domain.notificacoes.TipoNotificacao;
import cv.igrp.RH_Service.shared.domain.service.Datas;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * <b>A formação como processo</b> (Lei n.º 20/X/2023, art. 141.º; BR-FRM-01..16): o plano anual com as necessidades, as
 * acções com as inscrições, a avaliação e o registo no histórico do colaborador. A chefia directa e o próprio actuam em
 * {@code /me}; o RH em todo o lado.
 */
@Service
@RequiredArgsConstructor
public class FormacaoService {

    static final String RECURSO = "ACCAO_FORMACAO";
    /** O tipo com que a acção concluída entra no histórico (t_training). */
    public static final String TIPO_HISTORICO = "ACCAO_FORMACAO";

    /** Os termos de uma acção. */
    public record Dados(String tema, String entidade, AccaoFormacao.Modalidade modalidade, Boolean interna, LocalDate inicio, LocalDate fim,
                        Integer horas, String horario, String local, Integer vagas, BigDecimal custo, Boolean custeada, Integer mesesGarantia) {}

    private final FormacaoRepositorio repository;
    private final FormacaoRepository historico;
    private final FuncionarioRepository funcionarioRepository;
    private final ChefiaService chefiaService;
    private final Notificador notificador;

    // ---------------------------------------------------------------- plano

    @Transactional
    public PlanoFormacao criarPlano(int ano, UUID unidadeId, String designacao) {
        return repository.save(PlanoFormacao.criar(ano, unidadeId, designacao, hoje().getYear()));
    }

    @Transactional
    public PlanoFormacao identificar(PlanoFormacaoId id, String tema, FuncionarioId funcionarioId, PlanoFormacao.Prioridade prioridade,
                                     String justificacao) {
        var p = plano(id);
        if (funcionarioId != null) funcionario(funcionarioId);
        p.identificar(tema, funcionarioId, AccaoFormacao.Origem.RH, prioridade, justificacao);
        return repository.save(p);
    }

    /** O próprio identifica uma necessidade sua; a chefia, uma de alguém da sua equipa directa (403 a outros). */
    @Transactional
    public PlanoFormacao identificarComo(FuncionarioId eu, PlanoFormacaoId id, String tema, FuncionarioId para, PlanoFormacao.Prioridade prioridade,
                                         String justificacao) {
        var p = plano(id);
        FuncionarioId alvo = para != null ? para : eu;
        AccaoFormacao.Origem origem;
        if (alvo.equals(eu)) origem = AccaoFormacao.Origem.PROPRIO;
        else if (chefiaService.eChefeDirecto(eu, alvo)) origem = AccaoFormacao.Origem.CHEFIA;
        else throw proibido("Só identifica necessidades suas ou da sua equipa directa.");
        p.identificar(tema, alvo, origem, prioridade, justificacao);
        return repository.save(p);
    }

    @Transactional
    public PlanoFormacao aprovarPlano(PlanoFormacaoId id, String despacho, LocalDate data) {
        var p = plano(id);
        p.aprovar(despacho, data != null ? data : hoje());
        return repository.save(p);
    }

    // ---------------------------------------------------------------- acções

    /** Uma acção nova; pode responder a necessidades de um plano aprovado. */
    @Transactional
    public AccaoFormacao planear(Dados d, PlanoFormacaoId planoId, List<NecessidadeFormacaoId> necessidades) {
        PlanoFormacao plano = null;
        if (planoId != null) {
            plano = plano(planoId);
            if (plano.getEstado() != PlanoFormacao.Estado.APROVADO)
                throw invalido("O plano ainda não foi aprovado: as acções respondem a um plano aprovado.");
        } else if (necessidades != null && !necessidades.isEmpty()) {
            throw invalido("Indique o plano das necessidades a que a acção responde.");
        }
        var a = AccaoFormacao.planear(d.tema(), d.entidade(), d.modalidade(), Boolean.TRUE.equals(d.interna()), d.inicio(), d.fim(), d.horas(),
                d.horario(), d.local(), d.vagas(), d.custo(), Boolean.TRUE.equals(d.custeada()), d.mesesGarantia(), planoId, necessidades);
        if (plano != null && necessidades != null && !necessidades.isEmpty()) {
            plano.planear(necessidades, a.getId());
            repository.save(plano);
        }
        return repository.save(a);
    }

    @Transactional
    public AccaoFormacao actualizar(AccaoFormacaoId id, Dados d) {
        var a = accao(id);
        a.definir(d.tema() != null ? d.tema() : a.getTema(), d.entidade() != null ? d.entidade() : a.getEntidadeFormadora(),
                d.modalidade() != null ? d.modalidade() : a.getModalidade(), d.interna() != null ? d.interna() : a.isInterna(), d.inicio() != null ? d.inicio() : a.getInicio(),
                d.fim() != null ? d.fim() : a.getFim(), d.horas() != null ? d.horas() : a.getHoras(),
                d.horario() != null ? d.horario() : a.getHorario(), d.local() != null ? d.local() : a.getLocal(),
                d.vagas() != null ? d.vagas() : a.getVagas(), d.custo() != null ? d.custo() : a.getCustoPrevisto(),
                d.custeada() != null ? d.custeada() : a.isCusteadaPelaAdministracao(),
                d.mesesGarantia() != null ? d.mesesGarantia() : a.getMesesGarantia());
        return repository.save(a);
    }

    @Transactional
    public AccaoFormacao abrirInscricoes(AccaoFormacaoId id) {
        var a = accao(id);
        a.abrirInscricoes();
        return repository.save(a);
    }

    @Transactional
    public AccaoFormacao iniciar(AccaoFormacaoId id) {
        var a = accao(id);
        a.iniciar();
        return repository.save(a);
    }

    /**
     * Concluir (BR-FRM-10): cada aproveitamento entra no histórico de formação do colaborador; as necessidades do plano ficam
     * satisfeitas; cada formando é avisado do resultado (e do prazo de garantia, se o há).
     */
    @Transactional
    public AccaoFormacao concluir(AccaoFormacaoId id) {
        var a = accao(id);
        a.concluir();
        for (var i : a.getInscricoes()) {
            if (i.getEstado() == AccaoFormacao.EstadoInscricao.APROVEITAMENTO)
                historico.save(Formacao.criar(i.getFuncionarioId(), a.getTema(), a.getEntidadeFormadora(), TIPO_HISTORICO, a.getInicio(), a.getFim(),
                        a.getHoras()));
            if (i.admitida())
                notificador.para(i.getFuncionarioId()).tipo(TipoNotificacao.FORMACAO)
                        .titulo("Formação «" + a.getTema() + "» concluída: " + resultado(i.getEstado()))
                        .texto(i.getGarantiaAte() != null ? "A Administração custeou esta formação: fica obrigado a permanecer até "
                                + Datas.pt(i.getGarantiaAte()) + " (art. 95.º b) da Lei n.º 20/X/2023)." : null)
                        .recurso(RECURSO, a.getId().getStringValor()).enviar();
        }
        if (a.getPlanoId() != null) repository.findPlano(a.getPlanoId()).ifPresent(p -> {
            p.satisfazer(a.getId());
            repository.save(p);
        });
        return repository.save(a);
    }

    @Transactional
    public AccaoFormacao cancelar(AccaoFormacaoId id, String motivo) {
        var a = accao(id);
        a.cancelar(motivo);
        for (var i : a.getInscricoes())
            if (i.admitida() || i.getEstado() == AccaoFormacao.EstadoInscricao.PEDIDA)
                notificador.para(i.getFuncionarioId()).tipo(TipoNotificacao.FORMACAO)
                        .titulo("A formação «" + a.getTema() + "» foi cancelada").texto(a.getMotivoCancelamento())
                        .recurso(RECURSO, a.getId().getStringValor()).enviar();
        return repository.save(a);
    }

    // ---------------------------------------------------------------- inscrições

    @Transactional
    public AccaoFormacao inscrever(AccaoFormacaoId id, FuncionarioId funcionarioId) {
        var a = accao(id);
        exigirAoServico(funcionarioId);
        a.inscrever(funcionarioId, AccaoFormacao.Origem.RH, hoje());
        avisarAdmitido(a, funcionarioId);
        return repository.save(a);
    }

    /**
     * Em {@code /me}: o próprio pede (fica a aguardar a decisão; a chefia directa e o RH são avisados); a chefia inscreve
     * alguém da sua equipa directa (fica admitido). Outros: 403.
     */
    @Transactional
    public AccaoFormacao inscreverComo(FuncionarioId eu, AccaoFormacaoId id, FuncionarioId para) {
        var a = accao(id);
        FuncionarioId alvo = para != null ? para : eu;
        exigirAoServico(alvo);
        if (alvo.equals(eu)) {
            a.inscrever(eu, AccaoFormacao.Origem.PROPRIO, hoje());
            String titulo = nome(eu) + " pede inscrição na formação «" + a.getTema() + "» (" + a.periodo() + ")";
            notificador.para(chefiaService.chefeDirecto(eu)).tipo(TipoNotificacao.FORMACAO).titulo(titulo).recurso(RECURSO, a.getId().getStringValor()).enviar();
            notificador.paraRh().tipo(TipoNotificacao.FORMACAO).titulo(titulo).recurso(RECURSO, a.getId().getStringValor()).enviar();
        } else if (chefiaService.eChefeDirecto(eu, alvo)) {
            a.inscrever(alvo, AccaoFormacao.Origem.CHEFIA, hoje());
            avisarAdmitido(a, alvo);
        } else {
            throw proibido("Só inscreve pessoas da sua equipa directa.");
        }
        return repository.save(a);
    }

    @Transactional
    public AccaoFormacao decidir(AccaoFormacaoId id, InscricaoFormacaoId iid, boolean admitir, String motivo) {
        var a = accao(id);
        var i = a.decidir(iid, admitir, motivo);
        avisarDecisao(a, i);
        return repository.save(a);
    }

    /** A chefia directa decide os pedidos da sua equipa (403 a outros). */
    @Transactional
    public AccaoFormacao decidirComo(FuncionarioId eu, AccaoFormacaoId id, InscricaoFormacaoId iid, boolean admitir, String motivo) {
        var a = accao(id);
        if (!chefiaService.eChefeDirecto(eu, a.inscricao(iid).getFuncionarioId())) throw proibido("Só decide os pedidos da sua equipa directa.");
        var i = a.decidir(iid, admitir, motivo);
        avisarDecisao(a, i);
        return repository.save(a);
    }

    @Transactional
    public AccaoFormacao desistir(AccaoFormacaoId id, InscricaoFormacaoId iid, String motivo) {
        var a = accao(id);
        a.desistir(iid, motivo);
        return repository.save(a);
    }

    @Transactional
    public AccaoFormacao desistirComo(FuncionarioId eu, AccaoFormacaoId id, String motivo) {
        var a = accao(id);
        var i = a.inscricaoDe(eu).orElseThrow(() -> IgrpResponseStatusException.notFound("Não está inscrito nesta acção."));
        a.desistir(i.getId(), motivo);
        notificador.paraRh().tipo(TipoNotificacao.FORMACAO).titulo(nome(eu) + " desistiu da formação «" + a.getTema() + "»")
                .recurso(RECURSO, a.getId().getStringValor()).enviar();
        return repository.save(a);
    }

    @Transactional
    public AccaoFormacao avaliar(AccaoFormacaoId id, InscricaoFormacaoId iid, AccaoFormacao.EstadoInscricao resultado, Integer diasPresenca) {
        var a = accao(id);
        a.avaliar(iid, resultado, diasPresenca);
        return repository.save(a);
    }

    // ---------------------------------------------------------------- leitura

    @Transactional(readOnly = true)
    public AccaoFormacao accao(AccaoFormacaoId id) {
        return repository.findById(id).orElseThrow(() -> IgrpResponseStatusException.notFound("Acção de formação não encontrada."));
    }

    @Transactional(readOnly = true)
    public List<AccaoFormacao> accoes(AccaoFormacao.Estado estado, Integer ano) {
        return repository.find(estado, ano);
    }

    /** Em {@code /me}: as acções com inscrições abertas e as minhas, sem repetir. */
    @Transactional(readOnly = true)
    public List<AccaoFormacao> paraMim(FuncionarioId eu) {
        var l = new java.util.ArrayList<>(repository.findDoFuncionario(eu));
        repository.find(AccaoFormacao.Estado.INSCRICOES_ABERTAS, null).stream()
                .filter(a -> l.stream().noneMatch(x -> x.getId().equals(a.getId()))).forEach(l::add);
        return l;
    }

    @Transactional(readOnly = true)
    public PlanoFormacao plano(PlanoFormacaoId id) {
        return repository.findPlano(id).orElseThrow(() -> IgrpResponseStatusException.notFound("Plano de formação não encontrado."));
    }

    @Transactional(readOnly = true)
    public List<PlanoFormacao> planos(Integer ano) {
        return repository.findPlanos(ano);
    }

    /** As permanências que o colaborador ainda deve (art. 95.º b)) — para a exoneração voluntária. */
    @Transactional(readOnly = true)
    public List<AccaoFormacao> garantiasEmCurso(FuncionarioId funcionarioId, LocalDate em) {
        return repository.findComGarantiaEm(funcionarioId, em);
    }

    /** As horas de formação de cada colaborador no ano, pelo histórico (indicador do balanço social; BR-FRM-13). */
    @Transactional(readOnly = true)
    public java.util.Map<FuncionarioId, Integer> horasNoAno(int ano) {
        return historico.horasPorFuncionario(ano);
    }

    public String nome(FuncionarioId id) {
        return funcionarioRepository.findById(id).map(Funcionario::getNomeCompleto).orElse("um colaborador");
    }

    private void avisarAdmitido(AccaoFormacao a, FuncionarioId f) {
        notificador.para(f).tipo(TipoNotificacao.FORMACAO)
                .titulo("Foi inscrito na formação «" + a.getTema() + "» (" + a.periodo() + ")")
                .recurso(RECURSO, a.getId().getStringValor()).enviar();
    }

    private void avisarDecisao(AccaoFormacao a, AccaoFormacao.Inscricao i) {
        notificador.para(i.getFuncionarioId()).tipo(TipoNotificacao.FORMACAO)
                .titulo(i.admitida() ? "Foi admitido na formação «" + a.getTema() + "» (" + a.periodo() + ")"
                        : "O seu pedido de inscrição na formação «" + a.getTema() + "» foi recusado")
                .texto(i.getMotivo()).recurso(RECURSO, a.getId().getStringValor()).enviar();
    }

    private void exigirAoServico(FuncionarioId id) {
        if (!Boolean.TRUE.equals(funcionario(id).getIsActive())) throw invalido("Este colaborador já não está ao serviço.");
    }

    private Funcionario funcionario(FuncionarioId id) {
        return funcionarioRepository.findById(id).orElseThrow(() -> IgrpResponseStatusException.notFound("Colaborador não encontrado."));
    }

    static String resultado(AccaoFormacao.EstadoInscricao e) {
        return switch (e) {
            case APROVEITAMENTO -> "com aproveitamento";
            case SEM_APROVEITAMENTO -> "sem aproveitamento";
            case FALTOU -> "faltou";
            default -> e.name();
        };
    }

    LocalDate hoje() { return LocalDate.now(); }

    private static IgrpResponseStatusException invalido(String m) {
        return IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY, m);
    }

    private static IgrpResponseStatusException proibido(String m) {
        return IgrpResponseStatusException.of(HttpStatus.FORBIDDEN, m);
    }
}
