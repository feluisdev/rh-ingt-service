package cv.igrp.RH_Service.colaboradores.domain.models;

import cv.igrp.RH_Service.colaboradores.domain.valueobject.AcidenteServicoId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * <b>Acidente em serviço ou doença profissional</b> (Lei n.º 20/X/2023, arts. 187.º–191.º; BR-SST-01..10): a participação,
 * a qualificação (se é em serviço — art. 187.º n.º 3), os períodos de incapacidade temporária (faltas justificadas sem perda
 * de direitos nem de remuneração — arts. 188.º e 189.º n.º 2), a alta e a incapacidade permanente (aposentação por
 * invalidez — art. 189.º n.os 3 e 4). A seguradora, se o serviço transferiu a responsabilidade (art. 191.º).
 *
 * <p>PARTICIPADO → QUALIFICADO / NAO_QUALIFICADO → ENCERRADO.
 */
@Getter
public class AcidenteServico {

    public enum Tipo { ACIDENTE_SERVICO, ACIDENTE_TRAJECTO, DOENCA_PROFISSIONAL }
    public enum Estado { PARTICIPADO, QUALIFICADO, NAO_QUALIFICADO, ENCERRADO }
    public enum TipoIncapacidade { TEMPORARIA_ABSOLUTA, TEMPORARIA_PARCIAL }

    /** Prazo para participar [ind.]: 2 dias úteis (o diploma de desenvolvimento pode fixar outro). */
    public static final int DIAS_UTEIS_PARTICIPACAO = 2;

    public record Incapacidade(UUID id, TipoIncapacidade tipo, LocalDate inicio, LocalDate fim) {
        public boolean abrange(LocalDate d) {
            return !d.isBefore(inicio) && (fim == null || !d.isAfter(fim));
        }
    }

    private AcidenteServicoId id;
    private FuncionarioId funcionarioId;
    private Tipo tipo;
    private LocalDateTime dataHora;
    private String local;
    private String descricao;
    private String testemunhas;
    private LocalDate dataParticipacao;
    private boolean participadoPeloProprio;
    private Estado estado;
    private String despacho;
    private String motivo;
    private String seguradora;
    private String apolice;
    private LocalDate participacaoSeguradora;
    private List<Incapacidade> incapacidades = new ArrayList<>();
    private LocalDate alta;
    private BigDecimal incapacidadePermanente;
    private boolean incapacidadeAbsoluta;

    private AcidenteServico() {}

    public static AcidenteServico participar(FuncionarioId funcionarioId, Tipo tipo, LocalDateTime dataHora, String local, String descricao,
                                             String testemunhas, LocalDate dataParticipacao, boolean peloProprio, LocalDate hoje) {
        if (tipo == null) throw invalido("Diga se é acidente em serviço, de trajecto ou doença profissional.");
        if (dataHora == null) throw invalido("Indique a data e a hora do acidente (ou do diagnóstico da doença).");
        if (dataHora.toLocalDate().isAfter(hoje)) throw invalido("A data do acidente não pode ser no futuro.");
        if (descricao == null || descricao.isBlank()) throw invalido("Descreva o que aconteceu.");
        LocalDate participacao = dataParticipacao != null ? dataParticipacao : hoje;
        if (participacao.isBefore(dataHora.toLocalDate()) || participacao.isAfter(hoje))
            throw invalido("A participação é depois do acidente e não no futuro.");
        var a = new AcidenteServico();
        a.id = AcidenteServicoId.gerarNovo();
        a.funcionarioId = funcionarioId;
        a.tipo = tipo;
        a.dataHora = dataHora;
        a.local = texto(local);
        a.descricao = descricao.trim();
        a.testemunhas = texto(testemunhas);
        a.dataParticipacao = participacao;
        a.participadoPeloProprio = peloProprio;
        a.estado = Estado.PARTICIPADO;
        return a;
    }

    public static AcidenteServico reconstruir(AcidenteServicoId id, FuncionarioId funcionarioId, Tipo tipo, LocalDateTime dataHora, String local,
                                              String descricao, String testemunhas, LocalDate dataParticipacao, boolean peloProprio, Estado estado,
                                              String despacho, String motivo, String seguradora, String apolice, LocalDate participacaoSeguradora,
                                              List<Incapacidade> incapacidades, LocalDate alta, BigDecimal incapacidadePermanente,
                                              boolean incapacidadeAbsoluta) {
        var a = new AcidenteServico();
        a.id = id;
        a.funcionarioId = funcionarioId;
        a.tipo = tipo;
        a.dataHora = dataHora;
        a.local = local;
        a.descricao = descricao;
        a.testemunhas = testemunhas;
        a.dataParticipacao = dataParticipacao;
        a.participadoPeloProprio = peloProprio;
        a.estado = estado;
        a.despacho = despacho;
        a.motivo = motivo;
        a.seguradora = seguradora;
        a.apolice = apolice;
        a.participacaoSeguradora = participacaoSeguradora;
        a.incapacidades = new ArrayList<>(incapacidades);
        a.incapacidades.sort(Comparator.comparing(Incapacidade::inicio));
        a.alta = alta;
        a.incapacidadePermanente = incapacidadePermanente;
        a.incapacidadeAbsoluta = incapacidadeAbsoluta;
        return a;
    }

