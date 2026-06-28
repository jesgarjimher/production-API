package com.production.controllers;

import com.production.entities.Usuario;
import com.production.services.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // Endpoint para registrar usuarios: POST http://localhost:8081/auth/register
    @PostMapping("/register")
    public ResponseEntity<?> registrar(@RequestBody Map<String, String> body) {
        try {
            String nombre = body.get("nombre");
            String password = body.get("password");

            if (nombre == null || password == null) {
                return ResponseEntity.badRequest().body("Faltan los campos 'nombre' o 'password'");
            }

            Usuario usuarioCreado = authService.registrarUsuario(nombre, password);
            return ResponseEntity.ok(usuarioCreado);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
