package com.mycompany.proyectopolizasfinal.controlador;

import com.mycompany.proyectopolizasfinal.modelo.Usuario;
import com.mycompany.proyectopolizasfinal.repositorio.UsuarioRepositorio;
import java.util.List;

/**
 * Controlador del módulo de Usuarios y roles.
 */
public class ControladorUsuarios extends ControladorListado<Usuario> {

    private final UsuarioRepositorio usuarioRepositorio;

    public ControladorUsuarios(UsuarioRepositorio usuarioRepositorio) {
        super(usuarioRepositorio);
        this.usuarioRepositorio = usuarioRepositorio;
    }

    public List<Usuario> filtrar(boolean soloAdmins, boolean soloAgentes, boolean soloInactivos,
            String rolFiltro, String estadoFiltro, String texto) {
        return filtrar(u -> {
            if (soloAdmins && !"Administrador".equalsIgnoreCase(u.rol())) {
                return false;
            }
            if (soloAgentes && !"Agente de Ventas".equalsIgnoreCase(u.rol())) {
                return false;
            }
            if (soloInactivos && !"Inactivo".equalsIgnoreCase(u.estado())) {
                return false;
            }
            if (rolFiltro != null && !"Todos los roles".equals(rolFiltro)
                    && !rolFiltro.equalsIgnoreCase(u.rol())) {
                return false;
            }
            if (estadoFiltro != null && !"Estado".equals(estadoFiltro)
                    && !estadoFiltro.equalsIgnoreCase(u.estado())) {
                return false;
            }
            return contieneTexto(texto, u.nombre(), u.username(), u.rol(), u.descripcionRol());
        });
    }

    public boolean existeUsername(String username, Usuario excluir) {
        int excluirId = excluir != null ? excluir.id() : 0;
        return usuarioRepositorio.existeUsername(username, excluirId);
    }

    /** Guarda un usuario nuevo y, si se capturó, su contraseña. */
    public Usuario guardar(Usuario usuario, char[] password) {
        Usuario guardado = guardar(usuario);
        usuarioRepositorio.establecerPassword(guardado.id(), password);
        return guardado;
    }

    /** Actualiza el usuario y, si se capturó una nueva contraseña, la reemplaza. */
    public Usuario actualizar(Usuario usuario, char[] password) {
        Usuario actualizado = actualizar(usuario);
        usuarioRepositorio.establecerPassword(actualizado.id(), password);
        return actualizado;
    }

    public Usuario alternarEstado(Usuario usuario) {
        String nuevoEstado = "Activo".equalsIgnoreCase(usuario.estado()) ? "Inactivo" : "Activo";
        return actualizar(usuario.conEstado(nuevoEstado));
    }
}
