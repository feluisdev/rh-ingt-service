package cv.igrp.RH_Service.colaboradores.infrastructure.scheduler;

import cv.igrp.RH_Service.colaboradores.application.services.FeriasService;
import cv.igrp.RH_Service.colaboradores.domain.models.Funcionario;
import cv.igrp.RH_Service.colaboradores.domain.models.SaldoAusencia;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.shared.application.services.scheduler.JobContext;
import cv.igrp.RH_Service.shared.application.services.scheduler.TipoDisparo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VencimentoFeriasJobTest {

    @Mock private FuncionarioRepository funcionarioRepository;
    @Mock private FeriasService feriasService;

    private VencimentoFeriasJob job() {
        return new VencimentoFeriasJob(funcionarioRepository, feriasService, "0 5 0 * * *");
    }

    private static Funcionario funcionario(String nome, String numero) {
        var f = mock(Funcionario.class);
        when(f.getId()).thenReturn(FuncionarioId.gerarNovo());
        org.mockito.Mockito.lenient().when(f.getNomeCompleto()).thenReturn(nome);
        org.mockito.Mockito.lenient().when(f.getNumeroFuncionario()).thenReturn(numero);
        return f;
    }

    @Test
    @DisplayName("a execução de 31/12 repetida em Janeiro trata o ano que acabou")
    void anoDerivaDoAgendamento() {
        when(funcionarioRepository.findAll(any())).thenReturn(List.of());

        var resultado = job().executar(JobContext.of(Map.of(), LocalDateTime.of(2026, 12, 31, 0, 5),
                TipoDisparo.MANUAL, null, null, 1));

        assertEquals("2026", resultado.getReferencia());
    }

    @Test
    void anoExplicitoGanha() {
        var ana = funcionario("Ana Lopes", "101");
        when(funcionarioRepository.findAll(any())).thenReturn(List.of(ana));
        when(feriasService.garantirSaldoDoAno(any(), anyInt())).thenReturn(Optional.of(mock(SaldoAusencia.class)));

        job().executar(JobContext.of(Map.of("ano", "2025"), LocalDateTime.of(2026, 9, 25, 0, 5),
                TipoDisparo.MANUAL, null, null, 1));

        verify(feriasService).garantirSaldoDoAno(ana.getId(), 2025);
    }

    @Test
    @DisplayName("um colaborador que falha não derruba os outros, e fica identificado nos detalhes")
    void falhaDeUmNaoDerrubaOsOutros() {
        var ana = funcionario("Ana Lopes", "101");
        var rui = funcionario("Rui Tavares", "102");
        when(funcionarioRepository.findAll(any())).thenReturn(List.of(ana, rui));
        when(feriasService.garantirSaldoDoAno(eq(ana.getId()), anyInt())).thenThrow(new IllegalStateException("BD em baixo"));
        when(feriasService.garantirSaldoDoAno(eq(rui.getId()), anyInt())).thenReturn(Optional.of(mock(SaldoAusencia.class)));

        var resultado = job().executar(JobContext.para(LocalDate.of(2026, 9, 25)));

        assertEquals(2, resultado.getProcessados());
        assertEquals(1, resultado.getCriados());
        assertEquals(1, resultado.getFalhas());
        var falhados = (List<?>) resultado.getDetalhes().get("itensFalhados");
        assertTrue(falhados.get(0).toString().startsWith("Ana Lopes (n.º 101)"));
    }

    @Test
    @DisplayName("sem tipo de férias no catálogo, a mensagem di-lo em vez de parecer sucesso")
    void semTipoDeFerias_mensagemAvisa() {
        var ana = funcionario("Ana Lopes", "101");
        when(funcionarioRepository.findAll(any())).thenReturn(List.of(ana));
        when(feriasService.garantirSaldoDoAno(any(), anyInt())).thenReturn(Optional.empty());

        var resultado = job().executar(JobContext.para(LocalDate.of(2026, 9, 25)));

        assertEquals(1, resultado.getSaltados());
        assertTrue(resultado.getMensagem().contains("tipo de ausência de férias"));
    }
}
