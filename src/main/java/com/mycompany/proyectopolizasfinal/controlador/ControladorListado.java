package com.mycompany.proyectopolizasfinal.controlador;

import com.mycompany.proyectopolizasfinal.repositorio.RepositorioCrud;
import java.util.List;
import java.util.function.Predicate;

/**
 * Controlador base reutilizable para operaciones CRUD y filtrado sobre un repositorio.
 */
public class ControladorListado<T> {

    protected final RepositorioCrud<T> repositorio;

    public ControladorListado(RepositorioCrud<T> repositorio) {
        this.repositorio = repositorio;
    }

    public List<T> listar() {
        return repositorio.listar();
    }

    public List<T> filtrar(Predicate<T> criterio) {
        if (criterio == null) {
            return repositorio.listar();
        }
        return repositorio.listar().stream()
                .filter(criterio)
                .toList();
    }

    public T guardar(T item) {
        return repositorio.guardar(item);
    }

    public T actualizar(T item) {
        return repositorio.actualizar(item);
    }

    public void eliminar(int id) {
        repositorio.eliminar(id);
    }

    protected static boolean contieneTexto(String texto, String... campos) {
        if (texto == null || texto.isBlank()) {
            return true;
        }
        String needle = texto.trim().toLowerCase();
        for (String campo : campos) {
            if (campo != null && campo.toLowerCase().contains(needle)) {
                return true;
            }
        }
        return false;
    }
}
