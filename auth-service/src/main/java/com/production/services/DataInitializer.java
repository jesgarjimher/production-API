package com.production.services;

import com.production.entities.Rol;
import com.production.repositories.RolRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final RolRepository rolRepository;

    // Inyección de dependencias por constructor
    public DataInitializer(RolRepository rolRepository) {
        this.rolRepository = rolRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // Verificamos si el rol 'trabajador' ya existe; si no, lo creamos
        if (rolRepository.findByNombre("trabajador").isEmpty()) {
            Rol trabajador = new Rol();
            trabajador.setNombre("trabajador");
            rolRepository.save(trabajador);
            System.out.println("🌱 Rol 'trabajador' insertado correctamente.");
        }

        // Verificamos si el rol 'responsable_calidad' ya existe; si no, lo creamos
        if (rolRepository.findByNombre("responsable_calidad").isEmpty()) {
            Rol responsable = new Rol();
            responsable.setNombre("responsable_calidad");
            rolRepository.save(responsable);
            System.out.println("🌱 Rol 'responsable_calidad' insertado correctamente.");
        }
    }
}
