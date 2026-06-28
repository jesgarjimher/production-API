package com.production.services;

import com.production.entities.Rol;
import com.production.entities.Usuario;
import com.production.repositories.RolRepository;
import com.production.repositories.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;

    // Inyección de dependencias por constructor
    public AuthService(UsuarioRepository usuarioRepository, RolRepository rolRepository) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
    }

    /**
     * Registra un nuevo usuario asignándole un rol por defecto ('trabajador')
     */
    public Usuario registrarUsuario(String nombre, String password) {
        // 1. Validar si el usuario ya existe
        if (usuarioRepository.existsByNombre(nombre)) {
            throw new RuntimeException("El nombre de usuario ya está en uso.");
        }

        // 2. Buscar el rol 'trabajador' en la base de datos
        Rol rolPorDefecto = rolRepository.findByNombre("trabajador")
                .orElseThrow(() -> new RuntimeException("Error: El rol 'trabajador' no existe en el sistema."));

        // 3. Crear la nueva entidad Usuario
        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setNombre(nombre);

        // NOTA: Guardamos la contraseña en texto plano de momento para probar el flujo.
        // En el siguiente paso implementaremos el cifrado BCrypt por seguridad.
        nuevoUsuario.setPassword(password);
        nuevoUsuario.setRol(rolPorDefecto);

        // 4. Guardar en la base de datos a través del repositorio
        return usuarioRepository.save(nuevoUsuario);
    }
}
