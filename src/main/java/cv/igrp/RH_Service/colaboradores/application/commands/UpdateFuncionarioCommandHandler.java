package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.application.services.DadosBancariosService;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Component("colabsUpdateFuncionarioCommandHandler")
@RequiredArgsConstructor
public class UpdateFuncionarioCommandHandler
        implements CommandHandler<UpdateFuncionarioCommand, ResponseEntity<Map<String, String>>> {

    private final FuncionarioRepository funcionarioRepository;
    private final DadosBancariosService dadosBancariosService;

    @IgrpCommandHandler
    @Transactional
    public ResponseEntity<Map<String, String>> handle(UpdateFuncionarioCommand command) {
        var dp = command.getRequest().getDadosPessoais();
        var id = FuncionarioId.from(command.getFuncionarioId());

        var funcionario = funcionarioRepository.findById(id)
                .orElseThrow(() -> IgrpResponseStatusException.notFound(
                        "Funcionário não encontrado: " + command.getFuncionarioId()));

        if (dp.getNif() != null && funcionarioRepository.existsByNifAndIdNot(dp.getNif(), id)) {
            throw IgrpResponseStatusException.conflict("Já existe um funcionário com NIF '" + dp.getNif() + "'.");
        }
        if (dp.getNumeroDocumento() != null && !dp.getNumeroDocumento().isBlank()
                && funcionarioRepository.existsByNumeroDocumentoAndIdNot(dp.getNumeroDocumento(), id)) {
            throw IgrpResponseStatusException.conflict(
                    "Já existe um funcionário com número de documento '" + dp.getNumeroDocumento() + "'.");
        }

        funcionario.atualizar(
                dp.getNomeCompleto() != null ? dp.getNomeCompleto() : funcionario.getNomeCompleto(),
                dp.getDataNascimento() != null ? dp.getDataNascimento() : funcionario.getDataNascimento(),
                dp.getGenero() != null ? dp.getGenero() : funcionario.getGenero(),
                dp.getEstadoCivil() != null ? dp.getEstadoCivil() : funcionario.getEstadoCivil(),
                dp.getNif() != null ? dp.getNif() : funcionario.getNif(),
                dp.getDocumentTypeId() != null ? dp.getDocumentTypeId() : funcionario.getDocumentTypeId(),
                dp.getNumeroDocumento() != null ? dp.getNumeroDocumento() : funcionario.getNumeroDocumento(),
                dp.getDataEmissaoDoc() != null ? dp.getDataEmissaoDoc() : funcionario.getDataEmissaoDoc(),
                dp.getDataValidadeDoc() != null ? dp.getDataValidadeDoc() : funcionario.getDataValidadeDoc(),
                dp.getNacionalidade() != null ? dp.getNacionalidade() : funcionario.getNacionalidade(),
                dp.getEmail() != null ? dp.getEmail() : funcionario.getEmail(),
                dp.getTelefone() != null ? dp.getTelefone() : funcionario.getTelefone(),
                dp.getMorada() != null ? dp.getMorada() : funcionario.getMorada(),
                dp.getIlha() != null ? dp.getIlha() : funcionario.getIlha(),
                dp.getConcelho() != null ? dp.getConcelho() : funcionario.getConcelho(),
                dp.getLocalidade() != null ? dp.getLocalidade() : funcionario.getLocalidade(),
                dp.getDataAdmissao() != null ? dp.getDataAdmissao() : funcionario.getDataAdmissao()
        );
        funcionarioRepository.save(funcionario);

        var dbReq = command.getRequest().getDadosBancarios();
        if (dbReq != null) {
            dadosBancariosService.upsertDadosBancarios(id, dbReq);
        }

        return ResponseEntity.ok(Map.of("id", id.getStringValor()));
    }
}
