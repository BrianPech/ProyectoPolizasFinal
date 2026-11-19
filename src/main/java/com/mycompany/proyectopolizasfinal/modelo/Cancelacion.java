package com.mycompany.proyectopolizasfinal.modelo;

/**
 * Resultado de una solicitud de cancelación de póliza.
 */
public record Cancelacion(String motivo, String devolucionFormateada, String estadoSolicitud) {}
