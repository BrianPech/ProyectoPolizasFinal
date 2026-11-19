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
- `com.mysql:mysql-connector-j` (8.0.33) — Driver JDBC para conexión futura a MySQL.
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
- `repositorio/` — Contratos (`RepositorioCrud`, `UsuarioRepositorio`), implementación inicial en memoria y puente JDBC (`repositorio/jdbc/ConexionBD.java`, `BaseRepositorioJdbc.java`) listo para conectar MySQL según `db_scheme.md`.
- `controlador/` — Controladores MVC desacoplados de Swing (`ControladorSesion`, `ControladorInicio`, `ControladorPolizas`, etc.).
- `vistas/` y `componentes/` — Interfaces Swing, `PanelListadoBase`, `ModalFormularioBase` y `TablaOperativa`.
- `util/` — Utilidades multiplataforma (`EstiloUI`) y formateo (`Formatos`).
