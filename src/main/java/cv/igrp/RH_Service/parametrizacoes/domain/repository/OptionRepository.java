package cv.igrp.RH_Service.parametrizacoes.domain.repository;

import cv.igrp.RH_Service.parametrizacoes.domain.filter.OptionFilter;
import cv.igrp.RH_Service.parametrizacoes.domain.models.Option;
import cv.igrp.RH_Service.shared.domain.pagination.PageResult;
import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface OptionRepository {

    Option save(Option option);

    Optional<Option> findById(ExternalID id);

    List<Option> findByCcodeAndLocale(String ccode, String locale, boolean active);

    List<Option> findByCcodeAndCkey(String ccode, String ckey, boolean active);

    List<Option> findAllByCcodeAndCkeyIn(String ccode, Collection<String> ckeys, boolean active);

    boolean existsByCcodeAndCkeyAndLocale(String ccode, String ckey, String locale);

    PageResult<Option> findAll(OptionFilter filter);

    void delete(ExternalID id);
}
