package cv.igrp.RH_Service.colaboradores.domain.models;

import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.PeriodoProvaId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.ProvimentoId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.domain.service.Datas;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.util.Objects;

/**
 * <b>Um provimento</b> (Lei n.º 20/X/2023, arts. 52.º–78.º; BR-PRV-01..06): o acto por que alguém entra
 * (ou muda de forma de vínculo) — a modalidade, o despacho e a <b>posse</b>, que determina o início de
 * funções e a data a partir da qual a nomeação produz efeitos, nunca antes do início de funções (art. 63.º).
 * O estágio probatório e o período experimental ficam no {@link PeriodoProva} que o acompanha.
 */
@Getter
public class Provimento {

    private ProvimentoId id;
    private FuncionarioId funcionarioId;
    private ModalidadeProvimento modalidade;
    private String despachoNumero;
    private LocalDate despachoData;
    /** Data da posse (ou do início de funções, no contrato): o acto produz efeitos a partir dela. */
    private LocalDate dataPosse;
    /** O concurso de onde vem (referência; ou o id da candidatura, quando o concurso está no sistema). */
    private String concursoRef;
    /**
     * Já era nomeado definitivamente noutra carreira: o estágio faz-se em comissão de serviço e, sem sucesso,
     * regressa à carreira de origem em vez de sair (arts. 57.º n.º 2 e 5, 72.º n.os 7 e 8).
     */
    private boolean vemDeOutraCarreira;
    private PeriodoProvaId periodoProvaId;
    /** O provimento anterior de que este é a continuação (a nomeação definitiva depois do estágio). */
    private ProvimentoId anteriorId;
    private String observacoes;

    private Provimento() {}

    public static Provimento registar(FuncionarioId funcionarioId, ModalidadeProvimento modalidade, String despachoNumero,
                                      LocalDate despachoData, LocalDate dataPosse, String concursoRef,
                                      boolean vemDeOutraCarreira, ProvimentoId anteriorId, String observacoes) {
        Objects.requireNonNull(funcionarioId);
        if (modalidade == null)
            throw invalido("Indique a forma do provimento: nomeação provisória, definitiva, em comissão de serviço, contrato "
                    + "de gestão, contrato de estágio, por tempo indeterminado ou a termo.");
        if (dataPosse == null) throw invalido("Indique a data da posse ou do início de funções.");
        if (despachoData != null && dataPosse.isBefore(despachoData))
            throw invalido("A posse não pode ser anterior ao despacho (" + Datas.pt(despachoData) + ").");
        var p = new Provimento();
        p.id = ProvimentoId.gerarNovo();
        p.funcionarioId = funcionarioId;
        p.modalidade = modalidade;
        p.despachoNumero = despachoNumero == null || despachoNumero.isBlank() ? null : despachoNumero.trim();
        p.despachoData = despachoData;
        p.dataPosse = dataPosse;
        p.concursoRef = concursoRef == null || concursoRef.isBlank() ? null : concursoRef.trim();
        p.vemDeOutraCarreira = vemDeOutraCarreira;
        p.anteriorId = anteriorId;
        p.observacoes = observacoes == null || observacoes.isBlank() ? null : observacoes.trim();
        return p;
    }

    public static Provimento reconstruir(ProvimentoId id, FuncionarioId funcionarioId, ModalidadeProvimento modalidade,
                                         String despachoNumero, LocalDate despachoData, LocalDate dataPosse, String concursoRef,
                                         boolean vemDeOutraCarreira, PeriodoProvaId periodoProvaId, ProvimentoId anteriorId,
                                         String observacoes) {
        var p = new Provimento();
        p.id = id;
        p.funcionarioId = funcionarioId;
        p.modalidade = modalidade;
        p.despachoNumero = despachoNumero;
        p.despachoData = despachoData;
        p.dataPosse = dataPosse;
        p.concursoRef = concursoRef;
        p.vemDeOutraCarreira = vemDeOutraCarreira;
        p.periodoProvaId = periodoProvaId;
        p.anteriorId = anteriorId;
        p.observacoes = observacoes;
        return p;
    }

    public void comPeriodoProva(PeriodoProvaId periodo) {
        this.periodoProvaId = Objects.requireNonNull(periodo);
    }

    static IgrpResponseStatusException invalido(String m) {
        return IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY, m);
    }
}
