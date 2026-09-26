package cv.igrp.RH_Service.formacao.domain.models;

import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.formacao.domain.valueobject.AccaoFormacaoId;
import cv.igrp.RH_Service.formacao.domain.valueobject.InscricaoFormacaoId;
import cv.igrp.RH_Service.formacao.domain.valueobject.NecessidadeFormacaoId;
import cv.igrp.RH_Service.formacao.domain.valueobject.PlanoFormacaoId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.domain.service.Datas;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * <b>Uma acção de formação</b> (Lei n.º 20/X/2023, art. 141.º; BR-FRM-03..14): tema, entidade formadora, modalidade,
 * datas, horas, vagas e, quando a Administração a custeia, o <b>prazo de garantia</b> — os meses de permanência que o
 * formando fica a dever (art. 95.º b), condiciona a exoneração voluntária). As inscrições vivem dentro da acção, porque
 * as vagas são dela.
 *
 * <p>PLANEADA → INSCRICOES_ABERTAS → EM_CURSO → CONCLUIDA; CANCELADA antes de concluir.
 */
@Getter
public class AccaoFormacao {

    public enum Modalidade { PRESENCIAL, DISTANCIA, MISTA }
    public enum Estado { PLANEADA, INSCRICOES_ABERTAS, EM_CURSO, CONCLUIDA, CANCELADA }
    /** Quem inscreveu: o próprio pede; a chefia e o RH inscrevem (já admitido). */
    public enum Origem { PROPRIO, CHEFIA, RH }
    public enum EstadoInscricao { PEDIDA, ADMITIDA, RECUSADA, DESISTIU, APROVEITAMENTO, SEM_APROVEITAMENTO, FALTOU }

    @Getter
    public static class Inscricao {
        private InscricaoFormacaoId id;
        private FuncionarioId funcionarioId;
        private Origem origem;
        private EstadoInscricao estado;
        private LocalDate data;
        private String motivo;
        private Integer diasPresenca;
        private LocalDate garantiaAte;

        private Inscricao() {}

        public static Inscricao reconstruir(InscricaoFormacaoId id, FuncionarioId funcionarioId, Origem origem, EstadoInscricao estado,
                                            LocalDate data, String motivo, Integer diasPresenca, LocalDate garantiaAte) {
            var i = new Inscricao();
            i.id = id;
            i.funcionarioId = funcionarioId;
            i.origem = origem;
            i.estado = estado;
            i.data = data;
            i.motivo = motivo;
            i.diasPresenca = diasPresenca;
            i.garantiaAte = garantiaAte;
            return i;
        }

        /** Ocupa vaga e vai à formação. */
        public boolean admitida() {
            return estado == EstadoInscricao.ADMITIDA || estado == EstadoInscricao.APROVEITAMENTO || estado == EstadoInscricao.SEM_APROVEITAMENTO
                    || estado == EstadoInscricao.FALTOU;
        }

        public boolean frequentou() {
            return estado == EstadoInscricao.APROVEITAMENTO || estado == EstadoInscricao.SEM_APROVEITAMENTO;
        }
    }

    private AccaoFormacaoId id;
    private PlanoFormacaoId planoId;
    private List<NecessidadeFormacaoId> necessidades = new ArrayList<>();
    private String tema;
    private String entidadeFormadora;
    private Modalidade modalidade;
    private boolean interna;
    private LocalDate inicio;
    private LocalDate fim;
    private Integer horas;
    private String horario;
    private String local;
    private Integer vagas;
    private BigDecimal custoPrevisto;
    private boolean custeadaPelaAdministracao;
    private Integer mesesGarantia;
    private Estado estado;
    private String motivoCancelamento;
    private List<Inscricao> inscricoes = new ArrayList<>();

    private AccaoFormacao() {}

    public static AccaoFormacao planear(String tema, String entidade, Modalidade modalidade, boolean interna, LocalDate inicio, LocalDate fim,
                                        Integer horas, String horario, String local, Integer vagas, BigDecimal custo,
                                        boolean custeada, Integer mesesGarantia, PlanoFormacaoId planoId,
                                        List<NecessidadeFormacaoId> necessidades) {
        var a = new AccaoFormacao();
        a.id = AccaoFormacaoId.gerarNovo();
        a.estado = Estado.PLANEADA;
        a.definir(tema, entidade, modalidade, interna, inicio, fim, horas, horario, local, vagas, custo, custeada, mesesGarantia);
        a.planoId = planoId;
        a.necessidades = necessidades != null ? new ArrayList<>(necessidades) : new ArrayList<>();
        return a;
    }

