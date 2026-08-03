package com.fabricacion.controllers;

import com.fabricacion.entities.OrdenFabricacion;
import com.fabricacion.repositories.OrdenFabricacionRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import com.fabricacion.entities.EstadoOrden;

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
    // Listar órdenes:
    // Todo: GET http://localhost:8083/ordenes
    // Filtrado: GET http://localhost:8083/ordenes?estado=PENDIENTE
    @GetMapping
    public ResponseEntity<?> listarOrdenes(@RequestParam(value = "estado", required = false) String estado) {

        // Si el usuario NO pasa el parámetro '?estado=', devolvemos la lista completa
        if (estado == null || estado.isBlank()) {
            return ResponseEntity.ok(repository.findAll());
        }

        // Si SÍ pasa el parámetro, intentamos mapearlo al Enum para buscar
        try {
            EstadoOrden estadoEnum = EstadoOrden.valueOf(estado.toUpperCase());
            List<OrdenFabricacion> filtradas = repository.findByEstado(estadoEnum);
            return ResponseEntity.ok(filtradas);
        } catch (IllegalArgumentException e) {
            // Si mandan un estado inventado que no existe en el Enum (ej: ?estado=ROTO)
            return ResponseEntity.badRequest()
                    .body("Estado de filtro inválido. Use: PENDIENTE, EN_PROCESO, TERMINADA o CANCELADA");
        }
    }

    // Crear una nueva orden (Por defecto PENDIENTE): POST http://localhost:8083/ordenes
    @PostMapping
    public ResponseEntity<?> crearOrden(@RequestBody OrdenFabricacion orden) {
        orden.setEstado(EstadoOrden.PENDIENTE);
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

        //Transformamos el String de la URL a nuestro Enum de forma segura
        EstadoOrden estadoEnum;
        try {
            estadoEnum = EstadoOrden.valueOf(nuevoEstado.toUpperCase());
            // Si el usuario intenta saltar a CANCELADA por aquí, le obligamos a usar el endpoint específico
            if (estadoEnum == EstadoOrden.CANCELADA) {
                return ResponseEntity.badRequest().body("Para cancelar use el endpoint específico: /ordenes/{id}/cancelar");
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Estado inválido. Use: PENDIENTE, EN_PROCESO o TERMINADA");
        }
        String estadoUpper = nuevoEstado.toUpperCase();

        if (!estadoUpper.equals("PENDIENTE") && !estadoUpper.equals("EN_PROCESO") && !estadoUpper.equals("TERMINADA")) {
            return ResponseEntity.badRequest().body("Estado inválido. Use: PENDIENTE, EN_PROCESO o TERMINADA");
        }

        orden.setEstado(estadoEnum);
        repository.save(orden);

        //SI LA ORDEN SE TERMINA -> HACEMOS COMUNICACIÓN ENTRE MICROSERVICIOS
        //Comparamos directamente usando Enums
        if (orden.getEstado() == EstadoOrden.TERMINADA) {
            String tokenOriginal = request.getHeader("Authorization");

            try {
                String respuestaCatalog = webClient.put()
                        .uri(uriBuilder -> uriBuilder
                                .path("/productos/descontar")
                                .queryParam("codigo", orden.getCodigoProducto())
                                .queryParam("cantidad", orden.getCantidad())
                                .build())
                        .header("Authorization", tokenOriginal)
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();

                System.out.println("Respuesta del catalogo: " + respuestaCatalog);
            } catch (Exception e) {
                return ResponseEntity.status(500).body("Orden guardada pero falló la comunicación con catálogo: " + e.getMessage());
            }
        }
        return ResponseEntity.ok(orden);
    }

    // PUT http://localhost:8083/ordenes/1/cancelar
    @PutMapping("/{id}/cancelar")
    public ResponseEntity<?> cancelarOrden(@PathVariable("id") Long id, HttpServletRequest request) {

        //CONTROL DE SEGURIDAD: Recuperamos el rol inyectado por el JwtInterceptor
        String rol = (String) request.getAttribute("userRol");

        if (!"responsable_calidad".equals(rol)) {
            return ResponseEntity.status(403)
                    .body("Acceso prohibido: Solo el 'responsable_calidad' puede cancelar órdenes.");
        }

        // --- Continuamos con lógica de negocio ---
        Optional<OrdenFabricacion> opcional = repository.findById(id);
        if (opcional.isEmpty()) {
            return ResponseEntity.status(404)
                    .body("Error: La orden de fabricación con ID " + id + " no existe.");
        }

        OrdenFabricacion orden = opcional.get();

        // COMPARACIÓN SEGURA CON ENUMS
        if (orden.getEstado() == EstadoOrden.TERMINADA) {
            return ResponseEntity.badRequest()
                    .body("Error: No se puede cancelar una orden que ya está TERMINADA.");
        }

        if (orden.getEstado() == EstadoOrden.CANCELADA) {
            return ResponseEntity.badRequest()
                    .body("Aviso: Esta orden ya fue cancelada previamente.");
        }

        // Asignamos el Enum directamente
        orden.setEstado(EstadoOrden.CANCELADA);
        repository.save(orden);

        return ResponseEntity.ok("La orden ID " + id + " ha sido CANCELADA correctamente.");
    }

    // PUT http://localhost:8083/ordenes/{id}/avanzar-stock?cantidadProducida=20
    @PutMapping("/{id}/avanzar-stock")
    public ResponseEntity<?> avanzarStockParcial(
            @PathVariable("id") Long id,
            @RequestParam("cantidadProducida") int cantidadProducida,
            HttpServletRequest request) {

        if (cantidadProducida <= 0) {
            return ResponseEntity.badRequest().body("La cantidad producida debe ser mayor a 0.");
        }

        Optional<OrdenFabricacion> opcional = repository.findById(id);
        if (opcional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        OrdenFabricacion orden = opcional.get();

        if (orden.getEstado() != EstadoOrden.EN_PROCESO) {
            return ResponseEntity.badRequest().body("Solo se puede sumar stock parcial a órdenes que estén EN_PROCESO.");
        }

        // 1. Calculamos cuánto llevaríamos en total con este nuevo lote
        int actualmenteProducido = orden.getCantidadProducida();
        int nuevoAcumulado = actualmenteProducido + cantidadProducida;
        int pendiente = orden.getCantidad() - actualmenteProducido;

        // 2. VALIDACIÓN: No permitir pasarse del total restante
        if (nuevoAcumulado > orden.getCantidad()) {
            return ResponseEntity.badRequest()
                    .body("Error: Intentas añadir " + cantidadProducida + " unidades, pero solo quedan "
                            + pendiente + " unidades pendientes para completar esta orden (" + actualmenteProducido + "/" + orden.getCantidad() + ").");
        }

        // 3. COMUNICACIÓN CON PRODUCTION-SERVICE (Sumar al stock real en el catálogo)
        String tokenOriginal = request.getHeader("Authorization");
        try {
            String respuestaCatalog = webClient.put()
                    .uri(uriBuilder -> uriBuilder
                            .path("/productos/descontar")
                            .queryParam("codigo", orden.getCodigoProducto())
                            .queryParam("cantidad", cantidadProducida)
                            .build())
                    .header("Authorization", tokenOriginal)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            System.out.println("Respuesta del catálogo (Stock parcial): " + respuestaCatalog);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error al comunicar el stock al catálogo: " + e.getMessage());
        }

        // 4. GUARDAR EL NUEVO ACUMULADO
        orden.setCantidadProducida(nuevoAcumulado);

        // 5. SI SE ALCANZA EL TOTAL -> CAMBIAR A TERMINADA
        if (nuevoAcumulado >= orden.getCantidad()) {
            orden.setEstado(EstadoOrden.TERMINADA);
            repository.save(orden);
            return ResponseEntity.ok("¡Lote de " + cantidadProducida + " unidades añadido! La orden ha alcanzado el 100% ("
                    + nuevoAcumulado + "/" + orden.getCantidad() + ") y se ha marcado como TERMINADA.");
        }

        // Si aún quedan unidades pendientes:
        repository.save(orden);
        return ResponseEntity.ok("Lote de " + cantidadProducida + " unidades registrado. Avance actual: "
                + nuevoAcumulado + " de " + orden.getCantidad() + " unidades.");
    }
}
