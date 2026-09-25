package cv.igrp.RH_Service.colaboradores.application.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cv.igrp.RH_Service.colaboradores.application.dto.ColaboradorDetailsResponseDTO;
import cv.igrp.RH_Service.colaboradores.application.queries.GetColaboradorDetailsQueryHandler;
import cv.igrp.RH_Service.colaboradores.domain.models.DocumentoEmitido;
import cv.igrp.RH_Service.colaboradores.domain.models.Funcionario;
import cv.igrp.RH_Service.colaboradores.domain.models.PedidoDeclaracao;
import cv.igrp.RH_Service.colaboradores.domain.models.TipoDocumentoEmitido;
import cv.igrp.RH_Service.colaboradores.domain.repository.DocumentoEmitidoRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.PedidoDeclaracaoRepository;
import cv.igrp.RH_Service.colaboradores.domain.service.CalculadoraAntiguidade;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.shared.application.constants.DocumentoFolder;
import cv.igrp.RH_Service.shared.application.services.documentos.GeradorPdf;
import cv.igrp.RH_Service.shared.application.services.notificacoes.Notificador;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.domain.notificacoes.Notificacao;
import cv.igrp.RH_Service.shared.domain.notificacoes.NotificacaoRepository;
import cv.igrp.RH_Service.shared.domain.notificacoes.PerfilDestino;
import cv.igrp.RH_Service.shared.domain.service.DocumentoService;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
import org.springframework.http.ResponseEntity;

