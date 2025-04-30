package com.example.BrusnikaCoworking.config.jwt;

import com.example.BrusnikaCoworking.adapter.web.auth.dto.LogupUser;
import com.example.BrusnikaCoworking.config.jwt.dto.KeylockUserPrincipal;
import com.example.BrusnikaCoworking.config.jwt.dto.UserRole;
import com.example.BrusnikaCoworking.domain.user.UserEntity;
import com.example.BrusnikaCoworking.exception.ResourceException;
import com.example.BrusnikaCoworking.service.UserService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.PrematureJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Component
@RequiredArgsConstructor
public class KeylockTokenFilter extends OncePerRequestFilter {
    public static final String BEARER_PREFIX = "Bearer ";
    public static final String HEADER_NAME = "Authorization";
    private final UserService userService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader = request.getHeader(HEADER_NAME);

        // Пропускаем запросы без токена
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        String jwt = authHeader.substring(BEARER_PREFIX.length());

        try {
            // Декодируем токен без проверки подписи
            Claims claims = Jwts.parserBuilder()
                    .build()
                    .parseClaimsJwt(jwt.split("\\.")[0] + "." + jwt.split("\\.")[1] + ".")
                    .getBody();

            // Проверяем временные метки токена
            validateTokenTimestamps(claims);

            // Получаем полную структуру пользователя из токена
            Map<String, Object> userData = claims.get("user", Map.class);
            Map<String, Object> personData = (Map<String, Object>) userData.get("person");
//            List<Map<String, Object>> rolesData = (List<Map<String, Object>>) userData.get("roles");

//            // Создаем кастомный объект пользователя
//            KeylockUserPrincipal principal = createUserPrincipal(claims, personData, rolesData);
//
//            // Создаем authorities на основе ролей
//            List<GrantedAuthority> authorities = createAuthorities(rolesData);
//
//            // Создаем аутентификацию
//            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
//                    principal,
//                    null,
//                    authorities
//            );
//
//            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
//            SecurityContextHolder.getContext().setAuthentication(authToken);

            String username = (String) personData.get("email");
            UserEntity user;

            if (StringUtils.isNotEmpty(username) && SecurityContextHolder.getContext().getAuthentication() == null) {
                try {
                    user = userService.loadUserByUsername(username);

                } catch (ResourceException e) {
                    user = userService.createUser(new LogupUser((String) personData.get("email"),
                            (String) personData.get("title")));
                }
                SecurityContext context = SecurityContextHolder.createEmptyContext();

                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        user,
                        null,
                        user.getAuthorities()
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                context.setAuthentication(authToken);
                SecurityContextHolder.setContext(context);
            }
        } catch (ExpiredJwtException e) {
            SecurityContextHolder.clearContext();
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Срок действия токена истек");
            return;
        } catch (PrematureJwtException e) {
            SecurityContextHolder.clearContext();
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Токен еще не активен");
            return;
        } catch (Exception e) {
            SecurityContextHolder.clearContext();
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Неверный формат токена");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void validateTokenTimestamps(Claims claims) {
        Date now = new Date();

        // Проверка expiration time (exp)
        Date expiration = claims.getExpiration();
        if (expiration != null && expiration.before(now)) {
            throw new ExpiredJwtException(null, claims, "Token expired");
        }

        // Проверка issued at (iat) - токен не должен быть выдан в будущем
        Date issuedAt = claims.getIssuedAt();
        if (issuedAt != null && issuedAt.after(now)) {
            throw new PrematureJwtException(null, claims, "Token issued in future");
        }

        // Проверка auth_time (если есть)
        Date authTime = claims.get("auth_time", Date.class);
        if (authTime != null && authTime.after(now)) {
            throw new PrematureJwtException(null, claims, "Auth time in future");
        }
    }

    private KeylockUserPrincipal createUserPrincipal(Claims claims,
                                                     Map<String, Object> personData,
                                                     List<Map<String, Object>> rolesData) {
        KeylockUserPrincipal principal = new KeylockUserPrincipal();

        // Заполняем основные данные
        principal.setId(claims.getSubject());
        principal.setUsername((String) personData.get("id"));
        principal.setEmail((String) personData.get("email"));
        principal.setFullName((String) personData.get("title"));
        principal.setFirstName((String) personData.get("firstName"));
        principal.setLastName((String) personData.get("lastName"));
        principal.setMiddleName((String) personData.get("middleName"));

        // Заполняем данные о ролях
        List<UserRole> roles = rolesData.stream()
                .map(this::mapToUserRole)
                .collect(Collectors.toList());
        principal.setRoles(roles);

        return principal;
    }

    private UserRole mapToUserRole(Map<String, Object> roleData) {
        return new UserRole(
                (String) roleData.get("userType"),
                (String) roleData.get("id"),
                (String) roleData.get("qualification"),
                (String) roleData.get("instituteTitle"),
                (String) roleData.get("groupTitle"),
                (Integer) roleData.get("course"),
                (String) roleData.get("compensation"),
                (Boolean) roleData.get("isForeign")
        );
    }

    private List<GrantedAuthority> createAuthorities(List<Map<String, Object>> rolesData) {
        return rolesData.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.get("userType").toString().toUpperCase()))
                .collect(Collectors.toList());
    }
}