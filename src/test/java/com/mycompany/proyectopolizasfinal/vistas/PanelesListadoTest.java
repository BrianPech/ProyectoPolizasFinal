package com.mycompany.proyectopolizasfinal.vistas;

import com.formdev.flatlaf.util.SystemInfo;
import com.mycompany.proyectopolizasfinal.controlador.ControladorCatalogos;
import com.mycompany.proyectopolizasfinal.controlador.ControladorClientes;
import com.mycompany.proyectopolizasfinal.controlador.ControladorInicio;
import com.mycompany.proyectopolizasfinal.controlador.ControladorPagos;
import com.mycompany.proyectopolizasfinal.controlador.ControladorPolizas;
import com.mycompany.proyectopolizasfinal.controlador.ControladorReportes;
import com.mycompany.proyectopolizasfinal.controlador.ControladorSesion;
import com.mycompany.proyectopolizasfinal.controlador.ControladorUsuarios;
import com.mycompany.proyectopolizasfinal.controlador.ControladorVehiculos;
import com.mycompany.proyectopolizasfinal.modelo.Cliente;
import com.mycompany.proyectopolizasfinal.modelo.Poliza;
import com.mycompany.proyectopolizasfinal.repositorio.Repositorios;
import com.mycompany.proyectopolizasfinal.util.EstiloUI;
import com.mycompany.proyectopolizasfinal.vistas.catalogos.PanelCatalogos;
import com.mycompany.proyectopolizasfinal.vistas.clientes.PanelClientes;
import com.mycompany.proyectopolizasfinal.vistas.inicio.PanelInicio;
import com.mycompany.proyectopolizasfinal.vistas.pagos.PanelPagosAvisos;
import com.mycompany.proyectopolizasfinal.vistas.polizas.PanelDetallePoliza;
import com.mycompany.proyectopolizasfinal.vistas.polizas.PanelPolizas;
import com.mycompany.proyectopolizasfinal.vistas.reportes.PanelReportes;
import com.mycompany.proyectopolizasfinal.vistas.usuarios.PanelUsuarios;
import com.mycompany.proyectopolizasfinal.vistas.vehiculos.PanelVehiculos;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PanelesListadoTest {

    @BeforeAll
    static void configurarTema() throws Exception {
        SwingUtilities.invokeAndWait(EstiloUI::inicializarTema);
    }

    @Test
    void todosLosPanelesInicianVaciosYYPermitenAgregarDatos() throws Exception {
        Repositorios repos = Repositorios.enMemoria();

        SwingUtilities.invokeAndWait(() -> {
            ControladorClientes ctrlClientes = new ControladorClientes(repos.clientes());
            PanelClientes clientes = new PanelClientes(ctrlClientes);
            assertEquals(0, clientes.getTabla().getRowCount());
            assertTrue(clientes.getTabla().getFillsViewportHeight());
            assertEquals("Sin registros", clientes.getTabla().getTituloVacio());
            clientes.getTabla().setSize(600, 300);
            var img = new java.awt.image.BufferedImage(600, 300, java.awt.image.BufferedImage.TYPE_INT_ARGB);
            var g = img.createGraphics();
            assertDoesNotThrow(() -> clientes.getTabla().paint(g));
            g.dispose();

            ctrlClientes.guardar(new Cliente(0, "Juan", "Pérez", "PEMJ880412H19", "Fisica",
                    "55 0000 0000", "juan@correo.com", "CDMX", 0, "Sin pólizas", "$0", "Al corriente"));
            clientes.recargarTabla();
            assertEquals(1, clientes.getTabla().getRowCount());
            clientes.getTxtBuscar().setText("NoExiste");
            assertEquals(0, clientes.getTabla().getRowCount());

            PanelVehiculos vehiculos = new PanelVehiculos(new ControladorVehiculos(repos.vehiculos()));
            assertEquals(0, vehiculos.getTabla().getRowCount());

            ControladorPagos ctrlPagos = new ControladorPagos(repos.movimientos());
            PanelPagosAvisos pagos = new PanelPagosAvisos(ctrlPagos);
            assertEquals(0, pagos.getTabla().getRowCount());

            PanelReportes reportes = new PanelReportes(
                    new ControladorReportes(repos.reportes(), repos.polizas(), repos.movimientos()),
                    new ControladorSesion(repos.usuarios()));
            assertEquals(0, reportes.getTabla().getRowCount());

            PanelCatalogos catalogos = new PanelCatalogos(new ControladorCatalogos(repos.catalogos()));
            assertEquals(0, catalogos.getTabla().getRowCount());

            PanelUsuarios usuarios = new PanelUsuarios(new ControladorUsuarios(repos.usuarios()));
            assertEquals(0, usuarios.getTabla().getRowCount());

            ControladorPolizas ctrlPolizas = new ControladorPolizas(repos.polizas(), repos.movimientos());
            PanelPolizas polizas = new PanelPolizas(ctrlPolizas, ctrlPagos, _ -> {});
            assertEquals(0, polizas.getTabla().getRowCount());

            Poliza p1 = new Poliza(0, "POL-0001", "Juan Pérez", "Auto", "GNP", "Individual",
                    "18 Nov 2026", "Contado", "$5,000", "Activa", true, false, false);
            PanelDetallePoliza detalle = new PanelDetallePoliza(ctrlPolizas, ctrlPagos, () -> {});
            assertDoesNotThrow(() -> detalle.mostrarPoliza(p1));

            PanelInicio inicio = new PanelInicio(
                    new ControladorInicio(repos.polizas(), repos.movimientos()),
                    _ -> {});
            assertEquals(0, inicio.getTablaVencimientos().getRowCount());

            int esperado = SystemInfo.isMacFullWindowContentSupported ? 38 : 18;
            assertEquals(esperado, EstiloUI.margenSuperiorVentana(38, 18));
        });
    }
}
