package com.fabricacion.repositories;

import com.fabricacion.entities.OrdenFabricacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrdenFabricacionRepository extends JpaRepository<OrdenFabricacion, Long> {
}
