package cv.igrp.RH_Service.colaboradores.application.services;

import cv.igrp.RH_Service.colaboradores.domain.models.Funcionario;
import cv.igrp.RH_Service.colaboradores.domain.models.LicencaMobilidade;
import cv.igrp.RH_Service.colaboradores.domain.models.TipoFactoRh;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.LicencaMobilidadeRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.LicencaMobilidadeId;
import cv.igrp.RH_Service.shared.application.services.notificacoes.Notificador;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.domain.notificacoes.TipoNotificacao;
import cv.igrp.RH_Service.shared.domain.service.Datas;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * <b>A comissão de serviço</b> (Lei n.º 20/X/2023, arts. 59.º, 60.º e 64.º; BR-CMS-01..08) — dos dirigentes e dos
 * cargos de livre escolha. É um registo de mobilidade do subtipo que «regressa ou cessa» (o Lugar de origem mantém-se;
 * o regresso segue o art. 64.º n.º 2, já tratado no regresso das mobilidades). Aqui fica o que é próprio da comissão:
 * a <b>renovação por iguais períodos</b> de 3 anos, a <b>cessação a todo o tempo com aviso prévio de 60 dias</b>, os
 * factos (início, renovação, fim — daí a publicação) e o aviso antes do termo.
 */
@Service
@RequiredArgsConstructor
public class ComissaoServicoService {

    static final String RECURSO = "LICENCA_MOBILIDADE";
    /** Art. 60.º n.º 1: na falta de lei especial, 3 anos, renováveis por iguais períodos. */
    public static final int ANOS = 3;
    /** Art. 64.º n.º 1: aviso prévio de 60 dias. */
    public static final int DIAS_AVISO_PREVIO = 60;
    /** Com que antecedência se avisa do termo, para renovar ou não [ind.]. */
    public static final int DIAS_AVISO_TERMO = 90;

    /** Quem faz cessar: a entidade ou o nomeado (com aviso prévio), ou a pena disciplinar (Estatuto Disciplinar, sem aviso). */
    public enum Iniciativa { ENTIDADE, NOMEADO, PENA_DISCIPLINAR }

    private final LicencaMobilidadeRepository repository;
    private final MobilidadeService mobilidadeService;
    private final LicencaEfeitoService licencaEfeitoService;
    private final FuncionarioRepository funcionarioRepository;
    private final DiarioFactos diarioFactos;
    private final Notificador notificador;

    /** Renova por mais 3 anos a partir do fim actual, antes de terminar (BR-CMS-03). */
    @Transactional
    public LicencaMobilidade renovar(FuncionarioId funcionarioId, LicencaMobilidadeId id, String despacho) {
        var c = comissao(funcionarioId, id);
        if (c.getDataFim() == null) throw invalido("Esta comissão não tem termo: não há o que renovar.");
        if (hoje().isAfter(c.getDataFim()))
            throw IgrpResponseStatusException.conflict("A comissão terminou a " + Datas.pt(c.getDataFim()) + ": já não se renova.");
        LocalDate fimAnterior = c.getDataFim();
        c.prorrogar(fimAnterior.plusYears(ANOS), null);
        var gravada = repository.save(c);
        var dados = new LinkedHashMap<String, Object>();
        dados.put("evento", "RENOVACAO");
        dados.put("ate", gravada.getDataFim());
        dados.put("despacho", despacho);
        diarioFactos.registar(funcionarioId, TipoFactoRh.COMISSAO_SERVICO, fimAnterior.plusDays(1), RECURSO, id.getStringValor(),
                "Renovação da comissão de serviço até " + Datas.pt(gravada.getDataFim()), dados);
        notificador.para(funcionarioId).tipo(TipoNotificacao.COMISSAO_SERVICO)
                .titulo("A sua comissão de serviço foi renovada até " + Datas.pt(gravada.getDataFim()))
                .recurso(RECURSO, id.getStringValor()).enviar();
        return gravada;
    }

