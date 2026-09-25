package cv.igrp.RH_Service.shared.application.services.scheduler;

/**
 * Tipo de um {@link JobParametro}. Serve de contrato com a interface: é a partir daqui que a UI
 * decide que controlo desenhar no formulário do disparo manual.
 */
public enum TipoParametroJob {

    /** Data no formato ISO {@code aaaa-MM-dd} (também se aceita {@code dd/MM/aaaa}). */
    DATA,
    /** Ano com quatro algarismos. */
    ANO,
    TEXTO,
    INTEIRO,
    BOOLEANO,
    UUID
}
