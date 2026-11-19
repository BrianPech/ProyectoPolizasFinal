package com.mycompany.proyectopolizasfinal.repositorio.jdbc;

import com.mycompany.proyectopolizasfinal.modelo.Usuario;
import com.mycompany.proyectopolizasfinal.repositorio.PersistenciaException;
import com.mycompany.proyectopolizasfinal.repositorio.UsuarioRepositorio;
import com.mycompany.proyectopolizasfinal.util.Contrasenas;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Tablas {@code usuario} + {@code rol}.
 */
public class UsuarioRepositorioJdbc extends BaseRepositorioJdbc implements UsuarioRepositorio {

    /** Hash imposible de verificar; se usa hasta que se asigne una contraseña real. */
    private static final String HASH_SIN_PASSWORD = "!";

    private static final String SELECT_BASE = """
            SELECT u.id_usuario, u.nombre, u.usuario, u.estado,
                   r.nombre_rol, r.descripcion AS descripcion_rol,
                   (SELECT COUNT(*) FROM poliza p WHERE p.id_usuario = u.id_usuario) AS polizas
            FROM usuario u
            JOIN rol r ON r.id_rol = u.id_rol
            """;

    public UsuarioRepositorioJdbc(ConexionBD conexionBD) {
        super(conexionBD);
    }

    @Override
    public List<Usuario> listar() {
        return consultarLista(SELECT_BASE + " ORDER BY u.id_usuario DESC", this::mapear);
    }

    @Override
    public Optional<Usuario> buscarPorId(int id) {
        return consultarUno(SELECT_BASE + " WHERE u.id_usuario = ?", this::mapear, id);
    }

    @Override
    public Optional<Usuario> buscarPorUsername(String username) {
        if (username == null || username.isBlank()) {
            return Optional.empty();
        }
        return consultarUno(SELECT_BASE + " WHERE u.usuario = ?", this::mapear, username.trim());
    }

    @Override
    public boolean existeUsername(String username, int excluirId) {
        if (username == null || username.isBlank()) {
            return false;
        }
        return consultarEntero("SELECT 1 FROM usuario WHERE usuario = ? AND id_usuario <> ?",
                username.trim(), excluirId).isPresent();
    }

    @Override
    public Usuario guardar(Usuario u) {
        int idRol = idRol(u.rol());
        int id = ejecutarInsertConId(
                "INSERT INTO usuario (id_rol, nombre, usuario, contrasena_hash, estado) VALUES (?, ?, ?, ?, ?)",
                idRol, u.nombre(), u.username(), HASH_SIN_PASSWORD, estado(u.estado()));
        return buscarPorId(id).orElse(u.conId(id));
    }

    @Override
    public Usuario actualizar(Usuario u) {
        int filas = ejecutarUpdate(
                "UPDATE usuario SET id_rol = ?, nombre = ?, usuario = ?, estado = ? WHERE id_usuario = ?",
                idRol(u.rol()), u.nombre(), u.username(), estado(u.estado()), u.id());
        if (filas == 0) {
            throw new PersistenciaException("No se encontró el usuario con id=" + u.id());
        }
        return buscarPorId(u.id()).orElse(u);
    }

    @Override
    public void eliminar(int id) {
        ejecutarUpdate("DELETE FROM usuario WHERE id_usuario = ?", id);
    }

    @Override
    public void establecerPassword(int idUsuario, char[] password) {
        if (password == null || password.length == 0) {
            return;
        }
        ejecutarUpdate("UPDATE usuario SET contrasena_hash = ? WHERE id_usuario = ?",
                Contrasenas.hashear(password), idUsuario);
    }

    @Override
    public boolean verificarPassword(String username, char[] password) {
        Optional<String> hash = consultarUno(
                "SELECT contrasena_hash FROM usuario WHERE usuario = ?",
                rs -> rs.getString(1), username == null ? "" : username.trim());
        return hash.isPresent() && Contrasenas.verificar(password, hash.get());
    }

    private int idRol(String nombreRol) {
        return requerirId("No existe el rol '" + nombreRol + "' en la tabla rol.",
                "SELECT id_rol FROM rol WHERE nombre_rol = ?", nombreRol);
    }

    private static String estado(String estado) {
        return "Inactivo".equalsIgnoreCase(estado) ? "Inactivo" : "Activo";
    }

    private Usuario mapear(ResultSet rs) throws SQLException {
        return new Usuario(
                rs.getInt("id_usuario"),
                rs.getString("nombre"),
                rs.getString("usuario"),
                rs.getString("nombre_rol"),
                rs.getString("descripcion_rol"),
                rs.getInt("polizas"),
                rs.getString("estado"));
    }
}
