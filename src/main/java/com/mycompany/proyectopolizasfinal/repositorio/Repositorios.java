package com.mycompany.proyectopolizasfinal.repositorio;

import com.mycompany.proyectopolizasfinal.modelo.CatalogoItem;
import com.mycompany.proyectopolizasfinal.modelo.Cliente;
import com.mycompany.proyectopolizasfinal.modelo.MovimientoPago;
import com.mycompany.proyectopolizasfinal.modelo.Poliza;
import com.mycompany.proyectopolizasfinal.modelo.Reporte;
import com.mycompany.proyectopolizasfinal.modelo.Vehiculo;
import com.mycompany.proyectopolizasfinal.repositorio.jdbc.ConexionBD;
import java.util.List;

/**
 * Contenedor y fábrica de repositorios de la aplicación.
 * Inicia vacío en memoria; cuando se conecte MySQL, aquí se instanciarán las implementaciones JDBC usando {@link ConexionBD}.
 */
public final class Repositorios {

    private static final Repositorios INSTANCIA_MEMORIA = enMemoria();

    private final RepositorioCrud<Cliente> clientes;
    private final RepositorioCrud<Vehiculo> vehiculos;
    private final RepositorioCrud<Poliza> polizas;
    private final RepositorioCrud<MovimientoPago> movimientos;
    private final RepositorioCrud<CatalogoItem> catalogos;
    private final UsuarioRepositorio usuarios;
    private final RepositorioCrud<Reporte> reportes;

    public Repositorios(
            RepositorioCrud<Cliente> clientes,
            RepositorioCrud<Vehiculo> vehiculos,
            RepositorioCrud<Poliza> polizas,
            RepositorioCrud<MovimientoPago> movimientos,
            RepositorioCrud<CatalogoItem> catalogos,
            UsuarioRepositorio usuarios,
            RepositorioCrud<Reporte> reportes) {
        this.clientes = clientes;
        this.vehiculos = vehiculos;
        this.polizas = polizas;
        this.movimientos = movimientos;
        this.catalogos = catalogos;
        this.usuarios = usuarios;
        this.reportes = reportes;
    }

    public static Repositorios enMemoria() {
        return new Repositorios(
                new RepositorioMemoria<>(List.of(), Cliente::id, Cliente::conId),
                new RepositorioMemoria<>(List.of(), Vehiculo::id, Vehiculo::conId),
                new RepositorioMemoria<>(List.of(), Poliza::id, Poliza::conId),
                new RepositorioMemoria<>(List.of(), MovimientoPago::id, MovimientoPago::conId),
                new RepositorioMemoria<>(List.of(), CatalogoItem::id, CatalogoItem::conId),
                new UsuarioRepositorioMemoria(List.of()),
                new RepositorioMemoria<>(List.of(), Reporte::id, Reporte::conId));
    }

    /**
     * Punto único de inicialización para la app.
     * Deja el hueco preparado para activar implementaciones JDBC cuando {@link ConexionBD#estaConfigurada()} sea true.
     */
    public static Repositorios defecto() {
        ConexionBD conexion = ConexionBD.desdeEntornoOArchivo();
        // TODO(BD): cuando implementes *RepositorioJdbc, cambia el retorno cuando conexion.estaConfigurada() sea true.
        if (conexion.estaConfigurada()) {
            return INSTANCIA_MEMORIA;
        }
        return INSTANCIA_MEMORIA;
    }

    public RepositorioCrud<Cliente> clientes() {
        return clientes;
    }

    public RepositorioCrud<Vehiculo> vehiculos() {
        return vehiculos;
    }

    public RepositorioCrud<Poliza> polizas() {
        return polizas;
    }

    public RepositorioCrud<MovimientoPago> movimientos() {
        return movimientos;
    }

    public RepositorioCrud<CatalogoItem> catalogos() {
        return catalogos;
    }

    public UsuarioRepositorio usuarios() {
        return usuarios;
    }

    public RepositorioCrud<Reporte> reportes() {
        return reportes;
    }
}
