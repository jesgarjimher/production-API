package com.production.services;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.stereotype.Service;

@Service
public class JwtValidationService {

    // 🔥 DEBE SER EXACTAMENTE LA MISMA CLAVE QUE EN AUTH-SERVICE
    private static final String SECRET_KEY = "TODO_clave";

    /**
     * Valida el token y extrae el rol si todo es correcto.
     * Si el token ha caducado o está manipulado, lanzará una excepción.
     */
    public String extraerRol(String token) {
        Algorithm algoritmo = Algorithm.HMAC256(SECRET_KEY);

        DecodedJWT jwt = JWT.require(algoritmo)
                .build()
                .verify(token); // Aquí se valida la firma y la fecha de expiración

        return jwt.getClaim("rol").asString();
    }
}