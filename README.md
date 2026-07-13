# Minimarket Plus - Sistema de Gestion de Minimarket

## Descripcion

API REST desarrollada con **Spring Boot 3.4.1** y **JDK 17** para la gestion de un minimarket.
El sistema implementa operaciones CRUD completas para productos, categorias, carrito de compras,
inventario, usuarios, ventas y detalle de ventas, utilizando **JPA** con base de datos **H2**
en memoria y **Spring Security con JWT** para la autenticacion.

Esta version incorpora **OpenAPI 3.0** para la documentacion automatica de endpoints,
**Spring HATEOAS** con **PagedModel** para paginacion y navegabilidad entre recursos,
**DTOs** para proteger datos sensibles, **Manejo global de excepciones**, y
**Bean Validation** para validacion de datos de entrada.

Trabajo realizado por : Catalina Cabezas, Andrea Rosero, Nicolas Cavieres

---

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
| JWT (jjwt) | 0.11.5 |
| Jsoup | 1.22.2 |
| JaCoCo | 0.8.12 |
| Lombok | - |
| Maven | - |

---

## Dependencias Clave

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-hateoas</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.8.5</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.11.5</version>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
<dependency>
    <groupId>org.jsoup</groupId>
    <artifactId>jsoup</artifactId>
    <version>1.22.2</version>
</dependency>
```

---

## Como compilar y ejecutar

```bash
export JAVA_HOME="$HOME/jdk17"
export PATH="$JAVA_HOME/bin:$PATH"
cd minimarket/

# Compilar el proyecto
./mvnw clean compile

# Ejecutar tests unitarios
./mvnw clean test

