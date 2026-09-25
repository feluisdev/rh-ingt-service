package cv.igrp.RH_Service.colaboradores.application.services;

import cv.igrp.RH_Service.colaboradores.application.dto.ColaboradorDetailsResponseDTO;
import cv.igrp.RH_Service.colaboradores.application.queries.GetColaboradorDetailsQuery;
import cv.igrp.RH_Service.colaboradores.application.queries.GetColaboradorDetailsQueryHandler;
import cv.igrp.RH_Service.colaboradores.domain.models.DocumentoEmitido;
import cv.igrp.RH_Service.colaboradores.domain.models.Funcionario;
import cv.igrp.RH_Service.colaboradores.domain.models.PedidoDeclaracao;
import cv.igrp.RH_Service.colaboradores.domain.models.TipoDocumentoEmitido;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.PedidoDeclaracaoRepository;
import cv.igrp.RH_Service.colaboradores.domain.service.CalculadoraAntiguidade;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.PedidoDeclaracaoId;
import cv.igrp.RH_Service.shared.application.services.notificacoes.Notificador;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.domain.notificacoes.TipoNotificacao;
import cv.igrp.RH_Service.shared.domain.service.Datas;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <b>Declarações</b> (BR-DEC-01..04): o colaborador pede em {@code /me} (ou o RH regista e emite logo), o RH
 * emite ou recusa. O texto monta-se dos dados de hoje (a ficha, o contrato, o enquadramento, a antiguidade)
 * e fica congelado no PDF emitido, que vai para o MinIO ({@link EmissaoDocumentosService}).
 */
@Service
@RequiredArgsConstructor
public class DeclaracoesService {

    static final String RECURSO = "PEDIDO_DECLARACAO";

    public record Pedido(PedidoDeclaracao pedido, DocumentoEmitido documento) {}

    private final PedidoDeclaracaoRepository pedidoRepository;
    private final FuncionarioRepository funcionarioRepository;
    private final EmissaoDocumentosService emissao;
    private final GetColaboradorDetailsQueryHandler detalhes;
    private final AntiguidadeService antiguidadeService;
    private final ListaAntiguidadeService listaAntiguidadeService;
    private final cv.igrp.RH_Service.colaboradores.domain.repository.DocumentoEmitidoRepository documentoRepository;
    private final Notificador notificador;

    /** O próprio pede (fica PEDIDA, avisa o RH); o RH regista e emite logo. */
    @Transactional
    public Pedido pedir(FuncionarioId funcionarioId, PedidoDeclaracao.Tipo tipo, String finalidade, boolean peloProprio) {
        Funcionario f = funcionario(funcionarioId);
        var p = pedidoRepository.save(PedidoDeclaracao.pedir(funcionarioId, tipo, finalidade, peloProprio, hoje()));
        if (!peloProprio) return emitir(p);
        notificador.paraRh().tipo(TipoNotificacao.DECLARACAO_PEDIDA)
                .titulo(f.getNomeCompleto() + " pediu uma " + nome(tipo))
                .texto(p.getFinalidade() != null ? "Para " + p.getFinalidade() + "." : null)
                .recurso(RECURSO, p.getId().getStringValor()).enviar();
        return new Pedido(p, null);
    }

    @Transactional
    public Pedido emitir(FuncionarioId funcionarioId, PedidoDeclaracaoId id) {
        return emitir(pedido(funcionarioId, id));
    }

    private Pedido emitir(PedidoDeclaracao p) {
        FuncionarioId funcionarioId = p.getFuncionarioId();
        if (p.getEstado() != PedidoDeclaracao.Estado.PEDIDA)
            throw IgrpResponseStatusException.conflict("Este pedido de declaração já foi tratado.");
        Map<String, Object> v = new HashMap<>();
        v.put("paragrafos", paragrafos(p.getTipo(), funcionario(funcionarioId)));
        v.put("finalidade", p.getFinalidade());
        var doc = emissao.emitir(TipoDocumentoEmitido.DECLARACAO, funcionarioId, titulo(p.getTipo()), "declaracao", v,
                RECURSO, p.getId().getStringValor());
        p.emitida(doc.getId());
        pedidoRepository.save(p);
        notificador.para(funcionarioId).tipo(TipoNotificacao.DECLARACAO_EMITIDA)
                .titulo("A sua " + nome(p.getTipo()) + " está pronta (n.º " + doc.getNumero() + ")")
                .recurso("DOCUMENTO_EMITIDO", doc.getId().getStringValor()).enviar();
        return new Pedido(p, doc);
    }

