package cv.igrp.RH_Service.colaboradores.application.services;

import cv.igrp.RH_Service.colaboradores.domain.models.Exoneracao;
import cv.igrp.RH_Service.colaboradores.domain.models.Funcionario;
import cv.igrp.RH_Service.colaboradores.domain.repository.ExoneracaoRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.ProcessoDisciplinarRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.ExoneracaoId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.shared.application.services.notificacoes.Notificador;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.domain.notificacoes.TipoNotificacao;
import cv.igrp.RH_Service.shared.domain.service.Datas;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * <b>Exoneração voluntária</b> (Lei n.º 20/X/2023, arts. 94.º e 95.º; BR-EXO-01..08). O pedido (pelo próprio ou registado
 * pelo RH) com o pré-aviso; o despacho; e, no dia devido, a cessação do vínculo pelo caminho único da cessação — o que
 * publica o despacho de exoneração. As condicionantes vêem-se sempre e o dia de efeito ajusta-se a elas.
 */
@Service
@RequiredArgsConstructor
public class ExoneracaoService {

    static final String RECURSO = "EXONERACAO";
    public static final String MOTIVO_CESSACAO = "EXONERACAO_VOLUNTARIA";

    public record Resultado(Exoneracao exoneracao, List<String> condicionantes) {}

    private final ExoneracaoRepository repository;
    private final FuncionarioRepository funcionarioRepository;
    private final ProcessoDisciplinarRepository processoDisciplinarRepository;
    private final List<GarantiasDeFormacao> garantias;
    private final CessacaoService cessacaoService;
    private final ChefiaService chefiaService;
    private final Notificador notificador;

    @Transactional
    public Resultado pedir(FuncionarioId funcionarioId, LocalDate preAviso, LocalDate pretendida, String motivo, boolean peloProprio) {
        var f = funcionario(funcionarioId);
        if (!Boolean.TRUE.equals(f.getIsActive())) throw invalido("Este colaborador já não está ao serviço.");
        if (repository.findByFuncionario(funcionarioId).stream().anyMatch(Exoneracao::emCurso))
            throw IgrpResponseStatusException.conflict("Já há um pedido de exoneração em curso.");
        var e = repository.save(Exoneracao.pedir(funcionarioId, preAviso, pretendida, motivo, peloProprio, hoje()));
        var cond = condicionantes(funcionarioId);
        String titulo = "Pedido de exoneração de " + f.getNomeCompleto() + " com efeitos a " + Datas.pt(e.getDataPretendida());
        String texto = cond.isEmpty() ? null : "Condicionada: " + String.join(" ", cond);
        notificador.paraRh().tipo(TipoNotificacao.EXONERACAO).titulo(titulo).texto(texto).recurso(RECURSO, e.getId().getStringValor()).enviar();
        notificador.para(chefiaService.chefeDirecto(funcionarioId)).tipo(TipoNotificacao.EXONERACAO).titulo(titulo)
                .recurso(RECURSO, e.getId().getStringValor()).enviar();
        return new Resultado(e, cond);
    }

    /** O despacho. Se o dia de efeito já chegou (pré-aviso antigo), efectiva-se já. */
    @Transactional
    public Resultado deferir(FuncionarioId funcionarioId, ExoneracaoId id, String despacho, LocalDate data) {
        var e = exoneracao(funcionarioId, id);
        e.deferir(despacho, data != null ? data : hoje());
        var cond = condicionantes(funcionarioId);
        processar(e, cond, hoje());
        var gravada = repository.save(e);
        notificador.para(funcionarioId).tipo(TipoNotificacao.EXONERACAO)
                .titulo(gravada.getEstado() == Exoneracao.Estado.EFECTIVADA
                        ? "A sua exoneração produziu efeitos a " + Datas.pt(gravada.getDataEfeito())
                        : "A sua exoneração foi concedida: produz efeitos a " + Datas.pt(previsao(gravada, cond)))
                .texto(cond.isEmpty() ? null : "Enquanto se mantiver: " + String.join(" ", cond))
                .recurso(RECURSO, gravada.getId().getStringValor()).enviar();
        return new Resultado(gravada, cond);
    }

    /** Desistir (o próprio, do seu; ou o RH) antes de produzir efeitos. */
    @Transactional
    public Resultado desistir(FuncionarioId funcionarioId, ExoneracaoId id) {
        var e = exoneracao(funcionarioId, id);
        e.desistir();
        notificador.paraRh().tipo(TipoNotificacao.EXONERACAO).titulo(nome(funcionarioId) + " desistiu do pedido de exoneração")
                .recurso(RECURSO, e.getId().getStringValor()).enviar();
        return new Resultado(repository.save(e), List.of());
    }

