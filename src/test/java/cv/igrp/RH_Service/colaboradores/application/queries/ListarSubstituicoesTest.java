package cv.igrp.RH_Service.colaboradores.application.queries;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cv.igrp.RH_Service.colaboradores.domain.models.Assignment;
import cv.igrp.RH_Service.colaboradores.domain.models.Funcionario;
import cv.igrp.RH_Service.colaboradores.domain.models.TipoAfectacao;
import cv.igrp.RH_Service.colaboradores.domain.repository.AssignmentRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.AssignmentId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.estrutura.domain.repository.OrganizationalUnitRepository;
import cv.igrp.RH_Service.estrutura.domain.repository.PositionRepository;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * <b>Ler as substituições</b> — a lacuna que a V46 deixou aberta.
 *
 * <p>A substituição criava-se e mais nada a mostrava: o {@code POST} devolvia o id e o
 * {@code unidade-atual} só responde pela afectação {@code PRINCIPAL}. Um ecrã de RH não
 * conseguia dizer quem substitui quem.
 */
@ExtendWith(MockitoExtension.class)
class ListarSubstituicoesTest {

    private static final FuncionarioId SUBSTITUTO = FuncionarioId.gerarNovo();
    private static final FuncionarioId TITULAR = FuncionarioId.gerarNovo();
    private static final UUID LUGAR = UUID.randomUUID();

    @Mock private AssignmentRepository assignmentRepository;
    @Mock private FuncionarioRepository funcionarioRepository;
    @Mock private PositionRepository positionRepository;
    @Mock private OrganizationalUnitRepository unidadeRepository;

    private ListarSubstituicoesQueryHandler handler() {
        return new ListarSubstituicoesQueryHandler(
                assignmentRepository, funcionarioRepository, positionRepository, unidadeRepository);
    }

    private static Assignment afectacaoDoTitular() {
        return Assignment.reconstituir(AssignmentId.gerarNovo(), TITULAR, LUGAR, null, null,
                TipoAfectacao.PRINCIPAL.name(), "ADMISSAO", null,
                LocalDate.now().minusYears(2), null, true, true, null);
    }

    private static Assignment substituicao(AssignmentId idDaAfectacaoDoTitular, boolean corrente) {
        return Assignment.reconstituir(AssignmentId.gerarNovo(), SUBSTITUTO, LUGAR, null, null,
                TipoAfectacao.SUBSTITUICAO.name(), "SUBSTITUICAO", idDaAfectacaoDoTitular.getValor(),
                LocalDate.now().minusDays(20), corrente ? null : LocalDate.now().minusDays(2),
                corrente, true, "titular impedido");
    }

    /**
     * Regista o colaborador no repositorio. Nome e numero ficam lenientes de proposito: quem e
     * consultado so precisa de existir -- o nome dele nunca e lido, e so o da contraparte e que
     * aparece na resposta.
     */
    private void existe(FuncionarioId id, String nome, String numero) {
        Funcionario f = mock(Funcionario.class);
        when(funcionarioRepository.findById(id)).thenReturn(Optional.of(f));
        lenient().when(f.getNomeCompleto()).thenReturn(nome);
        lenient().when(f.getNumeroFuncionario()).thenReturn(numero);
    }

    /** Quem pergunta é o substituto: a contraparte é o titular que ele cobre. */
    @Test
    void vistaDoSubstitutoTrazOTitular() {
        var doTitular = afectacaoDoTitular();
        existe(SUBSTITUTO, "Joana Tavares", "0000003");
        existe(TITULAR, "Francisco Bastos", "0000001");
        when(assignmentRepository.findSubstituicoesDoFuncionario(any(), anyBoolean()))
                .thenReturn(List.of(substituicao(doTitular.getId(), true)));
        when(assignmentRepository.findById(doTitular.getId())).thenReturn(Optional.of(doTitular));

        var resposta = handler().handle(new ListarSubstituicoesQuery(SUBSTITUTO.getStringValor(), true));

        var linhas = resposta.getBody().getLinhas();
        assertEquals(1, linhas.size());
        assertEquals("SUBSTITUTO", linhas.get(0).getPapel());
        assertEquals("Francisco Bastos", linhas.get(0).getContraparteNome());
    }

    /** Quem pergunta é o titular impedido: a contraparte é quem o está a substituir. */
    @Test
    void vistaDoTitularTrazOSubstituto() {
        var doTitular = afectacaoDoTitular();
        existe(TITULAR, "Francisco Bastos", "0000001");
        existe(SUBSTITUTO, "Joana Tavares", "0000003");
        when(assignmentRepository.findSubstituicoesDoFuncionario(any(), anyBoolean()))
                .thenReturn(List.of(substituicao(doTitular.getId(), true)));

        var resposta = handler().handle(new ListarSubstituicoesQuery(TITULAR.getStringValor(), true));

        var linhas = resposta.getBody().getLinhas();
        assertEquals("TITULAR", linhas.get(0).getPapel());
        assertEquals("Joana Tavares", linhas.get(0).getContraparteNome());
    }

    /**
     * Enquanto dura, a substituição não tem fim: caduca quando o titular regressa
     * (art. 77.º n.º 2), e não numa data marcada à partida.
     */
    @Test
    void aSubstituicaoEmVigorNaoTemDataDeFim() {
        var doTitular = afectacaoDoTitular();
        existe(SUBSTITUTO, "Joana Tavares", "0000003");
        existe(TITULAR, "Francisco Bastos", "0000001");
        when(assignmentRepository.findSubstituicoesDoFuncionario(any(), anyBoolean()))
                .thenReturn(List.of(substituicao(doTitular.getId(), true)));
        when(assignmentRepository.findById(doTitular.getId())).thenReturn(Optional.of(doTitular));

        var linha = handler().handle(
                new ListarSubstituicoesQuery(SUBSTITUTO.getStringValor(), true)).getBody().getLinhas().get(0);

        assertNull(linha.getDataFim());
        assertEquals(true, linha.isCorrente());
    }

    /** O histórico é o que se pede sem filtro; o filtro chega ao repositório, não à memória. */
    @Test
    void semFiltroPedeOHistoricoAoRepositorio() {
        existe(SUBSTITUTO, "Joana Tavares", "0000003");
        when(assignmentRepository.findSubstituicoesDoFuncionario(any(), anyBoolean()))
                .thenReturn(List.of());

        handler().handle(new ListarSubstituicoesQuery(SUBSTITUTO.getStringValor(), false));

        verify(assignmentRepository).findSubstituicoesDoFuncionario(SUBSTITUTO, false);
    }

    @Test
    void colaboradorInexistenteDa404() {
        when(funcionarioRepository.findById(any())).thenReturn(Optional.empty());

        var ex = assertThrows(IgrpResponseStatusException.class,
                () -> handler().handle(new ListarSubstituicoesQuery(UUID.randomUUID().toString(), true)));

        assertEquals(404, ex.getStatusCode().value());
    }
}
