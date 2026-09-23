package cv.igrp.RH_Service.parametrizacoes.application.commands;

import cv.igrp.RH_Service.parametrizacoes.application.dto.ParametroFeriasResponseDTO;
import cv.igrp.RH_Service.parametrizacoes.domain.models.ParametroFerias;
import cv.igrp.RH_Service.parametrizacoes.domain.repository.ParametroFeriasRepository;
import cv.igrp.RH_Service.parametrizacoes.domain.valueobject.ParametroFeriasId;
import cv.igrp.RH_Service.parametrizacoes.infrastructure.mappers.ParametroFeriasMapper;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UpdateParametroFeriasCommandHandler implements CommandHandler<UpdateParametroFeriasCommand, ResponseEntity<ParametroFeriasResponseDTO>> {

    private final ParametroFeriasRepository parametroFeriasRepository;
    private final ParametroFeriasMapper parametroFeriasMapper;

    @IgrpCommandHandler
    public ResponseEntity<ParametroFeriasResponseDTO> handle(UpdateParametroFeriasCommand command) {
        var id = ParametroFeriasId.from(command.getParametroFeriasId());
        var dto = command.getParametroFeriasRequest();

        ParametroFerias parametro = parametroFeriasRepository.findById(id)
            .orElseThrow(() -> IgrpResponseStatusException.notFound(
                "Parâmetros de férias não encontrados: " + command.getParametroFeriasId()));

        // O que o pedido omitir fica como estava: muda-se um prazo sem reenviar os outros.
        // Limpa-se o fundamento enviando-o em branco.
        int vigenteDesde = dto.getVigenteDesde() != null ? dto.getVigenteDesde() : parametro.getVigenteDesde();
        if (vigenteDesde != parametro.getVigenteDesde() && parametroFeriasRepository.existsByVigenteDesde(vigenteDesde))
            throw IgrpResponseStatusException.conflict("Já há parâmetros de férias em vigor desde " + vigenteDesde + ".");

        parametro.atualizar(vigenteDesde,
                dto.getPrazoPreferencia() != null ? dto.getPrazoPreferencia() : ParametroFerias.texto(parametro.getPrazoPreferencia()),
                dto.getPrazoMapa() != null ? dto.getPrazoMapa() : ParametroFerias.texto(parametro.getPrazoMapa()),
                dto.getFixacaoInicio() != null ? dto.getFixacaoInicio() : ParametroFerias.texto(parametro.getFixacaoInicio()),
                dto.getFixacaoFim() != null ? dto.getFixacaoFim() : ParametroFerias.texto(parametro.getFixacaoFim()),
                dto.getPeriodoMinimoInterpolado() != null ? dto.getPeriodoMinimoInterpolado() : parametro.getPeriodoMinimoInterpolado(),
                dto.getFundamento() != null ? dto.getFundamento() : parametro.getFundamento());

        return ResponseEntity.ok(parametroFeriasMapper.toDTO(parametroFeriasRepository.save(parametro)));
    }
}
