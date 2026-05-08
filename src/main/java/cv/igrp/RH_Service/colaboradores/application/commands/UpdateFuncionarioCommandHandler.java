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
                .orElseThrow(() -> IgrpResponseStatusException.notFound(
                        "Funcionário não encontrado: " + command.getFuncionarioId()));

        if (dto.getNif() != null && funcionarioRepository.existsByNifAndIdNot(dto.getNif(), id)) {
            throw IgrpResponseStatusException.conflict("Já existe um funcionário com NIF '" + dto.getNif() + "'.");
        }
        if (dto.getNumeroDocumento() != null && !dto.getNumeroDocumento().isBlank()
                && funcionarioRepository.existsByNumeroDocumentoAndIdNot(dto.getNumeroDocumento(), id)) {
            throw IgrpResponseStatusException.conflict(
                    "Já existe um funcionário com número de documento '" + dto.getNumeroDocumento() + "'.");
        }

        funcionario.atualizar(
                dto.getNomeCompleto() != null ? dto.getNomeCompleto() : funcionario.getNomeCompleto(),
                dto.getDataNascimento() != null ? dto.getDataNascimento() : funcionario.getDataNascimento(),
                dto.getGenero() != null ? dto.getGenero() : funcionario.getGenero(),
                dto.getEstadoCivil() != null ? dto.getEstadoCivil() : funcionario.getEstadoCivil(),
                dto.getNif() != null ? dto.getNif() : funcionario.getNif(),
                dto.getDocumentTypeId() != null ? dto.getDocumentTypeId() : funcionario.getDocumentTypeId(),
                dto.getNumeroDocumento() != null ? dto.getNumeroDocumento() : funcionario.getNumeroDocumento(),
                dto.getDataEmissaoDoc() != null ? dto.getDataEmissaoDoc() : funcionario.getDataEmissaoDoc(),
                dto.getDataValidadeDoc() != null ? dto.getDataValidadeDoc() : funcionario.getDataValidadeDoc(),
                dto.getNacionalidade() != null ? dto.getNacionalidade() : funcionario.getNacionalidade(),
                dto.getEmail() != null ? dto.getEmail() : funcionario.getEmail(),
                dto.getTelefone() != null ? dto.getTelefone() : funcionario.getTelefone(),
                dto.getMorada() != null ? dto.getMorada() : funcionario.getMorada(),
                dto.getIlha() != null ? dto.getIlha() : funcionario.getIlha(),
                dto.getConcelho() != null ? dto.getConcelho() : funcionario.getConcelho(),
                dto.getLocalidade() != null ? dto.getLocalidade() : funcionario.getLocalidade(),
                funcionario.getSituacaoProfissional(),
                dto.getDataAdmissao() != null ? dto.getDataAdmissao() : funcionario.getDataAdmissao()
        );

        return ResponseEntity.ok(mapper.toDTO(funcionarioRepository.save(funcionario)));
    }
}
