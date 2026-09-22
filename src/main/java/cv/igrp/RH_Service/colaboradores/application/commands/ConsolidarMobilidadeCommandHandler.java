package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.application.dto.ConsolidacaoMobilidadeResponseDTO;
import cv.igrp.RH_Service.colaboradores.application.services.AssignmentService;
import cv.igrp.RH_Service.colaboradores.application.services.MobilidadeService;
import cv.igrp.RH_Service.colaboradores.domain.repository.LicencaMobilidadeRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.LicencaMobilidadeId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

/**
 * <b>Consolidação da mobilidade</b> — Lei n.º 20/X/2023, art. 132.º n.º 4: «A mobilidade
 * definitiva ocorre nos casos de consolidação da mobilidade transitória, na mesma função e
 * categoria […].»
 *
 * <p>Junta duas coisas que já existiam e nunca se tinham falado: o registo da mobilidade, que diz
 * onde a pessoa exerce funções, e a afectação, que diz de que Lugar é titular. Consolidar é
 * acabar o período transitório e dar-lhe um Lugar no serviço de destino — o que o art. 135.º
 * n.º 8 chama mobilidade «com ocupação do lugar do quadro», por oposição à transitória do n.º 7.
 *
 * <p><b>Só a mobilidade interna se consolida.</b> Numa mobilidade externa o destino é outra
 * entidade, e não há Lugar nosso onde pôr a pessoa: o que a lei prevê aí é a saída do quadro,
 * que é uma cessação e não este movimento.
 *
 * <p>As regras da grelha e do Lugar vivem em {@link AssignmentService#consolidarMobilidade}.
 */
@Component
@RequiredArgsConstructor
public class ConsolidarMobilidadeCommandHandler
        implements CommandHandler<ConsolidarMobilidadeCommand, ResponseEntity<ConsolidacaoMobilidadeResponseDTO>> {

    private final LicencaMobilidadeRepository licencaRepository;
    private final MobilidadeService mobilidadeService;
    private final AssignmentService assignmentService;

    @IgrpCommandHandler
    @Transactional
    public ResponseEntity<ConsolidacaoMobilidadeResponseDTO> handle(ConsolidarMobilidadeCommand command) {
        var req = command.getRequest();
        if (req == null || req.getDataEfeito() == null)
            throw IgrpResponseStatusException.badRequest("A data de efeito (dataEfeito) é obrigatória.");
        if (req.getPositionId() == null || req.getPositionId().isBlank())
            throw IgrpResponseStatusException.badRequest("O Lugar de destino (positionId) é obrigatório.");

        var funcionarioId = FuncionarioId.from(command.getFuncionarioId());
        var licenca = licencaRepository.findById(LicencaMobilidadeId.from(command.getLicencaId()))
                .orElseThrow(() -> IgrpResponseStatusException.notFound(
                        "Licença/mobilidade não encontrada: " + command.getLicencaId()));

        if (!licenca.getFuncionarioId().equals(funcionarioId))
            throw IgrpResponseStatusException.notFound(
                    "Licença/mobilidade não encontrada: " + command.getLicencaId());

        var subtipo = mobilidadeService.subtipoDe(licenca);
        if (!subtipo.isMobilidade())
            throw IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Só uma mobilidade se consolida (art. 132.º n.º 4). O subtipo '" + subtipo.getNome()
                            + "' é uma licença.");

        if (!licenca.isDestinoInterno())
            throw IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Só a mobilidade interna se consolida: numa mobilidade externa o destino é outra "
                            + "entidade, e não há Lugar do nosso quadro onde colocar o colaborador.");

        // Primeiro o período: é o agregado que decide se há mobilidade transitória para consolidar.
        licenca.consolidar(req.getDataEfeito(), LocalDate.now());

        var consolidacao = assignmentService.consolidarMobilidade(
                funcionarioId,
                uuid(req.getPositionId()),
                licenca.getDestinationUnitId(),
                req.getDataEfeito(),
                notas(req.getDespachoNumero(), req.getObservacoes()));

        licencaRepository.save(licenca);

        var nova = consolidacao.afectacao();
        return ResponseEntity.status(201).body(new ConsolidacaoMobilidadeResponseDTO(
                nova.getId().getStringValor(),
                funcionarioId.getStringValor(),
                licenca.getId().getStringValor(),
                licenca.getDataFim(),
                consolidacao.lugarAnterior().getId().getStringValor(),
                consolidacao.lugarAnterior().getNumeroLugar(),
                texto(consolidacao.lugarAnterior().getUnidadeOrganicaId()),
                consolidacao.lugarNovo().getId().getStringValor(),
                consolidacao.lugarNovo().getNumeroLugar(),
                texto(consolidacao.lugarNovo().getUnidadeOrganicaId()),
                req.getDataEfeito()));
    }

    private static String texto(UUID valor) {
        return valor == null ? null : valor.toString();
    }

    private static UUID uuid(String valor) {
        try {
            return UUID.fromString(valor);
        } catch (IllegalArgumentException e) {
            throw IgrpResponseStatusException.badRequest("O campo positionId não é um UUID válido: " + valor);
        }
    }

    private static String notas(String despachoNumero, String observacoes) {
        String notas = "Consolidação da mobilidade" + (despachoNumero != null && !despachoNumero.isBlank()
                ? " (despacho " + despachoNumero + ")" : "");
        return observacoes != null && !observacoes.isBlank() ? notas + " — " + observacoes : notas;
    }
}
