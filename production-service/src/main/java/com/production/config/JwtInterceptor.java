package com.production.config;

import com.production.services.JwtValidationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class JwtInterceptor implements HandlerInterceptor {

    private final JwtValidationService jwtValidationService;

    public JwtInterceptor(JwtValidationService jwtValidationService) {
        this.jwtValidationService = jwtValidationService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1. Si la petición es un GET (para ver productos), permitimos el paso a cualquier operario logueado
        // Pero primero comprobamos si viene el token de autenticación obligatorio para entrar a la intranet.
        // Si la petición es un preflight de CORS (OPTIONS), la dejamos pasar sin validar token
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            return true;
        }
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.getWriter().write("Acceso denegado: Se requiere Token JWT corporativo.");
            return false; // Bloquea la petición
        }

        try {
            String token = authHeader.substring(7);
            String rol = jwtValidationService.extraerRol(token);

            // 2. 🔥 RESTRICCIÓN ADICIONAL: Si intentan crear productos (POST), exigimos rol responsable_calidad
            if ("POST".equalsIgnoreCase(request.getMethod())) {
                if (!"responsable_calidad".equals(rol)) {
                    response.setStatus(HttpStatus.FORBIDDEN.value());
                    response.getWriter().write("Acceso prohibido: Solo el 'responsable_calidad' puede modificar el catalogo.");
                    return false; // Bloquea la petición
                }
            }

            // Si pasa los filtros, añadimos el rol al request por si hiciera falta en el controlador
            request.setAttribute("userRol", rol);
            return true; // Da luz verde para continuar hacia el Controller

        } catch (Exception e) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.getWriter().write("Token invalido o caducado: " + e.getMessage());
            return false;
        }
    }
}