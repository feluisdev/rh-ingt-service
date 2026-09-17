package cv.igrp.RH_Service.colaboradores.domain.models;

import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.LicencaMobilidadeId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.SubtipoLicencaMobilidadeId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import lombok.Getter;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/**
 * Licença ou mobilidade — processo com despacho e aprovação.
 *
 * <p><b>A mobilidade transitória não tira o Lugar ao titular</b> (Lei n.º 20/X/2023, art. 135.º
 * n.º 7: "sem ocupação do lugar do quadro"; art. 137.º: o tempo conta no lugar de origem). Este
 * registo diz apenas <i>onde a pessoa exerce funções</i> e até quando; a titularidade do Lugar
 * vive na Afectação e não é tocada. A mudança <b>definitiva</b> de Lugar é outra coisa: é a
 * transferência.
 *
 * <p>O destino é interno (uma unidade nossa, {@code destinationUnitId}) ou externo (uma entidade
 * de fora, {@code entidadeDestino}) — o comportamento é o mesmo nos dois casos.
 */
@Getter
public class LicencaMobilidade {

    public static final String PENDING = "PENDING";
    public static final String ACTIVE = "ACTIVE";
    public static final String REJECTED = "REJECTED";
    public static final String CLOSED = "CLOSED";
    public static final String CANCELLED = "CANCELLED";

    private LicencaMobilidadeId id;
    private FuncionarioId funcionarioId;
    private SubtipoLicencaMobilidadeId subtipoId;
    private LocalDate dataInicio;
    private LocalDate dataFim;
    private String entidadeDestino;
    private String despachoNumero;
    private String observacoes;
    private Boolean isActive;
    private String status;
    /** Destino interno: unidade orgânica nossa onde passa a exercer funções. */
    private UUID destinationUnitId;
    /**
     * Legado do modelo anterior, em que a mobilidade abria uma afectação no destino. A mobilidade
     * transitória não ocupa Lugar no destino; a mudança de Lugar faz-se pela transferência.
     */
    private UUID destinationPositionId;
    private String justification;
    private UUID documentId;
    private String rejectionReason;
    /** Prorrogações já concedidas (art. 132.º n.º 5: em regra, uma única). */
    private Integer extensionsCount;

    private LicencaMobilidade() {}

    public static LicencaMobilidade criar(FuncionarioId funcionarioId,
                                          SubtipoLicencaMobilidadeId subtipoId,
                                          LocalDate dataInicio, LocalDate dataFim,
                                          String entidadeDestino, String despachoNumero,
                                          String observacoes, String justification,
                                          UUID destinationUnitId, UUID destinationPositionId,
                                          UUID documentId) {
        if (dataInicio == null)
            throw IgrpResponseStatusException.badRequest("A data de início é obrigatória.");
        if (dataFim != null && dataFim.isBefore(dataInicio))
            throw IgrpResponseStatusException.badRequest("A data de fim não pode ser anterior à data de início.");

        LicencaMobilidade l = new LicencaMobilidade();
        l.id = LicencaMobilidadeId.gerarNovo();
        l.funcionarioId = funcionarioId;
        l.subtipoId = subtipoId;
        l.dataInicio = dataInicio;
        l.dataFim = dataFim;
        l.entidadeDestino = entidadeDestino;
        l.despachoNumero = despachoNumero;
        l.observacoes = observacoes;
        l.justification = justification;
        l.destinationUnitId = destinationUnitId;
        l.destinationPositionId = destinationPositionId;
        l.documentId = documentId;
        l.isActive = true;
        l.status = PENDING;
        l.extensionsCount = 0;
        return l;
    }

    public static LicencaMobilidade reconstituir(LicencaMobilidadeId id,
                                                  FuncionarioId funcionarioId,
                                                  SubtipoLicencaMobilidadeId subtipoId,
                                                  LocalDate dataInicio, LocalDate dataFim,
                                                  String entidadeDestino, String despachoNumero,
                                                  String observacoes, Boolean isActive,
                                                  String status, UUID destinationUnitId,
                                                  UUID destinationPositionId,
                                                  String justification, UUID documentId,
                                                  String rejectionReason, Integer extensionsCount) {
        LicencaMobilidade l = new LicencaMobilidade();
        l.id = id;
        l.funcionarioId = funcionarioId;
        l.subtipoId = subtipoId;
        l.dataInicio = dataInicio;
        l.dataFim = dataFim;
        l.entidadeDestino = entidadeDestino;
        l.despachoNumero = despachoNumero;
        l.observacoes = observacoes;
        l.isActive = isActive;
        l.status = status != null ? status : PENDING;
        l.destinationUnitId = destinationUnitId;
        l.destinationPositionId = destinationPositionId;
        l.justification = justification;
        l.documentId = documentId;
        l.rejectionReason = rejectionReason;
        l.extensionsCount = extensionsCount != null ? extensionsCount : 0;
        return l;
    }

