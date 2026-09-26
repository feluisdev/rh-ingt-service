package cv.igrp.RH_Service.colaboradores.application.services;

import cv.igrp.RH_Service.colaboradores.domain.models.EspecieProcessoDisciplinar;
import cv.igrp.RH_Service.colaboradores.domain.models.PedidoAusencia;
import cv.igrp.RH_Service.colaboradores.domain.repository.PedidoAusenciaRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.ProcessoDisciplinarRepository;
import cv.igrp.RH_Service.colaboradores.domain.service.RegrasAssiduidadeDisciplinar;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.shared.application.services.notificacoes.Notificador;
import cv.igrp.RH_Service.shared.domain.notificacoes.NotificacaoRepository;
import cv.igrp.RH_Service.shared.domain.notificacoes.TipoNotificacao;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Predicate;

/**
 * <b>Autos sugeridos</b> por falta de assiduidade e abandono de lugar (Estatuto Disciplinar, arts. 80.º e 81.º; BR-DIS-25):
 * a partir das faltas injustificadas aprovadas, avisa o RH e a chefia de que há auto a levantar. Não abre o processo
 * sozinho — o auto é do superior hierárquico [interp.]; o RH participa com a espécie sugerida.
 */
@Service
@RequiredArgsConstructor
public class AutosAssiduidadeService {

    static final String RECURSO = "AUTO_ASSIDUIDADE";

    public record Sugestao(FuncionarioId funcionarioId, String nome, RegrasAssiduidadeDisciplinar.Sinal sinal) {}

    private final PedidoAusenciaRepository pedidoAusenciaRepository;
    private final ProcessoDisciplinarRepository processoRepository;
    private final CalendarioFeriadosService calendario;
    private final ChefiaService chefiaService;
    private final ProcessoDisciplinarService processos;
    private final NotificacaoRepository notificacaoRepository;
    private final Notificador notificador;

    /** Os colaboradores em que, a este dia, as faltas injustificadas chegam a auto — sem processo dessa espécie em curso. */
    @Transactional(readOnly = true)
    public List<Sugestao> sugestoes(LocalDate dia) {
        LocalDate desde = dia.minusMonths(24);
        Map<FuncionarioId, List<PedidoAusencia>> porPessoa = new LinkedHashMap<>();
        for (var p : pedidoAusenciaRepository.findFaltasInjustificadasDesde(desde))
            if (!p.isEmHoras()) porPessoa.computeIfAbsent(p.getFuncionarioId(), k -> new ArrayList<>()).add(p);
        var l = new ArrayList<Sugestao>();
        for (var e : porPessoa.entrySet()) {
            var fid = e.getKey();
            Predicate<LocalDate> util = util(fid, desde, dia);
            var faltas = new TreeSet<LocalDate>();
            for (var p : e.getValue()) {
                LocalDate de = p.getDataInicio().isBefore(desde) ? desde : p.getDataInicio();
                LocalDate ate = p.ultimoDiaEmVigor().isAfter(dia) ? dia : p.ultimoDiaEmVigor();
                if (!de.isAfter(ate)) faltas.addAll(RegrasAssiduidadeDisciplinar.uteis(de, ate, util));
            }
            RegrasAssiduidadeDisciplinar.avaliar(faltas, dia, util)
                    .filter(s -> processoRepository.findAllByFuncionarioId(fid).stream()
                            .noneMatch(pr -> pr.getEspecie() == s.especie() && pr.getFase() != null
                                    && (pr.getFase().emCurso() || pr.getStartDate().getYear() == dia.getYear())))
                    .ifPresent(s -> l.add(new Sugestao(fid, processos.nome(fid), s)));
        }
        return l;
    }

    /** Para o job: um aviso por colaborador, espécie e ano (ao RH e à chefia directa). */
    @Transactional
    public int avisar(LocalDate dia) {
        int avisos = 0;
        for (var s : sugestoes(dia)) {
            String chave = s.funcionarioId().getStringValor() + ":" + dia.getYear();
            String tipo = RECURSO + "_" + s.sinal().especie().name();
            if (notificacaoRepository.existeSobre(TipoNotificacao.AUTO_ASSIDUIDADE, tipo, chave)) continue;
            String auto = s.sinal().especie() == EspecieProcessoDisciplinar.ABANDONO_LUGAR ? "abandono de lugar" : "falta de assiduidade";
            String titulo = "Há auto por " + auto + " a levantar: " + s.nome();
            notificador.paraRh().tipo(TipoNotificacao.AUTO_ASSIDUIDADE).titulo(titulo).texto(s.sinal().motivo() + ".")
                    .recurso(tipo, chave).enviar();
            notificador.para(chefiaService.chefeDirecto(s.funcionarioId())).tipo(TipoNotificacao.AUTO_ASSIDUIDADE).titulo(titulo)
                    .texto(s.sinal().motivo() + ". O auto é levantado pelo superior hierárquico (arts. 80.º e 81.º do Estatuto Disciplinar).")
                    .recurso(tipo, chave).enviar();
            avisos++;
        }
        return avisos;
    }

    private Predicate<LocalDate> util(FuncionarioId fid, LocalDate de, LocalDate ate) {
        Set<LocalDate> feriados = calendario.feriadosDoColaborador(fid, de, ate);
        return d -> d.getDayOfWeek() != DayOfWeek.SATURDAY && d.getDayOfWeek() != DayOfWeek.SUNDAY && !feriados.contains(d);
    }
}
