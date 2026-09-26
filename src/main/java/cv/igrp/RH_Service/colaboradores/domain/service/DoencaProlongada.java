package cv.igrp.RH_Service.colaboradores.domain.service;

import cv.igrp.RH_Service.colaboradores.domain.models.PedidoAusencia;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * <b>Doença prolongada</b> (DL n.º 3/2010, art. 26.º n.º 1; BR-SST-19): atingidos 30 dias consecutivos de ausência por doença, se o
 * funcionário não está em condições de regressar, é submetido à comissão de verificação de incapacidades — salvo internamento e
 * doença no estrangeiro, que o sistema não sabe e o RH decide.
 *
 * <p>Os pedidos de doença seguidos juntam-se num só período; um intervalo feito só de sábados e domingos não o interrompe [ind.].
 * Conta-se do início do período até ao dia (inclusive), e só enquanto a ausência dura: quem já regressou não vai à comissão.
 */
public final class DoencaProlongada {

    /** Art. 26.º n.º 1: o limite de dias consecutivos. */
    public static final int LIMITE_DIAS = 30;

    private DoencaProlongada() {}

    /** Um período seguido de doença de um colaborador que chega a {@code dia}; {@code dias} contados até ao dia. */
    public record Periodo(FuncionarioId funcionarioId, LocalDate de, LocalDate ate, int dias) {}

    /** Os períodos de doença em curso no {@code dia} que atingiram o limite. */
    public static List<Periodo> atingidos(List<PedidoAusencia> pedidosDeDoenca, LocalDate dia) {
        var porColaborador = new LinkedHashMap<FuncionarioId, List<PedidoAusencia>>();
        for (var p : pedidosDeDoenca) porColaborador.computeIfAbsent(p.getFuncionarioId(), k -> new ArrayList<>()).add(p);
        var resultado = new ArrayList<Periodo>();
        porColaborador.forEach((fid, pedidos) -> periodoEm(fid, pedidos, dia)
                .filter(per -> per.dias() >= LIMITE_DIAS).ifPresent(resultado::add));
        return resultado;
    }

    /** O período seguido que contém {@code dia}, se houver. */
    static java.util.Optional<Periodo> periodoEm(FuncionarioId fid, List<PedidoAusencia> pedidos, LocalDate dia) {
        var ordenados = pedidos.stream().sorted(Comparator.comparing(PedidoAusencia::getDataInicio)).toList();
        LocalDate de = null, ate = null;
        for (var p : ordenados) {
            if (de != null && seguido(ate, p.getDataInicio())) {
                if (p.getDataFim().isAfter(ate)) ate = p.getDataFim();
                continue;
            }
            if (de != null && !dia.isBefore(de) && !dia.isAfter(ate)) break;
            de = p.getDataInicio();
            ate = p.getDataFim();
        }
        if (de == null || dia.isBefore(de) || dia.isAfter(ate)) return java.util.Optional.empty();
        return java.util.Optional.of(new Periodo(fid, de, ate, (int) ChronoUnit.DAYS.between(de, dia) + 1));
    }

    /** O pedido seguinte continua o período: começa até ao dia a seguir ao fim, ou só há fim-de-semana entre os dois. */
    static boolean seguido(LocalDate fim, LocalDate inicioSeguinte) {
        for (var d = fim.plusDays(1); d.isBefore(inicioSeguinte); d = d.plusDays(1))
            if (d.getDayOfWeek() != DayOfWeek.SATURDAY && d.getDayOfWeek() != DayOfWeek.SUNDAY) return false;
        return true;
    }
}
