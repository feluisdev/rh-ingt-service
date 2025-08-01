package cv.igrp.RH_Service.funcionarios.application.commands;

import cv.igrp.RH_Service.funcionarios.domain.models.Departamento;
import cv.igrp.RH_Service.funcionarios.domain.repository.DepartamentoRepository;
import cv.igrp.RH_Service.funcionarios.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@Component
public class CreateDepartamentoCommandHandler implements CommandHandler<CreateDepartamentoCommand, ResponseEntity<Map<String, ?>>> {

  private static final Logger LOGGER = LoggerFactory.getLogger(CreateDepartamentoCommandHandler.class);

  private final DepartamentoRepository departamentoRepository;
  private final FuncionarioRepository funcionarioRepository;

  public CreateDepartamentoCommandHandler(DepartamentoRepository departamentoRepository, FuncionarioRepository funcionarioRepository) {

    this.departamentoRepository = departamentoRepository;
    this.funcionarioRepository = funcionarioRepository;
  }

  @IgrpCommandHandler
  public ResponseEntity<Map<String, ?>> handle(CreateDepartamentoCommand command) {
    var dto = command.getDepartamentorequest();

    var responsavelId = ExternalID.from(dto.getResponsavelId());

    var responsavel = funcionarioRepository.getById(responsavelId)
        .orElseThrow(() -> IgrpResponseStatusException.notFound("Responsável não encontrado com ID: " + responsavelId.getStringValor()));

    var departamento = Departamento.criarNovo(
        dto.getNome(),
        dto.getCodigo(),
        dto.getDescricao(),
        dto.getLocalizacao(),
        dto.getOrcamento(),
        responsavel
    );
    departamentoRepository.save(departamento);

    Map<String, Object> response = Map.of(
        "departamentoId", departamento.getIdDepartamento().getStringValor(),
        "message", "Departamento criado com sucesso"
    );

    return ResponseEntity.ok(response);
  }

}
