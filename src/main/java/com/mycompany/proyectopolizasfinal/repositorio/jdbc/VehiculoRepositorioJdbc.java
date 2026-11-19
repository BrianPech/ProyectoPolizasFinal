package com.mycompany.proyectopolizasfinal.repositorio.jdbc;

import com.mycompany.proyectopolizasfinal.modelo.Vehiculo;
import com.mycompany.proyectopolizasfinal.repositorio.PersistenciaException;
import com.mycompany.proyectopolizasfinal.repositorio.RepositorioCrud;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Tablas {@code vehiculo} + {@code marca}. La póliza vigente y su estatus se calculan
 * a partir de la póliza más reciente no cancelada del vehículo.
 */
public class VehiculoRepositorioJdbc extends BaseRepositorioJdbc implements RepositorioCrud<Vehiculo> {

    private static final String SELECT_BASE = """
            SELECT v.*, m.nombre_marca,
                   p.numero_poliza, p.estado AS estado_poliza,
                   a.nombre AS aseguradora,
                   TRIM(CONCAT(c.nombre, ' ', COALESCE(c.apellidos, ''))) AS cliente
            FROM vehiculo v
            JOIN marca m ON m.id_marca = v.id_marca
            LEFT JOIN poliza p ON p.id_poliza = (
                SELECT p2.id_poliza FROM poliza p2
                WHERE p2.id_vehiculo = v.id_vehiculo AND p2.estado <> 'Cancelada'
                ORDER BY p2.fecha_vencimiento DESC, p2.id_poliza DESC LIMIT 1)
            LEFT JOIN compania_aseguradora a ON a.id_aseguradora = p.id_aseguradora
            LEFT JOIN cliente c ON c.id_cliente = p.id_cliente
            """;

    public VehiculoRepositorioJdbc(ConexionBD conexionBD) {
        super(conexionBD);
    }

    @Override
    public List<Vehiculo> listar() {
        return consultarLista(SELECT_BASE + " ORDER BY v.id_vehiculo DESC", this::mapear);
    }

    @Override
    public Optional<Vehiculo> buscarPorId(int id) {
        return consultarUno(SELECT_BASE + " WHERE v.id_vehiculo = ?", this::mapear, id);
    }

    @Override
    public Vehiculo guardar(Vehiculo v) {
        int id = ejecutarInsertConId("""
                INSERT INTO vehiculo (id_marca, placas, numero_serie_vin, modelo, anio, color, uso, activo)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """,
                idMarca(v.marca()), v.placas(), v.vin(), v.modelo(), v.anio(), v.color(), uso(v), activo(v));
        return buscarPorId(id).orElse(v.conId(id));
    }

    @Override
    public Vehiculo actualizar(Vehiculo v) {
        int filas = ejecutarUpdate("""
                UPDATE vehiculo SET id_marca = ?, placas = ?, numero_serie_vin = ?, modelo = ?, anio = ?,
                                    color = ?, uso = ?, activo = ?
                WHERE id_vehiculo = ?
                """,
                idMarca(v.marca()), v.placas(), v.vin(), v.modelo(), v.anio(), v.color(), uso(v), activo(v),
                v.id());
        if (filas == 0) {
            throw new PersistenciaException("No se encontró el vehículo con id=" + v.id());
        }
        return buscarPorId(v.id()).orElse(v);
    }

    @Override
    public void eliminar(int id) {
        ejecutarUpdate("DELETE FROM vehiculo WHERE id_vehiculo = ?", id);
    }

    /** Busca la marca por nombre; si no existe en el catálogo la da de alta. */
    private int idMarca(String nombreMarca) {
        if (nombreMarca == null || nombreMarca.isBlank()) {
            throw new PersistenciaException("Selecciona la marca del vehículo.");
        }
        String nombre = nombreMarca.trim();
        return consultarEntero("SELECT id_marca FROM marca WHERE nombre_marca = ?", nombre)
                .orElseGet(() -> ejecutarInsertConId("INSERT INTO marca (nombre_marca) VALUES (?)", nombre));
    }

    private static String uso(Vehiculo v) {
        return "Comercial".equalsIgnoreCase(v.uso()) ? "Comercial" : "Particular";
    }

    private static boolean activo(Vehiculo v) {
        return !"Baja de unidad".equalsIgnoreCase(v.aseguradoraCliente());
    }

    private Vehiculo mapear(ResultSet rs) throws SQLException {
        boolean activo = rs.getBoolean("activo");
        String numeroPoliza = rs.getString("numero_poliza");
        String estadoPoliza = rs.getString("estado_poliza");

        String poliza;
        String asegCliente;
        String estatus;
        if (!activo) {
            poliza = "Sin póliza activa";
            asegCliente = "Baja de unidad";
            estatus = "Sin póliza";
        } else if (numeroPoliza == null || "Vencida".equals(estadoPoliza)) {
            poliza = numeroPoliza == null ? "Sin póliza activa" : numeroPoliza + " (vencida)";
            asegCliente = "Disponible para asignar";
            estatus = "Sin póliza";
        } else {
            poliza = numeroPoliza;
            asegCliente = rs.getString("aseguradora") + " · " + rs.getString("cliente");
            estatus = "Por Vencer".equals(estadoPoliza) ? "Por Vencer" : "Asegurado";
        }

        return new Vehiculo(
                rs.getInt("id_vehiculo"),
                rs.getString("nombre_marca"),
                rs.getString("modelo"),
                rs.getString("placas"),
                rs.getString("color"),
                rs.getString("numero_serie_vin"),
                rs.getString("uso"),
                poliza,
                asegCliente,
                rs.getInt("anio"),
                estatus);
    }
}