# Iniciar el servidor
./mvnw spring-boot:run
```

---

## Acceso a la documentacion

Una vez ejecutando la aplicacion, abrir en el navegador:

| Recurso | URL |
|---|---|
| Swagger UI | http://localhost:8080/swagger-ui.html |
| Especificacion OpenAPI (JSON) | http://localhost:8080/v3/api-docs |
| Consola H2 | http://localhost:8080/h2-console |
| Actuator Health | http://localhost:8080/actuator/health |
| Actuator Info | http://localhost:8080/actuator/info |

---

## Autenticacion

El sistema utiliza **JWT (JSON Web Token)** para autenticacion stateless.

### Login

```
POST /api/auth/login
Body: { "username": "admin", "password": "admin123" }
Respuesta: { "token": "eyJhbGciOiJIUzI1NiJ9..." }
```

Usar el token en las peticiones subsiguientes con el header:
```
Authorization: Bearer <token>
```

### Datos de prueba (seed)

Al iniciar la aplicacion se crean automaticamente:
- **Roles**: ADMIN, USER
- **Usuario**: admin / admin123
- **Categorias**: Bebidas, Lacteos, Panaderia
- **50 productos** de prueba

---

## Endpoints de la API

Todos los endpoints GET de listado soportan **paginacion y ordenamiento**:

| Parametro | Default | Descripcion |
|---|---|---|
| `page` | 0 | Numero de pagina (0-indexado) |
| `size` | 10 | Elementos por pagina |
| `sortBy` | varia | Campo por el cual ordenar |
| `sortDir` | asc | Direccion: `asc` o `desc` |

### Autenticacion

| Metodo | Ruta | Descripcion |
|---|---|---|
| POST | /api/auth/login | Iniciar sesion (JWT) |

### Productos

| Metodo | Ruta | Descripcion | Codigos HTTP |
|---|---|---|---|
| GET | /api/productos | Listar productos (paginado) | 200 |
| GET | /api/productos/{id} | Obtener producto por ID | 200, 404 |
| POST | /api/productos | Crear producto | 201, 400 |
| PUT | /api/productos/{id} | Actualizar producto | 200, 400, 404 |
| DELETE | /api/productos/{id} | Eliminar producto | 200, 404 |
| GET | /api/productos/{id}/categoria | Obtener categoria del producto | 200, 404 |
| POST | /api/productos/{id}/categoria | Asignar categoria al producto | 200, 400, 404 |

### Categorias

| Metodo | Ruta | Descripcion | Codigos HTTP |
|---|---|---|---|
| GET | /api/categorias | Listar categorias (paginado) | 200 |
| GET | /api/categorias/{id} | Obtener categoria por ID | 200, 404 |
| POST | /api/categorias | Crear categoria | 201, 400 |
| PUT | /api/categorias/{id} | Actualizar categoria | 200, 400, 404 |
| DELETE | /api/categorias/{id} | Eliminar categoria | 200, 404 |

### Carrito

| Metodo | Ruta | Descripcion | Codigos HTTP |
|---|---|---|---|
| GET | /api/carrito | Listar carrito (paginado) | 200 |
| GET | /api/carrito/{id} | Obtener item por ID | 200, 404 |
| POST | /api/carrito | Agregar producto al carrito | 201, 400 |
| PUT | /api/carrito/{id} | Actualizar item | 200, 400, 404 |
| DELETE | /api/carrito/{id} | Eliminar item | 200, 404 |

### Inventario

| Metodo | Ruta | Descripcion | Codigos HTTP |
|---|---|---|---|
| GET | /api/inventario | Listar movimientos de inventario (paginado) | 200 |
| GET | /api/inventario/{id} | Obtener movimiento por ID | 200, 404 |
| POST | /api/inventario | Registrar movimiento | 201, 400 |
| PUT | /api/inventario/{id} | Actualizar movimiento | 200, 400, 404 |
| DELETE | /api/inventario/{id} | Eliminar movimiento | 200, 404 |

### Usuarios

| Metodo | Ruta | Descripcion | Codigos HTTP |
|---|---|---|---|
| GET | /api/usuarios | Listar usuarios (paginado) | 200 |
| GET | /api/usuarios/{id} | Obtener usuario por ID (sin password) | 200, 404 |
| POST | /api/usuarios | Crear usuario | 201, 400 |
| PUT | /api/usuarios/{id} | Actualizar usuario | 200, 400, 404 |
| DELETE | /api/usuarios/{id} | Eliminar usuario | 200, 404 |

### Ventas

| Metodo | Ruta | Descripcion | Codigos HTTP |
|---|---|---|---|
| GET | /api/ventas | Listar ventas (paginado) | 200 |
| GET | /api/ventas/{id} | Obtener venta por ID | 200, 404 |
| POST | /api/ventas | Registrar venta | 201, 400 |

### Detalle de Ventas

| Metodo | Ruta | Descripcion | Codigos HTTP |
|---|---|---|---|
| GET | /api/detalle-ventas | Listar detalles de ventas (paginado) | 200 |
| GET | /api/detalle-ventas/{id} | Obtener detalle por ID | 200, 404 |
| POST | /api/detalle-ventas | Crear detalle de venta | 201, 400 |
| PUT | /api/detalle-ventas/{id} | Actualizar detalle | 200, 400, 404 |
| DELETE | /api/detalle-ventas/{id} | Eliminar detalle | 200, 404 |

---

## HATEOAS

Todas las respuestas incluyen enlaces `_links` que permiten navegar entre recursos relacionados.
Cada recurso expone enlaces a si mismo (self), a la coleccion (listar), a las operaciones
(crear, editar, eliminar) y a sus recursos asociados.

Las respuestas de listado usan `PagedModel` con enlaces de navegacion (`first`, `prev`, `next`, `last`).
Las respuestas DELETE retornan `200 OK` con mensaje de confirmacion y enlaces HATEOAS.

### Ejemplo de respuesta con HATEOAS (Producto individual)

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

### Ejemplo de respuesta DELETE

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

### Ejemplo de respuesta de error

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

---

## Estructura del proyecto

```
minimarket/
├── pom.xml
├── README.md
└── src/main/java/com/minimarket/
    ├── MinimarketApplication.java
    ├── config/
    │   ├── OpenApiConfig.java               # @SecurityScheme JWT en Swagger
    │   └── DataInitializer.java             # Seed de datos de prueba
    ├── dto/
    │   ├── AsignarCategoriaRequest.java
    │   ├── ProductoResponseDTO.java          # DTO sin exponer entidad
    │   ├── CategoriaResponseDTO.java
    │   ├── UsuarioResponseDTO.java           # Sin password
    │   ├── CarritoResponseDTO.java
    │   ├── InventarioResponseDTO.java
    │   ├── VentaResponseDTO.java
    │   └── DetalleVentaResponseDTO.java
    ├── exception/
    │   ├── ErrorResponse.java                # Estructura de error
    │   ├── GlobalExceptionHandler.java       # Manejo centralizado
    │   └── ResourceNotFoundException.java
    ├── assembler/                            # 7 assemblers HATEOAS con DTOs
    ├── controller/                           # 8 controladores con paginacion
    ├── service/                              # Interfaces + implementaciones
    ├── entity/                               # 8 entidades con @Valid
    ├── repository/                           # 8 repositorios JPA
    └── security/
        ├── config/
        │   ├── SecurityConfig.java           # JWT + stateless
        │   └── JwtProperties.java            # Configuracion JWT
        ├── controller/
        │   └── AuthController.java           # POST /api/auth/login
        ├── filter/
        │   └── JwtAuthenticationFilter.java  # Filtro JWT
        ├── model/
        │   ├── CustomUserDetails.java
        │   ├── JwtResponse.java
        │   └── LoginRequest.java
        ├── monitor/
        │   └── SuspiciousActivityService.java # Monitoreo de seguridad
        ├── service/
        │   └── CustomUserDetailsService.java
        └── util/
            └── JwtUtil.java
