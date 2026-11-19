package com.mycompany.proyectopolizasfinal.vistas.clientes;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.mycompany.proyectopolizasfinal.componentes.PanelListadoBase;
import com.mycompany.proyectopolizasfinal.componentes.tabla.CeldaConIconoRenderer;
import com.mycompany.proyectopolizasfinal.componentes.tabla.CeldaDobleLineaRenderer;
import com.mycompany.proyectopolizasfinal.componentes.tabla.CeldaTextoSimpleRenderer;
import com.mycompany.proyectopolizasfinal.componentes.tabla.TablaOperativa;
import com.mycompany.proyectopolizasfinal.componentes.tabla.TextoDoble;
import com.mycompany.proyectopolizasfinal.controlador.ControladorClientes;
import com.mycompany.proyectopolizasfinal.modelo.Cliente;
import com.mycompany.proyectopolizasfinal.repositorio.Repositorios;
import com.mycompany.proyectopolizasfinal.util.EstiloUI;
import com.mycompany.proyectopolizasfinal.util.Formatos;
import java.util.Comparator;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

/**
 * Vista de directorio de Clientes (contratantes, contacto, pólizas y estado de cuenta).
 */
public class PanelClientes extends PanelListadoBase<Cliente> {

    private final ControladorClientes controlador;
    private final JToggleButton tabTodos;
    private final JToggleButton tabActivos;
    private final JToggleButton tabAdeudo;
    private final JComboBox<String> cmbEstatus;
    private final JComboBox<String> cmbTipo;

    private final FlatSVGIcon iconoPersona = EstiloUI.cargarIcono("user.svg", 15, "Label.foreground");
    private final FlatSVGIcon iconoEmpresa = EstiloUI.cargarIcono("users.svg", 15, "Label.foreground");

    public PanelClientes() {
        this(new ControladorClientes(Repositorios.defecto().clientes()));
    }

    public PanelClientes(ControladorClientes controlador) {
        super("Clientes",
                "Directorio de contratantes, contacto, pólizas activas y saldo",
                "Buscar por nombre, RFC, teléfono o correo...",
                new String[]{"Cliente", "Contacto", "Pólizas", "Prima anual", "Estatus", ""},
                56,
                3, 4);
        this.controlador = controlador;

        agregarBotonPrimario("Nuevo Cliente", "user.svg", () -> abrirModalCliente(null));

        tabTodos = agregarTab("Todos", true);
        tabActivos = agregarTab("Con pólizas activas", false);
        tabAdeudo = agregarTab("Con adeudo", false);

        cmbEstatus = agregarComboFiltro(new String[]{"Todos los estatus", "Al corriente", "Con adeudo", "Inactivo"}, 148);
        cmbTipo = agregarComboFiltro(new String[]{"Tipo", "Fisica", "Moral"}, 132);

        setAnchoMinimoMenu(200);
        agregarItemMenu("Ver expediente", "eye.svg", this::abrirModalCliente);
        agregarItemMenu("Editar cliente", "settings.svg", this::abrirModalCliente);
        agregarItemMenu("Nueva póliza", "shield-check.svg", null);
        agregarItemMenu("Enviar aviso", "send.svg", null);
        agregarSeparadorMenu();
        agregarItemMenuDestructivo("Desactivar cliente", "shield-off.svg", this::desactivarCliente);

        sorter.setComparator(0, TablaOperativa.COMPARADOR_TEXTO_DOBLE);
        sorter.setComparator(1, TablaOperativa.COMPARADOR_TEXTO_DOBLE);
        sorter.setComparator(2, Comparator.comparingInt(
                v -> v instanceof TextoDoble td ? Formatos.parsearEntero(td.principal()) : 0));
        sorter.setComparator(3, Comparator.comparingDouble(Formatos::parsearMonto));
        sorter.setComparator(4, String.CASE_INSENSITIVE_ORDER);

        tabla.configurarColumna(0, 210, new CeldaConIconoRenderer());
        tabla.configurarColumna(1, 165, new CeldaDobleLineaRenderer());
        tabla.configurarColumna(2, 95, new CeldaDobleLineaRenderer());
        tabla.configurarColumna(3, 90, new CeldaTextoSimpleRenderer(SwingConstants.RIGHT, true, false));
        tabla.configurarColumna(4, 95, new CeldaTextoSimpleRenderer(SwingConstants.RIGHT, true, true));
        tabla.configurarColumnaAccion(5, 38, 42);

        recargarTabla();
    }

    private void abrirModalCliente(Cliente existente) {
        new ModalCliente(SwingUtilities.getWindowAncestor(this), existente, guardado -> {
            if (existente == null) {
                controlador.guardar(guardado);
                tabTodos.setSelected(true);
            } else {
                controlador.actualizar(guardado);
            }
            recargarTabla();
        }).setVisible(true);
    }

    private void desactivarCliente(Cliente sel) {
        int op = JOptionPane.showConfirmDialog(this,
                "¿Desactivar al cliente \"" + sel.nombreCompleto() + "\"?",
                "Confirmar baja",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (op == JOptionPane.YES_OPTION) {
            controlador.desactivar(sel);
            recargarTabla();
        }
    }

    @Override
    protected List<Cliente> obtenerElementosFiltrados(String textoBusqueda) {
        return controlador.filtrar(
                tabActivos.isSelected(),
                tabAdeudo.isSelected(),
                (String) cmbEstatus.getSelectedItem(),
                (String) cmbTipo.getSelectedItem(),
                textoBusqueda);
    }

    @Override
    protected Object[] mapearFila(Cliente c) {
        boolean esEmpresa = "Moral".equalsIgnoreCase(c.tipoPersona());
        String txtPolizas = c.polizasActivas() == 1 ? "1 activa" : c.polizasActivas() + " activas";
        return new Object[]{
            new TextoDoble(c.nombreCompleto(), c.rfc(), esEmpresa ? iconoEmpresa : iconoPersona),
            new TextoDoble(c.telefono(), c.correo(), null),
            new TextoDoble(txtPolizas, c.resumenPolizas(), null),
            c.primaAnual(),
            c.estatus(),
            ""
        };
    }
}
