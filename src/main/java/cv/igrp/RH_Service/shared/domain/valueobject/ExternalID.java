package cv.igrp.RH_Service.shared.domain.valueobject;

import java.util.UUID;


public class ExternalID {


    private final UUID valor; // Final para garantir imutabilidade

    /**
     * Construtor privado para controlar a instanciação através de métodos de factory.
     * @param valor O UUID a ser encapsulado.
     */
    private ExternalID(UUID valor) {
        if (valor == null) {
            throw new IllegalArgumentException("O valor do identificador não pode ser nulo.");
        }
        this.valor = valor;
    }

    /**
     * Cria uma instância de Identificador a partir de um UUID existente.
     * @param uuid O UUID.
     * @return Uma nova instância de Identificador.
     */
    public static ExternalID from(UUID uuid) {
        return new ExternalID(uuid);
    }

    /**
     * Cria uma instância de Identificador a partir de uma representação em String de um UUID.
     * @param uuidString A String do UUID.
     * @return Uma nova instância de Identificador.
     * @throws IllegalArgumentException se a string não for um UUID válido.
     */
    public static ExternalID from(String uuidString) {
        if (uuidString == null || uuidString.trim().isEmpty()) {
            throw new IllegalArgumentException("A string do UUID não pode ser nula ou vazia.");
        }
        try {
            return new ExternalID(UUID.fromString(uuidString));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("String fornecida não é um UUID válido: " + uuidString, e);
        }
    }

    /**
     * Gera um novo Identificador com um UUID aleatório.
     * @return Uma nova instância de Identificador.
     */
    public static ExternalID gerarNovo() {
        return new ExternalID(UUID.randomUUID());
    }

    /**
     * Retorna o valor UUID encapsulado.
     * @return O UUID.
     */
    public UUID getValor() {
        return valor;
    }

    public String getStringValor() {
        return valor.toString();
    }
}
