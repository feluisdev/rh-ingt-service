package cv.igrp.RH_Service.colaboradores.domain.models;

import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.MissaoServicoId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * <b>Uma missão de serviço</b> (Lei n.º 20/X/2023, art. 159.º; BR-MSS-01..10): a deslocação do domicílio profissional por
 * motivo de serviço, de um ou mais colaboradores. Dá direito a ajudas de custo e transporte pelas tabelas do diploma de
 * desenvolvimento — aqui calculam-se só os <b>dias</b> de ajudas de custo (o valor é do salarial).
 *
 * <p>PEDIDA → AUTORIZADA / RECUSADA → REALIZADA (com o relatório de regresso); CANCELADA antes de realizada.
 */
@Getter
public class MissaoServico {

    public enum Destino { NACIONAL, ESTRANGEIRO }
    public enum Transporte { AVIAO, BARCO, VIATURA_SERVICO, VIATURA_PROPRIA, TRANSPORTE_PUBLICO, OUTRO }
    public enum Estado { PEDIDA, AUTORIZADA, RECUSADA, REALIZADA, CANCELADA }

    /** A partir desta hora a partida conta meio dia, e até ela o regresso também [ind.]. */
    public static final LocalTime MEIO_DIA = LocalTime.of(13, 0);
    private static final BigDecimal MEIO = new BigDecimal("0.5");

    private MissaoServicoId id;
    private List<FuncionarioId> participantes = new ArrayList<>();
    private Destino destinoTipo;
    private String destino;
    private String objectivo;
    private LocalDateTime partida;
    private LocalDateTime regresso;
    private Transporte transporte;
    private boolean alojamentoACargo;
    private boolean adiantamento;
    private Estado estado;
    private FuncionarioId pedidoPor;
    private String despacho;
    private String motivo;
    private String relatorio;
    private LocalDate dataRelatorio;

    private MissaoServico() {}

    public static MissaoServico pedir(List<FuncionarioId> participantes, Destino destinoTipo, String destino, String objectivo,
                                      LocalDateTime partida, LocalDateTime regresso, Transporte transporte, boolean alojamentoACargo,
                                      boolean adiantamento, FuncionarioId pedidoPor) {
        if (participantes == null || participantes.isEmpty()) throw invalido("Indique quem vai em missão.");
        if (participantes.stream().distinct().count() != participantes.size()) throw invalido("Há participantes repetidos.");
        if (destinoTipo == null) throw invalido("Diga se a missão é no país ou no estrangeiro.");
        if (destino == null || destino.isBlank()) throw invalido("Indique o destino (ilha e concelho, ou país e cidade).");
        if (objectivo == null || objectivo.isBlank()) throw invalido("Indique o objectivo da missão.");
        validarDatas(partida, regresso);
        var m = new MissaoServico();
        m.id = MissaoServicoId.gerarNovo();
        m.participantes = new ArrayList<>(participantes);
        m.destinoTipo = destinoTipo;
        m.destino = destino.trim();
        m.objectivo = objectivo.trim();
        m.partida = partida;
        m.regresso = regresso;
        m.transporte = transporte != null ? transporte : Transporte.OUTRO;
        m.alojamentoACargo = alojamentoACargo;
        m.adiantamento = adiantamento;
        m.pedidoPor = pedidoPor;
        m.estado = Estado.PEDIDA;
        return m;
    }

    public static MissaoServico reconstruir(MissaoServicoId id, List<FuncionarioId> participantes, Destino destinoTipo, String destino,
                                            String objectivo, LocalDateTime partida, LocalDateTime regresso, Transporte transporte,
                                            boolean alojamentoACargo, boolean adiantamento, Estado estado, FuncionarioId pedidoPor,
                                            String despacho, String motivo, String relatorio, LocalDate dataRelatorio) {
        var m = new MissaoServico();
        m.id = id;
        m.participantes = new ArrayList<>(participantes);
        m.destinoTipo = destinoTipo;
        m.destino = destino;
        m.objectivo = objectivo;
        m.partida = partida;
        m.regresso = regresso;
        m.transporte = transporte;
        m.alojamentoACargo = alojamentoACargo;
        m.adiantamento = adiantamento;
        m.estado = estado;
        m.pedidoPor = pedidoPor;
        m.despacho = despacho;
        m.motivo = motivo;
        m.relatorio = relatorio;
        m.dataRelatorio = dataRelatorio;
        return m;
    }

