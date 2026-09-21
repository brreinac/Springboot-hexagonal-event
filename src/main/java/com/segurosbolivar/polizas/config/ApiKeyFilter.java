package com.segurosbolivar.polizas.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ApiKeyFilter extends OncePerRequestFilter {

    private static final String HEADER_API_KEY = "x-api-key";

    private final ConfiguracionPolizas configuracionPolizas;
    private final ObjectMapper objectMapper;

    public ApiKeyFilter(ConfiguracionPolizas configuracionPolizas, ObjectMapper objectMapper) {
        this.configuracionPolizas = configuracionPolizas;
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return "/error".equals(request.getRequestURI());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String apiKeyRecibida = request.getHeader(HEADER_API_KEY);
        if (!configuracionPolizas.getApiKey().equals(apiKeyRecibida)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            objectMapper.writeValue(response.getOutputStream(), Map.of(
                    "fecha", Instant.now(),
                    "estado", HttpServletResponse.SC_UNAUTHORIZED,
                    "error", "Unauthorized",
                    "mensaje", "El header x-api-key es obligatorio o no es válido.",
                    "ruta", request.getRequestURI()
            ));
            return;
        }
        filterChain.doFilter(request, response);
    }
}
