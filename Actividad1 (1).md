# **Caso de estudio**

El Parqueadero “Autos Colombia”, parqueadero que presta el servicio de parqueadero por mensualidad, necesita un desarrollo de software para la gestión de sus operaciones diarias que le permita registrar las entradas y salidas de los vehículos, registrar usuarios, manejar las celdas, y registrar novedades ocurridas sobre determinado vehículo en el parqueadero y gestionar los pagos. Esto incluye el análisis de los siguientes requerimientos funcionales:

* Gestión de la entrada y salida de vehículos.  
* Gestión de usuarios y gestión de celdas.  
* Gestión de pagos.

# **Requerimientos funcionales**

**RF1:** Tener un registro de los vehículos con mensualidad, en donde se guarden datos de importancia asociados con el vehículo, tales como: número de placa, dueño, tipo de vehículo (carro, moto), marca, modelo, color, celda asociada, fecha de vinculación, estado (Activo, Inactivo), estado_presencia (Presente, Ausente).  
**RF2:** Tener un registro de dueños que contenga datos relevantes del cliente: tipo y número de documento de identidad, nombre y apellido, número de teléfono, correo electrónico, fecha de vinculación, fecha de corte de mensualidad, estado (Activo, Inactivo).  
**RF3:** Cada vehículo tiene una celda asociada, por lo que se debe tener un registro con las celdas del parqueadero con atributos como: Identificador (código de celda), tipo de vehículo admitido, estado de la celda (Disponible, Asignada, En mantenimiento), vehículo asociado.  
**RF4:** Tener un registro de entradas y salidas de los vehículos, en donde se defina con cada movimiento: Fecha y hora, placa del vehículo, tipo de movimiento (Entrada, Salida), usuario del sistema que autoriza el registro. Este registro debe validar que el vehículo y su dueño se encuentren en estado Activo; además, actualizará automáticamente el atributo estado_presencia del vehículo, impidiendo registrar una entrada si el vehículo ya está Presente o una salida si figura como Ausente.  
**RF5:** Tener registro de novedades, en donde se puedan anotar novedades ocurridas sobre determinado vehículo dentro del parqueadero, almacenando: fecha y hora de la novedad, placa del vehículo, tipo de novedad, descripción detallada, usuario del sistema responsable del reporte y estado de la novedad (Abierta, Atendida, Cerrada).  
**RF6:** El pago de la mensualidad se debe realizar a través de una pasarela de pagos; la mensualidad está asociada al dueño del vehículo. El valor que este debe pagar es equivalente al valor de la tarifa multiplicado por el número de vehículos que estén asociados al dueño. Cada vez que el dueño realice el pago de la mensualidad, se le suma un mes a la fecha de corte registrada en la tabla de dueños. Se debe recordar al dueño del vehículo el pago de la mensualidad tres días calendario antes de la fecha de corte; esto se realiza mediante correo electrónico. En caso de que el dueño no pague la tarifa, su estado pasará a inactivo y, por consiguiente, el mismo estado se aplicará a todos los vehículos asociados.  
**RF7:** Debe haber un panel administrativo en donde el administrador pueda consultar, agregar, actualizar e inhabilitar los usuarios (dueños), vehículos, celdas, registros de entradas y salidas, y registro de novedades; así como parametrizar las tarifas de mensualidad y consultar el estado de pagos.  
**RF8:** Debe existir un panel operativo en donde el operario pueda únicamente registrar mediante formularios movimientos (entradas y salidas), novedades, nuevos usuarios (dueños) y nuevos vehículos, sin permisos de modificación estructural ni eliminación.  
**RF9:** Para la eliminación de registros en la base de datos se utilizará borrado lógico, por lo que se administrará mediante estados (Activo / Inactivo).  
**RF10:** El sistema debe contar con un módulo de autenticación y control de acceso que permita el inicio de sesión seguro, diferenciando permisos entre los roles de Administrador y Operario.  

# **Requerimientos no funcionales**

