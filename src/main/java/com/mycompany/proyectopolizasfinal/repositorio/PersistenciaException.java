package com.mycompany.proyectopolizasfinal.repositorio;

/**
 * Excepción de acceso o consistencia en la capa de persistencia.
 */
public class PersistenciaException extends RuntimeException {

    public PersistenciaException(String message) {
        super(message);
    }

    public PersistenciaException(String message, Throwable cause) {
        super(message, cause);
    }
}
