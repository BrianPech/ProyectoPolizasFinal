package com.mycompany.proyectopolizasfinal.controlador;

import com.mycompany.proyectopolizasfinal.modelo.Vehiculo;
import com.mycompany.proyectopolizasfinal.repositorio.RepositorioCrud;
import java.util.List;

/**
 * Controlador del módulo de Vehículos.
 */
public class ControladorVehiculos extends ControladorListado<Vehiculo> {

    public ControladorVehiculos(RepositorioCrud<Vehiculo> repositorio) {
        super(repositorio);
    }

    public List<Vehiculo> filtrar(boolean soloAsegurados, boolean soloPorRenovar, boolean soloSinPoliza,
            String usoFiltro, String estatusFiltro, String texto) {
        return filtrar(v -> {
            if (soloAsegurados && !"Asegurado".equalsIgnoreCase(v.estatus())) {
                return false;
            }
            if (soloPorRenovar && !"Por Vencer".equalsIgnoreCase(v.estatus())) {
                return false;
            }
            if (soloSinPoliza && !"Sin póliza".equalsIgnoreCase(v.estatus())) {
                return false;
            }
            if (usoFiltro != null && !"Todos los usos".equals(usoFiltro)
                    && !usoFiltro.equalsIgnoreCase(v.uso())) {
                return false;
            }
            if (estatusFiltro != null && !"Estatus".equals(estatusFiltro)
                    && !estatusFiltro.equalsIgnoreCase(v.estatus())) {
                return false;
            }
            return contieneTexto(texto, v.marcaModelo(), v.placasColor(), v.vin(),
                    v.poliza(), v.aseguradoraCliente());
        });
    }

    public Vehiculo darDeBaja(Vehiculo vehiculo) {
        return actualizar(vehiculo.dadoDeBaja());
    }
}
