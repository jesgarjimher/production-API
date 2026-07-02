package com.production.services;

import com.production.entities.Rol;
import com.production.entities.Usuario;
import com.production.repositories.RolRepository;
import com.production.repositories.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final RolRepository rolRepository;
    private final UsuarioRepository usuarioRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    // Inyectamos los repositorios y el encriptador
    public DataInitializer(RolRepository rolRepository,
                           UsuarioRepository usuarioRepository,
                           BCryptPasswordEncoder passwordEncoder) {
        this.rolRepository = rolRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        // 1. Asegurar que los roles existan (igual que antes)
        if (rolRepository.findByNombre("trabajador").isEmpty()) {
            Rol trabajador = new Rol();
            trabajador.setNombre("trabajador");
            rolRepository.save(trabajador);
            System.out.println("Rol 'trabajador' insertado.");
        }

        if (rolRepository.findByNombre("responsable_calidad").isEmpty()) {
            Rol responsable = new Rol();
            responsable.setNombre("responsable_calidad");
            rolRepository.save(responsable);
            System.out.println("Rol 'responsable_calidad' insertado.");
        }

        // 2. 🔥 CREAR AL JEFE POR DEFECTO
        // Si no hay ningún usuario llamado 'jefe_calidad', lo creamos de forma automática
        if (usuarioRepository.findByNombre("jefe_calidad").isEmpty()) {
            Rol rolResponsable = rolRepository.findByNombre("responsable_calidad")
                    .orElseThrow(() -> new RuntimeException("Error al inicializar: Rol no encontrado."));

            Usuario jefe = new Usuario();
            jefe.setNombre("jefe_calidad");
            // Encriptamos su clave de acceso
            jefe.setPassword(passwordEncoder.encode("jefe123"));
            jefe.setRol(rolResponsable);

            usuarioRepository.save(jefe);
            System.out.println("Usuario 'jefe_calidad' creado con éxito. Clave: jefe123");
        }
    }
}