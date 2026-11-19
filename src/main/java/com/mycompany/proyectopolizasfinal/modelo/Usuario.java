package com.mycompany.proyectopolizasfinal.modelo;

/**
 * Modelo de cuenta de usuario y rol de acceso.
 */
public record Usuario(
        int id,
        String nombre,
        String username,
        String rol,
        String descripcionRol,
        int polizasGestionadas,
        String estado) {

    public Usuario conId(int nuevoId) {
        return new Usuario(nuevoId, nombre, username, rol, descripcionRol, polizasGestionadas, estado);
    }

    public Usuario conEstado(String nuevoEstado) {
        return new Usuario(id, nombre, username, rol, descripcionRol, polizasGestionadas, nuevoEstado);
    }
}