**RNF1:** La persistencia de datos se gestionará a través de un motor de base de datos relacional; para fines del desarrollo y ejecución del proyecto académico se utilizará H2 Database en modo embebido/archivo con persistencia local.  
**RNF2:** La arquitectura del sistema debe basarse en el patrón Modelo-Vista-Controlador (MVC), estructurado en capas desacopladas (Controladores, Servicios de Lógica de Negocio, Repositorios de Datos y Entidades de Dominio) utilizando el framework Spring Boot 3.x / Java 21.  
**RNF3:** La seguridad del sistema garantizará la autenticación y autorización mediante Spring Security, asegurando que las contraseñas de los usuarios se encuentren cifradas mediante el algoritmo de hash seguro BCrypt.  
**RNF4:** La interfaz gráfica de usuario debe desarrollarse utilizando Thymeleaf y Bootstrap 5, garantizando un diseño responsivo, limpio e intuitivo, adaptable a resoluciones de escritorio y dispositivos móviles.  
**RNF5:** El sistema debe garantizar un tiempo de respuesta inferior a dos (2) segundos en las operaciones comunes de registro de entradas, salidas y consultas de estado en condiciones normales de uso.  
**RNF6:** Se debe asegurar la integridad y consistencia de los datos mediante validaciones tanto en el lado del cliente como en el servidor, impidiendo duplicidad en placas, correos electrónicos y números de documento.  
**RNF7:** El código fuente debe estructurarse siguiendo las buenas prácticas de programación Java y principios SOLID, apoyándose en la librería Lombok para reducir código boilerplate y mejorar la legibilidad y mantenibilidad del aplicativo.  
**RNF8:** La gestión de transacciones de base de datos debe ser atómica (ACID), implementada mediante la anotación @Transactional en los servicios, garantizando que operaciones complejas (como la asignación de celdas o registro de pagos) no dejen datos inconsistentes ante eventuales fallos.  

# **Historias de usuario**

### **HU01: Autenticación y control de acceso basado en roles**
* **Enunciado:** Yo como usuario del sistema (Administrador / Operario) quiero iniciar sesión con mis credenciales (nombre de usuario y contraseña) para acceder de manera segura a los módulos y vistas que corresponden a mi nivel de autorización.
* **Requerimiento funcional asociado:** RF10
* **Criterios de aceptación:**
  1. Si las credenciales son válidas y el usuario posee el rol de Administrador, el sistema lo redirecciona al panel administrativo general.
  2. Si las credenciales son válidas y el usuario posee el rol de Operario, el sistema lo redirecciona al panel operativo simplificado.
  3. Si las credenciales son inválidas o el usuario se encuentra inactivo, el sistema muestra un mensaje de alerta genérico sin especificar si falló el usuario o la contraseña.
  4. Se bloquea el acceso directo mediante URL a las rutas del panel administrativo para usuarios con rol de Operario o usuarios no autenticados.

### **HU02: Registro y vinculación de dueños de vehículos**
* **Enunciado:** Yo como operario o administrador quiero registrar a un nuevo cliente con sus datos personales y de contacto para vincularlo formalmente al servicio de parqueadero por mensualidad.
* **Requerimiento funcional asociado:** RF2, RF7, RF8
* **Criterios de aceptación:**
  1. El formulario solicita de forma obligatoria: tipo y número de documento de identidad, nombres y apellidos, teléfono, correo electrónico y fecha de vinculación.
  2. El sistema valida que ni el número de documento ni el correo electrónico existan previamente en la base de datos.
  3. El sistema asigna automáticamente el estado inicial Activo y fija la primera fecha de corte a un (1) mes calendario exacto a partir de la fecha de vinculación.
  4. Ante datos con formato incorrecto (ej. correo inválido o campos obligatorios vacíos), el formulario destaca los errores y no permite el guardado.

