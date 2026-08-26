package cv.igrp.RH_Service.sigdi.infrastructure.lookup;

import cv.igrp.RH_Service.colaboradores.domain.models.Funcionario;
import cv.igrp.RH_Service.colaboradores.domain.repository.EnquadramentoRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.sigdi.application.dto.FuncionarioDTO;
import cv.igrp.RH_Service.sigdi.application.port.FuncionarioLookupPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class FuncionarioLookupAdapter implements FuncionarioLookupPort {

    private final FuncionarioRepository repository;
    private final EnquadramentoRepository enquadramentoRepository;

    @Override
    public Optional<FuncionarioDTO> findById(UUID id) {
        return repository.findById(FuncionarioId.from(id)).map(this::toDto);
    }

    @Override
    public Map<UUID, FuncionarioDTO> findAllByIds(Collection<UUID> ids) {
        if (ids == null || ids.isEmpty()) return Map.of();
        return repository.findAllByIds(ids).stream()
                .collect(Collectors.toMap(
                        f -> f.getId().getValor(),
                        this::toDto));
    }

    @Override
    public Optional<UUID> findCurrentOrganizationalUnitId(UUID employeeId) {
        return enquadramentoRepository.findCurrentByFuncionarioId(FuncionarioId.from(employeeId))
                .map(e -> e.getUnidadeOrganicaId());
    }

    private FuncionarioDTO toDto(Funcionario funcionario) {
        FuncionarioDTO dto = new FuncionarioDTO();
        dto.setId(funcionario.getId().getStringValor());
        dto.setNomeCompleto(funcionario.getNomeCompleto());
        return dto;
    }
}
