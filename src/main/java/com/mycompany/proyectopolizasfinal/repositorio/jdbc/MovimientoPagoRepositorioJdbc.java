package com.mycompany.proyectopolizasfinal.repositorio.jdbc;

import com.mycompany.proyectopolizasfinal.modelo.MovimientoPago;
import com.mycompany.proyectopolizasfinal.repositorio.ContextoSesion;
import com.mycompany.proyectopolizasfinal.repositorio.PersistenciaException;
import com.mycompany.proyectopolizasfinal.repositorio.RepositorioCrud;
import com.mycompany.proyectopolizasfinal.util.Formatos;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Une {@code pago} (tipo "Pago / Cuota") y {@code aviso_vencimiento} (tipo "Aviso") en un solo
 * repositorio de {@link MovimientoPago}, como los muestra el módulo de Pagos y Avisos.
 * <p>
 * El id del movimiento se codifica como {@code idReal * 10 + 1} (pago) o {@code idReal * 10 + 2} (aviso).
 * La póliza se identifica por el número al inicio de "POL-2026-0142 · Juan Pérez".
 */
public class MovimientoPagoRepositorioJdbc extends BaseRepositorioJdbc implements RepositorioCrud<MovimientoPago> {

    public static final String TIPO_PAGO = "Pago / Cuota";
    public static final String TIPO_AVISO = "Aviso";

    private static final int COD_PAGO = 1;
    private static final int COD_AVISO = 2;

