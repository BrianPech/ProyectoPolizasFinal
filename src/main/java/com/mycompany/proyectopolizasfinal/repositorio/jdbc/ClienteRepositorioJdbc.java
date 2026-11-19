package com.mycompany.proyectopolizasfinal.repositorio.jdbc;

import com.mycompany.proyectopolizasfinal.modelo.Cliente;
import com.mycompany.proyectopolizasfinal.repositorio.PersistenciaException;
import com.mycompany.proyectopolizasfinal.repositorio.RepositorioCrud;
import com.mycompany.proyectopolizasfinal.util.Formatos;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Tabla {@code cliente}. Las columnas de resumen (pólizas activas, prima, estatus de adeudo)
 * se calculan a partir de {@code poliza} y {@code pago}.
 */
public class ClienteRepositorioJdbc extends BaseRepositorioJdbc implements RepositorioCrud<Cliente> {

    private static final String SELECT_BASE = """
            SELECT c.*,
                   (SELECT COUNT(*) FROM poliza p
                     WHERE p.id_cliente = c.id_cliente AND p.estado IN ('Activa','Por Vencer')) AS polizas_activas,
                   (SELECT COALESCE(SUM(p.prima_total), 0) FROM poliza p
                     WHERE p.id_cliente = c.id_cliente AND p.estado IN ('Activa','Por Vencer')) AS prima_activa,
                   (SELECT COUNT(*) FROM pago g JOIN poliza p ON p.id_poliza = g.id_poliza
                     WHERE p.id_cliente = c.id_cliente AND g.estado = 'Vencido') AS pagos_vencidos
            FROM cliente c
            """;

    public ClienteRepositorioJdbc(ConexionBD conexionBD) {
        super(conexionBD);
    }

    @Override
    public List<Cliente> listar() {
        return consultarLista(SELECT_BASE + " ORDER BY c.id_cliente DESC", this::mapear);
    }

    @Override
    public Optional<Cliente> buscarPorId(int id) {
        return consultarUno(SELECT_BASE + " WHERE c.id_cliente = ?", this::mapear, id);
    }

    @Override
    public Cliente guardar(Cliente c) {
        int id = ejecutarInsertConId("""
                INSERT INTO cliente (tipo_persona, nombre, apellidos, rfc, telefono, correo, direccion,
                                     fecha_registro, activo)
                VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_DATE, ?)
                """,
                tipo(c), c.nombre(), apellidos(c), c.rfc(), c.telefono(), c.correo(), c.direccion(),
                activo(c));
        return buscarPorId(id).orElse(c.conId(id));
    }

    @Override
    public Cliente actualizar(Cliente c) {
        int filas = ejecutarUpdate("""
                UPDATE cliente SET tipo_persona = ?, nombre = ?, apellidos = ?, rfc = ?, telefono = ?,
                                   correo = ?, direccion = ?, activo = ?
                WHERE id_cliente = ?
                """,
                tipo(c), c.nombre(), apellidos(c), c.rfc(), c.telefono(), c.correo(), c.direccion(),
                activo(c), c.id());
        if (filas == 0) {
            throw new PersistenciaException("No se encontró el cliente con id=" + c.id());
        }
        return buscarPorId(c.id()).orElse(c);
    }

    @Override
    public void eliminar(int id) {
        ejecutarUpdate("DELETE FROM cliente WHERE id_cliente = ?", id);
    }

    private static String tipo(Cliente c) {
        return "Moral".equalsIgnoreCase(c.tipoPersona()) ? "Moral" : "Fisica";
    }

    private static String apellidos(Cliente c) {
        return "Moral".equalsIgnoreCase(c.tipoPersona()) ? null : vacioANull(c.apellidos());
    }

    private static boolean activo(Cliente c) {
        return !"Inactivo".equalsIgnoreCase(c.estatus());
    }

    private Cliente mapear(ResultSet rs) throws SQLException {
        int activas = rs.getInt("polizas_activas");
        String estatus;
        if (!rs.getBoolean("activo")) {
            estatus = "Inactivo";
        } else if (rs.getInt("pagos_vencidos") > 0) {
            estatus = "Con adeudo";
        } else {
            estatus = "Al corriente";
        }
        String resumen = activas == 0 ? "Sin pólizas"
                : activas + (activas == 1 ? " póliza activa" : " pólizas activas");
        String apellidos = rs.getString("apellidos");
        return new Cliente(
                rs.getInt("id_cliente"),
                rs.getString("nombre"),
                apellidos == null ? "" : apellidos,
                rs.getString("rfc"),
                rs.getString("tipo_persona"),
                rs.getString("telefono"),
                rs.getString("correo"),
                rs.getString("direccion"),
                activas,
                resumen,
                Formatos.formatearMoneda(rs.getDouble("prima_activa")),
                estatus);
    }
}