    /**
     * Para o job: as deferidas cujo dia chegou produzem efeitos; as pedidas sem despacho na data pretendida lembram o RH de que
     * a exoneração deve ser concedida (art. 94.º n.º 4).
     */
    @Transactional
    public int processarDevidas(LocalDate dia) {
        int n = 0;
        for (var e : repository.find(Exoneracao.Estado.DEFERIDA)) {
            var antes = e.getEstado();
            processar(e, condicionantes(e.getFuncionarioId()), dia);
            repository.save(e);
            if (e.getEstado() != antes) n++;
        }
        for (var e : repository.find(Exoneracao.Estado.PEDIDA))
            if (dia.equals(e.getDataPretendida()) || dia.equals(e.dataLimite()))
                notificador.paraRh().tipo(TipoNotificacao.EXONERACAO)
                        .titulo("A exoneração de " + nome(e.getFuncionarioId()) + " devia produzir efeitos a " + Datas.pt(dia) + ": falta o despacho")
                        .recurso(RECURSO, e.getId().getStringValor()).enviar();
        return n;
    }

    private void processar(Exoneracao e, List<String> cond, LocalDate dia) {
        if (e.getEstado() != Exoneracao.Estado.DEFERIDA) return;
        if (!cond.isEmpty()) e.registarCondicionada(dia);
        LocalDate efeito = e.efeitoDevido(!cond.isEmpty(), dia);
        if (efeito == null) return;
        var f = funcionario(e.getFuncionarioId());
        if (Boolean.TRUE.equals(f.getIsActive()))
            cessacaoService.cessar(e.getFuncionarioId(), cessacaoService.estadoDeCessacaoPorOmissao(), efeito, MOTIVO_CESSACAO,
                    "Exoneração voluntária — " + e.getDespacho());
        e.efectivar(efeito);
    }

    /**
     * As causas que condicionam (art. 95.º): processo disciplinar em que é arguido, inquérito ou sindicância em curso a que
     * está ligado, prazos de garantia de formação por cumprir (BR-EXO-03).
     */
    public List<String> condicionantes(FuncionarioId funcionarioId) {
        var l = new ArrayList<String>();
        if (processoDisciplinarRepository.existeArguidoEmCurso(funcionarioId))
            l.add("É arguido em processo disciplinar em curso (art. 95.º a)).");
        boolean inquerito = processoDisciplinarRepository.findAllByFuncionarioId(funcionarioId).stream()
                .anyMatch(p -> p.getEspecie() != null && p.getEspecie().semArguido() && p.getFase() != null && p.getFase().emCurso());
        if (inquerito) l.add("Há inquérito ou sindicância em curso (art. 95.º a)).");
        for (var g : garantias) l.addAll(g.emCurso(funcionarioId, hoje()));
        return l;
    }

    /** Quando se prevê que produza efeitos, com o que hoje se sabe. */
    public LocalDate previsao(Exoneracao e, List<String> condicionantes) {
        if (e.getDataEfeito() != null) return e.getDataEfeito();
        return condicionantes.isEmpty() ? e.getDataPretendida() : e.dataLimite();
    }

    @Transactional(readOnly = true)
    public List<Exoneracao> listar(Exoneracao.Estado estado) {
        return repository.find(estado);
    }

    @Transactional(readOnly = true)
    public List<Exoneracao> doFuncionario(FuncionarioId funcionarioId) {
        return repository.findByFuncionario(funcionarioId);
    }

    public String nome(FuncionarioId id) {
        return funcionarioRepository.findById(id).map(Funcionario::getNomeCompleto).orElse("um colaborador");
    }

    private Exoneracao exoneracao(FuncionarioId funcionarioId, ExoneracaoId id) {
        return repository.findById(id).filter(e -> e.getFuncionarioId().equals(funcionarioId))
                .orElseThrow(() -> IgrpResponseStatusException.notFound("Pedido de exoneração não encontrado."));
    }

    private Funcionario funcionario(FuncionarioId id) {
        return funcionarioRepository.findById(id).orElseThrow(() -> IgrpResponseStatusException.notFound("Colaborador não encontrado."));
    }

    LocalDate hoje() { return LocalDate.now(); }

    private static IgrpResponseStatusException invalido(String m) {
        return IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY, m);
    }
}