### **HU03: Registro de vehículos y asignación de celda exclusiva**
* **Enunciado:** Yo como operario o administrador quiero registrar un vehículo asociándolo a un dueño activo y asignándole una celda libre acorde a su tipo para habilitar su derecho de estacionamiento mensual.
* **Requerimiento funcional asociado:** RF1, RF3, RF7, RF8
* **Criterios de aceptación:**
  1. El formulario solicita: placa, tipo de vehículo (carro o moto), marca, modelo, color, selección de un dueño activo y selección de una celda disponible compatible con el tipo de vehículo.
  2. El sistema valida que la placa no se encuentre registrada previamente y cumpla el formato alfanumérico válido.
  3. Al registrar exitosamente el vehículo, se le asigna estado Activo, estado_presencia inicial Ausente, y la celda seleccionada cambia automáticamente su estado a Asignada.
  4. El sistema restringe la selección de dueños que se encuentren en estado Inactivo.

### **HU04: Registro de entrada de vehículos al parqueadero**
* **Enunciado:** Yo como operario quiero registrar el ingreso de un vehículo mediante su número de placa para controlar la presencia física en las instalaciones y registrar la hora exacta de ingreso.
* **Requerimiento funcional asociado:** RF4, RF8
* **Criterios de aceptación:**
  1. El operario ingresa o selecciona la placa del vehículo que ingresa al parqueadero.
  2. El sistema valida que el vehículo exista, se encuentre en estado Activo y que su dueño esté en estado Activo (al día con su mensualidad).
  3. El sistema valida que el atributo estado_presencia del vehículo sea Ausente. Si figura como Presente, se rechaza el registro con un mensaje indicando que el vehículo ya está dentro.
  4. Al confirmarse el ingreso, se genera un registro de movimiento con fecha y hora actual, tipo de movimiento Entrada, usuario responsable en sesión, y se actualiza el estado_presencia del vehículo a Presente.

### **HU05: Registro de salida de vehículos del parqueadero**
* **Enunciado:** Yo como operario quiero registrar la salida de un vehículo mediante su número de placa para dejar constancia del egreso y actualizar la ocupación del parqueadero.
* **Requerimiento funcional asociado:** RF4, RF8
* **Criterios de aceptación:**
  1. El operario ingresa o selecciona la placa del vehículo que egresa.
  2. El sistema valida que el vehículo se encuentre con estado_presencia en Presente. Si figura como Ausente, se bloquea el registro indicando inconsistencia de movimiento.
  3. Al confirmarse la salida, se genera un registro de movimiento con fecha y hora actual, tipo de movimiento Salida, usuario responsable en sesión, y se actualiza el estado_presencia del vehículo a Ausente.
  4. La celda continúa asignada al vehículo en el sistema, conservando su exclusividad por mensualidad.

### **HU06: Registro y seguimiento de novedades sobre vehículos**
* **Enunciado:** Yo como operario quiero registrar novedades o incidencias detectadas sobre un vehículo dentro del parqueadero para conservar un respaldo formal que proteja al parqueadero ante reclamos.
* **Requerimiento funcional asociado:** RF5, RF7, RF8
* **Criterios de aceptación:**
  1. El formulario solicita: placa del vehículo involucrado, tipo de novedad (golpe previo, rayón, fuga de fluidos, objeto olvidado, etc.), descripción detallada y nivel de severidad.
  2. El sistema asocia automáticamente la fecha y hora actual, el usuario operario en sesión y define el estado inicial de la novedad como Abierta.
  3. La novedad queda visible de inmediato tanto en el panel operativo como en el panel administrativo.

### **HU07: Gestión y parametrización de celdas de parqueo**
* **Enunciado:** Yo como administrador quiero crear, consultar y gestionar el estado de las celdas del parqueadero para administrar la capacidad física disponible e inhabilitar espacios por mantenimiento.
* **Requerimiento funcional asociado:** RF3, RF7
* **Criterios de aceptación:**
  1. Permite crear celdas indicando un código identificador único (ej: C-01, M-01) y el tipo de vehículo permitido (Carro o Moto).
  2. Permite modificar el estado de la celda entre Disponible, Asignada y En mantenimiento.
  3. Impide poner en estado En mantenimiento o reasignar una celda si el vehículo vinculado a ella tiene estado_presencia en Presente.
  4. Permite filtrar y visualizar el listado de celdas por tipo de vehículo y estado de disponibilidad.

