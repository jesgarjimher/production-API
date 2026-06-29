package com.production.services;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.production.entities.Usuario;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class JwtService {

    // Clave secreta para firmar los tokens. ¡En producción esto iría en variables de entorno!
    private static final String SECRET_KEY = "TODO_clave";

    // Duración del token: 1 día (en milisegundos)
    private static final long EXPIRATION_TIME = 86_400_000;

    /**
     * Genera un Token JWT con los datos clave del usuario
     */
    public String generarToken(Usuario usuario) {
        Algorithm algoritmo = Algorithm.HMAC256(SECRET_KEY);

        return JWT.create()
                .withSubject(usuario.getNombre())
                .withClaim("id", usuario.getId())
                .withClaim("rol", usuario.getRol().getNombre()) // ¡Crucial para los permisos!
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .sign(algoritmo);
    }
}
