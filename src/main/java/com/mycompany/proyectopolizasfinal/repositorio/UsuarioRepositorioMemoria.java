package com.mycompany.proyectopolizasfinal.repositorio;

import com.mycompany.proyectopolizasfinal.modelo.Usuario;
import java.util.List;
import java.util.Optional;

/**
 * Implementación en memoria de {@link UsuarioRepositorio}.
 */
public class UsuarioRepositorioMemoria extends RepositorioMemoria<Usuario> implements UsuarioRepositorio {

    public UsuarioRepositorioMemoria(List<Usuario> iniciales) {
        super(iniciales, Usuario::id, Usuario::conId);
    }

    @Override
    public Optional<Usuario> buscarPorUsername(String username) {
        if (username == null || username.isBlank()) {
            return Optional.empty();
        }
        String buscado = username.trim();
        return datos.stream()
                .filter(u -> u.username().equalsIgnoreCase(buscado))
                .findFirst();
    }

    @Override
    public boolean existeUsername(String username, int excluirId) {
        if (username == null || username.isBlank()) {
            return false;
        }
        String buscado = username.trim();
        return datos.stream()
                .anyMatch(u -> u.id() != excluirId && u.username().equalsIgnoreCase(buscado));
    }
}
