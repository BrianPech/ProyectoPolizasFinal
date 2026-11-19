package com.mycompany.proyectopolizasfinal.repositorio;

import java.util.List;
import java.util.Optional;

/**
 * Contrato genérico de persistencia orientado a entidad con identificador (PK).
 * Compatible tanto con memoria como con implementaciones JDBC / MySQL.
 */
public interface RepositorioCrud<T> {

    List<T> listar();

    Optional<T> buscarPorId(int id);

    T guardar(T item);

    T actualizar(T item);

    void eliminar(int id);
}
