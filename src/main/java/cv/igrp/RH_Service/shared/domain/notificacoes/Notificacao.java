package cv.igrp.RH_Service.shared.domain.notificacoes;

import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Uma notificação na aplicação: um aviso para <b>uma pessoa</b> ({@link #destinatario}) ou para
 * <b>uma caixa partilhada</b> ({@link #perfil}), nunca para as duas. Nasce por ler e fica lida quando
 * alguém a abre — não se apaga (é o registo de que o aviso foi dado).
 *
 * <p>O {@link #recursoTipo}/{@link #recursoId} dizem a que se refere (um pedido, um processo…), para o
 * ecrã abrir o sítio certo; não são chaves estrangeiras — a notificação sobrevive ao que anuncia.
 */
@Getter
public class Notificacao {

    public static final int MAX_TITULO = 200;
    public static final int MAX_TEXTO = 2000;

    private NotificacaoId id;
    private FuncionarioId destinatario;
    private PerfilDestino perfil;
    private TipoNotificacao tipo;
    private String titulo;
    private String texto;
    private String recursoTipo;
    private String recursoId;
    private LocalDateTime criadaEm;
    private LocalDateTime lidaEm;

    private Notificacao() {}

    public static Notificacao criar(FuncionarioId destinatario, PerfilDestino perfil, TipoNotificacao tipo,
                                    String titulo, String texto, String recursoTipo, String recursoId,
                                    LocalDateTime agora) {
        if ((destinatario == null) == (perfil == null))
            throw new IllegalArgumentException("Uma notificação vai para uma pessoa ou para um perfil — exactamente um dos dois.");
        if (titulo == null || titulo.isBlank())
            throw new IllegalArgumentException("Uma notificação tem título.");
        var n = new Notificacao();
        n.id = NotificacaoId.gerarNovo();
        n.destinatario = destinatario;
        n.perfil = perfil;
        n.tipo = tipo != null ? tipo : TipoNotificacao.AVISO;
        n.titulo = cortar(titulo.trim(), MAX_TITULO);
        n.texto = texto == null || texto.isBlank() ? null : cortar(texto.trim(), MAX_TEXTO);
        n.recursoTipo = recursoTipo;
        n.recursoId = recursoId;
        n.criadaEm = Objects.requireNonNull(agora);
        return n;
    }

    public static Notificacao reconstruir(NotificacaoId id, FuncionarioId destinatario, PerfilDestino perfil,
                                          TipoNotificacao tipo, String titulo, String texto, String recursoTipo,
                                          String recursoId, LocalDateTime criadaEm, LocalDateTime lidaEm) {
        var n = new Notificacao();
        n.id = id;
        n.destinatario = destinatario;
        n.perfil = perfil;
        n.tipo = tipo;
        n.titulo = titulo;
        n.texto = texto;
        n.recursoTipo = recursoTipo;
        n.recursoId = recursoId;
        n.criadaEm = criadaEm;
        n.lidaEm = lidaEm;
        return n;
    }

    /** Idempotente: marcar lida uma notificação já lida mantém a primeira data. */
    public void marcarLida(LocalDateTime agora) {
        if (lidaEm == null) lidaEm = agora;
    }

    public boolean isLida() {
        return lidaEm != null;
    }

    /** É desta pessoa: o destinatário é ela (as das caixas partilhadas não são de ninguém em particular). */
    public boolean eDe(FuncionarioId funcionarioId) {
        return destinatario != null && destinatario.equals(funcionarioId);
    }

    /** Um texto maior do que a coluna corta-se, com reticências: a notificação não pode falhar por isso. */
    private static String cortar(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max - 1) + "…";
    }
}
