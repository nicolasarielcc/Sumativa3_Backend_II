# Minimarket Plus - Sistema de Gestion de Minimarket

## Descripcion

API REST desarrollada con **Spring Boot 3.4.1** y **JDK 17** para la gestion de un minimarket.
El sistema implementa operaciones CRUD completas para productos, categorias, carrito de compras,
inventario, usuarios, ventas y detalle de ventas, utilizando **JPA** con base de datos **H2**
en memoria y **Spring Security** para la autenticacion.

Esta version incorpora **OpenAPI 3.0** para la documentacion automatica de endpoints y
**Spring HATEOAS** para la navegabilidad entre recursos a traves de enlaces dinamicos.

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

---

## Endpoints de la API

### Productos

| Metodo | Ruta | Descripcion | Codigos HTTP |
|---|---|---|---|
| GET | /api/productos | Listar todos los productos | 200 |
| GET | /api/productos/{id} | Obtener producto por ID | 200, 404 |
| POST | /api/productos | Crear producto | 201, 400 |
| PUT | /api/productos/{id} | Actualizar producto | 200, 400, 404 |
| DELETE | /api/productos/{id} | Eliminar producto | 204, 404 |
| GET | /api/productos/{id}/categoria | Obtener categoria del producto | 200, 404 |
| POST | /api/productos/{id}/categoria | Asignar categoria al producto | 200, 400, 404 |

### Categorias

| Metodo | Ruta | Descripcion | Codigos HTTP |
|---|---|---|---|
| GET | /api/categorias | Listar categorias | 200 |
| GET | /api/categorias/{id} | Obtener categoria por ID | 200, 404 |
| POST | /api/categorias | Crear categoria | 201, 400 |
| PUT | /api/categorias/{id} | Actualizar categoria | 200, 400, 404 |
| DELETE | /api/categorias/{id} | Eliminar categoria | 204, 404 |

### Carrito

| Metodo | Ruta | Descripcion | Codigos HTTP |
|---|---|---|---|
| GET | /api/carrito | Listar carrito | 200 |
| GET | /api/carrito/{id} | Obtener item por ID | 200, 404 |
| POST | /api/carrito | Agregar producto al carrito | 201, 400 |
| PUT | /api/carrito/{id} | Actualizar item | 200, 400, 404 |
| DELETE | /api/carrito/{id} | Eliminar item | 204, 404 |

### Inventario

| Metodo | Ruta | Descripcion | Codigos HTTP |
|---|---|---|---|
| GET | /api/inventario | Listar movimientos de inventario | 200 |
| GET | /api/inventario/{id} | Obtener movimiento por ID | 200, 404 |
| POST | /api/inventario | Registrar movimiento | 201, 400 |
| PUT | /api/inventario/{id} | Actualizar movimiento | 200, 400, 404 |
| DELETE | /api/inventario/{id} | Eliminar movimiento | 204, 404 |

### Usuarios

| Metodo | Ruta | Descripcion | Codigos HTTP |
|---|---|---|---|
| GET | /api/usuarios | Listar usuarios | 200 |
| GET | /api/usuarios/{id} | Obtener usuario por ID | 200, 404 |
| POST | /api/usuarios | Crear usuario | 201, 400 |
| PUT | /api/usuarios/{id} | Actualizar usuario | 200, 400, 404 |
| DELETE | /api/usuarios/{id} | Eliminar usuario | 204, 404 |

### Ventas

| Metodo | Ruta | Descripcion | Codigos HTTP |
|---|---|---|---|
| GET | /api/ventas | Listar ventas | 200 |
| GET | /api/ventas/{id} | Obtener venta por ID | 200, 404 |
| POST | /api/ventas | Registrar venta | 201, 400 |

### Detalle de Ventas

| Metodo | Ruta | Descripcion | Codigos HTTP |
|---|---|---|---|
| GET | /api/detalle-ventas | Listar detalles de ventas | 200 |
| GET | /api/detalle-ventas/{id} | Obtener detalle por ID | 200, 404 |
| POST | /api/detalle-ventas | Crear detalle de venta | 201, 400 |
| PUT | /api/detalle-ventas/{id} | Actualizar detalle | 200, 400, 404 |
| DELETE | /api/detalle-ventas/{id} | Eliminar detalle | 204, 404 |

---

## HATEOAS

Todas las respuestas incluyen enlaces `_links` que permiten navegar entre recursos relacionados.
Cada recurso expone enlaces a si mismo (self), a la coleccion (listar), a las operaciones
(crear, editar, eliminar) y a sus recursos asociados.

### Ejemplo de respuesta con HATEOAS

```json
GET /api/productos/1

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

---

## Estructura del proyecto

```
minimarket/
├── pom.xml
└── src/main/java/com/minimarket/
    ├── MinimarketApplication.java
    ├── config/
    │   └── OpenApiConfig.java              # Bean OpenAPI personalizado
    ├── dto/
    │   └── AsignarCategoriaRequest.java    # DTO para subrecurso
    ├── assembler/
    │   ├── ProductoModelAssembler.java     # 7 enlaces
    │   ├── CategoriaModelAssembler.java    # 5 enlaces
    │   ├── CarritoModelAssembler.java      # 7 enlaces
    │   ├── InventarioModelAssembler.java   # 6 enlaces
    │   ├── UsuarioModelAssembler.java      # 5 enlaces
    │   ├── VentaModelAssembler.java        # 4 enlaces
    │   └── DetalleVentaModelAssembler.java  # 7 enlaces
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
    ├── entity/
    ├── repository/
    └── security/config/SecurityConfig.java
```

---

## Beneficios de OpenAPI y HATEOAS

### OpenAPI / Swagger
- Documentacion viva que se actualiza automaticamente con el codigo fuente
- Interfaz grafica interactiva (Swagger UI) para explorar y probar endpoints
- Especificacion estandarizada (OAS 3.0) que permite integracion con herramientas de terceros
- Versionado semantico de la API a traves de configuracion centralizada

### HATEOAS
- Clientes descubren la API navegando enlaces en lugar de usar URLs hardcodeadas
- Desacoplamiento entre cliente y servidor: los cambios de rutas no afectan a clientes HATEOAS
- Relaciones entre recursos visibles en las propias respuestas JSON
- APIs autodescriptivas y auto-contenidas (Richardson Maturity Model Nivel 3)

---

## Evidencias

| Tipo | Archivo / Ubicacion |
|---|---|
| Respuestas del formulario | `desarrollo-preguntas.md` |
| Resumen del sprint | `contexto-sesion.md` |
| Planificacion tecnica | `planificacion-codigo.md` |
| Compilacion | `./mvnw clean compile` — BUILD SUCCESS (55 source files) |
| Tests | `./mvnw clean test` — Tests run: 4, Failures: 0, Errors: 0 |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| Especificacion OAS | http://localhost:8080/v3/api-docs |