    public List<FuncionarioId> getParticipantes() {
        return Collections.unmodifiableList(participantes);
    }

    public void autorizar(String despacho) {
        exigir(Estado.PEDIDA, "autorizar");
        this.despacho = texto(despacho);
        this.estado = Estado.AUTORIZADA;
    }

    public void recusar(String motivo) {
        exigir(Estado.PEDIDA, "recusar");
        if (motivo == null || motivo.isBlank()) throw invalido("Indique porque recusa a missão.");
        this.motivo = motivo.trim();
        this.estado = Estado.RECUSADA;
    }

    /**
     * O regresso: o relatório (obrigatório), e as horas reais de partida e regresso se mudaram — os dias de ajudas de custo
     * acertam-se por elas (BR-MSS-07). Só depois do regresso previsto ou real.
     */
    public void realizar(String relatorio, LocalDateTime partidaReal, LocalDateTime regressoReal, LocalDate hoje) {
        exigir(Estado.AUTORIZADA, "registar o regresso");
        if (relatorio == null || relatorio.isBlank()) throw invalido("Escreva o relatório da missão.");
        LocalDateTime p = partidaReal != null ? partidaReal : partida;
        LocalDateTime r = regressoReal != null ? regressoReal : regresso;
        validarDatas(p, r);
        if (r.toLocalDate().isAfter(hoje)) throw invalido("O regresso ainda não aconteceu.");
        this.partida = p;
        this.regresso = r;
        this.relatorio = relatorio.trim();
        this.dataRelatorio = hoje;
        this.estado = Estado.REALIZADA;
    }

    public void cancelar(String motivo) {
        if (estado != Estado.PEDIDA && estado != Estado.AUTORIZADA) throw IgrpResponseStatusException.conflict("Esta missão já terminou.");
        if (motivo == null || motivo.isBlank()) throw invalido("Indique o motivo do cancelamento.");
        this.motivo = motivo.trim();
        this.estado = Estado.CANCELADA;
    }

    /** Conta para as ajudas de custo e para o apuramento: autorizada ou realizada. */
    public boolean conta() {
        return estado == Estado.AUTORIZADA || estado == Estado.REALIZADA;
    }

    public BigDecimal diasAjudasCusto() {
        return diasAjudasCusto(partida, regresso);
    }

    /**
     * <b>Dias de ajudas de custo</b> [ind.]: o dia da partida conta inteiro se se parte até às 13:00, meio depois; o do
     * regresso, inteiro se se chega depois das 13:00, meio até; os do meio, inteiros. Partida e regresso no mesmo dia: meio
     * dia se atravessa as 13:00, nada se não.
     */
    public static BigDecimal diasAjudasCusto(LocalDateTime partida, LocalDateTime regresso) {
        LocalDate p = partida.toLocalDate(), r = regresso.toLocalDate();
        if (p.equals(r))
            return partida.toLocalTime().isBefore(MEIO_DIA) && regresso.toLocalTime().isAfter(MEIO_DIA) ? MEIO : BigDecimal.ZERO;
        BigDecimal dias = BigDecimal.valueOf(r.toEpochDay() - p.toEpochDay() - 1);
        dias = dias.add(partida.toLocalTime().isAfter(MEIO_DIA) ? MEIO : BigDecimal.ONE);
        dias = dias.add(regresso.toLocalTime().isAfter(MEIO_DIA) ? BigDecimal.ONE : MEIO);
        return dias;
    }

    private static void validarDatas(LocalDateTime partida, LocalDateTime regresso) {
        if (partida == null || regresso == null) throw invalido("Indique a data e a hora de partida e de regresso.");
        if (!regresso.isAfter(partida)) throw invalido("O regresso é depois da partida.");
    }

    private void exigir(Estado esperado, String accao) {
        if (estado != esperado) throw IgrpResponseStatusException.conflict("Não é possível " + accao + " no estado em que a missão está.");
    }

    private static String texto(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }

    static IgrpResponseStatusException invalido(String m) {
        return IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY, m);
    }
}
