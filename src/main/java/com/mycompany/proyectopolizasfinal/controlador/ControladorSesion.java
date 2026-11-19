package com.mycompany.proyectopolizasfinal.controlador;

import com.mycompany.proyectopolizasfinal.modelo.Usuario;
import com.mycompany.proyectopolizasfinal.repositorio.ContextoSesion;
import com.mycompany.proyectopolizasfinal.repositorio.UsuarioRepositorio;
import java.util.Optional;

/**
 * Controlador de autenticación y sesión de usuario activo.
 */
public class ControladorSesion {

    private final UsuarioRepositorio usuarioRepositorio;
    private Usuario usuarioActual;

    public ControladorSesion(UsuarioRepositorio usuarioRepositorio) {
        this.usuarioRepositorio = usuarioRepositorio;
    }

    /**
     * Valida credenciales contra el repositorio de usuarios.
     * Mientras el repositorio no tenga cuentas registradas, permite acceso inicial en blanco.
     *
     * @return mensaje de error si falla, o vacío si la sesión se abrió correctamente.
     */
    public Optional<String> iniciarSesion(String identificador, char[] password) {
        String raw = identificador == null ? "" : identificador.trim();
        if (raw.isEmpty()) {
            return Optional.of("Ingresa tu usuario o correo.");
        }
        if (password == null || password.length == 0) {
            return Optional.of("Ingresa tu contraseña.");
        }

        String username = raw.contains("@") ? raw.substring(0, raw.indexOf('@')) : raw;
        if (usuarioRepositorio.listar().isEmpty()) {
            // Acceso inicial: aún no hay cuentas; permite entrar para dar de alta al primer administrador.
            this.usuarioActual = new Usuario(
                    0, username, username, "Administrador", "Acceso inicial", 0, "Activo");
            ContextoSesion.limpiar();
            return Optional.empty();
        }

        Optional<Usuario> encontrado = usuarioRepositorio.buscarPorUsername(username);
        if (encontrado.isEmpty()) {
            return Optional.of("Usuario no encontrado.");
        }
        Usuario u = encontrado.get();
        if (!"Activo".equalsIgnoreCase(u.estado())) {
            return Optional.of("La cuenta está inactiva. Contacta al administrador.");
        }
        if (!usuarioRepositorio.verificarPassword(u.username(), password)) {
            return Optional.of("Contraseña incorrecta.");
        }

        this.usuarioActual = u;
        ContextoSesion.establecer(u.id());
        return Optional.empty();
    }

    public Usuario getUsuarioActual() {
        return usuarioActual;
    }

    public void cerrarSesion() {
        this.usuarioActual = null;
        ContextoSesion.limpiar();
    }
}
