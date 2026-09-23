package cv.igrp.RH_Service.parametrizacoes.application.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cv.igrp.RH_Service.parametrizacoes.application.dto.ParametroFeriasRequestDTO;
import cv.igrp.RH_Service.parametrizacoes.domain.models.ParametroFerias;
import cv.igrp.RH_Service.parametrizacoes.domain.repository.ParametroFeriasRepository;
import cv.igrp.RH_Service.parametrizacoes.infrastructure.mappers.ParametroFeriasMapper;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

/**
 * Criar uma vigencia repetida e 409; o PUT so muda o que vem, e o que vem omisso fica como estava.
 */
@ExtendWith(MockitoExtension.class)
class ParametroFeriasCommandHandlerTest {

    @Mock private ParametroFeriasRepository repository;
    @Mock private ParametroFeriasMapper mapper;

    private static ParametroFeriasRequestDTO pedido(Integer ano) {
        return new ParametroFeriasRequestDTO(ano, "01-31", "03-31", "05-01", "10-31", 11, "DL n.o 3/2010");
    }

    private static ParametroFerias daLeiGravada() {
        return ParametroFerias.criar(2010, "01-31", "03-31", "05-01", "10-31", 11, "DL n.o 3/2010");
    }

    @Test
    void criarVigenciaNova() {
        when(repository.existsByVigenteDesde(2027)).thenReturn(false);
        when(repository.save(any(ParametroFerias.class))).thenAnswer(inv -> inv.getArgument(0));

        var r = new CreateParametroFeriasCommandHandler(repository).handle(new CreateParametroFeriasCommand(pedido(2027)));

        assertEquals(201, r.getStatusCode().value());
    }

    @Test
    void criarNoMesmoAnoDeOutraE409() {
        when(repository.existsByVigenteDesde(2010)).thenReturn(true);

        var e = assertThrows(IgrpResponseStatusException.class,
                () -> new CreateParametroFeriasCommandHandler(repository).handle(new CreateParametroFeriasCommand(pedido(2010))));

        assertEquals(HttpStatus.CONFLICT.value(), e.getStatusCode().value());
        verify(repository, never()).save(any());
    }

    @Test
    void putSoComUmPrazoMantemOResto() {
        var existente = daLeiGravada();
        when(repository.findById(existente.getId())).thenReturn(Optional.of(existente));
        when(repository.save(any(ParametroFerias.class))).thenAnswer(inv -> inv.getArgument(0));

        var dto = new ParametroFeriasRequestDTO();
        dto.setPrazoMapa("04-15");
        new UpdateParametroFeriasCommandHandler(repository, mapper)
                .handle(new UpdateParametroFeriasCommand(dto, existente.getId().getStringValor()));

        var captor = ArgumentCaptor.forClass(ParametroFerias.class);
        verify(repository).save(captor.capture());
        var gravado = captor.getValue();
        assertEquals("04-15", ParametroFerias.texto(gravado.getPrazoMapa()));
        assertEquals("01-31", ParametroFerias.texto(gravado.getPrazoPreferencia()));
        assertEquals("05-01", ParametroFerias.texto(gravado.getFixacaoInicio()));
        assertEquals("10-31", ParametroFerias.texto(gravado.getFixacaoFim()));
        assertEquals(11, gravado.getPeriodoMinimoInterpolado());
        assertEquals(2010, gravado.getVigenteDesde());
        assertEquals("DL n.o 3/2010", gravado.getFundamento());
        verify(repository, never()).existsByVigenteDesde(org.mockito.ArgumentMatchers.anyInt());
    }

    @Test
    void putComFundamentoEmBrancoLimpa() {
        var existente = daLeiGravada();
        when(repository.findById(existente.getId())).thenReturn(Optional.of(existente));
        when(repository.save(any(ParametroFerias.class))).thenAnswer(inv -> inv.getArgument(0));

        var dto = new ParametroFeriasRequestDTO();
        dto.setFundamento("");
        new UpdateParametroFeriasCommandHandler(repository, mapper)
                .handle(new UpdateParametroFeriasCommand(dto, existente.getId().getStringValor()));

        assertNull(existente.getFundamento());
    }

    @Test
    void putParaUmAnoJaOcupadoE409() {
        var existente = daLeiGravada();
        when(repository.findById(existente.getId())).thenReturn(Optional.of(existente));
        when(repository.existsByVigenteDesde(2027)).thenReturn(true);

        var dto = new ParametroFeriasRequestDTO();
        dto.setVigenteDesde(2027);
        var e = assertThrows(IgrpResponseStatusException.class, () -> new UpdateParametroFeriasCommandHandler(repository, mapper)
                .handle(new UpdateParametroFeriasCommand(dto, existente.getId().getStringValor())));

        assertEquals(HttpStatus.CONFLICT.value(), e.getStatusCode().value());
    }

    @Test
    void putQueDeixaOPrazoDaPreferenciaDepoisDoMapaE422() {
        var existente = daLeiGravada();
        when(repository.findById(existente.getId())).thenReturn(Optional.of(existente));

        var dto = new ParametroFeriasRequestDTO();
        dto.setPrazoPreferencia("04-30");
        var e = assertThrows(IgrpResponseStatusException.class, () -> new UpdateParametroFeriasCommandHandler(repository, mapper)
                .handle(new UpdateParametroFeriasCommand(dto, existente.getId().getStringValor())));

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.value(), e.getStatusCode().value());
    }
}
