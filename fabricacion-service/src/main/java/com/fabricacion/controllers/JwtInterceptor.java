package com.fabricacion.controllers;

import com.fabricacion.services.JwtValidationService;
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
        // Si la petición es un preflight de CORS (OPTIONS), la dejamos pasar sin validar token
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            return true;
        }

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.getWriter().write("Acceso denegado: Falta token de autenticacion de planta.");
            return false;
        }

        try {
            String token = authHeader.substring(7);
            String rol = jwtValidationService.extraerRol(token);

            // Regla 1: Solo el responsable de calidad puede dar de alta nuevas órdenes
            if ("POST".equalsIgnoreCase(request.getMethod())) {
                if (!"responsable_calidad".equals(rol)) {
                    response.setStatus(HttpStatus.FORBIDDEN.value());
                    response.getWriter().write("Acceso prohibido: Solo el 'responsable_calidad' puede planificar ordenes.");
                    return false;
                }
            }

            // Regla 2: Para modificar estados (PUT) o consultar (GET), permitimos tanto a trabajadores como a responsables
            // Por tanto, si el token es válido y tiene uno de los roles autorizados, dejamos pasar.
            request.setAttribute("userRol", rol);
            return true;

        } catch (Exception e) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.getWriter().write("Token invalido o caducado en planta: " + e.getMessage());
            return false;
        }
    }
}