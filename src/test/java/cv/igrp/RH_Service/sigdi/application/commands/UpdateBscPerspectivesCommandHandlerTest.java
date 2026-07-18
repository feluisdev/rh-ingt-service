package cv.igrp.RH_Service.sigdi.application.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.sigdi.application.dto.BscPerspectiveItemDTO;
import cv.igrp.RH_Service.sigdi.domain.strategy.models.BscPerspectiveConfig;
import cv.igrp.RH_Service.sigdi.domain.strategy.repository.BscPerspectiveConfigRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class UpdateBscPerspectivesCommandHandlerTest {

    @Mock
    private BscPerspectiveConfigRepository repository;

    @InjectMocks
    private UpdateBscPerspectivesCommandHandler handler;

    private static BscPerspectiveConfig existing(String code, Integer order) {
        return BscPerspectiveConfig.reconstruct(UUID.randomUUID(), code, code + "-label", order);
    }

    @Test
    void handleWithValidPermutationUpdatesAllFourAndSavesOnce() {
        when(repository.findByCode("FINANCIAL")).thenReturn(Optional.of(existing("FINANCIAL", 1)));
        when(repository.findByCode("CUSTOMER")).thenReturn(Optional.of(existing("CUSTOMER", 2)));
        when(repository.findByCode("PROCESS")).thenReturn(Optional.of(existing("PROCESS", 3)));
        when(repository.findByCode("LEARNING")).thenReturn(Optional.of(existing("LEARNING", 4)));
        when(repository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        List<BscPerspectiveItemDTO> requested = List.of(
                new BscPerspectiveItemDTO("FINANCIAL", "Financeira", 1),
                new BscPerspectiveItemDTO("CUSTOMER", "Cliente / Mercado", 2),
                new BscPerspectiveItemDTO("PROCESS", "Processos Internos", 3),
                new BscPerspectiveItemDTO("LEARNING", "Aprendizagem e Crescimento", 4));

        ResponseEntity<List<BscPerspectiveItemDTO>> response =
                handler.handle(new UpdateBscPerspectivesCommand(requested));

        assertEquals(200, response.getStatusCode().value());
        assertEquals(4, response.getBody().size());

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<BscPerspectiveConfig>> captor = ArgumentCaptor.forClass(List.class);
        verify(repository).saveAll(captor.capture());
        assertEquals(4, captor.getValue().size());
    }

    @Test
    void handleWithGappedOrderThrowsBadRequestAndNeverSaves() {
        // orders {1,2,3,3}: PROCESS and LEARNING both claim position 3, no item claims 4.
        List<BscPerspectiveItemDTO> requested = List.of(
                new BscPerspectiveItemDTO("FINANCIAL", "Financeira", 1),
                new BscPerspectiveItemDTO("CUSTOMER", "Cliente / Mercado", 2),
                new BscPerspectiveItemDTO("PROCESS", "Processos Internos", 3),
                new BscPerspectiveItemDTO("LEARNING", "Aprendizagem e Crescimento", 3));

        assertThrows(IgrpResponseStatusException.class,
                () -> handler.handle(new UpdateBscPerspectivesCommand(requested)));

        verify(repository, never()).findByCode(any());
        verify(repository, never()).saveAll(any());
    }

    @Test
    void handleWithCodeMissingFromRepositoryThrowsBadRequestAndNeverSaves() {
        // All 4 codes are the correct, expected codes (passes the code-set check), but the
        // repository unexpectedly has no row for LEARNING -- exercises the defensive
        // findByCode(...).orElseThrow(...) branch (T-73-06).
        when(repository.findByCode("FINANCIAL")).thenReturn(Optional.of(existing("FINANCIAL", 1)));
        when(repository.findByCode("CUSTOMER")).thenReturn(Optional.of(existing("CUSTOMER", 2)));
        when(repository.findByCode("PROCESS")).thenReturn(Optional.of(existing("PROCESS", 3)));
        when(repository.findByCode("LEARNING")).thenReturn(Optional.empty());

        List<BscPerspectiveItemDTO> requested = List.of(
                new BscPerspectiveItemDTO("FINANCIAL", "Financeira", 1),
                new BscPerspectiveItemDTO("CUSTOMER", "Cliente / Mercado", 2),
                new BscPerspectiveItemDTO("PROCESS", "Processos Internos", 3),
                new BscPerspectiveItemDTO("LEARNING", "Aprendizagem e Crescimento", 4));

        assertThrows(IgrpResponseStatusException.class,
                () -> handler.handle(new UpdateBscPerspectivesCommand(requested)));

        verify(repository, never()).saveAll(any());
    }

    @Test
    void handleWithDuplicateValidCodeThrowsBadRequestBeforeAnyLookupOrSave() {
        // Two FINANCIAL items, no LEARNING -- order values {1,2,3,4} would otherwise be a valid
        // permutation, so this specifically proves the code-set check runs BEFORE the order check.
        List<BscPerspectiveItemDTO> requested = List.of(
                new BscPerspectiveItemDTO("FINANCIAL", "Financeira", 1),
                new BscPerspectiveItemDTO("FINANCIAL", "Financeira Duplicada", 2),
                new BscPerspectiveItemDTO("PROCESS", "Processos Internos", 3),
                new BscPerspectiveItemDTO("CUSTOMER", "Cliente / Mercado", 4));

        assertThrows(IgrpResponseStatusException.class,
                () -> handler.handle(new UpdateBscPerspectivesCommand(requested)));

        verify(repository, never()).findByCode(any());
        verify(repository, never()).saveAll(any());
    }
}
