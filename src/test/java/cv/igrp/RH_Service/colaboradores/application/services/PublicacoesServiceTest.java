package cv.igrp.RH_Service.colaboradores.application.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cv.igrp.RH_Service.colaboradores.application.queries.GetColaboradorDetailsQueryHandler;
import cv.igrp.RH_Service.colaboradores.domain.models.FactoRh;
import cv.igrp.RH_Service.colaboradores.domain.models.PublicacaoOficial;
import cv.igrp.RH_Service.colaboradores.domain.models.TipoFactoRh;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.PublicacaoOficialRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.QualificacaoRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.shared.application.services.documentos.GeradorPdf;
import cv.igrp.RH_Service.shared.application.services.notificacoes.Notificador;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.domain.notificacoes.NotificacaoRepository;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/** BR-PUB-01..06: os actos da lei nascem dos factos, sem duplicar; ciclo; extracto em PDF. */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PublicacoesServiceTest {

    @Mock private PublicacaoOficialRepository repository;
    @Mock private FuncionarioRepository funcionarioRepository;
    @Mock private QualificacaoRepository qualificacaoRepository;
    @Mock private GetColaboradorDetailsQueryHandler detalhes;
    @Mock private EmissaoDocumentosService emissao;
    @Mock private NotificacaoRepository notificacaoRepository;
    private PublicacoesService service;
    private final FuncionarioId pessoa = FuncionarioId.gerarNovo();

    @BeforeEach
    void setUp() {
        service = new PublicacoesService(repository, funcionarioRepository, qualificacaoRepository, detalhes, emissao,
                new Notificador(notificacaoRepository));
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(funcionarioRepository.findById(any())).thenReturn(Optional.empty());
    }

    private DiarioFactos.FactoRegistado facto(TipoFactoRh tipo) {
        return new DiarioFactos.FactoRegistado(FactoRh.registar(pessoa, tipo, LocalDate.of(2026, 10, 1), null, "AFECTACAO",
                "x", "Promoção à categoria Técnico Superior", null, LocalDateTime.now()));
    }

    @Test
    void promocaoPedeANomeacaoNoBoletimOficial() {
        service.aoRegistarFacto(facto(TipoFactoRh.PROMOCAO));
        var c = ArgumentCaptor.forClass(PublicacaoOficial.class);
        verify(repository).save(c.capture());
        assertEquals(PublicacaoOficial.TipoActo.NOMEACAO, c.getValue().getTipoActo());
        assertEquals(PublicacaoOficial.Estado.A_PUBLICAR, c.getValue().getEstado());
        assertEquals("FACTO_RH", c.getValue().getReferenciaTipo());
    }

    @Test
    void factosQueNaoSaoActosPublicaveisNaoCriamNada() {
        service.aoRegistarFacto(facto(TipoFactoRh.PROGRESSAO));
        service.aoRegistarFacto(facto(TipoFactoRh.MUDANCA_SITUACAO));
        verify(repository, never()).save(any());
    }

    @Test
    void oMesmoActoNaoSeDuplica() {
        when(repository.existeParaReferencia(anyString(), anyString())).thenReturn(true);
        assertTrue(service.aPublicar(PublicacaoOficial.TipoActo.OUTRO, null, pessoa, "X", "1", "Acto", null).isEmpty());
        verify(repository, never()).save(any());
    }

    @Test
    void cicloDaPublicacao() {
        var p = PublicacaoOficial.aPublicar(PublicacaoOficial.TipoActo.CESSACAO, null, pessoa, "X", "1", "Exoneração", LocalDate.of(2026, 9, 1));
        assertEquals(PublicacaoOficial.Meio.BOLETIM_OFICIAL, p.getMeio());
        assertEquals(422, assertThrows(IgrpResponseStatusException.class,
                () -> p.publicada("II", " ", LocalDate.of(2026, 9, 10))).getStatusCode().value());
        assertEquals(422, assertThrows(IgrpResponseStatusException.class,
                () -> p.publicada("II", "40", LocalDate.of(2026, 8, 10))).getStatusCode().value());
        p.publicada("II", "40", LocalDate.of(2026, 9, 10));
        assertEquals(409, assertThrows(IgrpResponseStatusException.class, () -> p.cancelar("x")).getStatusCode().value());
    }

    @Test
    void oModeloDoExtractoDaUmPdf() {
        var g = new GeradorPdf();
        var v = new HashMap<String, Object>();
        v.put("titulo", "Extracto");
        v.put("instituicao", "Teste");
        v.put("tipoActo", "Nomeação");
        v.put("sumario", "Maria Lopes — Promoção à categoria Técnico Superior");
        v.put("dataActo", "01/10/2026");
        v.put("linhas", List.of("Carreira: Regime Geral", "Categoria: Técnico Superior"));
        v.put("numero", "EXT-2026-000001");
        v.put("codigo", "ABCDEFGH23");
        v.put("dataEmissao", "25/09/2026");
        byte[] pdf = g.pdf(g.preencher("extracto", v));
        assertEquals("%PDF", new String(pdf, 0, 4, StandardCharsets.US_ASCII));
    }
}