    @Transactional
    public Pedido recusar(FuncionarioId funcionarioId, PedidoDeclaracaoId id, String motivo) {
        var p = pedido(funcionarioId, id);
        p.recusar(motivo);
        pedidoRepository.save(p);
        notificador.para(funcionarioId).tipo(TipoNotificacao.DECLARACAO_EMITIDA)
                .titulo("O seu pedido de " + nome(p.getTipo()) + " não foi aceite")
                .texto(p.getMotivoRecusa()).recurso(RECURSO, p.getId().getStringValor()).enviar();
        return new Pedido(p, null);
    }

    @Transactional(readOnly = true)
    public List<Pedido> doFuncionario(FuncionarioId funcionarioId) {
        return pedidoRepository.findByFuncionario(funcionarioId).stream().map(this::comDocumento).toList();
    }

    @Transactional(readOnly = true)
    public List<Pedido> porEmitir() {
        return pedidoRepository.findPorEmitir().stream().map(p -> new Pedido(p, null)).toList();
    }

    /** O texto de cada tipo, com os dados de hoje. */
    List<String> paragrafos(PedidoDeclaracao.Tipo tipo, Funcionario f) {
        LocalDate hoje = hoje();
        ColaboradorDetailsResponseDTO d = detalhes.handle(new GetColaboradorDetailsQuery(f.getId().getStringValor())).getBody();
        var ficha = d != null ? d.getFuncionario() : null;
        var contrato = d != null ? d.getContrato() : null;
        var enq = d != null ? d.getEnquadramento() : null;
        String quem = f.getNomeCompleto() + identificacao(f, ficha);
        List<String> ps = new ArrayList<>();
        switch (tipo) {
            case VINCULO -> {
                if (!Boolean.TRUE.equals(f.getIsActive()))
                    throw invalido("Este colaborador já não está ao serviço. Emita antes uma declaração de tempo de serviço.");
                ps.add("Declara-se, para os devidos efeitos, que " + quem + ", exerce funções neste serviço desde "
                        + Datas.pt(f.getDataAdmissao()) + (contrato != null && contrato.getVinculoLaboralDesc() != null
                        ? ", com o vínculo de " + contrato.getVinculoLaboralDesc().toLowerCase() : "")
                        + (contrato != null && contrato.getContractTypeName() != null ? " (" + contrato.getContractTypeName() + ")" : "") + ".");
                if (enq != null)
                    ps.add("Está integrado(a) " + enquadramento(enq.getCareerName(), enq.getCategoryName(), enq.getGradeName(),
                            enq.getCargoName(), enq.getFunctionName(), enq.getUnitName()) + ".");
            }
            case TEMPO_SERVICO -> {
                var t = tempo(f, hoje);
                ps.add("Declara-se, para os devidos efeitos, que " + quem + ", admitido(a) em " + Datas.pt(f.getDataAdmissao())
                        + ", conta à data de " + Datas.pt(hoje) + " " + anosMesesDias(t) + " de serviço (" + t.diasContados()
                        + " dias), descontados os períodos que a lei manda não contar.");
            }
            case ANTIGUIDADE_CATEGORIA -> {
                LocalDate inicio = listaAntiguidadeService.inicioNoCargo(f, hoje)
                        .orElseThrow(() -> invalido("Este colaborador não tem Lugar nem categoria à data de hoje."));
                var t = antiguidadeService.calcularDesde(f.getId(), inicio, hoje);
                ps.add("Declara-se, para os devidos efeitos, que " + quem + ", está na categoria de "
                        + (enq != null && enq.getCategoryName() != null ? enq.getCategoryName() : "que ocupa")
                        + (enq != null && enq.getCareerName() != null ? ", da carreira " + enq.getCareerName() : "")
                        + ", desde " + Datas.pt(inicio) + ", e conta nela, à data de " + Datas.pt(hoje) + ", " + anosMesesDias(t)
                        + " (" + t.diasContados() + " dias).");
            }
            case SITUACAO_FUNCIONAL -> ps.add("Declara-se, para os devidos efeitos, que " + quem + ", se encontra à data de "
                    + Datas.pt(hoje) + " na situação de «" + (ficha != null && ficha.getWorkerStateName() != null
                    ? ficha.getWorkerStateName() : "sem estado registado") + "»"
                    + (Boolean.TRUE.equals(f.getIsActive()) ? "" : ", tendo cessado funções") + ".");
        }
        return ps;
    }