    private static final Pattern CUOTA = Pattern.compile("Cuota\\s+(\\d+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern RECIBO = Pattern.compile("Recibo\\s*#\\s*(\\S+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern DIAS = Pattern.compile("(-?\\d+)");

    private static final String SQL_PAGO = """
            SELECT g.*, p.numero_poliza,
                   TRIM(CONCAT(c.nombre, ' ', COALESCE(c.apellidos, ''))) AS cliente
            FROM pago g
            JOIN poliza p ON p.id_poliza = g.id_poliza
            JOIN cliente c ON c.id_cliente = p.id_cliente
            """;

    private static final String SQL_AVISO = """
            SELECT av.*, p.numero_poliza,
                   TRIM(CONCAT(c.nombre, ' ', COALESCE(c.apellidos, ''))) AS cliente
            FROM aviso_vencimiento av
            JOIN poliza p ON p.id_poliza = av.id_poliza
            JOIN cliente c ON c.id_cliente = p.id_cliente
            """;

    public MovimientoPagoRepositorioJdbc(ConexionBD conexionBD) {
        super(conexionBD);
    }

    @Override
    public List<MovimientoPago> listar() {
        List<MovimientoPago> todos = new ArrayList<>();
        todos.addAll(consultarLista(SQL_PAGO + " ORDER BY g.fecha_vencimiento_cuota DESC, g.id_pago DESC",
                this::mapearPago));
        todos.addAll(consultarLista(SQL_AVISO + " ORDER BY av.fecha_generacion DESC, av.id_aviso DESC",
                this::mapearAviso));
        return todos;
    }

    @Override
    public Optional<MovimientoPago> buscarPorId(int id) {
        return switch (id % 10) {
            case COD_PAGO -> consultarUno(SQL_PAGO + " WHERE g.id_pago = ?", this::mapearPago, id / 10);
            case COD_AVISO -> consultarUno(SQL_AVISO + " WHERE av.id_aviso = ?", this::mapearAviso, id / 10);
            default -> Optional.empty();
        };
    }

    @Override
    public MovimientoPago guardar(MovimientoPago m) {
        int idPoliza = idPoliza(m.polizaCliente());
        int id = TIPO_AVISO.equals(m.tipo())
                ? guardarAviso(idPoliza, m) * 10 + COD_AVISO
                : guardarPago(idPoliza, m) * 10 + COD_PAGO;
        return buscarPorId(id).orElse(m.conId(id));
    }

    @Override
    public MovimientoPago actualizar(MovimientoPago m) {
        int real = m.id() / 10;
        int filas;
        if (m.id() % 10 == COD_AVISO) {
            String estado = estadoAviso(m.estado());
            if (estado == null) {
                throw new PersistenciaException("Un aviso no puede quedar en estado '" + m.estado()
                        + "'. Estados válidos: Pendiente, Enviado, Contactado, Renovado.");
            }
            filas = ejecutarUpdate("UPDATE aviso_vencimiento SET estado = ? WHERE id_aviso = ?", estado, real);
        } else {
            filas = ejecutarUpdate("UPDATE pago SET estado = ? WHERE id_pago = ?", estadoPago(m.estado()), real);
        }
        if (filas == 0) {
            throw new PersistenciaException("No se encontró el movimiento con id=" + m.id());
        }
        return buscarPorId(m.id()).orElse(m);
    }

    @Override
    public void eliminar(int id) {
        if (id % 10 == COD_AVISO) {
            ejecutarUpdate("DELETE FROM aviso_vencimiento WHERE id_aviso = ?", id / 10);
        } else {
            ejecutarUpdate("DELETE FROM pago WHERE id_pago = ?", id / 10);
        }
    }

    // ------------------------------------------------------------------ pagos

    /**
     * Si la cuota ya existe para la póliza (por ejemplo, programada como Pendiente) se actualiza;
     * si no, se inserta.
     */
    private int guardarPago(int idPoliza, MovimientoPago m) {
        int cuota = extraerEntero(CUOTA, m.concepto(), 1);
        double monto = Formatos.parsearMonto(m.monto());
        LocalDate fechaLimite = Formatos.parsearFechaCorta(m.fecha());
        if (fechaLimite == null) {
            throw new PersistenciaException("Fecha límite de cuota inválida: " + m.fecha());
        }
        String estado = estadoPago(m.estado());
        boolean pagado = "Pagado".equals(estado);
        int idCajero = ContextoSesion.idUsuarioActual();
        if (pagado && idCajero <= 0) {
            throw new PersistenciaException(
                    "Para registrar cobros inicia sesión con un usuario registrado en la base de datos.");
        }
        Matcher r = RECIBO.matcher(m.detalleCanal() == null ? "" : m.detalleCanal());
        String recibo = r.find() ? r.group(1) : null;
        String forma = formaPago(m.canal());

        Object cajero = pagado ? idCajero : null;
        Object fechaPago = pagado ? LocalDate.now() : null;

        Optional<Integer> existente = consultarEntero(
                "SELECT id_pago FROM pago WHERE id_poliza = ? AND numero_cuota = ?", idPoliza, cuota);
        if (existente.isPresent()) {
            ejecutarUpdate("""
                    UPDATE pago SET id_usuario = ?, monto = ?, fecha_vencimiento_cuota = ?, fecha_pago = ?,
                                    numero_recibo = ?, forma_pago = ?, estado = ?
                    WHERE id_pago = ?
                    """, cajero, monto, fechaLimite, fechaPago, recibo, forma, estado, existente.get());
            return existente.get();
        }
        return ejecutarInsertConId("""
                INSERT INTO pago (id_poliza, id_usuario, numero_cuota, monto, fecha_vencimiento_cuota,
                                  fecha_pago, numero_recibo, forma_pago, estado)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """, idPoliza, cajero, cuota, monto, fechaLimite, fechaPago, recibo, forma, estado);
    }

    private MovimientoPago mapearPago(ResultSet rs) throws SQLException {
        String estado = rs.getString("estado");
        String recibo = rs.getString("numero_recibo");
        String forma = rs.getString("forma_pago");
        boolean pagado = "Pagado".equals(estado);
        return new MovimientoPago(
                rs.getInt("id_pago") * 10 + COD_PAGO,
                "Cuota " + rs.getInt("numero_cuota") + " · " + (pagado ? "Pago registrado" : "Pendiente de pago"),
                rs.getString("numero_poliza") + " · " + rs.getString("cliente"),
                TIPO_PAGO,
                forma == null ? "—" : forma,
                recibo == null ? "Sin recibo" : "Recibo #" + recibo,
                Formatos.formatearFechaCorta(rs.getObject("fecha_vencimiento_cuota", LocalDate.class)),
                pagado ? "Liquidado" : "Cuota programada",
                Formatos.formatearMoneda(rs.getDouble("monto")),
                estado);
    }

    // ------------------------------------------------------------------ avisos

    private int guardarAviso(int idPoliza, MovimientoPago m) {
        boolean deCobro = m.concepto() != null && m.concepto().toLowerCase().contains("cobro");
        Integer idPago = null;
        if (deCobro) {
            idPago = consultarEntero("""
                    SELECT id_pago FROM pago
                    WHERE id_poliza = ? AND estado IN ('Pendiente', 'Vencido')
                    ORDER BY fecha_vencimiento_cuota, numero_cuota LIMIT 1
                    """, idPoliza)
                    .orElseThrow(() -> new PersistenciaException(
                            "La póliza no tiene cuotas pendientes para enviar un aviso de cobro."));
        }
        String estado = estadoAviso(m.estado());
        return ejecutarInsertConId("""
                INSERT INTO aviso_vencimiento (id_poliza, id_pago, tipo_aviso, fecha_generacion,
                                               dias_restantes, medio_envio, estado)
                VALUES (?, ?, ?, CURRENT_DATE, ?, ?, ?)
                """,
                idPoliza, idPago, deCobro ? "Cobro_Cuota" : "Renovacion_Poliza",
                extraerEntero(DIAS, m.detalleFecha(), 0),
                "Correo".equalsIgnoreCase(m.canal()) ? "Correo" : "Notificación en Pantalla",
                estado == null ? "Pendiente" : estado);
    }

    private MovimientoPago mapearAviso(ResultSet rs) throws SQLException {
        boolean deCobro = "Cobro_Cuota".equals(rs.getString("tipo_aviso"));
        String fecha = Formatos.formatearFechaCorta(rs.getObject("fecha_generacion", LocalDate.class));
        return new MovimientoPago(
                rs.getInt("id_aviso") * 10 + COD_AVISO,
                deCobro ? "Aviso de cobro de cuota" : "Aviso de renovación",
                rs.getString("numero_poliza") + " · " + rs.getString("cliente"),
                TIPO_AVISO,
                rs.getString("medio_envio"),
                "Generado " + fecha,
                fecha,
                "Faltan " + rs.getInt("dias_restantes") + " días",
                "—",
                rs.getString("estado"));
    }

    // ------------------------------------------------------------------ helpers

    private int idPoliza(String polizaCliente) {
        String t = polizaCliente == null ? "" : polizaCliente.trim();
        String numero = t.contains("·") ? t.substring(0, t.indexOf('·')).trim() : t;
        return requerirId(
                "No existe la póliza '" + numero + "'. Escribe el número de póliza (ej. POL-2026-0142 · Cliente).",
                "SELECT id_poliza FROM poliza WHERE numero_poliza = ?", numero);
    }

    private static int extraerEntero(Pattern patron, String texto, int porDefecto) {
        if (texto == null) {
            return porDefecto;
        }
        Matcher m = patron.matcher(texto);
        return m.find() ? Integer.parseInt(m.group(1)) : porDefecto;
    }

    private static String formaPago(String canal) {
        if (canal != null) {
            for (String f : new String[]{"Efectivo", "Tarjeta", "Transferencia"}) {
                if (f.equalsIgnoreCase(canal.trim())) {
                    return f;
                }
            }
        }
        return null;
    }

    private static String estadoPago(String estado) {
        if (estado != null) {
            for (String e : new String[]{"Pendiente", "Pagado", "Vencido", "Cancelado"}) {
                if (e.equalsIgnoreCase(estado.trim())) {
                    return e;
                }
            }
        }
        return "Pendiente";
    }

    private static String estadoAviso(String estado) {
        if (estado != null) {
            for (String e : new String[]{"Pendiente", "Enviado", "Contactado", "Renovado"}) {
                if (e.equalsIgnoreCase(estado.trim())) {
                    return e;
                }
            }
        }
        return null;
    }
}
