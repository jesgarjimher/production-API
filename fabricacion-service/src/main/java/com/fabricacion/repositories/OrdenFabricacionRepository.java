package com.fabricacion.repositories;

import com.fabricacion.entities.EstadoOrden;
import com.fabricacion.entities.OrdenFabricacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrdenFabricacionRepository extends JpaRepository<OrdenFabricacion, Long> {
    List<OrdenFabricacion> findByEstado(EstadoOrden estado);
}
