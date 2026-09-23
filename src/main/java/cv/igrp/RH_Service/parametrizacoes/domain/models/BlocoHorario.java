package cv.igrp.RH_Service.parametrizacoes.domain.models;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalTime;

/**
 * Um bloco de um dia da semana. O intervalo de descanso é o espaço entre dois blocos.
 *
 * <p>{@code obrigatorio} só distingue alguma coisa no horário flexível: marca as plataformas
 * fixas (presença obrigatória); os outros blocos são a margem. No fixo todos os blocos são
 * obrigatórios.
 */
public record BlocoHorario(DayOfWeek dia, LocalTime inicio, LocalTime fim, boolean obrigatorio) {

    public int minutos() {
        return (int) Duration.between(inicio, fim).toMinutes();
    }

    boolean sobrepoe(BlocoHorario outro) {
        return dia == outro.dia && inicio.isBefore(outro.fim) && outro.inicio.isBefore(fim);
    }
}
