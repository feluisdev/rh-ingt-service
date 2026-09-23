package cv.igrp.RH_Service.colaboradores.domain.models;

import cv.igrp.RH_Service.colaboradores.domain.valueobject.MapaFeriasId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import lombok.Getter;

import java.time.LocalDate;

/**
 * <b>O mapa de férias de um ano</b> — DL n.º 3/2010, art. 6.º n.º 1: «Até 31 de Março de cada ano,
 * os serviços devem elaborar o mapa de férias e dele dar conhecimento aos respectivos
 * funcionários.»
 *
 * <p><b>Não há aprovação.</b> A lei manda elaborar e dar conhecimento; o acto que conta é a
 * <b>publicação</b>, e é a partir dela que uma marcação só muda com o motivo do n.º 2. O conteúdo
 * do mapa são as marcações de cada colaborador ({@link FeriasDoAno}); aqui guarda-se só quando se
 * deu conhecimento.
 */
@Getter
public class MapaFerias {

    private MapaFeriasId id;
    private int ano;
    private LocalDate publicadoEm;

    private MapaFerias() {}

    public static MapaFerias publicar(int ano, LocalDate hoje) {
        MapaFerias m = new MapaFerias();
        m.id = MapaFeriasId.gerarNovo();
        m.ano = ano;
        m.publicadoEm = hoje;
        return m;
    }

    public static MapaFerias reconstituir(MapaFeriasId id, int ano, LocalDate publicadoEm) {
        MapaFerias m = new MapaFerias();
        m.id = id;
        m.ano = ano;
        m.publicadoEm = publicadoEm;
        return m;
    }

    /** Dar conhecimento duas vezes não é um acto novo: o que muda depois são alterações (n.º 2). */
    public static IgrpResponseStatusException jaPublicado(MapaFerias existente) {
        return IgrpResponseStatusException.conflict("O mapa de férias de " + existente.getAno()
                + " já foi dado a conhecer em " + existente.getPublicadoEm()
                + ". O que muda a partir daqui altera-se marcação a marcação (art. 6.º n.º 2).");
    }
}
