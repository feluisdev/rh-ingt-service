package cv.igrp.RH_Service.parametrizacoes.application.services;

import cv.igrp.RH_Service.parametrizacoes.application.dto.HorarioResponseDTO;
import cv.igrp.RH_Service.parametrizacoes.application.port.HorarioUtilizacaoPort;
import cv.igrp.RH_Service.parametrizacoes.domain.models.Horario;
import cv.igrp.RH_Service.parametrizacoes.domain.models.VigenciaHorarioBase;
import cv.igrp.RH_Service.parametrizacoes.domain.repository.HorarioBaseHistoricoRepository;
import cv.igrp.RH_Service.parametrizacoes.domain.repository.HorarioRepository;
import cv.igrp.RH_Service.parametrizacoes.domain.valueobject.HorarioId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * <b>O horário base, por data</b> — o horário da instituição tem data de efeito: marcar outro vale a
 * partir de hoje (ou de uma data futura), e os dias passados continuam com o que vigorava. Uma data
 * passada reescreveria o apuramento já visto (DL n.º 3/2010, art. 13.º: apura-se contra o horário que
 * vigorava) — 422.
 *
 * <p>Sem histórico (bases antigas), vale o horário marcado como base, para todas as datas; a primeira
 * mudança regista-o como «desde sempre».
 */
@Service
@RequiredArgsConstructor
public class HorarioBaseService implements HorarioUtilizacaoPort {

    private final HorarioRepository horarioRepository;
    private final HorarioBaseHistoricoRepository historicoRepository;

    /** O horário base que vigorava em {@code data}. */
    @Transactional(readOnly = true)
    public Optional<Horario> baseEm(LocalDate data) {
        return idBaseEm(data).flatMap(horarioRepository::findById);
    }

    Optional<HorarioId> idBaseEm(LocalDate data) {
        List<VigenciaHorarioBase> historico = historicoRepository.findAll();
        if (historico.isEmpty()) return horarioRepository.findBase().map(Horario::getId);
        return historico.stream().filter(v -> v.comecouAte(data))
                .max(Comparator.comparing(VigenciaHorarioBase::desde, Comparator.nullsFirst(Comparator.naturalOrder())))
                .map(VigenciaHorarioBase::horarioId);
    }

    /** Marca o base a partir de {@code desde} (nulo = hoje). */
    @Transactional
    public Horario marcar(HorarioId id, LocalDate desde) {
        LocalDate hoje = hoje();
        LocalDate efeito = desde != null ? desde : hoje;
        if (efeito.isBefore(hoje))
            throw IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY,
                    "O horário base muda de hoje em diante (ou numa data futura): " + efeito
                            + " já passou, e mudá-lo reescreveria o apuramento desses dias.");
        var horario = horarioRepository.findById(id)
                .orElseThrow(() -> IgrpResponseStatusException.notFound("Horário não encontrado: " + id.getStringValor()));
        horario.marcarComoBase();

        boolean semHistorico = historicoRepository.findAll().isEmpty();
        Optional<Horario> anteriorSemHistorico = semHistorico ? horarioRepository.findBase() : Optional.empty();
        anteriorSemHistorico.filter(anterior -> !anterior.getId().equals(id))
                .ifPresent(anterior -> historicoRepository.registar(new VigenciaHorarioBase(anterior.getId(), null)));
        // O primeiro horário base da instituição, marcado hoje, vale desde sempre: antes dele não
        // havia horário nenhum contra o qual apurar.
        boolean primeiro = semHistorico && anteriorSemHistorico.isEmpty() && efeito.equals(hoje);
        historicoRepository.registar(new VigenciaHorarioBase(id, primeiro ? null : efeito));

        // A marca no catálogo segue o último marcado; o base de cada data vem do histórico.
        horarioRepository.findBase().filter(anterior -> !anterior.getId().equals(id)).ifPresent(anterior -> {
            anterior.desmarcarBase();
            horarioRepository.save(anterior);
        });
        return horarioRepository.save(horario);
    }

    /** É o base hoje ou tem uma vigência marcada para depois: não se desactiva. */
    @Transactional(readOnly = true)
    public boolean eBaseHojeOuDepois(HorarioId id) {
        LocalDate hoje = hoje();
        if (idBaseEm(hoje).filter(id::equals).isPresent()) return true;
        return historicoRepository.findAll().stream()
                .anyMatch(v -> v.horarioId().equals(id) && v.desde() != null && v.desde().isAfter(hoje));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean vigorouAntesDe(HorarioId id, LocalDate data) {
        List<VigenciaHorarioBase> historico = historicoRepository.findAll();
        if (historico.isEmpty())
            return horarioRepository.findBase().filter(b -> b.getId().equals(id)).isPresent();
        return historico.stream().anyMatch(v -> v.horarioId().equals(id) && (v.desde() == null || v.desde().isBefore(data)));
    }

    /** O {@code isBase} do catálogo é o de hoje, não o da marca (que pode ser uma mudança agendada). */
    @Transactional(readOnly = true)
    public HorarioResponseDTO comBaseDeHoje(HorarioResponseDTO dto) {
        if (dto != null) dto.setIsBase(idBaseEm(hoje()).map(b -> b.getStringValor().equals(dto.getId())).orElse(false));
        return dto;
    }

    protected LocalDate hoje() { return LocalDate.now(); }
}
