package com.mycompany.proyectopolizasfinal.modelo;

/**
 * Modelo de cliente (contratante persona física o moral).
 */
public record Cliente(
        int id,
        String nombre,
        String apellidos,
        String rfc,
        String tipoPersona,
        String telefono,
        String correo,
        String direccion,
        int polizasActivas,
        String resumenPolizas,
        String primaAnual,
        String estatus) {

    public String nombreCompleto() {
        return (apellidos == null || apellidos.isBlank()) ? nombre : nombre + " " + apellidos;
    }

    public Cliente conId(int nuevoId) {
        return new Cliente(nuevoId, nombre, apellidos, rfc, tipoPersona, telefono, correo,
                direccion, polizasActivas, resumenPolizas, primaAnual, estatus);
    }

    public Cliente conEstatus(String nuevoEstatus) {
        return new Cliente(id, nombre, apellidos, rfc, tipoPersona, telefono, correo,
                direccion, polizasActivas, resumenPolizas, primaAnual, nuevoEstatus);
    }
}