```

---

## Seguridad

- **JWT Stateless** con `JwtAuthenticationFilter`
- **BCryptPasswordEncoder** para hashing de contraseñas
- **SuspiciousActivityService** monitorea intentos fallidos y tasa de requests
- **@SecurityScheme("bearerAuth")** en Swagger UI para probar endpoints autenticados
- Endpoints publicos: `GET /api/productos`, `/api/auth/**`, `/public/**`, Swagger UI, H2 console, Actuator

---

## Validacion

Todas las entidades utilizan Bean Validation:
- `@NotBlank`, `@Size`, `@Min`, `@NotNull` en campos
- `@Valid` en todos los request bodies
- `@Positive` en path variables de ID
- `GlobalExceptionHandler` retorna errores estructurados

---

## Beneficios de OpenAPI y HATEOAS

### OpenAPI / Swagger
- Documentacion viva que se actualiza automaticamente con el codigo fuente
- Interfaz grafica interactiva (Swagger UI) para explorar y probar endpoints
- Especificacion estandarizada (OAS 3.0) que permite integracion con herramientas de terceros
- Esquema de seguridad JWT visible en Swagger UI
- Links HATEOAS visibles en la especificacion OAS mediante @Link

### HATEOAS
- Clientes descubren la API navegando enlaces en lugar de usar URLs hardcodeadas
- Desacoplamiento entre cliente y servidor: los cambios de rutas no afectan a clientes HATEOAS
- Relaciones entre recursos visibles en las propias respuestas JSON
- APIs autodescriptivas y auto-contenidas (Richardson Maturity Model Nivel 3)
- Paginacion completa con first, prev, next, last

---

## Evidencias

| Tipo | Archivo / Ubicacion |
|---|---|
| Respuestas del formulario | `desarrollo-preguntas.md` |
| Compilacion | `./mvnw clean compile` -- BUILD SUCCESS (71 source files) |
| Tests | `./mvnw clean test` -- Tests run: 4, Failures: 0, Errors: 0 |
| JaCoCo Report | `./mvnw test` -> `target/site/jacoco/index.html` |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| Especificacion OAS | http://localhost:8080/v3/api-docs |
| Actuator | http://localhost:8080/actuator/health |
