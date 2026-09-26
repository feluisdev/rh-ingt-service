package cv.igrp.RH_Service.colaboradores.application.services;

import cv.igrp.RH_Service.colaboradores.domain.models.Funcionario;
import cv.igrp.RH_Service.colaboradores.domain.models.MissaoServico;
import cv.igrp.RH_Service.colaboradores.domain.models.TipoFactoRh;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.MissaoServicoRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.PedidoAusenciaRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.MissaoServicoId;
import cv.igrp.RH_Service.shared.application.services.notificacoes.Notificador;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.domain.notificacoes.TipoNotificacao;
import cv.igrp.RH_Service.shared.domain.service.Datas;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * <b>Missões de serviço</b> (Lei n.º 20/X/2023, art. 159.º; BR-MSS-01..10). Pedidas pelo próprio ou pela chefia (em
 * {@code /me}) ou pelo RH; autorizadas pela chefia directa de todos os participantes ou pelo RH; no regresso, o relatório.
 * Os dias de ajudas de custo vão ao diário de factos por participante — o valor é do salarial.
 */
@Service
@RequiredArgsConstructor
public class MissaoServicoService {

    static final String RECURSO = "MISSAO_SERVICO";

    /** Os termos da missão. */
    public record Dados(MissaoServico.Destino destinoTipo, String destino, String objectivo, LocalDateTime partida, LocalDateTime regresso,
                        MissaoServico.Transporte transporte, boolean alojamentoACargo, boolean adiantamento) {}

    public record Resultado(MissaoServico missao, List<String> alertas) {}

    private final MissaoServicoRepository repository;
    private final FuncionarioRepository funcionarioRepository;
    private final ChefiaService chefiaService;
    private final PedidoAusenciaRepository pedidoAusenciaRepository;
    private final DiarioFactos diarioFactos;
    private final Notificador notificador;

    // ---------------------------------------------------------------- pedir

    @Transactional
    public Resultado pedir(Dados d, List<FuncionarioId> participantes) {
        return criar(d, participantes, null);
    }

    /** O próprio pede a sua; a chefia, para a sua equipa directa (pode ir junto). Outros: 403. */
    @Transactional
    public Resultado pedirComo(FuncionarioId eu, Dados d, List<FuncionarioId> participantes) {
        var lista = participantes == null || participantes.isEmpty() ? List.of(eu) : participantes;
        for (var p : lista)
            if (!p.equals(eu) && !chefiaService.eChefeDirecto(eu, p)) throw proibido("Só pede missões para si ou para a sua equipa directa.");
        var r = criar(d, lista, eu);
        boolean eChefeDeTodos = lista.stream().allMatch(p -> chefiaService.eChefeDirecto(eu, p));
        if (!eChefeDeTodos)
            for (var p : lista) if (p.equals(eu))
                notificador.para(chefiaService.chefeDirecto(eu)).tipo(TipoNotificacao.MISSAO_SERVICO)
                        .titulo(nome(eu) + " pede missão de serviço a " + r.missao().getDestino() + " (" + periodo(r.missao()) + ")")
                        .recurso(RECURSO, r.missao().getId().getStringValor()).enviar();
        notificador.paraRh().tipo(TipoNotificacao.MISSAO_SERVICO)
                .titulo("Missão de serviço pedida: " + r.missao().getDestino() + " (" + periodo(r.missao()) + ")")
                .recurso(RECURSO, r.missao().getId().getStringValor()).enviar();
        return r;
    }

    private Resultado criar(Dados d, List<FuncionarioId> participantes, FuncionarioId pedidoPor) {
        var m = MissaoServico.pedir(participantes, d.destinoTipo(), d.destino(), d.objectivo(), d.partida(), d.regresso(), d.transporte(),
                d.alojamentoACargo(), d.adiantamento(), pedidoPor);
        for (var p : participantes) {
            if (!Boolean.TRUE.equals(funcionario(p).getIsActive())) throw invalido(nome(p) + " já não está ao serviço.");
            if (!repository.findSobrepostas(p, m.getPartida(), m.getRegresso(), m.getId()).isEmpty())
                throw IgrpResponseStatusException.conflict(nome(p) + " já tem outra missão nessas datas.");
        }
        return new Resultado(repository.save(m), alertas(m));
    }

