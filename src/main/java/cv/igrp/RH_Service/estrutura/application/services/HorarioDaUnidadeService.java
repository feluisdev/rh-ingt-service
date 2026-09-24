package cv.igrp.RH_Service.estrutura.application.services;

import cv.igrp.RH_Service.estrutura.domain.models.OrganizationalUnit;
import cv.igrp.RH_Service.estrutura.domain.models.VigenciaHorarioUnidade;
import cv.igrp.RH_Service.estrutura.domain.repository.HorarioUnidadeHistoricoRepository;
import cv.igrp.RH_Service.estrutura.domain.repository.OrganizationalUnitRepository;
import cv.igrp.RH_Service.parametrizacoes.application.port.HorarioUtilizacaoPort;
import cv.igrp.RH_Service.parametrizacoes.domain.valueobject.HorarioId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

/**
 * <b>O horário de uma unidade orgânica, por data</b> — mudar o horário da unidade vale de hoje em
 * diante (ou de uma data futura); os dias passados continuam com o que vigorava, porque é contra ele
 * que se apuram (DL n.º 3/2010, art. 13.º). Uma data passada: 422.
 *
 * <p>Sem histórico (unidades de antes disto), vale a coluna {@code horario_id} para todas as datas; a
 * primeira mudança regista-a como «desde sempre». A coluna guarda o horário de hoje, quando a mudança
 * é de hoje; uma mudança agendada só se lê pelo histórico.
 */
@Service
@RequiredArgsConstructor
public class HorarioDaUnidadeService implements HorarioUtilizacaoPort {

    private final HorarioUnidadeHistoricoRepository historicoRepository;
    private final OrganizationalUnitRepository unidadeRepository;

    /** O horário próprio da unidade em {@code data} (nulo = segue a unidade-mãe). */
    @Transactional(readOnly = true)
    public HorarioId horarioEm(OrganizationalUnit unidade, LocalDate data) {
        List<VigenciaHorarioUnidade> historico = historicoRepository.findByUnidade(unidade.getId());
        if (historico.isEmpty()) return unidade.getHorarioId();
        return historico.stream().filter(v -> v.comecouAte(data))
                .max(Comparator.comparing(VigenciaHorarioUnidade::desde, Comparator.nullsFirst(Comparator.naturalOrder())))
                .map(VigenciaHorarioUnidade::horarioId)
                .orElse(null);
    }

    /** Muda o horário da unidade a partir de {@code desde} (nulo = hoje). {@code horarioId} nulo = nenhum. */
    @Transactional
    public void definir(OrganizationalUnit unidade, HorarioId horarioId, LocalDate desde) {
        LocalDate hoje = hoje();
        LocalDate efeito = desde != null ? desde : hoje;
        if (efeito.isBefore(hoje))
            throw IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY,
                    "O horário da unidade muda de hoje em diante (ou numa data futura): " + efeito
                            + " já passou, e mudá-lo reescreveria o apuramento desses dias.");
        if (historicoRepository.findByUnidade(unidade.getId()).isEmpty())
            historicoRepository.registar(new VigenciaHorarioUnidade(unidade.getId(), unidade.getHorarioId(), null));
        historicoRepository.registar(new VigenciaHorarioUnidade(unidade.getId(), horarioId, efeito));
        if (!efeito.isAfter(hoje)) unidade.definirHorario(horarioId);
    }

    /** Vigorou numa unidade num dia antes de {@code data}: pelo histórico, ou pela coluna de uma unidade sem histórico. */
    @Override
    @Transactional(readOnly = true)
    public boolean vigorouAntesDe(HorarioId horarioId, LocalDate data) {
        if (historicoRepository.findByHorario(horarioId).stream().anyMatch(v -> v.desde() == null || v.desde().isBefore(data)))
            return true;
        return unidadeRepository.findAllByHorario(horarioId).stream()
                .anyMatch(u -> historicoRepository.findByUnidade(u.getId()).isEmpty());
    }

    protected LocalDate hoje() { return LocalDate.now(); }
}
