# API de Gestión de Pólizas

Este proyecto es una API REST pequeña para administrar pólizas de arrendamiento. Permite consultar pólizas y riesgos, renovar pólizas, cancelar pólizas o riesgos y agregar riesgos a pólizas colectivas. Es la implementación del Módulo 2 de la prueba técnica.

## Tecnologías utilizadas

- Java 17.
- Spring Boot 3.
- Spring Web para las rutas HTTP.
- Spring Data JPA para guardar los datos.
- H2, una base de datos en memoria que no necesita instalación adicional.
- Maven para descargar dependencias y ejecutar el proyecto.
- JUnit 5, MockMvc y Spring Boot Test para las pruebas.

## Requisitos previos

Antes de empezar, instala:

1. Java 17 o superior. Compruébalo con `java -version`.
2. Maven 3.9 o superior. Compruébalo con `mvn -version`.
3. Postman, si quieres consumir la API con una interfaz gráfica.

## Cómo abrir y ejecutar el proyecto

1. Abre una terminal en la carpeta del proyecto.
2. Ejecuta `mvn spring-boot:run`.
3. Espera el mensaje de Spring Boot indicando que la aplicación inició.

La API queda disponible en el puerto `8080`, por lo que la URL base es `http://localhost:8080`.

## Configuración

La configuración está en `src/main/resources/application.yml`.

### IPC

El porcentaje para renovar se define en `app.polizas.ipc`. Por defecto es `0.05`, es decir, 5%. Por ejemplo, con un canon de 1.000.000 y un período de 12 meses:

```text
nuevo canon = 1000000 * (1 + 0.05) = 1050000
nueva prima = 1050000 * 12 = 12600000
```

### API Key

Todas las rutas requieren el header siguiente:

```http
x-api-key: 123456
```

El valor se puede cambiar en `app.polizas.api-key`. Si el header no existe o no coincide, la API responde `401 Unauthorized`.

## Estructura del proyecto

```text
src/main/java/com/segurosbolivar/polizas
├── config       Configuración, datos iniciales y filtro de API Key
├── controller   Endpoints REST
├── dto          Objetos de entrada y salida JSON
├── entity       Poliza, Riesgo y sus enums
├── exception    Errores HTTP controlados
├── repository   Acceso a H2 con Spring Data JPA
└── service      Reglas de negocio y cliente CORE mock
```

## Datos iniciales

Al iniciar se crean estos datos para hacer pruebas rápidas:

| Póliza | Tipo | Estado | Riesgos |
|---|---|---|---|
| 1 | INDIVIDUAL | ACTIVA | 1 riesgo activo |
| 2 | COLECTIVA | ACTIVA | 2 riesgos activos |
| 3 | COLECTIVA | CANCELADA | 2 riesgos cancelados |

Los identificadores se generan en ese orden en una base H2 nueva. Reiniciar la aplicación vuelve a crear los datos porque H2 está en memoria.

## Endpoints

Incluye siempre el header `x-api-key: 123456` en cada solicitud.

| Método | URL completa | Uso |
|---|---|---|
| GET | `http://localhost:8080/polizas` | Lista todas las pólizas. |
| GET | `http://localhost:8080/polizas?tipo=INDIVIDUAL` | Filtra por tipo (`INDIVIDUAL` o `COLECTIVA`). |
| GET | `http://localhost:8080/polizas?estado=ACTIVA` | Filtra por estado (`ACTIVA`, `RENOVADA` o `CANCELADA`). |
| GET | `http://localhost:8080/polizas?tipo=COLECTIVA&estado=ACTIVA` | Aplica ambos filtros. |
| GET | `http://localhost:8080/polizas/2/riesgos` | Consulta riesgos de una póliza. |
| POST | `http://localhost:8080/polizas/1/renovar` | Renueva una póliza no cancelada. |
| POST | `http://localhost:8080/polizas/1/cancelar` | Cancela una póliza y todos sus riesgos. |
| POST | `http://localhost:8080/polizas/2/riesgos` | Crea un riesgo únicamente en una póliza colectiva. |
| POST | `http://localhost:8080/riesgos/2/cancelar` | Cancela un riesgo. |
| POST | `http://localhost:8080/core-mock/evento` | Registra en el log el intento de actualización del CORE. |

