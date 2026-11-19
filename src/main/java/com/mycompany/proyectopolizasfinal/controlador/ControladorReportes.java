package com.mycompany.proyectopolizasfinal.controlador;

import com.mycompany.proyectopolizasfinal.modelo.MovimientoPago;
import com.mycompany.proyectopolizasfinal.modelo.Poliza;
import com.mycompany.proyectopolizasfinal.modelo.Reporte;
import com.mycompany.proyectopolizasfinal.modelo.Usuario;
import com.mycompany.proyectopolizasfinal.repositorio.RepositorioCrud;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Controlador del módulo de Reportes e historial.
 */
public class ControladorReportes extends ControladorListado<Reporte> {

    private static final DateTimeFormatter FMT_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final RepositorioCrud<Poliza> polizaRepositorio;
    private final RepositorioCrud<MovimientoPago> movimientoRepositorio;
    private int consecutivo = 1;

    public ControladorReportes(RepositorioCrud<Reporte> repositorio) {
        this(repositorio, null, null);
    }

    public ControladorReportes(
            RepositorioCrud<Reporte> repositorio,
            RepositorioCrud<Poliza> polizaRepositorio,
            RepositorioCrud<MovimientoPago> movimientoRepositorio) {
        super(repositorio);
        this.polizaRepositorio = polizaRepositorio;
        this.movimientoRepositorio = movimientoRepositorio;
        for (Reporte r : repositorio.listar()) {
            if (r.folio() != null && r.folio().startsWith("RPT-")) {
                try {
                    int n = Integer.parseInt(r.folio().substring(4));
                    consecutivo = Math.max(consecutivo, n + 1);
                } catch (NumberFormatException ignored) {}
            }
        }
    }

    public List<Reporte> filtrar(boolean soloOperacion, boolean soloCobranza, boolean soloCancelaciones,
            String tipoFiltro, String userFiltro, String texto) {
        return filtrar(r -> {
            if (soloOperacion && !"Operación".equalsIgnoreCase(r.categoria())) {
                return false;
            }
            if (soloCobranza && !"Cobranza".equalsIgnoreCase(r.categoria())) {
                return false;
            }
            if (soloCancelaciones && !"Cancelaciones".equalsIgnoreCase(r.categoria())) {
                return false;
            }
            if (tipoFiltro != null && !"Todos los tipos".equals(tipoFiltro)
                    && !tipoFiltro.equalsIgnoreCase(r.tipo())) {
                return false;
            }
            if (userFiltro != null && !"Usuario".equals(userFiltro)
                    && !userFiltro.equalsIgnoreCase(r.username())) {
                return false;
            }
            return contieneTexto(texto, r.folio(), r.tipo(), r.usuarioNombre(),
                    r.username(), r.parametrosFiltros());
        });
    }

    public Reporte generar(String tipo, String aseguradora, LocalDate desde, LocalDate hasta, Usuario autor) {
        String categoria = "Cobranza y Pagos".equals(tipo)
                ? "Cobranza"
                : ("Cancelaciones".equals(tipo) ? "Cancelaciones" : "Operación");
        String rango = desde.format(FMT_FECHA) + " - " + hasta.format(FMT_FECHA);
        String folio = String.format("RPT-%04d", consecutivo++);
        String nombreAutor = autor != null ? autor.nombre() : "Sistema";
        String userAutor = autor != null ? autor.username() : "sistema";
        int registros = filasParaTipo(tipo).size();

        Reporte nuevo = new Reporte(
                0, folio, tipo, categoria, "Hoy · Recién generado",
                nombreAutor, userAutor, rango,
                "Aseguradora: " + aseguradora, registros, "Generado");
        return guardar(nuevo);
    }

    public List<String[]> filasParaTipo(String tipo) {
        return switch (tipo) {
            case "Cobranza y Pagos" -> movimientoRepositorio == null ? List.of()
                    : movimientoRepositorio.listar().stream()
                            .filter(m -> "Pago / Cuota".equals(m.tipo()))
                            .map(m -> new String[]{m.concepto(), m.polizaCliente(), m.canal(), m.monto(), m.estado()})
                            .toList();
            case "Cancelaciones" -> polizaRepositorio == null ? List.of()
                    : polizaRepositorio.listar().stream()
                            .filter(Poliza::esCancelacion)
                            .map(p -> new String[]{p.numero(), p.cliente(), p.detalleVigencia(), p.vigencia(), p.estado()})
                            .toList();
            default -> polizaRepositorio == null ? List.of()
                    : polizaRepositorio.listar().stream()
                            .map(p -> new String[]{p.numero(), p.cliente() + " · " + p.vehiculo(),
                                p.aseguradora() + " · " + p.tipo(), p.prima(), p.estado()})
                            .toList();
        };
    }
}
