package cv.igrp.RH_Service.shared.infrastructure.persistence.notificacoes;

import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.shared.domain.notificacoes.Notificacao;
import cv.igrp.RH_Service.shared.domain.notificacoes.NotificacaoId;
import cv.igrp.RH_Service.shared.domain.notificacoes.NotificacaoRepository;
import cv.igrp.RH_Service.shared.domain.notificacoes.PerfilDestino;
import cv.igrp.RH_Service.shared.domain.notificacoes.TipoNotificacao;
import cv.igrp.RH_Service.shared.domain.pagination.PageResult;
import cv.igrp.RH_Service.shared.infrastructure.persistence.entity.notificacoes.NotificacaoEntity;
import cv.igrp.RH_Service.shared.infrastructure.persistence.entity.notificacoes.NotificacaoEnvioEntity;
import cv.igrp.RH_Service.shared.infrastructure.persistence.repository.notificacoes.NotificacaoEntityRepository;
import cv.igrp.RH_Service.shared.infrastructure.persistence.repository.notificacoes.NotificacaoEnvioEntityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class NotificacaoRepositoryImpl implements NotificacaoRepository {

    private final NotificacaoEntityRepository entityRepository;
    private final NotificacaoEnvioEntityRepository envioRepository;

    @Transactional
    @Override
    public Notificacao save(Notificacao n) {
        NotificacaoEntity e = entityRepository.findById(n.getId().getValor()).orElseGet(() -> {
            NotificacaoEntity novo = new NotificacaoEntity();
            novo.setId(n.getId().getValor());
            novo.setDestinatarioId(n.getDestinatario() != null ? n.getDestinatario().getValor() : null);
            novo.setPerfil(n.getPerfil() != null ? n.getPerfil().name() : null);
            novo.setTipo(n.getTipo().name());
            novo.setTitulo(n.getTitulo());
            novo.setTexto(n.getTexto());
            novo.setRecursoTipo(n.getRecursoTipo());
            novo.setRecursoId(n.getRecursoId());
            novo.setCriadaEm(n.getCriadaEm());
            return novo;
        });
        // Depois de criada, só a leitura muda.
        e.setLidaEm(n.getLidaEm());
        return toDomain(entityRepository.save(e));
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<Notificacao> findById(NotificacaoId id) {
        return entityRepository.findById(id.getValor()).map(NotificacaoRepositoryImpl::toDomain);
    }

    @Transactional(readOnly = true)
    @Override
    public PageResult<Notificacao> findDoDestinatario(FuncionarioId destinatario, boolean soNaoLidas, int pagina, int tamanho) {
        return pagina(entityRepository.findDoDestinatario(destinatario.getValor(), soNaoLidas, PageRequest.of(pagina, tamanho)));
    }

    @Transactional(readOnly = true)
    @Override
    public PageResult<Notificacao> findDoPerfil(PerfilDestino perfil, boolean soNaoLidas, int pagina, int tamanho) {
        return pagina(entityRepository.findDoPerfil(perfil.name(), soNaoLidas, PageRequest.of(pagina, tamanho)));
    }

    @Transactional(readOnly = true)
    @Override
    public long contarNaoLidas(FuncionarioId destinatario) {
        return entityRepository.countByDestinatarioIdAndLidaEmIsNull(destinatario.getValor());
    }

    @Transactional(readOnly = true)
    @Override
    public long contarNaoLidas(PerfilDestino perfil) {
        return entityRepository.countByPerfilAndLidaEmIsNull(perfil.name());
    }

    @Transactional
    @Override
    public int marcarTodasLidas(FuncionarioId destinatario, LocalDateTime agora) {
        return entityRepository.marcarTodasLidas(destinatario.getValor(), agora);
    }

    @Transactional
    @Override
    public void enfileirarEmail(NotificacaoId notificacao, LocalDateTime agora) {
        NotificacaoEnvioEntity e = new NotificacaoEnvioEntity();
        e.setId(UUID.randomUUID());
        e.setNotificacaoId(notificacao.getValor());
        e.setCanal("EMAIL");
        e.setEstado("PENDENTE");
        e.setTentativas(0);
        e.setCriadoEm(agora);
        envioRepository.save(e);
    }

    private static PageResult<Notificacao> pagina(Page<NotificacaoEntity> p) {
        return new PageResult<>(p.getContent().stream().map(NotificacaoRepositoryImpl::toDomain).toList(),
                p.getNumber(), p.getSize(), p.getTotalElements(), p.getTotalPages(), p.isFirst(), p.isLast());
    }

    private static Notificacao toDomain(NotificacaoEntity e) {
        return Notificacao.reconstruir(NotificacaoId.from(e.getId()),
                e.getDestinatarioId() != null ? FuncionarioId.from(e.getDestinatarioId()) : null,
                e.getPerfil() != null ? PerfilDestino.valueOf(e.getPerfil()) : null,
                tipo(e.getTipo()), e.getTitulo(), e.getTexto(), e.getRecursoTipo(), e.getRecursoId(),
                e.getCriadaEm(), e.getLidaEm());
    }

    /** Um tipo que deixou de existir no código lê-se como aviso genérico, em vez de rebentar a caixa. */
    private static TipoNotificacao tipo(String valor) {
        try {
            return TipoNotificacao.valueOf(valor);
        } catch (IllegalArgumentException | NullPointerException e) {
            return TipoNotificacao.AVISO;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existeSobre(TipoNotificacao tipo, String recursoTipo, String recursoId) {
        return entityRepository.existsByTipoAndRecursoTipoAndRecursoId(tipo.name(), recursoTipo, recursoId);
    }
}
