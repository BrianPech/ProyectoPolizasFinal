package com.mycompany.proyectopolizasfinal.controlador;

import com.mycompany.proyectopolizasfinal.modelo.Cliente;
import com.mycompany.proyectopolizasfinal.repositorio.RepositorioCrud;
import java.util.List;

/**
 * Controlador del módulo de Clientes.
 */
public class ControladorClientes extends ControladorListado<Cliente> {

    public ControladorClientes(RepositorioCrud<Cliente> repositorio) {
        super(repositorio);
    }

    public List<Cliente> filtrar(boolean soloActivos, boolean soloAdeudo, String estatusFiltro,
            String tipoFiltro, String texto) {
        return filtrar(c -> {
            if (soloActivos && c.polizasActivas() <= 0) {
                return false;
            }
            if (soloAdeudo && !"Con adeudo".equalsIgnoreCase(c.estatus())) {
                return false;
            }
            if (estatusFiltro != null && !"Todos los estatus".equals(estatusFiltro)
                    && !estatusFiltro.equalsIgnoreCase(c.estatus())) {
                return false;
            }
            if (tipoFiltro != null && !"Tipo".equals(tipoFiltro)
                    && !tipoFiltro.equalsIgnoreCase(c.tipoPersona())) {
                return false;
            }
            return contieneTexto(texto, c.nombreCompleto(), c.rfc(), c.telefono(), c.correo());
        });
    }

    public Cliente desactivar(Cliente cliente) {
        return actualizar(cliente.conEstatus("Inactivo"));
    }
}
