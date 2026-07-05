package com.fabricacion.services;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.stereotype.Service;

@Service
public class JwtValidationService {

    private static final String SECRET_KEY = "TODO_clave";

    public String extraerRol(String token) {
        Algorithm algoritmo = Algorithm.HMAC256(SECRET_KEY);
        DecodedJWT jwt = JWT.require(algoritmo)
                .build()
                .verify(token);
        return jwt.getClaim("rol").asString();
    }
}