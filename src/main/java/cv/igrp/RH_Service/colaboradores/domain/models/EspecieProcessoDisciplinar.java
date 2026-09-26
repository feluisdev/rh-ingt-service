package cv.igrp.RH_Service.colaboradores.domain.models;

/** As formas de processo (Estatuto Disciplinar, arts. 37.º, 78.º–82.º e 96.º). */
public enum EspecieProcessoDisciplinar {
    /** O processo comum (arts. 47.º–77.º). */
    DISCIPLINAR_COMUM,
    /** Art. 78.º: infracção presenciada pelo superior — acusação em 48 horas, defesa até 5 dias. */
    INFRACCAO_CONSTATADA,
    /** Art. 80.º: auto por falta de assiduidade. */
    FALTA_ASSIDUIDADE,
    /** Art. 81.º: auto por abandono de lugar. */
    ABANDONO_LUGAR,
    /** Art. 96.º: apurar factos determinados. */
    INQUERITO,
    /** Art. 96.º: averiguação geral do funcionamento do serviço. */
    SINDICANCIA,
    /** Mero processo de averiguações (art. 6.º n.º 3). */
    AVERIGUACOES;

    /** Os que seguem os trâmites do art. 78.º (art. 82.º n.º 1): defesa até 5 dias. */
    public boolean sumario() {
        return this == INFRACCAO_CONSTATADA || this == FALTA_ASSIDUIDADE || this == ABANDONO_LUGAR;
    }

    /** Os que não acusam ninguém: terminam num relatório (art. 101.º). */
    public boolean semArguido() {
        return this == INQUERITO || this == SINDICANCIA || this == AVERIGUACOES;
    }
}
