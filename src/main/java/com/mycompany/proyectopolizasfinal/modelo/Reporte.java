package com.mycompany.proyectopolizasfinal.modelo;

/**
 * Modelo de reporte generado para vista previa, PDF e historial.
 */
public record Reporte(
        int id,
        String folio,
        String tipo,
        String categoria,
        String fechaGeneracion,
        String usuarioNombre,
        String username,
        String rangoFechas,
        String parametrosFiltros,
        int registros,
        String estado) {

    public Reporte conId(int nuevoId) {
        return new Reporte(nuevoId, folio, tipo, categoria, fechaGeneracion,
                usuarioNombre, username, rangoFechas, parametrosFiltros, registros, estado);
    }
}
