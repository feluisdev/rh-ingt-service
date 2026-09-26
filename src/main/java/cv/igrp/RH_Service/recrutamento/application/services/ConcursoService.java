package cv.igrp.RH_Service.recrutamento.application.services;

import cv.igrp.RH_Service.carreiras.domain.repository.CategoryRepository;
import cv.igrp.RH_Service.carreiras.domain.valueobject.CategoryId;
import cv.igrp.RH_Service.colaboradores.application.services.PublicacoesService;
import cv.igrp.RH_Service.colaboradores.domain.models.Funcionario;
import cv.igrp.RH_Service.colaboradores.domain.models.PublicacaoOficial;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.estrutura.application.port.PositionOccupancyPort;
import cv.igrp.RH_Service.estrutura.domain.repository.PositionRepository;
import cv.igrp.RH_Service.estrutura.domain.valueobject.PositionId;
import cv.igrp.RH_Service.recrutamento.domain.models.Candidatura;
import cv.igrp.RH_Service.recrutamento.domain.models.Concurso;
import cv.igrp.RH_Service.recrutamento.domain.models.MetodoSelecao;
import cv.igrp.RH_Service.recrutamento.domain.repository.ConcursoRepository;
import cv.igrp.RH_Service.recrutamento.domain.service.ClassificacaoConcurso;
import cv.igrp.RH_Service.recrutamento.domain.valueobject.CandidaturaId;
import cv.igrp.RH_Service.recrutamento.domain.valueobject.ConcursoId;
import cv.igrp.RH_Service.shared.application.services.notificacoes.Notificador;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.domain.notificacoes.TipoNotificacao;
import cv.igrp.RH_Service.shared.domain.service.Datas;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * <b>Recrutamento e selecção</b> (Lei n.º 20/X/2023, arts. 123.º–129.º; BR-CNC-01..20). O RH prepara o concurso em
 * rascunho, abre-o (o aviso vai para as publicações), regista as candidaturas (decisão do cliente: sem portal),
 * admite ou propõe a exclusão (com audiência), regista as notas, publica a lista provisória, homologa, e provê os
 * Lugares pela ordem da lista — com a quota de deficiência à frente. A entrada ao serviço de quem é provido faz-se pelo
 * registo, contrato, colocação e provimento, com a referência do concurso.
 */
@Service
@RequiredArgsConstructor
public class ConcursoService {

    static final String RECURSO = "CONCURSO";

    /** Os dados de um concurso em rascunho (tudo opcional na edição, excepto o que {@link Concurso#definir} exige). */
    public record Dados(String referencia, Concurso.Finalidade finalidade, Concurso.Tipo tipo, Concurso.Modalidade modalidade,
                        String vinculo, UUID categoriaId, String requisitos, String habilitacaoMinima, Integer quotaDeficiencia,
                        List<UUID> lugares, List<Concurso.Metodo> metodos, String dispensaMetodosDespacho,
                        List<Concurso.MembroJuri> juri, LocalDate dataAviso, LocalDate candidaturasDe, LocalDate candidaturasAte) {}

    /** Os dados do candidato. {@code funcionarioId}: é da casa (o nome e a identificação vêm da ficha). */
    public record Candidato(String nome, String documento, String nif, String email, String telefone, String habilitacao,
                            boolean deficiencia, UUID funcionarioId, boolean vinculadoAdministracao) {}

    private final ConcursoRepository repository;
    private final CategoryRepository categoryRepository;
    private final PositionRepository positionRepository;
    private final PositionOccupancyPort ocupacao;
    private final FuncionarioRepository funcionarioRepository;
    private final PublicacoesService publicacoes;
    private final Notificador notificador;

    // ---------------------------------------------------------------- rascunho

    @Transactional
    public Concurso criar(Dados d) {
        if (d.referencia() != null && repository.existeReferencia(d.referencia().trim(), null))
            throw IgrpResponseStatusException.conflict("Já existe um concurso com a referência " + d.referencia().trim() + ".");
        exigirCategoria(d.categoriaId());
        var c = Concurso.criar(d.referencia(), d.finalidade(), d.tipo(), d.modalidade(), d.vinculo(), d.categoriaId());
        aplicar(c, d);
        return repository.save(c);
    }

