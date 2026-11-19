package com.mycompany.proyectopolizasfinal.util;

import com.mycompany.proyectopolizasfinal.controlador.ControladorInicio;
import com.mycompany.proyectopolizasfinal.controlador.ControladorPolizas;
import com.mycompany.proyectopolizasfinal.controlador.ControladorReportes;
import com.mycompany.proyectopolizasfinal.controlador.ControladorSesion;
import com.mycompany.proyectopolizasfinal.controlador.ControladorUsuarios;
import com.mycompany.proyectopolizasfinal.modelo.Cancelacion;
import com.mycompany.proyectopolizasfinal.modelo.Cliente;
import com.mycompany.proyectopolizasfinal.modelo.Poliza;
import com.mycompany.proyectopolizasfinal.modelo.Reporte;
import com.mycompany.proyectopolizasfinal.modelo.Usuario;
import com.mycompany.proyectopolizasfinal.repositorio.PersistenciaException;
import com.mycompany.proyectopolizasfinal.repositorio.RepositorioCrud;
import com.mycompany.proyectopolizasfinal.repositorio.RepositorioMemoria;
import com.mycompany.proyectopolizasfinal.repositorio.Repositorios;
import com.mycompany.proyectopolizasfinal.repositorio.jdbc.ConexionBD;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FormatosYRepositoriosTest {

    @Test
    void parsearEnteroYMontosManejanFormatosMixtos() {
        assertEquals(12, Formatos.parsearEntero("12 activas"));
        assertEquals(0, Formatos.parsearEntero("sin datos"));
        assertEquals(0, Formatos.parsearEntero(null));

        assertEquals(12450.5, Formatos.parsearMonto("$12,450.50"), 0.001);
        assertEquals(0.0, Formatos.parsearMonto("—"), 0.001);
        assertEquals(0.0, Formatos.parsearMonto(null), 0.001);

        assertEquals(6225.0, Formatos.parsearMontoCaptura(" $6,225 "), 0.001);
        assertEquals(-1.0, Formatos.parsearMontoCaptura(""), 0.001);
        assertEquals(-1.0, Formatos.parsearMontoCaptura("abc"), 0.001);
    }

    @Test
    void formatearMonedaYFechaYClaveOrden() {
        assertEquals("$12,450", Formatos.formatearMoneda(12450.2));
        assertEquals("$0", Formatos.formatearMoneda(0));
        assertEquals("18 Nov 2026", Formatos.formatearFechaCorta(LocalDate.of(2026, 11, 18)));
        assertEquals("02 Dic 2026", Formatos.formatearFechaCorta(LocalDate.of(2026, 12, 2)));
        assertEquals("-", Formatos.formatearFechaCorta(null));

        assertTrue(Formatos.claveOrdenFecha("18 Nov 2026")
                .compareTo(Formatos.claveOrdenFecha("02 Dic 2026")) < 0);
    }

    @Test
    void repositorioMemoriaIniciaVacioYAsignaIds() {
        Repositorios vacios = Repositorios.enMemoria();
        assertTrue(vacios.clientes().listar().isEmpty());
        assertTrue(vacios.vehiculos().listar().isEmpty());
        assertTrue(vacios.polizas().listar().isEmpty());
        assertTrue(vacios.movimientos().listar().isEmpty());
        assertTrue(vacios.catalogos().listar().isEmpty());
        assertTrue(vacios.usuarios().listar().isEmpty());
        assertTrue(vacios.reportes().listar().isEmpty());

        Cliente c1 = new Cliente(0, "Ana", "Ruiz", "RUIA900101XXX", "Fisica", "555", "a@b.mx", "Dir", 1, "Ind", "$1,000", "Al corriente");
        Cliente c2 = new Cliente(0, "Luis", "Paz", "PAZL910202YYY", "Fisica", "556", "l@b.mx", "Dir 2", 0, "Sin", "$0", "Al corriente");
        RepositorioCrud<Cliente> repo = new RepositorioMemoria<>(List.of(c1), Cliente::id, Cliente::conId);

        assertEquals(1, repo.listar().size());
        assertEquals(1, repo.listar().get(0).id());
        assertThrows(UnsupportedOperationException.class, () -> repo.listar().add(c2));

        Cliente guardado = repo.guardar(c2);
        assertEquals(2, guardado.id());
        assertTrue(repo.buscarPorId(2).isPresent());

        Cliente actualizado = repo.actualizar(guardado.conEstatus("Inactivo"));
        assertEquals("Inactivo", repo.buscarPorId(actualizado.id()).orElseThrow().estatus());

        repo.eliminar(1);
        assertEquals(1, repo.listar().size());
    }

    @Test
    void controladoresDeSesionYDominioFuncionanDesdeEstadoVacio() {
        Repositorios repos = Repositorios.enMemoria();
        ControladorSesion sesion = new ControladorSesion(repos.usuarios());
        ControladorUsuarios ctrlUsuarios = new ControladorUsuarios(repos.usuarios());

        // Con repositorio de usuarios vacío, permite acceso inicial si hay usuario y contraseña
        assertTrue(sesion.iniciarSesion("", "1234".toCharArray()).isPresent());
        assertTrue(sesion.iniciarSesion("operador", "1234".toCharArray()).isEmpty());
        assertEquals("operador", sesion.getUsuarioActual().username());

        // Una vez registrados usuarios, valida contra ellos
        Usuario admin = ctrlUsuarios.guardar(new Usuario(0, "Admin", "admin", "Administrador", "Total", 0, "Activo"));
        ctrlUsuarios.guardar(new Usuario(0, "Inactivo", "inactivo", "Agente de Ventas", "Cobros", 0, "Inactivo"));
        assertTrue(sesion.iniciarSesion("desconocido", "1234".toCharArray()).isPresent());
        assertTrue(sesion.iniciarSesion("inactivo", "1234".toCharArray()).isPresent());
        assertTrue(sesion.iniciarSesion("admin", "1234".toCharArray()).isEmpty());
        assertFalse(ctrlUsuarios.existeUsername("ADMIN", admin));
        assertTrue(ctrlUsuarios.existeUsername("ADMIN", null));

        ControladorPolizas ctrlPolizas = new ControladorPolizas(repos.polizas(), repos.movimientos());
        Poliza nueva = ctrlPolizas.guardar(new Poliza(
                0, "POL-001", "Cliente Prueba", "Auto", "GNP", "Individual",
                "18 Nov 2026", "Contado", "$5,000", "Activa", true, false, false));
        Poliza cancelada = ctrlPolizas.aplicarCancelacion(nueva, new Cancelacion("Venta", "$3,000", "Aprobada"));
        assertEquals("Cancelada", cancelada.estado());
        assertTrue(cancelada.esCancelacion());

        ControladorReportes ctrlReportes = new ControladorReportes(
                repos.reportes(), repos.polizas(), repos.movimientos());
        Reporte rpt = ctrlReportes.generar("Pólizas Vigentes", "GNP", LocalDate.now().minusDays(7), LocalDate.now(), admin);
        assertEquals("RPT-0001", rpt.folio());
        assertEquals(1, rpt.registros());

        ControladorInicio ctrlInicio = new ControladorInicio(repos.polizas(), repos.movimientos());
        assertEquals(0, ctrlInicio.obtenerKpis().polizasActivas());
        assertTrue(ctrlInicio.proximosVencimientos(5).isEmpty());
    }

    @Test
    void conexionBdSinConfigurarFallaDeFormaExplicita() {
        ConexionBD bd = new ConexionBD(null, null, null);
        assertFalse(bd.estaConfigurada());
        assertThrows(PersistenciaException.class, bd::abrirConexion);
    }
}
