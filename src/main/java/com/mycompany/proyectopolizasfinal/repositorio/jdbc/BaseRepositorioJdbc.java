package com.mycompany.proyectopolizasfinal.repositorio.jdbc;

import com.mycompany.proyectopolizasfinal.repositorio.PersistenciaException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Base utilitaria para implementar repositorios JDBC concretos (SELECT/INSERT/UPDATE/DELETE)
 * sin repetir manejo de conexiones, PreparedStatement ni ResultSet.
 */
public abstract class BaseRepositorioJdbc {

    @FunctionalInterface
    public interface MapeadorFila<T> {
        T mapear(ResultSet rs) throws SQLException;
    }

    protected final ConexionBD conexionBD;

    protected BaseRepositorioJdbc(ConexionBD conexionBD) {
        this.conexionBD = conexionBD;
    }

    protected final <T> List<T> consultarLista(String sql, MapeadorFila<T> mapper, Object... params) {
        try (Connection con = conexionBD.abrirConexion();
             PreparedStatement ps = preparar(con, sql, false, params);
             ResultSet rs = ps.executeQuery()) {
            List<T> resultado = new ArrayList<>();
            while (rs.next()) {
                resultado.add(mapper.mapear(rs));
            }
            return resultado;
        } catch (SQLException ex) {
            throw new PersistenciaException("Error en consulta SQL: " + ex.getMessage(), ex);
        }
    }

    protected final <T> Optional<T> consultarUno(String sql, MapeadorFila<T> mapper, Object... params) {
        List<T> lista = consultarLista(sql, mapper, params);
        return lista.isEmpty() ? Optional.empty() : Optional.of(lista.get(0));
    }

    protected final int ejecutarInsertConId(String sql, Object... params) {
        try (Connection con = conexionBD.abrirConexion();
             PreparedStatement ps = preparar(con, sql, true, params)) {
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
            throw new PersistenciaException("El INSERT no devolvió llave generada.");
        } catch (SQLException ex) {
            throw new PersistenciaException("Error al insertar en BD: " + ex.getMessage(), ex);
        }
    }

    protected final int ejecutarUpdate(String sql, Object... params) {
        try (Connection con = conexionBD.abrirConexion();
             PreparedStatement ps = preparar(con, sql, false, params)) {
            return ps.executeUpdate();
        } catch (SQLException ex) {
            throw new PersistenciaException("Error al actualizar en BD: " + ex.getMessage(), ex);
        }
    }

    private static PreparedStatement preparar(Connection con, String sql, boolean generatedKeys, Object... params)
            throws SQLException {
        PreparedStatement ps = generatedKeys
                ? con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
                : con.prepareStatement(sql);
        for (int i = 0; i < params.length; i++) {
            ps.setObject(i + 1, params[i]);
        }
        return ps;
    }
}
