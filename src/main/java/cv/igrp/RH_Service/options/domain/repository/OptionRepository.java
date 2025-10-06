package cv.igrp.RH_Service.options.domain.repository;



import cv.igrp.RH_Service.options.domain.filter.OptionFilter;
import cv.igrp.RH_Service.options.domain.models.Option;
import cv.igrp.RH_Service.options.domain.valueobject.OptionId;

import java.util.List;
import java.util.Optional;

public interface OptionRepository {
  Option save(Option option);

  Optional<Option> findById(OptionId id);

  List<Option> findAll();

  List<Option> findAll(OptionFilter filter);

  void delete(OptionId id);

  boolean existsByCkeyAndCcodeAndLocale(String ckey, String ccode,String locale);

  boolean existsById(OptionId id);

  boolean existsByCcode(String ccode);

  List<Option> findByCcode(String ccode);

  boolean existsByCkeyAndCcode(String ckey, String ccode);


}