    public List<Incapacidade> getIncapacidades() {
        return Collections.unmodifiableList(incapacidades);
    }

    /** Qualificar: é acidente em serviço (ou doença profissional), por despacho; ou não é, com o motivo (art. 187.º n.º 3). */
    public void qualificar(boolean emServico, String despacho, String motivo) {
        if (estado != Estado.PARTICIPADO) throw IgrpResponseStatusException.conflict("Este acidente já foi qualificado.");
        if (emServico && (despacho == null || despacho.isBlank())) throw invalido("Indique o despacho que qualifica o acidente.");
        if (!emServico && (motivo == null || motivo.isBlank())) throw invalido("Indique porque não é acidente em serviço.");
        this.despacho = texto(despacho);
        this.motivo = texto(motivo);
        this.estado = emServico ? Estado.QUALIFICADO : Estado.NAO_QUALIFICADO;
    }

    /** A seguradora a quem se participou (art. 191.º). */
    public void registarSeguradora(String seguradora, String apolice, LocalDate participacao) {
        if (seguradora == null || seguradora.isBlank()) throw invalido("Indique a seguradora.");
        this.seguradora = seguradora.trim();
        this.apolice = texto(apolice);
        this.participacaoSeguradora = participacao;
    }

    /** Um período de incapacidade temporária (fim em aberto até à alta), a partir do acidente, sem sobreposição. */
    public Incapacidade registarIncapacidade(TipoIncapacidade tipo, LocalDate inicio, LocalDate fim) {
        if (estado == Estado.NAO_QUALIFICADO || estado == Estado.ENCERRADO)
            throw IgrpResponseStatusException.conflict("Só se registam incapacidades num acidente participado ou qualificado.");
        if (inicio == null) throw invalido("Indique o início da incapacidade.");
        if (inicio.isBefore(dataHora.toLocalDate())) throw invalido("A incapacidade não começa antes do acidente.");
        if (fim != null && fim.isBefore(inicio)) throw invalido("O fim da incapacidade não pode ser antes do início.");
        if (alta != null && inicio.isAfter(alta)) throw invalido("Depois da alta não há incapacidade temporária; reabra com uma recaída.");
        for (var i : incapacidades) {
            boolean sobrepoe = (i.fim() == null || !inicio.isAfter(i.fim())) && (fim == null || !fim.isBefore(i.inicio()));
            if (sobrepoe) throw invalido("Há outro período de incapacidade nesses dias.");
        }
        var i = new Incapacidade(UUID.randomUUID(), tipo != null ? tipo : TipoIncapacidade.TEMPORARIA_ABSOLUTA, inicio, fim);
        incapacidades.add(i);
        incapacidades.sort(Comparator.comparing(Incapacidade::inicio));
        return i;
    }

    /** A alta: fecha o período em aberto na véspera. */
    public void darAlta(LocalDate data) {
        if (data == null) throw invalido("Indique a data da alta.");
        if (data.isBefore(dataHora.toLocalDate())) throw invalido("A alta não pode ser antes do acidente.");
        Optional<Incapacidade> aberta = incapacidades.stream().filter(i -> i.fim() == null).findFirst();
        aberta.ifPresent(i -> {
            if (!data.isAfter(i.inicio())) throw invalido("A alta é depois do início da incapacidade.");
            incapacidades.set(incapacidades.indexOf(i), new Incapacidade(i.id(), i.tipo(), i.inicio(), data.minusDays(1)));
        });
        this.alta = data;
    }

    /**
     * A incapacidade permanente, em percentagem (art. 189.º n.os 3 e 4): absoluta, ou parcial que não deixa exercer as funções,
     * dá direito a aposentação. Devolve se é esse o caso.
     */
    public boolean registarIncapacidadePermanente(BigDecimal percentagem, boolean absoluta, boolean impedeFuncoes) {
        if (estado != Estado.QUALIFICADO) throw IgrpResponseStatusException.conflict("Só num acidente qualificado como em serviço.");
        if (percentagem == null || percentagem.signum() <= 0 || percentagem.compareTo(BigDecimal.valueOf(100)) > 0)
            throw invalido("A incapacidade permanente vai de 0 a 100 %.");
        this.incapacidadePermanente = percentagem;
        this.incapacidadeAbsoluta = absoluta;
        return absoluta || impedeFuncoes;
    }

    public void encerrar() {
        if (estado == Estado.PARTICIPADO) throw IgrpResponseStatusException.conflict("Qualifique o acidente antes de o encerrar.");
        if (estado == Estado.ENCERRADO) throw IgrpResponseStatusException.conflict("Este acidente já está encerrado.");
        if (incapacidades.stream().anyMatch(i -> i.fim() == null)) throw invalido("Há uma incapacidade em aberto: registe a alta primeiro.");
        this.estado = Estado.ENCERRADO;
    }

    /** Os dias justificados como acidente em serviço (só depois de qualificado). */
    public boolean contaComoAcidente() {
        return estado == Estado.QUALIFICADO || (estado == Estado.ENCERRADO && despacho != null);
    }

    private static String texto(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }

    static IgrpResponseStatusException invalido(String m) {
        return IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY, m);
    }
}
