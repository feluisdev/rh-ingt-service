package cv.igrp.RH_Service.colaboradores.infrastructure.persistence.adapters;

import cv.igrp.RH_Service.colaboradores.domain.models.MarcacaoAssiduidade;
import cv.igrp.RH_Service.colaboradores.domain.models.OrigemMarcacao;
import cv.igrp.RH_Service.colaboradores.domain.models.SentidoMarcacao;
import cv.igrp.RH_Service.colaboradores.domain.repository.MarcacaoAssiduidadeRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.MarcacaoAssiduidadeId;
import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.FuncionarioEntity;
import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.MarcacaoAssiduidadeEntity;
import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.repository.ColabsMarcacaoAssiduidadeEntityRepository;
import cv.igrp.RH_Service.shared.infrastructure.persistence.JpaReferences;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MarcacaoAssiduidadeRepositoryImpl implements MarcacaoAssiduidadeRepository {

    private final ColabsMarcacaoAssiduidadeEntityRepository entityRepository;
    private final JpaReferences refs;

    @Transactional
    @Override
    public MarcacaoAssiduidade save(MarcacaoAssiduidade m) {
        MarcacaoAssiduidadeEntity e = entityRepository.findById(m.getId().getValor()).orElseGet(() -> {
            MarcacaoAssiduidadeEntity nova = new MarcacaoAssiduidadeEntity();
            nova.setId(m.getId().getValor());
            nova.setFuncionario(refs.ref(FuncionarioEntity.class, m.getFuncionarioId().getValor()));
            nova.setMomento(m.getMomento());
            nova.setSentido(m.getSentido().name());
            nova.setOrigem(m.getOrigem().name());
            nova.setMotivo(m.getMotivo());
            nova.setReferenciaExterna(m.getReferenciaExterna());
            return nova;
        });
        // Uma marcação nunca muda: só pode ser anulada.
        e.setAnulada(m.isAnulada());
        e.setMotivoAnulacao(m.getMotivoAnulacao());
        e.setAnuladaEm(m.getAnuladaEm());
        return toDomain(entityRepository.save(e));
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<MarcacaoAssiduidade> findById(MarcacaoAssiduidadeId id) {
        return entityRepository.findById(id.getValor()).map(this::toDomain);
    }

    @Transactional(readOnly = true)
    @Override
    public List<MarcacaoAssiduidade> findByFuncionarioEntre(FuncionarioId funcionarioId, LocalDate de, LocalDate ate) {
        return entityRepository.findEntre(funcionarioId.getValor(), de.atStartOfDay(), ate.plusDays(1).atStartOfDay())
                .stream().map(this::toDomain).toList();
    }

    @Transactional(readOnly = true)
    @Override
    public boolean existeValidaNoDia(FuncionarioId funcionarioId, LocalDate data) {
        return entityRepository.existeValidaEntre(funcionarioId.getValor(), data.atStartOfDay(), data.plusDays(1).atStartOfDay());
    }

    @Transactional(readOnly = true)
    @Override
    public boolean existsByReferenciaExterna(String referenciaExterna) {
        return entityRepository.existsByReferenciaExterna(referenciaExterna);
    }

    private MarcacaoAssiduidade toDomain(MarcacaoAssiduidadeEntity e) {
        return MarcacaoAssiduidade.reconstruir(
                MarcacaoAssiduidadeId.from(e.getId()),
                FuncionarioId.from(refs.idOf(e.getFuncionario(), FuncionarioEntity::getId)),
                e.getMomento(),
                SentidoMarcacao.valueOf(e.getSentido()),
                OrigemMarcacao.valueOf(e.getOrigem()),
                e.getMotivo(),
                e.getReferenciaExterna(),
                Boolean.TRUE.equals(e.getAnulada()),
                e.getMotivoAnulacao(),
                e.getAnuladaEm());
    }
}
