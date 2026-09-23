package cv.igrp.RH_Service.estrutura.application.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import cv.igrp.RH_Service.parametrizacoes.domain.models.BlocoHorario;
import cv.igrp.RH_Service.parametrizacoes.domain.models.ControloHorario;
import cv.igrp.RH_Service.parametrizacoes.domain.models.Horario;
import cv.igrp.RH_Service.parametrizacoes.domain.repository.HorarioRepository;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** O horarioId da unidade (V57): em branco limpa; senao, um horario que exista e esteja activo. */
@ExtendWith(MockitoExtension.class)
class HorarioDaUnidadeTest {

    @Mock private HorarioRepository repository;
    @InjectMocks private HorarioDaUnidade horarioDaUnidade;

    private static Horario horario() {
        return Horario.criar("H", ControloHorario.FIXO, null, null,
                List.of(new BlocoHorario(DayOfWeek.MONDAY, LocalTime.of(8, 0), LocalTime.of(16, 0), true)));
    }

    @Test
    void emBrancoLimpa() {
        assertNull(horarioDaUnidade.validar("  "));
    }

    @Test
    void activoPassa() {
        var h = horario();
        when(repository.findById(h.getId())).thenReturn(Optional.of(h));
        assertEquals(h.getId(), horarioDaUnidade.validar(h.getId().getStringValor()));
    }

    @Test
    void inexistenteInactivoOuMalEscritoE422() {
        var inactivo = horario();
        inactivo.desativar();
        when(repository.findById(inactivo.getId())).thenReturn(Optional.of(inactivo));
        var inexistente = UUID.randomUUID().toString();

        for (String id : List.of(inactivo.getId().getStringValor(), inexistente, "abc")) {
            var e = assertThrows(IgrpResponseStatusException.class, () -> horarioDaUnidade.validar(id));
            assertEquals(422, e.getStatusCode().value());
        }
    }
}
