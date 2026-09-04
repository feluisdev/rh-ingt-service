package cv.igrp.RH_Service.estrutura.infrastructure.lookup;

// Unico ficheiro de estrutura autorizado a importar colaboradores (D-08,
// 109-02-PLAN.md) -- espelha ponto por ponto sigdi.infrastructure.lookup.
// FuncionarioLookupAdapter, o unico precedente deste alcance cross-modulo no
// repositorio. O portao automatico e:
//   grep -rln "colaboradores\." src/main/java/cv/igrp/RH_Service/estrutura/
// que tem de devolver exatamente este ficheiro.
import cv.igrp.RH_Service.colaboradores.domain.models.Funcionario;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.estrutura.application.port.FuncionarioLookupDTO;
import cv.igrp.RH_Service.estrutura.application.port.FuncionarioLookupPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

// Nome de bean explicito: sigdi.infrastructure.lookup.FuncionarioLookupAdapter tem a
// mesma classe simples, e o Spring atribuiria o mesmo nome de bean por omissao aos
// dois, causando ConflictingBeanDefinitionException no arranque do contexto
// (encontrado ao correr mvn test completo, Task 2, 109-02-PLAN.md -- Rule 1).
@Component("estruturaFuncionarioLookupAdapter")
@RequiredArgsConstructor
public class FuncionarioLookupAdapter implements FuncionarioLookupPort {

    private final FuncionarioRepository repository;

    @Override
    public Optional<FuncionarioLookupDTO> findById(UUID id) {
        return repository.findById(FuncionarioId.from(id)).map(this::toDto);
    }

    @Override
    public Map<UUID, FuncionarioLookupDTO> findAllByIds(Collection<UUID> ids) {
        if (ids == null || ids.isEmpty()) return Map.of();
        return repository.findAllByIds(ids).stream()
                .collect(Collectors.toMap(
                        f -> f.getId().getValor(),
                        this::toDto));
    }

    private FuncionarioLookupDTO toDto(Funcionario funcionario) {
        FuncionarioLookupDTO dto = new FuncionarioLookupDTO();
        dto.setId(funcionario.getId().getValor());
        dto.setNomeCompleto(funcionario.getNomeCompleto());
        return dto;
    }
}
