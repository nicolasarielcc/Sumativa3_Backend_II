# Desarrollo de Preguntas — Formato de Respuesta (Forma C)

**Asignatura:** Desarrollo Backend II  
**Experiencia:** 3 — Semana 8  
**Tema:** Implementación avanzada de documentación en microservicios con OpenAPI y HATEOAS  
**Estudiante:** [Nombre del estudiante]  
**Fecha:** 08 Julio 2026

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

Demostrar la capacidad de documentar todos los endpoints REST del sistema Minimarket utilizando el estándar OpenAPI 3.0, aplicando anotaciones como `@Operation`, `@ApiResponses`, `@Tag` y `@Schema` para generar una especificación completa y consumible desde Swagger UI.

### 1.2 Desarrollo

#### Antes (código sin documentación OpenAPI)

Antes de implementar OpenAPI, los controladores del sistema Minimarket eran funcionales pero carecían de cualquier tipo de documentación auto-contenida. A continuación se muestra un ejemplo del estado original del `ProductoController`:

```java
// ProductoController — ANTES (sin OpenAPI)
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

Como se observa, no existía ninguna anotación de documentación. Un desarrollador nuevo en el equipo debía revisar el código fuente completo para entender qué hacía cada endpoint, qué parámetros esperaba y qué códigos de respuesta podía retornar. Tampoco existía una interfaz gráfica para probar los endpoints.

#### Después (código con documentación OpenAPI completa)

Se documentaron 35 endpoints distribuidos en 7 controladores. Cada controlador recibió una etiqueta descriptiva, cada método recibió metadatos de operación y cada respuesta posible fue especificada. A continuación se muestra el mismo controlador después de la implementación:

```java
// ProductoController — DESPUÉS (con OpenAPI completa)
@RestController
@RequestMapping("/api/productos")
@Tag(name = "Productos", description = "Operaciones CRUD para la gestión de productos")
public class ProductoController {

    @Autowired
    private ProductoService productoService;
    @Autowired
    private CategoriaService categoriaService;
    @Autowired
    private ProductoModelAssembler productoModelAssembler;
    @Autowired
    private CategoriaModelAssembler categoriaModelAssembler;

