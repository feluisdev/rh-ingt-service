package cv.igrp.RH_Service.shared.config;

import java.util.Optional;

/**
 * Âmbito de autor de sistema, por thread, para escritas feitas por trabalhos de fundo
 * (agendadores) que não correm no contexto de um utilizador autenticado.
 *
 * <p>Um agendador que corre à meia-noite não tem um {@code Authentication} no
 * {@link org.springframework.security.core.context.SecurityContextHolder}. Sem esta classe,
 * {@code ApplicationAuditorAware} cairia sempre no fallback genérico {@code system-bot@nosi.cv},
 * que não distingue qual job escreveu o quê. {@link #runAs(String, Runnable)} nomeia
 * explicitamente esse autor — por exemplo {@code "scheduler:period-expiry"} — durante a
 * execução da acção, para que a auditoria (Envers, {@code created_by}/{@code last_modified_by})
 * registe um nome distinguível em vez do genérico.
 *
 * <p><strong>Porque o {@code finally} é a peça central:</strong> o valor vive num
 * {@link ThreadLocal}, e as threads de um pool de agendamento são reutilizadas entre
 * execuções. Se a acção lançar uma excepção e o valor anterior não for reposto, o nome do
 * trabalho de fundo fica colado à thread — e a próxima vez que essa thread for reaproveitada
 * para servir um pedido HTTP (ou outro agendador), as escritas dessa thread seriam atribuídas
 * ao sistema, e não a quem realmente as fez. Por isso {@link #runAs(String, Runnable)} repõe o
 * valor anterior (não remove, para não partir um âmbito aninhado) num bloco {@code finally},
 * que corre quer a acção termine normalmente quer lance.
 */
public final class SystemAuditor {

    private static final ThreadLocal<String> CURRENT_AUDITOR = new ThreadLocal<>();

    private SystemAuditor() {
    }

    /**
     * Devolve o autor de sistema activo na thread corrente, se houver algum âmbito em curso.
     */
    public static Optional<String> current() {
        return Optional.ofNullable(CURRENT_AUDITOR.get());
    }

    /**
     * Corre {@code action} com {@code auditor} como autor de sistema activo na thread
     * corrente, repondo sempre o valor anterior no fim — mesmo que {@code action} lance.
     *
     * <p>Repor, não remover: se este {@code runAs} estiver aninhado dentro de outro, remover
     * apagaria também o âmbito exterior. Se não havia âmbito exterior (valor anterior nulo),
     * repor um valor nulo equivale a remover.
     *
     * @param auditor o nome do autor de sistema, por exemplo {@code "scheduler:period-expiry"}
     * @param action  a acção a correr sob esse âmbito
     */
    public static void runAs(String auditor, Runnable action) {
        String previous = CURRENT_AUDITOR.get();
        CURRENT_AUDITOR.set(auditor);
        try {
            action.run();
        } finally {
            if (previous == null) {
                CURRENT_AUDITOR.remove();
            } else {
                CURRENT_AUDITOR.set(previous);
            }
        }
    }
}
