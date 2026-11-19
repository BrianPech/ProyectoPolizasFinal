package com.mycompany.proyectopolizasfinal.repositorio;

import com.mycompany.proyectopolizasfinal.modelo.Usuario;
import java.util.Optional;

/**
 * Contrato extendido de persistencia para cuentas de usuario y autenticación.
 */
public interface UsuarioRepositorio extends RepositorioCrud<Usuario> {

    Optional<Usuario> buscarPorUsername(String username);

    boolean existeUsername(String username, int excluirId);
}
