package cv.igrp.RH_Service.shared.application.services.documentos;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;

/**
 * Documentos em PDF a partir de modelos <b>Thymeleaf</b> ({@code src/main/resources/modelos/documentos/<nome>.html}).
 *
 * <p>Regras dos modelos, por causa do openhtmltopdf: <b>XHTML bem formado</b> (todas as tags fechadas,
 * {@code <br/>}) e <b>CSS 2.1 simples</b> — tabelas, margens, bordas, fontes, {@code @page}; nada de
 * flexbox, grid, variáveis CSS nem fontes da web. Os textos entram com {@code th:text} (escapados).
 *
 * <p>Os modelos de hoje são <b>de teste</b> (decisão do cliente, 2026-09-25): o modelo oficial substitui o
 * ficheiro, sem mexer no código.
 */
@Service
public class GeradorPdf {

    private final TemplateEngine motor;

    public GeradorPdf() {
        var resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("modelos/documentos/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resolver.setCacheable(true);
        this.motor = new TemplateEngine();
        this.motor.setTemplateResolver(resolver);
    }

    /** O modelo {@code nome} preenchido com as {@code variaveis}. */
    public String preencher(String nome, Map<String, Object> variaveis) {
        try {
            return motor.process(nome, new Context(Locale.forLanguageTag("pt-PT"), variaveis));
        } catch (Exception e) {
            throw IgrpResponseStatusException.of(HttpStatus.INTERNAL_SERVER_ERROR, "Não foi possível preparar o documento (" + nome + ").");
        }
    }

    /** O HTML (XHTML bem formado) em PDF. */
    public byte[] pdf(String html) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.withHtmlContent(html, null);
            builder.toStream(out);
            builder.run();
            return out.toByteArray();
        } catch (Exception e) {
            throw IgrpResponseStatusException.of(HttpStatus.INTERNAL_SERVER_ERROR, "Não foi possível gerar o PDF do documento.");
        }
    }
}