    @Transactional
    public Concurso actualizar(ConcursoId id, Dados d) {
        var c = concurso(id);
        if (d.referencia() != null && repository.existeReferencia(d.referencia().trim(), id))
            throw IgrpResponseStatusException.conflict("Já existe um concurso com a referência " + d.referencia().trim() + ".");
        if (d.categoriaId() != null) exigirCategoria(d.categoriaId());
        c.definir(d.referencia() != null ? d.referencia() : c.getReferencia(),
                d.finalidade() != null ? d.finalidade() : c.getFinalidade(), d.tipo() != null ? d.tipo() : c.getTipo(),
                d.modalidade() != null ? d.modalidade() : c.getModalidade(), d.vinculo() != null ? d.vinculo() : c.getVinculo(),
                d.categoriaId() != null ? d.categoriaId() : c.getCategoriaId());
        aplicar(c, d);
        return repository.save(c);
    }

    private void aplicar(Concurso c, Dados d) {
        if (d.requisitos() != null || d.habilitacaoMinima() != null || d.quotaDeficiencia() != null)
            c.definirRequisitos(d.requisitos() != null ? d.requisitos() : c.getRequisitos(),
                    d.habilitacaoMinima() != null ? d.habilitacaoMinima() : c.getHabilitacaoMinima(),
                    d.quotaDeficiencia() != null ? d.quotaDeficiencia() : c.getQuotaDeficiencia());
        if (d.lugares() != null && !d.lugares().isEmpty()) c.definirLugares(d.lugares());
        if (d.metodos() != null && !d.metodos().isEmpty()) c.definirMetodos(d.metodos(), d.dispensaMetodosDespacho());
        if (d.juri() != null && !d.juri().isEmpty()) c.definirJuri(d.juri());
        if (d.candidaturasDe() != null || d.candidaturasAte() != null)
            c.definirPrazo(d.dataAviso(), d.candidaturasDe(), d.candidaturasAte());
    }

    /** Abrir: os Lugares têm de existir, estar activos, vagos, ser da categoria e não estar noutro concurso activo. */
    @Transactional
    public Concurso abrir(ConcursoId id) {
        var c = concurso(id);
        var ocupados = ocupacao.ocupados(c.getLugares());
        for (UUID lugar : c.getLugares()) {
            var p = positionRepository.findById(PositionId.from(lugar))
                    .orElseThrow(() -> invalido("Um dos Lugares do concurso não existe."));
            if (!p.podeSerOcupado()) throw invalido("O Lugar " + p.getNumeroLugar() + " não está activo (congelado ou extinto).");
            if (ocupados.contains(lugar)) throw invalido("O Lugar " + p.getNumeroLugar() + " já está ocupado.");
            if (!c.getCategoriaId().equals(p.getCategoryId()))
                throw invalido("O Lugar " + p.getNumeroLugar() + " não é da categoria do concurso.");
            if (repository.lugarEmConcursoActivo(lugar, id))
                throw IgrpResponseStatusException.conflict("O Lugar " + p.getNumeroLugar() + " já está noutro concurso em curso.");
        }
        c.abrir(hoje());
        var gravado = repository.save(c);
        publicacoes.aPublicar(PublicacaoOficial.TipoActo.CONCURSO, PublicacaoOficial.Meio.BOLETIM_OFICIAL, null, "CONCURSO_AVISO",
                c.getId().getStringValor(), "Aviso de abertura do concurso " + c.getReferencia() + " (" + c.getLugares().size()
                        + " Lugar(es)); candidaturas de " + Datas.pt(c.getCandidaturasDe()) + " a " + Datas.pt(c.getCandidaturasAte()),
                c.getDataAviso());
        return gravado;
    }

    @Transactional
    public Concurso encerrarCandidaturas(ConcursoId id) {
        var c = concurso(id);
        c.encerrarCandidaturas(hoje());
        return repository.save(c);
    }

    @Transactional
    public Concurso iniciarAvaliacao(ConcursoId id) {
        var c = concurso(id);
        boolean pendentes = repository.findCandidaturas(id).stream()
                .anyMatch(x -> x.getEstado() == Candidatura.Estado.EM_AUDIENCIA || x.getEstado() == Candidatura.Estado.APRESENTADA);
        c.iniciarAvaliacao(pendentes);
        return repository.save(c);
    }

    /** A lista provisória: classifica e ordena os aprovados; avisa os candidatos da casa. */
    @Transactional
    public Concurso listaProvisoria(ConcursoId id) {
        var c = concurso(id);
        var cands = repository.findCandidaturas(id);
        boolean emFalta = cands.stream().anyMatch(x -> x.getEstado() == Candidatura.Estado.ADMITIDA && !x.temTodasAsNotas(c.getMetodos()));
        c.listaProvisoria(emFalta);
        ClassificacaoConcurso.classificar(c, cands);
        cands.forEach(repository::save);
        for (var x : cands)
            if (x.getFuncionarioId() != null)
                avisar(x, x.aprovada() ? "Ficou em " + x.getPosicao() + ".º lugar na lista provisória do concurso " + c.getReferencia()
                        : "Não foi aprovado no concurso " + c.getReferencia());
        return repository.save(c);
    }

