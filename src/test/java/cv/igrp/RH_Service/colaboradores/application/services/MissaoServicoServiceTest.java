package cv.igrp.RH_Service.colaboradores.application.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cv.igrp.RH_Service.colaboradores.domain.models.Funcionario;
import cv.igrp.RH_Service.colaboradores.domain.models.MissaoServico;
import cv.igrp.RH_Service.colaboradores.domain.models.TipoFactoRh;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.MissaoServicoRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.PedidoAusenciaRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.shared.application.services.notificacoes.Notificador;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.domain.notificacoes.NotificacaoRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/** BR-MSS: em /me, para si ou para a equipa; decide a chefia de todos; um facto por participante; sobreposição dá 409. */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MissaoServicoServiceTest {

    private static final LocalDate D = LocalDate.of(2026, 10, 5);
    @Mock private MissaoServicoRepository repository;
    @Mock private FuncionarioRepository funcionarioRepository;
    @Mock private ChefiaService chefiaService;
    @Mock private PedidoAusenciaRepository pedidoAusenciaRepository;
    @Mock private DiarioFactos diarioFactos;
    @Mock private NotificacaoRepository notificacaoRepository;
    private MissaoServicoService service;
    private final FuncionarioId chefe = FuncionarioId.gerarNovo();
    private final FuncionarioId membro = FuncionarioId.gerarNovo();
    private final FuncionarioId outro = FuncionarioId.gerarNovo();
    private final MissaoServicoService.Dados dados = new MissaoServicoService.Dados(MissaoServico.Destino.NACIONAL, "Sal", "Inspecção",
            D.atTime(8, 0), D.plusDays(1).atTime(18, 0), MissaoServico.Transporte.AVIAO, false, false);

    @BeforeEach
    void setUp() {
        service = new MissaoServicoService(repository, funcionarioRepository, chefiaService, pedidoAusenciaRepository, diarioFactos,
                new Notificador(notificacaoRepository));
        var f = mock(Funcionario.class);
        when(f.getIsActive()).thenReturn(true);
        when(f.getNomeCompleto()).thenReturn("Ana");
        when(funcionarioRepository.findById(any())).thenReturn(Optional.of(f));
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(repository.findSobrepostas(any(), any(), any(), any())).thenReturn(List.of());
        when(pedidoAusenciaRepository.findAprovadosEntre(any(), any(), any())).thenReturn(List.of());
        when(chefiaService.eChefeDirecto(chefe, membro)).thenReturn(true);
    }

    @Test
    void chefiaPedeParaAEquipaEAutorizaComFactoPorParticipante() {
        assertThrows(IgrpResponseStatusException.class, () -> service.pedirComo(chefe, dados, List.of(outro)));
        var m = service.pedirComo(chefe, dados, List.of(membro)).missao();
        when(repository.findById(m.getId())).thenReturn(Optional.of(m));
        var ex = assertThrows(IgrpResponseStatusException.class, () -> service.autorizarComo(outro, m.getId(), null));
        assertEquals(403, ex.getStatusCode().value());
        service.autorizarComo(chefe, m.getId(), "D");
        verify(diarioFactos, times(1)).registar(eq(membro), eq(TipoFactoRh.MISSAO_SERVICO), eq(D), anyString(), any(), anyString(), any());
    }

    @Test
    void sobreposicaoComOutraMissaoDa409() {
        var outra = MissaoServico.pedir(List.of(membro), MissaoServico.Destino.NACIONAL, "Fogo", "X", D.atTime(9, 0), D.atTime(17, 0), null,
                false, false, null);
        when(repository.findSobrepostas(eq(membro), any(), any(), any())).thenReturn(List.of(outra));
        var ex = assertThrows(IgrpResponseStatusException.class, () -> service.pedir(dados, List.of(membro)));
        assertEquals(409, ex.getStatusCode().value());
    }
}
