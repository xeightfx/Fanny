# Backend - Fanny.com (E-commerce Retail de Ropa)

API REST hecha con Spring Boot 4.1.1 + Spring Data JPA + Spring Security,
conectada a PostgreSQL en Supabase.

## Como ejecutar

1. Revisa `src/main/resources/application.properties` y confirma tu contraseña de Supabase.
2. En una terminal, dentro de esta carpeta:

   ```
   .\gradlew bootRun          (Windows)
   ./gradlew bootRun          (Mac/Linux)
   ```

3. Espera a ver `Started DemoApplication`. La API queda en `http://localhost:8080`.

## Como probar (con Postman, Insomnia o curl)

La autenticacion es HTTP Basic: en cada peticion que requiera login, se manda
el email y password del usuario (Postman: pestaña "Authorization" -> "Basic Auth").

Usuarios de prueba (creados por `supabase_datos_prueba.sql`, contraseña `123456`):

| Rol       | Email                |
|-----------|-----------------------|
| Admin     | admin@fanny.com       |
| Vendedor  | vendedor@fanny.com    |
| Cliente   | cliente@fanny.com     |
| Almacen   | almacen@fanny.com     |

### Ejemplos rapidos

```
GET  http://localhost:8080/api/productos              (publico, sin login)
GET  http://localhost:8080/api/categorias              (publico, sin login)

POST http://localhost:8080/api/auth/registro           (publico, crea un cliente nuevo)
GET  http://localhost:8080/api/auth/me                 (requiere login)

GET  http://localhost:8080/api/cliente/carrito                    (login como cliente)
POST http://localhost:8080/api/cliente/carrito/items               (login como cliente)
POST http://localhost:8080/api/cliente/checkout                    (login como cliente)
GET  http://localhost:8080/api/cliente/pedidos                     (login como cliente)

GET  http://localhost:8080/api/vendedor/productos                  (login como vendedor)
POST http://localhost:8080/api/vendedor/productos                  (login como vendedor)

GET  http://localhost:8080/api/almacen/inventario/stock-bajo       (login como almacen)

GET  http://localhost:8080/api/admin/usuarios                      (login como admin)
GET  http://localhost:8080/api/admin/reportes/dashboard            (login como admin)
```

## Estructura

```
src/main/java/com/fanny/
├── DemoApplication.java
├── model/          16 entidades JPA + 6 enums (Rol, Usuario, Producto, Pedido, ...)
├── repository/     17 interfaces JpaRepository
├── dto/            Records de entrada/salida (no se exponen las entidades directo)
├── service/        11 clases con las reglas de negocio y @Transactional
├── controller/      14 controllers REST, organizados por actor (admin/vendedor/cliente/almacen)
├── security/        UsuarioPrincipal, UsuarioDetailsService, SecurityUtils
├── config/           SecurityConfig (roles, CORS, BCrypt)
└── exception/        Excepciones propias + manejador global (respuestas JSON de error)
```

## Seguridad (importante antes de subir a GitHub)

`application.properties` tiene la contraseña de Supabase escrita. Antes de tu
primer `git push`, muevela a variables de entorno o a un `application-local.properties`
agregado al `.gitignore`.
