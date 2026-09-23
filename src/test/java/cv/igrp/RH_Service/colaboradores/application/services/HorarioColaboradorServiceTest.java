package cv.igrp.RH_Service.colaboradores.application.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cv.igrp.RH_Service.colaboradores.application.services.HorarioColaboradorService.Origem;
import cv.igrp.RH_Service.colaboradores.domain.models.Contrato;
import cv.igrp.RH_Service.colaboradores.domain.models.Funcionario;
import cv.igrp.RH_Service.colaboradores.domain.models.HorarioColaborador;
import cv.igrp.RH_Service.colaboradores.domain.models.RegimePrestacao;
import cv.igrp.RH_Service.colaboradores.domain.repository.ContratoRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.HorarioColaboradorRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.HorarioColaboradorId;
import cv.igrp.RH_Service.estrutura.domain.models.OrganizationalUnit;
import cv.igrp.RH_Service.estrutura.domain.valueobject.OrganizationalUnitId;
import cv.igrp.RH_Service.parametrizacoes.domain.models.BlocoHorario;
import cv.igrp.RH_Service.parametrizacoes.domain.models.ControloHorario;
import cv.igrp.RH_Service.parametrizacoes.domain.models.Horario;
import cv.igrp.RH_Service.parametrizacoes.domain.repository.HorarioRepository;
import cv.igrp.RH_Service.parametrizacoes.domain.valueobject.HorarioId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/**
 * O horario que vale numa data: o atribuido; senao o da unidade (herdado da mae); senao o base;
 * senao nenhum. E o alerta do tempo parcial.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class HorarioColaboradorServiceTest {

    private static final LocalDate DATA = LocalDate.of(2026, 10, 5);

    @Mock private HorarioColaboradorRepository horarioColaboradorRepository;
    @Mock private HorarioRepository horarioRepository;
    @Mock private FuncionarioRepository funcionarioRepository;
    @Mock private ContratoRepository contratoRepository;
    @Mock private UnidadeDeExercicioService unidadeDeExercicio;

    @InjectMocks private HorarioColaboradorService service;

    private final FuncionarioId funcionario = FuncionarioId.gerarNovo();
    private final UUID unidade = UUID.randomUUID();

    @BeforeEach
    void existe() {
        when(funcionarioRepository.findById(funcionario)).thenReturn(Optional.of(mock(Funcionario.class)));
        when(unidadeDeExercicio.unidadeOndeExerceFuncoes(funcionario, DATA)).thenReturn(unidade);
        when(horarioColaboradorRepository.findVigente(funcionario, DATA)).thenReturn(Optional.empty());
        when(horarioRepository.findBase()).thenReturn(Optional.empty());
    }

    /** {@code horas} por dia, de segunda a sexta. */
    private Horario horario(String nome, int horas) {
        List<BlocoHorario> blocos = List.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY, DayOfWeek.FRIDAY).stream()
                .map(d -> new BlocoHorario(d, LocalTime.of(8, 0), LocalTime.of(8 + horas, 0), true)).toList();
        var h = Horario.criar(nome, ControloHorario.FIXO, null, null, blocos);
        when(horarioRepository.findById(h.getId())).thenReturn(Optional.of(h));
        return h;
    }

    @SuppressWarnings("unchecked")
    private void unidadeTemHorario(HorarioId id) {
        var comHorario = OrganizationalUnit.reconstruir(OrganizationalUnitId.from(unidade), "U", "U", "U",
                null, null, null, null, null, id, true);
        when(unidadeDeExercicio.herdado(any(), any(Function.class)))
                .thenAnswer(inv -> ((Function<OrganizationalUnit, Object>) inv.getArgument(1)).apply(comHorario));
    }

    @Test
    void oAtribuidoGanha() {
        var proprio = horario("Próprio", 6);
        var atribuicao = HorarioColaborador.reconstruir(HorarioColaboradorId.gerarNovo(), funcionario, proprio.getId(),
                RegimePrestacao.MISTO, LocalDate.of(2026, 1, 1), null);
        when(horarioColaboradorRepository.findVigente(funcionario, DATA)).thenReturn(Optional.of(atribuicao));

        var v = service.vigente(funcionario, DATA);

        assertEquals(Origem.COLABORADOR, v.origem());
        assertEquals(RegimePrestacao.MISTO, v.regime());
        assertEquals(proprio, v.horario());
    }

    @Test
    void semAtribuicaoValeODaUnidadeEmPresencial() {
        var daUnidade = horario("Unidade", 8);
        unidadeTemHorario(daUnidade.getId());

        var v = service.vigente(funcionario, DATA);

        assertEquals(Origem.UNIDADE, v.origem());
        assertEquals(RegimePrestacao.PRESENCIAL, v.regime());
        assertEquals(daUnidade, v.horario());
    }

    @Test
    void semHorarioNaCadeiaValeOBase() {
        var base = horario("Base", 8);
        base.marcarComoBase();
        when(horarioRepository.findBase()).thenReturn(Optional.of(base));

        var v = service.vigente(funcionario, DATA);

        assertEquals(Origem.BASE, v.origem());
        assertEquals(base, v.horario());
    }

    @Test
    void semBaseNenhum() {
        var v = service.vigente(funcionario, DATA);

        assertEquals(Origem.NENHUM, v.origem());
        assertNull(v.horario());
        assertNull(v.regime());
    }

    @Test
    void atribuirFechaAAnteriorEGravaAsDuas() {
        var novo = horario("Novo", 8);
        var anterior = HorarioColaborador.reconstruir(HorarioColaboradorId.gerarNovo(), funcionario, HorarioId.gerarNovo(),
                RegimePrestacao.PRESENCIAL, LocalDate.of(2026, 1, 1), null);
        when(horarioColaboradorRepository.findByFuncionario(funcionario)).thenReturn(List.of(anterior));
        when(horarioColaboradorRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var r = service.atribuir(funcionario, novo.getId().getStringValor(), null, DATA);

        var captor = ArgumentCaptor.forClass(HorarioColaborador.class);
        verify(horarioColaboradorRepository, org.mockito.Mockito.times(2)).save(captor.capture());
        assertEquals(DATA.minusDays(1), captor.getAllValues().get(0).getDataFim());
        assertEquals(DATA, r.atribuicao().getDataInicio());
        assertTrue(r.alertas().isEmpty());
    }

    @Test
    void horarioInactivoOuInexistenteE422() {
        var inactivo = horario("Velho", 8);
        inactivo.desativar();

        for (String id : List.of(inactivo.getId().getStringValor(), UUID.randomUUID().toString(), "nao-e-uuid", "")) {
            var e = assertThrows(IgrpResponseStatusException.class, () -> service.atribuir(funcionario, id, null, DATA));
            assertEquals(422, e.getStatusCode().value());
        }
        verify(horarioColaboradorRepository, never()).save(any());
    }

    @Test
    void tempoParcialComHorarioDeHorasCompletasDaAlerta() {
        var daUnidade = horario("Unidade", 8);
        unidadeTemHorario(daUnidade.getId());
        var igual = horario("Igual", 8);
        var contrato = mock(Contrato.class);
        when(contrato.getRegimeTrabalho()).thenReturn("TEMPO_PARCIAL");
        when(contratoRepository.findCurrentByFuncionarioId(funcionario)).thenReturn(Optional.of(contrato));
        when(horarioColaboradorRepository.findByFuncionario(funcionario)).thenReturn(List.of());
        when(horarioColaboradorRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        assertEquals(1, service.atribuir(funcionario, igual.getId().getStringValor(), null, DATA).alertas().size());
        var meio = horario("Meio", 4);
        assertTrue(service.atribuir(funcionario, meio.getId().getStringValor(), null, DATA.plusDays(1)).alertas().isEmpty());
    }
}
