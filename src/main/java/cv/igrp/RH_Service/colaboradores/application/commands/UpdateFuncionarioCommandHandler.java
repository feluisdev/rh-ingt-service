package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.application.dto.FuncionarioResponseDTO;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.infrastructure.mappers.FuncionarioMapper;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component("colabsUpdateFuncionarioCommandHandler")
@RequiredArgsConstructor
public class UpdateFuncionarioCommandHandler
        implements CommandHandler<UpdateFuncionarioCommand, ResponseEntity<FuncionarioResponseDTO>> {

    private final FuncionarioRepository funcionarioRepository;
    private final FuncionarioMapper mapper;

    @IgrpCommandHandler
    public ResponseEntity<FuncionarioResponseDTO> handle(UpdateFuncionarioCommand command) {
        var dto = command.getRequest();
        var id = FuncionarioId.from(command.getFuncionarioId());

        var funcionario = funcionarioRepository.findById(id)
                .orElseThrow(() -> IgrpResponseStatusException.notFound("Funcionário não encontrado: " + command.getFuncionarioId()));

        if (dto.getNif() != null && funcionarioRepository.existsByNifAndIdNot(dto.getNif(), id)) {
            throw IgrpResponseStatusException.conflict("Já existe um funcionário com NIF '" + dto.getNif() + "'.");
        }
        if (dto.getBiNumero() != null && funcionarioRepository.existsByBiNumeroAndIdNot(dto.getBiNumero(), id)) {
            throw IgrpResponseStatusException.conflict("Já existe um funcionário com BI '" + dto.getBiNumero() + "'.");
        }

        // situacaoProfissional é gerida automaticamente via contrato — não é editável aqui
        if (dto.getDataSaida() != null && "ATIVO".equalsIgnoreCase(funcionario.getSituacaoProfissional())) {
            throw IgrpResponseStatusException.badRequest("O campo dataSaida não pode ser preenchido quando situacaoProfissional é ATIVO.");
        }

        funcionario.atualizar(
                dto.getNomeCompleto() != null ? dto.getNomeCompleto() : funcionario.getNomeCompleto(),
                dto.getDataNascimento() != null ? dto.getDataNascimento() : funcionario.getDataNascimento(),
                dto.getGenero() != null ? dto.getGenero() : funcionario.getGenero(),
                dto.getEstadoCivil() != null ? dto.getEstadoCivil() : funcionario.getEstadoCivil(),
                dto.getNif() != null ? dto.getNif() : funcionario.getNif(),
                dto.getBiNumero() != null ? dto.getBiNumero() : funcionario.getBiNumero(),
                dto.getBiValidade() != null ? dto.getBiValidade() : funcionario.getBiValidade(),
                dto.getNacionalidade() != null ? dto.getNacionalidade() : funcionario.getNacionalidade(),
                dto.getEmail() != null ? dto.getEmail() : funcionario.getEmail(),
                dto.getTelefone() != null ? dto.getTelefone() : funcionario.getTelefone(),
                dto.getMorada() != null ? dto.getMorada() : funcionario.getMorada(),
                dto.getFotoUrl() != null ? dto.getFotoUrl() : funcionario.getFotoUrl(),
                funcionario.getSituacaoProfissional(),
                dto.getDataAdmissao() != null ? dto.getDataAdmissao() : funcionario.getDataAdmissao(),
                dto.getDataSaida() != null ? dto.getDataSaida() : funcionario.getDataSaida()
        );

        return ResponseEntity.ok(mapper.toDTO(funcionarioRepository.save(funcionario)));
    }
}
