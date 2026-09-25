package cv.igrp.RH_Service.colaboradores.domain.models;

import cv.igrp.RH_Service.colaboradores.domain.valueobject.DocumentoEmitidoId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.PublicacaoOficialId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.util.Objects;

/**
 * <b>Um acto a publicar</b> (Lei n.º 20/X/2023, arts. 89.º e 90.º; Estatuto Disciplinar art. 15.º n.º 2;
 * BR-PUB-01..06): no Boletim Oficial, por extracto, ou na página electrónica. Nasce A_PUBLICAR — muitos
 * sozinhos, dos factos do RH —, o RH gera o extracto e regista a publicação (série, número, data).
 */
@Getter
public class PublicacaoOficial {

    /** Os actos que a lei manda publicar (e OUTRO, para o que a instituição publique além deles). */
    public enum TipoActo {
        /** Art. 89.º n.º 1 a) e b): nomeação, contrato por tempo indeterminado — a entrada ao serviço. */
        PROVIMENTO,
        /** Nomeação para um lugar de acesso (promoção, mudança de carreira — art. 58.º n.º 3). */
        NOMEACAO,
        /** Art. 89.º n.º 1 a) e b): mobilidade definitiva ou transitória. */
        MOBILIDADE,
        /** Art. 89.º n.º 1 c) e art. 90.º n.º 1 c): comissões de serviço e renovações. */
        COMISSAO_SERVICO,
        /** Art. 89.º n.º 1 d). */
        CONTRATO_GESTAO,
        /** Art. 89.º n.º 1 e): cessação da relação de emprego público. */
        CESSACAO,
        /** Art. 94.º n.º 5: o despacho de exoneração. */
        EXONERACAO,
        /** Estatuto Disciplinar art. 15.º n.º 2: penas de aposentação compulsiva e demissão. */
        PENA_DISCIPLINAR,
        /** Estatuto Disciplinar art. 95.º n.º 7. */
        REABILITACAO,
        /** DL n.º 3/2010, art. 71.º n.º 2. */
        LISTA_ANTIGUIDADE,
        /** Lei 20, art. 125.º (publicidade): aviso de abertura e lista homologada do concurso. */
        CONCURSO,
        OUTRO
    }

    public enum Meio { BOLETIM_OFICIAL, PAGINA_ELECTRONICA }

    public enum Estado { A_PUBLICAR, PUBLICADA, CANCELADA }

    private PublicacaoOficialId id;
    private TipoActo tipoActo;
    private Meio meio;
    private FuncionarioId funcionarioId;
    private String referenciaTipo;
    private String referenciaId;
    /** O extracto: o que se publica (texto do acto). */
    private String sumario;
    private LocalDate dataActo;
    private Estado estado;
    private DocumentoEmitidoId extractoId;
    private String serie;
    private String numero;
    private LocalDate dataPublicacao;
    private String motivoCancelamento;

    private PublicacaoOficial() {}

    public static PublicacaoOficial aPublicar(TipoActo tipoActo, Meio meio, FuncionarioId funcionarioId, String referenciaTipo,
                                              String referenciaId, String sumario, LocalDate dataActo) {
        if (tipoActo == null) throw invalido("Indique o tipo do acto a publicar.");
        if (sumario == null || sumario.isBlank()) throw invalido("Indique o sumário do acto a publicar.");
        var p = new PublicacaoOficial();
        p.id = PublicacaoOficialId.gerarNovo();
        p.tipoActo = tipoActo;
        p.meio = meio != null ? meio : Meio.BOLETIM_OFICIAL;
        p.funcionarioId = funcionarioId;
        p.referenciaTipo = referenciaTipo;
        p.referenciaId = referenciaId;
        p.sumario = sumario.trim();
        p.dataActo = Objects.requireNonNull(dataActo);
        p.estado = Estado.A_PUBLICAR;
        return p;
    }

    public static PublicacaoOficial reconstruir(PublicacaoOficialId id, TipoActo tipoActo, Meio meio, FuncionarioId funcionarioId,
                                                String referenciaTipo, String referenciaId, String sumario, LocalDate dataActo,
                                                Estado estado, DocumentoEmitidoId extractoId, String serie, String numero,
                                                LocalDate dataPublicacao, String motivoCancelamento) {
        var p = new PublicacaoOficial();
        p.id = id;
        p.tipoActo = tipoActo;
        p.meio = meio;
        p.funcionarioId = funcionarioId;
        p.referenciaTipo = referenciaTipo;
        p.referenciaId = referenciaId;
        p.sumario = sumario;
        p.dataActo = dataActo;
        p.estado = estado;
        p.extractoId = extractoId;
        p.serie = serie;
        p.numero = numero;
        p.dataPublicacao = dataPublicacao;
        p.motivoCancelamento = motivoCancelamento;
        return p;
    }

    public void extractoGerado(DocumentoEmitidoId documento) {
        exigirAPublicar();
        this.extractoId = Objects.requireNonNull(documento);
    }

    public void publicada(String serie, String numero, LocalDate data) {
        exigirAPublicar();
        if (data == null) throw invalido("Indique a data da publicação.");
        if (meio == Meio.BOLETIM_OFICIAL && (numero == null || numero.isBlank()))
            throw invalido("Indique o número do Boletim Oficial.");
        if (data.isBefore(dataActo)) throw invalido("A publicação não pode ser anterior ao acto.");
        this.serie = serie == null || serie.isBlank() ? null : serie.trim();
        this.numero = numero == null || numero.isBlank() ? null : numero.trim();
        this.dataPublicacao = data;
        this.estado = Estado.PUBLICADA;
    }

    public void cancelar(String motivo) {
        exigirAPublicar();
        if (motivo == null || motivo.isBlank()) throw invalido("Cancelar a publicação exige o motivo.");
        this.motivoCancelamento = motivo.trim();
        this.estado = Estado.CANCELADA;
    }

    private void exigirAPublicar() {
        if (estado != Estado.A_PUBLICAR)
            throw IgrpResponseStatusException.conflict("Esta publicação já foi registada ou cancelada.");
    }

    private static IgrpResponseStatusException invalido(String m) {
        return IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY, m);
    }
}
