package cv.igrp.RH_Service.parametrizacoes.application.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

import cv.igrp.RH_Service.parametrizacoes.application.dto.HorarioBlocoDTO;
import cv.igrp.RH_Service.parametrizacoes.application.dto.HorarioRequestDTO;
import cv.igrp.RH_Service.parametrizacoes.application.port.HorarioUtilizacaoPort;
import cv.igrp.RH_Service.parametrizacoes.application.services.HorarioBaseService;
import cv.igrp.RH_Service.parametrizacoes.domain.models.BlocoHorario;
import cv.igrp.RH_Service.parametrizacoes.domain.models.ControloHorario;
import cv.igrp.RH_Service.parametrizacoes.domain.models.Horario;
import cv.igrp.RH_Service.parametrizacoes.domain.models.PeriodoAfericao;
import cv.igrp.RH_Service.parametrizacoes.domain.models.VigenciaHorarioBase;
import cv.igrp.RH_Service.parametrizacoes.domain.repository.HorarioBaseHistoricoRepository;
import cv.igrp.RH_Service.parametrizacoes.domain.repository.HorarioRepository;
import cv.igrp.RH_Service.parametrizacoes.infrastructure.mappers.HorarioMapper;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/**
 * Horarios com data de efeito: o base muda de hoje (ou de uma data futura) e cada dia passado fica com o
 * que vigorava; um horario que ja vigorou nao muda de conteudo (so o nome) -- duplica-se; o PUT nao apaga
 * o omisso, e passar a FIXO limpa o que e so do flexivel.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class HorarioCommandHandlerTest {

    private static final LocalDate HOJE = LocalDate.of(2026, 9, 24);

    @Mock private HorarioRepository repository;
    @Mock private HorarioBaseHistoricoRepository historico;
    @Mock private HorarioUtilizacaoPort colaboradores;

    private final HorarioMapper mapper = new HorarioMapper();
    private final List<VigenciaHorarioBase> linhas = new ArrayList<>();
    private HorarioBaseService baseService;

    private static Horario fixo(String nome) {
        return Horario.criar(nome, ControloHorario.FIXO, null, null,
                List.of(new BlocoHorario(DayOfWeek.MONDAY, LocalTime.of(8, 0), LocalTime.of(16, 0), true)));
    }

    @BeforeEach
    void base() {
        baseService = new HorarioBaseService(repository, historico) {
            @Override protected LocalDate hoje() { return HOJE; }
        };
        when(historico.findAll()).thenAnswer(i -> List.copyOf(linhas));
        doAnswer(i -> {
            VigenciaHorarioBase v = i.getArgument(0);
            linhas.removeIf(l -> v.desde() != null && v.desde().equals(l.desde()));
            linhas.add(v);
            return null;
        }).when(historico).registar(any());
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    private UpdateHorarioCommandHandler update() {
        return new UpdateHorarioCommandHandler(repository, mapper, baseService, List.of(colaboradores, baseService)) {
            @Override protected LocalDate hoje() { return HOJE; }
        };
    }

    @Test
    void marcarBaseDeHojeOAnteriorFicaParaTras() {
        var anterior = fixo("Antigo");
        anterior.marcarComoBase();
        var novo = fixo("Novo");
        when(repository.findById(novo.getId())).thenReturn(Optional.of(novo));
        when(repository.findById(anterior.getId())).thenReturn(Optional.of(anterior));
        when(repository.findBase()).thenReturn(Optional.of(anterior));

        var r = new MarcarHorarioBaseCommandHandler(mapper, baseService).handle(new MarcarHorarioBaseCommand(novo.getId().getStringValor()));

        assertTrue(r.getBody().getIsBase());
        assertFalse(anterior.isBase());
        assertEquals(anterior.getId(), baseService.baseEm(HOJE.minusDays(1)).orElseThrow().getId());
        assertEquals(novo.getId(), baseService.baseEm(HOJE).orElseThrow().getId());
    }

    @Test
    void marcarBaseNumaDataFuturaSoVaiDessaData() {
        var anterior = fixo("Antigo");
        anterior.marcarComoBase();
        var verao = fixo("Verao");
        when(repository.findById(verao.getId())).thenReturn(Optional.of(verao));
        when(repository.findById(anterior.getId())).thenReturn(Optional.of(anterior));
        when(repository.findBase()).thenReturn(Optional.of(anterior));

        var r = new MarcarHorarioBaseCommandHandler(mapper, baseService)
                .handle(new MarcarHorarioBaseCommand(verao.getId().getStringValor(), HOJE.plusDays(7)));

        assertFalse(r.getBody().getIsBase());   // hoje ainda e o antigo
        assertEquals(anterior.getId(), baseService.baseEm(HOJE).orElseThrow().getId());
        assertEquals(verao.getId(), baseService.baseEm(HOJE.plusDays(7)).orElseThrow().getId());
        assertTrue(baseService.eBaseHojeOuDepois(verao.getId()));
    }

    @Test
    void oPrimeiroBaseDaInstituicaoValeDesdeSempre() {
        var h = fixo("Primeiro");
        when(repository.findById(h.getId())).thenReturn(Optional.of(h));
        when(repository.findBase()).thenReturn(Optional.empty());
        new MarcarHorarioBaseCommandHandler(mapper, baseService).handle(new MarcarHorarioBaseCommand(h.getId().getStringValor()));
        assertEquals(h.getId(), baseService.baseEm(LocalDate.of(2020, 1, 1)).orElseThrow().getId());
    }

    @Test
    void marcarBaseNumaDataPassadaE422() {
        var h = fixo("X");
        when(repository.findById(h.getId())).thenReturn(Optional.of(h));
        var e = assertThrows(IgrpResponseStatusException.class, () -> new MarcarHorarioBaseCommandHandler(mapper, baseService)
                .handle(new MarcarHorarioBaseCommand(h.getId().getStringValor(), HOJE.minusDays(1))));
        assertEquals(422, e.getStatusCode().value());
    }

    @Test
    void putSoComONomeMantemOsBlocosMesmoQueJaTenhaVigorado() {
        var h = fixo("Antigo");
        when(repository.findById(h.getId())).thenReturn(Optional.of(h));
        when(colaboradores.vigorouAntesDe(h.getId(), HOJE)).thenReturn(true);

        var dto = new HorarioRequestDTO();
        dto.setNome("Novo nome");
        update().handle(new UpdateHorarioCommand(dto, h.getId().getStringValor()));

        assertEquals("Novo nome", h.getNome());
        assertEquals(1, h.getBlocos().size());
    }

    @Test
    void mudarOsBlocosDeUmHorarioQueJaVigorouE409() {
        var h = fixo("Normal");
        when(repository.findById(h.getId())).thenReturn(Optional.of(h));
        when(colaboradores.vigorouAntesDe(h.getId(), HOJE)).thenReturn(true);

        var dto = new HorarioRequestDTO();
        dto.setBlocos(List.of(new HorarioBlocoDTO(1, "09:00", "17:00", true)));
        var e = assertThrows(IgrpResponseStatusException.class, () -> update().handle(new UpdateHorarioCommand(dto, h.getId().getStringValor())));
        assertEquals(409, e.getStatusCode().value());
        assertEquals(LocalTime.of(8, 0), h.getBlocos().get(0).inicio());
    }

    @Test
    void reenviarOsMesmosBlocosNaoContaComoMudanca() {
        var h = fixo("Normal");
        when(repository.findById(h.getId())).thenReturn(Optional.of(h));
        when(colaboradores.vigorouAntesDe(h.getId(), HOJE)).thenReturn(true);

        var dto = new HorarioRequestDTO();
        dto.setControlo("FIXO");
        dto.setBlocos(List.of(new HorarioBlocoDTO(1, "08:00", "16:00", true)));
        dto.setNome("Normal (renomeado)");
        update().handle(new UpdateHorarioCommand(dto, h.getId().getStringValor()));
        assertEquals("Normal (renomeado)", h.getNome());
    }

    @Test
    void oBaseQueJaVigorouTambemEImutavel() {
        var h = fixo("Base");
        h.marcarComoBase();
        when(repository.findById(h.getId())).thenReturn(Optional.of(h));
        when(repository.findBase()).thenReturn(Optional.of(h));   // sem historico: base desde sempre

        var dto = new HorarioRequestDTO();
        dto.setBlocos(List.of(new HorarioBlocoDTO(1, "09:00", "17:00", true)));
        var e = assertThrows(IgrpResponseStatusException.class, () -> update().handle(new UpdateHorarioCommand(dto, h.getId().getStringValor())));
        assertEquals(409, e.getStatusCode().value());
    }

    @Test
    void passarAFixoLimpaPeriodoEDuracaoQuandoNuncaVigorou() {
        var h = Horario.criar("Flex", ControloHorario.FLEXIVEL, PeriodoAfericao.SEMANA, 6 * 60,
                List.of(new BlocoHorario(DayOfWeek.MONDAY, LocalTime.of(8, 0), LocalTime.of(18, 0), false)));
        when(repository.findById(h.getId())).thenReturn(Optional.of(h));

        var dto = new HorarioRequestDTO();
        dto.setControlo("FIXO");
        update().handle(new UpdateHorarioCommand(dto, h.getId().getStringValor()));

        assertEquals(ControloHorario.FIXO, h.getControlo());
        assertNull(h.getPeriodoAfericao());
        assertNull(h.getDuracaoDiariaMinutos());
        assertEquals(10 * 60, h.minutosSemanais());
    }

    @Test
    void duplicarDaUmaCopiaEditavelQueNaoEBase() {
        var h = fixo("Normal");
        h.marcarComoBase();
        when(repository.findById(h.getId())).thenReturn(Optional.of(h));
        var r = new DuplicarHorarioCommandHandler(repository, mapper, baseService).handle(new DuplicarHorarioCommand(h.getId().getStringValor(), null));
        assertEquals(201, r.getStatusCode().value());
        assertEquals("Normal (cópia)", r.getBody().getNome());
        assertFalse(r.getBody().getIsBase());
        assertEquals(1, r.getBody().getBlocos().size());
    }

    @Test
    void naoSeDesactivaOBaseDeHojeNemOAgendado() {
        var h = fixo("Base");
        h.marcarComoBase();
        when(repository.findById(h.getId())).thenReturn(Optional.of(h));
        when(repository.findBase()).thenReturn(Optional.of(h));
        var e = assertThrows(IgrpResponseStatusException.class, () -> new DesativarHorarioCommandHandler(repository, baseService)
                .handle(new DesativarHorarioCommand(h.getId().getStringValor())));
        assertEquals(409, e.getStatusCode().value());
    }
}
