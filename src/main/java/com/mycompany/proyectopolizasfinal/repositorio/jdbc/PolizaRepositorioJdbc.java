package com.mycompany.proyectopolizasfinal.repositorio.jdbc;

import com.mycompany.proyectopolizasfinal.modelo.Poliza;
import com.mycompany.proyectopolizasfinal.repositorio.ContextoSesion;
import com.mycompany.proyectopolizasfinal.repositorio.PersistenciaException;
import com.mycompany.proyectopolizasfinal.repositorio.RepositorioCrud;
import com.mycompany.proyectopolizasfinal.util.Formatos;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Tabla {@code poliza} (con sus llaves a cliente, vehículo, aseguradora, tipo y agente)
 * y su trámite de cancelación en {@code cancelacion_poliza}.
 * <p>
 * El asistente de pólizas captura cliente y vehículo como texto, así que aquí se resuelven:
 * <ul>
 *   <li>Cliente: por nombre completo, nombre/razón social o RFC.</li>
 *   <li>Vehículo: por placas o VIN (acepta "Nissan Versa · ABC-123").</li>
 *   <li>Aseguradora y tipo de póliza: por nombre en sus catálogos.</li>
 *   <li>Agente vendedor: el usuario con sesión abierta.</li>
 * </ul>
 */
public class PolizaRepositorioJdbc extends BaseRepositorioJdbc implements RepositorioCrud<Poliza> {

    private static final String SELECT_BASE = """
            SELECT p.*,
                   TRIM(CONCAT(c.nombre, ' ', COALESCE(c.apellidos, ''))) AS cliente,
                   CONCAT(m.nombre_marca, ' ', v.modelo, ' · ', v.placas) AS vehiculo,
                   a.nombre AS aseguradora,
                   t.nombre_tipo AS tipo,
                   cp.id_cancelacion, cp.motivo, cp.devolucion_prima
            FROM poliza p
            JOIN cliente c ON c.id_cliente = p.id_cliente
            JOIN vehiculo v ON v.id_vehiculo = p.id_vehiculo
            JOIN marca m ON m.id_marca = v.id_marca
            JOIN compania_aseguradora a ON a.id_aseguradora = p.id_aseguradora
            JOIN tipo_poliza t ON t.id_tipo_poliza = p.id_tipo_poliza
            LEFT JOIN cancelacion_poliza cp ON cp.id_poliza = p.id_poliza
            """;

    public PolizaRepositorioJdbc(ConexionBD conexionBD) {
        super(conexionBD);
    }

    @Override
    public List<Poliza> listar() {
        return consultarLista(SELECT_BASE + " ORDER BY p.id_poliza DESC", this::mapear);
    }

    @Override
    public Optional<Poliza> buscarPorId(int id) {
        return consultarUno(SELECT_BASE + " WHERE p.id_poliza = ?", this::mapear, id);
    }

    @Override
    public Poliza guardar(Poliza p) {
        int idAgente = ContextoSesion.idUsuarioActual();
        if (idAgente <= 0) {
            throw new PersistenciaException(
                    "Para emitir pólizas inicia sesión con un usuario registrado en la base de datos.");
        }
        int idCliente = idCliente(p.cliente());
        int idVehiculo = idVehiculo(p.vehiculo());
        int idAseguradora = requerirId(
                "La aseguradora '" + p.aseguradora() + "' no existe. Regístrala en Catálogos.",
                "SELECT id_aseguradora FROM compania_aseguradora WHERE nombre = ?", p.aseguradora());
        int idTipo = requerirId(
                "El tipo de póliza '" + p.tipo() + "' no existe. Regístralo en Catálogos.",
                "SELECT id_tipo_poliza FROM tipo_poliza WHERE nombre_tipo = ?", p.tipo());

        String periodicidad = periodicidad(p.detalleVigencia());
        LocalDate vencimiento = Formatos.parsearFechaCorta(p.vigencia());
        if (vencimiento == null) {
            throw new PersistenciaException("Fecha de vencimiento inválida: " + p.vigencia());
        }
        // El asistente no envía la fecha de inicio: se calcula igual que él la propone por periodicidad.
        LocalDate inicio = vencimiento.minusMonths(mesesDe(periodicidad));

        int id = ejecutarInsertConId("""
                INSERT INTO poliza (numero_poliza, id_tipo_poliza, id_cliente, id_vehiculo, id_aseguradora,
                                    id_usuario, periodicidad_pago, fecha_emision, fecha_inicio,
                                    fecha_vencimiento, prima_total, estado)
                VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_DATE, ?, ?, ?, ?)
                """,
                p.numero(), idTipo, idCliente, idVehiculo, idAseguradora, idAgente, periodicidad,
                inicio, vencimiento, Formatos.parsearMonto(p.prima()), estado(p.estado()));
        return buscarPorId(id).orElse(p.conId(id));
    }

