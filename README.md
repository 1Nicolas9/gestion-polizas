# API de Gestión de Pólizas de Seguros

API REST desarrollada con **Java 17** y **Spring Boot 3.2.5** para la gestión de pólizas de seguros y sus riesgos asociados.

## Tecnologías

- Java 17
- Spring Boot 3.2.5
- Spring Web + Validation
- Lombok
- SpringDoc OpenAPI (Swagger UI)
- Persistencia en memoria (simulada con ConcurrentHashMap)

## Estructura del Proyecto

```
src/main/java/com/bolivar/gestionpolizas/
├── GestionPolizasApplication.java          # Clase principal
├── config/
│   ├── ApiKeyInterceptor.java              # Interceptor para validar x-api-key
│   ├── WebConfig.java                      # Configuración del interceptor
│   ├── GlobalExceptionHandler.java         # Manejo global de excepciones
│   └── exception/
│       ├── NegocioException.java           # Excepción de reglas de negocio (422)
│       └── RecursoNoEncontradoException.java # Excepción recurso no encontrado (404)
├── controller/
│   ├── PolizaController.java              # Endpoints de pólizas
│   ├── RiesgoController.java             # Endpoints de riesgos
│   ├── CoreMockController.java           # Endpoint mock del CORE
│   └── dto/
│       ├── RenovarRequest.java           # DTO para renovación
│       ├── RiesgoRequest.java            # DTO para crear riesgo
│       └── CoreMockRequest.java          # DTO para evento core-mock
├── model/
│   ├── Poliza.java                       # Entidad Póliza
│   ├── Riesgo.java                       # Entidad Riesgo
│   ├── TipoPoliza.java                   # Enum INDIVIDUAL, COLECTIVA
│   ├── EstadoPoliza.java                 # Enum ACTIVA, RENOVADA, CANCELADA
│   └── EstadoRiesgo.java                 # Enum ACTIVO, CANCELADO
├── repository/
│   ├── PolizaRepository.java             # Repositorio en memoria de pólizas
│   └── RiesgoRepository.java            # Repositorio en memoria de riesgos
└── service/
    ├── PolizaService.java                # Lógica de negocio de pólizas
    ├── RiesgoService.java                # Lógica de negocio de riesgos
    └── CoreMockService.java              # Servicio mock de comunicación con CORE
```

## Requisitos Previos

- **Java 17** o superior
- **Maven 3.8+** (o usar el wrapper `mvnw` incluido)

## Instalación y Ejecución

```bash
# Clonar el repositorio
git clone https://github.com/1Nicolas9/gestion-polizas.git
cd gestion-polizas

# Compilar el proyecto
mvn clean package -DskipTests

# Ejecutar la aplicación
mvn spring-boot:run
```

La aplicación iniciará en: `http://localhost:8080`

## Documentación Swagger

Una vez ejecutada la aplicación, acceder a:
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/api-docs

## Seguridad - API Key

Todos los endpoints requieren el header `x-api-key` con valor `123456`.

```
x-api-key: 123456
```

Si el header no está presente o es inválido, se retorna un error **401 Unauthorized**.

## Endpoints

### Pólizas

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/polizas` | Listar pólizas (filtros opcionales: `tipo`, `estado`) |
| GET | `/polizas/{id}/riesgos` | Obtener riesgos de una póliza |
| POST | `/polizas/{id}/renovar` | Renovar una póliza con IPC |
| POST | `/polizas/{id}/cancelar` | Cancelar una póliza y sus riesgos |
| POST | `/polizas/{id}/riesgos` | Agregar riesgo a póliza COLECTIVA |

### Riesgos

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/riesgos/{id}/cancelar` | Cancelar un riesgo específico |

### Core Mock

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/core-mock/evento` | Simular evento al CORE |

## Ejemplos de Uso (cURL)

### Listar todas las pólizas
```bash
curl -X GET http://localhost:8080/polizas \
  -H "x-api-key: 123456"
```

### Listar pólizas filtradas por tipo
```bash
curl -X GET "http://localhost:8080/polizas?tipo=COLECTIVA" \
  -H "x-api-key: 123456"
```

### Obtener riesgos de una póliza
```bash
curl -X GET http://localhost:8080/polizas/2/riesgos \
  -H "x-api-key: 123456"
```

### Renovar póliza con IPC del 5%
```bash
curl -X POST http://localhost:8080/polizas/1/renovar \
  -H "x-api-key: 123456" \
  -H "Content-Type: application/json" \
  -d '{"ipc": 0.05}'
```

### Cancelar póliza
```bash
curl -X POST http://localhost:8080/polizas/2/cancelar \
  -H "x-api-key: 123456"
```

### Agregar riesgo a póliza colectiva
```bash
curl -X POST http://localhost:8080/polizas/2/riesgos \
  -H "x-api-key: 123456" \
  -H "Content-Type: application/json" \
  -d '{"descripcion": "Riesgo sísmico", "cobertura": "Daños por terremoto"}'
```

### Cancelar un riesgo
```bash
curl -X POST http://localhost:8080/riesgos/1/cancelar \
  -H "x-api-key: 123456"
```

### Evento Core Mock
```bash
curl -X POST http://localhost:8080/core-mock/evento \
  -H "x-api-key: 123456" \
  -H "Content-Type: application/json" \
  -d '{"evento": "ACTUALIZACION", "polizaId": 1}'
```

## Reglas de Negocio

1. **Póliza INDIVIDUAL**: Solo puede tener máximo 1 riesgo asociado.
2. **Renovación**: No se puede renovar una póliza en estado `CANCELADA`.
3. **Agregar riesgo**: Solo se permite en pólizas de tipo `COLECTIVA`.
4. **Cancelar póliza**: Automáticamente cancela todos los riesgos asociados.

## Datos Precargados

La aplicación incluye datos de ejemplo para pruebas inmediatas:

### Pólizas
| ID | Número | Tipo | Estado | Canon Mensual | Titular |
|----|--------|------|--------|---------------|---------|
| 1 | POL-001 | INDIVIDUAL | ACTIVA | $150,000 | Juan Carlos Pérez |
| 2 | POL-002 | COLECTIVA | ACTIVA | $500,000 | Empresa ABC S.A.S |
| 3 | POL-003 | INDIVIDUAL | CANCELADA | $200,000 | María López Rodríguez |

### Riesgos
| ID | Póliza | Descripción | Estado |
|----|--------|-------------|--------|
| 1 | POL-001 | Riesgo de incendio en propiedad | ACTIVO |
| 2 | POL-002 | Riesgo de accidente laboral | ACTIVO |
| 3 | POL-002 | Riesgo de enfermedad profesional | ACTIVO |

## Códigos de Respuesta

| Código | Descripción |
|--------|-------------|
| 200 | Operación exitosa |
| 201 | Recurso creado exitosamente |
| 400 | Error de validación en los datos de entrada |
| 401 | API Key ausente o inválida |
| 404 | Recurso no encontrado |
| 422 | Violación de regla de negocio |
| 500 | Error interno del servidor |
