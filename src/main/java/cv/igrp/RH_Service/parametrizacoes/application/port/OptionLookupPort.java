package cv.igrp.RH_Service.parametrizacoes.application.port;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public interface OptionLookupPort {

    Optional<OptionDTO> findByCcodeAndCkey(String ccode, String ckey);

    Map<String, OptionDTO> findAllByCcodeAndCkeys(String ccode, Collection<String> ckeys);
}