    @Transactional
    public Concurso homologar(ConcursoId id, String despacho, LocalDate data) {
        var c = concurso(id);
        c.homologar(despacho, data != null ? data : hoje());
        var gravado = repository.save(c);
        publicacoes.aPublicar(PublicacaoOficial.TipoActo.CONCURSO, PublicacaoOficial.Meio.BOLETIM_OFICIAL, null, "CONCURSO_LISTA",
                c.getId().getStringValor(), "Lista de classificação final homologada do concurso " + c.getReferencia(),
                c.getHomologacaoData());
        return gravado;
    }

    @Transactional
    public Concurso concluir(ConcursoId id) {
        var c = concurso(id);
        c.concluir();
        return repository.save(c);
    }

    @Transactional
    public Concurso anular(ConcursoId id, String motivo) {
        var c = concurso(id);
        c.anular(motivo);
        return repository.save(c);
    }

    // ---------------------------------------------------------------- candidaturas

    /**
     * Regista uma candidatura. Nos internos o candidato tem de estar vinculado à Administração; no interno restrito, ser da
     * casa (art. 127.º n.º 3). Um candidato não concorre duas vezes ao mesmo concurso (409).
     */
    @Transactional
    public Candidatura candidatar(ConcursoId id, Candidato d) {
        var c = concurso(id);
        String nome = d.nome(), documento = d.documento(), nif = d.nif(), email = d.email(), telefone = d.telefone();
        if (d.funcionarioId() != null) {
            Funcionario f = funcionarioRepository.findById(FuncionarioId.from(d.funcionarioId()))
                    .orElseThrow(() -> IgrpResponseStatusException.notFound("Colaborador não encontrado."));
            if (!Boolean.TRUE.equals(f.getIsActive())) throw invalido("Este colaborador já não está ao serviço.");
            nome = f.getNomeCompleto();
            documento = documento != null ? documento : f.getNumeroDocumento();
            nif = nif != null ? nif : f.getNif();
            email = email != null ? email : f.getEmail();
            telefone = telefone != null ? telefone : f.getTelefone();
        }
        if (c.getModalidade() == Concurso.Modalidade.INTERNO_RESTRITO && d.funcionarioId() == null)
            throw invalido("O concurso interno restrito é só para os funcionários desta entidade: escolha o colaborador.");
        if (c.getModalidade() == Concurso.Modalidade.INTERNO && d.funcionarioId() == null && !d.vinculadoAdministracao())
            throw invalido("O concurso interno é só para funcionários da Administração Pública.");
        String docNormalizado = documento != null && !documento.isBlank() ? documento.trim().toUpperCase() : null;
        if (repository.existeCandidatura(id, docNormalizado, d.funcionarioId()))
            throw IgrpResponseStatusException.conflict("Este candidato já concorre a este concurso.");
        return repository.save(Candidatura.apresentar(c, nome, documento, nif, email, telefone, d.habilitacao(), d.deficiencia(),
                d.funcionarioId(), d.vinculadoAdministracao(), hoje()));
    }

    @Transactional
    public Candidatura admitir(ConcursoId id, CandidaturaId cid) {
        var x = candidatura(id, cid);
        exigirEstado(concurso(id), Concurso.Estado.ABERTO, Concurso.Estado.CANDIDATURAS_ENCERRADAS);
        x.admitir();
        avisar(x, "Foi admitido ao concurso");
        return repository.save(x);
    }

    @Transactional
    public Candidatura proporExclusao(ConcursoId id, CandidaturaId cid, String motivo) {
        var x = candidatura(id, cid);
        exigirEstado(concurso(id), Concurso.Estado.ABERTO, Concurso.Estado.CANDIDATURAS_ENCERRADAS);
        x.proporExclusao(motivo, hoje());
        avisar(x, "Foi proposta a sua exclusão do concurso. Pode pronunciar-se até " + Datas.pt(x.getAudienciaAte()) + ".");
        return repository.save(x);
    }

    @Transactional
    public Candidatura decidirAudiencia(ConcursoId id, CandidaturaId cid, boolean excluir, String resposta) {
        var x = candidatura(id, cid);
        x.decidirAudiencia(excluir, resposta);
        avisar(x, excluir ? "Foi excluído do concurso" : "Foi admitido ao concurso");
        return repository.save(x);
    }

    @Transactional
    public Candidatura registarNota(ConcursoId id, CandidaturaId cid, MetodoSelecao metodo, BigDecimal nota) {
        var c = concurso(id);
        exigirEstado(c, Concurso.Estado.EM_AVALIACAO);
        var x = candidatura(id, cid);
        x.registarNota(c.getMetodos(), metodo, nota);
        return repository.save(x);
    }