    public void atualizar(LocalDate dataInicio, LocalDate dataFim, String entidadeDestino,
                          String despachoNumero, String observacoes) {
        if (!isPending())
            throw IgrpResponseStatusException.conflict(
                    "Só um registo PENDING pode ser actualizado. Estado actual: " + this.status);
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
        this.entidadeDestino = entidadeDestino;
        this.despachoNumero = despachoNumero;
        this.observacoes = observacoes;
    }

    /** Aprovar = pôr em vigor. Só a partir de PENDING. */
    public void aprovar() {
        if (!isPending())
            throw IgrpResponseStatusException.conflict(
                    "Apenas registos PENDING podem ser aprovados. Estado actual: " + this.status);
        this.isActive = true;
        this.status = ACTIVE;
    }

    public void rejeitar(String reason) {
        if (!isPending())
            throw IgrpResponseStatusException.conflict(
                    "Apenas registos PENDING podem ser rejeitados. Estado actual: " + this.status);
        this.isActive = false;
        this.status = REJECTED;
        this.rejectionReason = reason;
    }

    /** Encerrar no fim do período (ou antes, com regresso antecipado). */
    public void encerrar(LocalDate dataFimEfectiva) {
        if (!isApproved())
            throw IgrpResponseStatusException.conflict(
                    "Apenas registos ACTIVE podem ser encerrados. Estado actual: " + this.status);
        this.isActive = false;
        this.status = CLOSED;
        this.dataFim = dataFimEfectiva != null ? dataFimEfectiva
                : (this.dataFim != null ? this.dataFim : LocalDate.now());
    }

    public void cancelar() {
        if (!isPending() && !isApproved())
            throw IgrpResponseStatusException.conflict(
                    "Apenas registos PENDING ou ACTIVE podem ser cancelados. Estado actual: " + this.status);
        this.isActive = false;
        this.status = CANCELLED;
    }

    /**
     * Prorroga o período. O limite de prorrogações vem do subtipo (parametrizado); a lei prevê,
     * em regra, uma única prorrogação por igual período (art. 132.º n.º 5).
     */
    public void prorrogar(LocalDate novaDataFim, Integer maxExtensions) {
        if (!isApproved())
            throw IgrpResponseStatusException.conflict(
                    "Apenas registos ACTIVE podem ser prorrogados. Estado actual: " + this.status);
        if (novaDataFim == null)
            throw IgrpResponseStatusException.badRequest("A nova data de fim é obrigatória.");
        if (this.dataFim != null && !novaDataFim.isAfter(this.dataFim))
            throw IgrpResponseStatusException.badRequest(
                    "A nova data de fim tem de ser posterior à actual (" + this.dataFim + ").");
        if (maxExtensions != null && extensoes() >= maxExtensions)
            throw IgrpResponseStatusException.of(org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY,
                    "Limite de prorrogações atingido (" + maxExtensions + ").");

        this.dataFim = novaDataFim;
        this.extensionsCount = extensoes() + 1;
    }

    /** Soft delete do registo (não é uma transição de estado do processo). */
    public void desativar() {
        this.isActive = false;
    }

    public void associarDocumento(UUID documentId) {
        this.documentId = documentId;
    }

    /** Duração em dias, contando o dia de início. Nulo quando o período é aberto. */
    public Long duracaoEmDias() {
        if (dataInicio == null || dataFim == null) return null;
        return ChronoUnit.DAYS.between(dataInicio, dataFim) + 1;
    }

    public int extensoes() { return extensionsCount != null ? extensionsCount : 0; }

    /** Destino interno = uma unidade nossa; caso contrário é externo (entidade de fora). */
    public boolean isDestinoInterno() { return destinationUnitId != null; }

    public boolean isPending() { return PENDING.equals(this.status); }
    public boolean isApproved() { return ACTIVE.equals(this.status); }
}
