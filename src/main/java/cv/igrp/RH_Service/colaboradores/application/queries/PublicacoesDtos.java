package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.RH_Service.colaboradores.application.dto.PublicacaoOficialDTO;
import cv.igrp.RH_Service.colaboradores.domain.models.Funcionario;
import cv.igrp.RH_Service.colaboradores.domain.models.PublicacaoOficial;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import org.springframework.http.HttpStatus;

/** Das publicações para os DTOs, e a leitura dos enums vindos de fora. */
public final class PublicacoesDtos {

    private PublicacoesDtos() {}

    public static PublicacaoOficialDTO dto(PublicacaoOficial p, FuncionarioRepository funcionarios) {
        String nome = p.getFuncionarioId() == null ? null
                : funcionarios.findById(p.getFuncionarioId()).map(Funcionario::getNomeCompleto).orElse(null);
        return new PublicacaoOficialDTO(p.getId().getStringValor(), p.getTipoActo().name(), p.getMeio().name(),
                p.getFuncionarioId() != null ? p.getFuncionarioId().getStringValor() : null, nome, p.getReferenciaTipo(),
                p.getReferenciaId(), p.getSumario(), p.getDataActo(), p.getEstado().name(),
                p.getExtractoId() != null ? p.getExtractoId().getStringValor() : null, p.getSerie(), p.getNumero(),
                p.getDataPublicacao(), p.getMotivoCancelamento());
    }

    public static <E extends Enum<E>> E enumOuNulo(Class<E> tipo, String valor, String oQue) {
        if (valor == null || valor.isBlank()) return null;
        try {
            return Enum.valueOf(tipo, valor.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY, oQue + " desconhecido: " + valor + ".");
        }
    }
}
