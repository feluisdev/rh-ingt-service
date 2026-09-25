package cv.igrp.RH_Service.shared.application.services.notificacoes;

import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.shared.domain.notificacoes.Notificacao;
import cv.igrp.RH_Service.shared.domain.notificacoes.NotificacaoRepository;
import cv.igrp.RH_Service.shared.domain.notificacoes.PerfilDestino;
import cv.igrp.RH_Service.shared.domain.notificacoes.TipoNotificacao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * <b>O ponto único para notificar</b>, de qualquer módulo. Injecta-se e escreve-se numa linha:
 *
 * <pre>{@code
 * notificador.para(funcionarioId)
 *         .tipo(TipoNotificacao.PEDIDO_AUSENCIA_DECIDIDO)
 *         .titulo("O seu pedido de férias foi aprovado")
 *         .texto("De 03/11/2026 a 14/11/2026.")
 *         .recurso("PEDIDO_AUSENCIA", pedido.getId().getStringValor())
 *         .enviar();
 *
 * notificador.paraRh().tipo(TipoNotificacao.DECLARACAO_PEDIDA).titulo("...").enviar();
 * notificador.para(chefiaService.chefeDirecto(id)).titulo("...").enviar();   // sem chefia: não faz nada
 * }</pre>
 *
 * <ul>
 *   <li>Grava na <b>mesma transacção</b> de quem chama: a notificação existe se, e só se, o facto que a
 *       causou ficou gravado.</li>
 *   <li><b>Nunca faz falhar quem chama</b> por um defeito da própria notificação (título em falta, texto
 *       longo demais — este corta-se): regista um aviso no log e segue.</li>
 *   <li>{@code porEmail()} enfileira também o envio por correio electrónico; o envio em si é
 *       {@code TODO(smtp)} — a fila fica em {@code t_notificacao_envio} à espera do canal.</li>
 * </ul>
 */
@Service
public class Notificador {

    private static final Logger LOGGER = LoggerFactory.getLogger(Notificador.class);

    private final NotificacaoRepository repository;

    public Notificador(NotificacaoRepository repository) {
        this.repository = repository;
    }

    /** Para uma pessoa. */
    public Envio para(FuncionarioId destinatario) {
        return new Envio(destinatario, null);
    }

    /** Para uma pessoa que pode não existir (a chefia directa de quem não tem Lugar-pai, por exemplo). */
    public Envio para(Optional<FuncionarioId> destinatario) {
        return new Envio(destinatario.orElse(null), null).semDestinoNaoFazNada();
    }

    /** Para a caixa partilhada de um perfil. */
    public Envio para(PerfilDestino perfil) {
        return new Envio(null, perfil);
    }

    /** Para a caixa partilhada do RH. */
    public Envio paraRh() {
        return para(PerfilDestino.RH);
    }

    LocalDateTime agora() { return LocalDateTime.now(); }

    /** Uma notificação a compor. Cada método devolve o próprio envio; termina-se com {@link #enviar()}. */
    public final class Envio {
        private final FuncionarioId destinatario;
        private final PerfilDestino perfil;
        private TipoNotificacao tipo = TipoNotificacao.AVISO;
        private String titulo;
        private String texto;
        private String recursoTipo;
        private String recursoId;
        private boolean porEmail;
        private boolean semDestinoNaoFazNada;

        private Envio(FuncionarioId destinatario, PerfilDestino perfil) {
            this.destinatario = destinatario;
            this.perfil = perfil;
        }

        private Envio semDestinoNaoFazNada() {
            this.semDestinoNaoFazNada = true;
            return this;
        }

        public Envio tipo(TipoNotificacao tipo) { this.tipo = tipo; return this; }
        public Envio titulo(String titulo) { this.titulo = titulo; return this; }
        public Envio texto(String texto) { this.texto = texto; return this; }

        /** A que se refere: o tipo do recurso (ex.: {@code PEDIDO_AUSENCIA}) e o seu id. */
        public Envio recurso(String tipo, Object id) {
            this.recursoTipo = tipo;
            this.recursoId = id != null ? id.toString() : null;
            return this;
        }

        /** Também por correio electrónico ({@code TODO(smtp)}: fica na fila). */
        public Envio porEmail() { this.porEmail = true; return this; }

        /** Grava. Vazio quando não havia destinatário ou a notificação estava mal composta (fica no log). */
        public Optional<Notificacao> enviar() {
            if (destinatario == null && perfil == null && semDestinoNaoFazNada) return Optional.empty();
            try {
                LocalDateTime agora = agora();
                Notificacao n = repository.save(Notificacao.criar(destinatario, perfil, tipo, titulo, texto,
                        recursoTipo, recursoId, agora));
                if (porEmail) repository.enfileirarEmail(n.getId(), agora);
                return Optional.of(n);
            } catch (IllegalArgumentException e) {
                LOGGER.warn("Notificação {} não enviada: {}", tipo, e.getMessage());
                return Optional.empty();
            }
        }
    }
}
