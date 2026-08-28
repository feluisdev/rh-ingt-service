package cv.igrp.RH_Service.authorization;

import cv.igrp.framework.auth.core.autoconfig.AuthorizationSyncRunner;
import cv.igrp.framework.auth.core.security.IgrpAuthorizationService;
import cv.igrp.framework.auth.core.security.IgrpMethodSecurityExpressionHandler;
import cv.igrp.platform.access.client.ApiClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;

/**
 * Liga manualmente, por declaração directa de beans, três peças do jar
 * {@code core-spring-boot} que nada regista por omissão neste serviço.
 *
 * <h2>Porquê beans directos, e não {@code @Import}</h2>
 *
 * O plano original desta tarefa (115-01) previa {@code @Import({MethodSecurityConfig.class,
 * IgrpAuthorizationService.class, AuthorizationSyncRunner.class})}. Medido nesta máquina que
 * isso é impossível com o par de versões actual:
 *
 * <ul>
 *   <li>Os jars do framework IGRP ({@code core-spring-boot-0.2.0-beta.8},
 *       {@code client-0.2.0-beta.12}) são bytecode {@code major version 70} (JDK 26) --
 *       confirmado por {@code javap -verbose}.</li>
 *   <li>O ASM embutido no Spring Framework 6.2.8 ({@code org.springframework.asm.Opcodes})
 *       só conhece constantes até {@code V25 = 69} -- confirmado por {@code javap -constants}
 *       sobre {@code spring-core-6.2.8.jar}. Falha por <b>uma</b> versão de bytecode.</li>
 *   <li>{@code @Import} de uma classe de configuração obriga o Spring a pedir ao ASM que
 *       <em>parseie</em> o ficheiro {@code .class} para descobrir os seus {@code @Bean}. Com
 *       um {@code major version 70} desconhecido do ASM, isso rebenta com
 *       {@code ClassFormatException: Unsupported class file major version 70} -- mesmo com a
 *       JVM 26 instalada e a correr, porque quem recusa não é a JVM, é o parser ASM.</li>
 *   <li>O mesmo acontecia sem {@code @Import} nenhum: o
 *       {@code META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports}
 *       do jar aponta para {@code cv.igrp.framework.auth.core.autoconfig.AutoConfiguration},
 *       também {@code major version 70} -- por isso essa autoconfiguração é excluída por nome
 *       em {@code application.properties} ({@code spring.autoconfigure.exclude}), nunca por
 *       referência a {@code .class}, que voltaria a forçar o ASM.</li>
 * </ul>
 *
 * <p>A JVM em si (JDK 26.0.2 nesta máquina) carrega e reflecte sobre bytecode 70 sem
 * problema -- é <em>só</em> o parser ASM do Spring que fica um passo atrás. Por isso a solução
 * é nunca pedir ao Spring para parsear estas classes: declaram-se os beans directamente nesta
 * {@code @Configuration}, que é código nosso (bytecode 67, compilado por este projeto), e o
 * Spring só precisa de <em>carregar</em> e <em>instanciar</em> as classes do jar -- o que a JVM
 * 26 faz sem qualquer intervenção do ASM.
 *
 * <p>Confirmado por {@code javap -c} sobre {@code MethodSecurityConfig.class} do jar: o único
 * bean que essa classe declara, {@code methodSecurityExpressionHandler()}, faz exactamente
 * {@code new IgrpMethodSecurityExpressionHandler()} -- nenhuma lógica adicional. Declarar o
 * mesmo bean aqui é, byte a byte, equivalente a importar a classe original.
 *
 * <h2>O que cada bean evita</h2>
 *
 * <ul>
 *   <li>{@code methodSecurityExpressionHandler} -- sem ele, o
 *       {@code MethodSecurityConfig$IgrpTypeLocator} nunca é instalado (é o construtor de
 *       {@link IgrpMethodSecurityExpressionHandler} que o regista, com
 *       {@code registerImport("cv.igrp.framework.auth.generated")} e
 *       {@code registerImport("cv.igrp.framework.auth.generated.PermissionsRegistry")}), e
 *       {@code T(Permission)} falha com {@code SpelEvaluationException} no primeiro pedido que
 *       avaliar a expressão -- nunca à compilação.</li>
 *   <li>{@code igrpAuthorization} -- sem este bean, com este nome exacto, não existe alvo para
 *       {@code @igrpAuthorization} dentro do SpEL e a expressão falha a resolver o bean.</li>
 *   <li>{@code authorizationSyncRunner} -- sem ele, nenhuma sincronização de permissões acontece
 *       no arranque, e o {@code AUT-05} fica por medir em silêncio em vez de ser um limite
 *       nomeado.</li>
 * </ul>
 *
 * <h2>Escape de super-admin -- registado para não ser descoberto por acidente</h2>
 *
 * {@link IgrpAuthorizationService#checkPermission} devolve {@code true} a qualquer chamador que
 * tenha a authority {@code IGRP_SUPER_ADMIN} ({@link IgrpAuthorizationService#SUPER_ADMIN_AUTHORITY}),
 * contornando as sete permissões declaradas nesta fase. É comportamento do framework, aceite por
 * decisão registada no {@code 115-CONTEXT.md} (T-115-02, disposição {@code accept}).
 *
 * <h2>Limite herdado -- {@code @EnableMethodSecurity} condicional</h2>
 *
 * O {@code @EnableMethodSecurity} deste projecto ({@code cv.igrp.RH_Service.shared.security})
 * é <b>condicional</b> ({@code @Conditional(MethodSecurityCondition.class)}), activo excepto
 * quando {@code app.security.enabled=false} no perfil {@code development}. Nesse caso, nenhum
 * {@code @PreAuthorize} desta fase -- nem de nenhuma outra -- é avaliado. Não é uma regressão
 * introduzida por este ficheiro (T-115-03, disposição {@code accept}, herdado da Fase 108), mas
 * quem lê esta classe tem de o saber antes de assumir que a guarda está sempre activa.
 */
