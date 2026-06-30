package com.production.controllers;

import com.production.entities.Usuario;
import com.production.services.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.production.services.JwtService;

import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;

    public AuthController(AuthService authService, JwtService jwtService) {
        this.authService = authService;
        this.jwtService = jwtService;

    }

    // Endpoint para registrar usuarios: POST http://localhost:8081/auth/register
    @PostMapping("/register")
    public ResponseEntity<?> registrar(@RequestHeader(value = "Authorization", required = false) String bearerToken,
                                       @RequestBody Map<String, String> body) {
        try {

            // 1. Validar si el cliente ha enviado el token en la cabecera
            if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
                return ResponseEntity.status(401).body("Acceso denegado: Se requiere un token válido.");
            }

            // 2. Extraer el token puro (quitando la palabra "Bearer ")
            String token = bearerToken.substring(7);

            // 3. Extraer el rol del token usando nuestro JwtService
            String rolDelSolicitante = jwtService.extraerRol(token);

            // 4. 🔥 RESTRICCIÓN DE SEGURIDAD: Solo el 'responsable_calidad' puede registrar
            if (!"responsable_calidad".equals(rolDelSolicitante)) {
                return ResponseEntity.status(403)
                        .body("Acceso prohibido: Solo los usuarios con rol 'responsable_calidad' pueden registrar personal.");
            }

            // 5. Si pasa el filtro, procedemos con el registro normal
            String nombre = body.get("nombre");
            String password = body.get("password");

            if (nombre == null || password == null) {
                return ResponseEntity.badRequest().body("Faltan los campos 'nombre' o 'password'");
            }

            Usuario usuarioCreado = authService.registrarUsuario(nombre, password);
            return ResponseEntity.ok(usuarioCreado);
        } catch (Exception e) {
            return ResponseEntity.status(401).body("Token inválido o vencido: " + e.getMessage());
        }
    }

    // Endpoint para Login: POST http://localhost:8081/auth/login
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        try {
            String nombre = body.get("nombre");
            String password = body.get("password");

            if (nombre == null || password == null) {
                return ResponseEntity.badRequest().body("Faltan los campos 'nombre' o 'password'");
            }

            Usuario usuarioLogueado = authService.login(nombre, password);

            //generar el toekn si todo ha ido bien
            String token = jwtService.generarToken((usuarioLogueado));

            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("token", token);
            respuesta.put("username", usuarioLogueado.getNombre());
            respuesta.put("rol", usuarioLogueado.getRol().getNombre());


            return ResponseEntity.ok(respuesta);
        } catch (RuntimeException e) {
            // Devolvemos un 401 Unauthorized si las credenciales fallan
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }
}
