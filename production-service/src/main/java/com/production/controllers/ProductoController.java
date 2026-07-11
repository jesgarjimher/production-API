package com.production.controllers;

import com.production.entities.Producto;
import com.production.repositories.ProductoRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;
import java.util.List;

@RestController
@RequestMapping("/productos")
public class ProductoController {

    private final ProductoRepository productoRepository;

    public ProductoController(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    // Listar todo el catálogo: GET http://localhost:8082/productos
    @GetMapping
    public List<Producto> listarProductos() {
        return productoRepository.findAll();
    }

    // Insertar un nuevo producto: POST http://localhost:8082/productos
    @PostMapping
    public ResponseEntity<?> crearProducto(@RequestBody Producto producto) {
        try {
            Producto nuevo = productoRepository.save(producto);
            return ResponseEntity.ok(nuevo);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al crear el producto: " + e.getMessage());
        }
    }

    // PUT http://localhost:8082/productos/descontar?codigo=PROD-001&cantidad=50
    @PutMapping("/descontar") // Mantenemos la ruta para que fabricacion-service no falle
    public ResponseEntity<?> incrementarStock(
            @RequestParam("codigo") String codigo,
            @RequestParam("cantidad") int cantidad) {

        // 1. Buscar el producto en MySQL
        Optional<Producto> opcional = productoRepository.findByCodigo(codigo);
        if (opcional.isEmpty()) {
            return ResponseEntity.status(404)
                    .body("Error: El producto '" + codigo + "' no existe en el catálogo.");
        }
        Producto producto = opcional.get();

        //SUMA AL STOCK
        int nuevoStock = producto.getStock() + cantidad;
        producto.setStock(nuevoStock);

        // 3. Guardar en DB
        productoRepository.save(producto);

        System.out.println("¡Orden completada! Se han sumado " + cantidad + " unidades a [" + codigo + "]. Nuevo stock total: " + nuevoStock);

        return ResponseEntity.ok("Stock incrementado con éxito. Unidades totales de " + producto.getNombre() + ": " + nuevoStock);
    }
}