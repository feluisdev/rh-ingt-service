package cv.igrp.RH_Service.shared.infrastructure.security;

import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.repository.ColabsFuncionarioEntityRepository;
import cv.igrp.RH_Service.shared.infrastructure.persistence.entity.IAMUserProfileEntity;
import cv.igrp.RH_Service.shared.infrastructure.persistence.repository.IAMUserProfileEntityRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class IAMUserProfileSyncFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(IAMUserProfileSyncFilter.class);

    private final IAMUserProfileEntityRepository iamProfileRepository;
    private final ColabsFuncionarioEntityRepository funcionarioRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth instanceof JwtAuthenticationToken jwtAuth) {
            Jwt jwt = jwtAuth.getToken();
            String sub = jwt.getSubject();
            try {
                var existing = iamProfileRepository.findBySub(sub);
                if (existing.isEmpty()) {
                    LOGGER.debug("Creating IAM user profile. sub={}", sub);
                    iamProfileRepository.save(buildProfile(jwt));
                } else {
                    buildUpdatedProfile(existing.get(), jwt).ifPresent(updated -> {
                        LOGGER.debug("Updating IAM user profile. sub={}", sub);
                        iamProfileRepository.save(updated);
                    });
                }
            } catch (Exception e) {
                LOGGER.warn("Failed to sync IAM user profile. sub={}, error={}", sub, e.getMessage(), e);
            }
        }

        chain.doFilter(request, response);
    }

    private IAMUserProfileEntity buildProfile(Jwt jwt) {
        String sub = jwt.getSubject();
        String username = resolveUsername(jwt);

        if (!StringUtils.hasText(username)) {
            throw new IllegalStateException("Could not determine username from JWT claims. sub=" + sub);
        }

        String email = jwt.getClaimAsString("email");
        UUID funcionarioId = resolveByEmail(email);

        var entity = new IAMUserProfileEntity();
        entity.setId(UUID.fromString(sub));
        entity.setSub(sub);
        entity.setUsername(username);
        entity.setEmail(email);
        entity.setFirstName(jwt.getClaimAsString("given_name"));
        entity.setLastName(jwt.getClaimAsString("family_name"));
        entity.setFullName(buildFullName(jwt.getClaimAsString("given_name"), jwt.getClaimAsString("family_name")));
        entity.setFuncionarioId(funcionarioId);
        return entity;
    }

    private java.util.Optional<IAMUserProfileEntity> buildUpdatedProfile(IAMUserProfileEntity existing, Jwt jwt) {
        String username = resolveUsername(jwt);
        String email = jwt.getClaimAsString("email");
        String firstName = jwt.getClaimAsString("given_name");
        String lastName = jwt.getClaimAsString("family_name");

        boolean changed = (StringUtils.hasText(username) && !username.equals(existing.getUsername()))
                || !Objects.equals(email, existing.getEmail())
                || !Objects.equals(firstName, existing.getFirstName())
                || !Objects.equals(lastName, existing.getLastName());

        if (!changed) return java.util.Optional.empty();

        existing.setUsername(StringUtils.hasText(username) ? username : existing.getUsername());
        existing.setEmail(email);
        existing.setFirstName(firstName);
        existing.setLastName(lastName);
        existing.setFullName(buildFullName(firstName, lastName));
        if (existing.getFuncionarioId() == null && StringUtils.hasText(email)) {
            existing.setFuncionarioId(resolveByEmail(email));
        }
        return java.util.Optional.of(existing);
    }

    private UUID resolveByEmail(String email) {
        if (!StringUtils.hasText(email)) return null;
        return funcionarioRepository.findByEmailIgnoreCase(email)
                .map(f -> f.getId())
                .orElse(null);
    }

    private String resolveUsername(Jwt jwt) {
        String preferred = jwt.getClaimAsString("preferred_username");
        return StringUtils.hasText(preferred) ? preferred : jwt.getClaimAsString("email");
    }

    private String buildFullName(String firstName, String lastName) {
        if (!StringUtils.hasText(firstName) && !StringUtils.hasText(lastName)) return null;
        return (StringUtils.hasText(firstName) ? firstName : "") +
               (StringUtils.hasText(lastName) ? " " + lastName : "");
    }
}
