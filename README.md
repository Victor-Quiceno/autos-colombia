# Autos Colombia - Sistema de Gestión de Parqueadero

Sistema web para la administración y control operativo de parqueaderos con modelo de cobro por mensualidad. Desarrollado bajo una arquitectura Modelo-Vista-Controlador (MVC) en capas desacopladas, utilizando Spring Boot 3 y Java 21.

---

## 1. Descripción del Proyecto

Autos Colombia centraliza la operación de vehículos vinculados bajo la modalidad de mensualidad exclusiva. El sistema controla el flujo físico de entradas y salidas, la asignación y disponibilidad de celdas según tipología vehicular, la gestión de novedades sobre los vehículos, la liquidación de pagos y la automatización de políticas de cobro y suspensión por mora.

### Características Principales

* **Control de Acceso y Presencia:** Validación en tiempo real del estado de cuenta del cliente y del estado físico del vehículo (Presente / Ausente) para impedir ingresos o salidas inconsistentes.
* **Asignación Exclusiva de Celdas:** Vinculación estricta uno a uno entre vehículo y celda, validando compatibilidad por tipo (Carro o Moto) y bloqueando celdas en mantenimiento.
* **Seguridad y Control de Roles:** Autenticación mediante Spring Security con contraseñas encriptadas en BCrypt, diferenciando interfaces y permisos entre Administrador y Operario.
* **Liquidación y Registro de Pagos:** Simulación de pasarela de pagos con cálculo dinámico según tarifa vigente y número de vehículos activos, extendiendo automáticamente la fecha de corte.
* **Automatización de Procesos (Cron / Scheduled):** Tareas en segundo plano que verifican vencimientos, envían recordatorios preventivos de cobro vía correo electrónico y ejecutan suspensión en cascada por mora.
* **Integridad y Borrado Lógico:** Las bajas de clientes y vehículos operan mediante actualización de estados (Activo / Inactivo), preservando el historial transaccional de auditoría.

---

## 2. Tecnologías Utilizadas

* **Lenguaje:** Java 21 (LTS)
* **Framework:** Spring Boot 3.5.x
  * Spring Data JPA (Hibernate como ORM)
  * Spring Security 6
  * Spring Web (Spring MVC)
  * Spring Task Execution & Scheduling
  * Spring Boot Validation (Jakarta Validation)
* **Base de Datos:** H2 Database (modo embebido persistente en disco)
* **Motor de Plantillas:** Thymeleaf (con integración Thymeleaf Spring Security Extras)
* **Frontend:** HTML5 semántico y CSS3 personalizado (diseño modular con Flexbox, CSS Grid y tipografía Google Fonts Inter)
* **Productividad:** Project Lombok
* **Gestor de Dependencias:** Apache Maven

---

## 3. Requisitos del Entorno

Para compilar y ejecutar el proyecto se requiere:

* **Java Development Kit (JDK):** Versión 21 o superior configurada en la variable de entorno `JAVA_HOME`.
* **Apache Maven:** Versión 3.9+ (opcional si se utiliza el Maven Wrapper incluido: `./mvnw` o `mvnw.cmd`).
* **Navegador Web:** Chrome, Firefox, Edge o cualquier navegador moderno.

---

## 4. Instalación y Ejecución

### Paso 1: Clonar el repositorio
```bash
git clone https://github.com/tu-usuario/autos-colombia.git
cd autos-colombia
```

### Paso 2: Compilar el proyecto
Si se tiene Maven instalado globalmente:
```bash
mvn clean compile
```

O utilizando el Maven Wrapper:
```bash
# En Windows:
mvnw.cmd clean compile

# En Linux / macOS:
./mvnw clean compile
```

### Paso 3: Iniciar la aplicación
```bash
# Con Maven global:
mvn spring-boot:run

# Con Maven Wrapper en Windows:
mvnw.cmd spring-boot:run

# Con Maven Wrapper en Linux / macOS:
./mvnw spring-boot:run
```

La aplicación iniciará en el puerto `8080`.

### Paso 4: Acceder a la plataforma
Abrir el navegador e ingresar a:
```
http://localhost:8080
```

---

