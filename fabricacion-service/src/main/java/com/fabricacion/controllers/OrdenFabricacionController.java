package com.fabricacion.controllers;

import com.fabricacion.entities.OrdenFabricacion;
import com.fabricacion.repositories.OrdenFabricacionRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/ordenes")
public class OrdenFabricacionController {

    private final OrdenFabricacionRepository repository;

    public OrdenFabricacionController(OrdenFabricacionRepository repository) {
        this.repository = repository;
    }

    // Listar todas las órdenes: GET http://localhost:8083/ordenes
    @GetMapping
    public List<OrdenFabricacion> listarOrdenes() {
        return repository.findAll();
    }

    // Crear una nueva orden (Por defecto PENDIENTE): POST http://localhost:8083/ordenes
    @PostMapping
    public ResponseEntity<?> crearOrden(@RequestBody OrdenFabricacion orden) {
        orden.setEstado("PENDIENTE");
        OrdenFabricacion nueva = repository.save(orden);
        return ResponseEntity.ok(nueva);
    }

    // Cambiar estado (Iniciar o Terminar): PUT http://localhost:8083/ordenes/{id}/estado?nuevoEstado=EN_PROCESO
    @PutMapping("/{id}/estado")
    public ResponseEntity<?> actualizarEstado(@PathVariable("id") Long id, @RequestParam("nuevoEstado") String nuevoEstado) {
        Optional<OrdenFabricacion> opcional = repository.findById(id);
        if (opcional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        OrdenFabricacion orden = opcional.get();
        String estadoUpper = nuevoEstado.toUpperCase();

        if (!estadoUpper.equals("PENDIENTE") && !estadoUpper.equals("EN_PROCESO") && !estadoUpper.equals("TERMINADA")) {
            return ResponseEntity.badRequest().body("Estado inválido. Use: PENDIENTE, EN_PROCESO o TERMINADA");
        }

        orden.setEstado(estadoUpper);
        repository.save(orden);
        return ResponseEntity.ok(orden);
    }
}
