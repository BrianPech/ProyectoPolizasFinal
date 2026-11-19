package com.mycompany.proyectopolizasfinal.repositorio;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.ToIntFunction;

/**
 * Implementación en memoria de {@link RepositorioCrud} con asignación automática de IDs.
 */
public class RepositorioMemoria<T> implements RepositorioCrud<T> {

    protected final List<T> datos = new ArrayList<>();
    private final ToIntFunction<T> extractorId;
    private final BiFunction<T, Integer, T> asignadorId;
    private final AtomicInteger secuencia = new AtomicInteger(0);

    public RepositorioMemoria(
            List<T> iniciales,
            ToIntFunction<T> extractorId,
            BiFunction<T, Integer, T> asignadorId) {
        this.extractorId = extractorId;
        this.asignadorId = asignadorId;
        for (T item : iniciales) {
            int id = extractorId.applyAsInt(item);
            if (id <= 0) {
                id = secuencia.incrementAndGet();
                datos.add(asignadorId.apply(item, id));
            } else {
                secuencia.accumulateAndGet(id, Math::max);
                datos.add(item);
            }
        }
    }

    @Override
    public List<T> listar() {
        return Collections.unmodifiableList(datos);
    }

    @Override
    public Optional<T> buscarPorId(int id) {
        return datos.stream()
                .filter(item -> extractorId.applyAsInt(item) == id)
                .findFirst();
    }

    @Override
    public T guardar(T item) {
        if (item == null) {
            throw new IllegalArgumentException("El registro a guardar no puede ser nulo.");
        }
        int id = extractorId.applyAsInt(item);
        T persistido = id > 0 ? item : asignadorId.apply(item, secuencia.incrementAndGet());
        secuencia.accumulateAndGet(extractorId.applyAsInt(persistido), Math::max);
        datos.add(0, persistido);
        return persistido;
    }

    @Override
    public T actualizar(T item) {
        if (item == null) {
            throw new IllegalArgumentException("El registro a actualizar no puede ser nulo.");
        }
        int id = extractorId.applyAsInt(item);
        for (int i = 0; i < datos.size(); i++) {
            if (extractorId.applyAsInt(datos.get(i)) == id) {
                datos.set(i, item);
                return item;
            }
        }
        throw new PersistenciaException("No se encontró el registro con id=" + id);
    }

    @Override
    public void eliminar(int id) {
        datos.removeIf(item -> extractorId.applyAsInt(item) == id);
    }
}
