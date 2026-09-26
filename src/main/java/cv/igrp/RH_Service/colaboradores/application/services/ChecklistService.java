package cv.igrp.RH_Service.colaboradores.application.services;

import cv.igrp.RH_Service.colaboradores.domain.models.Checklist;
import cv.igrp.RH_Service.colaboradores.domain.models.Funcionario;
import cv.igrp.RH_Service.colaboradores.domain.models.ItemChecklistModelo;
import cv.igrp.RH_Service.colaboradores.domain.models.ResponsavelChecklist;
import cv.igrp.RH_Service.colaboradores.domain.models.TipoChecklist;
import cv.igrp.RH_Service.colaboradores.domain.models.TipoFactoRh;
import cv.igrp.RH_Service.colaboradores.domain.repository.ChecklistRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.ChecklistId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.ItemChecklistId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.ItemChecklistModeloId;
import cv.igrp.RH_Service.shared.application.services.notificacoes.Notificador;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.domain.notificacoes.TipoNotificacao;
import cv.igrp.RH_Service.shared.domain.service.Datas;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * <b>Checklists de entrada e de saída</b> (BR-CHK-01..12). A de entrada abre-se com a admissão ou o reingresso; a de
 * saída, com a cessação — a partir do diário de factos, sem ninguém se lembrar. Cada área marca os seus itens; a chefia
 * e o próprio marcam os deles em {@code /me/checklists}. Qualquer serviço pode cumprir um item pelo código com
 * {@link #cumprir(FuncionarioId, TipoChecklist, String, String, LocalDate)} — não faz nada se não houver checklist ou item.
 */
@Service
@RequiredArgsConstructor
public class ChecklistService {

    static final String RECURSO = "CHECKLIST";

    /** Os códigos que o sistema marca sozinho. */
    public static final String PROVIMENTO = "PROVIMENTO";
    public static final String CARTAO_PROFISSIONAL = "CARTAO_PROFISSIONAL";
    public static final String DEVOLUCAO_CARTAO = "DEVOLUCAO_CARTAO";

    private final ChecklistRepository repository;
    private final FuncionarioRepository funcionarioRepository;
    private final ChefiaService chefiaService;
    private final Notificador notificador;

    // ---------------------------------------------------------------- abrir

    /** A admissão e o reingresso abrem a de entrada; a cessação, a de saída (BR-CHK-04). Não duplica. */
    @EventListener
    public void aoRegistarFacto(DiarioFactos.FactoRegistado evento) {
        var f = evento.facto();
        TipoChecklist tipo = f.getTipo() == TipoFactoRh.ADMISSAO || f.getTipo() == TipoFactoRh.REINGRESSO ? TipoChecklist.ENTRADA
                : f.getTipo() == TipoFactoRh.CESSACAO ? TipoChecklist.SAIDA : null;
        if (tipo == null || aberta(f.getFuncionarioId(), tipo).isPresent()) return;
        criar(f.getFuncionarioId(), tipo, f.getDataEfeito());
    }

    /** À mão (quem entrou antes das checklists, ou uma saída que não passou pela cessação). */
    @Transactional
    public Checklist abrir(FuncionarioId funcionarioId, TipoChecklist tipo, LocalDate dataReferencia) {
        funcionario(funcionarioId);
        if (tipo == null) throw invalido("Diga se é a checklist de entrada ou de saída.");
        if (aberta(funcionarioId, tipo).isPresent())
            throw IgrpResponseStatusException.conflict("Este colaborador já tem uma checklist de " + nome(tipo) + " em curso.");
        return criar(funcionarioId, tipo, dataReferencia);
    }

    private Checklist criar(FuncionarioId funcionarioId, TipoChecklist tipo, LocalDate dataReferencia) {
        var c = repository.save(Checklist.abrir(funcionarioId, tipo, dataReferencia, repository.findModelos(tipo), hoje()));
        String quem = nomeDe(funcionarioId);
        notificador.paraRh().tipo(TipoNotificacao.CHECKLIST)
                .titulo("Checklist de " + nome(tipo) + " aberta: " + quem)
                .texto(c.pendentes() + " item(ns) a tratar.").recurso(RECURSO, c.getId().getStringValor()).enviar();
        if (temPendentes(c, ResponsavelChecklist.PROPRIO))
            notificador.para(funcionarioId).tipo(TipoNotificacao.CHECKLIST)
                    .titulo("Tem itens a tratar na sua " + (tipo == TipoChecklist.ENTRADA ? "entrada" : "saída"))
                    .recurso(RECURSO, c.getId().getStringValor()).enviar();
        if (temPendentes(c, ResponsavelChecklist.CHEFIA))
            notificador.para(chefiaService.chefeDirecto(funcionarioId)).tipo(TipoNotificacao.CHECKLIST)
                    .titulo("Checklist de " + nome(tipo) + " de " + quem + ": tem itens a tratar")
                    .recurso(RECURSO, c.getId().getStringValor()).enviar();
        return c;
    }

    // ---------------------------------------------------------------- marcar

    @Transactional
    public Checklist marcar(FuncionarioId funcionarioId, ChecklistId id, ItemChecklistId itemId, Checklist.EstadoItem estado,
                            String observacao, LocalDate data) {
        var c = checklist(funcionarioId, id);
        c.marcar(itemId, estado, observacao, data, hoje());
        return concluidaAvisa(c);
    }

    /**
     * O próprio marca os seus itens; a chefia directa, os da chefia (BR-CHK-08). Os outros não (403).
     */
    @Transactional
    public Checklist marcarComo(FuncionarioId quem, ChecklistId id, ItemChecklistId itemId, Checklist.EstadoItem estado,
                                String observacao, LocalDate data) {
        var c = repository.findById(id).orElseThrow(() -> IgrpResponseStatusException.notFound("Checklist não encontrada."));
        var item = c.getItens().stream().filter(i -> i.getId().equals(itemId)).findFirst()
                .orElseThrow(() -> IgrpResponseStatusException.notFound("Item não encontrado nesta checklist."));
        boolean pode = item.getResponsavel() == ResponsavelChecklist.PROPRIO ? c.getFuncionarioId().equals(quem)
                : item.getResponsavel() == ResponsavelChecklist.CHEFIA && chefiaService.eChefeDirecto(quem, c.getFuncionarioId());
        if (!pode) throw IgrpResponseStatusException.of(HttpStatus.FORBIDDEN, "Este item não é seu para marcar.");
        c.marcar(itemId, estado, observacao, data, hoje());
        return concluidaAvisa(c);
    }

    /** Para qualquer serviço: cumpre o item com este código, se houver checklist corrente com ele pendente. */
    @Transactional
    public Optional<Checklist> cumprir(FuncionarioId funcionarioId, TipoChecklist tipo, String codigo, String observacao, LocalDate data) {
        var c = repository.findCorrente(funcionarioId, tipo).orElse(null);
        if (c == null || c.cumprirPorCodigo(codigo, observacao, data != null ? data : hoje()).isEmpty()) return Optional.empty();
        return Optional.of(concluidaAvisa(c));
    }

    @Transactional
    public Checklist acrescentar(FuncionarioId funcionarioId, ChecklistId id, String descricao, ResponsavelChecklist responsavel,
                                 boolean obrigatorio, LocalDate prazo) {
        var c = checklist(funcionarioId, id);
        c.acrescentar(descricao, responsavel, obrigatorio, prazo, hoje());
        return repository.save(c);
    }

    @Transactional
    public Checklist cancelar(FuncionarioId funcionarioId, ChecklistId id, String motivo) {
        var c = checklist(funcionarioId, id);
        c.cancelar(motivo);
        return repository.save(c);
    }

    private Checklist concluidaAvisa(Checklist c) {
        boolean jaEstava = repository.findById(c.getId()).map(x -> x.getEstado() == Checklist.Estado.CONCLUIDA).orElse(false);
        var gravada = repository.save(c);
        if (c.getEstado() == Checklist.Estado.CONCLUIDA && !jaEstava)
            notificador.paraRh().tipo(TipoNotificacao.CHECKLIST)
                    .titulo("Checklist de " + nome(c.getTipo()) + " concluída: " + nomeDe(c.getFuncionarioId()))
                    .recurso(RECURSO, c.getId().getStringValor()).enviar();
        return gravada;
    }

    // ---------------------------------------------------------------- avisos (job)

    /** Os itens cujo prazo terminou ontem: o RH, e o próprio ou a chefia se forem deles (BR-CHK-10). */
    @Transactional
    public int avisarPrazos(LocalDate dia) {
        LocalDate ontem = dia.minusDays(1);
        int avisos = 0;
        for (var c : repository.findComPrazoEm(ontem)) {
            var itens = c.getItens().stream().filter(i -> i.pendente() && ontem.equals(i.getPrazo())).toList();
            String quem = nomeDe(c.getFuncionarioId());
            notificador.paraRh().tipo(TipoNotificacao.CHECKLIST)
                    .titulo("Checklist de " + nome(c.getTipo()) + " de " + quem + ": " + itens.size() + " item(ns) fora do prazo")
                    .texto(String.join("; ", itens.stream().map(Checklist.Item::getDescricao).toList()))
                    .recurso(RECURSO, c.getId().getStringValor()).enviar();
            if (itens.stream().anyMatch(i -> i.getResponsavel() == ResponsavelChecklist.PROPRIO))
                notificador.para(c.getFuncionarioId()).tipo(TipoNotificacao.CHECKLIST)
                        .titulo("Tem itens da sua " + (c.getTipo() == TipoChecklist.ENTRADA ? "entrada" : "saída") + " fora do prazo")
                        .recurso(RECURSO, c.getId().getStringValor()).enviar();
            if (itens.stream().anyMatch(i -> i.getResponsavel() == ResponsavelChecklist.CHEFIA))
                notificador.para(chefiaService.chefeDirecto(c.getFuncionarioId())).tipo(TipoNotificacao.CHECKLIST)
                        .titulo("Checklist de " + nome(c.getTipo()) + " de " + quem + ": itens seus fora do prazo")
                        .recurso(RECURSO, c.getId().getStringValor()).enviar();
            avisos++;
        }
        return avisos;
    }

    // ---------------------------------------------------------------- modelo

    @Transactional
    public ItemChecklistModelo criarModelo(TipoChecklist tipo, String codigo, String descricao, ResponsavelChecklist responsavel,
                                           boolean obrigatorio, Integer prazoDias, Integer ordem) {
        var m = ItemChecklistModelo.criar(tipo, codigo, descricao, responsavel, obrigatorio, prazoDias,
                ordem != null ? ordem : repository.findModelos(tipo).size() + 1);
        if (m.getCodigo() != null && repository.existeCodigo(tipo, m.getCodigo(), null))
            throw IgrpResponseStatusException.conflict("Já existe um item com o código " + m.getCodigo() + " neste modelo.");
        return repository.save(m);
    }

    /** Altera o texto, o responsável, o prazo e a ordem, ou desactiva (o tipo e o código não mudam). */
    @Transactional
    public ItemChecklistModelo actualizarModelo(ItemChecklistModeloId id, String descricao, ResponsavelChecklist responsavel,
                                                Boolean obrigatorio, Integer prazoDias, Integer ordem, Boolean activo) {
        var m = repository.findModelo(id).orElseThrow(() -> IgrpResponseStatusException.notFound("Item do modelo não encontrado."));
        m.definir(descricao != null ? descricao : m.getDescricao(), responsavel != null ? responsavel : m.getResponsavel(),
                obrigatorio != null ? obrigatorio : m.isObrigatorio(), prazoDias != null ? prazoDias : m.getPrazoDias(),
                ordem != null ? ordem : m.getOrdem());
        if (activo != null) m.activar(activo);
        return repository.save(m);
    }

    // ---------------------------------------------------------------- leitura

    @Transactional(readOnly = true)
    public List<ItemChecklistModelo> modelo(TipoChecklist tipo) {
        return repository.findModelos(tipo);
    }

    @Transactional(readOnly = true)
    public List<Checklist> doFuncionario(FuncionarioId funcionarioId) {
        funcionario(funcionarioId);
        return repository.findByFuncionario(funcionarioId);
    }

    /** A lista de trabalho: as abertas com itens pendentes (de uma área, e só as atrasadas, se pedido). */
    @Transactional(readOnly = true)
    public List<Checklist> pendentes(TipoChecklist tipo, ResponsavelChecklist responsavel, boolean atrasadas) {
        return repository.findComPendentes(tipo, responsavel, atrasadas ? hoje() : null);
    }

    /** As minhas checklists e as da minha equipa directa com itens da chefia pendentes. */
    @Transactional(readOnly = true)
    public List<Checklist> minhas(FuncionarioId eu) {
        var minhas = new java.util.ArrayList<>(repository.findByFuncionario(eu));
        for (var membro : chefiaService.equipaDirecta(eu))
            repository.findByFuncionario(membro).stream()
                    .filter(c -> c.getEstado() == Checklist.Estado.ABERTA && temPendentes(c, ResponsavelChecklist.CHEFIA))
                    .forEach(minhas::add);
        return minhas;
    }

    public String nomeDe(FuncionarioId id) {
        return funcionarioRepository.findById(id).map(Funcionario::getNomeCompleto).orElse("um colaborador");
    }

    private Optional<Checklist> aberta(FuncionarioId funcionarioId, TipoChecklist tipo) {
        return repository.findCorrente(funcionarioId, tipo).filter(c -> c.getEstado() == Checklist.Estado.ABERTA);
    }

    private static boolean temPendentes(Checklist c, ResponsavelChecklist r) {
        return c.getItens().stream().anyMatch(i -> i.pendente() && i.getResponsavel() == r);
    }

    private Checklist checklist(FuncionarioId funcionarioId, ChecklistId id) {
        return repository.findById(id).filter(c -> c.getFuncionarioId().equals(funcionarioId))
                .orElseThrow(() -> IgrpResponseStatusException.notFound("Checklist não encontrada."));
    }

    private Funcionario funcionario(FuncionarioId id) {
        return funcionarioRepository.findById(id).orElseThrow(() -> IgrpResponseStatusException.notFound("Colaborador não encontrado."));
    }

    static String nome(TipoChecklist t) {
        return t == TipoChecklist.ENTRADA ? "entrada" : "saída";
    }

    LocalDate hoje() { return LocalDate.now(); }

    private static IgrpResponseStatusException invalido(String m) {
        return IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY, m);
    }
}