### **HU08: Procesamiento y simulación de pago de mensualidad**
* **Enunciado:** Yo como administrador o cliente quiero registrar el pago de la mensualidad a través de la pasarela de pagos integrada para renovar la suscripción del servicio por un mes adicional.
* **Requerimiento funcional asociado:** RF6, RF7
* **Criterios de aceptación:**
  1. El sistema calcula automáticamente el valor total a cancelar multiplicando la tarifa vigente por el número total de vehículos activos asociados al dueño.
  2. Al confirmar satisfactoriamente la transacción en la pasarela, se emite un comprobante con referencia de pago, fecha/hora, valor liquidado y método de pago.
  3. El sistema suma automáticamente un (1) mes calendario a la fecha de corte registrada en el dueño.
  4. Si el dueño y sus vehículos se encontraban en estado Inactivo por mora, el registro de pago exitoso los reactiva automáticamente a estado Activo.

### **HU09: Notificación automática de cobro y suspensión por mora**
* **Enunciado:** Yo como sistema quiero verificar diariamente las fechas de corte de los clientes para enviar recordatorios de pago preventivos y suspender automáticamente a los usuarios en mora.
* **Requerimiento funcional asociado:** RF6
* **Criterios de aceptación:**
  1. Mediante una tarea programada diaria, el sistema detecta a los dueños cuya fecha de corte vencerá en exactamente tres (3) días calendario y les despacha un correo electrónico informativo con el monto a cancelar.
  2. Si la fecha actual sobrepasa la fecha de corte del dueño sin registrarse el pago, el sistema cambia automáticamente el estado del dueño a Inactivo.
  3. La inactivación del dueño cambia en cascada el estado de todos sus vehículos vinculados a Inactivo, inhabilitando su autorización de ingreso en el panel operativo.

### **HU10: Administración de registros con borrado lógico**
* **Enunciado:** Yo como administrador quiero actualizar la información de dueños y vehículos, e inhabilitar aquellos que cancelen el servicio para mantener la información vigente sin perder el histórico transaccional en la base de datos.
* **Requerimiento funcional asociado:** RF7, RF9
* **Criterios de aceptación:**
  1. El administrador puede actualizar los datos de contacto del dueño y los datos no identificadores del vehículo.
  2. La acción de eliminar un registro ejecuta un borrado lógico actualizando el atributo estado a Inactivo (sin aplicar borrado físico DELETE).
  3. Al inhabilitar un vehículo de forma permanente, la celda asociada se libera automáticamente cambiando su estado a Disponible.
  4. Las vistas administrativas permiten alternar filtros para consultar registros activos e inactivos.

### **HU11: Visualización del panel operativo y de control en tiempo real**
* **Enunciado:** Yo como operario o administrador quiero visualizar un tablero resumen con la ocupación actual del parqueadero y los últimos movimientos para monitorear el flujo de vehículos y la disponibilidad de espacios de manera ágil.
* **Requerimiento funcional asociado:** RF7, RF8
* **Criterios de aceptación:**
  1. El panel muestra tarjetas con métricas en tiempo real: número de vehículos presentes, celdas disponibles para carros, celdas disponibles para motos y novedades abiertas.
  2. Presenta una tabla dinámica con los últimos movimientos de entrada y salida (placa, fecha/hora, tipo de movimiento y operario).
  3. Contiene accesos directos y visibles a los formularios de registro de entrada, salida y reporte de novedades.
  4. La interfaz es responsiva y se visualiza correctamente en dispositivos móviles y de escritorio.

