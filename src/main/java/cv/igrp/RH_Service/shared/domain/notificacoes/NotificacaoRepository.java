package cv.igrp.RH_Service.shared.domain.notificacoes;

import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.shared.domain.pagination.PageResult;

import java.time.LocalDateTime;
import java.util.Optional;

public interface NotificacaoRepository {

    Notificacao save(Notificacao notificacao);

    Optional<Notificacao> findById(NotificacaoId id);

    /** As de uma pessoa, das mais recentes para as mais antigas; {@code soNaoLidas} filtra na consulta. */
    PageResult<Notificacao> findDoDestinatario(FuncionarioId destinatario, boolean soNaoLidas, int pagina, int tamanho);

    /** As de uma caixa partilhada, das mais recentes para as mais antigas. */
    PageResult<Notificacao> findDoPerfil(PerfilDestino perfil, boolean soNaoLidas, int pagina, int tamanho);

    long contarNaoLidas(FuncionarioId destinatario);

    long contarNaoLidas(PerfilDestino perfil);

    /** Marca lidas todas as por ler de uma pessoa; devolve quantas mudaram. */
    int marcarTodasLidas(FuncionarioId destinatario, LocalDateTime agora);

    /** Enfileira o envio por correio electrónico (o envio em si é {@code TODO(smtp)}). */
    void enfileirarEmail(NotificacaoId notificacao, LocalDateTime agora);
}
