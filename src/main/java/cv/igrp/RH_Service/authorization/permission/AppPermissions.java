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
 * seven permissions for the access-management side. Nothing enforces the correspondence today --
 * adding a permission in one place and not the other fails silently.
 * <p>
 * <b>These replace the employee-id allow-lists.</b> Until now the only authorization in this
 * service was two comma-separated lists of employee ids read from the environment
 * ({@code SIADAP_CCA_EMPLOYEE_IDS}, {@code SIADAP_SELF_EVALUATION_OPENER_EMPLOYEE_IDS}) plus a
 * role literal defaulting to {@code "RH"} that was never confirmed against the IAM realm. The
 * call sites that still read them are listed against each constant below and are NOT yet
 * migrated -- declaring a permission does not enforce it.
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

    /** No call site yet -- declared ahead of the period-opening form generator. */
    @IgrpPermission(name = "paa.periodoSubmissao.gerarFormularios",
            description = "Gerar os formulários de um período de submissão para os responsáveis")
    public static String PAA_PERIODOSUBMISSAO_GERARFORMULARIOS = "paa.periodoSubmissao.gerarFormularios";

}
