package com.mycompany.proyectopolizasfinal.modelo;

/**
 * Modelo de movimiento de cobranza (pago/cuota o aviso de vencimiento).
 */
public record MovimientoPago(
        int id,
        String concepto,
        String polizaCliente,
        String tipo,
        String canal,
        String detalleCanal,
        String fecha,
        String detalleFecha,
        String monto,
        String estado) {

    public MovimientoPago conId(int nuevoId) {
        return new MovimientoPago(nuevoId, concepto, polizaCliente, tipo, canal,
                detalleCanal, fecha, detalleFecha, monto, estado);
    }

    public MovimientoPago conEstado(String nuevoEstado) {
        return new MovimientoPago(id, concepto, polizaCliente, tipo, canal, detalleCanal,
                fecha, detalleFecha, monto, nuevoEstado);
    }
}
