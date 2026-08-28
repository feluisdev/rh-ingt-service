package cv.igrp.RH_Service.authorization.permission;

import cv.igrp.framework.stereotype.IgrpPermission;

/**
 * Single declaration of every permission this service enforces, mirroring the layout the
 * {@code cadastro} project uses ({@code cv.igrp.dnre.cadastro.authorization.permission.AppPermissions}).
 * <p>
 * <b>What consumes this class.</b> {@code PermissionSourceGenerator}, the annotation processor
 * shipped in {@code cv.igrp.framework.auth:core}, reads every {@link IgrpPermission} here and
 * generates {@code cv.igrp.framework.auth.generated.PermissionsRegistry.Permission}. That enum is
 * what {@code @PreAuthorize("@igrpAuthorization.checkPermission(T(Permission).X)")} resolves, and
 * what {@code AuthorizationSyncRunner} pushes to the access-management platform on startup. The
 * constants below are therefore the source of truth; the enum and the sync are both derived.
 * <p>
 * <b>Keep in step with {@code .igrpstudio/permissions.json}.</b> That manifest declares the same
 * eight permissions for the access-management side. {@code AppPermissionsManifestTest} (Phase
 * 120, plan 03) verifies the correspondence in both directions -- adding a permission in one
 * place and not the other now fails that test instead of failing silently. That test cannot read
 * {@link IgrpPermission} directly by reflection: the annotation has
 * {@code @Retention(RetentionPolicy.SOURCE)} and is stripped from the compiled {@code .class} --
 * it compares the manifest against {@code PermissionsRegistry.Permission} instead, the enum this
 * same annotation processor generates from these constants.
 * <p>
 * <b>These replaced the employee-id allow-lists.</b> Until Phase 115 the only authorization in
 * this service was two comma-separated lists of employee ids read from the environment
 * ({@code SIADAP_CCA_EMPLOYEE_IDS}, {@code SIADAP_SELF_EVALUATION_OPENER_EMPLOYEE_IDS}) plus a
 * role literal defaulting to {@code "RH"} that was never confirmed against the IAM realm. Both
 * lists and the role literal, and the configuration classes that read them, are gone --
 * deleted, not doubled (see {@code 115-07-SUMMARY.md} / {@code 115-08-PLAN.md} for the record).
 * Seven of the eight permissions below are migrated: their javadoc names the real call site,
 * either a {@code @PreAuthorize} on a controller method or, for
 * {@code SIADAP_CCA_CONSULTARESTADO}, a direct {@code IgrpAuthorizationService.checkPermission}
 * read inside its query handler. {@code PAA_PERIODOSUBMISSAO_GERARFORMULARIOS}, declared ahead
 * of the Phase 119 form generator with no call site yet, now has two -- both read endpoints of
 * {@code PaaSubmissionPeriodController} added in Phase 119 plan 05
 * ({@code getPeriodGenerationSummaries}, {@code getPeriodGeneration}).
 */
public class AppPermissions {

    // SIADAP

    /** Enforced by this permission itself, via @PreAuthorize on
     *  ComplianceController#assignMeritRating (Phase 115/AUT-04) -- the previous
     *  configured-employee-ids allow-list class was eliminated, not doubled. */
    @IgrpPermission(name = "siadap.mencaoMerito.atribuir",
            description = "Atribuir ou corrigir a menção de mérito de uma avaliação SIADAP")
    public static String SIADAP_MENCAOMERITO_ATRIBUIR = "siadap.mencaoMerito.atribuir";

    /** Enforced by this permission itself, via @PreAuthorize on
     *  ComplianceController#closeEvaluations (Phase 115/AUT-04) -- the previous
     *  configured-employee-ids allow-list class was eliminated, not doubled. */
    @IgrpPermission(name = "siadap.avaliacoes.fecharEmLote",
            description = "Fechar avaliações SIADAP em lote")
    public static String SIADAP_AVALIACOES_FECHAREMLOTE = "siadap.avaliacoes.fecharEmLote";

