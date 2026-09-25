package cv.igrp.RH_Service.colaboradores.application.services;

import cv.igrp.RH_Service.colaboradores.domain.models.DocumentoEmitido;
import cv.igrp.RH_Service.colaboradores.domain.models.Funcionario;
import cv.igrp.RH_Service.colaboradores.domain.models.TipoDocumentoEmitido;
import cv.igrp.RH_Service.colaboradores.domain.repository.DocumentoEmitidoRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.DocumentoEmitidoId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.shared.application.constants.DocumentoFolder;
import cv.igrp.RH_Service.shared.application.services.documentos.GeradorPdf;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.domain.service.Datas;
import cv.igrp.RH_Service.shared.domain.service.DocumentoService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * <b>Emitir um documento do RH</b> (BR-DEC-05..08): numera-o na série e no ano (sem saltos nem repetidos,
 * com a série trancada), dá-lhe um código de verificação, preenche o modelo, gera o PDF e guarda-o no
 * <b>MinIO</b>. Na base ficam só os metadados e o SHA-256 do PDF. Serve as declarações, o cartão
 * profissional e os extractos para publicação.
 */
@Service
public class EmissaoDocumentosService {

    /** Sem letras que se confundem (O/0, I/1, L). */
    private static final String ALFABETO = "ABCDEFGHJKMNPQRSTUVWXYZ23456789";
    private static final int TAMANHO_CODIGO = 10;
    private static final SecureRandom ALEATORIO = new SecureRandom();

    /** O que uma verificação pública responde: o essencial para confirmar que o documento é verdadeiro. */
    public record Verificacao(boolean existe, boolean anulado, DocumentoEmitido documento, String titular) {}

    private final DocumentoEmitidoRepository repository;
    private final FuncionarioRepository funcionarioRepository;
    private final GeradorPdf geradorPdf;
    private final DocumentoService documentoService;
    private final String instituicao;
    private final String urlVerificacao;

    public EmissaoDocumentosService(DocumentoEmitidoRepository repository, FuncionarioRepository funcionarioRepository,
                                    GeradorPdf geradorPdf, DocumentoService documentoService,
                                    @Value("${rh.documentos.instituicao:Instituição (modelo de teste)}") String instituicao,
                                    @Value("${rh.documentos.url-verificacao:/api/v1/rh/verificacao/documentos/}") String urlVerificacao) {
        this.repository = repository;
        this.funcionarioRepository = funcionarioRepository;
        this.geradorPdf = geradorPdf;
        this.documentoService = documentoService;
        this.instituicao = instituicao;
        this.urlVerificacao = urlVerificacao;
    }

    /**
     * Emite. {@code valores} preenche o {@code modelo}; o serviço junta {@code numero}, {@code codigo},
     * {@code dataEmissao}, {@code urlVerificacao} e {@code instituicao}.
     */
    @Transactional
    public DocumentoEmitido emitir(TipoDocumentoEmitido tipo, FuncionarioId funcionarioId, String titulo, String modelo,
                                   Map<String, Object> valores, String referenciaTipo, String referenciaId) {
        LocalDateTime agora = agora();
        int ano = agora.getYear();
        String numero = "%s-%d-%06d".formatted(tipo.getSerie(), ano, repository.proximoNumero(tipo.getSerie(), ano));
        String codigo = novoCodigo();
        Map<String, Object> v = new HashMap<>(valores);
        v.put("numero", numero);
        v.put("codigo", codigo);
        v.put("dataEmissao", Datas.pt(agora.toLocalDate()));
        v.put("urlVerificacao", urlVerificacao + codigo);
        v.put("instituicao", instituicao);
        v.put("titulo", titulo);
        byte[] pdf = geradorPdf.pdf(geradorPdf.preencher(modelo, v));
        String ficheiro = documentoService.guardarGerado(DocumentoFolder.DOCUMENTOS_EMITIDOS, numero + ".pdf", pdf, "application/pdf");
        return repository.save(DocumentoEmitido.emitir(tipo, numero, funcionarioId, titulo, codigo, ficheiro, pdf,
                referenciaTipo, referenciaId, agora));
    }

    /** O link (assinado, temporário) para descarregar o PDF do MinIO. */
    @Transactional(readOnly = true)
    public String link(DocumentoEmitidoId id, FuncionarioId soDe) {
        var d = repository.findById(id).filter(x -> soDe == null || soDe.equals(x.getFuncionarioId()))
                .orElseThrow(() -> IgrpResponseStatusException.notFound("Documento não encontrado."));
        var r = documentoService.getPresignedLink(d.getFicheiro());
        return r.getBody() != null ? r.getBody().getUrl() : null;
    }

    @Transactional
    public DocumentoEmitido anular(DocumentoEmitidoId id, String motivo) {
        var d = repository.findById(id).orElseThrow(() -> IgrpResponseStatusException.notFound("Documento não encontrado."));
        d.anular(motivo, agora());
        return repository.save(d);
    }

    /** Pelo código impresso no documento: existe? foi anulado? de quem é? */
    @Transactional(readOnly = true)
    public Verificacao verificar(String codigo) {
        String c = codigo == null ? "" : codigo.trim().toUpperCase();
        return repository.findByCodigo(c)
                .map(d -> new Verificacao(true, d.isAnulado(), d, d.getFuncionarioId() == null ? null
                        : funcionarioRepository.findById(d.getFuncionarioId()).map(Funcionario::getNomeCompleto).orElse(null)))
                .orElse(new Verificacao(false, false, null, null));
    }

    private String novoCodigo() {
        for (int tentativa = 0; tentativa < 5; tentativa++) {
            StringBuilder sb = new StringBuilder(TAMANHO_CODIGO);
            for (int i = 0; i < TAMANHO_CODIGO; i++) sb.append(ALFABETO.charAt(ALEATORIO.nextInt(ALFABETO.length())));
            if (!repository.existsByCodigo(sb.toString())) return sb.toString();
        }
        throw new IllegalStateException("Não foi possível gerar um código de verificação único.");
    }

    LocalDateTime agora() { return LocalDateTime.now(); }
}