    /**
     * Provê um aprovado num Lugar do concurso — pela ordem da lista, com a quota à frente [ind.], e dentro da validade
     * da reserva. O Lugar tem de estar vago e não prometido a outro candidato.
     */
    @Transactional
    public Candidatura prover(ConcursoId id, CandidaturaId cid, UUID lugarId) {
        var c = concurso(id);
        if (!c.reservaValida(hoje()))
            throw invalido(c.getEstado() != Concurso.Estado.HOMOLOGADO ? "Só se provê depois de a lista ser homologada."
                    : "A lista deixou de valer como reserva de recrutamento a " + Datas.pt(c.getReservaAte()) + ".");
        if (lugarId == null || !c.getLugares().contains(lugarId)) throw invalido("Escolha um dos Lugares do concurso.");
        var cands = repository.findCandidaturas(id);
        if (cands.stream().anyMatch(x -> lugarId.equals(x.getLugarProvidoId())))
            throw IgrpResponseStatusException.conflict("Esse Lugar já foi provido por outro candidato do concurso.");
        if (ocupacao.ocupados(List.of(lugarId)).contains(lugarId)) throw invalido("Esse Lugar já está ocupado.");
        var aprovadas = cands.stream().filter(x -> x.getEstado() == Candidatura.Estado.APROVADA)
                .sorted(java.util.Comparator.comparing(Candidatura::getPosicao)).toList();
        long providosQuota = cands.stream().filter(x -> x.getEstado() == Candidatura.Estado.PROVIDA && x.isDeficiencia()).count();
        Optional<Candidatura> proximo = ClassificacaoConcurso.ordemDeProvimento(c, aprovadas, providosQuota).stream().findFirst();
        var x = candidatura(id, cid);
        if (proximo.isPresent() && !proximo.get().getId().equals(x.getId()))
            throw IgrpResponseStatusException.conflict("Provê-se pela ordem da lista: segue-se " + proximo.get().getNome()
                    + " (" + proximo.get().getPosicao() + ".º" + (proximo.get().isDeficiencia() ? ", quota de deficiência" : "") + ").");
        x.prover(lugarId);
        avisar(x, "Foi provido no concurso " + c.getReferencia() + ". O RH contacta-o para a entrada ao serviço.");
        return repository.save(x);
    }

    @Transactional
    public Candidatura desistir(ConcursoId id, CandidaturaId cid) {
        var x = candidatura(id, cid);
        x.desistir(hoje());
        return repository.save(x);
    }

    // ---------------------------------------------------------------- leitura

    @Transactional(readOnly = true)
    public List<Concurso> listar(Concurso.Estado estado) {
        return repository.find(estado);
    }

    @Transactional(readOnly = true)
    public Concurso concurso(ConcursoId id) {
        return repository.findById(id).orElseThrow(() -> IgrpResponseStatusException.notFound("Concurso não encontrado."));
    }

    @Transactional(readOnly = true)
    public List<Candidatura> candidaturas(ConcursoId id) {
        return repository.findCandidaturas(id);
    }

    @Transactional(readOnly = true)
    public List<Candidatura> doFuncionario(UUID funcionarioId) {
        return repository.findCandidaturasDoFuncionario(funcionarioId);
    }

    private void exigirCategoria(UUID categoriaId) {
        if (categoriaId != null && categoryRepository.findById(CategoryId.from(categoriaId)).isEmpty())
            throw invalido("A categoria indicada não existe.");
    }

    private void exigirEstado(Concurso c, Concurso.Estado... permitidos) {
        for (var e : permitidos) if (c.getEstado() == e) return;
        throw IgrpResponseStatusException.conflict("Não é possível fazer isto no estado em que o concurso está.");
    }

    private Candidatura candidatura(ConcursoId id, CandidaturaId cid) {
        return repository.findCandidatura(cid).filter(x -> x.getConcursoId().equals(id))
                .orElseThrow(() -> IgrpResponseStatusException.notFound("Candidatura não encontrada."));
    }

    private void avisar(Candidatura x, String titulo) {
        if (x.getFuncionarioId() == null) return;
        notificador.para(FuncionarioId.from(x.getFuncionarioId())).tipo(TipoNotificacao.CONCURSO).titulo(titulo)
                .recurso(RECURSO, x.getConcursoId().getStringValor()).enviar();
    }

    LocalDate hoje() { return LocalDate.now(); }

    private static IgrpResponseStatusException invalido(String m) {
        return IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY, m);
    }
}