    // ---------------------------------------------------------------- decidir

    @Transactional
    public Resultado autorizar(MissaoServicoId id, String despacho) {
        var m = missao(id);
        m.autorizar(despacho);
        return depoisDeAutorizar(repository.save(m));
    }

    /** A chefia directa autoriza, se o é de todos os participantes (403 se não). */
    @Transactional
    public Resultado autorizarComo(FuncionarioId eu, MissaoServicoId id, String despacho) {
        var m = missao(id);
        exigirChefeDeTodos(eu, m);
        m.autorizar(despacho);
        return depoisDeAutorizar(repository.save(m));
    }

    private Resultado depoisDeAutorizar(MissaoServico m) {
        registarFactos(m, "AUTORIZADA");
        for (var p : m.getParticipantes())
            notificador.para(p).tipo(TipoNotificacao.MISSAO_SERVICO)
                    .titulo("Missão de serviço autorizada: " + m.getDestino() + " (" + periodo(m) + ")")
                    .texto("No regresso, registe o relatório da missão.").recurso(RECURSO, m.getId().getStringValor()).enviar();
        return new Resultado(m, alertas(m));
    }

    @Transactional
    public Resultado recusar(MissaoServicoId id, String motivo) {
        var m = missao(id);
        m.recusar(motivo);
        return depoisDeRecusar(repository.save(m));
    }

    @Transactional
    public Resultado recusarComo(FuncionarioId eu, MissaoServicoId id, String motivo) {
        var m = missao(id);
        exigirChefeDeTodos(eu, m);
        m.recusar(motivo);
        return depoisDeRecusar(repository.save(m));
    }

    private Resultado depoisDeRecusar(MissaoServico m) {
        for (var p : m.getParticipantes())
            notificador.para(p).tipo(TipoNotificacao.MISSAO_SERVICO).titulo("Missão de serviço a " + m.getDestino() + " recusada")
                    .texto(m.getMotivo()).recurso(RECURSO, m.getId().getStringValor()).enviar();
        return new Resultado(m, List.of());
    }

    // ---------------------------------------------------------------- regresso e cancelamento

    /** O relatório de regresso (pelo RH ou por um participante), com as horas reais se mudaram; os dias acertam-se. */
    @Transactional
    public Resultado realizar(FuncionarioId quem, MissaoServicoId id, String relatorio, LocalDateTime partidaReal, LocalDateTime regressoReal) {
        var m = missao(id);
        if (quem != null && !m.getParticipantes().contains(quem)) throw proibido("Só um participante regista o relatório da missão.");
        m.realizar(relatorio, partidaReal, regressoReal, hoje());
        var gravada = repository.save(m);
        registarFactos(gravada, "REALIZADA");
        return new Resultado(gravada, List.of());
    }

    @Transactional
    public Resultado cancelar(MissaoServicoId id, String motivo) {
        var m = missao(id);
        boolean estavaAutorizada = m.getEstado() == MissaoServico.Estado.AUTORIZADA;
        m.cancelar(motivo);
        var gravada = repository.save(m);
        if (estavaAutorizada) registarFactos(gravada, "CANCELADA");
        for (var p : gravada.getParticipantes())
            notificador.para(p).tipo(TipoNotificacao.MISSAO_SERVICO).titulo("Missão de serviço a " + gravada.getDestino() + " cancelada")
                    .texto(gravada.getMotivo()).recurso(RECURSO, gravada.getId().getStringValor()).enviar();
        return new Resultado(gravada, List.of());
    }

    /** Para o job: no dia a seguir ao regresso previsto, sem relatório, lembra os participantes e o RH (BR-MSS-09). */
    @Transactional
    public int lembrarRelatorios(LocalDate dia) {
        int n = 0;
        for (var m : repository.findAutorizadasComRegressoEm(dia.minusDays(1))) {
            for (var p : m.getParticipantes())
                notificador.para(p).tipo(TipoNotificacao.MISSAO_SERVICO).titulo("Registe o relatório da missão a " + m.getDestino())
                        .recurso(RECURSO, m.getId().getStringValor()).enviar();
            notificador.paraRh().tipo(TipoNotificacao.MISSAO_SERVICO)
                    .titulo("Missão a " + m.getDestino() + " (" + periodo(m) + "): falta o relatório de regresso")
                    .recurso(RECURSO, m.getId().getStringValor()).enviar();
            n++;
        }
        return n;
    }

