# Desarrollo de Preguntas — Formato de Respuesta (Forma C)

**Asignatura:** Desarrollo Backend II  
**Experiencia:** 3 — Semana 8  
**Tema:** Implementación avanzada de documentación en microservicios con OpenAPI y HATEOAS  
**Estudiantes:** Catalina Cabezas, Andrea Rosero, Nicolas Cavieres
**Fecha:** 12 Julio 2026

---

## Índice

1. [Pregunta 1 — Diseño y documentación de endpoints con OpenAPI](#pregunta-1)
2. [Pregunta 2 — Configuración del entorno OpenAPI + HATEOAS](#pregunta-2)
3. [Pregunta 3 — Implementación de HATEOAS con enlaces dinámicos](#pregunta-3)
4. [Pregunta 4 — Generación y validación de documentación en Swagger UI](#pregunta-4)
5. [Pregunta 5 — Análisis y reflexión sobre resultados](#pregunta-5)
6. [Pregunta 6 — Documentación del proceso y subida a GitHub](#pregunta-6)

---

## Pregunta 1

**Diseña y documenta endpoints con OpenAPI (OAS) — 25 puntos**

### 1.1 Objetivo

Demostrar la capacidad de documentar todos los endpoints REST del sistema Minimarket utilizando el estándar OpenAPI 3.0, aplicando anotaciones como `@Operation`, `@ApiResponses`, `@Tag`, `@Schema`, `@SecurityRequirement` y `@Link` para generar una especificación completa y consumible desde Swagger UI, incluyendo paginación, validación de datos y esquema de seguridad JWT.

### 1.2 Desarrollo

#### Antes (código sin documentación OpenAPI)

Antes de implementar OpenAPI, los controladores del sistema Minimarket eran funcionales pero carecían de cualquier tipo de documentación auto-contenida. A continuación se muestra un ejemplo del estado original del `ProductoController`:

```java
// ProductoController — ANTES (sin OpenAPI, sin paginación)
@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    @Autowired
    private ProductoService productoService;

    @GetMapping
    public ResponseEntity<List<Producto>> listarProductos() {
        return ResponseEntity.ok(productoService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Producto> obtenerProductoPorId(@PathVariable Long id) {
        Producto producto = productoService.findById(id);
        if (producto == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(producto);
    }
}
```

Como se observa, no existía ninguna anotación de documentación, no existía paginación, se retornaban entidades crudas con datos sensibles, y no había validación de entrada.

#### Después (código con documentación OpenAPI completa)

Se documentaron 36 endpoints distribuidos en 8 controladores (7 de dominio + 1 de autenticación JWT). Cada controlador recibió una etiqueta descriptiva, cada método metadatos de operación, y se aplicó un esquema de seguridad `bearerAuth` para JWT. A continuación se muestra el mismo controlador después de la implementación:

```java
// ProductoController — DESPUÉS (con OpenAPI, paginación, DTOs, validación)
@RestController
@RequestMapping("/api/productos")
@Tag(name = "Productos", description = "Operaciones CRUD para la gestión de productos")
@SecurityRequirement(name = "bearerAuth")
public class ProductoController {

    @Autowired
    private ProductoService productoService;
    @Autowired
    private ProductoModelAssembler productoModelAssembler;

    @GetMapping
    @Operation(summary = "Listar todos los productos",
               description = "Retorna una lista paginada con todos los productos registrados")
    @ApiResponses({
        @ApiResponse(responseCode = "200",
                     description = "Lista de productos obtenida correctamente",
                     links = {
                         @Link(name = "self", operationId = "listarProductos")
                     })
    })
    public ResponseEntity<PagedModel<EntityModel<ProductoResponseDTO>>> listarProductos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "nombre") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Producto> productosPage = productoService.findAll(pageable);
        // ... construcción de PagedModel con first, prev, next, last
        return ResponseEntity.ok(pagedModel);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener un producto por ID",
               description = "Retorna un producto según su ID", operationId = "obtenerProductoPorId",
               responses = {
                   @ApiResponse(responseCode = "200", description = "Producto encontrado",
                       content = @Content(schema = @Schema(implementation = ProductoResponseDTO.class)),
                       links = {
                           @Link(name = "self", operationId = "obtenerProductoPorId"),
                           @Link(name = "allProductos", operationId = "listarProductos")
                       }),
                   @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    public ResponseEntity<EntityModel<ProductoResponseDTO>> obtenerProductoPorId(
            @Parameter(description = "ID del producto", required = true, example = "1")
            @PathVariable @Positive Long id) {
        Producto producto = productoService.findById(id);
        return ResponseEntity.ok(productoModelAssembler.toModel(producto));
    }

    @PostMapping
    @Operation(summary = "Crear un nuevo producto")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Producto creado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos")
    })
    public ResponseEntity<EntityModel<ProductoResponseDTO>> guardarProducto(
            @Valid @RequestBody Producto producto) {
        Producto nuevo = productoService.save(producto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(productoModelAssembler.toModel(nuevo));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un producto")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Producto eliminado correctamente"),
        @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    public ResponseEntity<EntityModel<Map<String, String>>> eliminarProducto(
            @Parameter(description = "ID del producto", required = true)
            @PathVariable @Positive Long id) {
        productoService.deleteById(id);
        EntityModel<Map<String, String>> response = EntityModel.of(
                Map.of("message", "Producto eliminado exitosamente"),
                linkTo(methodOn(ProductoController.class)
                    .listarProductos(0, 10, "nombre", "asc")).withRel("allProductos"),
                linkTo(methodOn(ProductoController.class)
                    .guardarProducto(null)).withRel("addProducto")
        );
        return ResponseEntity.ok(response);
    }
}
```

#### Controladores documentados (8 en total)

| Controlador              | Tag               | Endpoints                                                                                      |
| ------------------------ | ----------------- | ---------------------------------------------------------------------------------------------- |
| `ProductoController`     | Productos         | GET (paginado), GET/{id}, POST, PUT/{id}, DELETE/{id}, GET/{id}/categoria, POST/{id}/categoria |
| `CategoriaController`    | Categorías        | GET (paginado), GET/{id}, POST, PUT/{id}, DELETE/{id}                                          |
| `CarritoController`      | Carrito           | GET (paginado), GET/{id}, POST, PUT/{id}, DELETE/{id}                                          |
| `InventarioController`   | Inventario        | GET (paginado), GET/{id}, POST, PUT/{id}, DELETE/{id}                                          |
| `UsuarioController`      | Usuarios          | GET (paginado), GET/{id}, POST, PUT/{id}, DELETE/{id}                                          |
| `VentaController`        | Ventas            | GET (paginado), GET/{id}, POST                                                                 |
| `DetalleVentaController` | Detalle de Ventas | GET (paginado), GET/{id}, POST, PUT/{id}, DELETE/{id}                                          |
| `AuthController`         | Autenticación     | POST /api/auth/login (JWT)                                                                     |

#### Detalles de la documentación OpenAPI mejorada

- **Anotaciones `@Tag`**: 8 tags aplicados, uno por cada controlador
- **Anotaciones `@Operation`**: 36 operaciones documentadas con `summary`, `description` y `operationId`
- **Anotaciones `@ApiResponses`**: Cada endpoint especifica todos los códigos HTTP posibles y enlaces OAS (`@Link`)
- **Anotaciones `@SecurityRequirement`**: `bearerAuth` declarado en todos los controladores protegidos
- **Anotaciones `@SecurityScheme`**: Esquema JWT definido en `OpenApiConfig` (type = HTTP, scheme = bearer, bearerFormat = JWT)
- **Paginación documentada**: Parámetros `page`, `size`, `sortBy`, `sortDir` en todos los GET de listado
- **Validación `@Valid`**: Documentada en todos los POST y PUT
- **Validación `@Positive`**: Parámetros de ruta validados
- **DELETE 200**: Respuesta estructurada con mensaje + enlaces HATEOAS
- **DTOs**: Las respuestas usan `ProductoResponseDTO`, `UsuarioResponseDTO`, etc., ocultando campos sensibles
- **Modelos `@Schema`**: Visibles en Swagger UI en la sección Schemas con todos los campos documentados

### 1.3 Validación

- [x] Los 36 endpoints están documentados (35 de dominio + login JWT)
- [x] Cada endpoint tiene `@Operation` con summary y description
- [x] Cada endpoint tiene `@ApiResponses` con códigos HTTP correctos
- [x] Esquema de seguridad JWT visible en Swagger UI
- [x] Links HATEOAS documentados en OAS con `@Link`
- [x] Paginación visible en todos los GET de listado
- [x] Validación de entrada documentada (`@Valid`, `@Positive`)
- [x] La especificación OpenAPI se genera en `/v3/api-docs`

---

## Pregunta 2

**Configura entorno OpenAPI + HATEOAS — 15 puntos**

### 2.1 Objetivo

Demostrar la correcta configuración del entorno de desarrollo para integrar OpenAPI (springdoc-openapi), Spring HATEOAS, JWT (jjwt), Spring Actuator y JaCoCo en el proyecto Minimarket, incluyendo las dependencias Maven, el bean de configuración OpenAPI con esquema de seguridad, y los ajustes de seguridad JWT necesarios.

### 2.2 Desarrollo

#### 2.2.1 Dependencias Maven (pom.xml)

Se configuraron las siguientes dependencias en `pom.xml`:

```xml
<properties>
    <java.version>17</java.version>
    <junit.version>5.10.2</junit.version>
    <mockito.version>5.11.0</mockito.version>
    <jacoco.version>0.8.12</jacoco.version>
</properties>

<dependencies>
    <!-- Spring Boot Starters -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-hateoas</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>

    <!-- SpringDoc OpenAPI + Swagger UI -->
    <dependency>
        <groupId>org.springdoc</groupId>
        <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
        <version>2.8.5</version>
    </dependency>

    <!-- JWT -->
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-api</artifactId>
        <version>0.11.5</version>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-impl</artifactId>
        <version>0.11.5</version>
        <scope>runtime</scope>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-jackson</artifactId>
        <version>0.11.5</version>
        <scope>runtime</scope>
    </dependency>

    <!-- Sanitización de inputs (XSS) -->
    <dependency>
        <groupId>org.jsoup</groupId>
        <artifactId>jsoup</artifactId>
        <version>1.22.2</version>
    </dependency>

    <!-- DevTools, H2, Lombok, Testing -->
    <!-- ... -->
</dependencies>
```

**Explicación de cada dependencia:**

| Dependencia                                 | Propósito                                                                      |
| ------------------------------------------- | ------------------------------------------------------------------------------ |
| `spring-boot-starter-hateoas`               | `EntityModel`, `CollectionModel`, `PagedModel`, `RepresentationModelAssembler` |
| `spring-boot-starter-validation`            | `@Valid`, `@NotNull`, `@NotBlank`, `@Size`, `@Min`, `@Positive`                |
| `spring-boot-starter-actuator`              | Health checks, info endpoints (`/actuator/health`, `/actuator/info`)           |
| `springdoc-openapi-starter-webmvc-ui:2.8.5` | Generación de especificación OAS 3.0 y Swagger UI                              |
| `jjwt-api/impl/jackson:0.11.5`              | Generación y validación de tokens JWT (HS256)                                  |
| `jsoup:1.22.2`                              | Sanitización de inputs para prevenir XSS                                       |
| `jacoco-maven-plugin:0.8.12`                | Cobertura de tests con reportes HTML                                           |
| `h2`                                        | Base de datos en memoria para desarrollo y testing                             |

#### 2.2.2 Bean de Configuración OpenAPI con esquema de seguridad (OpenApiConfig.java)

```java
package com.minimarket.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
public class OpenApiConfig {
}
```

**Elementos configurados:**

- **name**: "bearerAuth" — nombre del esquema de seguridad
- **type**: HTTP — tipo de autenticación
- **scheme**: bearer — esquema HTTP Bearer
- **bearerFormat**: JWT — formato del token

Este esquema aparece en Swagger UI como un botón "Authorize" donde se puede ingresar el token JWT para probar endpoints protegidos.

#### 2.2.3 Configuración de Seguridad JWT (SecurityConfig.java)

```java
@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(JwtProperties.class)
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .headers(headers -> headers.frameOptions(
                frameOptions -> frameOptions.disable()))
            .sessionManagement(sm -> sm.sessionCreationPolicy(
                SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.GET, "/api/productos").permitAll()
                .requestMatchers("/public/**").permitAll()
                .requestMatchers("/h2-console/**").permitAll()
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/swagger-ui.html", "/swagger-ui/**",
                                 "/v3/api-docs/**").permitAll()
                .requestMatchers("/actuator/**").permitAll()
                .anyRequest().authenticated()
            );

        http.addFilterBefore(jwtAuthenticationFilter,
            UsernamePasswordAuthenticationFilter.class);
        http.authenticationProvider(authenticationProvider());
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    // ...
}
```

**Cambios clave respecto a la versión anterior:**

- **Form Login reemplazado por JWT Stateless**: `SessionCreationPolicy.STATELESS`
- **Filtro JWT**: `JwtAuthenticationFilter` se ejecuta antes de `UsernamePasswordAuthenticationFilter`
- **Endpoint de login público**: `POST /api/auth/login` no requiere autenticación previa
- **GET /api/productos público**: Permite consultar productos sin token
- **H2 Console y Actuator accesibles**: Para desarrollo y monitoreo
- **Swagger UI público**: Acceso a documentación sin autenticación

#### 2.2.4 Archivos de Seguridad JWT implementados

| Archivo                                        | Propósito                                                                                      |
| ---------------------------------------------- | ---------------------------------------------------------------------------------------------- |
| `security/config/JwtProperties.java`           | `@ConfigurationProperties(prefix = "jwt")` — secret y expiration                               |
| `security/util/JwtUtil.java`                   | Generación y validación de tokens JWT HS256                                                    |
| `security/filter/JwtAuthenticationFilter.java` | `OncePerRequestFilter` que extrae y valida el token del header `Authorization: Bearer <token>` |
| `security/controller/AuthController.java`      | `POST /api/auth/login` — autentica con `AuthenticationManager` y retorna `JwtResponse`         |
| `security/model/LoginRequest.java`             | DTO con `username` y `password`                                                                |
| `security/model/JwtResponse.java`              | DTO con `token` JWT                                                                            |

#### 2.2.5 Configuración de propiedades (application.properties)

```properties
spring.application.name=minimarket

# H2 Database
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console

# HikariCP Connection Pool
spring.datasource.hikari.connection-test-query=SELECT 1
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=2
spring.datasource.hikari.connection-timeout=30000
spring.datasource.hikari.idle-timeout=600000
spring.datasource.hikari.max-lifetime=1800000
spring.datasource.hikari.pool-name=HikariPool

# JPA / Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect

# Actuator
management.endpoints.web.exposure.include=health,info
management.endpoint.health.show-details=always

# App Info
info.app.name="Minimarket API"
info.app.description="API REST para la gestión de un Minimarket con OpenAPI y HATEOAS"
info.app.version=1.0.0

# JWT
jwt.secret=aK7mP2nL9qR5tW8xY3bD6vF4hJ1cG0sZ8eN4wM7uO9iT2aB5kL3dP6jH8rX1vC4fE
jwt.expiration=3600000
```

#### 2.2.6 Seed de datos de prueba (DataInitializer)

Se implementó un `ApplicationRunner` que inicializa automáticamente:

- **Roles**: ADMIN, USER
- **Usuario admin**: admin / admin123 (contraseña hasheada con BCrypt)
- **3 Categorías**: Bebidas, Lácteos, Panadería
- **50 Productos** de prueba con precios y stock variados

```java
@Component
public class DataInitializer implements ApplicationRunner {

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (rolRepo.findByNombre("ADMIN").isEmpty()) {
            rolRepo.save(new Rol("ADMIN"));
            rolRepo.save(new Rol("USER"));
        }

        if (usuarioRepo.findByUsername("admin").isEmpty()) {
            Usuario admin = new Usuario();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRoles(Set.of(adminRol, userRol));
            usuarioRepo.save(admin);
        }

        if (categoriaRepo.count() == 0) {
            // Crea 3 categorías
        }

        if (productoRepo.count() == 0) {
            // Crea 50 productos con categorías asignadas
        }
    }
}
```

### 2.3 Verificación de la configuración

- [x] Dependencias Maven: `spring-boot-starter-hateoas`, `springdoc-openapi`, `jjwt`, `jsoup`, `actuator`, `jacoco`
- [x] `@SecurityScheme("bearerAuth")` definido en OpenApiConfig
- [x] JWT configurado con `JwtProperties`, `JwtUtil`, `JwtAuthenticationFilter`
- [x] `POST /api/auth/login` funcional con token JWT
- [x] Pool HikariCP configurado con health checks
- [x] Actuator expone `/actuator/health` y `/actuator/info`
- [x] DataInitializer crea usuario admin, roles, categorías y 50 productos
- [x] Compilación exitosa (71 source files, BUILD SUCCESS)
- [x] Tests unitarios pasan (Tests run: 4, Failures: 0, Errors: 0)
- [x] JaCoCo report generado en `target/site/jacoco/index.html`

---

## Pregunta 3

**Implementa HATEOAS con enlaces dinámicos — 20 puntos**

### 3.1 Objetivo

Transformar las respuestas REST del sistema Minimarket para que incluyan enlaces HATEOAS dinámicos con DTOs, paginación completa (PagedModel con first/prev/next/last) y respuestas DELETE con mensaje + enlaces, permitiendo que los clientes naveguen la API de forma autodescriptiva.

### 3.2 Desarrollo

#### 3.2.1 DTOs de respuesta implementados (7 DTOs)

Se crearon DTOs para cada entidad, ocultando datos sensibles y usando `@Relation`:

```java
@Relation(collectionRelation = "productos", itemRelation = "producto")
public class ProductoResponseDTO {
    private Long id;
    private String nombre;
    private Double precio;
    private Integer stock;
    private Long categoriaId;
    private String categoriaNombre;
    // ... getters, setters, static from(Producto)
}

@Relation(collectionRelation = "usuarios", itemRelation = "usuario")
public class UsuarioResponseDTO {
    private Long id;
    private String username;
    private Set<String> roles;     // Sin password!
    // ... getters, setters, static from(Usuario)
}
```

**DTOs creados:** `ProductoResponseDTO`, `CategoriaResponseDTO`, `UsuarioResponseDTO`, `CarritoResponseDTO`, `InventarioResponseDTO`, `VentaResponseDTO`, `DetalleVentaResponseDTO`

#### 3.2.2 ModelAssemblers actualizados (7 assemblers con DTOs y paginación)

**ProductoModelAssembler** — ahora retorna `EntityModel<ProductoResponseDTO>`:

```java
@Component
public class ProductoModelAssembler
        implements RepresentationModelAssembler<Producto, EntityModel<ProductoResponseDTO>> {

    @Override
    public EntityModel<ProductoResponseDTO> toModel(Producto producto) {
        ProductoResponseDTO dto = ProductoResponseDTO.from(producto);
        return EntityModel.of(dto,
            linkTo(methodOn(ProductoController.class)
                .obtenerProductoPorId(producto.getId())).withSelfRel(),
            linkTo(methodOn(ProductoController.class)
                .listarProductos(0, 10, "nombre", "asc")).withRel("listar"),
            linkTo(methodOn(ProductoController.class)
                .guardarProducto(null)).withRel("crear"),
            linkTo(methodOn(ProductoController.class)
                .actualizarProducto(producto.getId(), null)).withRel("editar"),
            linkTo(methodOn(ProductoController.class)
                .eliminarProducto(producto.getId())).withRel("eliminar"),
            linkTo(methodOn(ProductoController.class)
                .obtenerCategoria(producto.getId())).withRel("categoria"),
            linkTo(methodOn(ProductoController.class)
                .asignarCategoria(producto.getId(), null)).withRel("asignar-categoria")
        );
    }
}
```

**Usuarios ya no exponen password** — `UsuarioResponseDTO` solo incluye `id`, `username` y `roles`.

#### 3.2.3 Paginación HATEOAS (PagedModel)

Todos los GET de listado retornan `PagedModel` con enlaces de navegación:

```java
public ResponseEntity<PagedModel<EntityModel<ProductoResponseDTO>>> listarProductos(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "nombre") String sortBy,
        @RequestParam(defaultValue = "asc") String sortDir) {

    Page<Producto> productosPage = productoService.findAll(pageable);

    List<EntityModel<ProductoResponseDTO>> productosModel =
        productosPage.getContent().stream()
            .map(productoModelAssembler::toModel)
            .toList();

    PagedModel<EntityModel<ProductoResponseDTO>> pagedModel =
        PagedModel.of(productosModel, metadata);

    // Enlaces de navegación
    pagedModel.add(linkTo(methodOn(ProductoController.class)
        .listarProductos(page, size, sortBy, sortDir)).withSelfRel());
    pagedModel.add(linkTo(methodOn(ProductoController.class)
        .listarProductos(0, size, sortBy, sortDir)).withRel("first"));
    pagedModel.add(linkTo(methodOn(ProductoController.class)
        .listarProductos(totalPages - 1, size, sortBy, sortDir))
        .withRel("last"));
    if (productosPage.hasPrevious()) {
        pagedModel.add(linkTo(methodOn(ProductoController.class)
            .listarProductos(page - 1, size, sortBy, sortDir))
            .withRel("prev"));
    }
    if (productosPage.hasNext()) {
        pagedModel.add(linkTo(methodOn(ProductoController.class)
            .listarProductos(page + 1, size, sortBy, sortDir))
            .withRel("next"));
    }

    return ResponseEntity.ok(pagedModel);
}
```

#### 3.2.4 DELETE con respuesta estructurada + enlaces HATEOAS

Los endpoints DELETE ahora retornan `200 OK` con un mensaje y enlaces:

```java
@DeleteMapping("/{id}")
public ResponseEntity<EntityModel<Map<String, String>>> eliminarProducto(
        @PathVariable @Positive Long id) {
    productoService.deleteById(id);
    EntityModel<Map<String, String>> response = EntityModel.of(
            Map.of("message", "Producto eliminado exitosamente"),
            linkTo(methodOn(ProductoController.class)
                .listarProductos(0, 10, "nombre", "asc"))
                .withRel("allProductos"),
            linkTo(methodOn(ProductoController.class)
                .guardarProducto(null)).withRel("addProducto")
    );
    return ResponseEntity.ok(response);
}
```

#### 3.2.5 Estructura de enlaces por recurso (actualizada)

| Recurso                | Tipo retorno                                   | Enlaces incluidos                                                   |
| ---------------------- | ---------------------------------------------- | ------------------------------------------------------------------- |
| Producto               | `EntityModel<ProductoResponseDTO>`             | self, listar, crear, editar, eliminar, categoria, asignar-categoria |
| Lista Productos        | `PagedModel<EntityModel<ProductoResponseDTO>>` | self, first, last, prev, next                                       |
| Categoría              | `EntityModel<CategoriaResponseDTO>`            | self, listar, crear, editar, eliminar                               |
| Carrito                | `EntityModel<CarritoResponseDTO>`              | self, listar, crear, editar, eliminar, usuario, producto            |
| Inventario             | `EntityModel<InventarioResponseDTO>`           | self, listar, crear, editar, eliminar, producto                     |
| Usuario (sin password) | `EntityModel<UsuarioResponseDTO>`              | self, listar, crear, editar, eliminar                               |
| Venta                  | `EntityModel<VentaResponseDTO>`                | self, listar, crear, usuario                                        |
| DetalleVenta           | `EntityModel<DetalleVentaResponseDTO>`         | self, listar, crear, editar, eliminar, venta, producto              |
| DELETE respuesta       | `EntityModel<Map<String,String>>`              | allProductos/allCategorias/etc, addProducto/addCategoria/etc        |

### 3.3 Ejemplos de respuestas JSON

#### Producto individual con DTO + links

```json
GET /api/productos/1

{
  "id": 1,
  "nombre": "Producto 1",
  "precio": 1100.0,
  "stock": 21,
  "categoriaId": 1,
  "categoriaNombre": "Bebidas",
  "_links": {
    "self": { "href": "http://localhost:8080/api/productos/1" },
    "listar": { "href": "http://localhost:8080/api/productos?page=0&size=10&sortBy=nombre&sortDir=asc" },
    "crear": { "href": "http://localhost:8080/api/productos" },
    "editar": { "href": "http://localhost:8080/api/productos/1" },
    "eliminar": { "href": "http://localhost:8080/api/productos/1" },
    "categoria": { "href": "http://localhost:8080/api/productos/1/categoria" },
    "asignar-categoria": { "href": "http://localhost:8080/api/productos/1/categoria" }
  }
}
```

#### Usuario (sin password expuesto)

```json
GET /api/usuarios/1

{
  "id": 1,
  "username": "admin",
  "roles": ["ADMIN", "USER"],
  "_links": {
    "self": { "href": "http://localhost:8080/api/usuarios/1" },
    "listar": { "href": "http://localhost:8080/api/usuarios?page=0&size=10&sortBy=username&sortDir=asc" },
    "crear": { "href": "http://localhost:8080/api/usuarios" },
    "editar": { "href": "http://localhost:8080/api/usuarios/1" },
    "eliminar": { "href": "http://localhost:8080/api/usuarios/1" }
  }
}
```

#### DELETE con respuesta

```json
DELETE /api/productos/1

{
  "message": "Producto eliminado exitosamente",
  "_links": {
    "allProductos": { "href": "http://localhost:8080/api/productos?page=0&size=10&sortBy=nombre&sortDir=asc" },
    "addProducto": { "href": "http://localhost:8080/api/productos" }
  }
}
```

#### Error estructurado

```json
GET /api/productos/999

{
  "timestamp": "2026-07-11T20:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Producto no encontrado con ID: 999",
  "path": "uri=/api/productos/999"
}
```

### 3.4 Verificación

- [x] 7 assemblers implementados con `RepresentationModelAssembler`
- [x] Todos los assemblers retornan DTOs (no entidades crudas)
- [x] 29 endpoints del dominio retornan `EntityModel`/`PagedModel`
- [x] Enlaces son dinámicos (basados en el ID del recurso)
- [x] Uso consistente de `linkTo(methodOn(Controller.class))`
- [x] Paginación con `PagedModel.PageMetadata` y first/prev/next/last
- [x] DELETE retorna 200 con mensaje + enlaces HATEOAS
- [x] Passwords no aparecen en respuesta de usuarios
- [x] Validación con `@Positive` en path variables
- [x] `@Valid` en request bodies para validación de entrada

---

## Pregunta 4

**Genera y valida documentación en Swagger UI — 20 puntos**

### 4.1 Objetivo

Verificar que la documentación OpenAPI es accesible a través de Swagger UI, que todos los endpoints son visibles y funcionales, que el esquema de seguridad JWT permite probar endpoints autenticados, y que la paginación y validación funcionan correctamente.

### 4.2 Desarrollo

#### 4.2.1 Acceso a Swagger UI

La documentación se encuentra disponible en:

```
http://localhost:8080/swagger-ui.html
```

Para iniciar la aplicación:

```bash
export JAVA_HOME="$HOME/jdk17"
export PATH="$JAVA_HOME/bin:$PATH"
cd /home/nicolasariel/trabajos/Experiencia3/minimarket
./mvnw spring-boot:run
```

#### 4.2.2 Estructura de Swagger UI mejorada

Al cargar Swagger UI, se observan:

1. **Esquema de seguridad**: Botón "Authorize" con el esquema `bearerAuth` (JWT)
2. **8 secciones de tags**: Productos, Categorías, Carrito, Inventario, Usuarios, Ventas, Detalle de Ventas, Autenticación (auth-controller)
3. **36 operaciones documentadas**: Con método HTTP, ruta, parámetros de paginación, resumen y descripción
4. **Links OAS visibles**: En las respuestas de los endpoints que lo incluyen (ej. `getProductoById`)
5. **Modelos DTO visibles**: `ProductoResponseDTO`, `CategoriaResponseDTO`, `UsuarioResponseDTO`, etc.
6. **Botón "Try it out"**: Permite ejecutar peticiones directamente desde el navegador, con soporte para paginación
7. **Parámetros de paginación**: `page`, `size`, `sortBy`, `sortDir` visibles en cada GET de listado

#### 4.2.3 Flujo de autenticación JWT en Swagger UI

1. Ejecutar `POST /api/auth/login` con `{ "username": "admin", "password": "admin123" }`
2. Copiar el token JWT de la respuesta
3. Click en el botón "Authorize" en la parte superior
4. Pegar el token en el campo `Value`: `Bearer <token>`
5. Todos los endpoints protegidos ahora son accesibles desde Swagger UI

#### 4.2.4 Códigos HTTP verificados (actualizados)

| Método HTTP          | Código esperado | Contexto                                |
| -------------------- | --------------- | --------------------------------------- |
| GET                  | 200 OK          | Recurso encontrado                      |
| POST                 | 201 Created     | Recurso creado exitosamente             |
| PUT                  | 200 OK          | Recurso actualizado exitosamente        |
| DELETE               | 200 OK          | Recurso eliminado (con mensaje + links) |
| POST/PUT             | 400 Bad Request | Datos de entrada inválidos (validación) |
| GET/PUT/DELETE       | 404 Not Found   | Recurso no encontrado                   |
| POST /api/auth/login | 200 OK          | Token JWT retornado                     |
| Acceso sin token     | 401/403         | Endpoint protegido rechaza petición     |

#### 4.2.5 Pruebas realizadas desde Swagger UI

1. **Login JWT** — `POST /api/auth/login` → 200 OK con token
2. **GET /api/productos (paginado)** — Lista paginada con first/prev/next/last (200 OK)
3. **GET /api/productos?page=0&size=5&sortBy=precio&sortDir=desc** — Ordenamiento (200 OK)
4. **POST /api/productos** — Crear producto con validación (201 Created)
5. **GET /api/productos/{id}** — Obtener producto con \_links OAS visibles (200 OK)
6. **PUT /api/productos/{id}** — Actualizar producto con @Valid (200 OK)
7. **DELETE /api/productos/{id}** — Eliminar con respuesta estructurada (200 OK)
8. **GET /api/productos/{id}** — Producto eliminado (404 Not Found con ErrorResponse)
9. **GET /api/usuarios** — Lista sin exponer passwords (200 OK)
10. **POST con datos inválidos** — 400 Bad Request con detalles de validación

#### 4.2.6 Resultados de compilación y tests

```bash
./mvnw clean compile   # BUILD SUCCESS (71 source files)
./mvnw clean test      # Tests run: 4, Failures: 0, Errors: 0, Skipped: 0
```

JaCoCo report generado en `target/site/jacoco/index.html`.

### 4.3 Evidencias

- **Swagger UI** visualiza los 36 endpoints agrupados por 8 tags
- **Botón Authorize** presente con esquema bearerAuth (JWT)
- **Modelos DTO** aparecen con sus campos y tipos de datos
- **Parámetros de paginación** documentados en cada GET de listado
- **Parámetros de ruta** con validación `@Positive` visible
- **Códigos de respuesta** especificados para cada operación
- **Links OAS** visibles en las respuestas (ej. self, allProductos)
- **ErrorResponse** visible en schemas con timestamp, status, error, message, path

---

## Pregunta 5

**Analiza y reflexiona sobre resultados — 10 puntos**

### 5.1 Objetivo

Analizar el impacto técnico de implementar OpenAPI, HATEOAS, JWT, DTOs, paginación, validación y manejo de excepciones en el sistema Minimarket, reflexionando sobre cómo estas tecnologías afectan la calidad del software, la mantenibilidad, la navegabilidad y la escalabilidad.

### 5.2 Análisis comparativo antes y después

#### 5.2.1 Comparación de código: Controlador ProductoController

**Antes (sin OpenAPI, HATEOAS, paginación, DTOs):**

```java
@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    @Autowired
    private ProductoService productoService;

    @GetMapping
    public ResponseEntity<List<Producto>> listarProductos() {
        return ResponseEntity.ok(productoService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Producto> obtenerProductoPorId(@PathVariable Long id) {
        Producto producto = productoService.findById(id);
        if (producto == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(producto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarProducto(@PathVariable Long id) {
        productoService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
```

**Después (con OpenAPI, HATEOAS, paginación, DTOs, validación, JWT):**

```java
@RestController
@RequestMapping("/api/productos")
@Tag(name = "Productos", description = "Operaciones CRUD para la gestión de productos")
@SecurityRequirement(name = "bearerAuth")
public class ProductoController {

    @Autowired
    private ProductoService productoService;
    @Autowired
    private ProductoModelAssembler productoModelAssembler;

    @GetMapping
    @Operation(summary = "Listar todos los productos", description = "...",
               operationId = "listarProductos")
    public ResponseEntity<PagedModel<EntityModel<ProductoResponseDTO>>> listarProductos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "nombre") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        // ... lógica de paginación con PagedModel y links first/prev/next/last
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener un producto por ID", operationId = "obtenerProductoPorId",
        responses = {
            @ApiResponse(responseCode = "200",
                links = {
                    @Link(name = "self", operationId = "obtenerProductoPorId"),
                    @Link(name = "allProductos", operationId = "listarProductos")
                })
    })
    public ResponseEntity<EntityModel<ProductoResponseDTO>> obtenerProductoPorId(
            @Parameter(description = "ID del producto", required = true, example = "1")
            @PathVariable @Positive Long id) {
        return ResponseEntity.ok(productoModelAssembler.toModel(productoService.findById(id)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<EntityModel<Map<String, String>>> eliminarProducto(
            @PathVariable @Positive Long id) {
        productoService.deleteById(id);
        return ResponseEntity.ok(EntityModel.of(
            Map.of("message", "Producto eliminado exitosamente"),
            linkTo(...).withRel("allProductos"),
            linkTo(...).withRel("addProducto")
        ));
    }
}
```

**Diferencias clave:**

| Aspecto                     | Antes                      | Después                                                |
| --------------------------- | -------------------------- | ------------------------------------------------------ |
| Tipo retorno GET lista      | `List<Producto>`           | `PagedModel<EntityModel<ProductoResponseDTO>>`         |
| Tipo retorno GET individual | `Producto` (entidad cruda) | `EntityModel<ProductoResponseDTO>` (DTO)               |
| DELETE respuesta            | `204 No Content`           | `200 + EntityModel<Map>` con mensaje y links           |
| Paginación                  | No                         | `page`, `size`, `sortBy`, `sortDir`                    |
| Validación de entrada       | No                         | `@Valid`, `@Positive`                                  |
| Seguridad                   | Form Login                 | JWT stateless con `@SecurityRequirement("bearerAuth")` |
| Manejo de errores           | `return null` manual       | `GlobalExceptionHandler` + `ResourceNotFoundException` |
| Documentación               | Ninguna                    | `@Tag`, `@Operation`, `@ApiResponses`, `@Link`         |
| Passwords expuestos         | Sí                         | No (DTO oculta `password`)                             |

#### 5.2.2 Comparación de respuestas JSON

**Antes — GET /api/productos:**

```json
[
  {
    "id": 1,
    "nombre": "Leche Entera",
    "precio": 1200,
    "stock": 50,
    "categoria": { "id": 1, "nombre": "Lácteos", "productos": null }
  }
]
```

**Después — GET /api/productos (paginado, DTO, HATEOAS):**

```json
{
  "_embedded": {
    "productos": [
      {
        "id": 1,
        "nombre": "Producto 1",
        "precio": 1100.0,
        "stock": 21,
        "categoriaId": 1,
        "categoriaNombre": "Bebidas",
        "_links": {
          "self": { "href": "http://localhost:8080/api/productos/1" },
          "listar": {
            "href": "http://localhost:8080/api/productos?page=0&size=10&..."
          },
          "crear": { "href": "http://localhost:8080/api/productos" },
          "editar": { "href": "http://localhost:8080/api/productos/1" },
          "eliminar": { "href": "http://localhost:8080/api/productos/1" },
          "categoria": {
            "href": "http://localhost:8080/api/productos/1/categoria"
          },
          "asignar-categoria": {
            "href": "http://localhost:8080/api/productos/1/categoria"
          }
        }
      }
    ]
  },
  "_links": {
    "self": { "href": "..." },
    "first": { "href": "..." },
    "last": { "href": "..." },
    "next": { "href": "..." }
  },
  "page": {
    "size": 10,
    "totalElements": 50,
    "totalPages": 5,
    "number": 0
  }
}
```

### 5.3 Reflexión técnica ampliada

#### 5.3.1 Impacto en la seguridad

La migración de Form Login a JWT stateless representa un avance significativo:

- **Tokens sin estado**: El servidor no mantiene sesiones, cada request se autentica independientemente
- **DTOs ocultan datos**: `UsuarioResponseDTO` no expone `password` ni datos internos de la entidad
- **Monitoreo**: `SuspiciousActivityService` registra intentos fallidos de login, JWT inválidos y tasa de requests
- **Password Encoding**: BCryptPasswordEncoder aplicado en el service layer al crear/actualizar usuarios
- **Sanitización**: Jsoup para prevenir XSS en inputs de texto

#### 5.3.2 Impacto en la mantenibilidad

1. **Documentación viva con esteroides**: Además de las anotaciones OpenAPI, los `@Link` en `@ApiResponse` documentan la navegabilidad HATEOAS directamente en el spec OAS
2. **Manejo centralizado de errores**: `GlobalExceptionHandler` con `ErrorResponse` estructurado garantiza consistencia en todas las respuestas de error
3. **DTOs como capa de abstracción**: Los DTOs protegen la evolución de las entidades internas sin romper el contrato de la API
4. **Validación declarativa**: `@NotBlank`, `@Size`, `@Min`, `@Positive` en entidades + `@Valid` en controladores = validación consistente sin código boilerplate

#### 5.3.3 Impacto en la navegabilidad (HATEOAS avanzado)

1. **Paginación navegable**: `PagedModel` con `first`, `prev`, `next`, `last` permite a los clientes recorrer colecciones grandes sin necesidad de hardcodear parámetros
2. **Ordenamiento**: `sortBy` y `sortDir` documentados en cada GET de listado
3. **DELETE autodescriptivo**: La respuesta DELETE incluye enlaces para volver a la lista y crear un nuevo recurso, guiando al cliente en el flujo natural
4. **Links OAS documentados**: Los `@Link` en `@ApiResponse` permiten que herramientas de generación de clientes comprendan la navegabilidad

#### 5.3.4 Impacto en la escalabilidad

1. **JWT para microservicios**: La autenticación stateless es fundamental para escalar horizontalmente
2. **Paginación**: Evita cargar datasets completos en memoria, preparando el sistema para crecimiento
3. **Actuator + HikariCP**: Monitoreo de salud y pool de conexiones configurado para entornos productivos
4. **JaCoCo**: Cobertura de tests medible y visible para garantizar calidad en el crecimiento del código
5. **DataInitializer**: Seed de datos facilita desarrollo, testing y demos sin configuración manual

#### 5.3.5 Desafíos y lecciones aprendidas

1. **DTOs + Assemblers**: La combinación añade una capa de código pero protege la API contra cambios internos
2. **JWT vs Form Login**: La migración requiere más infraestructura (`JwtUtil`, `JwtAuthenticationFilter`, `AuthController`) pero habilita la escalabilidad
3. **Paginación en múltiples controladores**: El patrón `PagedModel` es repetitivo pero consistente y predecible para los clientes
4. **Validación en entidades vs DTOs**: Las anotaciones de validación en entidades (`@Entity`) se ejecutan antes de la persistencia, protegiendo la integridad de datos

### 5.4 Conclusión

El sistema Minimarket ha pasado de un diseño REST de Nivel 2 a uno de Nivel 3 (Richardson Maturity Model) con mejoras sustanciales en cada capa:

- **Capa de presentación**: DTOs que ocultan entidades, `@Link` en OAS, `@SecurityRequirement` en Swagger
- **Capa de negocio**: Validación declarativa, manejo centralizado de excepciones, password encoding
- **Capa de datos**: Paginación, ordenamiento, HikariCP connection pool
- **Capa de seguridad**: JWT stateless, SuspiciousActivityService, endpoints públicos/privados bien definidos
- **Capa de operaciones**: Actuator health checks, JaCoCo cobertura, DataInitializer seed

71 archivos fuente, 36 endpoints, 7 DTOs, 7 assemblers, 8 entidades validadas, 4 tests pasando.

---

## Pregunta 6

**Documenta proceso y sube a GitHub — 10 puntos**

### 6.1 Objetivo

Documentar el proceso completo de implementación, actualizar el README con toda la información del proyecto y subir el código a GitHub con una estructura clara y organizada.

### 6.2 Desarrollo

#### 6.2.1 Estructura del proyecto completa

```
minimarket/
├── pom.xml
├── README.md
├── desarrollo-preguntas.md
└── src/
    ├── main/java/com/minimarket/
    │   ├── MinimarketApplication.java
    │   ├── config/
    │   │   ├── OpenApiConfig.java               # @SecurityScheme JWT
    │   │   └── DataInitializer.java             # Seed datos prueba
    │   ├── dto/
    │   │   ├── AsignarCategoriaRequest.java
    │   │   ├── ProductoResponseDTO.java          # 7 DTOs de respuesta
    │   │   ├── CategoriaResponseDTO.java
    │   │   ├── UsuarioResponseDTO.java           # Sin password
    │   │   ├── CarritoResponseDTO.java
    │   │   ├── InventarioResponseDTO.java
    │   │   ├── VentaResponseDTO.java
    │   │   └── DetalleVentaResponseDTO.java
    │   ├── exception/
    │   │   ├── ErrorResponse.java                # Estructura error
    │   │   ├── GlobalExceptionHandler.java       # Manejo centralizado
    │   │   └── ResourceNotFoundException.java
    │   ├── assembler/                            # 7 assemblers HATEOAS
    │   │   ├── ProductoModelAssembler.java
    │   │   ├── CategoriaModelAssembler.java
    │   │   ├── CarritoModelAssembler.java
    │   │   ├── InventarioModelAssembler.java
    │   │   ├── UsuarioModelAssembler.java
    │   │   ├── VentaModelAssembler.java
    │   │   └── DetalleVentaModelAssembler.java
    │   ├── controller/                           # 8 controladores
    │   │   ├── ProductoController.java
    │   │   ├── CategoriaController.java
    │   │   ├── CarritoController.java
    │   │   ├── InventarioController.java
    │   │   ├── UsuarioController.java
    │   │   ├── VentaController.java
    │   │   ├── DetalleVentaController.java
    │   │   └── HolaMundoController.java
    │   ├── service/                              # 8 interfaces + 8 impl
    │   ├── entity/                               # 8 entidades con @Valid
    │   ├── repository/                           # 8 repositorios JPA
    │   └── security/
    │       ├── config/
    │       │   ├── SecurityConfig.java           # JWT stateless
    │       │   └── JwtProperties.java            # Config JWT
    │       ├── controller/
    │       │   └── AuthController.java           # POST /api/auth/login
    │       ├── filter/
    │       │   └── JwtAuthenticationFilter.java  # Filtro JWT
    │       ├── model/
    │       │   ├── CustomUserDetails.java
    │       │   ├── JwtResponse.java
    │       │   └── LoginRequest.java
    │       ├── monitor/
    │       │   └── SuspiciousActivityService.java
    │       ├── service/
    │       │   └── CustomUserDetailsService.java
    │       └── util/
    │           └── JwtUtil.java
    └── test/java/com/minimarket/
        ├── MinimarketApplicationTests.java
        └── UsuarioTest.java
```

#### 6.2.2 Nuevos archivos creados (31)

| Categoría     | Archivos                                                                                                                                                                                             |
| ------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| DTOs          | `ProductoResponseDTO.java`, `CategoriaResponseDTO.java`, `UsuarioResponseDTO.java`, `CarritoResponseDTO.java`, `InventarioResponseDTO.java`, `VentaResponseDTO.java`, `DetalleVentaResponseDTO.java` |
| Excepciones   | `ErrorResponse.java`, `GlobalExceptionHandler.java`, `ResourceNotFoundException.java`                                                                                                                |
| Seguridad JWT | `JwtProperties.java`, `JwtUtil.java`, `JwtAuthenticationFilter.java`, `AuthController.java`, `JwtResponse.java`, `LoginRequest.java`                                                                 |
| Monitoreo     | `SuspiciousActivityService.java`                                                                                                                                                                     |
| Configuración | `DataInitializer.java`                                                                                                                                                                               |
| Total         | **31 nuevos archivos + 40 modificados = 71 source files**                                                                                                                                            |

#### 6.2.3 Archivos originales modificados (11 controladores/servicios + config)

| Archivo                  | Cambio principal                                                              |
| ------------------------ | ----------------------------------------------------------------------------- |
| `pom.xml`                | + JWT (jjwt), Jsoup, Actuator, JaCoCo, test props                             |
| `OpenApiConfig.java`     | Reescrito con `@SecurityScheme("bearerAuth")`                                 |
| `SecurityConfig.java`    | Form Login → JWT stateless + `DaoAuthenticationProvider`                      |
| `application.properties` | + HikariCP, Actuator, JWT secret/expiration                                   |
| `7 Controllers`          | + Paginación, DTOs, `@Valid`, `@Positive`, DELETE 200, `@SecurityRequirement` |
| `7 Assemblers`           | + DTOs como tipo de retorno, enlaces con params de paginación                 |
| `8 Entity classes`       | + `@NotBlank`, `@Size`, `@Min`, `@NotNull`, no-arg constructors               |
| `8 Service Impls`        | + `Pageable` methods, `ResourceNotFoundException`, password encoding          |
| `8 Service Interfaces`   | + `findAll(Pageable)` methods, `updateUsuario`, `getUsuarioByIdOrThrow`       |
| `README.md`              | Reescrito completo con JWT, paginación, DTOs, nuevos endpoints                |

#### 6.2.4 README.md actualizado

El README fue completamente reescrito para reflejar todas las mejoras:

- Tecnologías actualizadas (JWT, Jsoup, JaCoCo, Actuator)
- Sección de Autenticación JWT con flujo de login
- Sección de Datos de prueba (seed automático)
- Tabla de paginación y ordenamiento
- Nuevos códigos HTTP (DELETE 200)
- Ejemplos de respuesta actualizados (DTOs, paginación, DELETE, errores)
- Estructura de proyecto completa (71 archivos)
- Sección de Seguridad con JWT, BCrypt, SuspiciousActivityService
- Sección de Validación con Bean Validation
- Acceso a Actuator health/info

#### 6.2.5 Comandos de compilación y verificación

```bash
# Compilar el proyecto (71 source files)
./mvnw clean compile  # BUILD SUCCESS

# Ejecutar tests unitarios (4 tests) + JaCoCo report
./mvnw clean test     # Tests run: 4, Failures: 0, Errors: 0

# Iniciar la aplicación
./mvnw spring-boot:run

# Abrir en el navegador:
# Swagger UI:        http://localhost:8080/swagger-ui.html
# Especificación OAS: http://localhost:8080/v3/api-docs
# Actuator Health:    http://localhost:8080/actuator/health
# Actuator Info:      http://localhost:8080/actuator/info
# H2 Console:         http://localhost:8080/h2-console
# JaCoCo Report:      target/site/jacoco/index.html
```

### 6.3 Checklist de entrega

- [x] Proyecto compila sin errores (BUILD SUCCESS, 71 source files)
- [x] Tests unitarios pasan (4/4 exitosos)
- [x] Swagger UI accesible con esquema bearerAuth JWT
- [x] 36 endpoints documentados con OpenAPI
- [x] 7 assemblers implementados con HATEOAS + DTOs
- [x] Paginación con PagedModel y first/prev/next/last
- [x] DELETE retorna 200 con mensaje + enlaces
- [x] DTOs ocultan datos sensibles (password, relaciones cíclicas)
- [x] JWT implementado con AuthController /api/auth/login
- [x] DataInitializer crea datos de prueba automáticamente
- [x] GlobalExceptionHandler con ErrorResponse estructurado
- [x] Validación Bean Validation en entidades y controladores
- [x] SuspiciousActivityService monitorea seguridad
- [x] Actuator health/info accesibles
- [x] JaCoCo cobertura de tests con reporte HTML
- [x] HikariCP connection pool configurado
- [x] Links OAS (@Link) documentados en especificación OpenAPI
- [x] Análisis y reflexión sobre resultados ampliados
- [x] README actualizado con toda la información
- [x] Estructura de carpetas organizada
- [x] Código listo para subir a GitHub
