package cv.igrp.RH_Service.parametrizacoes.domain.models;

import cv.igrp.RH_Service.parametrizacoes.domain.valueobject.WorkerStateId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.Objects;
import java.util.Optional;

@Getter
public class WorkerState {

    private WorkerStateId id;
    private String code;
    private String description;
    private boolean core;
    private boolean active;
    /** Estado de cessação: ao ser atribuído, termina a relação de emprego público. */
    private boolean endsEmployment;
    /**
     * Situação administrativa perante o quadro (Lei n.º 20/X/2023, art. 117.º) — V42.
     * Nula nos estados de cessação que não são aposentação: quem cessa deixa de ter
     * situação perante o quadro.
     */
    private SituacaoFuncional situacaoFuncional;

    private WorkerState() {}

    private WorkerState(WorkerStateId id, String code, String description, boolean core, boolean active,
                        boolean endsEmployment, SituacaoFuncional situacaoFuncional) {
        this.id = id;
        this.code = code;
        this.description = description;
        this.core = core;
        this.active = active;
        this.endsEmployment = endsEmployment;
        this.situacaoFuncional = situacaoFuncional;
    }

    public static WorkerState criar(String code, String description, Boolean isCore, Boolean endsEmployment) {
        return criar(code, description, isCore, endsEmployment, null);
    }

    public static WorkerState criar(String code, String description, Boolean isCore, Boolean endsEmployment,
                                    String situacaoFuncional) {
        Objects.requireNonNull(code, "code não pode ser nulo");
        boolean effectiveCore = isCore != null && isCore;
        boolean cessa = endsEmployment != null && endsEmployment;
        SituacaoFuncional situacao = SituacaoFuncional.de(situacaoFuncional);
        validarCoerencia(cessa, situacao);
        return new WorkerState(WorkerStateId.gerarNovo(), code, description, effectiveCore, true, cessa, situacao);
    }

    public static WorkerState reconstruir(WorkerStateId id, String code, String description, boolean core,
                                          boolean active, boolean endsEmployment) {
        return new WorkerState(id, code, description, core, active, endsEmployment, null);
    }

    public static WorkerState reconstruir(WorkerStateId id, String code, String description, boolean core,
                                          boolean active, boolean endsEmployment,
                                          SituacaoFuncional situacaoFuncional) {
        return new WorkerState(id, code, description, core, active, endsEmployment, situacaoFuncional);
    }

    public void atualizar(String description) {
        this.description = description;
    }

    public void atualizar(String description, Boolean endsEmployment) {
        atualizar(description, endsEmployment, null);
    }

    /**
     * Actualiza a descrição, a marca de cessação e a situação funcional. Campos nulos
     * não são alterados — excepto a situação, que se limpa com a cadeia vazia.
     */
    public void atualizar(String description, Boolean endsEmployment, String situacaoFuncional) {
        this.description = description;

        boolean novoEndsEmployment = endsEmployment != null ? endsEmployment : this.endsEmployment;
        SituacaoFuncional novaSituacao = situacaoFuncional == null
                ? this.situacaoFuncional
                : SituacaoFuncional.de(situacaoFuncional);

        validarCoerencia(novoEndsEmployment, novaSituacao);

        this.endsEmployment = novoEndsEmployment;
        this.situacaoFuncional = novaSituacao;
    }

    /** A situação funcional deste estado, quando está classificado. */
    public Optional<SituacaoFuncional> situacao() {
        return Optional.ofNullable(situacaoFuncional);
    }

    /**
     * A lei só conhece uma situação que termina o vínculo — a aposentação (art. 93.º
     * al. b) — e quem cessa por outra causa deixa de ter situação perante o quadro.
     * Estas duas leituras da mesma regra têm de bater certo nos dados.
     */
    private static void validarCoerencia(boolean endsEmployment, SituacaoFuncional situacao) {
        if (situacao == null) return;

        if (endsEmployment && !situacao.cessaVinculo())
            throw IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Um estado de cessação só pode ter a situação APOSENTACAO ou ficar sem situação; "
                            + "'" + situacao.name() + "' não termina a relação de emprego público.");

        if (!endsEmployment && situacao.cessaVinculo())
            throw IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY,
                    "A situação APOSENTACAO termina a relação de emprego público (art. 93.º al. b): "
                            + "marque o estado como de cessação (endsEmployment).");
    }

    public void desativar() {
        if (this.core) {
            throw IgrpResponseStatusException.conflict("Não é possível desativar um estado núcleo do sistema.");
        }
        if (!this.active) {
            throw IgrpResponseStatusException.conflict("Já está inactivo.");
        }
        this.active = false;
    }

    public void reativar() {
        if (this.active) {
            throw IgrpResponseStatusException.conflict("Já está activo.");
        }
        this.active = true;
    }
}
