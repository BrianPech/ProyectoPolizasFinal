package com.mycompany.proyectopolizasfinal.modelo;

/**
 * Modelo unificado de registro en catálogos maestros (aseguradoras, tipos de póliza, coberturas y marcas).
 */
public record CatalogoItem(
        int id,
        String nombre,
        String claveDetalle,
        String catalogo,
        String subtipo,
        String regla,
        String detalleRegla,
        int usos,
        String estado) {

    public CatalogoItem conId(int nuevoId) {
        return new CatalogoItem(nuevoId, nombre, claveDetalle, catalogo, subtipo,
                regla, detalleRegla, usos, estado);
    }

    public CatalogoItem conEstado(String nuevoEstado) {
        return new CatalogoItem(id, nombre, claveDetalle, catalogo, subtipo,
                regla, detalleRegla, usos, nuevoEstado);
    }
}
