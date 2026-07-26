# customer-service

## 📌 Descripción

Microservicio REST para gestión de clientes (alta, baja, modificación y consulta), desarrollado con Spring Boot.

## 🏗 Arquitectura

- API REST con controlador principal: `/api/v1/customers`
- Capa de negocio (`service`) y persistencia (`repository`) con Spring Data JPA
- Base de datos MySQL (contenedor Docker)
- Integración con Config Server y registro en Eureka
- Cliente Feign para integración con servicio de productos

```text
                +----------------------+
                |   Config Server      |
                |       :8888          |
                +----------+-----------+
                           |
                           |
                +----------v-----------+
                |    Eureka Server     |
                |       :8761          |
                +----+-----------+-----+
                     |           |
          +----------+           +-----------+
          |                                  |
+---------v--------+              +----------v---------+
| Product Service  |              | Customer Service   |
|      :8080       |<-------------| :8081 Feign Client |
+------------------+              +--------------------+
```

## ⚙ Tecnologías

- Java 21
- Spring Boot 3.4
- Spring Web
- Spring Data JPA
- Spring Cloud (Config Client, OpenFeign, Eureka Client)
- MySQL 8.4
- MapStruct
- Springdoc OpenAPI / Swagger UI
- Maven

## 🚀 Cómo ejecutar

1. Levantar MySQL:

```bash
docker compose up -d
```

2. Ejecutar la aplicación:

```bash
mvn spring-boot:run
```

La API queda disponible en `http://localhost:8081`.

## 📖 Configuración (Config Server)

Este servicio importa configuración remota desde:

```yaml
spring:
  config:
    import: configserver:http://localhost:8888
```

Asegurate de tener el Config Server activo en `http://localhost:8888` antes de iniciar este servicio.

## 📡 Registro en Eureka

El proyecto incluye `spring-cloud-starter-netflix-eureka-client`, por lo que espera registrarse en Eureka usando la configuración externa provista por Config Server.

## 📄 Swagger

- UI: `http://localhost:8081/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8081/v3/api-docs`

## 🧪 Ejemplos con cURL

```bash
# 1) Crear cliente
curl --location 'http://localhost:8081/api/v1/customers' \
--header 'Content-Type: application/json' \
--data-raw '{
  "firstName": "Juan",
  "lastNameOrCompanyName": "Perez",
  "documentNumber": "12345678",
  "address": "Av. Siempre Viva 123",
  "phoneNumber": "+5491112345678",
  "email": "juan.perez@mail.com",
  "customerType": "PERSON",
  "outstandingBalance": 1500.50
}'

# 2) Listar clientes
curl --location 'http://localhost:8081/api/v1/customers'

# 3) Obtener cliente por ID
curl --location 'http://localhost:8081/api/v1/customers/1'

# 4) Actualizar cliente
curl --location --request PUT 'http://localhost:8081/api/v1/customers/1' \
--header 'Content-Type: application/json' \
--data-raw '{
  "firstName": "Juan Carlos",
  "lastNameOrCompanyName": "Perez",
  "documentNumber": "12345678",
  "address": "Calle Nueva 456",
  "phoneNumber": "+5491198765432",
  "email": "juan.carlos@mail.com",
  "customerType": "PERSON",
  "outstandingBalance": 900.00
}'

# 5) Eliminar cliente
curl --location --request DELETE 'http://localhost:8081/api/v1/customers/1'
```

## 📮 Colección de Postman

Podés generar la colección de dos formas:

1. Importar cada comando `curl` desde **Postman > Import > Raw text**.
2. Importar el OpenAPI:

```bash
curl --location 'http://localhost:8081/v3/api-docs' --output customer-service-openapi.json
```
