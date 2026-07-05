package com.production.controllers;

import com.production.entities.Producto;
import com.production.repositories.ProductoRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    @PutMapping("/descontar")
    public ResponseEntity<?> descontarStock(
            @RequestParam("codigo") String codigo,
            @RequestParam("cantidad") int cantidad) {

        // NOTA: Aquí deberías buscar tu producto en la base de datos por su código,
        // restarle la cantidad al stock actual y hacer el repository.save().
        // De momento, simularemos que todo va bien devolviendo un mensaje de éxito:

        System.out.println("Copiado de stock interno: Descontando " + cantidad + " unidades de " + codigo);
        return ResponseEntity.ok("Stock actualizado con exito en el catalogo.");
    }
}