## 5. Credenciales de Acceso

La aplicación cuenta con un inicializador de datos (`DataInitializer`) que se ejecuta automáticamente al iniciar por primera vez, precargando usuarios, tarifas, celdas, clientes, vehículos y movimientos de prueba.

| Usuario | Contraseña | Rol Asignado | Alcance de Permisos |
| :--- | :--- | :--- | :--- |
| **admin** o **admin01** | `admin123` | `ROLE_ADMIN` | Acceso total: Panel administrativo, gestión de celdas, tarifas, clientes, vehículos, movimientos, novedades, pagos, auditoría y ejecución de tareas cron. |
| **operario**, **m.rios** o **c.diaz** | `operario123` | `ROLE_OPERARIO` | Acceso operativo: Panel de control en tiempo real, registro de entradas y salidas, registro de novedades y registro de nuevos clientes/vehículos. |

### Consola de Base de Datos H2

Para inspeccionar las tablas y registros directamente desde el navegador:

* **URL de la consola:** `http://localhost:8080/h2-console`
* **Driver Class:** `org.h2.Driver`
* **JDBC URL:** `jdbc:h2:file:./data/autos_colombia_db;DB_CLOSE_ON_EXIT=FALSE;AUTO_RECONNECT=TRUE`
* **User Name:** `sa`
* **Password:** *(dejar en blanco)*

---

## 6. Estructura del Código Fuente

El proyecto sigue las directrices estándar de organización de Spring Boot:

```
autos-colombia/
├── src/
│   ├── main/
│   │   ├── java/com/autos_colombia/
│   │   │   ├── config/          # Seguridad (SecurityConfig) e inicialización (DataInitializer)
│   │   │   ├── controller/      # Controladores Spring MVC (Rutas web y binding de vistas)
│   │   │   ├── dto/             # Objetos de transferencia de datos con validaciones Jakarta
│   │   │   ├── model/           # Entidades de dominio JPA con anotaciones de persistencia
│   │   │   ├── repository/     # Interfaces de persistencia Spring Data JPA
│   │   │   ├── service/         # Lógica de negocio transaccional y servicios programados
│   │   │   └── AutosColombiaApplication.java
│   │   └── resources/
│   │       ├── templates/       # Vistas HTML con Thymeleaf (auth, panel, duenos, etc.)
│   │       └── application.properties # Parámetros de conexión, H2, logging y seguridad
│   └── test/                    # Pruebas unitarias y de integración
├── .gitignore
├── pom.xml
└── README.md
```

---

## 7. Mapeo de Rutas Principales

| Módulo | Ruta HTTP | Método | Acceso Requerido |
| :--- | :--- | :---: | :--- |
| **Autenticación** | `/login` | GET | Público |
| **Panel Operativo** | `/panel/operativo` | GET | `ROLE_ADMIN`, `ROLE_OPERARIO` |
| **Panel Administrativo** | `/panel/administrativo` | GET | `ROLE_ADMIN` |
| **Entradas y Salidas** | `/movimientos` | GET | `ROLE_ADMIN`, `ROLE_OPERARIO` |
| **Registro Entrada** | `/movimientos/entrada` | GET / POST | `ROLE_ADMIN`, `ROLE_OPERARIO` |
| **Registro Salida** | `/movimientos/salida` | GET / POST | `ROLE_ADMIN`, `ROLE_OPERARIO` |
| **Novedades** | `/novedades` | GET / POST | `ROLE_ADMIN`, `ROLE_OPERARIO` |
| **Gestión Dueños** | `/duenos` | GET / POST | `ROLE_ADMIN`, `ROLE_OPERARIO` |
| **Gestión Vehículos** | `/vehiculos` | GET / POST | `ROLE_ADMIN`, `ROLE_OPERARIO` |
| **Gestión Celdas** | `/celdas` | GET / POST | `ROLE_ADMIN` |
| **Tarifas** | `/tarifas` | GET / POST | `ROLE_ADMIN` |
| **Pasarela de Pagos** | `/pagos/mensualidad` | GET / POST | `ROLE_ADMIN` |
| **Historial Pagos** | `/pagos/historial` | GET | `ROLE_ADMIN` |
| **Bandeja Notificaciones** | `/pagos/notificaciones` | GET | `ROLE_ADMIN` |