### Ejemplo de consulta

```http
GET http://localhost:8080/polizas?tipo=COLECTIVA&estado=ACTIVA
x-api-key: 123456
```

Ejemplo de respuesta:

```json
[
  {
    "id": 2,
    "tipo": "COLECTIVA",
    "estado": "ACTIVA",
    "periodoMeses": 12,
    "canonMensual": 1500000.00,
    "prima": 18000000.00,
    "cantidadRiesgos": 2
  }
]
```

### Crear un riesgo

```http
POST http://localhost:8080/polizas/2/riesgos
x-api-key: 123456
Content-Type: application/json
```

```json
{
  "nombre": "Apartamento 203",
  "descripcion": "Riesgo de arrendamiento para nueva unidad"
}
```

La respuesta exitosa es `201 Created`. Una póliza individual no acepta esta operación.

### Probar el CORE mock

Las renovaciones, cancelaciones y creación de riesgos usan internamente `CoreClient`, cuya implementación local solo escribe un log. También se puede llamar el endpoint del mock directamente:

```http
POST http://localhost:8080/core-mock/evento
x-api-key: 123456
Content-Type: application/json
```

```json
{
  "evento": "ACTUALIZACION",
  "polizaId": 555
}
```

La respuesta confirma el registro y la consola muestra el intento. No hay llamadas a servicios externos.

## Orden recomendado para probar

1. Lista las pólizas con `GET /polizas` y confirma los datos iniciales.
2. Consulta `GET /polizas/2/riesgos`.
3. Agrega un riesgo a la póliza 2.
4. Cancela uno de los riesgos activos de la póliza 2.
5. Renueva la póliza 1 y revisa que cambien canon, prima y estado.
6. Cancela una póliza activa y después consulta sus riesgos para confirmar la cancelación en cascada.
7. Intenta agregar un riesgo a la póliza 1 y renovar la póliza 3 para ver las reglas de negocio.
8. Envía el evento al CORE mock.

## Errores esperados

Las respuestas de error usan JSON claro, por ejemplo:

```json
{
  "fecha": "2026-09-20T12:00:00Z",
  "estado": 400,
  "error": "Bad Request",
  "mensaje": "No se puede renovar una póliza cancelada.",
  "ruta": "/polizas/3/renovar"
}
```

Casos útiles para validar:

- Usa una API Key incorrecta: responde `401`.
- Consulta una póliza inexistente, por ejemplo `/polizas/999999/riesgos`: responde `404`.
- Renueva la póliza 3: responde `400` porque está cancelada.
- Agrega un riesgo a la póliza 1: responde `400` porque es individual.
- Cancela dos veces la misma póliza o riesgo: responde `400` y evita una transición inválida.

## Colección de Postman

El archivo `postman/Polizas.postman_collection.json` contiene todas las rutas.

1. Abre Postman.
2. Selecciona **Import**.
3. Elige el archivo `postman/Polizas.postman_collection.json`.
4. Abre la colección importada.
5. Revisa o modifica sus variables: `baseUrl`, `apiKey`, `polizaId` y `riesgoId`.
6. Ejecuta las solicitudes en el orden recomendado.

La colección coloca el header `x-api-key: {{apiKey}}` en cada request.

## Pruebas

Las pruebas están en `src/test/java/com/segurosbolivar/polizas/GestionPolizasIntegrationTest.java`. Cubren consultas, filtros, renovaciones, cancelaciones, cascada de riesgos, creación de riesgos, validación de API Key, recursos inexistentes y CORE mock.

Las pruebas fueron creadas pero no ejecutadas durante la implementación.

Si el evaluador desea ejecutarlas, puede usar:

```bash
mvn test
```
