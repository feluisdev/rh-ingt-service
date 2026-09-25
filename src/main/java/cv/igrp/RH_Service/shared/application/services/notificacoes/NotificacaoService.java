package cv.igrp.RH_Service.shared.application.services.notificacoes;

import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.domain.notificacoes.Notificacao;
import cv.igrp.RH_Service.shared.domain.notificacoes.NotificacaoId;
import cv.igrp.RH_Service.shared.domain.notificacoes.NotificacaoRepository;
import cv.igrp.RH_Service.shared.domain.notificacoes.PerfilDestino;
import cv.igrp.RH_Service.shared.domain.pagination.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Ler e marcar lidas as notificações: a caixa de cada pessoa ({@code /me}) e as caixas partilhadas
 * dos perfis. Para escrever notificações usa-se o {@link Notificador}.
 */
@Service
@RequiredArgsConstructor
public class NotificacaoService {

    static final int TAMANHO_MAXIMO = 100;

    private final NotificacaoRepository repository;

    @Transactional(readOnly = true)
    public PageResult<Notificacao> minhas(FuncionarioId eu, boolean soNaoLidas, int pagina, int tamanho) {
        return repository.findDoDestinatario(eu, soNaoLidas, Math.max(0, pagina), tamanho(tamanho));
    }

    @Transactional(readOnly = true)
    public PageResult<Notificacao> doPerfil(PerfilDestino perfil, boolean soNaoLidas, int pagina, int tamanho) {
        return repository.findDoPerfil(perfil, soNaoLidas, Math.max(0, pagina), tamanho(tamanho));
    }

    @Transactional(readOnly = true)
    public long naoLidas(FuncionarioId eu) {
        return repository.contarNaoLidas(eu);
    }

    @Transactional(readOnly = true)
    public long naoLidas(PerfilDestino perfil) {
        return repository.contarNaoLidas(perfil);
    }

    /** Marca lida uma notificação minha. A de outra pessoa não se vê (404), como se não existisse. */
    @Transactional
    public Notificacao marcarLida(FuncionarioId eu, NotificacaoId id) {
        Notificacao n = repository.findById(id).filter(x -> x.eDe(eu))
                .orElseThrow(() -> IgrpResponseStatusException.notFound("Notificação não encontrada."));
        n.marcarLida(agora());
        return repository.save(n);
    }

    /** Marca lida uma notificação de uma caixa partilhada (fica lida para todo o perfil). */
    @Transactional
    public Notificacao marcarLida(PerfilDestino perfil, NotificacaoId id) {
        Notificacao n = repository.findById(id).filter(x -> x.getPerfil() == perfil)
                .orElseThrow(() -> IgrpResponseStatusException.notFound("Notificação não encontrada."));
        n.marcarLida(agora());
        return repository.save(n);
    }

    @Transactional
    public int marcarTodasLidas(FuncionarioId eu) {
        return repository.marcarTodasLidas(eu, agora());
    }

    private static int tamanho(int pedido) {
        if (pedido <= 0) return 20;
        return Math.min(pedido, TAMANHO_MAXIMO);
    }

    LocalDateTime agora() { return LocalDateTime.now(); }
}
