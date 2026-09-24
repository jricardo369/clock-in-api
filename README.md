# ClockInApi (clock-in-api)

API REST backend para la aplicación móvil **LUA** (control de asistencias a clases).
Gestiona usuarios, inicio de sesión, clases, asistencias, inscripciones, lista de espera, asuetos, configuraciones, multas/faltas, correos y reportes.

Base URL local: `http://localhost:8080/ClockInApi/api/`

## Tecnología utilizada

* Java 21 (`<release>21</release>` en `pom.xml`)
* Maven 3.9+
* Jakarta EE 10 / Jakarta Servlet 6.0 / Jakarta RS 3.1
* Jersey 3.1.5 (JAX-RS, servlet `org.glassfish.jersey.servlet.ServletContainer`)
* Empaquetado `war` (`WebContent` como `warSourceDirectory`)
* Servidor: Apache Tomcat 10.1+ o TomEE 10 (soporta Jakarta EE 10)
* Base de datos: MySQL 8 (`mysql-connector-java 8.0.33`)
* Librerías: Gson, JJWT 0.9.0, Jakarta Mail 2.0.1, POI 3.17, iText 2.1.7, Tika 1.19, SLF4J 2.0.13

> Nota: el README anterior indicaba Java 1.8, está desactualizado. El `pom.xml` actual compila con Java 21.

## Estructura del proyecto

```
src/com/vazjim/controlasistencias/
├── conexion/       # Conexion.java (pool JNDI), CorsFilter.java
├── correo/         # CorreoElectronico.java, CorreosAEnviar.java
├── logica/         # Acceso a BD por entidad (Usuario, Clase, Asistencia, etc.)
├── modelo/         # POJOs: Usuario, Clase, Inscripcion, Mensaje, etc.
├── servicios/      # Endpoints JAX-RS (@Path)
└── utilidades/     # Propiedades.java, Utilidades.java
src/main/resources/ # config.properties, msjerrores.properties
WebContent/
├── WEB-INF/web.xml # Jersey en /api/* + CorsFilter
└── recursos/
```

## Endpoints principales

Prefijo común: `/api`

| Recurso | Path | Descripción |
|---|---|---|
| Login | `IniciarSesion/iniciar-sesion` | Inicio de sesión app móvil |
| Usuarios | `Usuarios`, `Usuarios/entrenadores`, `Usuarios/actualizar-datos-perfil`, `Usuarios/cambio-contrasenia`, `Usuarios/falta-pago/{id}`, `Usuarios/multas/{id}`, `Usuarios/faltas/{id}`, etc. | CRUD usuarios, perfil, multas, faltas, bloqueo por pago |
| Clases | `Clases`, `Clases/por-fecha/{fecha}`, `Clases/por-fecha/{fecha}/{idUsuario}`, `Clases/esta-en-clase/...` | Alta/baja clases, consulta por fecha/usuario/profesor |
| Asistencias | `AsistenciaClases/{idClase}`, `AsistenciaClases/lugares/{idClase}`, `AsistenciaClases/asistencias-usuario/{idUsuario}` | Check-in / check-out, lugares, validaciones |
| Inscripciones | `Inscripciones`, `Inscripciones/tipos-inscripcion`, `Inscripciones/tipos-pago` | Inscripciones y catálogos de pago |
| Lista espera | `lista-espera/{idClase}/{idUsuario}` | Alta/baja lista de espera |
| Asuetos | `Asuetos` | Días festivos / suspensión de clases |
| Configuraciones | `Configuraciones`, `Configuraciones/por-identificado/{id}` | Parámetros generales |
| Log | `LogInterno` | Log interno |
| Test | `test` | Endpoint de prueba |

## Requisitos previos

1. JDK 21
2. Maven 3.9+
3. MySQL 8 con la BD de control de asistencias creada
4. Tomcat 10.1+ (o TomEE 10)
5. Driver MySQL disponible para el pool del servidor

## Configuración

1. **DataSource JNDI (obligatorio):**
   `Conexion.java` busca `java:comp/env/jdbc/control_asistencias-lua`.
   Definir el recurso en Tomcat (`conf/context.xml` o `META-INF/context.xml`):
   ```xml
   <Resource name="jdbc/control_asistencias-lua"
             auth="Container"
             type="javax.sql.DataSource"
             driverClassName="com.mysql.cj.jdbc.Driver"
             url="jdbc:mysql://localhost:3306/control_asistencias?useSSL=false&amp;serverTimezone=UTC"
             username="TU_USUARIO"
             password="TU_PASSWORD"
             maxTotal="50" maxIdle="20" maxWaitMillis="10000" />
   ```

2. **Propiedades:**
   `src/main/resources/config.properties`:
   ```properties
   id_sesion = "wod"
   correo = black.crossfit.lp@gmail.com
   correo_pass = Blackcrossfitlp.
   compania = lua
   ```
   Ajustar correo/clave de envío y compañía antes de compilar.

## Cómo compilar

```bash
mvn clean package
```

Genera: `target/ClockInApi-0.0.1.war`

Solo compilar sin empaquetar:

```bash
mvn clean compile
```

## Cómo iniciar / desplegar

1. Compilar el WAR (ver arriba).
2. Copiar `target/ClockInApi-0.0.1.war` a `webapps/` de Tomcat (renombrar a `ClockInApi.war` si se quiere la URL corta).
3. Verificar el recurso JNDI MySQL y que MySQL esté arriba.
4. Arrancar Tomcat:
   ```bash
   $CATALINA_HOME/bin/startup.sh
   # Windows:
   %CATALINA_HOME%\bin\startup.bat
   ```
5. Probar:
   * `http://localhost:8080/ClockInApi/api/test`
   * Login: `POST http://localhost:8080/ClockInApi/api/IniciarSesion/iniciar-sesion`

Los logs JNDI se imprimen al arranque (`Conexion.listContext`) para verificar el pool.
