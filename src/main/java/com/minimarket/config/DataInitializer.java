package com.minimarket.config;

import com.minimarket.entity.Categoria;
import com.minimarket.entity.Producto;
import com.minimarket.entity.Rol;
import com.minimarket.entity.Usuario;
import com.minimarket.repository.CategoriaRepository;
import com.minimarket.repository.ProductoRepository;
import com.minimarket.repository.RolRepository;
import com.minimarket.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class DataInitializer implements ApplicationRunner {

    @Autowired
    private UsuarioRepository usuarioRepo;
    @Autowired
    private RolRepository rolRepo;
    @Autowired
    private ProductoRepository productoRepo;
    @Autowired
    private CategoriaRepository categoriaRepo;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (rolRepo.findByNombre("ADMIN").isEmpty()) {
            rolRepo.save(new Rol("ADMIN"));
            rolRepo.save(new Rol("USER"));
            System.out.println("Roles creados: ADMIN, USER");
        }

        if (usuarioRepo.findByUsername("admin").isEmpty()) {
            Usuario admin = new Usuario();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            Rol adminRol = rolRepo.findByNombre("ADMIN").orElse(null);
            Rol userRol = rolRepo.findByNombre("USER").orElse(null);
            admin.setRoles(Set.of(adminRol, userRol));
            usuarioRepo.save(admin);
            System.out.println("Usuario 'admin' creado con contraseña hasheada");
        }

        if (categoriaRepo.count() == 0) {
            Categoria bebidas = new Categoria();
            bebidas.setNombre("Bebidas");
            Categoria lacteos = new Categoria();
            lacteos.setNombre("Lácteos");
            Categoria panaderia = new Categoria();
            panaderia.setNombre("Panadería");
            categoriaRepo.save(bebidas);
            categoriaRepo.save(lacteos);
            categoriaRepo.save(panaderia);
            System.out.println("Categorías creadas: Bebidas, Lácteos, Panadería");
        }

        if (productoRepo.count() == 0) {
            for (int i = 1; i <= 50; i++) {
                Producto producto = new Producto();
                producto.setNombre("Producto " + i);
                producto.setPrecio(1000.0 + (i * 100));
                producto.setStock(20 + i);
                producto.setCategoria(categoriaRepo.findById((long) ((i % 3) + 1)).orElse(null));
                productoRepo.save(producto);
            }
            System.out.println("50 productos de prueba creados");
        }
    }
}
