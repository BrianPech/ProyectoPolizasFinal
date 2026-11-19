package com.mycompany.proyectopolizasfinal.repositorio;

import com.mycompany.proyectopolizasfinal.modelo.Usuario;
import java.util.Optional;

/**
 * Contrato extendido de persistencia para cuentas de usuario y autenticación.
 */
public interface UsuarioRepositorio extends RepositorioCrud<Usuario> {

    Optional<Usuario> buscarPorUsername(String username);

    boolean existeUsername(String username, int excluirId);

    /**
     * Guarda la contraseña (hasheada) del usuario. La implementación en memoria no guarda contraseñas.
     */
    default void establecerPassword(int idUsuario, char[] password) {}

    /**
     * Verifica la contraseña del usuario. La implementación en memoria acepta cualquier contraseña.
     */
    default boolean verificarPassword(String username, char[] password) {
        return true;
    }
}
