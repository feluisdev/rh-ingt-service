package cv.igrp.RH_Service.parametrizacoes.application.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import cv.igrp.RH_Service.parametrizacoes.application.dto.HorarioRequestDTO;
import cv.igrp.RH_Service.parametrizacoes.domain.models.BlocoHorario;
import cv.igrp.RH_Service.parametrizacoes.domain.models.ControloHorario;
import cv.igrp.RH_Service.parametrizacoes.domain.models.Horario;
import cv.igrp.RH_Service.parametrizacoes.domain.models.PeriodoAfericao;
import cv.igrp.RH_Service.parametrizacoes.domain.repository.HorarioRepository;
import cv.igrp.RH_Service.parametrizacoes.infrastructure.mappers.HorarioMapper;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Marcar o base desmarca o anterior; o PUT nao apaga o omisso, e passar a FIXO limpa o que e so do flexivel. */
@ExtendWith(MockitoExtension.class)
class HorarioCommandHandlerTest {

    @Mock private HorarioRepository repository;

    private final HorarioMapper mapper = new HorarioMapper();

    private static Horario fixo(String nome) {
        return Horario.criar(nome, ControloHorario.FIXO, null, null,
                List.of(new BlocoHorario(DayOfWeek.MONDAY, LocalTime.of(8, 0), LocalTime.of(16, 0), true)));
    }

    @Test
    void marcarBaseDesmarcaOAnterior() {
        var anterior = fixo("Antigo");
        anterior.marcarComoBase();
        var novo = fixo("Novo");
        when(repository.findById(novo.getId())).thenReturn(Optional.of(novo));
        when(repository.findBase()).thenReturn(Optional.of(anterior));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var r = new MarcarHorarioBaseCommandHandler(repository, mapper).handle(new MarcarHorarioBaseCommand(novo.getId().getStringValor()));

        assertTrue(novo.isBase());
        assertFalse(anterior.isBase());
        assertTrue(r.getBody().getIsBase());
    }

    @Test
    void putSoComONomeMantemOsBlocos() {
        var h = fixo("Antigo");
        when(repository.findById(h.getId())).thenReturn(Optional.of(h));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var dto = new HorarioRequestDTO();
        dto.setNome("Novo nome");
        new UpdateHorarioCommandHandler(repository, mapper).handle(new UpdateHorarioCommand(dto, h.getId().getStringValor()));

        assertEquals("Novo nome", h.getNome());
        assertEquals(1, h.getBlocos().size());
    }

    @Test
    void passarAFixoLimpaPeriodoEDuracao() {
        var h = Horario.criar("Flex", ControloHorario.FLEXIVEL, PeriodoAfericao.SEMANA, 6 * 60,
                List.of(new BlocoHorario(DayOfWeek.MONDAY, LocalTime.of(8, 0), LocalTime.of(18, 0), false)));
        when(repository.findById(h.getId())).thenReturn(Optional.of(h));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var dto = new HorarioRequestDTO();
        dto.setControlo("FIXO");
        new UpdateHorarioCommandHandler(repository, mapper).handle(new UpdateHorarioCommand(dto, h.getId().getStringValor()));

        assertEquals(ControloHorario.FIXO, h.getControlo());
        assertNull(h.getPeriodoAfericao());
        assertNull(h.getDuracaoDiariaMinutos());
        assertEquals(10 * 60, h.minutosSemanais());
    }
}