    /**
     * Cessa a comissão (BR-CMS-04..06): pela entidade ou pelo nomeado, com aviso prévio de 60 dias — a data de efeito
     * não pode ser antes de passarem 60 dias sobre o aviso; pela pena disciplinar, sem aviso. Se a data de efeito já
     * chegou, o regresso (ou a cessação da relação) aplica-se já.
     */
    @Transactional
    public LicencaMobilidade cessar(FuncionarioId funcionarioId, LicencaMobilidadeId id, Iniciativa iniciativa, LocalDate dataAviso,
                                    LocalDate dataEfeito, String motivo) {
        if (iniciativa == null) throw invalido("Diga quem faz cessar a comissão: a entidade, o nomeado, ou a pena disciplinar.");
        var c = comissao(funcionarioId, id);
        LocalDate aviso = dataAviso != null ? dataAviso : hoje();
        if (aviso.isAfter(hoje())) throw invalido("A data do aviso não pode ser no futuro.");
        LocalDate minima = iniciativa == Iniciativa.PENA_DISCIPLINAR ? aviso : aviso.plusDays(DIAS_AVISO_PREVIO);
        LocalDate efeito = dataEfeito != null ? dataEfeito : minima;
        if (efeito.isBefore(minima))
            throw invalido("A comissão cessa com aviso prévio de " + DIAS_AVISO_PREVIO + " dias: com o aviso a " + Datas.pt(aviso)
                    + ", produz efeitos a partir de " + Datas.pt(minima) + ".");
        String nota = "Cessação da comissão por " + nomeIniciativa(iniciativa) + ", aviso a " + Datas.pt(aviso) + ", efeitos a "
                + Datas.pt(efeito) + (motivo != null && !motivo.isBlank() ? ": " + motivo.trim() : ".");
        c.fixarFimDaComissao(efeito, hoje(), nota);
        var gravada = repository.save(c);
        if (!efeito.isAfter(hoje())) licencaEfeitoService.aplicarRegresso(gravada);
        notificador.paraRh().tipo(TipoNotificacao.COMISSAO_SERVICO)
                .titulo("Cessação da comissão de serviço de " + nome(funcionarioId) + " a " + Datas.pt(efeito))
                .texto(nota).recurso(RECURSO, id.getStringValor()).enviar();
        if (iniciativa != Iniciativa.NOMEADO)
            notificador.para(funcionarioId).tipo(TipoNotificacao.COMISSAO_SERVICO)
                    .titulo("A sua comissão de serviço cessa a " + Datas.pt(efeito)).texto(nota)
                    .recurso(RECURSO, id.getStringValor()).enviar();
        return gravada;
    }

    /** As comissões em curso (as que terminam até uma data, se indicada), pelo fim. */
    @Transactional(readOnly = true)
    public List<LicencaMobilidade> emCurso(LocalDate terminaAte) {
        return repository.findComissoesEmCurso(terminaAte);
    }

    /** O aviso do termo, 90 dias antes (BR-CMS-07): o RH e o nomeado — renovar, ou deixar terminar. */
    @Transactional
    public int avisarTermos(LocalDate dia) {
        LocalDate alvo = dia.plusDays(DIAS_AVISO_TERMO);
        int avisos = 0;
        for (var c : repository.findComissoesEmCurso(alvo)) {
            if (!alvo.equals(c.getDataFim())) continue;
            notificador.paraRh().tipo(TipoNotificacao.COMISSAO_SERVICO)
                    .titulo("A comissão de serviço de " + nome(c.getFuncionarioId()) + " termina a " + Datas.pt(c.getDataFim()))
                    .texto("Renove-a por mais " + ANOS + " anos ou deixe-a terminar.")
                    .recurso(RECURSO, c.getId().getStringValor()).enviar();
            notificador.para(c.getFuncionarioId()).tipo(TipoNotificacao.COMISSAO_SERVICO)
                    .titulo("A sua comissão de serviço termina a " + Datas.pt(c.getDataFim()))
                    .recurso(RECURSO, c.getId().getStringValor()).enviar();
            avisos++;
        }
        return avisos;
    }

    public String nome(FuncionarioId id) {
        return funcionarioRepository.findById(id).map(Funcionario::getNomeCompleto).orElse("um colaborador");
    }

    private LicencaMobilidade comissao(FuncionarioId funcionarioId, LicencaMobilidadeId id) {
        var c = repository.findById(id).filter(l -> l.getFuncionarioId().equals(funcionarioId))
                .orElseThrow(() -> IgrpResponseStatusException.notFound("Comissão de serviço não encontrada."));
        if (!mobilidadeService.subtipoDe(c).regressaOuCessa())
            throw invalido("Este registo não é uma comissão de serviço.");
        if (!c.isApproved()) throw IgrpResponseStatusException.conflict("Esta comissão ainda não foi deferida.");
        return c;
    }

    static String nomeIniciativa(Iniciativa i) {
        return switch (i) {
            case ENTIDADE -> "iniciativa da entidade";
            case NOMEADO -> "iniciativa do nomeado";
            case PENA_DISCIPLINAR -> "efeito de pena disciplinar";
        };
    }

    LocalDate hoje() { return LocalDate.now(); }

    private static IgrpResponseStatusException invalido(String m) {
        return IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY, m);
    }
}
