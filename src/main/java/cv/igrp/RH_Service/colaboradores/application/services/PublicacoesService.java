package cv.igrp.RH_Service.colaboradores.application.services;

import cv.igrp.RH_Service.colaboradores.application.dto.ColaboradorDetailsResponseDTO;
import cv.igrp.RH_Service.colaboradores.application.queries.GetColaboradorDetailsQuery;
import cv.igrp.RH_Service.colaboradores.application.queries.GetColaboradorDetailsQueryHandler;
import cv.igrp.RH_Service.colaboradores.domain.models.Funcionario;
import cv.igrp.RH_Service.colaboradores.domain.models.PublicacaoOficial;
import cv.igrp.RH_Service.colaboradores.domain.models.TipoDocumentoEmitido;
import cv.igrp.RH_Service.colaboradores.domain.models.TipoFactoRh;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.PublicacaoOficialRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.QualificacaoRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.PublicacaoOficialId;
import cv.igrp.RH_Service.shared.application.services.notificacoes.Notificador;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.domain.notificacoes.TipoNotificacao;
import cv.igrp.RH_Service.shared.domain.service.Datas;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * <b>Os actos a publicar</b> (Lei n.º 20/X/2023, arts. 89.º–90.º; BR-PUB-01..06). Os que a lei manda
 * publicar nascem sozinhos dos factos do RH ({@link #aoRegistarFacto}); os outros criam-se à mão, ou por
 * outro serviço com {@link #aPublicar}. O RH gera o extracto (PDF, no MinIO) e regista a publicação.
 */
@Service
@RequiredArgsConstructor
public class PublicacoesService {

    static final String RECURSO = "PUBLICACAO_OFICIAL";

    /** Que factos correspondem a actos sujeitos a publicação no Boletim Oficial (art. 89.º n.º 1). */
    static final Map<TipoFactoRh, PublicacaoOficial.TipoActo> ACTOS = Map.of(
            TipoFactoRh.ADMISSAO, PublicacaoOficial.TipoActo.PROVIMENTO,
            TipoFactoRh.PROVIMENTO, PublicacaoOficial.TipoActo.PROVIMENTO,
            TipoFactoRh.PROMOCAO, PublicacaoOficial.TipoActo.NOMEACAO,
            TipoFactoRh.MUDANCA_CARREIRA, PublicacaoOficial.TipoActo.NOMEACAO,
            TipoFactoRh.TRANSFERENCIA, PublicacaoOficial.TipoActo.MOBILIDADE,
            TipoFactoRh.CONSOLIDACAO_MOBILIDADE, PublicacaoOficial.TipoActo.MOBILIDADE,
            TipoFactoRh.COMISSAO_SERVICO, PublicacaoOficial.TipoActo.COMISSAO_SERVICO,
            TipoFactoRh.CESSACAO, PublicacaoOficial.TipoActo.CESSACAO);

    private final PublicacaoOficialRepository repository;
    private final FuncionarioRepository funcionarioRepository;
    private final QualificacaoRepository qualificacaoRepository;
    private final GetColaboradorDetailsQueryHandler detalhes;
    private final EmissaoDocumentosService emissao;
    private final Notificador notificador;

    /** Um facto do RH que é acto sujeito a publicação fica logo na caixa do RH (BR-PUB-02). */
    @EventListener
    public void aoRegistarFacto(DiarioFactos.FactoRegistado evento) {
        var f = evento.facto();
        var acto = ACTOS.get(f.getTipo());
        if (acto == null) return;
        // A cessação por pena disciplinar publica-se pela pena (art. 15.º n.º 2 do Estatuto); a exoneração voluntária
        // publica-se como o despacho de exoneração (Lei n.º 20/X/2023, art. 94.º n.º 5).
        String motivo = f.getDados() != null ? String.valueOf(f.getDados().get("motivo")) : "";
        if (f.getTipo() == TipoFactoRh.CESSACAO && "PENA_DISCIPLINAR".equals(motivo)) return;
        if (f.getTipo() == TipoFactoRh.CESSACAO && "EXONERACAO_VOLUNTARIA".equals(motivo)) acto = PublicacaoOficial.TipoActo.EXONERACAO;
        String nome = funcionarioRepository.findById(f.getFuncionarioId()).map(Funcionario::getNomeCompleto).orElse("");
        aPublicar(acto, PublicacaoOficial.Meio.BOLETIM_OFICIAL, f.getFuncionarioId(), "FACTO_RH", f.getId().getStringValor(),
                nome + " — " + f.getDescricao(), f.getDataEfeito());
    }

    /** Para qualquer serviço: um acto a publicar. Não duplica o mesmo acto (a mesma referência). */
    @Transactional
    public Optional<PublicacaoOficial> aPublicar(PublicacaoOficial.TipoActo tipo, PublicacaoOficial.Meio meio,
                                                 FuncionarioId funcionarioId, String referenciaTipo, String referenciaId,
                                                 String sumario, LocalDate dataActo) {
        if (repository.existeParaReferencia(referenciaTipo, referenciaId)) return Optional.empty();
        var p = repository.save(PublicacaoOficial.aPublicar(tipo, meio, funcionarioId, referenciaTipo, referenciaId, sumario,
                dataActo != null ? dataActo : hoje()));
        notificador.paraRh().tipo(TipoNotificacao.PUBLICACAO_PENDENTE)
                .titulo("Acto a publicar: " + p.getSumario())
                .recurso(RECURSO, p.getId().getStringValor()).enviar();
        return Optional.ofNullable(p);
    }

    /** O extracto do acto (art. 89.º n.º 2: carreira, função, categoria, habilitações), em PDF no MinIO. */
    @Transactional
    public PublicacaoOficial gerarExtracto(PublicacaoOficialId id) {
        var p = publicacao(id);
        if (p.getEstado() != PublicacaoOficial.Estado.A_PUBLICAR)
            throw IgrpResponseStatusException.conflict("Esta publicação já foi registada ou cancelada.");
        Map<String, Object> v = new java.util.HashMap<>();
        v.put("tipoActo", nomeActo(p.getTipoActo()));
        v.put("sumario", p.getSumario());
        v.put("dataActo", Datas.pt(p.getDataActo()));
        v.put("linhas", linhasDoExtracto(p));
        var doc = emissao.emitir(TipoDocumentoEmitido.EXTRACTO_PUBLICACAO, p.getFuncionarioId(),
                "Extracto para publicação — " + nomeActo(p.getTipoActo()), "extracto", v, RECURSO, p.getId().getStringValor());
        p.extractoGerado(doc.getId());
        return repository.save(p);
    }

    @Transactional
    public PublicacaoOficial registarPublicacao(PublicacaoOficialId id, String serie, String numero, LocalDate data) {
        var p = publicacao(id);
        p.publicada(serie, numero, data);
        return repository.save(p);
    }

    @Transactional
    public PublicacaoOficial cancelar(PublicacaoOficialId id, String motivo) {
        var p = publicacao(id);
        p.cancelar(motivo);
        return repository.save(p);
    }

    @Transactional(readOnly = true)
    public List<PublicacaoOficial> listar(PublicacaoOficial.Estado estado) {
        return repository.find(estado);
    }

    @Transactional(readOnly = true)
    public List<PublicacaoOficial> doFuncionario(FuncionarioId funcionarioId) {
        return repository.findByFuncionario(funcionarioId);
    }

    /** Os dados do n.º 2 do art. 89.º; a remuneração fica de fora — é do processamento salarial. */
    List<String> linhasDoExtracto(PublicacaoOficial p) {
        List<String> ls = new ArrayList<>();
        if (p.getFuncionarioId() == null) return ls;
        var f = funcionarioRepository.findById(p.getFuncionarioId()).orElse(null);
        if (f == null) return ls;
        ls.add("Nome: " + f.getNomeCompleto() + (f.getNumeroFuncionario() != null ? " (n.º " + f.getNumeroFuncionario() + ")" : ""));
        ColaboradorDetailsResponseDTO d = detalhes.handle(new GetColaboradorDetailsQuery(f.getId().getStringValor())).getBody();
        var e = d != null ? d.getEnquadramento() : null;
        if (e != null) {
            if (e.getCareerName() != null) ls.add("Carreira: " + e.getCareerName());
            if (e.getCategoryName() != null) ls.add("Categoria: " + e.getCategoryName());
            if (e.getFunctionName() != null) ls.add("Função: " + e.getFunctionName());
            else if (e.getCargoName() != null) ls.add("Cargo: " + e.getCargoName());
        }
        String habilitacoes = qualificacaoRepository.findAllByFuncionarioId(f.getId()).stream()
                .filter(q -> !Boolean.FALSE.equals(q.getIsActive()))
                .map(q -> q.getCourseName() != null ? q.getCourseName() : q.getLevel())
                .filter(java.util.Objects::nonNull).distinct().collect(Collectors.joining("; "));
        if (!habilitacoes.isBlank()) ls.add("Habilitações literárias: " + habilitacoes);
        return ls;
    }

    static String nomeActo(PublicacaoOficial.TipoActo t) {
        return switch (t) {
            case PROVIMENTO -> "Provimento";
            case NOMEACAO -> "Nomeação";
            case MOBILIDADE -> "Mobilidade";
            case COMISSAO_SERVICO -> "Comissão de serviço";
            case CONTRATO_GESTAO -> "Contrato de gestão";
            case CESSACAO -> "Cessação da relação de emprego público";
            case EXONERACAO -> "Exoneração";
            case PENA_DISCIPLINAR -> "Pena disciplinar";
            case REABILITACAO -> "Reabilitação";
            case LISTA_ANTIGUIDADE -> "Lista de antiguidade";
            case CONCURSO -> "Concurso";
            case OUTRO -> "Acto";
        };
    }

    private PublicacaoOficial publicacao(PublicacaoOficialId id) {
        return repository.findById(id).orElseThrow(() -> IgrpResponseStatusException.notFound("Publicação não encontrada."));
    }

    LocalDate hoje() { return LocalDate.now(); }
}
