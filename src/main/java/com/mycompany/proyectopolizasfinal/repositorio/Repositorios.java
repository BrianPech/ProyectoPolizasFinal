package com.mycompany.proyectopolizasfinal.repositorio;

import com.mycompany.proyectopolizasfinal.modelo.CatalogoItem;
import com.mycompany.proyectopolizasfinal.modelo.Cliente;
import com.mycompany.proyectopolizasfinal.modelo.MovimientoPago;
import com.mycompany.proyectopolizasfinal.modelo.Poliza;
import com.mycompany.proyectopolizasfinal.modelo.Reporte;
import com.mycompany.proyectopolizasfinal.modelo.Vehiculo;
import com.mycompany.proyectopolizasfinal.repositorio.jdbc.CatalogoRepositorioJdbc;
import com.mycompany.proyectopolizasfinal.repositorio.jdbc.ClienteRepositorioJdbc;
import com.mycompany.proyectopolizasfinal.repositorio.jdbc.ConexionBD;
import com.mycompany.proyectopolizasfinal.repositorio.jdbc.MovimientoPagoRepositorioJdbc;
import com.mycompany.proyectopolizasfinal.repositorio.jdbc.PolizaRepositorioJdbc;
import com.mycompany.proyectopolizasfinal.repositorio.jdbc.ReporteRepositorioJdbc;
import com.mycompany.proyectopolizasfinal.repositorio.jdbc.UsuarioRepositorioJdbc;
import com.mycompany.proyectopolizasfinal.repositorio.jdbc.VehiculoRepositorioJdbc;
import java.util.List;
import java.util.Optional;

/**
 * Contenedor y fábrica de repositorios de la aplicación.
 * Usa MySQL (implementaciones JDBC) cuando {@link ConexionBD} está configurada y responde; si no, memoria.
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

    public static Repositorios jdbc(ConexionBD conexion) {
        return new Repositorios(
                new ClienteRepositorioJdbc(conexion),
                new VehiculoRepositorioJdbc(conexion),
                new PolizaRepositorioJdbc(conexion),
                new MovimientoPagoRepositorioJdbc(conexion),
                new CatalogoRepositorioJdbc(conexion),
                new UsuarioRepositorioJdbc(conexion),
                new ReporteRepositorioJdbc(conexion));
    }

    private static volatile Repositorios instanciaDefecto;
    private static volatile String errorConexion;

    /**
     * Punto único de inicialización para la app.
     * Si hay configuración de BD ({@code db.properties} o variables de entorno) y MySQL responde,
     * usa los repositorios JDBC; si no, cae a memoria y deja el motivo en {@link #errorConexion()}.
     */
    public static Repositorios defecto() {
        Repositorios r = instanciaDefecto;
        if (r == null) {
            synchronized (Repositorios.class) {
                r = instanciaDefecto;
                if (r == null) {
                    r = crearDefecto();
                    instanciaDefecto = r;
                }
            }
        }
        return r;
    }

    private static Repositorios crearDefecto() {
        ConexionBD conexion = ConexionBD.desdeEntornoOArchivo();
        if (!conexion.estaConfigurada()) {
            errorConexion = "No hay db.properties ni variables DB_URL/DB_USER: se usan datos en memoria.";
            System.err.println("[BD] " + errorConexion);
            return INSTANCIA_MEMORIA;
        }
        Optional<String> error = conexion.probarConexion();
        if (error.isPresent()) {
            errorConexion = error.get();
            System.err.println("[BD] No se pudo conectar a MySQL, se usan datos en memoria: " + errorConexion);
            return INSTANCIA_MEMORIA;
        }
        errorConexion = null;
        return jdbc(conexion);
    }

    /** Motivo por el que {@link #defecto()} cayó a memoria, o vacío si está conectado a MySQL. */
    public static Optional<String> errorConexion() {
        return Optional.ofNullable(errorConexion);
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
