package cv.igrp.RH_Service.parametrizacoes.application.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cv.igrp.RH_Service.parametrizacoes.application.dto.PublicHolidayRequestDTO;
import cv.igrp.RH_Service.parametrizacoes.domain.models.PublicHoliday;
import cv.igrp.RH_Service.parametrizacoes.domain.repository.PublicHolidayRepository;
import cv.igrp.RH_Service.parametrizacoes.domain.valueobject.PublicHolidayId;
import cv.igrp.RH_Service.parametrizacoes.infrastructure.mappers.PublicHolidayMapper;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * O PUT do feriado nao apaga o que o front actual nao envia (V55). Um ecra que so conhece nome,
 * data, nacional e descricao nao pode, ao corrigir o nome, desmarcar a recorrencia do Natal --
 * seria voltar ao defeito de 2027 por um caminho novo.
 */
@ExtendWith(MockitoExtension.class)
class UpdatePublicHolidayCommandHandlerTest {

    @Mock private PublicHolidayRepository repository;
    @Mock private PublicHolidayMapper mapper;

    @InjectMocks private UpdatePublicHolidayCommandHandler handler;

    private PublicHoliday gravar(PublicHoliday existente, Boolean recorrente, String area, boolean nacional) {
        when(repository.findById(existente.getId())).thenReturn(Optional.of(existente));
        when(repository.save(any(PublicHoliday.class))).thenAnswer(inv -> inv.getArgument(0));

        var dto = new PublicHolidayRequestDTO();
        dto.setName("Nome corrigido");
        dto.setHolidayDate(existente.getHolidayDate());
        dto.setIsNational(nacional);
        dto.setIsRecurring(recorrente);
        dto.setAreaCkey(area);
        handler.handle(new UpdatePublicHolidayCommand(dto, existente.getId().getStringValor()));

        ArgumentCaptor<PublicHoliday> captor = ArgumentCaptor.forClass(PublicHoliday.class);
        verify(repository).save(captor.capture());
        return captor.getValue();
    }

    private static PublicHoliday natal() {
        return PublicHoliday.reconstruir(PublicHolidayId.gerarNovo(), "Natal", LocalDate.of(2026, 12, 25),
                true, null, true, null, true);
    }

    private static PublicHoliday municipal() {
        return PublicHoliday.reconstruir(PublicHolidayId.gerarNovo(), "Municipio", LocalDate.of(2026, 1, 22),
                false, null, true, "MINDELO", true);
    }

    @Test
    void semOsCamposNovosMantemRecorrenciaEArea() {
        var gravado = gravar(municipal(), null, null, false);

        assertEquals("Nome corrigido", gravado.getName());
        assertTrue(gravado.isRecurring());
        assertEquals("MINDELO", gravado.getAreaCkey());
    }

    @Test
    void semOsCamposNovosONatalContinuaRecorrente() {
        assertTrue(gravar(natal(), null, null, true).isRecurring());
    }

    @Test
    void falseDesmarcaARecorrencia() {
        assertFalse(gravar(municipal(), false, null, false).isRecurring());
    }

    @Test
    void areaEmBrancoLimpaA() {
        assertNull(gravar(municipal(), null, "", false).getAreaCkey());
    }
}
