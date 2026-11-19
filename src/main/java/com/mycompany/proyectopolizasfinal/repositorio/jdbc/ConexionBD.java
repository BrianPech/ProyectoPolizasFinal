package com.mycompany.proyectopolizasfinal.repositorio.jdbc;

import com.mycompany.proyectopolizasfinal.repositorio.PersistenciaException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Proveedor centralizado de conexiones JDBC hacia MySQL.
 * Busca configuración en variables de entorno ({@code DB_URL}, {@code DB_USER}, {@code DB_PASSWORD})
 * o en {@code db.properties} (directorio de trabajo o classpath).
 */
public final class ConexionBD {

    private final String url;
    private final String usuario;
    private final String password;

    public ConexionBD(String url, String usuario, String password) {
        this.url = normalizar(url);
        this.usuario = normalizar(usuario);
        this.password = password != null ? password : "";
    }

    public static ConexionBD desdeEntornoOArchivo() {
        Properties props = cargarProperties();
        String url = primeroNoVacio(System.getenv("DB_URL"), props.getProperty("db.url"));
        String user = primeroNoVacio(System.getenv("DB_USER"), props.getProperty("db.user"));
        String pass = primeroNoVacio(System.getenv("DB_PASSWORD"), props.getProperty("db.password"));
        return new ConexionBD(url, user, pass);
    }

    public boolean estaConfigurada() {
        return url != null && usuario != null;
    }

    public Connection abrirConexion() {
        if (!estaConfigurada()) {
            throw new PersistenciaException(
                    "Base de datos no configurada. Define DB_URL/DB_USER o crea db.properties.");
        }
        try {
            return DriverManager.getConnection(url, usuario, password);
        } catch (SQLException ex) {
            throw new PersistenciaException("No se pudo abrir conexión con MySQL: " + ex.getMessage(), ex);
        }
    }

    /** Intenta abrir y cerrar una conexión; devuelve el error si falla o vacío si todo está bien. */
    public java.util.Optional<String> probarConexion() {
        try (Connection con = abrirConexion()) {
            return con.isValid(3) ? java.util.Optional.empty()
                    : java.util.Optional.of("La conexión con MySQL no respondió.");
        } catch (PersistenciaException | SQLException ex) {
            return java.util.Optional.of(ex.getMessage());
        }
    }

    private static Properties cargarProperties() {
        Properties props = new Properties();
        Path local = Path.of("db.properties");
        if (Files.isRegularFile(local)) {
            try (InputStream in = Files.newInputStream(local)) {
                props.load(in);
                return props;
            } catch (IOException ignored) {}
        }
        try (InputStream in = ConexionBD.class.getResourceAsStream("/db.properties")) {
            if (in != null) {
                props.load(in);
            }
        } catch (IOException ignored) {}
        return props;
    }

    private static String primeroNoVacio(String a, String b) {
        String na = normalizar(a);
        return na != null ? na : normalizar(b);
    }

    private static String normalizar(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }
}
