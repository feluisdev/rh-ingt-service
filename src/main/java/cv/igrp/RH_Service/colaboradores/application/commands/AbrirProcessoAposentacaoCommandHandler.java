package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.application.dto.ProcessoAposentacaoDTO;
import cv.igrp.RH_Service.colaboradores.application.queries.AposentacaoDtos;
import cv.igrp.RH_Service.colaboradores.application.services.AposentacaoService;
import cv.igrp.RH_Service.colaboradores.domain.models.ModalidadeAposentacao;
import cv.igrp.RH_Service.colaboradores.domain.models.ProcessoAposentacao;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.shared.domain.service.CurrentEmployeeResolver;
import cv.igrp.RH_Service.shared.domain.service.Entrada;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AbrirProcessoAposentacaoCommandHandler
        implements CommandHandler<AbrirProcessoAposentacaoCommand, ResponseEntity<ProcessoAposentacaoDTO>> {

    private final AposentacaoService aposentacaoService;
    private final CurrentEmployeeResolver currentEmployeeResolver;

    @IgrpCommandHandler
    public ResponseEntity<ProcessoAposentacaoDTO> handle(AbrirProcessoAposentacaoCommand command) {
        var req = Entrada.corpo(command.getRequest(), "a modalidade da aposentação");
        FuncionarioId funcionarioId = command.isPeloProprio() ? currentEmployeeResolver.resolve()
                : FuncionarioId.from(Entrada.uuid(command.getFuncionarioId(), "o colaborador"));
        var p = aposentacaoService.abrir(funcionarioId, ModalidadeAposentacao.de(req.getModalidade()),
                command.isPeloProprio() ? ProcessoAposentacao.Iniciativa.FUNCIONARIO : ProcessoAposentacao.Iniciativa.ADMINISTRACAO,
                req.getDataPrevista(), req.getFundamentacao(), Boolean.TRUE.equals(req.getAcordoFuncionario()));
        return ResponseEntity.status(201).body(AposentacaoDtos.dto(p));
    }
}
