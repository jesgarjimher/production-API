package com.fabricacion.controllers;

import com.fabricacion.entities.OrdenFabricacion;
import com.fabricacion.repositories.OrdenFabricacionRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/ordenes")
public class OrdenFabricacionController {

    private final OrdenFabricacionRepository repository;
    private final WebClient webClient; //inyecta el cliente HTTP

    public OrdenFabricacionController(OrdenFabricacionRepository repository, WebClient webClient) {
        this.repository = repository;
        this.webClient = webClient;
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
    public ResponseEntity<?> actualizarEstado(
            @PathVariable("id") Long id,
            @RequestParam("nuevoEstado") String nuevoEstado,
            HttpServletRequest request) { //se pide el request para capturar el JWT original

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
        //SI LA ORDEN SE TERMINA -> HACEMOS COMUNICACIÓN ENTRE MICROSERVICIOS
        if ("TERMINADA".equals(estadoUpper)) {
            // Extraemos el token original que traía la petición del operario
            String tokenOriginal = request.getHeader("Authorization");

            try {
                // Llamada HTTP interna hacia production-service reenviando el Token JWT
                String respuestaCatalog = webClient.put()
                        .uri(uriBuilder -> uriBuilder
                                .path("/productos/descontar")
                                .queryParam("codigo", orden.getCodigoProducto())
                                .queryParam("cantidad", orden.getCantidad())
                                .build())
                        .header("Authorization", tokenOriginal) // reenviar el token
                        .retrieve()
                        .bodyToMono(String.class)
                        .block(); // .block() hace la llamada síncrona para esperar el resultado

                System.out.println("Respuesta del catalogo: " + respuestaCatalog);
            } catch (Exception e) {
                // Si la comunicación falla o producción devuelve un 401/403, capturamos el error
                return ResponseEntity.status(500).body("Orden guardada pero fallo la comunicacion con catalogo: " + e.getMessage());
            }
        }
            return ResponseEntity.ok(orden);
    }
}
