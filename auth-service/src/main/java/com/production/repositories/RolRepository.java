package com.production.repositories;

import com.production.entities.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface RolRepository extends JpaRepository<Rol, Integer> {
    // Método personalizado para buscar un rol por su nombre (útil para inicializar)
    Optional<Rol> findByNombre(String nombre);
}