    // ---------------------------------------------------------------- leitura

    @Transactional(readOnly = true)
    public MissaoServico missao(MissaoServicoId id) {
        return repository.findById(id).orElseThrow(() -> IgrpResponseStatusException.notFound("Missão de serviço não encontrada."));
    }

    @Transactional(readOnly = true)
    public List<MissaoServico> listar(MissaoServico.Estado estado, FuncionarioId participante) {
        return repository.find(estado, participante);
    }

    /** As minhas e as pedidas da minha equipa directa (para decidir). */
    @Transactional(readOnly = true)
    public List<MissaoServico> minhas(FuncionarioId eu) {
        var l = new ArrayList<>(repository.find(null, eu));
        for (var membro : chefiaService.equipaDirecta(eu))
            repository.find(MissaoServico.Estado.PEDIDA, membro).stream()
                    .filter(m -> l.stream().noneMatch(x -> x.getId().equals(m.getId()))).forEach(l::add);
        return l;
    }

    /** Sobreposição com férias ou ausências aprovadas de algum participante: avisa, não impede (BR-MSS-05). */
    public List<String> alertas(MissaoServico m) {
        var l = new ArrayList<String>();
        for (var p : m.getParticipantes())
            if (!pedidoAusenciaRepository.findAprovadosEntre(p, m.getPartida().toLocalDate(), m.getRegresso().toLocalDate()).isEmpty())
                l.add(nome(p) + " tem férias ou ausência aprovada nesses dias.");
        return l;
    }

    public String nome(FuncionarioId id) {
        return funcionarioRepository.findById(id).map(Funcionario::getNomeCompleto).orElse("um colaborador");
    }

    private void registarFactos(MissaoServico m, String evento) {
        for (var p : m.getParticipantes()) {
            var dados = new LinkedHashMap<String, Object>();
            dados.put("evento", evento);
            dados.put("diasAjudasCusto", "CANCELADA".equals(evento) ? 0 : m.diasAjudasCusto());
            dados.put("destinoTipo", m.getDestinoTipo().name());
            dados.put("destino", m.getDestino());
            dados.put("partida", m.getPartida());
            dados.put("regresso", m.getRegresso());
            dados.put("alojamentoACargo", m.isAlojamentoACargo());
            dados.put("transporte", m.getTransporte().name());
            diarioFactos.registar(p, TipoFactoRh.MISSAO_SERVICO, m.getPartida().toLocalDate(), RECURSO, m.getId().getStringValor(),
                    "Missão de serviço a " + m.getDestino() + " (" + periodo(m) + ") — " + evento.toLowerCase(), dados);
        }
    }

    private void exigirChefeDeTodos(FuncionarioId eu, MissaoServico m) {
        if (!m.getParticipantes().stream().allMatch(p -> chefiaService.eChefeDirecto(eu, p)))
            throw proibido("Só decide a chefia directa de todos os participantes; os outros casos são do RH.");
    }

    private Funcionario funcionario(FuncionarioId id) {
        return funcionarioRepository.findById(id).orElseThrow(() -> IgrpResponseStatusException.notFound("Colaborador não encontrado."));
    }

    static String periodo(MissaoServico m) {
        LocalDate p = m.getPartida().toLocalDate(), r = m.getRegresso().toLocalDate();
        return p.equals(r) ? Datas.pt(p) : Datas.pt(p) + " a " + Datas.pt(r);
    }

    LocalDate hoje() { return LocalDate.now(); }

    private static IgrpResponseStatusException invalido(String m) {
        return IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY, m);
    }

    private static IgrpResponseStatusException proibido(String m) {
        return IgrpResponseStatusException.of(HttpStatus.FORBIDDEN, m);
    }
}
