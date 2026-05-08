package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.domain.models.Funcionario;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.parametrizacoes.domain.repository.WorkerStateRepository;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component("colabsCreateFuncionarioCommandHandler")
@RequiredArgsConstructor
public class CreateFuncionarioCommandHandler
        implements CommandHandler<CreateFuncionarioCommand, ResponseEntity<Map<String, ?>>> {

    private final FuncionarioRepository funcionarioRepository;
    private final WorkerStateRepository workerStateRepository;
    private final JdbcTemplate jdbcTemplate;

    @IgrpCommandHandler
    public ResponseEntity<Map<String, ?>> handle(CreateFuncionarioCommand command) {
        var dto = command.getRequest();

        if (funcionarioRepository.existsByNif(dto.getNif())) {
            throw IgrpResponseStatusException.conflict("Já existe um funcionário com NIF '" + dto.getNif() + "'.");
        }
        if (dto.getNumeroDocumento() != null && !dto.getNumeroDocumento().isBlank()
                && funcionarioRepository.existsByNumeroDocumento(dto.getNumeroDocumento())) {
            throw IgrpResponseStatusException.conflict(
                    "Já existe um funcionário com número de documento '" + dto.getNumeroDocumento() + "'.");
        }

        UUID workerStateId = workerStateRepository.findByCode("ATIVO")
                .orElseThrow(() -> IgrpResponseStatusException.badRequest(
                        "Estado 'ATIVO' não configurado no sistema."))
                .getId().getValor();

        Long seq = jdbcTemplate.queryForObject("SELECT nextval('seq_numero_funcionario')", Long.class);
        String numeroFuncionario = String.format("F%06d", seq);

        Funcionario saved = funcionarioRepository.save(
                Funcionario.criar(numeroFuncionario, dto.getNomeCompleto(), dto.getDataNascimento(),
                        dto.getGenero(), dto.getEstadoCivil(), dto.getNif(),
                        dto.getDocumentTypeId(), dto.getNumeroDocumento(),
                        dto.getDataEmissaoDoc(), dto.getDataValidadeDoc(),
                        dto.getNacionalidade(), dto.getEmail(), dto.getTelefone(),
                        dto.getMorada(), dto.getIlha(), dto.getConcelho(), dto.getLocalidade(),
                        workerStateId, dto.getDataAdmissao()));

        return ResponseEntity.status(201).body(Map.of(
                "id", saved.getId().getStringValor(),
                "numeroFuncionario", saved.getNumeroFuncionario(),
                "message", "Criado com sucesso"));
    }
}
