package com.mycompany.proyectopolizasfinal.repositorio.jdbc;

import com.mycompany.proyectopolizasfinal.modelo.Reporte;
import com.mycompany.proyectopolizasfinal.repositorio.ContextoSesion;
import com.mycompany.proyectopolizasfinal.repositorio.PersistenciaException;
import com.mycompany.proyectopolizasfinal.repositorio.RepositorioCrud;
import com.mycompany.proyectopolizasfinal.util.Formatos;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Tabla {@code reporte}. Como la tabla sólo tiene tipo, fecha, autor y {@code parametros_filtros},
 * los demás datos del historial (folio, categoría, rango, registros, estado) se guardan dentro de
 * {@code parametros_filtros}, una línea {@code clave=valor} por dato.
 */
public class ReporteRepositorioJdbc extends BaseRepositorioJdbc implements RepositorioCrud<Reporte> {

    private static final String SELECT_BASE = """
            SELECT r.*, u.nombre AS usuario_nombre, u.usuario AS username
            FROM reporte r
            JOIN usuario u ON u.id_usuario = r.id_usuario
            """;

    public ReporteRepositorioJdbc(ConexionBD conexionBD) {
        super(conexionBD);
    }

    @Override
    public List<Reporte> listar() {
        return consultarLista(SELECT_BASE + " ORDER BY r.fecha_generacion DESC, r.id_reporte DESC", this::mapear);
    }

    @Override
    public Optional<Reporte> buscarPorId(int id) {
        return consultarUno(SELECT_BASE + " WHERE r.id_reporte = ?", this::mapear, id);
    }

    @Override
    public Reporte guardar(Reporte r) {
        int idUsuario = consultarEntero("SELECT id_usuario FROM usuario WHERE usuario = ?", r.username())
                .orElse(ContextoSesion.idUsuarioActual());
        if (idUsuario <= 0) {
            throw new PersistenciaException(
                    "Para generar reportes inicia sesión con un usuario registrado en la base de datos.");
        }
        int id = ejecutarInsertConId(
                "INSERT INTO reporte (id_usuario, tipo, fecha_generacion, parametros_filtros) VALUES (?, ?, NOW(), ?)",
                idUsuario, r.tipo(), serializar(r));
        return buscarPorId(id).orElse(r.conId(id));
    }

    @Override
    public Reporte actualizar(Reporte r) {
        int filas = ejecutarUpdate("UPDATE reporte SET tipo = ?, parametros_filtros = ? WHERE id_reporte = ?",
                r.tipo(), serializar(r), r.id());
        if (filas == 0) {
            throw new PersistenciaException("No se encontró el reporte con id=" + r.id());
        }
        return buscarPorId(r.id()).orElse(r);
    }

    @Override
    public void eliminar(int id) {
        ejecutarUpdate("DELETE FROM reporte WHERE id_reporte = ?", id);
    }

    private static String serializar(Reporte r) {
        return "folio=" + limpio(r.folio()) + "\n"
                + "categoria=" + limpio(r.categoria()) + "\n"
                + "rango=" + limpio(r.rangoFechas()) + "\n"
                + "registros=" + r.registros() + "\n"
                + "estado=" + limpio(r.estado()) + "\n"
                + "filtros=" + limpio(r.parametrosFiltros());
    }

    private static String limpio(String s) {
        return s == null ? "" : s.replace('\n', ' ').replace('\r', ' ');
    }

    private static Map<String, String> deserializar(String texto) {
        Map<String, String> m = new LinkedHashMap<>();
        if (texto == null) {
            return m;
        }
        for (String linea : texto.split("\\R")) {
            int eq = linea.indexOf('=');
            if (eq > 0) {
                m.put(linea.substring(0, eq).trim(), linea.substring(eq + 1).trim());
            }
        }
        return m;
    }

    private Reporte mapear(ResultSet rs) throws SQLException {
        int id = rs.getInt("id_reporte");
        String crudo = rs.getString("parametros_filtros");
        Map<String, String> p = deserializar(crudo);
        LocalDateTime fecha = rs.getObject("fecha_generacion", LocalDateTime.class);
        String fechaTxt = fecha == null ? "-"
                : Formatos.formatearFechaCorta(fecha.toLocalDate())
                        + String.format(" · %02d:%02d", fecha.getHour(), fecha.getMinute());
        int registros;
        try {
            registros = Integer.parseInt(p.getOrDefault("registros", "0"));
        } catch (NumberFormatException ex) {
            registros = 0;
        }
        return new Reporte(
                id,
                p.getOrDefault("folio", String.format("RPT-%04d", id)),
                rs.getString("tipo"),
                p.getOrDefault("categoria", "Operación"),
                fechaTxt,
                rs.getString("usuario_nombre"),
                rs.getString("username"),
                p.getOrDefault("rango", "-"),
                p.getOrDefault("filtros", p.isEmpty() ? crudo : ""),
                registros,
                p.getOrDefault("estado", "Generado"));
    }
}
