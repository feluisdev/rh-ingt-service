package cv.igrp.RH_Service.parametrizacoes.application.commands;

import cv.igrp.RH_Service.parametrizacoes.domain.models.ParametroFerias;
import cv.igrp.RH_Service.parametrizacoes.domain.repository.ParametroFeriasRepository;
import cv.igrp.RH_Service.shared.application.dto.SuccessResponseDTO;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

/** Uma vigência nova dos parâmetros do mapa de férias — um diploma novo, a partir de um ano. */
@Component
@RequiredArgsConstructor
public class CreateParametroFeriasCommandHandler implements CommandHandler<CreateParametroFeriasCommand, ResponseEntity<SuccessResponseDTO>> {

    private final ParametroFeriasRepository parametroFeriasRepository;

    @IgrpCommandHandler
    public ResponseEntity<SuccessResponseDTO> handle(CreateParametroFeriasCommand command) {
        var dto = command.getParametroFeriasRequest();

        var parametro = ParametroFerias.criar(dto.getVigenteDesde(), dto.getPrazoPreferencia(),
                dto.getPrazoMapa(), dto.getFixacaoInicio(), dto.getFixacaoFim(),
                dto.getPeriodoMinimoInterpolado(), dto.getFundamento());

        if (parametroFeriasRepository.existsByVigenteDesde(parametro.getVigenteDesde()))
            throw IgrpResponseStatusException.conflict("Já há parâmetros de férias em vigor desde "
                    + parametro.getVigenteDesde() + ". Altere essa linha.");

        var saved = parametroFeriasRepository.save(parametro);
        return ResponseEntity.status(201).body(SuccessResponseDTO.de(saved.getId().getStringValor()));
    }
}
