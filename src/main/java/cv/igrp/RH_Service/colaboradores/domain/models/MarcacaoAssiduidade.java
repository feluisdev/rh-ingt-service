package cv.igrp.RH_Service.colaboradores.domain.models;

import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.MarcacaoAssiduidadeId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Uma marcação de assiduidade — o registo do art. 164.º n.º 3 da Lei n.º 20/X/2023 guarda-se
 * como a <b>prova</b> do que aconteceu: cada marcação fica como veio e <b>nunca se apaga</b>.
 * Uma correcção é uma marcação nova, com motivo; a errada fica <b>anulada</b>, com quem e porquê,
 * mas visível. Os períodos do dia calculam-se a partir das válidas ({@link DiaAssiduidade}).
 */
@Getter
public class MarcacaoAssiduidade {

    private MarcacaoAssiduidadeId id;
    private FuncionarioId funcionarioId;
    private LocalDateTime momento;
    private SentidoMarcacao sentido;
    private OrigemMarcacao origem;
    /** Obrigatório quando a marcação corrige um dia que já tinha marcações. */
    private String motivo;
    /** Identificador no sistema de origem (relógio): uma importação repetida não duplica. */
    private String referenciaExterna;
    private boolean anulada;
    private String motivoAnulacao;
    private LocalDateTime anuladaEm;

    private MarcacaoAssiduidade() {}

    public static MarcacaoAssiduidade registar(FuncionarioId funcionarioId, LocalDateTime momento,
                                               SentidoMarcacao sentido, OrigemMarcacao origem,
                                               String motivo, String referenciaExterna, LocalDateTime agora) {
        Objects.requireNonNull(funcionarioId);
        Objects.requireNonNull(origem);
        if (momento == null) throw invalido("A data e hora da marcação são obrigatórias.");
        if (sentido == null) throw invalido("O sentido da marcação é obrigatório: ENTRADA ou SAIDA.");
        // Regista-se o que aconteceu, não o que se prevê.
        if (momento.isAfter(agora)) throw invalido("Não se regista uma marcação no futuro: " + momento + ".");

        var m = new MarcacaoAssiduidade();
        m.id = MarcacaoAssiduidadeId.gerarNovo();
        m.funcionarioId = funcionarioId;
        m.momento = momento.withSecond(0).withNano(0);
        m.sentido = sentido;
        m.origem = origem;
        m.motivo = texto(motivo);
        m.referenciaExterna = texto(referenciaExterna);
        return m;
    }

    public static MarcacaoAssiduidade reconstruir(MarcacaoAssiduidadeId id, FuncionarioId funcionarioId,
                                                  LocalDateTime momento, SentidoMarcacao sentido,
                                                  OrigemMarcacao origem, String motivo, String referenciaExterna,
                                                  boolean anulada, String motivoAnulacao, LocalDateTime anuladaEm) {
        var m = new MarcacaoAssiduidade();
        m.id = id;
        m.funcionarioId = funcionarioId;
        m.momento = momento;
        m.sentido = sentido;
        m.origem = origem;
        m.motivo = motivo;
        m.referenciaExterna = referenciaExterna;
        m.anulada = anulada;
        m.motivoAnulacao = motivoAnulacao;
        m.anuladaEm = anuladaEm;
        return m;
    }

    /** A marcação fica, anulada: é prova do que foi picado. O motivo é obrigatório. */
    public void anular(String motivo, LocalDateTime agora) {
        if (anulada) throw IgrpResponseStatusException.conflict("A marcação já está anulada.");
        if (motivo == null || motivo.isBlank()) throw invalido("Anular uma marcação exige motivo.");
        this.anulada = true;
        this.motivoAnulacao = motivo.trim();
        this.anuladaEm = agora;
    }

    private static String texto(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }

    static IgrpResponseStatusException invalido(String mensagem) {
        return IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY, mensagem);
    }
}
