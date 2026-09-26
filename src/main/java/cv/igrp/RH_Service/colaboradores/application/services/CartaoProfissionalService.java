package cv.igrp.RH_Service.colaboradores.application.services;

import cv.igrp.RH_Service.colaboradores.application.dto.EnquadramentoResponseDTO;
import cv.igrp.RH_Service.colaboradores.application.queries.GetColaboradorDetailsQuery;
import cv.igrp.RH_Service.colaboradores.application.queries.GetColaboradorDetailsQueryHandler;
import cv.igrp.RH_Service.colaboradores.domain.models.CartaoProfissional;
import cv.igrp.RH_Service.colaboradores.domain.models.Funcionario;
import cv.igrp.RH_Service.colaboradores.domain.models.TipoChecklist;
import cv.igrp.RH_Service.colaboradores.domain.models.TipoDocumentoEmitido;
import cv.igrp.RH_Service.colaboradores.domain.repository.CartaoProfissionalRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.CartaoProfissionalId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.shared.application.services.notificacoes.Notificador;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.domain.notificacoes.TipoNotificacao;
import cv.igrp.RH_Service.shared.domain.service.Datas;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * <b>Cartão de identificação profissional</b> (Lei n.º 20/X/2023, art. 25.º; BR-CID-01..06): emitir (PDF no
 * MinIO, série CIP), entregar (atestando a recepção), devolver, anular, e a validade pela função e categoria.
 */
@Service
@RequiredArgsConstructor
public class CartaoProfissionalService {

    static final String RECURSO = "CARTAO_PROFISSIONAL";

    /** Um cartão e se vale hoje (nulo = vale). */
    public record Situacao(CartaoProfissional cartao, String motivoInvalidade) {}

    private final CartaoProfissionalRepository repository;
    private final FuncionarioRepository funcionarioRepository;
    private final GetColaboradorDetailsQueryHandler detalhes;
    private final EmissaoDocumentosService emissao;
    private final Notificador notificador;
    private final ChecklistService checklists;

    /** Emite um cartão novo; o que estava em uso fica anulado (substituído) e o seu documento também. */
    @Transactional
    public CartaoProfissional emitir(FuncionarioId funcionarioId) {
        Funcionario f = funcionario(funcionarioId);
        if (!Boolean.TRUE.equals(f.getIsActive()))
            throw IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY, "Este colaborador já não está ao serviço.");
        EnquadramentoResponseDTO e = enquadramento(funcionarioId);
        if (e == null || e.getCategoryName() == null && e.getCargoName() == null)
            throw IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY,
                    "O colaborador não tem Lugar nem categoria: o cartão identifica a categoria e o cargo. Coloque-o antes num Lugar.");
        Map<String, Object> v = new HashMap<>();
        v.put("nome", f.getNomeCompleto());
        v.put("numeroFuncionario", f.getNumeroFuncionario());
        v.put("categoria", e.getCategoryName());
        v.put("carreira", e.getCareerName());
        v.put("funcao", e.getFunctionName() != null ? e.getFunctionName() : e.getCargoName());
        v.put("unidade", e.getUnitName());
        var doc = emissao.emitir(TipoDocumentoEmitido.CARTAO_PROFISSIONAL, funcionarioId, "Cartão de identificação profissional",
                "cartao", v, RECURSO, null);
        for (CartaoProfissional antigo : repository.findByFuncionario(funcionarioId)) {
            if (!antigo.emUso()) continue;
            antigo.anular("Substituído pelo cartão n.º " + doc.getNumero() + ".");
            repository.save(antigo);
            anularDocumento(antigo, antigo.getMotivoAnulacao());
        }
        var c = repository.save(CartaoProfissional.emitir(funcionarioId, doc.getId(), doc.getNumero(), hoje(),
                e.getCategoryId(), e.getCategoryName(), e.getFunctionId(), e.getFunctionName(), e.getCargoName()));
        notificador.para(funcionarioId).tipo(TipoNotificacao.CARTAO_PROFISSIONAL)
                .titulo("O seu cartão profissional (n.º " + c.getNumero() + ") está pronto para levantar")
                .recurso(RECURSO, c.getId().getStringValor()).enviar();
        return c;
    }

    @Transactional
    public CartaoProfissional entregar(FuncionarioId funcionarioId, CartaoProfissionalId id, LocalDate data) {
        var c = cartao(funcionarioId, id);
        c.entregar(data != null ? data : hoje());
        var gravado = repository.save(c);
        checklists.cumprir(funcionarioId, TipoChecklist.ENTRADA, ChecklistService.CARTAO_PROFISSIONAL,
                "Cartão n.º " + c.getNumero() + " entregue.", c.getDataEntrega());
        return gravado;
    }

    @Transactional
    public CartaoProfissional devolver(FuncionarioId funcionarioId, CartaoProfissionalId id, LocalDate data) {
        var c = cartao(funcionarioId, id);
        c.devolver(data != null ? data : hoje());
        anularDocumento(c, "Cartão devolvido em " + Datas.pt(c.getDataDevolucao()) + ".");
        var gravado = repository.save(c);
        checklists.cumprir(funcionarioId, TipoChecklist.SAIDA, ChecklistService.DEVOLUCAO_CARTAO,
                "Cartão n.º " + c.getNumero() + " devolvido.", c.getDataDevolucao());
        return gravado;
    }

    @Transactional
    public CartaoProfissional anular(FuncionarioId funcionarioId, CartaoProfissionalId id, String motivo) {
        var c = cartao(funcionarioId, id);
        c.anular(motivo);
        anularDocumento(c, c.getMotivoAnulacao());
        return repository.save(c);
    }

    /** Os cartões do colaborador, cada um com o motivo por que não vale (nulo se vale). */
    @Transactional(readOnly = true)
    public List<Situacao> doFuncionario(FuncionarioId funcionarioId) {
        Funcionario f = funcionario(funcionarioId);
        var e = Optional.ofNullable(enquadramento(funcionarioId));
        String categoria = e.map(EnquadramentoResponseDTO::getCategoryId).orElse(null);
        String funcao = e.map(EnquadramentoResponseDTO::getFunctionId).orElse(null);
        boolean activo = Boolean.TRUE.equals(f.getIsActive());
        return repository.findByFuncionario(funcionarioId).stream()
                .map(c -> new Situacao(c, c.motivoInvalidade(activo, categoria, funcao))).toList();
    }

    private void anularDocumento(CartaoProfissional c, String motivo) {
        try {
            emissao.anular(c.getDocumentoId(), motivo);
        } catch (IgrpResponseStatusException jaAnulado) {
            // O documento já estava anulado: nada a fazer.
        }
    }

    private EnquadramentoResponseDTO enquadramento(FuncionarioId id) {
        var d = detalhes.handle(new GetColaboradorDetailsQuery(id.getStringValor())).getBody();
        return d != null ? d.getEnquadramento() : null;
    }

    private Funcionario funcionario(FuncionarioId id) {
        return funcionarioRepository.findById(id).orElseThrow(() -> IgrpResponseStatusException.notFound("Colaborador não encontrado."));
    }

    private CartaoProfissional cartao(FuncionarioId funcionarioId, CartaoProfissionalId id) {
        return repository.findById(id).filter(c -> c.getFuncionarioId().equals(funcionarioId))
                .orElseThrow(() -> IgrpResponseStatusException.notFound("Cartão não encontrado."));
    }

    LocalDate hoje() { return LocalDate.now(); }
}
