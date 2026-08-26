package cv.igrp.RH_Service.shared.security;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * Guards {@link MethodSecurityConfig}, which declares the project's single method-security
 * enabling annotation. Matches (method security stays installed) in every case except the one
 * where {@link SecurityMode#isSecurityDisabled} says security is off.
 */
class MethodSecurityCondition implements Condition {

  @Override
  public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
    return !SecurityMode.isSecurityDisabled(context.getEnvironment());
  }
}
