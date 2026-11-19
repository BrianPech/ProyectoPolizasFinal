package com.mycompany.proyectopolizasfinal.controlador;

import com.mycompany.proyectopolizasfinal.modelo.MovimientoPago;
import com.mycompany.proyectopolizasfinal.modelo.Poliza;
import com.mycompany.proyectopolizasfinal.repositorio.RepositorioCrud;
import com.mycompany.proyectopolizasfinal.util.Formatos;
import java.util.List;

/**
 * Controlador de resumen operativo para la pantalla Inicio.
 */
public class ControladorInicio {

    public record ResumenKpis(
            int polizasActivas,
            int renovaciones,
            int porVencer,
            String montoPendiente,
            int cuotasPendientes,
            String montoCobrado) {}

    public record ItemPendiente(String titulo, String detalle, String etiqueta, boolean critico) {}

    private final RepositorioCrud<Poliza> polizaRepositorio;
    private final RepositorioCrud<MovimientoPago> movimientoRepositorio;

    public ControladorInicio(
            RepositorioCrud<Poliza> polizaRepositorio,
            RepositorioCrud<MovimientoPago> movimientoRepositorio) {
        this.polizaRepositorio = polizaRepositorio;
        this.movimientoRepositorio = movimientoRepositorio;
    }

    public ResumenKpis obtenerKpis() {
        List<Poliza> polizas = polizaRepositorio.listar();
        int activas = 0;
        int renovaciones = 0;
        int porVencer = 0;
        for (Poliza p : polizas) {
            if ("Activa".equalsIgnoreCase(p.estado()) || "Por Vencer".equalsIgnoreCase(p.estado())) {
                activas++;
            }
            if (p.esRenovacion()) {
                renovaciones++;
            }
            if ("Por Vencer".equalsIgnoreCase(p.estado())) {
                porVencer++;
            }
        }

        List<MovimientoPago> movimientos = movimientoRepositorio.listar();
        double pendiente = 0;
        int cuotasPend = 0;
        double cobrado = 0;
        for (MovimientoPago m : movimientos) {
            if (!"Pago / Cuota".equals(m.tipo())) {
                continue;
            }
            double monto = Formatos.parsearMonto(m.monto());
            if ("Pendiente".equalsIgnoreCase(m.estado()) || "Vencido".equalsIgnoreCase(m.estado())) {
                pendiente += monto;
                cuotasPend++;
            } else if ("Pagado".equalsIgnoreCase(m.estado())) {
                cobrado += monto;
            }
        }

        return new ResumenKpis(
                activas,
                renovaciones,
                porVencer,
                Formatos.formatearMoneda(pendiente),
                cuotasPend,
                Formatos.formatearMoneda(cobrado));
    }

    public List<Poliza> proximosVencimientos(int limite) {
        return polizaRepositorio.listar().stream()
                .filter(p -> !"Cancelada".equalsIgnoreCase(p.estado()))
                .limit(limite)
                .toList();
    }

    public List<ItemPendiente> pendientesDelDia(int limite) {
        return movimientoRepositorio.listar().stream()
                .filter(m -> "Vencido".equalsIgnoreCase(m.estado()) || "Pendiente".equalsIgnoreCase(m.estado()))
                .limit(limite)
                .map(m -> new ItemPendiente(
                        m.concepto(),
                        m.polizaCliente() + ("—".equals(m.monto()) ? "" : " · " + m.monto()),
                        m.estado(),
                        "Vencido".equalsIgnoreCase(m.estado())))
                .toList();
    }

    public Poliza registrarPoliza(Poliza poliza) {
        return polizaRepositorio.guardar(poliza);
    }

    public MovimientoPago registrarMovimiento(MovimientoPago movimiento) {
        return movimientoRepositorio.guardar(movimiento);
    }
}
