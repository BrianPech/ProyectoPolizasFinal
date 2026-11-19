package com.mycompany.proyectopolizasfinal.vistas.vehiculos;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.mycompany.proyectopolizasfinal.componentes.PanelListadoBase;
import com.mycompany.proyectopolizasfinal.componentes.tabla.CeldaConIconoRenderer;
import com.mycompany.proyectopolizasfinal.componentes.tabla.CeldaDobleLineaRenderer;
import com.mycompany.proyectopolizasfinal.componentes.tabla.CeldaTextoSimpleRenderer;
import com.mycompany.proyectopolizasfinal.componentes.tabla.TablaOperativa;
import com.mycompany.proyectopolizasfinal.componentes.tabla.TextoDoble;
import com.mycompany.proyectopolizasfinal.controlador.ControladorVehiculos;
import com.mycompany.proyectopolizasfinal.modelo.Vehiculo;
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
 * Vista de parque vehicular asegurado (vehículos, placas, VIN y póliza vinculada).
 */
public class PanelVehiculos extends PanelListadoBase<Vehiculo> {

    private final ControladorVehiculos controlador;
    private final JToggleButton tabTodos;
    private final JToggleButton tabAsegurados;
    private final JToggleButton tabPorRenovar;
    private final JToggleButton tabSinPoliza;
    private final JComboBox<String> cmbUso;
    private final JComboBox<String> cmbEstatus;

    private final FlatSVGIcon iconoAuto = EstiloUI.cargarIcono("car.svg", 15, "Label.foreground");

    public PanelVehiculos() {
        this(new ControladorVehiculos(Repositorios.defecto().vehiculos()));
    }

    public PanelVehiculos(ControladorVehiculos controlador) {
        super("Vehículos",
                "Parque vehicular asegurado, placas, número de serie (VIN) y póliza vinculada",
                "Buscar por marca, modelo, placas, VIN o propietario...",
                new String[]{"Vehículo", "VIN / Uso", "Póliza vinculada", "Año", "Estatus", ""},
                56,
                3, 4);
        this.controlador = controlador;

        agregarBotonPrimario("Nuevo Vehículo", "car.svg", () -> abrirModalVehiculo(null));

        tabTodos = agregarTab("Todos", true);
        tabAsegurados = agregarTab("Asegurados", false);
        tabPorRenovar = agregarTab("Por renovar", false);
        tabSinPoliza = agregarTab("Sin póliza", false);

        cmbUso = agregarComboFiltro(new String[]{"Todos los usos", "Particular", "Comercial"}, 148);
        cmbEstatus = agregarComboFiltro(new String[]{"Estatus", "Asegurado", "Por Vencer", "Sin póliza"}, 132);

        setAnchoMinimoMenu(210);
        agregarItemMenu("Ver ficha del vehículo", "eye.svg", this::abrirModalVehiculo);
        agregarItemMenu("Editar vehículo", "settings.svg", this::abrirModalVehiculo);
        agregarItemMenu("Ver / asignar póliza", "shield-check.svg", null);
        agregarItemMenu("Enviar recordatorio", "send.svg", null);
        agregarSeparadorMenu();
        agregarItemMenuDestructivo("Dar de baja vehículo", "shield-off.svg", this::darDeBaja);

        sorter.setComparator(0, TablaOperativa.COMPARADOR_TEXTO_DOBLE);
        sorter.setComparator(1, TablaOperativa.COMPARADOR_TEXTO_DOBLE);
        sorter.setComparator(2, TablaOperativa.COMPARADOR_TEXTO_DOBLE);
        sorter.setComparator(3, Comparator.comparingDouble(Formatos::parsearMonto));
        sorter.setComparator(4, String.CASE_INSENSITIVE_ORDER);

        tabla.configurarColumna(0, 205, new CeldaConIconoRenderer());
        tabla.configurarColumna(1, 175, new CeldaDobleLineaRenderer());
        tabla.configurarColumna(2, 120, new CeldaDobleLineaRenderer());
        tabla.configurarColumna(3, 100, new CeldaTextoSimpleRenderer(SwingConstants.RIGHT, true, false));
        tabla.configurarColumna(4, 90, new CeldaTextoSimpleRenderer(SwingConstants.RIGHT, true, true));
        tabla.configurarColumnaAccion(5, 38, 42);

        recargarTabla();
    }

    private void abrirModalVehiculo(Vehiculo existente) {
        new ModalVehiculo(SwingUtilities.getWindowAncestor(this), existente, guardado -> {
            if (existente == null) {
                controlador.guardar(guardado);
                tabTodos.setSelected(true);
            } else {
                controlador.actualizar(guardado);
            }
            recargarTabla();
        }).setVisible(true);
    }

    private void darDeBaja(Vehiculo sel) {
        int op = JOptionPane.showConfirmDialog(this,
                "¿Dar de baja el vehículo \"" + sel.marcaModelo() + "\"?",
                "Confirmar baja",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (op == JOptionPane.YES_OPTION) {
            controlador.darDeBaja(sel);
            recargarTabla();
        }
    }

    @Override
    protected List<Vehiculo> obtenerElementosFiltrados(String textoBusqueda) {
        return controlador.filtrar(
                tabAsegurados.isSelected(),
                tabPorRenovar.isSelected(),
                tabSinPoliza.isSelected(),
                (String) cmbUso.getSelectedItem(),
                (String) cmbEstatus.getSelectedItem(),
                textoBusqueda);
    }

    @Override
    protected Object[] mapearFila(Vehiculo v) {
        return new Object[]{
            new TextoDoble(v.marcaModelo(), v.placasColor(), iconoAuto),
            new TextoDoble(v.vin(), "Uso " + v.uso(), null),
            new TextoDoble(v.poliza(), v.aseguradoraCliente(), null),
            String.valueOf(v.anio()),
            v.estatus(),
            ""
        };
    }
}