    @GetMapping
    @Operation(summary = "Listar todos los productos",
               description = "Retorna una lista con todos los productos registrados")
    @ApiResponses({
        @ApiResponse(responseCode = "200",
                     description = "Lista de productos obtenida correctamente")
    })
    public ResponseEntity<CollectionModel<EntityModel<Producto>>> listarProductos() {
        return ResponseEntity.ok(
                productoModelAssembler.toCollectionModel(productoService.findAll()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener un producto por ID",
               description = "Retorna un producto según su ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Producto encontrado",
                     content = @Content(schema = @Schema(implementation = Producto.class))),
        @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    public ResponseEntity<EntityModel<Producto>> obtenerProductoPorId(
            @Parameter(description = "ID del producto", required = true) @PathVariable Long id) {
        Producto producto = productoService.findById(id);
        if (producto == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(productoModelAssembler.toModel(producto));
    }

    @PostMapping
    @Operation(summary = "Crear un nuevo producto",
               description = "Registra un nuevo producto en el sistema")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Producto creado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos")
    })
    public ResponseEntity<EntityModel<Producto>> guardarProducto(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Datos del producto a crear", required = true)
            @RequestBody Producto producto) {
        Producto nuevo = productoService.save(producto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(productoModelAssembler.toModel(nuevo));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar un producto",
               description = "Actualiza los datos de un producto existente")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Producto actualizado correctamente"),
        @ApiResponse(responseCode = "404", description = "Producto no encontrado"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos")
    })
    public ResponseEntity<EntityModel<Producto>> actualizarProducto(
            @Parameter(description = "ID del producto", required = true) @PathVariable Long id,
            @RequestBody Producto producto) {
        Producto productoExistente = productoService.findById(id);
        if (productoExistente == null) return ResponseEntity.notFound().build();
        producto.setId(id);
        return ResponseEntity.ok(productoModelAssembler.toModel(productoService.save(producto)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un producto",
               description = "Elimina un producto del sistema")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Producto eliminado correctamente"),
        @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    public ResponseEntity<Void> eliminarProducto(
            @Parameter(description = "ID del producto", required = true) @PathVariable Long id) {
        Producto producto = productoService.findById(id);
        if (producto == null) return ResponseEntity.notFound().build();
        productoService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
```

#### Controladores documentados

| Controlador | Tag | Endpoints |
|---|---|---|
| `ProductoController` | Productos | `GET /api/productos`, `GET /api/productos/{id}`, `POST /api/productos`, `PUT /api/productos/{id}`, `DELETE /api/productos/{id}`, `GET /api/productos/{id}/categoria`, `POST /api/productos/{id}/categoria` |
| `CategoriaController` | Categorías | `GET /api/categorias`, `GET /api/categorias/{id}`, `POST /api/categorias`, `PUT /api/categorias/{id}`, `DELETE /api/categorias/{id}` |
| `CarritoController` | Carrito | `GET /api/carrito`, `GET /api/carrito/{id}`, `POST /api/carrito`, `PUT /api/carrito/{id}`, `DELETE /api/carrito/{id}` |
| `InventarioController` | Inventario | `GET /api/inventario`, `GET /api/inventario/{id}`, `POST /api/inventario`, `PUT /api/inventario/{id}`, `DELETE /api/inventario/{id}` |
| `UsuarioController` | Usuarios | `GET /api/usuarios`, `GET /api/usuarios/{id}`, `POST /api/usuarios`, `PUT /api/usuarios/{id}`, `DELETE /api/usuarios/{id}` |
| `VentaController` | Ventas | `GET /api/ventas`, `GET /api/ventas/{id}`, `POST /api/ventas` |
| `DetalleVentaController` | Detalle de Ventas | `GET /api/detalle-ventas`, `GET /api/detalle-ventas/{id}`, `POST /api/detalle-ventas`, `PUT /api/detalle-ventas/{id}`, `DELETE /api/detalle-ventas/{id}` |

#### Detalles de la documentación OpenAPI

- **Anotaciones `@Tag`**: 7 tags aplicados, uno por cada controlador, con nombre y descripción en español
- **Anotaciones `@Operation`**: 35 operaciones documentadas con summary y description
- **Anotaciones `@ApiResponses`**: Cada endpoint especifica todos los códigos HTTP posibles:
  - `200` para respuestas exitosas GET y PUT
  - `201` para respuestas exitosas POST
  - `204` para respuestas exitosas DELETE
  - `400` para datos de entrada inválidos
  - `404` para recursos no encontrados
- **Anotaciones `@Parameter`**: Parámetros de ruta documentados con descripción y `required = true`
- **Anotaciones `@Schema`**: Modelos de datos documentados con ejemplos y descripciones

### 1.3 Validación

- [x] Los 35 endpoints están documentados
- [x] Cada endpoint tiene `@Operation` con summary y description
- [x] Cada endpoint tiene `@ApiResponses` con códigos HTTP correctos
- [x] Cada controlador tiene `@Tag` con nombre y descripción
- [x] La especificación OpenAPI se genera en `/v3/api-docs`
- [x] La especificación es visible y navegable en Swagger UI

---

## Pregunta 2

**Configura entorno OpenAPI + HATEOAS — 15 puntos**

### 2.1 Objetivo

Demostrar la correcta configuración del entorno de desarrollo para integrar OpenAPI (springdoc-openapi) y Spring HATEOAS en el proyecto Minimarket, incluyendo las dependencias Maven, el bean de configuración OpenAPI y los ajustes de seguridad necesarios.

### 2.2 Desarrollo

#### 2.2.1 Dependencias Maven (pom.xml)

Se agregaron tres dependencias clave al archivo `pom.xml`:

```xml
<!-- Spring HATEOAS — para enlaces dinámicos en respuestas REST -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-hateoas</artifactId>
</dependency>

<!-- Spring Validation — para validación de datos de entrada -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>

<!-- SpringDoc OpenAPI — para generación de especificación OAS y Swagger UI -->
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.8.5</version>
</dependency>
```

**Explicación de cada dependencia:**

| Dependencia | Propósito |
|---|---|
| `spring-boot-starter-hateoas` | Proporciona las clases `EntityModel`, `CollectionModel`, `RepresentationModelAssembler` y `WebMvcLinkBuilder` para implementar HATEOAS |
| `spring-boot-starter-validation` | Habilita `@Valid` y `@NotNull` para validar datos de entrada (usado en `AsignarCategoriaRequest`) |
| `springdoc-openapi-starter-webmvc-ui:2.8.5` | Genera automáticamente la especificación OpenAPI 3.0 a partir de las anotaciones y provee Swagger UI en `/swagger-ui.html` |

#### 2.2.2 Bean de Configuración OpenAPI (OpenApiConfig.java)

Se creó la clase `OpenApiConfig` en el paquete `com.minimarket.config` para personalizar la información mostrada en Swagger UI:

```java
package com.minimarket.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI minimarketOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Minimarket API")
                        .description("API REST para la gestión de un Minimarket. " +
                                     "Documentación completa de endpoints, modelos " +
                                     "y ejemplos con OpenAPI y HATEOAS.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Desarrollo Backend II")
                                .email("contacto@minimarket.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")));
    }
}
```

**Elementos configurados en el bean:**
- **title**: "Minimarket API" — nombre visible de la API
- **description**: Descripción del propósito del sistema
- **version**: "1.0.0" — versionado semántico
- **contact**: Nombre del equipo y correo de contacto
- **license**: Apache 2.0 — licencia de uso

#### 2.2.3 Ajustes de Seguridad (SecurityConfig.java)

Se modificó la configuración de seguridad para permitir el acceso público a Swagger UI y a la especificación OpenAPI, sin comprometer la protección de los endpoints de negocio:

```java
// SecurityConfig.java — rutas públicas habilitadas
.authorizeHttpRequests(auth -> auth
        .requestMatchers("/public/**").permitAll()
        .requestMatchers("/swagger-ui.html",
                         "/swagger-ui/**",
                         "/v3/api-docs/**").permitAll()
        .anyRequest().authenticated()
)
```

**Rutas habilitadas sin autenticación:**
- `/swagger-ui.html` — página principal de Swagger UI
- `/swagger-ui/**` — recursos estáticos de Swagger UI (CSS, JS)
- `/v3/api-docs/**` — especificación OpenAPI en formato JSON

#### 2.2.4 Ajuste de Encoding (application.properties)

Se encontró y corrigió un error `MalformedInputException` causado por caracteres especiales (acentos, ñ) en los comentarios del código. La solución fue asegurar que el archivo `application.properties` y los archivos fuente usaran UTF-8:

```properties
spring.application.name=minimarket
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.h2.console.enabled=true

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect
```

### 2.3 Verificación de la configuración

- [x] Dependencias agregadas al `pom.xml`
- [x] Bean `OpenApiConfig` se carga al iniciar la aplicación
- [x] `/v3/api-docs` responde con la especificación OpenAPI JSON
- [x] Swagger UI accesible en `http://localhost:8080/swagger-ui.html`
- [x] Endpoints de negocio siguen protegidos por autenticación
- [x] Compilación exitosa (55 source files, BUILD SUCCESS)
- [x] Tests unitarios pasan (Tests run: 4, Failures: 0, Errors: 0)

---

## Pregunta 3

**Implementa HATEOAS con enlaces dinámicos — 20 puntos**

### 3.1 Objetivo

Transformar las respuestas REST del sistema Minimarket para que incluyan enlaces HATEOAS dinámicos, permitiendo que los clientes naveguen la API de forma autodescriptiva sin necesidad de conocer las URLs de antemano.

### 3.2 Desarrollo

#### 3.2.1 ModelAssemblers creados

Se implementaron 7 clases `ModelAssembler`, una por cada entidad del sistema, todas implementando la interfaz `RepresentationModelAssembler<T, EntityModel<T>>` de Spring HATEOAS:

**ProductoModelAssembler** — 7 enlaces dinámicos:

```java
@Component
public class ProductoModelAssembler
        implements RepresentationModelAssembler<Producto, EntityModel<Producto>> {

    @Override
    public EntityModel<Producto> toModel(Producto producto) {
        return EntityModel.of(producto,
                linkTo(methodOn(ProductoController.class)
                        .obtenerProductoPorId(producto.getId())).withSelfRel(),
                linkTo(methodOn(ProductoController.class)
                        .listarProductos()).withRel("listar"),
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

**CategoriaModelAssembler** — 5 enlaces dinámicos:

```java
@Component
public class CategoriaModelAssembler
        implements RepresentationModelAssembler<Categoria, EntityModel<Categoria>> {

    @Override
    public EntityModel<Categoria> toModel(Categoria categoria) {
        return EntityModel.of(categoria,
                linkTo(methodOn(CategoriaController.class)
                        .obtenerCategoriaPorId(categoria.getId())).withSelfRel(),
                linkTo(methodOn(CategoriaController.class)
                        .listarCategorias()).withRel("listar"),
                linkTo(methodOn(CategoriaController.class)
                        .guardarCategoria(null)).withRel("crear"),
                linkTo(methodOn(CategoriaController.class)
                        .actualizarCategoria(categoria.getId(), null)).withRel("editar"),
                linkTo(methodOn(CategoriaController.class)
                        .eliminarCategoria(categoria.getId())).withRel("eliminar")
        );
    }
}
```

**CarritoModelAssembler** — 7 enlaces (incluye relaciones con Usuario y Producto):

```java
@Component
public class CarritoModelAssembler
        implements RepresentationModelAssembler<Carrito, EntityModel<Carrito>> {

    @Override
    public EntityModel<Carrito> toModel(Carrito carrito) {
        return EntityModel.of(carrito,
                linkTo(methodOn(CarritoController.class)
                        .obtenerCarritoPorId(carrito.getId())).withSelfRel(),
                linkTo(methodOn(CarritoController.class)
                        .listarCarrito()).withRel("listar"),
                linkTo(methodOn(CarritoController.class)
                        .agregarProductoAlCarrito(null)).withRel("crear"),
                linkTo(methodOn(CarritoController.class)
                        .actualizarCarrito(carrito.getId(), null)).withRel("editar"),
                linkTo(methodOn(CarritoController.class)
                        .eliminarProductoDelCarrito(carrito.getId())).withRel("eliminar"),
                linkTo(methodOn(UsuarioController.class)
                        .obtenerUsuarioPorId(carrito.getUsuario().getId())).withRel("usuario"),
                linkTo(methodOn(ProductoController.class)
                        .obtenerProductoPorId(carrito.getProducto().getId())).withRel("producto")
        );
    }
}
```

**InventarioModelAssembler** — 6 enlaces (incluye relación con Producto):

```java
@Component
public class InventarioModelAssembler
        implements RepresentationModelAssembler<Inventario, EntityModel<Inventario>> {

    @Override
    public EntityModel<Inventario> toModel(Inventario inventario) {
        return EntityModel.of(inventario,
                linkTo(methodOn(InventarioController.class)
                        .obtenerMovimientoPorId(inventario.getId())).withSelfRel(),
                linkTo(methodOn(InventarioController.class)
                        .listarMovimientosDeInventario()).withRel("listar"),
                linkTo(methodOn(InventarioController.class)
                        .registrarMovimiento(null)).withRel("crear"),
                linkTo(methodOn(InventarioController.class)
                        .actualizarMovimiento(inventario.getId(), null)).withRel("editar"),
                linkTo(methodOn(InventarioController.class)
                        .eliminarMovimiento(inventario.getId())).withRel("eliminar"),
                linkTo(methodOn(ProductoController.class)
                        .obtenerProductoPorId(inventario.getProducto().getId()))
                        .withRel("producto")
        );
    }
}
```

**UsuarioModelAssembler** — 5 enlaces:

```java
@Component
public class UsuarioModelAssembler
        implements RepresentationModelAssembler<Usuario, EntityModel<Usuario>> {

    @Override
    public EntityModel<Usuario> toModel(Usuario usuario) {
        return EntityModel.of(usuario,
                linkTo(methodOn(UsuarioController.class)
                        .obtenerUsuarioPorId(usuario.getId())).withSelfRel(),
                linkTo(methodOn(UsuarioController.class)
                        .listarUsuarios()).withRel("listar"),
                linkTo(methodOn(UsuarioController.class)
                        .guardarUsuario(null)).withRel("crear"),
                linkTo(methodOn(UsuarioController.class)
                        .actualizarUsuario(usuario.getId(), null)).withRel("editar"),
                linkTo(methodOn(UsuarioController.class)
                        .eliminarUsuario(usuario.getId())).withRel("eliminar")
        );
    }
}
```

**VentaModelAssembler** — 4 enlaces (incluye relación con Usuario):

```java
@Component
public class VentaModelAssembler
        implements RepresentationModelAssembler<Venta, EntityModel<Venta>> {

    @Override
    public EntityModel<Venta> toModel(Venta venta) {
        return EntityModel.of(venta,
                linkTo(methodOn(VentaController.class)
                        .obtenerVentaPorId(venta.getId())).withSelfRel(),
                linkTo(methodOn(VentaController.class)
                        .listarVentas()).withRel("listar"),
                linkTo(methodOn(VentaController.class)
                        .guardarVenta(null)).withRel("crear"),
                linkTo(methodOn(UsuarioController.class)
                        .obtenerUsuarioPorId(venta.getUsuario().getId())).withRel("usuario")
        );
    }
}
```

**DetalleVentaModelAssembler** — 7 enlaces (incluye relaciones con Venta y Producto):

```java
@Component
public class DetalleVentaModelAssembler
        implements RepresentationModelAssembler<DetalleVenta, EntityModel<DetalleVenta>> {

    @Override
    public EntityModel<DetalleVenta> toModel(DetalleVenta detalleVenta) {
        return EntityModel.of(detalleVenta,
                linkTo(methodOn(DetalleVentaController.class)
                        .obtenerDetalleVentaPorId(detalleVenta.getId())).withSelfRel(),
                linkTo(methodOn(DetalleVentaController.class)
                        .listarDetalleVentas()).withRel("listar"),
                linkTo(methodOn(DetalleVentaController.class)
                        .guardarDetalleVenta(null)).withRel("crear"),
                linkTo(methodOn(DetalleVentaController.class)
                        .actualizarDetalleVenta(detalleVenta.getId(), null)).withRel("editar"),
                linkTo(methodOn(DetalleVentaController.class)
                        .eliminarDetalleVenta(detalleVenta.getId())).withRel("eliminar"),
                linkTo(methodOn(VentaController.class)
                        .obtenerVentaPorId(detalleVenta.getVenta().getId())).withRel("venta"),
                linkTo(methodOn(ProductoController.class)
                        .obtenerProductoPorId(detalleVenta.getProducto().getId()))
                        .withRel("producto")
        );
    }
}
```

#### 3.2.2 Estructura de enlaces por recurso

| Recurso | Enlaces incluidos |
|---|---|
| Producto | self, listar, crear, editar, eliminar, categoria, asignar-categoria |
| Categoría | self, listar, crear, editar, eliminar |
| Carrito | self, listar, crear, editar, eliminar, usuario, producto |
| Inventario | self, listar, crear, editar, eliminar, producto |
| Usuario | self, listar, crear, editar, eliminar |
| Venta | self, listar, crear, usuario |
| DetalleVenta | self, listar, crear, editar, eliminar, venta, producto |

#### 3.2.3 Ejemplo de respuesta JSON con HATEOAS

Al consultar `GET /api/productos/1`, la respuesta ahora incluye los enlaces HATEOAS:

```json
{
  "id": 1,
  "nombre": "Leche Entera 1L",
  "precio": 1200,
  "stock": 50,
  "_links": {
    "self": {
      "href": "http://localhost:8080/api/productos/1"
    },
    "listar": {
      "href": "http://localhost:8080/api/productos"
    },
    "crear": {
      "href": "http://localhost:8080/api/productos"
    },
    "editar": {
      "href": "http://localhost:8080/api/productos/1"
    },
    "eliminar": {
      "href": "http://localhost:8080/api/productos/1"
    },
    "categoria": {
      "href": "http://localhost:8080/api/productos/1/categoria"
    },
    "asignar-categoria": {
      "href": "http://localhost:8080/api/productos/1/categoria"
    }
  }
}
```

#### 3.2.4 Subrecurso especial: Categoría desde Producto

Se implementaron dos endpoints adicionales para navegar la relación entre Producto y su Categoría:

**GET /api/productos/{id}/categoria** — Obtiene la categoría del producto con enlaces HATEOAS:

```java
@GetMapping("/{id}/categoria")
@Operation(summary = "Obtener categoría de un producto",
           description = "Retorna la categoría asociada a un producto")
@ApiResponses({
    @ApiResponse(responseCode = "200", description = "Categoría encontrada"),
    @ApiResponse(responseCode = "404", description = "Producto no encontrado o sin categoría")
})
public ResponseEntity<EntityModel<Categoria>> obtenerCategoria(
        @Parameter(description = "ID del producto", required = true) @PathVariable Long id) {
    Producto producto = productoService.findById(id);
    if (producto == null || producto.getCategoria() == null)
        return ResponseEntity.notFound().build();
    return ResponseEntity.ok(categoriaModelAssembler.toModel(producto.getCategoria()));
}
```

**POST /api/productos/{id}/categoria** — Asigna una categoría existente al producto:

```java
@PostMapping("/{id}/categoria")
@Operation(summary = "Asignar categoría a un producto",
           description = "Asigna una categoría existente a un producto")
@ApiResponses({
    @ApiResponse(responseCode = "200", description = "Categoría asignada correctamente"),
    @ApiResponse(responseCode = "404", description = "Producto o categoría no encontrados"),
    @ApiResponse(responseCode = "400", description = "Datos inválidos")
})
public ResponseEntity<EntityModel<Producto>> asignarCategoria(
        @Parameter(description = "ID del producto", required = true) @PathVariable Long id,
        @Valid @RequestBody AsignarCategoriaRequest request) {
    Producto producto = productoService.asignarCategoria(id, request.getCategoriaId());
    if (producto == null) return ResponseEntity.notFound().build();
    return ResponseEntity.ok(productoModelAssembler.toModel(producto));
}
```

### 3.3 Verificación

- [x] 7 assemblers implementados con `RepresentationModelAssembler`
- [x] 29 endpoints retornan `EntityModel`/`CollectionModel`
- [x] Enlaces son dinámicos (basados en el ID del recurso)
- [x] Uso consistente de `linkTo(methodOn(Controller.class))`
- [x] `toCollectionModel()` heredado automáticamente de la interfaz
- [x] Respuestas JSON incluyen `_links` con href y rel

---

## Pregunta 4

**Genera y valida documentación en Swagger UI — 20 puntos**

### 4.1 Objetivo

Verificar que la documentación OpenAPI es accesible a través de Swagger UI, que todos los endpoints son visibles y funcionales, y que la interfaz permite probar las operaciones CRUD del sistema.

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

#### 4.2.2 Estructura de Swagger UI

Al cargar Swagger UI, se observan los siguientes elementos:

1. **Encabezado personalizado**: Muestra el título "Minimarket API" y la descripción configurada en OpenApiConfig
2. **7 secciones de tags**: Productos, Categorías, Carrito, Inventario, Usuarios, Ventas, Detalle de Ventas
3. **35 operaciones documentadas**: Cada una con su método HTTP, ruta, resumen y descripción
4. **Modelos de datos**: Los schemas de Producto, Categoria, Carrito, etc., visibles en la sección "Schemas"
5. **Botón "Try it out"**: Permite ejecutar peticiones directamente desde el navegador

#### 4.2.3 Códigos HTTP verificados

| Método HTTP | Código esperado | Contexto |
|---|---|---|
| GET | 200 OK | Recurso encontrado |
| POST | 201 Created | Recurso creado exitosamente |
| PUT | 200 OK | Recurso actualizado exitosamente |
| DELETE | 204 No Content | Recurso eliminado exitosamente |
| GET/POST/PUT | 400 Bad Request | Datos de entrada inválidos |
| GET/PUT/DELETE | 404 Not Found | Recurso no encontrado |

#### 4.2.4 Pruebas realizadas desde Swagger UI

Se probaron los siguientes escenarios desde Swagger UI:

1. **GET /api/productos** — Lista vacía inicial (200 OK)
2. **POST /api/productos** — Crear producto (201 Created)
3. **GET /api/productos/{id}** — Obtener producto creado (200 OK con _links)
4. **PUT /api/productos/{id}** — Actualizar producto (200 OK)
5. **DELETE /api/productos/{id}** — Eliminar producto (204 No Content)
6. **GET /api/productos/{id}** — Producto eliminado (404 Not Found)
7. **GET /api/productos/{id}/categoria** — Categoría de producto (200 OK o 404)
8. **POST /api/productos/{id}/categoria** — Asignar categoría (200 OK)

#### 4.2.5 Resultados de compilación y tests

```bash
./mvnw clean compile   # BUILD SUCCESS (55 source files)
./mvnw clean test      # Tests run: 4, Failures: 0, Errors: 0, Skipped: 0
```

### 4.3 Evidencias

- **Swagger UI** visualiza todos los endpoints agrupados por tags
- **Modelos** aparecen con sus campos y tipos de datos
- **Parámetros de ruta** documentados con descripciones
- **Códigos de respuesta** especificados para cada operación
- **Ejemplos de respuesta** visibles al expandir cada endpoint

---

## Pregunta 5

**Analiza y reflexiona sobre resultados — 10 puntos**

### 5.1 Objetivo

Analizar el impacto técnico de implementar OpenAPI y HATEOAS en el sistema Minimarket, reflexionando sobre cómo estas tecnologías afectan la calidad del software, la mantenibilidad, la navegabilidad y la escalabilidad del sistema.

### 5.2 Análisis comparativo antes y después

#### 5.2.1 Comparación de código: Controlador ProductoController

**Antes (sin OpenAPI ni HATEOAS):**

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
}
```

**Después (con OpenAPI y HATEOAS):**

```java
@RestController
@RequestMapping("/api/productos")
@Tag(name = "Productos", description = "Operaciones CRUD para la gestión de productos")
public class ProductoController {

    @Autowired
    private ProductoService productoService;
    @Autowired
    private ProductoModelAssembler productoModelAssembler;

    @GetMapping
    @Operation(summary = "Listar todos los productos",
               description = "Retorna una lista con todos los productos registrados")
    @ApiResponses({
        @ApiResponse(responseCode = "200",
                     description = "Lista de productos obtenida correctamente")
    })
    public ResponseEntity<CollectionModel<EntityModel<Producto>>> listarProductos() {
        return ResponseEntity.ok(
                productoModelAssembler.toCollectionModel(productoService.findAll()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener un producto por ID",
               description = "Retorna un producto según su ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Producto encontrado"),
        @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    public ResponseEntity<EntityModel<Producto>> obtenerProductoPorId(
            @Parameter(description = "ID del producto", required = true) @PathVariable Long id) {
        Producto producto = productoService.findById(id);
        if (producto == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(productoModelAssembler.toModel(producto));
    }
}
```

**Diferencias clave:**

| Aspecto | Antes | Después |
|---|---|---|
| Tipo de retorno GET lista | `ResponseEntity<List<Producto>>` | `ResponseEntity<CollectionModel<EntityModel<Producto>>>` |
| Tipo de retorno GET individual | `ResponseEntity<Producto>` | `ResponseEntity<EntityModel<Producto>>` |
| Documentación de endpoints | Ausente | `@Tag`, `@Operation`, `@ApiResponses` |
| Enlaces en respuesta | No | `_links` con self, listar, crear, editar, eliminar, etc. |
| Dependencias externas | Solo Spring Web | + spring-boot-starter-hateoas, springdoc-openapi |
| Códigos HTTP documentados | No | 200, 201, 204, 400, 404 |

#### 5.2.2 Comparación de respuestas JSON

**Antes — Respuesta GET /api/productos:**
```json
[
  {
    "id": 1,
    "nombre": "Leche Entera 1L",
    "precio": 1200,
    "stock": 50,
    "categoria": null
  }
]
```

**Después — Respuesta GET /api/productos con HATEOAS:**
```json
{
  "_embedded": {
    "productoList": [
      {
        "id": 1,
        "nombre": "Leche Entera 1L",
        "precio": 1200,
        "stock": 50,
        "_links": {
          "self": { "href": "http://localhost:8080/api/productos/1" },
          "listar": { "href": "http://localhost:8080/api/productos" },
          "crear": { "href": "http://localhost:8080/api/productos" },
          "editar": { "href": "http://localhost:8080/api/productos/1" },
          "eliminar": { "href": "http://localhost:8080/api/productos/1" },
          "categoria": { "href": "http://localhost:8080/api/productos/1/categoria" },
          "asignar-categoria": { "href": "http://localhost:8080/api/productos/1/categoria" }
        }
      }
    ]
  },
  "_links": {
    "self": { "href": "http://localhost:8080/api/productos" }
  }
}
```

### 5.3 Reflexión técnica

#### 5.3.1 Impacto en la mantenibilidad del código

La implementación de OpenAPI tiene un impacto profundamente positivo en la mantenibilidad del sistema por las siguientes razones:

1. **Documentación viva**: La documentación ya no es un documento separado que debe actualizarse manualmente. Con OpenAPI, la documentación vive en el código fuente a través de anotaciones (`@Operation`, `@ApiResponses`, `@Tag`). Cuando un desarrollador modifica un endpoint, la documentación se actualiza automáticamente en la siguiente compilación. Esto elimina el problema clásico de la documentación desactualizada.

2. **Claridad del contrato**: Cada endpoint ahora especifica explícitamente:
   - Qué hace (summary + description)
   - Qué parámetros espera (@Parameter)
   - Qué códigos HTTP puede retornar (@ApiResponses)
   - Qué modelo de datos utiliza (@Schema)
   Esto convierte a los controladores en especificaciones vivas del contrato API.

3. **Reducción de la deuda técnica**: Antes, un desarrollador nuevo debía leer el código de cada controlador para entender su funcionamiento. Ahora, con solo abrir Swagger UI, cualquier miembro del equipo comprende el 100% de la superficie de la API.

4. **Desacoplamiento de URLs con HATEOAS**: La implementación de los ModelAssembler centraliza la construcción de URLs. Si una ruta cambia (por ejemplo, de `/api/productos` a `/api/v1/productos`), solo se modifica el assembler, no cada controlador ni los clientes que siguen los enlaces.

#### 5.3.2 Impacto en la navegabilidad

Antes de HATEOAS, la navegación de la API dependía completamente de que el cliente conociera las URLs específicas de antemano. Esto creaba un acoplamiento fuerte entre el cliente y la estructura de rutas del servidor.

Con HATEOAS, la navegabilidad mejora significativamente:

1. **Descubrimiento de recursos**: Un cliente puede comenzar con la URL base de la API y descubrir todos los recursos disponibles siguiendo los enlaces en las respuestas. Por ejemplo, al obtener un producto, el cliente descubre que puede navegar a su categoría a través del enlace `categoria`. No necesita saber que la URL es `/api/productos/{id}/categoria`.

2. **Relaciones entre recursos**: Los enlaces como `usuario` en CarritoModelAssembler o `producto` en InventarioModelAssembler permiten navegar naturalmente entre entidades relacionadas. Esto refleja la naturaleza relacional de los datos en la propia API.

3. **Subrecursos**: La implementación de `GET /api/productos/{id}/categoria` como subrecurso ejemplifica el patrón correcto de navegación HATEOAS: desde el recurso Producto se accede al recurso relacionado Categoría.

4. **Independencia del cliente**: Un cliente bien implementado que sigue enlaces HATEOAS no se rompe cuando cambian las URLs del servidor. Esto es un principio fundamental de REST de nivel 3 (the Richardson Maturity Model).

#### 5.3.3 Impacto en la escalabilidad

1. **Estandarización OpenAPI**: La especificación OAS generada automáticamente permite:
   - Integración con herramientas de generación de clientes (OpenAPI Generator)
   - Pruebas automatizadas de contrato (contract testing)
   - Documentación para integradores externos sin acceso al código fuente
   - Versionado semántico de la API a través de la versión en OpenApiConfig

2. **Preparación para microservicios**: Aunque Minimarket es un monolito, las prácticas aquí implementadas —documentación OAS, HATEOAS, validación — son fundamentales en arquitecturas de microservicios donde múltiples servicios deben interoperar.

3. **Swagger UI como herramienta de testing**: Swagger UI permite a cualquier miembro del equipo (backend, frontend, QA) probar endpoints sin necesidad de instalar herramientas adicionales como Postman ni de escribir código de prueba ad-hoc.

#### 5.3.4 Desafíos y consideraciones

1. **Complejidad inicial**: La implementación de HATEOAS agrega una capa adicional de abstracción (los ModelAssembler) que aumenta la cantidad de archivos y la complejidad de la respuesta JSON. Para una API pequeña, esto puede ser sobreingeniería.

2. **Documentación extra**: Las anotaciones de OpenAPI, aunque automatizan la documentación, representan líneas adicionales de código que deben ser mantenidas. Sin embargo, este costo inicial es ampliamente compensado por los beneficios.

3. **Curva de aprendizaje**: Tanto OpenAPI como HATEOAS requieren que el equipo de desarrollo comprenda conceptos nuevos como OAS, HAL, RepresentationModelAssembler y WebMvcLinkBuilder.

### 5.4 Conclusión

La implementación de OpenAPI y HATEOAS en el sistema Minimarket representa un salto cualitativo en la madurez de la API. Al pasar de un diseño REST de Nivel 2 (Richardson Maturity Model) a uno de Nivel 3, el sistema se vuelve:

- **Más mantenible**: la documentación vive en el código
- **Más navegable**: los clientes descubren la API mediante enlaces
- **Más escalable**: preparado para integraciones y microservicios
- **Más profesional**: cumple con estándares de la industria (OpenAPI 3.0)

Estas mejoras tienen un costo de implementación moderado (creación de assemblers y anotaciones) pero un retorno de inversión alto en términos de reducción de consultas entre equipos, menor tiempo de incorporación de nuevos desarrolladores y mayor calidad del contrato API.

---

## Pregunta 6

**Documenta proceso y sube a GitHub — 10 puntos**

### 6.1 Objetivo

Documentar el proceso completo de implementación, actualizar el README con la informacion del proyecto y subir el codigo a GitHub con una estructura clara y organizada.

### 6.2 Desarrollo

#### 6.2.1 Estructura del proyecto

```
Experiencia3/
├── contexto-sesion.md                   # Resumen ejecutivo del sprint
├── planificacion.md                     # Plan de desarrollo metodologico
├── planificacion-codigo.md              # Plan tecnico paso a paso
├── desarrollo-preguntas.md              # Respuestas al formulario
├── Semana7/                             # PDFs de la semana 7
│   ├── Guia de aprendizaje
│   ├── Pauta de evaluacion
│   └── Formato de respuesta
├── Semana8/                             # PDFs de la semana 8
│   ├── Guia de aprendizaje
│   ├── Pauta de evaluacion
│   └── Formato de respuesta
└── minimarket/                          # Proyecto Spring Boot
    ├── pom.xml
    └── src/
        └── main/java/com/minimarket/
            ├── MinimarketApplication.java
            ├── config/
            │   └── OpenApiConfig.java
            ├── dto/
            │   └── AsignarCategoriaRequest.java
            ├── assembler/
            │   ├── ProductoModelAssembler.java
            │   ├── CategoriaModelAssembler.java
            │   ├── CarritoModelAssembler.java
            │   ├── InventarioModelAssembler.java
            │   ├── UsuarioModelAssembler.java
            │   ├── VentaModelAssembler.java
            │   └── DetalleVentaModelAssembler.java
            ├── controller/
            │   ├── ProductoController.java
            │   ├── CategoriaController.java
            │   ├── CarritoController.java
            │   ├── InventarioController.java
            │   ├── UsuarioController.java
            │   ├── VentaController.java
            │   ├── DetalleVentaController.java
            │   └── HolaMundoController.java
            ├── service/
            │   ├── ProductoService.java
            │   └── impl/ProductoServiceImpl.java
            ├── entity/
            ├── repository/
            └── security/config/SecurityConfig.java
```

#### 6.2.2 Nuevos archivos creados (10)

| Archivo | Proposito |
|---|---|
| `config/OpenApiConfig.java` | Bean OpenAPI con titulo, descripcion, version 1.0.0, contacto, licencia Apache 2.0 |
| `dto/AsignarCategoriaRequest.java` | DTO con `@NotNull Long categoriaId` + `@Schema` para Swagger |
| `assembler/ProductoModelAssembler.java` | 7 enlaces HATEOAS (self, listar, crear, editar, eliminar, categoria, asignar-categoria) |
| `assembler/CategoriaModelAssembler.java` | 5 enlaces (self, listar, crear, editar, eliminar) |
| `assembler/CarritoModelAssembler.java` | 7 enlaces (self, listar, crear, editar, eliminar, usuario, producto) |
| `assembler/InventarioModelAssembler.java` | 6 enlaces (self, listar, crear, editar, eliminar, producto) |
| `assembler/UsuarioModelAssembler.java` | 5 enlaces (self, listar, crear, editar, eliminar) |
| `assembler/VentaModelAssembler.java` | 4 enlaces (self, listar, crear, usuario) |
| `assembler/DetalleVentaModelAssembler.java` | 7 enlaces (self, listar, crear, editar, eliminar, venta, producto) |

#### 6.2.3 Archivos modificados (11)

| Archivo | Cambio |
|---|---|
| `pom.xml` | + `spring-boot-starter-hateoas`, + `springdoc-openapi-starter-webmvc-ui:2.8.5`, + `spring-boot-starter-validation` |
| `SecurityConfig.java` | + rutas publicas `/swagger-ui.html`, `/swagger-ui/**`, `/v3/api-docs/**` |
| `application.properties` | Encoding ISO-8859-1 a UTF-8 |
| `ProductoService.java` | + metodo `asignarCategoria(Long productoId, Long categoriaId)` |
| `ProductoServiceImpl.java` | + implementacion con inyeccion de `CategoriaRepository` |
| `ProductoController.java` | + `@Tag`, `@Operation`, `@ApiResponses`, `EntityModel`/`CollectionModel`, subrecursos |
| `CategoriaController.java` | + OpenAPI + HATEOAS |
| `CarritoController.java` | + OpenAPI + HATEOAS |
| `InventarioController.java` | + OpenAPI + HATEOAS |
| `UsuarioController.java` | + OpenAPI + HATEOAS |
| `VentaController.java` | + OpenAPI + HATEOAS |
| `DetalleVentaController.java` | + OpenAPI + HATEOAS |

#### 6.2.4 README.md

```markdown
# Minimarket Plus - Sistema de Gestion de Minimarket

## Descripcion

API REST desarrollada con **Spring Boot 3.4.1** y **JDK 17** para la gestion de un minimarket.
El sistema implementa operaciones CRUD para productos, categorias, carrito de compras,
inventario, usuarios, ventas y detalle de ventas, utilizando **JPA** con base de datos **H2**
en memoria y **Spring Security** para la autenticacion.

## Tecnologias Utilizadas

| Tecnologia | Version |
|---|---|
| Spring Boot | 3.4.1 |
| JDK | 17 |
| Spring Data JPA | - |
| Spring Security | - |
| Spring HATEOAS | - |
| SpringDoc OpenAPI | 2.8.5 |
| H2 Database | - |
| Lombok | - |
| Maven | - |

## Dependencias Clave

```xml
<!-- HATEOAS para enlaces dinamicos -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-hateoas</artifactId>
</dependency>

<!-- OpenAPI / Swagger UI -->
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.8.5</version>
</dependency>

<!-- Validacion de datos -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```

## Como compilar y ejecutar

```bash
export JAVA_HOME="$HOME/jdk17"
export PATH="$JAVA_HOME/bin:$PATH"
cd minimarket/
./mvnw clean compile   # Compilar
./mvnw clean test        # Ejecutar tests
./mvnw spring-boot:run # Iniciar servidor
```

## Acceso a la documentacion

Una vez ejecutando la aplicacion, abrir en el navegador:

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **Especificacion OAS**: http://localhost:8080/v3/api-docs

## Endpoints de la API

### Productos
| Metodo | Ruta | Descripcion |
|---|---|---|
| GET | /api/productos | Listar todos los productos |
| GET | /api/productos/{id} | Obtener producto por ID |
| POST | /api/productos | Crear producto |
| PUT | /api/productos/{id} | Actualizar producto |
| DELETE | /api/productos/{id} | Eliminar producto |
| GET | /api/productos/{id}/categoria | Obtener categoria del producto |
| POST | /api/productos/{id}/categoria | Asignar categoria al producto |

### Categorias
| Metodo | Ruta | Descripcion |
|---|---|---|
| GET | /api/categorias | Listar categorias |
| GET | /api/categorias/{id} | Obtener categoria por ID |
| POST | /api/categorias | Crear categoria |
| PUT | /api/categorias/{id} | Actualizar categoria |
| DELETE | /api/categorias/{id} | Eliminar categoria |

### Carrito
| Metodo | Ruta | Descripcion |
|---|---|---|
| GET | /api/carrito | Listar carrito |
| GET | /api/carrito/{id} | Obtener item por ID |
| POST | /api/carrito | Agregar producto |
| PUT | /api/carrito/{id} | Actualizar item |
| DELETE | /api/carrito/{id} | Eliminar item |

### Inventario
| Metodo | Ruta | Descripcion |
|---|---|---|
| GET | /api/inventario | Listar movimientos |
| GET | /api/inventario/{id} | Obtener movimiento por ID |
| POST | /api/inventario | Registrar movimiento |
| PUT | /api/inventario/{id} | Actualizar movimiento |
| DELETE | /api/inventario/{id} | Eliminar movimiento |

### Usuarios
| Metodo | Ruta | Descripcion |
|---|---|---|
| GET | /api/usuarios | Listar usuarios |
| GET | /api/usuarios/{id} | Obtener usuario por ID |
| POST | /api/usuarios | Crear usuario |
| PUT | /api/usuarios/{id} | Actualizar usuario |
| DELETE | /api/usuarios/{id} | Eliminar usuario |

### Ventas
| Metodo | Ruta | Descripcion |
|---|---|---|
| GET | /api/ventas | Listar ventas |
| GET | /api/ventas/{id} | Obtener venta por ID |
| POST | /api/ventas | Registrar venta |

### Detalle de Ventas
| Metodo | Ruta | Descripcion |
|---|---|---|
| GET | /api/detalle-ventas | Listar detalles |
| GET | /api/detalle-ventas/{id} | Obtener detalle por ID |
| POST | /api/detalle-ventas | Crear detalle |
| PUT | /api/detalle-ventas/{id} | Actualizar detalle |
| DELETE | /api/detalle-ventas/{id} | Eliminar detalle |

## HATEOAS

Todas las respuestas incluyen enlaces `_links` que permiten navegar entre recursos
relacionados. Por ejemplo, al obtener un producto:

```json
{
  "id": 1,
  "nombre": "Leche Entera 1L",
  "precio": 1200,
  "stock": 50,
  "_links": {
    "self": { "href": "http://localhost:8080/api/productos/1" },
    "listar": { "href": "http://localhost:8080/api/productos" },
    "crear": { "href": "http://localhost:8080/api/productos" },
    "editar": { "href": "http://localhost:8080/api/productos/1" },
    "eliminar": { "href": "http://localhost:8080/api/productos/1" },
    "categoria": { "href": "http://localhost:8080/api/productos/1/categoria" },
    "asignar-categoria": { "href": "http://localhost:8080/api/productos/1/categoria" }
  }
}
```

## Beneficios de OpenAPI y HATEOAS

### OpenAPI
- Documentacion viva que se actualiza automaticamente con el codigo
- Interfaz grafica interactiva (Swagger UI) para probar los endpoints
- Especificacion estandarizada que permite integracion con herramientas externas
- Versionado semantico de la API

### HATEOAS
- Clientes descubren la API navegando enlaces en lugar de URLs hardcodeadas
- Desacoplamiento entre cliente y servidor: cambiar rutas no rompe clientes
- Relaciones entre recursos visibles en las propias respuestas
- APIs autodescriptivas y auto-contenidas

## Evidencias

(Incluir capturas de Swagger UI, ejemplos de respuestas JSON con _links
y resultados de compilacion/tests)
```

#### 6.2.5 Comandos de compilacion y verificacion

```bash
# Compilar el proyecto (55 source files)
./mvnw clean compile  # BUILD SUCCESS

# Ejecutar tests unitarios (4 tests)
./mvnw clean test     # Tests run: 4, Failures: 0, Errors: 0

# Iniciar la aplicacion
./mvnw spring-boot:run

# Abrir en el navegador:
# http://localhost:8080/swagger-ui.html
```

### 6.3 Checklist de entrega

- [x] Proyecto compila sin errores (BUILD SUCCESS)
- [x] Tests unitarios pasan (4/4 exitosos)
- [x] Swagger UI accesible en `http://localhost:8080/swagger-ui.html`
- [x] 35 endpoints documentados con OpenAPI
- [x] 7 assemblers implementados con HATEOAS
- [x] Subrecurso categoria funcionando (GET y POST)
- [x] Analisis y reflexion sobre resultados incluidos
- [x] README actualizado con toda la informacion del proyecto
- [x] Estructura de carpetas organizada
- [x] Codigo subido a GitHub