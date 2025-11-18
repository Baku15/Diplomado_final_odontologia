package com.app_odontologia.diplomado_final.security;

import com.app_odontologia.diplomado_final.context.ClinicContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.oauth2.jwt.Jwt;

import java.io.IOException;

@Component // 🔥 Importante: así Spring podrá inyectarla en SecurityConfig
public class ClinicFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();

            if (auth != null && auth.getPrincipal() instanceof Jwt jwt) {
                Object claim = jwt.getClaims().get("clinic_id");
                if (claim instanceof Number c) {
                    ClinicContext.setClinicId(c.longValue());
                } else if (claim != null) {
                    try {
                        ClinicContext.setClinicId(Long.valueOf(claim.toString()));
                    } catch (NumberFormatException ignored) {}
                }
            }
            filterChain.doFilter(request, response);
        } finally {
            ClinicContext.clear(); // limpia el ThreadLocal al final de cada request
        }
    }
}
