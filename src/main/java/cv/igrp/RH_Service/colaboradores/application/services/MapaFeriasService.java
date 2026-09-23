package cv.igrp.RH_Service.colaboradores.application.services;

import cv.igrp.RH_Service.parametrizacoes.application.services.ParametrosFeriasService;
import cv.igrp.RH_Service.colaboradores.domain.models.FeriasDoAno;
import cv.igrp.RH_Service.colaboradores.domain.models.Funcionario;
import cv.igrp.RH_Service.colaboradores.domain.models.MapaFerias;
import cv.igrp.RH_Service.colaboradores.domain.models.MotivoAlteracaoMapaFerias;
import cv.igrp.RH_Service.colaboradores.domain.models.OrigemMarcacaoFerias;
import cv.igrp.RH_Service.colaboradores.domain.models.PeriodoFerias;
import cv.igrp.RH_Service.colaboradores.domain.models.RegrasMarcacaoFerias;
import cv.igrp.RH_Service.colaboradores.domain.models.SaldoAusencia;
import cv.igrp.RH_Service.colaboradores.domain.repository.FeriasDoAnoRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.MapaFeriasRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.TipoAusenciaRepository;
import cv.igrp.RH_Service.colaboradores.domain.service.DiasUteisCalculator;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.parametrizacoes.domain.models.ContagemDias;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * <b>Mapa de férias</b> — DL n.º 3/2010, arts. 5.º e 6.º.
 *
 * <p>Orquestra o que o agregado {@link FeriasDoAno} não sabe ir buscar: o direito do ano (do
 * saldo), os dias úteis de cada período (do calendário de feriados do colaborador), os parâmetros
 * da lei e se o mapa já foi dado a conhecer. As regras vivem no agregado.
 *
 * <p><b>Marcar não é gozar</b>: nada aqui mexe em saldos. O gozo continua a ser o pedido de férias.
 */
@Service
@RequiredArgsConstructor
public class MapaFeriasService {

    private final FeriasDoAnoRepository feriasDoAnoRepository;
    private final MapaFeriasRepository mapaFeriasRepository;
    private final FuncionarioRepository funcionarioRepository;
    private final TipoAusenciaRepository tipoAusenciaRepository;
    private final FeriasService feriasService;
    private final CalendarioFeriadosService calendarioFeriadosService;
    private final DiasUteisCalculator diasUteisCalculator;
    private final ParametrosFeriasService parametrosFerias;

    /** Art. 5.º n.º 4. Fora do prazo é aceite, com alerta. */
    @Transactional
    public Resultado indicarPreferencia(FuncionarioId funcionarioId, int ano,
                                        List<PeriodoFerias> periodos, String observacoes) {
        funcionario(funcionarioId);
        FeriasDoAno ferias = feriasDoAno(funcionarioId, ano);
        List<String> alertas = ferias.indicarPreferencia(periodos, observacoes, hoje(), parametrosFerias.vigenteEm(ano).prazoPreferencia(ano));
        return new Resultado(feriasDoAnoRepository.save(ferias), alertas);
    }

    /** Arts. 5.º e 6.º n.º 2: marca, ou altera a marcação com o motivo que a lei exige. */
    @Transactional
    public Resultado marcar(FuncionarioId funcionarioId, int ano, List<PeriodoFerias> periodos,
                            OrigemMarcacaoFerias origem, String fundamentacao, MotivoAlteracaoMapaFerias motivo) {
        Funcionario funcionario = funcionario(funcionarioId);
        FeriasDoAno ferias = feriasDoAno(funcionarioId, ano);

        // Os dias úteis de cada período, com o calendário de feriados de quem vai gozar.
        List<PeriodoFerias> contados = new ArrayList<>();
        for (PeriodoFerias p : periodos) {
            if (p.inicio() == null || p.fim() == null || p.inicio().isAfter(p.fim())) {
                contados.add(p);   // o agregado recusa com a mensagem certa
                continue;
            }
            var feriados = calendarioFeriadosService.feriadosDoColaborador(funcionarioId, p.inicio(), p.fim());
            contados.add(p.comDiasUteis(contarUteis(p, feriados)));
        }

        boolean mapaPublicado = mapaFeriasRepository.findByAno(ano).isPresent();
        List<String> alertas = ferias.marcar(contados, origem, fundamentacao, regras(funcionario, ano),
                mapaPublicado, motivo, hoje());
        return new Resultado(feriasDoAnoRepository.save(ferias), alertas);
    }