/** BR-DEC-01..08: pedido, emissao numerada com PDF no MinIO (so metadados na base), recusa, anulacao, verificacao. */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DeclaracoesTest {

    private static final LocalDate HOJE = LocalDate.of(2026, 9, 25);

    @Mock private PedidoDeclaracaoRepository pedidoRepository;
    @Mock private FuncionarioRepository funcionarioRepository;
    @Mock private DocumentoEmitidoRepository documentoRepository;
    @Mock private DocumentoService documentoService;
    @Mock private GetColaboradorDetailsQueryHandler detalhes;
    @Mock private AntiguidadeService antiguidadeService;
    @Mock private ListaAntiguidadeService listaAntiguidadeService;
    @Mock private NotificacaoRepository notificacaoRepository;

    private EmissaoDocumentosService emissao;
    private DeclaracoesService service;
    private final FuncionarioId id = FuncionarioId.gerarNovo();
    private Funcionario pessoa;

    @BeforeEach
    void setUp() {
        emissao = new EmissaoDocumentosService(documentoRepository, funcionarioRepository, new GeradorPdf(), documentoService,
                "Instituicao de teste", "/api/v1/rh/verificacao/documentos/") {
            @Override LocalDateTime agora() { return HOJE.atTime(10, 0); }
        };
        service = new DeclaracoesService(pedidoRepository, funcionarioRepository, emissao, detalhes, antiguidadeService,
                listaAntiguidadeService, documentoRepository, new Notificador(notificacaoRepository)) {
            @Override LocalDate hoje() { return HOJE; }
        };
        pessoa = mock(Funcionario.class);
        when(pessoa.getId()).thenReturn(id);
        when(pessoa.getIsActive()).thenReturn(true);
        when(pessoa.getNomeCompleto()).thenReturn("Maria Lopes");
        when(pessoa.getNumeroFuncionario()).thenReturn("0000002");
        when(pessoa.getDataAdmissao()).thenReturn(LocalDate.of(2010, 1, 4));
        when(funcionarioRepository.findById(id)).thenReturn(Optional.of(pessoa));
        when(pedidoRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(documentoRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(documentoRepository.proximoNumero(anyString(), anyInt())).thenReturn(7);
        when(documentoService.guardarGerado(any(), anyString(), any(), anyString())).thenReturn("documentos_emitidos/DEC.pdf");
        when(notificacaoRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(detalhes.handle(any())).thenReturn(ResponseEntity.ok(new ColaboradorDetailsResponseDTO()));
        when(antiguidadeService.calcular(eq(id), any())).thenReturn(new CalculadoraAntiguidade.Antiguidade(
                LocalDate.of(2010, 1, 4), HOJE, 6100, 0, 6100, 16, 8, 21, List.of()));
    }

    @Test
    void peloProprioFicaPorEmitirEAvisaORh() {
        var r = service.pedir(id, PedidoDeclaracao.Tipo.VINCULO, "efeitos de credito bancario", true);
        assertEquals(PedidoDeclaracao.Estado.PEDIDA, r.pedido().getEstado());
        var c = ArgumentCaptor.forClass(Notificacao.class);
        verify(notificacaoRepository).save(c.capture());
        assertEquals(PerfilDestino.RH, c.getValue().getPerfil());
        verify(documentoService, never()).guardarGerado(any(), any(), any(), any());
    }

    @Test
    void peloRhEmiteLogoOPdfVaiParaOMinioEABaseSoGuardaMetadados() {
        var r = service.pedir(id, PedidoDeclaracao.Tipo.TEMPO_SERVICO, null, false);
        assertEquals(PedidoDeclaracao.Estado.EMITIDA, r.pedido().getEstado());
        DocumentoEmitido d = r.documento();
        assertEquals("DEC-2026-000007", d.getNumero());
        assertEquals(TipoDocumentoEmitido.DECLARACAO, d.getTipo());
        assertEquals(10, d.getCodigoVerificacao().length());
        assertEquals("documentos_emitidos/DEC.pdf", d.getFicheiro());
        var bytes = ArgumentCaptor.forClass(byte[].class);
        verify(documentoService).guardarGerado(eq(DocumentoFolder.DOCUMENTOS_EMITIDOS), eq("DEC-2026-000007.pdf"), bytes.capture(), eq("application/pdf"));
        assertTrue(d.eOFicheiroEmitido(bytes.getValue()), "a impressao digital e a do PDF enviado");
        assertEquals(64, d.getImpressaoDigital().length());
    }

    @Test
    void tempoDeServicoDizAnosMesesEDias() {
        var ps = service.paragrafos(PedidoDeclaracao.Tipo.TEMPO_SERVICO, pessoa);
        assertTrue(ps.get(0).contains("16 anos, 8 meses e 21 dias de serviço (6100 dias)"), ps.get(0));
    }

    @Test
    void vinculoDeQuemJaSaiuNaoSeEmite() {
        when(pessoa.getIsActive()).thenReturn(false);
        var e = assertThrows(IgrpResponseStatusException.class, () -> service.paragrafos(PedidoDeclaracao.Tipo.VINCULO, pessoa));
        assertEquals(422, e.getStatusCode().value());
    }

    @Test
    void recusaExigeMotivoESoUmaVez() {
        var p = PedidoDeclaracao.pedir(id, PedidoDeclaracao.Tipo.VINCULO, null, true, HOJE);
        when(pedidoRepository.findById(p.getId())).thenReturn(Optional.of(p));
        assertEquals(422, assertThrows(IgrpResponseStatusException.class, () -> service.recusar(id, p.getId(), " ")).getStatusCode().value());
        service.recusar(id, p.getId(), "Dados em actualizacao");
        assertEquals(409, assertThrows(IgrpResponseStatusException.class, () -> service.emitir(id, p.getId())).getStatusCode().value());
    }

    @Test
    void anuladoDeixaDeSerValidoNaVerificacao() {
        var d = DocumentoEmitido.emitir(TipoDocumentoEmitido.DECLARACAO, "DEC-2026-000001", id, "Declaração", "ABCDEFGH23",
                "x.pdf", new byte[]{1, 2, 3}, null, null, HOJE.atTime(9, 0));
        when(documentoRepository.findById(d.getId())).thenReturn(Optional.of(d));
        when(documentoRepository.findByCodigo("ABCDEFGH23")).thenReturn(Optional.of(d));
        assertFalse(emissao.verificar("abcdefgh23").anulado());
        emissao.anular(d.getId(), "Emitida com erro no nome");
        var v = emissao.verificar("ABCDEFGH23");
        assertTrue(v.existe());
        assertTrue(v.anulado());
        assertEquals("Maria Lopes", v.titular());
        assertFalse(emissao.verificar("NAOEXISTE").existe());
        assertEquals(409, assertThrows(IgrpResponseStatusException.class, () -> d.anular("de novo", HOJE.atTime(11, 0))).getStatusCode().value());
    }

    @Test
    void tipoEmFaltaE422() {
        assertEquals(422, assertThrows(IgrpResponseStatusException.class,
                () -> service.pedir(id, null, null, true)).getStatusCode().value());
    }
}
