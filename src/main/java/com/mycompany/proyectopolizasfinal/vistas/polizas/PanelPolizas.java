package com.mycompany.proyectopolizasfinal.vistas.polizas;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.mycompany.proyectopolizasfinal.componentes.PanelListadoBase;
import com.mycompany.proyectopolizasfinal.componentes.tabla.CeldaConIconoRenderer;
import com.mycompany.proyectopolizasfinal.componentes.tabla.CeldaDobleLineaRenderer;
import com.mycompany.proyectopolizasfinal.componentes.tabla.CeldaTextoSimpleRenderer;
import com.mycompany.proyectopolizasfinal.componentes.tabla.TablaOperativa;
import com.mycompany.proyectopolizasfinal.componentes.tabla.TextoDoble;
import com.mycompany.proyectopolizasfinal.controlador.ControladorPagos;
import com.mycompany.proyectopolizasfinal.controlador.ControladorPolizas;
import com.mycompany.proyectopolizasfinal.modelo.Poliza;
import com.mycompany.proyectopolizasfinal.repositorio.Repositorios;
import com.mycompany.proyectopolizasfinal.util.EstiloUI;
import com.mycompany.proyectopolizasfinal.util.Formatos;
import com.mycompany.proyectopolizasfinal.vistas.pagos.ModalPago;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.JComboBox;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

/**
 * Vista de diseño para el módulo de Pólizas (poliza, poliza_cobertura, renovaciones y cancelacion_poliza).
 */
public class PanelPolizas extends PanelListadoBase<Poliza> {

    private final ControladorPolizas controlador;
    private final ControladorPagos controladorPagos;
    private final Consumer<Poliza> onVerDetalle;

    private final JToggleButton tabTodas;
    private final JToggleButton tabRenovaciones;
    private final JToggleButton tabCancelaciones;
    private final JComboBox<String> cmbEstado;
    private final JComboBox<String> cmbAseguradora;

    private final FlatSVGIcon iconoAuto = EstiloUI.cargarIcono("car.svg", 15, "Label.foreground");
    private final FlatSVGIcon iconoPoliza = EstiloUI.cargarIcono("shield-check.svg", 15, "Label.foreground");

    public PanelPolizas() {
        this(new ControladorPolizas(Repositorios.defecto().polizas()), null, null);
    }

    public PanelPolizas(ControladorPolizas controlador, ControladorPagos controladorPagos,
            Consumer<Poliza> onVerDetalle) {
        super("Pólizas",
                "Administración de pólizas, coberturas, renovaciones y cancelaciones",
                "Buscar por # póliza, cliente o placas...",
                new String[]{"Póliza", "Aseguradora", "Vigencia", "Prima", "Estado", ""},
                50,
                3, 4);
        this.controlador = controlador;
        this.controladorPagos = controladorPagos;
        this.onVerDetalle = onVerDetalle;

        agregarBotonPrimario("Nueva Póliza", "shield-check.svg", this::abrirWizardNuevaPoliza);

        tabTodas = agregarTab("Todas", true);
        tabRenovaciones = agregarTab("Renovaciones", false);
        tabCancelaciones = agregarTab("Cancelaciones", false);

        cmbEstado = agregarComboFiltro(new String[]{"Todos los estados", "Activa", "Por Vencer", "Vencida", "Cancelada"}, 148);
        cmbAseguradora = agregarComboFiltro(new String[]{"Aseguradora", "GNP", "Qualitas", "AXA", "Mapfre"}, 132);

        setAnchoMinimoMenu(210);
        agregarItemMenu("Ver detalles y coberturas", "eye.svg", this::abrirDetalle);
        agregarItemMenu("Renovar póliza", "shield-check.svg", _ -> abrirWizardNuevaPoliza());
        agregarItemMenu("Registrar pago", "credit-card.svg", this::abrirModalPago);
        agregarSeparadorMenu();
        agregarItemMenuDestructivo("Solicitar cancelación", "shield-off.svg", this::solicitarCancelacion);

        setOnDobleClick(this::abrirDetalle);

        sorter.setComparator(0, TablaOperativa.COMPARADOR_TEXTO_DOBLE);
        sorter.setComparator(1, TablaOperativa.COMPARADOR_TEXTO_DOBLE);
        sorter.setComparator(2, Comparator.comparing(
                v -> Formatos.claveOrdenFecha(v instanceof TextoDoble td ? td.principal() : String.valueOf(v))));
        sorter.setComparator(3, Comparator.comparingDouble(Formatos::parsearMonto));
        sorter.setComparator(4, String.CASE_INSENSITIVE_ORDER);

        tabla.configurarColumna(0, 205, new CeldaConIconoRenderer());
        tabla.configurarColumna(1, 105, new CeldaDobleLineaRenderer());
        tabla.configurarColumna(2, 105, new CeldaDobleLineaRenderer());
        tabla.configurarColumna(3, 72, new CeldaTextoSimpleRenderer(SwingConstants.RIGHT, true, false));
        tabla.configurarColumna(4, 84, new CeldaTextoSimpleRenderer(SwingConstants.RIGHT, true, true));
        tabla.configurarColumnaAccion(5, 34, 34);

        recargarTabla();
    }

    private void abrirDetalle(Poliza sel) {
        if (onVerDetalle != null) {
            onVerDetalle.accept(sel);
        }
    }

    private void abrirModalPago(Poliza sel) {
        new ModalPago(SwingUtilities.getWindowAncestor(this),
                sel.numero() + " · " + sel.cliente(),
                sel.prima(),
                pago -> {
                    if (controladorPagos != null) {
                        controladorPagos.guardar(pago);
                    }
                }).setVisible(true);
    }

    private void solicitarCancelacion(Poliza sel) {
        new ModalCancelacion(SwingUtilities.getWindowAncestor(this),
                sel.numero() + " · " + sel.cliente(),
                res -> {
                    controlador.aplicarCancelacion(sel, res);
                    recargarTabla();
                }).setVisible(true);
    }

    private void abrirWizardNuevaPoliza() {
        new PolizasWizzard(SwingUtilities.getWindowAncestor(this), nueva -> {
            controlador.guardar(nueva);
            tabTodas.setSelected(true);
            recargarTabla();
        }).setVisible(true);
    }

    @Override
    protected void antesDeRecargar() {
        tabla.getColumnModel().getColumn(2).setHeaderValue(
                tabCancelaciones.isSelected() ? "Cancelación" : "Vigencia");
        tabla.getTableHeader().repaint();
    }

    @Override
    protected List<Poliza> obtenerElementosFiltrados(String textoBusqueda) {
        return controlador.filtrar(
                tabTodas.isSelected(),
                tabRenovaciones.isSelected(),
                tabCancelaciones.isSelected(),
                (String) cmbEstado.getSelectedItem(),
                (String) cmbAseguradora.getSelectedItem(),
                textoBusqueda);
    }

    @Override
    protected Object[] mapearFila(Poliza p) {
        return new Object[]{
            new TextoDoble(p.numero() + " · " + p.cliente(), p.vehiculo(), p.esAuto() ? iconoAuto : iconoPoliza),
            new TextoDoble(p.aseguradora(), p.tipo(), null),
            new TextoDoble(p.vigencia(), p.detalleVigencia(), null),
            p.prima(),
            p.estado(),
            ""
        };
    }
}