    @Override
    public Poliza actualizar(Poliza p) {
        boolean tieneCancelacion = consultarEntero(
                "SELECT id_cancelacion FROM cancelacion_poliza WHERE id_poliza = ?", p.id()).isPresent();
        if (p.esCancelacion() && !tieneCancelacion) {
            registrarCancelacion(p);
        }
        int filas = ejecutarUpdate("UPDATE poliza SET estado = ? WHERE id_poliza = ?", estado(p.estado()), p.id());
        if (filas == 0) {
            throw new PersistenciaException("No se encontró la póliza con id=" + p.id());
        }
        return buscarPorId(p.id()).orElse(p);
    }

    @Override
    public void eliminar(int id) {
        ejecutarUpdate("DELETE FROM poliza WHERE id_poliza = ?", id);
    }

    /**
     * {@link Poliza#conCancelacion} guarda el motivo en detalleVigencia ("Motivo: ...")
     * y la devolución en vigencia ("Devol. $1,200"). Estado "Cancelada" = solicitud aprobada.
     */
    private void registrarCancelacion(Poliza p) {
        int idUsuario = ContextoSesion.idUsuarioActual();
        if (idUsuario <= 0) {
            throw new PersistenciaException(
                    "Para cancelar pólizas inicia sesión con un usuario registrado en la base de datos.");
        }
        String motivo = p.detalleVigencia() == null ? "" : p.detalleVigencia().replaceFirst("^Motivo:\\s*", "");
        String vig = p.vigencia() == null ? "" : p.vigencia();
        double devolucion = Formatos.parsearMonto(vig.contains("$") ? vig.substring(vig.indexOf('$')) : vig);
        boolean aprobada = "Cancelada".equalsIgnoreCase(p.estado());
        ejecutarUpdate("""
                INSERT INTO cancelacion_poliza (id_poliza, id_usuario_solicita, id_usuario_autoriza,
                                                fecha_solicitud, fecha_resolucion, motivo,
                                                devolucion_prima, estado_solicitud)
                VALUES (?, ?, ?, NOW(), ?, ?, ?, ?)
                """,
                p.id(), idUsuario, aprobada ? idUsuario : null,
                aprobada ? java.time.LocalDateTime.now() : null,
                motivo.isBlank() ? "Sin motivo" : motivo, devolucion, aprobada ? "Aprobada" : "Pendiente");
    }

    private int idCliente(String texto) {
        String t = texto == null ? "" : texto.trim();
        return requerirId(
                "No existe el cliente '" + t + "'. Regístralo primero en Clientes (usa su nombre completo o RFC).",
                """
                SELECT id_cliente FROM cliente
                WHERE TRIM(CONCAT(nombre, ' ', COALESCE(apellidos, ''))) = ? OR nombre = ? OR rfc = ?
                ORDER BY activo DESC, id_cliente LIMIT 1
                """, t, t, t);
    }

    private int idVehiculo(String texto) {
        String t = texto == null ? "" : texto.trim();
        String clave = t.contains("·") ? t.substring(t.lastIndexOf('·') + 1).trim() : t;
        return requerirId(
                "No existe el vehículo '" + t + "'. Regístralo primero en Vehículos (indica sus placas o VIN).",
                """
                SELECT id_vehiculo FROM vehiculo
                WHERE placas = ? OR numero_serie_vin = ? OR placas = ? OR numero_serie_vin = ?
                ORDER BY activo DESC, id_vehiculo LIMIT 1
                """, clave, clave, t, t);
    }

    private static String periodicidad(String valor) {
        if (valor != null) {
            for (String p : new String[]{"Contado", "Semestral", "Trimestral", "Mensual"}) {
                if (p.equalsIgnoreCase(valor.trim())) {
                    return p;
                }
            }
        }
        return "Contado";
    }

    private static int mesesDe(String periodicidad) {
        return switch (periodicidad) {
            case "Semestral" -> 6;
            case "Trimestral" -> 3;
            case "Mensual" -> 1;
            default -> 12;
        };
    }

    private static String estado(String estado) {
        if (estado != null) {
            for (String e : new String[]{"Activa", "Por Vencer", "Vencida", "Cancelada"}) {
                if (e.equalsIgnoreCase(estado.trim())) {
                    return e;
                }
            }
        }
        return "Activa";
    }

    private Poliza mapear(ResultSet rs) throws SQLException {
        boolean cancelacion = rs.getObject("id_cancelacion") != null;
        String vigencia;
        String detalle;
        if (cancelacion) {
            vigencia = "Devol. " + Formatos.formatearMoneda(rs.getDouble("devolucion_prima"));
            detalle = "Motivo: " + rs.getString("motivo");
        } else {
            vigencia = Formatos.formatearFechaCorta(rs.getObject("fecha_vencimiento", LocalDate.class));
            detalle = rs.getString("periodicidad_pago");
        }
        return new Poliza(
                rs.getInt("id_poliza"),
                rs.getString("numero_poliza"),
                rs.getString("cliente"),
                rs.getString("vehiculo"),
                rs.getString("aseguradora"),
                rs.getString("tipo"),
                vigencia,
                detalle,
                Formatos.formatearMoneda(rs.getDouble("prima_total")),
                rs.getString("estado"),
                true,
                rs.getObject("id_poliza_anterior") != null,
                cancelacion);
    }
}
