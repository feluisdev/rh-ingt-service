package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.application.dto.ImportacaoMarcacoesResponseDTO;
import cv.igrp.RH_Service.colaboradores.application.dto.RejeicaoImportacaoDTO;
import cv.igrp.RH_Service.colaboradores.application.services.AssiduidadeService;
import cv.igrp.RH_Service.colaboradores.application.services.AssiduidadeService.Picagem;
import cv.igrp.RH_Service.colaboradores.application.services.AssiduidadeService.Rejeicao;
import cv.igrp.RH_Service.colaboradores.domain.models.SentidoMarcacao;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Picagens de um relógio, num formato genérico: o adaptador de cada marca de relógio converte para
 * este. Um sentido que não se perceba rejeita só essa picagem.
 */
@Component
@RequiredArgsConstructor
public class ImportarMarcacoesCommandHandler implements CommandHandler<ImportarMarcacoesCommand, ResponseEntity<ImportacaoMarcacoesResponseDTO>> {

    private final AssiduidadeService assiduidadeService;

    @IgrpCommandHandler
    public ResponseEntity<ImportacaoMarcacoesResponseDTO> handle(ImportarMarcacoesCommand command) {
        List<Picagem> picagens = new ArrayList<>();
        List<Rejeicao> sentidoInvalido = new ArrayList<>();
        var lista = command.getRequest() != null ? command.getRequest().getPicagens() : null;
        if (lista != null) {
            for (var p : lista) {
                SentidoMarcacao sentido = null;
                if (p.getSentido() != null && !p.getSentido().isBlank()) {
                    try {
                        sentido = SentidoMarcacao.valueOf(p.getSentido().trim());
                    } catch (IllegalArgumentException e) {
                        sentidoInvalido.add(new Rejeicao(p.getReferenciaExterna(), "sentido inválido: '" + p.getSentido() + "'."));
                        continue;
                    }
                }
                picagens.add(new Picagem(p.getFuncionarioId(), p.getNumeroFuncionario(), p.getMomento(), sentido,
                        p.getReferenciaExterna()));
            }
        }

        var relatorio = picagens.isEmpty() && !sentidoInvalido.isEmpty()
                ? new AssiduidadeService.Relatorio(0, 0, List.of())
                : assiduidadeService.importar(picagens);

        var dto = new ImportacaoMarcacoesResponseDTO();
        dto.setImportadas(relatorio.importadas());
        dto.setDuplicadas(relatorio.duplicadas());
        List<RejeicaoImportacaoDTO> rejeitadas = new ArrayList<>();
        sentidoInvalido.forEach(r -> rejeitadas.add(new RejeicaoImportacaoDTO(r.referenciaExterna(), r.motivo())));
        relatorio.rejeitadas().forEach(r -> rejeitadas.add(new RejeicaoImportacaoDTO(r.referenciaExterna(), r.motivo())));
        dto.setRejeitadas(rejeitadas);
        return ResponseEntity.ok(dto);
    }
}