    /** Enforced by this permission itself, read directly via IgrpAuthorizationService inside
     *  GetSiadapCcaStatusQueryHandler (Phase 115/AUT-04) -- the previous configured-employee-ids
     *  allow-list class was eliminated, not doubled. Deliberately NOT enforced via
     *  @PreAuthorize on the controller: see the handler's javadoc, point (d), for why this is
     *  the one permission of seven read inside a handler instead of guarding a controller
     *  method. */
    @IgrpPermission(name = "siadap.cca.consultarEstado",
            description = "Consultar o estado do Conselho Coordenador da Avaliação")
    public static String SIADAP_CCA_CONSULTARESTADO = "siadap.cca.consultarEstado";

    /** Enforced by this permission itself, via @PreAuthorize on
     *  ComplianceController#openSelfEvaluationPhase (Phase 115/AUT-04) -- the previous
     *  configured-employee-ids allow-list class was eliminated, not doubled. */
    @IgrpPermission(name = "siadap.autoavaliacao.abrir",
            description = "Abrir manualmente a fase de autoavaliação de uma avaliação SIADAP")
    public static String SIADAP_AUTOAVALIACAO_ABRIR = "siadap.autoavaliacao.abrir";

    // PAA -- PERÍODOS DE SUBMISSÃO

    /** Enforced by this permission itself, via @PreAuthorize on PaaSubmissionPeriodController
     *  (Phase 115/AUT-04) -- the previous hasRole("RH") check was eliminated, not doubled. */
    @IgrpPermission(name = "paa.periodoSubmissao.criar",
            description = "Criar um período de submissão")
    public static String PAA_PERIODOSUBMISSAO_CRIAR = "paa.periodoSubmissao.criar";

    /** Enforced by this permission itself, via @PreAuthorize on PaaSubmissionPeriodController
     *  (Phase 115/AUT-04) -- the previous hasRole("RH") check was eliminated, not doubled. */
    @IgrpPermission(name = "paa.periodoSubmissao.fechar",
            description = "Fechar um período de submissão")
    public static String PAA_PERIODOSUBMISSAO_FECHAR = "paa.periodoSubmissao.fechar";

    /** Enforced by this permission itself, via @PreAuthorize on
     *  PaaSubmissionPeriodController#getPeriodGenerationSummaries and
     *  #getPeriodGeneration (Phase 119, plan 05) -- declared ahead of the Phase 119 form
     *  generator with no call site, now has two: reading who can mandate generation is weaker
     *  than mandating it, so the two read endpoints reuse this permission (D-25, 119-05-PLAN.md)
     *  instead of a permission of their own. */
    @IgrpPermission(name = "paa.periodoSubmissao.gerarFormularios",
            description = "Gerar os formulários de um período de submissão para os responsáveis")
    public static String PAA_PERIODOSUBMISSAO_GERARFORMULARIOS = "paa.periodoSubmissao.gerarFormularios";

    /** Enforced by this permission itself, via @PreAuthorize on
     *  PaaSubmissionPeriodController#revertPeriodGeneration (Phase 120, plan 03). Declared as
     *  its own permission, not a reuse of PAA_PERIODOSUBMISSAO_GERARFORMULARIOS -- unlike the
     *  two read endpoints above, this one deletes data. Reading the generation report is
     *  strictly weaker than mandating generation (D-25 above); deleting what was generated is
     *  strictly stronger and irreversible. Reusing gerarFormularios would silently widen, to
     *  everyone who can currently only view the report, the power to destroy evaluations --
     *  nobody would have decided that. See 120-03-PLAN.md, "A decisão de permissão, tomada e
     *  justificada". */
    @IgrpPermission(name = "paa.periodoSubmissao.desfazerGeracao",
            description = "Desfazer um lote de geração de formulários de um período de submissão")
    public static String PAA_PERIODOSUBMISSAO_DESFAZERGERACAO = "paa.periodoSubmissao.desfazerGeracao";

}
