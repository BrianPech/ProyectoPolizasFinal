package com.mycompany.proyectopolizasfinal.repositorio;

/**
 * Guarda el id del usuario con sesión abierta para que la capa de persistencia
 * pueda llenar columnas de auditoría (agente vendedor, cajero, solicitante de cancelación).
 * Lo actualiza {@code ControladorSesion} al iniciar y cerrar sesión.
 */
public final class ContextoSesion {

    private static volatile int idUsuarioActual;

    private ContextoSesion() {}

    public static void establecer(int idUsuario) {
        idUsuarioActual = Math.max(idUsuario, 0);
    }

    public static void limpiar() {
        idUsuarioActual = 0;
    }

    /** Id del usuario en sesión, o 0 si no hay sesión con un usuario registrado. */
    public static int idUsuarioActual() {
        return idUsuarioActual;
    }
}
