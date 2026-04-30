package cv.igrp.RH_Service.parametrizacoes.infrastructure.persistence.adapters;

import cv.igrp.RH_Service.parametrizacoes.domain.filter.OptionFilter;
import cv.igrp.RH_Service.parametrizacoes.domain.models.Option;
import cv.igrp.RH_Service.parametrizacoes.domain.repository.OptionRepository;
import cv.igrp.RH_Service.parametrizacoes.infrastructure.mappers.OptionMapper;
import cv.igrp.RH_Service.parametrizacoes.infrastructure.persistence.entity.OptionEntity;
import cv.igrp.RH_Service.parametrizacoes.infrastructure.persistence.repository.OptionEntityRepository;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class OptionRepositoryImpl implements OptionRepository {

    private final OptionEntityRepository optionEntityRepository;
    private final OptionMapper optionMapper;

    @Transactional
    @Override
    public Option save(Option option) {
        OptionEntity entity = optionMapper.toEntity(option);
        OptionEntity saved = optionEntityRepository.save(entity);
        return optionMapper.toDomain(saved);
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<Option> findById(ExternalID id) {
        return optionEntityRepository.findById(id.getValor())
            .map(optionMapper::toDomain);
    }

    @Transactional(readOnly = true)
    @Override
    public List<Option> findByCcodeAndLocale(String ccode, String locale, boolean active) {
        return optionEntityRepository.findAllByCcodeAndLocaleAndActive(ccode, locale, active)
            .stream()
            .map(optionMapper::toDomain)
            .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public boolean existsByCcodeAndCkeyAndLocale(String ccode, String ckey, String locale) {
        return optionEntityRepository.existsByCkeyAndCcodeAndLocale(ckey, ccode, locale);
    }

    @Transactional(readOnly = true)
    @Override
    public List<Option> findAll(OptionFilter filter) {
        var pageable = PageRequest.of(filter.getPage(), filter.getSize());

        Specification<OptionEntity> spec = (root, query, cb) -> {
            var predicates = cb.conjunction();

            if (filter.getCcode() != null && !filter.getCcode().isBlank()) {
                predicates = cb.and(predicates,
                    cb.equal(root.get("ccode"), filter.getCcode().trim()));
            }

            if (filter.getLocale() != null && !filter.getLocale().isBlank()) {
                predicates = cb.and(predicates,
                    cb.equal(root.get("locale"), filter.getLocale().trim()));
            }

            if (filter.getCkey() != null && !filter.getCkey().isBlank()) {
                predicates = cb.and(predicates,
                    cb.like(cb.lower(root.get("ckey")), "%" + filter.getCkey().trim().toLowerCase() + "%"));
            }

            if (filter.getActive() != null) {
                predicates = cb.and(predicates, cb.equal(root.get("active"), filter.getActive()));
            } else {
                predicates = cb.and(predicates, cb.equal(root.get("active"), true));
            }

            return predicates;
        };

        return optionEntityRepository.findAll(spec, pageable)
            .stream()
            .map(optionMapper::toDomain)
            .toList();
    }

    @Transactional
    @Override
    public void delete(ExternalID id) {
        if (!optionEntityRepository.existsById(id.getValor())) {
            throw IgrpResponseStatusException.notFound("Etiqueta não encontrada com id: " + id.getStringValor());
        }
        optionEntityRepository.deleteById(id.getValor());
    }
}