    public static AccaoFormacao reconstruir(AccaoFormacaoId id, PlanoFormacaoId planoId, List<NecessidadeFormacaoId> necessidades, String tema,
                                            String entidade, Modalidade modalidade, boolean interna, LocalDate inicio, LocalDate fim,
                                            Integer horas, String horario, String local, Integer vagas, BigDecimal custo, boolean custeada,
                                            Integer mesesGarantia, Estado estado, String motivoCancelamento, List<Inscricao> inscricoes) {
        var a = new AccaoFormacao();
        a.id = id;
        a.planoId = planoId;
        a.necessidades = new ArrayList<>(necessidades);
        a.tema = tema;
        a.entidadeFormadora = entidade;
        a.modalidade = modalidade;
        a.interna = interna;
        a.inicio = inicio;
        a.fim = fim;
        a.horas = horas;
        a.horario = horario;
        a.local = local;
        a.vagas = vagas;
        a.custoPrevisto = custo;
        a.custeadaPelaAdministracao = custeada;
        a.mesesGarantia = mesesGarantia;
        a.estado = estado;
        a.motivoCancelamento = motivoCancelamento;
        a.inscricoes = new ArrayList<>(inscricoes);
        return a;
    }

    /** Os termos da acção, enquanto planeada ou com as inscrições abertas (as vagas não descem abaixo dos admitidos). */
    public void definir(String tema, String entidade, Modalidade modalidade, boolean interna, LocalDate inicio, LocalDate fim, Integer horas,
                        String horario, String local, Integer vagas, BigDecimal custo, boolean custeada, Integer mesesGarantia) {
        if (estado != Estado.PLANEADA && estado != Estado.INSCRICOES_ABERTAS)
            throw IgrpResponseStatusException.conflict("A acção já começou: os seus termos não mudam.");
        if (tema == null || tema.isBlank()) throw invalido("Indique o tema da acção.");
        if (inicio == null || fim == null) throw invalido("Indique as datas de início e de fim.");
        if (fim.isBefore(inicio)) throw invalido("A acção não pode acabar antes de começar.");
        if (horas == null || horas < 1) throw invalido("Indique a duração em horas.");
        if (vagas != null && vagas < 1) throw invalido("As vagas, se as há, são pelo menos uma.");
        if (vagas != null && admitidas() > vagas) throw invalido("Já há " + admitidas() + " formandos admitidos: as vagas não podem ser menos.");
        if (custo != null && custo.signum() < 0) throw invalido("O custo não pode ser negativo.");
        if (mesesGarantia != null && (mesesGarantia < 1 || mesesGarantia > 60)) throw invalido("O prazo de garantia vai de 1 a 60 meses.");
        if (mesesGarantia != null && !custeada)
            throw invalido("O prazo de garantia só existe quando a Administração custeia a formação (art. 95.º b)).");
        this.tema = tema.trim();
        this.entidadeFormadora = texto(entidade);
        this.modalidade = modalidade != null ? modalidade : Modalidade.PRESENCIAL;
        this.interna = interna;
        this.inicio = inicio;
        this.fim = fim;
        this.horas = horas;
        this.horario = texto(horario);
        this.local = texto(local);
        this.vagas = vagas;
        this.custoPrevisto = custo;
        this.custeadaPelaAdministracao = custeada;
        this.mesesGarantia = mesesGarantia;
    }

    public List<Inscricao> getInscricoes() {
        return Collections.unmodifiableList(inscricoes);
    }

    public void abrirInscricoes() {
        exigir(Estado.PLANEADA, "abrir as inscrições");
        this.estado = Estado.INSCRICOES_ABERTAS;
    }

    /**
     * Uma inscrição (BR-FRM-06): o próprio pede (fica PEDIDA, a chefia directa ou o RH decidem); a chefia e o RH inscrevem
     * já admitido. Uma vez por pessoa; dentro das vagas.
     */
    public Inscricao inscrever(FuncionarioId funcionarioId, Origem origem, LocalDate hoje) {
        if (estado != Estado.PLANEADA && estado != Estado.INSCRICOES_ABERTAS)
            throw IgrpResponseStatusException.conflict("As inscrições desta acção estão fechadas.");
        if (origem == Origem.PROPRIO && estado != Estado.INSCRICOES_ABERTAS)
            throw IgrpResponseStatusException.conflict("As inscrições desta acção ainda não abriram.");
        if (inscricoes.stream().anyMatch(i -> i.funcionarioId.equals(funcionarioId)
                && i.estado != EstadoInscricao.RECUSADA && i.estado != EstadoInscricao.DESISTIU))
            throw IgrpResponseStatusException.conflict("Este colaborador já está inscrito nesta acção.");
        boolean admite = origem != Origem.PROPRIO;
        if (admite) exigirVaga();
        var i = Inscricao.reconstruir(InscricaoFormacaoId.gerarNovo(), funcionarioId, origem,
                admite ? EstadoInscricao.ADMITIDA : EstadoInscricao.PEDIDA, hoje, null, null, null);
        inscricoes.add(i);
        return i;
    }

    public Inscricao decidir(InscricaoFormacaoId id, boolean admitir, String motivo) {
        var i = inscricao(id);
        if (i.estado != EstadoInscricao.PEDIDA) throw IgrpResponseStatusException.conflict("Esta inscrição já foi decidida.");
        if (estado != Estado.PLANEADA && estado != Estado.INSCRICOES_ABERTAS)
            throw IgrpResponseStatusException.conflict("As inscrições desta acção estão fechadas.");
        if (admitir) exigirVaga();
        else if (motivo == null || motivo.isBlank()) throw invalido("Indique porque recusa a inscrição.");
        i.estado = admitir ? EstadoInscricao.ADMITIDA : EstadoInscricao.RECUSADA;
        i.motivo = texto(motivo);
        return i;
    }

