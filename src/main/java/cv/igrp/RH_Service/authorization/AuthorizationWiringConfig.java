package cv.igrp.RH_Service.authorization;

import cv.igrp.framework.auth.core.autoconfig.AuthorizationSyncRunner;
import cv.igrp.framework.auth.core.security.IgrpAuthorizationService;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Liga explicitamente, neste serviço, as três classes do jar {@code core-spring-boot} que a
 * autoconfiguração declarada em
 * {@code META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports} não
 * regista. Essa autoconfiguração aponta apenas para
 * {@code cv.igrp.framework.auth.core.autoconfig.AutoConfiguration}, que declara um único bean
 * ({@code igrpApiClient}) — nada mais do jar entra no contexto Spring por conta própria.
 *
 * <p><b>O que cada import evita, e o que falha em silêncio sem ele:</b>
 *
 * <ul>
 *   <li>{@code cv.igrp.framework.auth.core.config.MethodSecurityConfig} — instala o
 *       {@code MethodSecurityConfig$IgrpTypeLocator}, que regista os prefixos de importação SpEL
 *       {@code cv.igrp.framework.auth.generated} e
 *       {@code cv.igrp.framework.auth.generated.PermissionsRegistry}. Sem esta classe,
 *       {@code T(Permission)} não resolve dentro de uma expressão {@code @PreAuthorize} — falha
 *       com {@code SpelEvaluationException} apenas no primeiro pedido que a avalia, nunca à
 *       compilação. <b>Usa-se o nome completamente qualificado no {@code @Import}</b>: este
 *       projeto já tem uma classe homónima em
 *       {@code cv.igrp.RH_Service.shared.security.MethodSecurityConfig} — a que traz o
 *       {@code @EnableMethodSecurity} do projeto — e as duas não podem ser importadas por nome
 *       simples no mesmo ficheiro. Deliberadamente <b>não se importa</b>
 *       {@code MethodSecurityEnableConfig}, a classe irmã do jar que também declara
 *       {@code @EnableMethodSecurity}: duplicá-la arriscaria dois advisors de método concorrentes
 *       sobre o mesmo proxy AOP.
 *   <li>{@code cv.igrp.framework.auth.core.security.IgrpAuthorizationService} — fornece o bean
 *       {@code igrpAuthorization} que {@code @PreAuthorize("@igrpAuthorization.checkPermission(...)")}
 *       invoca. Sem esta classe não existe esse bean e o SpEL falha a resolvê-lo, não a
 *       permissão em si.
 *   <li>{@code cv.igrp.framework.auth.core.autoconfig.AuthorizationSyncRunner} — envia as sete
 *       permissões declaradas em {@code AppPermissions} para a plataforma de gestão de acessos no
 *       arranque. Sem esta classe nenhuma sincronização acontece, e o {@code AUT-05} fica por
 *       medir em silêncio em vez de ficar registado como limite nomeado.
 * </ul>
 *
 * <p><b>Escape a não descobrir por acidente.</b>
 * {@link IgrpAuthorizationService#checkPermission} devolve {@code true} a qualquer chamador cuja
 * autoridade inclua {@link IgrpAuthorizationService#SUPER_ADMIN_AUTHORITY}
 * ({@code IGRP_SUPER_ADMIN}), contornando as sete permissões declaradas nesta fase. É
 * comportamento do framework, aceite por decisão registada no {@code 115-CONTEXT.md} (T-115-02).
 *
 * <p><b>Limite herdado, não introduzido aqui.</b> O
 * {@code @EnableMethodSecurity} deste serviço, em
 * {@code cv.igrp.RH_Service.shared.security.MethodSecurityConfig}, é <b>condicional</b>
 * ({@code MethodSecurityCondition}) e não é instalado quando {@code app.security.enabled=false}
 * no perfil {@code development}. Nesse caso nenhum {@code @PreAuthorize} desta fase — nem os das
 * ondas seguintes — chega a ser avaliado. Não é uma regressão desta classe, é comportamento já
 * existente do serviço (T-115-03), mas quem liga este wiring tem de o saber.
 */
@Configuration
@Import({
    cv.igrp.framework.auth.core.config.MethodSecurityConfig.class,
    IgrpAuthorizationService.class,
    AuthorizationSyncRunner.class
})
public class AuthorizationWiringConfig {
}