    /** Art. 6.º n.º 1: dar conhecimento do mapa. Depois do prazo é aceite, com alerta. */
    @Transactional
    public Publicacao publicar(int ano) {
        Optional<MapaFerias> existente = mapaFeriasRepository.findByAno(ano);
        if (existente.isPresent()) throw MapaFerias.jaPublicado(existente.get());

        LocalDate hoje = hoje();
        MapaFerias mapa = mapaFeriasRepository.save(MapaFerias.publicar(ano, hoje));

        List<String> alertas = new ArrayList<>();
        LocalDate prazo = parametrosFerias.vigenteEm(ano).prazoMapa(ano);
        if (hoje.isAfter(prazo))
            alertas.add("Mapa dado a conhecer depois de " + prazo + " (art. 6.º n.º 1).");
        int semMarcacao = feriasDoAnoRepository.findFuncionariosActivosSemMarcacao(ano).size();
        if (semMarcacao > 0)
            alertas.add(semMarcacao + " colaborador(es) activo(s) sem férias marcadas em " + ano
                    + ". Sem acordo, é o dirigente que as fixa entre Maio e Outubro (art. 5.º n.º 5).");
        return new Publicacao(mapa, alertas);
    }

    public Optional<FeriasDoAno> consultar(FuncionarioId funcionarioId, int ano) {
        return feriasDoAnoRepository.findByFuncionarioIdAndAno(funcionarioId, ano);
    }

    /** Direito do ano para a marcação, ou nulo se não houver tipo de férias no catálogo. */
    public Integer direitoParaMarcar(FuncionarioId funcionarioId, int ano) {
        return feriasService.garantirSaldoDoAno(funcionarioId, ano).map(MapaFeriasService::direitoDe).orElse(null);
    }

    RegrasMarcacaoFerias regras(Funcionario funcionario, int ano) {
        Integer direito = direitoParaMarcar(funcionario.getId(), ano);
        int maximoSeguidos = tipoAusenciaRepository.findFerias()
                .map(feriasService::direitoAnual)
                .orElse(FeriasService.DIAS_UTEIS_POR_LEI);
        boolean anoDeIngresso = funcionario.getDataAdmissao() != null
                && funcionario.getDataAdmissao().getYear() == ano;
        var parametros = parametrosFerias.vigenteEm(ano);
        return new RegrasMarcacaoFerias(direito, maximoSeguidos, parametros.getPeriodoMinimoInterpolado(),
                anoDeIngresso, parametros.fixacaoInicio(ano), parametros.fixacaoFim(ano));
    }

    /**
     * O que se pode marcar: o direito vencido mais os dias recebidos do ano anterior, menos os
     * cedidos ao seguinte. Não se descontam os já gozados — a marcação é o plano do ano inteiro, e
     * os dias gozados fazem parte dele.
     */
    private static int direitoDe(SaldoAusencia s) {
        return s.getDiasDireito() + s.getDiasAcumulados() - s.getDiasTransportados();
    }

    private int contarUteis(PeriodoFerias p, Set<LocalDate> feriados) {
        try {
            return diasUteisCalculator.calcular(p.inicio(), p.fim(), feriados, ContagemDias.DIAS_UTEIS);
        } catch (IgrpResponseStatusException e) {
            return 0;   // período sem dias úteis: o agregado recusa-o com a mensagem da marcação
        }
    }

    private Funcionario funcionario(FuncionarioId id) {
        return funcionarioRepository.findById(id)
                .orElseThrow(() -> IgrpResponseStatusException.notFound("Funcionário não encontrado: " + id.getStringValor()));
    }

    private FeriasDoAno feriasDoAno(FuncionarioId funcionarioId, int ano) {
        return feriasDoAnoRepository.findByFuncionarioIdAndAno(funcionarioId, ano)
                .orElseGet(() -> FeriasDoAno.novo(funcionarioId, ano));
    }

    private LocalDate hoje() { return LocalDate.now(); }

    public record Resultado(FeriasDoAno ferias, List<String> alertas) {}

    public record Publicacao(MapaFerias mapa, List<String> alertas) {}
}
