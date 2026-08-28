package cv.igrp.RH_Service.shared.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Transporta a mensagem de recusa em português de uma ação guardada por
 * {@code @PreAuthorize}, declarada ao lado da própria guarda.
 *
 * <h2>Porquê esta anotação existe</h2>
 *
 * O {@code @PreAuthorize} recusa <b>antes</b> de o método correr, lançando
 * {@link org.springframework.security.access.AccessDeniedException} (ou a sua subclasse
 * {@code AuthorizationDeniedException}) — e essa exceção não carrega mensagem nenhuma. Antes
 * desta fase, a mensagem em português vivia dentro do handler, escrita à mão em cada ponto de
 * decisão; com a guarda a subir para o controller, esse texto desaparecia com ela.
 * <p>
 * A mensagem fica declarada <b>onde o leitor a procura</b> — imediatamente ao lado do
 * {@code @PreAuthorize} que a provoca — em vez de num mapa central que ninguém abre quando altera
 * um endpoint. Quem acrescenta um {@code @PreAuthorize} sem {@code @DenialMessage} recebe a
 * mensagem genérica; não é erro, é omissão degradada com segurança (ver
 * {@code cv.igrp.RH_Service.shared.domain.exceptions.GlobalExceptionHandler}, que lê esta
 * anotação a partir do método recusado).
 *
 * <h2>Uso</h2>
 *
 * <pre>{@code
 * @PreAuthorize("@igrpAuthorization.checkPermission(T(Permission).SIADAP_MENCAOMERITO_ATRIBUIR)")
 * @DenialMessage("Apenas um membro do Conselho Coordenador da Avaliação (CCA) pode executar esta ação")
 * public void atribuirMencaoMerito(...) { ... }
 * }</pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface DenialMessage {

  /**
   * Mensagem em português, pronta a sair no {@code title} do {@code ProblemDetail} da recusa.
   * Não deve nomear configuração (propriedades, variáveis de ambiente, listas de identificadores)
   * — é prosa de negócio, visível ao chamador não autorizado.
   */
  String value();
}
