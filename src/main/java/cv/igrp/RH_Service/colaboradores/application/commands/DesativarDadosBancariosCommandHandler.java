package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.domain.repository.DadosBancariosRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.DadosBancariosId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component("colabsDesativarDadosBancariosCommandHandler")
@RequiredArgsConstructor
public class DesativarDadosBancariosCommandHandler
        implements CommandHandler<DesativarDadosBancariosCommand, ResponseEntity<Map<String, ?>>> {

    private final DadosBancariosRepository dadosBancariosRepository;

    @IgrpCommandHandler
    public ResponseEntity<Map<String, ?>> handle(DesativarDadosBancariosCommand command) {
        var id = DadosBancariosId.from(command.getDadosBancariosId());
        var dados = dadosBancariosRepository.findById(id)
                .orElseThrow(() -> IgrpResponseStatusException.notFound(
                        "Dados bancários não encontrados: " + command.getDadosBancariosId()));

        if (Boolean.FALSE.equals(dados.getIsActive()))
            throw IgrpResponseStatusException.badRequest("Os dados bancários já estão inactivos.");

        dados.desativar();
        dadosBancariosRepository.save(dados);
        return ResponseEntity.ok(Map.of("message", "Desactivado com sucesso"));
    }
}
