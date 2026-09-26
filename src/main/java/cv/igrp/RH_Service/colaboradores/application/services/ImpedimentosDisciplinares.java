package cv.igrp.RH_Service.colaboradores.application.services;

import cv.igrp.RH_Service.colaboradores.domain.models.ActoDisciplinar;
import cv.igrp.RH_Service.colaboradores.domain.models.PenaDisciplinar;
import cv.igrp.RH_Service.colaboradores.domain.models.ProcessoDisciplinar;
import cv.igrp.RH_Service.colaboradores.domain.repository.ProcessoDisciplinarRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.shared.domain.service.Datas;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

/**
 * <b>O que uma pena disciplinar impede</b> (Estatuto Disciplinar, art. 17.º; BR-DIS-29): para quem precisa de o saber —
 * o concurso, a promoção, a nomeação para cargo dirigente. Cada pergunta devolve a razão (para a mensagem) ou vazio.
 */
@Component
@RequiredArgsConstructor
public class ImpedimentosDisciplinares {

    private final ProcessoDisciplinarRepository repository;

    /** Art. 17.º n.º 2 c): não é admitido a concurso enquanto cumpre pena de suspensão. */
    @Transactional(readOnly = true)
    public Optional<String> impedeConcurso(FuncionarioId funcionarioId, LocalDate dia) {
        return repository.findComAfastamento(funcionarioId).stream()
                .filter(p -> p.getPena() == PenaDisciplinar.SUSPENSAO && emCumprimento(p, dia)).findFirst()
                .map(p -> "Está a cumprir pena de suspensão até " + Datas.pt(p.getPenaltyEndDate())
                        + ": não pode ser admitido a concurso (art. 17.º n.º 2 do Estatuto Disciplinar).");
    }

    /** Art. 17.º n.os 2 c) e 3: sem promoção durante a suspensão, e durante a inactividade e o ano seguinte ao seu termo. */
    @Transactional(readOnly = true)
    public Optional<String> impedePromocao(FuncionarioId funcionarioId, LocalDate dia) {
        for (var p : repository.findComAfastamento(funcionarioId)) {
            if (p.getPena() == PenaDisciplinar.SUSPENSAO && emCumprimento(p, dia))
                return Optional.of("Está a cumprir pena de suspensão até " + Datas.pt(p.getPenaltyEndDate())
                        + ": não pode ser promovido (art. 17.º n.º 2 do Estatuto Disciplinar).");
            if (p.getPena() == PenaDisciplinar.INACTIVIDADE && p.getEfeitosAplicadosEm() != null && p.getPenaltyEndDate() != null
                    && !dia.isBefore(p.getPenaltyStartDate()) && !dia.isAfter(p.getPenaltyEndDate().plusYears(1)))
                return Optional.of("A pena de inactividade impede a promoção até " + Datas.pt(p.getPenaltyEndDate().plusYears(1))
                        + " (art. 17.º n.º 3 do Estatuto Disciplinar).");
        }
        return Optional.empty();
    }

    /** Art. 17.º n.º 8: cessada a comissão por pena, 2 anos sem nova nomeação para cargo dirigente, contados da notificação. */
    @Transactional(readOnly = true)
    public Optional<String> impedeNomeacaoDirigente(FuncionarioId funcionarioId, LocalDate dia) {
        for (var p : repository.findAllByFuncionarioId(funcionarioId)) {
            boolean cessouComissao = p.ultimo(ActoDisciplinar.Tipo.EFEITOS)
                    .map(a -> a.texto() != null && a.texto().contains("Comissão de serviço cessada")).orElse(false);
            if (!cessouComissao || p.getPena() == null) continue;
            var notificacao = p.ultimo(ActoDisciplinar.Tipo.NOTIFICACAO_DECISAO).map(ActoDisciplinar::data).orElse(null);
            if (notificacao != null && dia.isBefore(notificacao.plusYears(2)))
                return Optional.of("A comissão de serviço cessou por pena disciplinar: não pode ser nomeado para cargo dirigente antes de "
                        + Datas.pt(notificacao.plusYears(2)) + " (art. 17.º n.º 8 do Estatuto Disciplinar).");
        }
        return Optional.empty();
    }

    private static boolean emCumprimento(ProcessoDisciplinar p, LocalDate dia) {
        return p.getEfeitosAplicadosEm() != null && p.getPenaltyStartDate() != null && p.getPenaltyEndDate() != null
                && !dia.isBefore(p.getPenaltyStartDate()) && !dia.isAfter(p.getPenaltyEndDate());
    }
}