@Configuration
public class AuthorizationWiringConfig {

  /**
   * Substitui o bean {@code igrpApiClient} que a autoconfiguração do jar (excluída por nome em
   * {@code application.properties}) normalmente forneceria. Usa a mesma propriedade e o mesmo
   * valor por omissão vazio que essa autoconfiguração usava, confirmado por {@code javap}.
   */
  @Bean(name = "igrpApiClient")
  public ApiClient igrpApiClient(@Value("${igrp.access.api.base-url:}") String baseUrl) {
    ApiClient client = new ApiClient();
    client.setBaseUrl(baseUrl);
    return client;
  }

  /**
   * O bean tem de se chamar exactamente {@code igrpAuthorization} -- é o nome que
   * {@code @PreAuthorize("@igrpAuthorization.checkPermission(...)")} resolve no SpEL. No jar,
   * este nome vem de {@code @Service("igrpAuthorization")} sobre {@link IgrpAuthorizationService};
   * aqui é o {@code name} explícito de {@link Bean} que faz o mesmo papel.
   */
  @Bean(name = "igrpAuthorization")
  public IgrpAuthorizationService igrpAuthorization() {
    return new IgrpAuthorizationService();
  }

  /**
   * Corre no arranque ({@code @PostConstruct}, dentro do próprio jar) e sincroniza as sete
   * permissões com a plataforma de gestão de acessos IGRP. Depende do bean
   * {@code igrpApiClient} declarado acima.
   */
  @Bean
  public AuthorizationSyncRunner authorizationSyncRunner(ApiClient igrpApiClient) {
    return new AuthorizationSyncRunner(igrpApiClient);
  }

  /**
   * Instala o {@code IgrpTypeLocator} que faz {@code T(Permission)} resolver sem nome de pacote
   * completo dentro de expressões SpEL avaliadas por {@code @PreAuthorize}. Byte a byte
   * equivalente ao bean {@code methodSecurityExpressionHandler()} de
   * {@code cv.igrp.framework.auth.core.config.MethodSecurityConfig} no jar -- ver o
   * javadoc da classe para a prova.
   */
  @Bean
  public MethodSecurityExpressionHandler methodSecurityExpressionHandler() {
    return new IgrpMethodSecurityExpressionHandler();
  }
}
