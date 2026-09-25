package cv.igrp.RH_Service.colaboradores.domain.models;

import cv.igrp.RH_Service.colaboradores.domain.valueobject.DocumentoEmitidoId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Objects;

/**
 * <b>Um documento emitido pelo RH</b> — declaração, cartão profissional, extracto para publicação
 * (BR-DEC-05..08). O PDF gera-se uma vez, no acto de emitir, e fica no <b>MinIO</b>
 * ({@link #ficheiro}); a base guarda só os metadados: o número da série, o código de verificação e a
 * impressão digital (SHA-256) do PDF, que prova que o ficheiro não foi trocado. É <b>imutável</b>: um
 * documento errado <b>anula-se</b> (com motivo) e emite-se outro — não se corrige.
 */
@Getter
public class DocumentoEmitido {

    private DocumentoEmitidoId id;
    private TipoDocumentoEmitido tipo;
    private String numero;
    private FuncionarioId funcionarioId;
    private String titulo;
    private LocalDateTime emitidoEm;
    private String codigoVerificacao;
    /** O caminho do PDF no MinIO. */
    private String ficheiro;
    /** SHA-256 do PDF, em hexadecimal. */
    private String impressaoDigital;
    private String referenciaTipo;
    private String referenciaId;
    private LocalDateTime anuladoEm;
    private String motivoAnulacao;

    private DocumentoEmitido() {}

    public static DocumentoEmitido emitir(TipoDocumentoEmitido tipo, String numero, FuncionarioId funcionarioId, String titulo,
                                          String codigoVerificacao, String ficheiro, byte[] pdf, String referenciaTipo,
                                          String referenciaId, LocalDateTime agora) {
        var d = new DocumentoEmitido();
        d.id = DocumentoEmitidoId.gerarNovo();
        d.tipo = Objects.requireNonNull(tipo);
        d.numero = Objects.requireNonNull(numero);
        d.funcionarioId = funcionarioId;
        d.titulo = titulo;
        d.emitidoEm = Objects.requireNonNull(agora);
        d.codigoVerificacao = Objects.requireNonNull(codigoVerificacao);
        d.ficheiro = Objects.requireNonNull(ficheiro);
        d.impressaoDigital = sha256(Objects.requireNonNull(pdf));
        d.referenciaTipo = referenciaTipo;
        d.referenciaId = referenciaId;
        return d;
    }

    public static DocumentoEmitido reconstruir(DocumentoEmitidoId id, TipoDocumentoEmitido tipo, String numero,
                                               FuncionarioId funcionarioId, String titulo, LocalDateTime emitidoEm,
                                               String codigoVerificacao, String ficheiro, String impressaoDigital,
                                               String referenciaTipo, String referenciaId, LocalDateTime anuladoEm,
                                               String motivoAnulacao) {
        var d = new DocumentoEmitido();
        d.id = id;
        d.tipo = tipo;
        d.numero = numero;
        d.funcionarioId = funcionarioId;
        d.titulo = titulo;
        d.emitidoEm = emitidoEm;
        d.codigoVerificacao = codigoVerificacao;
        d.ficheiro = ficheiro;
        d.impressaoDigital = impressaoDigital;
        d.referenciaTipo = referenciaTipo;
        d.referenciaId = referenciaId;
        d.anuladoEm = anuladoEm;
        d.motivoAnulacao = motivoAnulacao;
        return d;
    }

    public void anular(String motivo, LocalDateTime agora) {
        if (isAnulado()) throw IgrpResponseStatusException.conflict("Este documento já foi anulado.");
        if (motivo == null || motivo.isBlank())
            throw IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY, "Anular um documento emitido exige o motivo.");
        this.motivoAnulacao = motivo.trim();
        this.anuladoEm = agora;
    }

    public boolean isAnulado() {
        return anuladoEm != null;
    }

    /** Estes bytes são o PDF que foi emitido? */
    public boolean eOFicheiroEmitido(byte[] pdf) {
        return pdf != null && sha256(pdf).equals(impressaoDigital);
    }

    public static String sha256(byte[] b) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(b));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
