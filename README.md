# ProyectoPolizasFinal

Sistema de escritorio en Java Swing para la gestión de pólizas de seguros, clientes, vehículos, pagos, avisos de vencimiento, reportes en PDF, catálogos y usuarios. Estructurado con patrón **Repositorio + MVC** y soporte multiplataforma (**macOS** y **Windows/Linux**) usando FlatLaf.

## Requisitos

- **JDK 26** (configurado en `pom.xml` mediante `maven.compiler.release`). Si usas **JDK 25**, pasa `-Dmaven.compiler.release=25` a los comandos de Maven.
- **Apache Maven 3.9+** (o usar el Maven integrado en Apache NetBeans).

Si no tienes Maven instalado en terminal (macOS):

```bash
brew install maven
```

En Windows puedes instalarlo con Winget o Chocolatey, o abrir directamente el proyecto en Apache NetBeans / IntelliJ IDEA:

```powershell
winget install Apache.Maven
```

## Base de datos (MySQL / XAMPP)

1. Enciende **MySQL** desde el panel de XAMPP.
2. Crea la base ejecutando [`database/polizas_db.sql`](database/polizas_db.sql) (en phpMyAdmin → *Importar*, o por consola):
   ```bash
   mysql -u root < database/polizas_db.sql
   ```
   > ⚠️ El script hace `DROP DATABASE IF EXISTS polizas_db`: borra la base y la vuelve a crear vacía (con roles, tipos de póliza, coberturas y marcas iniciales).
3. Copia `db.properties.example` como `db.properties` en la raíz del proyecto y ajusta usuario/contraseña si no usas el `root` sin contraseña de XAMPP. `db.properties` está en `.gitignore`, cada quien tiene el suyo.
4. Ejecuta la app. Si no logra conectarse, avisa con un diálogo y trabaja con datos en memoria.

**Primer acceso:** con la tabla `usuario` vacía, el login deja entrar con cualquier usuario y contraseña para crear al primer administrador en el módulo *Usuarios*. A partir de ahí se valida la contraseña (se guarda con hash PBKDF2, nunca en texto plano).

**Orden para capturar datos:** las pólizas se ligan por texto a registros existentes, así que primero da de alta la **aseguradora** (Catálogos), el **cliente** y el **vehículo**. En el asistente de pólizas escribe el cliente con su nombre completo o RFC, y el vehículo con sus placas o VIN (ej. `Nissan Versa · ABC-123`). En Pagos y Avisos, la póliza se indica con su número (ej. `POL-2026-0142 · Juan Pérez`).

## Instalar dependencias de Maven

Desde la raíz del proyecto, descarga e instala todas las dependencias declaradas en `pom.xml`:

```bash
mvn dependency:resolve
```

O compila directamente (descarga las dependencias y compila el proyecto):

```bash
mvn compile
```

> **Si usas el Maven integrado de Apache NetBeans en macOS:**
> ```bash
> "/Applications/Apache NetBeans.app/Contents/Resources/netbeans/java/maven/bin/mvn" -Dmaven.compiler.release=25 compile
> ```

### Dependencias principales (`pom.xml`)

- `com.formdev:flatlaf` (3.7.2) y `flatlaf-extras` (3.7.2) — Tema moderno e íconos SVG.
- `com.formdev:flatlaf-fonts-inter` (4.1) y `flatlaf-fonts-roboto` (2.137) — Tipografías.
- `com.github.librepdf:openpdf` (3.0.5) — Exportación de reportes a PDF.
- `com.mysql:mysql-connector-j` (8.0.33) — Driver JDBC para la conexión a MySQL.
- `org.junit.jupiter:junit-jupiter` (5.11.4) — Pruebas unitarias.

## Ejecutar pruebas y aplicación

Ejecutar las pruebas unitarias:

```bash
mvn test
```

*(Con JDK 25: `mvn -Dmaven.compiler.release=25 test`)*

Compilar y ejecutar la aplicación desde Maven:

```bash
mvn compile exec:java
```

## Estructura del proyecto

- `modelo/` — Records de dominio (`Cliente`, `Vehiculo`, `Poliza`, `MovimientoPago`, `CatalogoItem`, `Usuario`, `Reporte`, `Cancelacion`).
- `repositorio/` — Contratos (`RepositorioCrud`, `UsuarioRepositorio`), implementación en memoria e implementaciones JDBC para MySQL (`repositorio/jdbc/`: `ConexionBD`, `BaseRepositorioJdbc` y un `*RepositorioJdbc` por módulo).
- `controlador/` — Controladores MVC desacoplados de Swing (`ControladorSesion`, `ControladorInicio`, `ControladorPolizas`, etc.).
- `vistas/` y `componentes/` — Interfaces Swing, `PanelListadoBase`, `ModalFormularioBase` y `TablaOperativa`.
- `util/` — Utilidades multiplataforma (`EstiloUI`) y formateo (`Formatos`).
