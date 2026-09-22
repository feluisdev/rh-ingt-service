package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.RH_Service.colaboradores.application.dto.SubstituicaoLinhaDTO;
import cv.igrp.RH_Service.colaboradores.application.dto.WrapperListaSubstituicoesDTO;
import cv.igrp.RH_Service.colaboradores.domain.models.Assignment;
import cv.igrp.RH_Service.colaboradores.domain.models.Funcionario;
import cv.igrp.RH_Service.colaboradores.domain.repository.AssignmentRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.AssignmentId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.estrutura.domain.models.OrganizationalUnit;
import cv.igrp.RH_Service.estrutura.domain.models.Position;
import cv.igrp.RH_Service.estrutura.domain.repository.OrganizationalUnitRepository;
import cv.igrp.RH_Service.estrutura.domain.repository.PositionRepository;
import cv.igrp.RH_Service.estrutura.domain.valueobject.PositionId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * <b>Ler as substituições de um colaborador</b>, nos dois papéis.
 *
 * <p>Até aqui a substituição criava-se e mais nada a mostrava: o {@code POST .../substituicao}
 * devolvia o id, e o {@code GET .../unidade-atual} só responde pela afectação {@code PRINCIPAL}.
 * Um ecrã de RH não conseguia dizer <i>quem substitui quem</i> — e na bateria o fecho automático
 * teve de ser provado por via indirecta, tentando abrir uma segunda substituição e verificando
 * que não dava conflito.
 *
 * <p><b>Os dois papéis na mesma consulta, de propósito.</b> A pergunta que um ecrã faz sobre uma
 * pessoa é «em que substituições está metida», e a resposta útil inclui tanto «está a substituir
 * o Francisco» como «está a ser substituída pela Joana». Separá-las em dois endpoints obrigaria
 * o cliente a fazer duas chamadas e a juntá-las.
 *
 * <p><b>Sem data de fim combinada.</b> A substituição caduca quando o titular regressa
 * (art. 77.º n.º 2 da Lei n.º 20/X/2023), e não numa data marcada à partida: enquanto durar, a
 * {@code dataFim} vem nula.
 */
@Component
@RequiredArgsConstructor
public class ListarSubstituicoesQueryHandler
        implements QueryHandler<ListarSubstituicoesQuery, ResponseEntity<WrapperListaSubstituicoesDTO>> {

    private static final String PAPEL_SUBSTITUTO = "SUBSTITUTO";
    private static final String PAPEL_TITULAR = "TITULAR";

    private final AssignmentRepository assignmentRepository;
    private final FuncionarioRepository funcionarioRepository;
    private final PositionRepository positionRepository;
    private final OrganizationalUnitRepository unidadeRepository;

    @IgrpQueryHandler
    public ResponseEntity<WrapperListaSubstituicoesDTO> handle(ListarSubstituicoesQuery query) {
        FuncionarioId funcionarioId = FuncionarioId.from(query.getFuncionarioId());
        funcionarioRepository.findById(funcionarioId)
                .orElseThrow(() -> IgrpResponseStatusException.notFound(
                        "Funcionário não encontrado: " + query.getFuncionarioId()));

        boolean apenasCorrentes = Boolean.TRUE.equals(query.getApenasCorrentes());
        List<Assignment> substituicoes =
                assignmentRepository.findSubstituicoesDoFuncionario(funcionarioId, apenasCorrentes);

        List<SubstituicaoLinhaDTO> linhas = new ArrayList<>();
        Map<UUID, Position> lugares = new HashMap<>();
        Map<UUID, OrganizationalUnit> unidades = new HashMap<>();
        Map<UUID, Funcionario> pessoas = new HashMap<>();

        for (Assignment s : substituicoes) {
            boolean souOSubstituto = s.getFuncionarioId().equals(funcionarioId);

            SubstituicaoLinhaDTO linha = new SubstituicaoLinhaDTO();
            linha.setId(s.getId().getStringValor());
            linha.setPapel(souOSubstituto ? PAPEL_SUBSTITUTO : PAPEL_TITULAR);
            linha.setDataInicio(s.getDataInicio());
            linha.setDataFim(s.getDataFim());
            linha.setCorrente(Boolean.TRUE.equals(s.getIsCurrent()));
            linha.setObservacoes(s.getNotes());

            preencherContraparte(linha, s, souOSubstituto, pessoas);
            preencherLugar(linha, s.getPositionId(), lugares, unidades);

            linhas.add(linha);
        }

        return ResponseEntity.ok(new WrapperListaSubstituicoesDTO(linhas, linhas.size()));
    }

    /**
     * Quem está do outro lado. Se quem pergunta é o substituto, a contraparte é o titular — que
     * se alcança pela afectação coberta; se é o titular, é o substituto, que é o dono desta.
     */
    private void preencherContraparte(SubstituicaoLinhaDTO linha, Assignment s,
                                      boolean souOSubstituto, Map<UUID, Funcionario> cache) {
        Optional<FuncionarioId> contraparte = souOSubstituto
                ? titularDe(s)
                : Optional.of(s.getFuncionarioId());

        contraparte.ifPresent(id -> {
            Funcionario f = cache.computeIfAbsent(id.getValor(),
                    v -> funcionarioRepository.findById(id).orElse(null));
            linha.setContraparteId(id.getStringValor());
            if (f != null) {
                linha.setContraparteNome(f.getNomeCompleto());
                linha.setContraparteNumero(f.getNumeroFuncionario());
            }
        });
    }

    /**
     * O titular é o dono da afectação coberta. Uma substituição sem essa ligação é anterior à
     * V46 — devolve-se a linha na mesma, sem contraparte, em vez de a esconder: o registo
     * existiu e escondê-lo seria pior do que mostrá-lo incompleto.
     */
    private Optional<FuncionarioId> titularDe(Assignment s) {
        if (s.getTitularAssignmentId() == null) return Optional.empty();
        return assignmentRepository.findById(AssignmentId.from(s.getTitularAssignmentId()))
                .map(Assignment::getFuncionarioId);
    }

    private void preencherLugar(SubstituicaoLinhaDTO linha, UUID positionId,
                                Map<UUID, Position> lugares, Map<UUID, OrganizationalUnit> unidades) {
        if (positionId == null) return;
        linha.setPositionId(positionId.toString());

        Position lugar = lugares.computeIfAbsent(positionId,
                id -> positionRepository.findById(PositionId.from(id)).orElse(null));
        if (lugar == null) return;

        linha.setNumeroLugar(lugar.getNumeroLugar());
        if (lugar.getUnidadeOrganicaId() == null) return;

        linha.setUnidadeOrganicaId(lugar.getUnidadeOrganicaId().toString());
        OrganizationalUnit unidade = unidades.computeIfAbsent(lugar.getUnidadeOrganicaId(),
                id -> unidadeRepository.findAllByIds(List.of(id)).stream().findFirst().orElse(null));
        if (unidade != null) linha.setUnidadeOrganicaNome(unidade.getName());
    }
}
