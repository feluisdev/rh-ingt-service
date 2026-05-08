package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.application.dto.DadosBancariosResponseDTO;
import cv.igrp.RH_Service.colaboradores.domain.repository.DadosBancariosRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.DadosBancariosId;
import cv.igrp.RH_Service.colaboradores.infrastructure.mappers.DadosBancariosMapper;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component("colabsUpdateDadosBancariosCommandHandler")
@RequiredArgsConstructor
public class UpdateDadosBancariosCommandHandler
        implements CommandHandler<UpdateDadosBancariosCommand, ResponseEntity<DadosBancariosResponseDTO>> {

    private final DadosBancariosRepository dadosBancariosRepository;
    private final DadosBancariosMapper mapper;

    @IgrpCommandHandler
    public ResponseEntity<DadosBancariosResponseDTO> handle(UpdateDadosBancariosCommand command) {
        var dto = command.getRequest();
        var id = DadosBancariosId.from(command.getDadosBancariosId());

        var dados = dadosBancariosRepository.findById(id)
                .orElseThrow(() -> IgrpResponseStatusException.notFound(
                        "Dados bancários não encontrados: " + command.getDadosBancariosId()));

        dados.atualizar(
                dto.getBanco() != null ? dto.getBanco() : dados.getBanco(),
                dto.getNumeroConta() != null ? dto.getNumeroConta() : dados.getNumeroConta(),
                dto.getIban() != null ? dto.getIban() : dados.getIban(),
                dto.getNumeroSegurancaSocial() != null ? dto.getNumeroSegurancaSocial() : dados.getNumeroSegurancaSocial()
        );

        return ResponseEntity.ok(mapper.toDTO(dadosBancariosRepository.save(dados)));
    }
}
