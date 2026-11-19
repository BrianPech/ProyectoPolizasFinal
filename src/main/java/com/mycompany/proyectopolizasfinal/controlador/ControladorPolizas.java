package com.mycompany.proyectopolizasfinal.controlador;

import com.mycompany.proyectopolizasfinal.modelo.Cancelacion;
import com.mycompany.proyectopolizasfinal.modelo.MovimientoPago;
import com.mycompany.proyectopolizasfinal.modelo.Poliza;
import com.mycompany.proyectopolizasfinal.repositorio.RepositorioCrud;
import java.util.List;

/**
 * Controlador del módulo de Pólizas (listado, cancelaciones y detalle).
 */
public class ControladorPolizas extends ControladorListado<Poliza> {

    private final RepositorioCrud<MovimientoPago> movimientoRepositorio;

    public ControladorPolizas(RepositorioCrud<Poliza> repositorio) {
        this(repositorio, null);
    }

    public ControladorPolizas(
            RepositorioCrud<Poliza> repositorio,
            RepositorioCrud<MovimientoPago> movimientoRepositorio) {
        super(repositorio);
        this.movimientoRepositorio = movimientoRepositorio;
    }

    public List<Poliza> filtrar(boolean tabTodas, boolean soloRenovaciones, boolean soloCancelaciones,
            String estadoFiltro, String asegFiltro, String texto) {
        return filtrar(p -> {
            if (soloRenovaciones && !p.esRenovacion()) {
                return false;
            }
            if (soloCancelaciones && !p.esCancelacion()) {
                return false;
            }
            if (!soloCancelaciones && tabTodas && p.esCancelacion() && "Activa".equals(estadoFiltro)) {
                return false;
            }
            if (estadoFiltro != null && !"Todos los estados".equals(estadoFiltro)
                    && !estadoFiltro.equalsIgnoreCase(p.estado())) {
                return false;
            }
            if (asegFiltro != null && !"Aseguradora".equals(asegFiltro)
                    && !asegFiltro.equalsIgnoreCase(p.aseguradora())) {
                return false;
            }
            return contieneTexto(texto, p.numero(), p.cliente(), p.vehiculo(), p.aseguradora());
        });
    }

    public Poliza aplicarCancelacion(Poliza poliza, Cancelacion cancelacion) {
        return actualizar(poliza.conCancelacion(cancelacion));
    }

    public List<Object[]> coberturasDe(Poliza p) {
        // TODO(BD): consultar tabla poliza_cobertura cuando se conecte MySQL.
        return List.of();
    }

    public List<Object[]> pagosDe(Poliza p) {
        if (movimientoRepositorio == null || p == null) {
            return List.of();
        }
        return movimientoRepositorio.listar().stream()
                .filter(m -> "Pago / Cuota".equals(m.tipo())
                        && m.polizaCliente() != null
                        && m.polizaCliente().contains(p.numero()))
                .map(m -> new Object[]{m.concepto(), m.fecha(), m.monto(), m.estado()})
                .toList();
    }

    public List<Object[]> historialDe(Poliza p) {
        if (p == null || !p.esCancelacion()) {
            return List.of();
        }
        return List.<Object[]>of(new Object[]{
            "Cancelación", p.estado(), p.detalleVigencia() + " (" + p.vigencia() + ")", "Sistema"
        });
    }
}
