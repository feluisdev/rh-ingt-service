package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.application.dto.SubstituicaoResponseDTO;
import cv.igrp.RH_Service.colaboradores.application.services.SubstituicaoService;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Põe um colaborador a substituir o titular de um Lugar (Lei n.º 20/X/2023,
 * art. 73.º al. a) a c) e art. 91.º n.º 1 al. a)).
 *
 * <p>Aqui valida-se quem pode substituir; as regras sobre o titular e o Lugar vivem
 * no {@link SubstituicaoService}.
 *
 * <p>Ao contrário dos movimentos de carreira, a substituição <b>não encerra a afectação
 * do substituto</b>: quem já é funcionário mantém o seu Lugar enquanto substitui
 * (nomeação em substituição), e quem vem de fora entra sem ter Lugar nenhum.
 */
@Component
@RequiredArgsConstructor
public class SubstituirColaboradorCommandHandler
        implements CommandHandler<SubstituirColaboradorCommand, ResponseEntity<SubstituicaoResponseDTO>> {

    private final FuncionarioRepository funcionarioRepository;
    private final SubstituicaoService substituicaoService;

    @IgrpCommandHandler
    @Transactional
    public ResponseEntity<SubstituicaoResponseDTO> handle(SubstituirColaboradorCommand command) {
        var req = command.getRequest();
        if (req == null || req.getDataInicio() == null)
            throw IgrpResponseStatusException.badRequest("A data de início (dataInicio) é obrigatória.");
        if (req.getPositionId() == null || req.getPositionId().isBlank())
            throw IgrpResponseStatusException.badRequest("O Lugar a substituir (positionId) é obrigatório.");

        var substitutoId = FuncionarioId.from(command.getFuncionarioId());
        var substituto = funcionarioRepository.findById(substitutoId)
                .orElseThrow(() -> IgrpResponseStatusException.notFound(
                        "Funcionário não encontrado: " + command.getFuncionarioId()));

        if (!Boolean.TRUE.equals(substituto.getIsActive()))
            throw IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY,
                    "O colaborador não está activo — não pode substituir ninguém.");

        var resultado = substituicaoService.substituir(
                substitutoId,
                uuid(req.getPositionId(), "positionId"),
                uuid(req.getGradeId(), "gradeId"),
                uuid(req.getFunctionId(), "functionId"),
                req.getDataInicio(),
                notas(req.getDespachoNumero(), req.getObservacoes()));

        var nova = resultado.afectacao();
        return ResponseEntity.status(201).body(new SubstituicaoResponseDTO(
                nova.getId().getStringValor(),
                substitutoId.getStringValor(),
                resultado.lugar().getId().getStringValor(),
                resultado.lugar().getNumeroLugar(),
                texto(resultado.lugar().getUnidadeOrganicaId()),
                texto(nova.getGradeId()),
                texto(nova.getFunctionId()),
                nova.getDataInicio(),
                resultado.titular().getId().getStringValor(),
                resultado.titular().getNomeCompleto(),
                resultado.afectacaoTitular().getId().getStringValor()));
    }

    private static String texto(UUID valor) {
        return valor == null ? null : valor.toString();
    }

    private static UUID uuid(String valor, String campo) {
        if (valor == null || valor.isBlank()) return null;
        try {
            return UUID.fromString(valor);
        } catch (IllegalArgumentException e) {
            throw IgrpResponseStatusException.badRequest("O campo " + campo + " não é um UUID válido: " + valor);
        }
    }

    private static String notas(String despachoNumero, String observacoes) {
        String notas = "Substituição" + (despachoNumero != null && !despachoNumero.isBlank()
                ? " (despacho " + despachoNumero + ")" : "");
        return observacoes != null && !observacoes.isBlank() ? notas + " — " + observacoes : notas;
    }
}
