package com.app_odontologia.diplomado_final.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;

import java.util.List;
import java.util.Locale;

public class CustomRequestCache extends HttpSessionRequestCache {

    // Patrónes que NO queremos almacenar como "saved request"
    private static final List<String> IGNORED_PREFIXES = List.of(
            "/.well-known/appspecific",
            "/.well-known/",
            "/favicon.ico",
            "/default-ui.css",
            "/assets/",
            "/static/",
            "/webjars/",
            "/robots.txt"
    );

    @Override
    public void saveRequest(HttpServletRequest request, HttpServletResponse response) {
        if (shouldIgnore(request)) {
            // no guardamos esta petición
            return;
        }
        // caso contrario delegamos a la implementación estándar
        super.saveRequest(request, response);
    }

    private boolean shouldIgnore(HttpServletRequest request) {
        String uri = request.getRequestURI();
        if (uri == null) return true;

        String lower = uri.toLowerCase(Locale.ROOT);

        // 1) prefijos ignorados (path-based)
        for (String p : IGNORED_PREFIXES) {
            if (lower.startsWith(p)) {
                return true;
            }
        }

        // 2) si no es una petición navegacional (accept header)
        String accept = request.getHeader("Accept");
        if (accept != null && !accept.contains("text/html")) {
            // XHR / Fetch / API calls normalmente no tienen `Accept: text/html`
            return true;
        }

        // 3) IGNORAR peticiones AJAX/Fetch con header X-Requested-With (opcional)
        String xr = request.getHeader("X-Requested-With");
        if (xr != null && xr.equalsIgnoreCase("XMLHttpRequest")) {
            return true;
        }

        // 4) si el método no es GET tampoco lo guardamos
        if (!"GET".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // ningún criterio de ignorar matcheó → guardamos la request
        return false;
    }

    @Override
    public SavedRequest getRequest(HttpServletRequest request, HttpServletResponse response) {
        // Comportamiento por defecto
        return super.getRequest(request, response);
    }
}
