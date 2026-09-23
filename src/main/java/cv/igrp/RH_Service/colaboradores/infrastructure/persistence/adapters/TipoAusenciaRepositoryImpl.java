package cv.igrp.RH_Service.colaboradores.infrastructure.persistence.adapters;

import cv.igrp.RH_Service.colaboradores.domain.filter.TipoAusenciaFilter;
import cv.igrp.RH_Service.colaboradores.domain.models.TipoAusencia;
import cv.igrp.RH_Service.colaboradores.domain.repository.TipoAusenciaRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.TipoAusenciaId;
import cv.igrp.RH_Service.parametrizacoes.domain.models.ContagemDias;
import cv.igrp.RH_Service.parametrizacoes.domain.models.RegimeAusencia;
import cv.igrp.RH_Service.parametrizacoes.infrastructure.persistence.entity.LeaveTypeEntity;
import cv.igrp.RH_Service.parametrizacoes.infrastructure.persistence.repository.LeaveTypeEntityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository("colabsTipoAusenciaRepositoryImpl")
@RequiredArgsConstructor
public class TipoAusenciaRepositoryImpl implements TipoAusenciaRepository {

    private final LeaveTypeEntityRepository entityRepository;

    /**
     * Uma classificação que o esquema já não deixa entrar (V49, ck_leave_type_regime) mas que
     * pode existir numa base anterior à migração: em vez de rebentar, trata-se como FALTA, que é
     * o regime que não produz efeitos automáticos.
     */
    private RegimeAusencia regimeDe(LeaveTypeEntity e) {
        if (e.getRegime() == null) return null;
        try {
            return RegimeAusencia.valueOf(e.getRegime());
        } catch (IllegalArgumentException ex) {
            return RegimeAusencia.FALTA;
        }
    }

    /** Como o regime: um valor que o esquema já não deixa entrar lê-se como DIAS_UTEIS. */
    private ContagemDias contagemDe(LeaveTypeEntity e) {
        if (e.getContagem() == null) return null;
        try {
            return ContagemDias.valueOf(e.getContagem());
        } catch (IllegalArgumentException ex) {
            return ContagemDias.DIAS_UTEIS;
        }
    }

    private TipoAusencia toDomain(LeaveTypeEntity e) {
        return TipoAusencia.reconstituir(
                TipoAusenciaId.from(e.getId()),
                e.getDescription(), e.getCode(),
                e.getDeductsBalance(), e.getRequiresApproval(),
                e.getMaxDaysPerYear(),
                e.getMaxDaysPerOccurrence(),
                e.getMaxDaysPerMonth(),
                e.getCategory(),
                e.getIsActive(),
                regimeDe(e),
                cv.igrp.RH_Service.parametrizacoes.domain.models.EfeitoNaRemuneracao
                        .de(e.getEfeitoRemuneracao()),
                contagemDe(e))
                .comMaxMinutosPorDia(e.getMaxMinutosPorDia());
    }

    private LeaveTypeEntity toEntity(TipoAusencia t) {
        LeaveTypeEntity e = new LeaveTypeEntity();
        e.setId(t.getId().getValor());
        e.setDescription(t.getNome());
        e.setCode(t.getCodigo());
        e.setDeductsBalance(t.getDeductsBalance());
        e.setRequiresApproval(t.getRequiresApproval());
        e.setMaxDaysPerYear(t.getMaxDaysPerYear());
        e.setMaxDaysPerOccurrence(t.getMaxDaysPerOccurrence());
        e.setMaxDaysPerMonth(t.getMaxDaysPerMonth());
        e.setCategory(t.getCategoryOptionCkey());
        e.setIsActive(t.getIsActive());
        e.setRegime(t.getRegime() != null ? t.getRegime().name() : null);
        e.setEfeitoRemuneracao(t.getEfeitoRemuneracao() != null ? t.getEfeitoRemuneracao().name() : null);
        e.setContagem(t.getContagem().name());
        e.setMaxMinutosPorDia(t.getMaxMinutosPorDia());
        return e;
    }

    @Transactional
    @Override
    public TipoAusencia save(TipoAusencia tipoAusencia) {
        return toDomain(entityRepository.save(toEntity(tipoAusencia)));
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<TipoAusencia> findById(TipoAusenciaId id) {
        return entityRepository.findById(id.getValor()).map(this::toDomain);
    }

    @Transactional(readOnly = true)
    @Override
    public List<TipoAusencia> findAll(TipoAusenciaFilter filter) {
        if (filter.getActive() != null)
            return entityRepository.findAllByIsActive(filter.getActive()).stream().map(this::toDomain).toList();
        return entityRepository.findAll().stream().map(this::toDomain).toList();
    }

    @Transactional(readOnly = true)
    @Override
    public boolean existsByCodigo(String codigo) {
        return entityRepository.existsByCode(codigo);
    }

    @Transactional(readOnly = true)
    @Override
    public boolean existsByCodigoAndIdNot(String codigo, TipoAusenciaId id) {
        return entityRepository.existsByCodeAndIdNot(codigo, id.getValor());
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<TipoAusencia> findFerias() {
        return entityRepository.findAllByRegimeAndIsActive(RegimeAusencia.FERIAS.name(), true)
                .stream().findFirst().map(this::toDomain);
    }
}
