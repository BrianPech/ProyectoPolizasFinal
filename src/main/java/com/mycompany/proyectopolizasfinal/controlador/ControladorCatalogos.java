package com.mycompany.proyectopolizasfinal.controlador;

import com.mycompany.proyectopolizasfinal.modelo.CatalogoItem;
import com.mycompany.proyectopolizasfinal.repositorio.RepositorioCrud;
import java.util.List;

/**
 * Controlador del módulo de Catálogos maestros.
 */
public class ControladorCatalogos extends ControladorListado<CatalogoItem> {

    public ControladorCatalogos(RepositorioCrud<CatalogoItem> repositorio) {
        super(repositorio);
    }

    public List<CatalogoItem> filtrar(String tabCatalogo, String catFiltro, String estadoFiltro, String texto) {
        return filtrar(item -> {
            if (tabCatalogo != null && !tabCatalogo.equalsIgnoreCase(item.catalogo())) {
                return false;
            }
            if (catFiltro != null && !"Todos los catálogos".equals(catFiltro)
                    && !catFiltro.equalsIgnoreCase(item.catalogo())) {
                return false;
            }
            if (estadoFiltro != null && !"Estado".equals(estadoFiltro)
                    && !estadoFiltro.equalsIgnoreCase(item.estado())) {
                return false;
            }
            return contieneTexto(texto, item.nombre(), item.claveDetalle(), item.regla(), item.catalogo());
        });
    }

    public CatalogoItem desactivar(CatalogoItem item) {
        return actualizar(item.conEstado("Inactivo"));
    }
}
