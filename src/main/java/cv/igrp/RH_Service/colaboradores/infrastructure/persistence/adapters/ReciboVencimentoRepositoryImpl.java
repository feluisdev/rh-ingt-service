package cv.igrp.RH_Service.colaboradores.infrastructure.persistence.adapters;

import cv.igrp.RH_Service.colaboradores.domain.filter.ReciboFilter;
import cv.igrp.RH_Service.colaboradores.domain.models.ReciboVencimento;
import cv.igrp.RH_Service.colaboradores.domain.repository.ReciboVencimentoRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.ReciboVencimentoId;
import cv.igrp.RH_Service.colaboradores.infrastructure.mappers.ReciboVencimentoMapper;
import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.repository.ColabsReciboVencimentoEntityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository("colabsReciboVencimentoRepositoryImpl")
@RequiredArgsConstructor
public class ReciboVencimentoRepositoryImpl implements ReciboVencimentoRepository {

    private final ColabsReciboVencimentoEntityRepository entityRepository;
    private final ReciboVencimentoMapper mapper;

    @Transactional
    @Override
    public ReciboVencimento save(ReciboVencimento recibo) {
        return mapper.toDomain(entityRepository.save(mapper.toEntity(recibo)));
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<ReciboVencimento> findById(ReciboVencimentoId id) {
        return entityRepository.findById(id.getValor()).map(mapper::toDomain);
    }

    @Transactional(readOnly = true)
    @Override
    public List<ReciboVencimento> findAllByFuncionarioId(FuncionarioId funcionarioId, ReciboFilter filter) {
        if (filter == null) {
            return entityRepository.findAllByFuncionarioId(funcionarioId.getValor())
                    .stream().map(mapper::toDomain).toList();
        }
        if (filter.getPeriodYear() != null && filter.getPeriodMonth() != null) {
            return entityRepository.findAllByFuncionarioIdAndPeriodYearAndPeriodMonth(
                            funcionarioId.getValor(), filter.getPeriodYear(), filter.getPeriodMonth())
                    .stream().map(mapper::toDomain).toList();
        }
        if (filter.getPeriodYear() != null) {
            return entityRepository.findAllByFuncionarioIdAndPeriodYear(funcionarioId.getValor(), filter.getPeriodYear())
                    .stream().map(mapper::toDomain).toList();
        }
        return entityRepository.findAllByFuncionarioId(funcionarioId.getValor())
                .stream().map(mapper::toDomain).toList();
    }

    @Transactional(readOnly = true)
    @Override
    public boolean existsByFuncionarioIdAndPeriod(FuncionarioId funcionarioId, Integer periodMonth, Integer periodYear) {
        return entityRepository.existsByFuncionarioIdAndPeriodMonthAndPeriodYear(
                funcionarioId.getValor(), periodMonth, periodYear);
    }
}