    public Inscricao desistir(InscricaoFormacaoId id, String motivo) {
        var i = inscricao(id);
        if (i.estado != EstadoInscricao.PEDIDA && i.estado != EstadoInscricao.ADMITIDA)
            throw IgrpResponseStatusException.conflict("Esta inscrição já terminou.");
        if (estado == Estado.CONCLUIDA || estado == Estado.CANCELADA) throw IgrpResponseStatusException.conflict("A acção já terminou.");
        i.estado = EstadoInscricao.DESISTIU;
        i.motivo = texto(motivo);
        return i;
    }

    /** Começar: com pelo menos um formando admitido; os pedidos por decidir caem (recusados por não decididos a tempo). */
    public void iniciar() {
        if (estado != Estado.PLANEADA && estado != Estado.INSCRICOES_ABERTAS)
            throw IgrpResponseStatusException.conflict("Esta acção não pode começar no estado em que está.");
        if (admitidas() == 0) throw invalido("A acção não tem formandos admitidos.");
        inscricoes.stream().filter(i -> i.estado == EstadoInscricao.PEDIDA).forEach(i -> {
            i.estado = EstadoInscricao.RECUSADA;
            i.motivo = "Não decidida antes do início da acção.";
        });
        this.estado = Estado.EM_CURSO;
    }

    /**
     * A avaliação de cada formando (BR-FRM-09): aproveitamento, sem aproveitamento ou faltou, com os dias de presença. Com
     * aproveitamento numa acção custeada com garantia, fica a dever a permanência até ao fim + os meses de garantia.
     */
    public Inscricao avaliar(InscricaoFormacaoId id, EstadoInscricao resultado, Integer diasPresenca) {
        exigir(Estado.EM_CURSO, "avaliar os formandos");
        if (resultado != EstadoInscricao.APROVEITAMENTO && resultado != EstadoInscricao.SEM_APROVEITAMENTO && resultado != EstadoInscricao.FALTOU)
            throw invalido("A avaliação é: aproveitamento, sem aproveitamento ou faltou.");
        var i = inscricao(id);
        if (!i.admitida()) throw IgrpResponseStatusException.conflict("Só se avaliam formandos admitidos.");
        long diasAccao = fim.toEpochDay() - inicio.toEpochDay() + 1;
        if (diasPresenca != null && (diasPresenca < 0 || diasPresenca > diasAccao))
            throw invalido("Os dias de presença vão de 0 a " + diasAccao + ".");
        i.estado = resultado;
        i.diasPresenca = resultado == EstadoInscricao.FALTOU ? Integer.valueOf(0) : diasPresenca;
        i.garantiaAte = resultado == EstadoInscricao.APROVEITAMENTO && custeadaPelaAdministracao && mesesGarantia != null
                ? fim.plusMonths(mesesGarantia) : null;
        return i;
    }

    /** Concluir: todos os admitidos avaliados. */
    public void concluir() {
        exigir(Estado.EM_CURSO, "concluir");
        long porAvaliar = inscricoes.stream().filter(i -> i.estado == EstadoInscricao.ADMITIDA).count();
        if (porAvaliar > 0) throw invalido("Faltam avaliar " + porAvaliar + " formando(s).");
        this.estado = Estado.CONCLUIDA;
    }

    public void cancelar(String motivo) {
        if (estado == Estado.CONCLUIDA || estado == Estado.CANCELADA) throw IgrpResponseStatusException.conflict("A acção já terminou.");
        if (motivo == null || motivo.isBlank()) throw invalido("Indique o motivo do cancelamento.");
        this.motivoCancelamento = motivo.trim();
        this.estado = Estado.CANCELADA;
    }

    public long admitidas() {
        return inscricoes.stream().filter(Inscricao::admitida).count();
    }

    public Optional<Inscricao> inscricaoDe(FuncionarioId funcionarioId) {
        return inscricoes.stream().filter(i -> i.funcionarioId.equals(funcionarioId)).reduce((a, b) -> b);
    }

    public Inscricao inscricao(InscricaoFormacaoId id) {
        return inscricoes.stream().filter(i -> i.id.equals(id)).findFirst()
                .orElseThrow(() -> IgrpResponseStatusException.notFound("Inscrição não encontrada nesta acção."));
    }

    private void exigirVaga() {
        if (vagas != null && admitidas() >= vagas)
            throw IgrpResponseStatusException.conflict("A acção não tem vagas (" + vagas + ").");
    }

    private void exigir(Estado esperado, String accao) {
        if (estado != esperado) throw IgrpResponseStatusException.conflict("Não é possível " + accao + " no estado em que a acção está.");
    }

    public String periodo() {
        return inicio.equals(fim) ? Datas.pt(inicio) : Datas.pt(inicio) + " a " + Datas.pt(fim);
    }

    private static String texto(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }

    static IgrpResponseStatusException invalido(String m) {
        return IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY, m);
    }
}