---

## 8. Reglas de Negocio Implementadas

1. **Unicidad de Celda:** Una celda sólo puede estar asociada a un único vehículo activo a la vez. No se permite asignar celdas que se encuentren en estado `ASIGNADA` o `MANTENIMIENTO`.
2. **Coherencia de Tipología:** Un vehículo de tipo `CARRO` únicamente puede vincularse a una celda de tipo `CARRO`. Similarmente ocurre con los vehículos de tipo `MOTO`.
3. **Validación de Presencia Física:** El sistema rechaza intentos de registrar la entrada de un vehículo cuyo estado ya figure como `PRESENTE`, o la salida de un vehículo en estado `AUSENTE`.
4. **Suspensión en Cascada por Mora:** Si un cliente supera su fecha límite de corte sin registrar el pago de su mensualidad, el proceso automático suspende su cuenta (`INACTIVO`) e inhabilita inmediatamente todos sus vehículos asociados, impidiendo su acceso físico al parqueadero.
5. **Reactivación por Pago:** Al registrar exitosamente un pago, el sistema renueva la fecha de corte agregando un mes calendario y reactiva automáticamente tanto al cliente como a todos sus vehículos vinculados.

---

## 9. Iteración 2: Gestión de Usuarios y Gestión de Celdas

Esta versión incorpora la **Iteración 2** de la Evidencia de Aprendizaje 2 (EA2), centrada en el control de acceso, gestión de clientes (dueños), vehículos, celdas y usuarios del sistema.

### Historias de Usuario Cubiertas
* **HU01 - Autenticación y control de acceso:** Inicio de sesión seguro con Spring Security 6, encriptación BCrypt, mensaje genérico de error y control 403.
* **HU02 - Registro y vinculación de dueños:** Registro obligatorio con fecha de corte inicial a 1 mes, documento y correo únicos. Edición restringida a `ROLE_ADMIN`.
* **HU03 - Registro de vehículos y celda exclusiva:** Asignación atómica de celda disponible por tipo (`CARRO`/`MOTO`), estado inicial `ACTIVO` y presencia `AUSENTE`.
* **HU07 - Gestión y parametrización de celdas:** Creación de celdas, filtros dinámicos por tipo y estado, y bloqueo de mantenimiento si el vehículo está `PRESENTE`.
* **HU10 - Administración con borrado lógico:** Inhabilitación de vehículos liberando celda (`id_celda` $\rightarrow$ `null`), inhabilitación de dueños en cascada atómica y reactivación independiente con selección de nueva celda disponible.
* **HU13 - Gestión de usuarios del sistema (Nuevo):** Módulo `/admin/usuarios` exclusivo para administradores, con listado por pestañas (Activos/Inactivos), creación con validación de contraseña $\ge 8$ caracteres, cifrado BCrypt, restablecimiento de contraseña, y restricciones de seguridad: impedimento de autoinhabilitación y protección del último administrador activo.

### Usuarios de Prueba Precargados

| Usuario | Contraseña | Rol | Descripción |
| :--- | :--- | :--- | :--- |
| `admin` | `admin123` | `ROLE_ADMIN` | Administrador principal |
| `admin01` | `admin123` | `ROLE_ADMIN` | Administrador secundario |
| `operario` | `operario123` | `ROLE_OPERARIO` | Operario general |
| `m.rios` | `operario123` | `ROLE_OPERARIO` | Operario de turno |
| `c.diaz` | `operario123` | `ROLE_OPERARIO` | Operaria de turno |

### Ejecución de Pruebas Unitarias y de Integración
Para ejecutar la suite completa de pruebas de la Iteración 2:
```bash
# En Windows:
mvnw.cmd test -Dtest=Iteracion2ServiciosTest

# En Linux / macOS:
./mvnw test -Dtest=Iteracion2ServiciosTest
```

### Ejecución de la Aplicación
```bash
# Iniciar el servidor web:
mvnw.cmd spring-boot:run
```
La aplicación estará disponible en `http://localhost:8080`.
