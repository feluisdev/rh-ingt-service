package cv.igrp.RH_Service.shared.infrastructure.security;

import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.repository.ColabsFuncionarioEntityRepository;
import cv.igrp.RH_Service.shared.domain.service.CurrentEmployeeResolver;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.infrastructure.persistence.repository.IAMUserProfileEntityRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.UUID;

@Component("currentEmployeeResolver")
@RequiredArgsConstructor
public class CurrentEmployeeResolverImpl implements CurrentEmployeeResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(CurrentEmployeeResolverImpl.class);
    private static final String DEV_EMPLOYEE_HEADER = "X-Employee-Id";

    private final IAMUserProfileEntityRepository iamProfileRepository;
    private final ColabsFuncionarioEntityRepository funcionarioRepository;
    private final Environment environment;

    @Override
    public FuncionarioId resolve() {
        if (isNonProduction()) {
            return resolveForDevelopment();
        }
        return resolveFromJwt();
    }

    private FuncionarioId resolveForDevelopment() {
        // Try X-Employee-Id debug header first
        HttpServletRequest request = getRequest();
        if (request != null) {
            String header = request.getHeader(DEV_EMPLOYEE_HEADER);
            if (StringUtils.hasText(header)) {
                try {
                    LOGGER.debug("Resolving employee from {} header: {}", DEV_EMPLOYEE_HEADER, header);
                    return FuncionarioId.from(UUID.fromString(header));
                } catch (IllegalArgumentException e) {
                    LOGGER.warn("Invalid UUID in {} header: {}", DEV_EMPLOYEE_HEADER, header);
                }
            }
        }
        // Fallback: first active employee in the DB (dev convenience)
        return funcionarioRepository.findAll().stream()
                .filter(f -> Boolean.TRUE.equals(f.getIsActive()))
                .findFirst()
                .map(f -> FuncionarioId.from(f.getId()))
                .orElseThrow(() -> IgrpResponseStatusException.notFound(
                        "Funcionário não encontrado para o utilizador autenticado"));
    }

    private FuncionarioId resolveFromJwt() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!(auth instanceof JwtAuthenticationToken jwtAuth)) {
            throw IgrpResponseStatusException.notFound("Funcionário não encontrado para o utilizador autenticado");
        }

        Jwt jwt = jwtAuth.getToken();
        String sub = jwt.getSubject();

        var iamProfile = iamProfileRepository.findBySub(sub)
                .orElseThrow(() -> IgrpResponseStatusException.notFound(
                        "Funcionário não encontrado para o utilizador autenticado"));

        if (iamProfile.getFuncionarioId() == null) {
            throw IgrpResponseStatusException.notFound("Funcionário não encontrado para o utilizador autenticado");
        }

        return FuncionarioId.from(iamProfile.getFuncionarioId());
    }

    private boolean isNonProduction() {
        return environment.matchesProfiles("development", "staging", "dev", "local");
    }

    private HttpServletRequest getRequest() {
        var attributes = RequestContextHolder.getRequestAttributes();
        return attributes instanceof ServletRequestAttributes servletAttributes
                ? servletAttributes.getRequest()
                : null;
    }
}
