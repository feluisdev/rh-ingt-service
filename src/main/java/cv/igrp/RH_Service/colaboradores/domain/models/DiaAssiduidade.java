package cv.igrp.RH_Service.colaboradores.domain.models;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * O dia calculado a partir das marcações válidas — o que o art. 164.º n.º 3 da Lei n.º 20/X/2023
 * pede: início e termo do trabalho e os intervalos. Não se guarda: deriva das marcações.
 *
 * <p>Emparelha-se por ordem: uma entrada abre, a saída seguinte fecha. O que não emparelha é
 * <b>anomalia</b>, e os minutos só contam nos pares completos — nunca se inventa uma hora que ninguém
 * picou. Um período que passe a meia-noite é trabalho por turnos, fora deste passo: as marcações
 * contam no dia da sua data.
 */
public record DiaAssiduidade(LocalDate data, List<Periodo> periodos, List<Integer> intervalosMinutos,
                             int minutosTrabalhados, Set<AnomaliaMarcacao> anomalias) {

    public record Periodo(LocalTime entrada, LocalTime saida) {
        public int minutos() { return (int) Duration.between(entrada, saida).toMinutes(); }
    }

    /** {@code marcacoes}: as do dia; as anuladas ignoram-se aqui. */
    public static DiaAssiduidade calcular(LocalDate data, List<MarcacaoAssiduidade> marcacoes) {
        List<MarcacaoAssiduidade> validas = marcacoes.stream()
                .filter(m -> !m.isAnulada())
                .filter(m -> m.getMomento().toLocalDate().equals(data))
                .sorted(Comparator.comparing(MarcacaoAssiduidade::getMomento))
                .toList();

        List<Periodo> periodos = new ArrayList<>();
        Set<AnomaliaMarcacao> anomalias = new LinkedHashSet<>();
        LocalTime aberta = null;
        for (MarcacaoAssiduidade m : validas) {
            LocalTime hora = m.getMomento().toLocalTime();
            if (m.getSentido() == SentidoMarcacao.ENTRADA) {
                if (aberta != null) anomalias.add(AnomaliaMarcacao.ENTRADAS_SEGUIDAS);
                aberta = hora;
            } else if (aberta == null) {
                anomalias.add(AnomaliaMarcacao.SAIDA_SEM_ENTRADA);
            } else {
                periodos.add(new Periodo(aberta, hora));
                aberta = null;
            }
        }
        if (aberta != null) anomalias.add(AnomaliaMarcacao.ENTRADA_SEM_SAIDA);

        List<Integer> intervalos = new ArrayList<>();
        for (int i = 1; i < periodos.size(); i++)
            intervalos.add((int) Duration.between(periodos.get(i - 1).saida(), periodos.get(i).entrada()).toMinutes());
        int minutos = periodos.stream().mapToInt(Periodo::minutos).sum();

        return new DiaAssiduidade(data, List.copyOf(periodos), List.copyOf(intervalos), minutos, Set.copyOf(anomalias));
    }
}