### **HU12: Parametrización de tarifas del parqueadero**
* **Enunciado:** Yo como administrador quiero configurar y actualizar los valores de las tarifas de mensualidad para carros y motos para mantener los cobros alineados a las políticas comerciales del parqueadero.
* **Requerimiento funcional asociado:** RF7
* **Criterios de aceptación:**
  1. El sistema permite ingresar y modificar el valor de la tarifa mensual para carros y para motos, validando que sean valores numéricos mayores a cero.
  2. Las tarifas modificadas aplican únicamente para los cobros que se generen a partir de la fecha de actualización, conservando el valor histórico de pagos anteriores.
  3. El módulo de tarifas es accesible únicamente para usuarios con rol de Administrador.

# **Modelo físico (Script DDL - H2 Database)**

```sql
-- 1. Tabla de Usuarios
CREATE TABLE IF NOT EXISTS usuarios (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    username VARCHAR(30) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    nombre_completo VARCHAR(80) NOT NULL,
    rol VARCHAR(20) NOT NULL CHECK (rol IN ('ROLE_ADMIN', 'ROLE_OPERARIO')),
    estado VARCHAR(15) DEFAULT 'ACTIVO' NOT NULL CHECK (estado IN ('ACTIVO', 'INACTIVO'))
);

-- 2. Tabla de Tarifas
CREATE TABLE IF NOT EXISTS tarifas (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    tipo_vehiculo VARCHAR(10) NOT NULL UNIQUE CHECK (tipo_vehiculo IN ('CARRO', 'MOTO')),
    valor_mensualidad DECIMAL(10, 2) NOT NULL CHECK (valor_mensualidad > 0),
    fecha_actualizacion DATE NOT NULL
);

-- 3. Tabla de Celdas del Parqueadero
CREATE TABLE IF NOT EXISTS celdas (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    codigo_celda VARCHAR(10) NOT NULL UNIQUE,
    tipo_permitido VARCHAR(10) NOT NULL CHECK (tipo_permitido IN ('CARRO', 'MOTO')),
    estado_celda VARCHAR(20) DEFAULT 'DISPONIBLE' NOT NULL CHECK (estado_celda IN ('DISPONIBLE', 'ASIGNADA', 'MANTENIMIENTO'))
);

-- 4. Tabla de Dueños (Clientes con mensualidad)
CREATE TABLE IF NOT EXISTS duenos (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    tipo_documento VARCHAR(10) NOT NULL CHECK (tipo_documento IN ('CC', 'CE', 'NIT', 'PASAPORTE')),
    numero_documento VARCHAR(20) NOT NULL UNIQUE,
    nombres VARCHAR(60) NOT NULL,
    apellidos VARCHAR(60) NOT NULL,
    telefono VARCHAR(20) NOT NULL,
    correo VARCHAR(100) NOT NULL UNIQUE,
    fecha_vinculacion DATE NOT NULL,
    fecha_corte DATE NOT NULL,
    estado VARCHAR(15) DEFAULT 'ACTIVO' NOT NULL CHECK (estado IN ('ACTIVO', 'INACTIVO'))
);

-- 5. Tabla de Vehículos Vinculados
CREATE TABLE IF NOT EXISTS vehiculos (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    placa VARCHAR(10) NOT NULL UNIQUE,
    tipo_vehiculo VARCHAR(10) NOT NULL CHECK (tipo_vehiculo IN ('CARRO', 'MOTO')),
    marca VARCHAR(40) NOT NULL,
    modelo INT NOT NULL CHECK (modelo >= 1950 AND modelo <= 2100),
    color VARCHAR(30) NOT NULL,
    fecha_vinculacion DATE NOT NULL,
    estado VARCHAR(15) DEFAULT 'ACTIVO' NOT NULL CHECK (estado IN ('ACTIVO', 'INACTIVO')),
    estado_presencia VARCHAR(15) DEFAULT 'AUSENTE' NOT NULL CHECK (estado_presencia IN ('PRESENTE', 'AUSENTE')),
    id_dueno BIGINT NOT NULL,
    id_celda BIGINT NOT NULL UNIQUE,
    CONSTRAINT fk_vehiculo_dueno FOREIGN KEY (id_dueno) REFERENCES duenos(id),
    CONSTRAINT fk_vehiculo_celda FOREIGN KEY (id_celda) REFERENCES celdas(id)
);

-- 6. Tabla de Movimientos (Entradas y Salidas)
CREATE TABLE IF NOT EXISTS movimientos (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    id_vehiculo BIGINT NOT NULL,
    id_usuario BIGINT NOT NULL,
    tipo_movimiento VARCHAR(10) NOT NULL CHECK (tipo_movimiento IN ('ENTRADA', 'SALIDA')),
    fecha_hora TIMESTAMP NOT NULL,
    CONSTRAINT fk_movimiento_vehiculo FOREIGN KEY (id_vehiculo) REFERENCES vehiculos(id),
    CONSTRAINT fk_movimiento_usuario FOREIGN KEY (id_usuario) REFERENCES usuarios(id)
);

-- 7. Tabla de Novedades sobre Vehículos
CREATE TABLE IF NOT EXISTS novedades (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    id_vehiculo BIGINT NOT NULL,
    id_usuario BIGINT NOT NULL,
    tipo_novedad VARCHAR(40) NOT NULL,
    descripcion VARCHAR(500) NOT NULL,
    nivel_severidad VARCHAR(15) NOT NULL CHECK (nivel_severidad IN ('BAJA', 'MEDIA', 'ALTA')),
    fecha_hora TIMESTAMP NOT NULL,
    estado_novedad VARCHAR(15) DEFAULT 'ABIERTA' NOT NULL CHECK (estado_novedad IN ('ABIERTA', 'ATENDIDA', 'CERRADA')),
    CONSTRAINT fk_novedad_vehiculo FOREIGN KEY (id_vehiculo) REFERENCES vehiculos(id),
    CONSTRAINT fk_novedad_usuario FOREIGN KEY (id_usuario) REFERENCES usuarios(id)
);

-- 8. Tabla de Pagos de Mensualidad
CREATE TABLE IF NOT EXISTS pagos (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    id_dueno BIGINT NOT NULL,
    fecha_pago TIMESTAMP NOT NULL,
    monto_total DECIMAL(12, 2) NOT NULL CHECK (monto_total > 0),
    cantidad_vehiculos INT NOT NULL CHECK (cantidad_vehiculos > 0),
    referencia_transaccion VARCHAR(60) NOT NULL UNIQUE,
    metodo_pago VARCHAR(30) NOT NULL,
    estado_pago VARCHAR(20) DEFAULT 'APROBADO' NOT NULL CHECK (estado_pago IN ('APROBADO', 'RECHAZADO')),
    CONSTRAINT fk_pago_dueno FOREIGN KEY (id_dueno) REFERENCES duenos(id)
);

-- Índices para optimización de consultas
CREATE INDEX IF NOT EXISTS idx_vehiculo_placa ON vehiculos(placa);
CREATE INDEX IF NOT EXISTS idx_dueno_documento ON duenos(numero_documento);
CREATE INDEX IF NOT EXISTS idx_movimiento_fechahora ON movimientos(fecha_hora);
```

# **Entregables**

1. Realizar los requerimientos funcionales y no funcionales para la gestión de entrada y salida de vehículos.  
2. Realizar las historias de usuario asociadas.  
3. Realizar la arquitectura del sistema de la aplicación en STARUML.  
4. Realizar los mockups de las historias de usuario asociadas en Figma.  
5. Realizar el MER, MR y modelo físico.  
6. Realizar los diagramas de casos de uso STARUML.  
7. Realizar el diagrama de clases STARUML.  
8. Realizar el diagrama de paquetes STARUML.  
9. Código fuente corriendo y en repositorio en GitHub.

**NOTA:** Para efectos prácticos de la actividad, vamos a usar como base de datos a H2 database.
