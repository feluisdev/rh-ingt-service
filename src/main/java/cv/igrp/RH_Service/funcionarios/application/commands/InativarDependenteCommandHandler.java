package cv.igrp.RH_Service.funcionarios.application.commands;

import cv.igrp.RH_Service.funcionarios.domain.repository.DependenteRepository;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@Component
public class InativarDependenteCommandHandler implements CommandHandler<InativarDependenteCommand, ResponseEntity<Map<String, ?>>> {

   private static final Logger LOGGER = LoggerFactory.getLogger(InativarDependenteCommandHandler.class);

  private final DependenteRepository dependenteRepository;

  public InativarDependenteCommandHandler(DependenteRepository dependenteRepository) {
    this.dependenteRepository = dependenteRepository;
  }

  @IgrpCommandHandler
  public ResponseEntity<Map<String, ?>> handle(InativarDependenteCommand command) {
    var externalId = ExternalID.from(command.getDependenteId());
    var funcionarioId = ExternalID.from(command.getFuncionarioId());

    var dependente = dependenteRepository.getById(externalId)
        .orElseThrow(() -> IgrpResponseStatusException.of(
            HttpStatus.NOT_FOUND, "Dependente não encontrado com id: " + externalId.getStringValor()
        ));

    if (!dependente.getFuncionario().getIdFuncionario().equals(funcionarioId)) {
      throw IgrpResponseStatusException.of(HttpStatus.FORBIDDEN, "Dependente não pertence ao funcionário informado.");
    }

    dependente.desativar();

    dependenteRepository.save(dependente);

    Map<String, Object> response = Map.of(
        "mensagem", "Dependente inativado com sucesso.",
        "dependenteId", externalId.getStringValor()
    );

    return ResponseEntity.ok(response);
   }

}
