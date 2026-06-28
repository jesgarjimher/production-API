package com.production.repositories;

import com.production.entities.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    // Método clave para el futuro Login: buscar al usuario por su nombre
    Optional<Usuario> findByNombre(String nombre);

    // Método para saber si un nombre de usuario ya está registrado
    boolean existsByNombre(String nombre);
}