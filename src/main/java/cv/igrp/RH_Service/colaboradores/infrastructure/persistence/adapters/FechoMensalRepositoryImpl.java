package cv.igrp.RH_Service.colaboradores.infrastructure.persistence.adapters;

import cv.igrp.RH_Service.colaboradores.domain.models.FechoMensal;
import cv.igrp.RH_Service.colaboradores.domain.repository.FechoMensalRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FechoMensalId;
import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.FechoMensalEntity;
import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.repository.ColabsFechoMensalEntityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class FechoMensalRepositoryImpl implements FechoMensalRepository {

    private final ColabsFechoMensalEntityRepository entityRepository;

    @Transactional
    @Override
    public FechoMensal save(FechoMensal f) {
        var e = entityRepository.findById(f.getId().getValor()).orElseGet(() -> {
            var n = new FechoMensalEntity();
            n.setId(f.getId().getValor());
            n.setMes(f.getMes().toString());
            return n;
        });
        e.setEstado(f.getEstado().name());
        e.setFechadoEm(f.getFechadoEm());
        e.setFechos(f.getFechos());
        e.setMotivoReabertura(f.getMotivoReabertura());
        e.setReabertoEm(f.getReabertoEm());
        e.setRelacao(f.getRelacao());
        e.setTotalFactos(f.getTotalFactos());
        return toDomain(entityRepository.save(e));
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<FechoMensal> findByMes(YearMonth mes) {
        return entityRepository.findByMes(mes.toString()).map(this::toDomain);
    }

    @Transactional(readOnly = true)
    @Override
    public List<FechoMensal> findAll() {
        return entityRepository.findTodos().stream().map(this::toDomain).toList();
    }

    @Transactional(readOnly = true)
    @Override
    public Set<YearMonth> mesesFechadosDesde(YearMonth mes) {
        return entityRepository.mesesFechadosDesde(mes.toString()).stream().map(YearMonth::parse).collect(Collectors.toSet());
    }

    private FechoMensal toDomain(FechoMensalEntity e) {
        return FechoMensal.reconstruir(FechoMensalId.from(e.getId()), YearMonth.parse(e.getMes()), FechoMensal.Estado.valueOf(e.getEstado()),
                e.getFechadoEm(), e.getFechos() != null ? e.getFechos() : 1, e.getMotivoReabertura(), e.getReabertoEm(), e.getRelacao(),
                e.getTotalFactos() != null ? e.getTotalFactos() : 0);
    }
}
