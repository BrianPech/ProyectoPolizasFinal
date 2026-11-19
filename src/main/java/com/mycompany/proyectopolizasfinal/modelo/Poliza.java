package com.mycompany.proyectopolizasfinal.modelo;

/**
 * Modelo de póliza de seguro.
 */
public record Poliza(
        int id,
        String numero,
        String cliente,
        String vehiculo,
        String aseguradora,
        String tipo,
        String vigencia,
        String detalleVigencia,
        String prima,
        String estado,
        boolean esAuto,
        boolean esRenovacion,
        boolean esCancelacion) {

    public Poliza conId(int nuevoId) {
        return new Poliza(nuevoId, numero, cliente, vehiculo, aseguradora, tipo,
                vigencia, detalleVigencia, prima, estado, esAuto, esRenovacion, esCancelacion);
    }

    public Poliza conCancelacion(Cancelacion res) {
        return new Poliza(
                id, numero, cliente, vehiculo, aseguradora, tipo,
                "Devol. " + res.devolucionFormateada(),
                "Motivo: " + res.motivo(),
                prima,
                "Aprobada".equals(res.estadoSolicitud()) ? "Cancelada" : "Por Vencer",
                esAuto, esRenovacion, true);
    }
}
