package cv.igrp.RH_Service.colaboradores.infrastructure.persistence.adapters;

import cv.igrp.RH_Service.colaboradores.domain.models.MissaoServico;
import cv.igrp.RH_Service.colaboradores.domain.repository.MissaoServicoRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.MissaoServicoId;
import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.MissaoServicoEntity;
import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.repository.ColabsMissaoServicoEntityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MissaoServicoRepositoryImpl implements MissaoServicoRepository {

    private final ColabsMissaoServicoEntityRepository entityRepository;

    @Transactional
    @Override
    public MissaoServico save(MissaoServico m) {
        var e = entityRepository.findById(m.getId().getValor()).orElseGet(() -> {
            var n = new MissaoServicoEntity();
            n.setId(m.getId().getValor());
            return n;
        });
        e.getParticipantes().clear();
        m.getParticipantes().forEach(p -> e.getParticipantes().add(p.getValor()));
        e.setDestinoTipo(m.getDestinoTipo().name());
        e.setDestino(m.getDestino());
        e.setObjectivo(m.getObjectivo());
        e.setPartida(m.getPartida());
        e.setRegresso(m.getRegresso());
        e.setTransporte(m.getTransporte().name());
        e.setAlojamentoACargo(m.isAlojamentoACargo());
        e.setAdiantamento(m.isAdiantamento());
        e.setEstado(m.getEstado().name());
        e.setPedidoPor(m.getPedidoPor() != null ? m.getPedidoPor().getValor() : null);
        e.setDespacho(m.getDespacho());
        e.setMotivo(m.getMotivo());
        e.setRelatorio(m.getRelatorio());
        e.setDataRelatorio(m.getDataRelatorio());
        return toDomain(entityRepository.save(e));
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<MissaoServico> findById(MissaoServicoId id) {
        return entityRepository.findById(id.getValor()).map(this::toDomain);
    }

    @Transactional(readOnly = true)
    @Override
    public List<MissaoServico> find(MissaoServico.Estado estado, FuncionarioId participante) {
        return entityRepository.find(estado != null ? estado.name() : null, participante != null ? participante.getValor() : null)
                .stream().map(this::toDomain).toList();
    }

    @Transactional(readOnly = true)
    @Override
    public List<MissaoServico> findSobrepostas(FuncionarioId participante, LocalDateTime de, LocalDateTime ate, MissaoServicoId excepto) {
        return entityRepository.findSobrepostas(participante.getValor(), de, ate, excepto.getValor()).stream().map(this::toDomain).toList();
    }

    @Transactional(readOnly = true)
    @Override
    public List<MissaoServico> findQueContamEntre(FuncionarioId participante, LocalDate de, LocalDate ate) {
        return entityRepository.findQueContamEntre(participante.getValor(), de.atStartOfDay(), ate.plusDays(1).atStartOfDay())
                .stream().map(this::toDomain).toList();
    }

    @Transactional(readOnly = true)
    @Override
    public List<MissaoServico> findAutorizadasComRegressoEm(LocalDate dia) {
        return entityRepository.findAutorizadasComRegressoEntre(dia.atStartOfDay(), dia.plusDays(1).atStartOfDay())
                .stream().map(this::toDomain).toList();
    }

    private MissaoServico toDomain(MissaoServicoEntity e) {
        return MissaoServico.reconstruir(MissaoServicoId.from(e.getId()), e.getParticipantes().stream().map(FuncionarioId::from).toList(),
                MissaoServico.Destino.valueOf(e.getDestinoTipo()), e.getDestino(), e.getObjectivo(), e.getPartida(), e.getRegresso(),
                MissaoServico.Transporte.valueOf(e.getTransporte()), Boolean.TRUE.equals(e.getAlojamentoACargo()),
                Boolean.TRUE.equals(e.getAdiantamento()), MissaoServico.Estado.valueOf(e.getEstado()),
                e.getPedidoPor() != null ? FuncionarioId.from(e.getPedidoPor()) : null, e.getDespacho(), e.getMotivo(), e.getRelatorio(),
                e.getDataRelatorio());
    }
}