    private CalculadoraAntiguidade.Antiguidade tempo(Funcionario f, LocalDate hoje) {
        if (f.getDataAdmissao() == null) throw invalido("Registe a data de admissão do colaborador antes de emitir a declaração.");
        return antiguidadeService.calcular(f.getId(), hoje);
    }

    private static String identificacao(Funcionario f, cv.igrp.RH_Service.colaboradores.application.dto.FuncionarioResponseDTO ficha) {
        StringBuilder sb = new StringBuilder();
        if (f.getNumeroFuncionario() != null) sb.append(", n.º ").append(f.getNumeroFuncionario());
        if (f.getNif() != null) sb.append(", NIF ").append(f.getNif());
        if (ficha != null && ficha.getNumeroDocumento() != null)
            sb.append(", portador(a) do documento de identificação n.º ").append(ficha.getNumeroDocumento());
        return sb.toString();
    }

    private static String enquadramento(String carreira, String categoria, String escalao, String cargo, String funcao, String unidade) {
        List<String> partes = new ArrayList<>();
        if (carreira != null) partes.add("na carreira " + carreira);
        if (categoria != null) partes.add("categoria " + categoria);
        if (escalao != null) partes.add(escalao.toLowerCase().startsWith("escal") ? escalao : "escalão " + escalao);
        if (cargo != null) partes.add("no cargo de " + cargo);
        if (funcao != null) partes.add("com a função de " + funcao);
        if (unidade != null) partes.add("afecto(a) a " + unidade);
        return partes.isEmpty() ? "no quadro deste serviço" : String.join(", ", partes);
    }

    static String anosMesesDias(CalculadoraAntiguidade.Antiguidade t) {
        return t.anos() + (t.anos() == 1 ? " ano, " : " anos, ") + t.meses() + (t.meses() == 1 ? " mês e " : " meses e ")
                + t.dias() + (t.dias() == 1 ? " dia" : " dias");
    }

    static String titulo(PedidoDeclaracao.Tipo tipo) {
        return switch (tipo) {
            case VINCULO -> "Declaração de vínculo";
            case TEMPO_SERVICO -> "Declaração de tempo de serviço";
            case ANTIGUIDADE_CATEGORIA -> "Declaração de antiguidade na categoria";
            case SITUACAO_FUNCIONAL -> "Declaração de situação funcional";
        };
    }

    static String nome(PedidoDeclaracao.Tipo tipo) {
        return tipo == null ? "declaração" : titulo(tipo).toLowerCase();
    }

    private Pedido comDocumento(PedidoDeclaracao p) {
        return new Pedido(p, p.getDocumentoId() != null ? documentoRepository.findById(p.getDocumentoId()).orElse(null) : null);
    }

    private Funcionario funcionario(FuncionarioId id) {
        return funcionarioRepository.findById(id).orElseThrow(() -> IgrpResponseStatusException.notFound("Colaborador não encontrado."));
    }

    private PedidoDeclaracao pedido(FuncionarioId funcionarioId, PedidoDeclaracaoId id) {
        return pedidoRepository.findById(id).filter(p -> p.getFuncionarioId().equals(funcionarioId))
                .orElseThrow(() -> IgrpResponseStatusException.notFound("Pedido de declaração não encontrado."));
    }

    LocalDate hoje() { return LocalDate.now(); }

    private static IgrpResponseStatusException invalido(String m) {
        return IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY, m);
    }
}
