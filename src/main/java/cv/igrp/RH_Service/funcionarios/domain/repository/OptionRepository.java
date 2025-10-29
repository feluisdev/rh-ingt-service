package cv.igrp.RH_Service.funcionarios.domain.repository;

import cv.igrp.RH_Service.funcionarios.domain.filter.OptionFilter;
import cv.igrp.RH_Service.funcionarios.domain.models.Option;
import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;

import java.util.List;
import java.util.Optional;

public interface OptionRepository {

  Option save(Option option);

  Optional<Option> getById(ExternalID optionId);

  List<Option> getAll();

  List<Option> getAll(OptionFilter filter);

  boolean existsByCkeyAndCcodeAndLocale(String ckey, String ccode, String locale);
}
