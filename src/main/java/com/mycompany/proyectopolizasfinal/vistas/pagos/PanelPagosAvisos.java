package com.mycompany.proyectopolizasfinal.vistas.pagos;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.mycompany.proyectopolizasfinal.componentes.PanelListadoBase;
import com.mycompany.proyectopolizasfinal.componentes.tabla.CeldaConIconoRenderer;
import com.mycompany.proyectopolizasfinal.componentes.tabla.CeldaDobleLineaRenderer;
import com.mycompany.proyectopolizasfinal.componentes.tabla.CeldaTextoSimpleRenderer;
import com.mycompany.proyectopolizasfinal.componentes.tabla.TablaOperativa;
import com.mycompany.proyectopolizasfinal.componentes.tabla.TextoDoble;
import com.mycompany.proyectopolizasfinal.controlador.ControladorPagos;
import com.mycompany.proyectopolizasfinal.modelo.MovimientoPago;
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
 * Vista operativa de Pagos, cuotas por cobrar y Avisos de vencimiento.
 */
public class PanelPagosAvisos extends PanelListadoBase<MovimientoPago> {

    private final ControladorPagos controlador;
    private final JToggleButton tabTodos;
    private final JToggleButton tabPendientes;
    private final JToggleButton tabVencidos;
    private final JToggleButton tabAvisos;
    private final JComboBox<String> cmbTipo;
    private final JComboBox<String> cmbEstado;

    private final FlatSVGIcon iconoPago = EstiloUI.cargarIcono("credit-card.svg", 15, "Label.foreground");
    private final FlatSVGIcon iconoAviso = EstiloUI.cargarIcono("send.svg", 15, "Label.foreground");

    public PanelPagosAvisos() {
        this(new ControladorPagos(Repositorios.defecto().movimientos()));
    }

    public PanelPagosAvisos(ControladorPagos controlador) {
        super("Pagos y Avisos",
                "Control de cuotas, cobranza pendiente y avisos de vencimiento",
                "Buscar por # póliza, cliente, referencia o canal...",
                new String[]{"Movimiento", "Canal / Método", "Fecha", "Monto", "Estado", ""},
                56,
                3, 4);
        this.controlador = controlador;

        agregarBotonSecundario("Nuevo Aviso", "send.svg", () -> abrirModalAviso(null));
        agregarBotonPrimario("Registrar Pago", "credit-card.svg", () -> abrirModalPago(null, null));

        tabTodos = agregarTab("Todos", true);
        tabPendientes = agregarTab("Pagos pendientes", false);
        tabVencidos = agregarTab("Vencidos", false);
        tabAvisos = agregarTab("Avisos enviados", false);

        cmbTipo = agregarComboFiltro(new String[]{"Todos los tipos", "Pago / Cuota", "Aviso"}, 148);
        cmbEstado = agregarComboFiltro(new String[]{"Estado", "Pendiente", "Vencido", "Pagado", "Enviado"}, 132);

        setAnchoMinimoMenu(215);
        agregarItemMenu("Registrar pago", "credit-card.svg", m -> abrirModalPago(m.polizaCliente(), m.monto()));
        agregarItemMenu("Enviar / reenviar aviso", "send.svg", m -> abrirModalAviso(m.polizaCliente()));
        agregarItemMenu("Ver comprobante", "eye.svg", this::mostrarComprobante);
        agregarSeparadorMenu();
        agregarItemMenuDestructivo("Anular movimiento", "shield-off.svg", this::anularMovimiento);

        sorter.setComparator(0, TablaOperativa.COMPARADOR_TEXTO_DOBLE);
        sorter.setComparator(1, TablaOperativa.COMPARADOR_TEXTO_DOBLE);
        sorter.setComparator(2, Comparator.comparing(
                v -> Formatos.claveOrdenFecha(v instanceof TextoDoble td ? td.principal() : String.valueOf(v))));
        sorter.setComparator(3, Comparator.comparingDouble(Formatos::parsearMonto));
        sorter.setComparator(4, String.CASE_INSENSITIVE_ORDER);

        tabla.configurarColumna(0, 220, new CeldaConIconoRenderer());
        tabla.configurarColumna(1, 135, new CeldaDobleLineaRenderer());
        tabla.configurarColumna(2, 115, new CeldaDobleLineaRenderer());
        tabla.configurarColumna(3, 85, new CeldaTextoSimpleRenderer(SwingConstants.RIGHT, true, false));
        tabla.configurarColumna(4, 85, new CeldaTextoSimpleRenderer(SwingConstants.RIGHT, true, true));
        tabla.configurarColumnaAccion(5, 38, 42);

        recargarTabla();
    }

    private void abrirModalPago(String poliza, String monto) {
        new ModalPago(SwingUtilities.getWindowAncestor(this), poliza, monto, nuevo -> {
            controlador.guardar(nuevo);
            tabTodos.setSelected(true);
            recargarTabla();
        }).setVisible(true);
    }

    private void abrirModalAviso(String poliza) {
        new ModalAviso(SwingUtilities.getWindowAncestor(this), poliza, nuevo -> {
            controlador.guardar(nuevo);
            tabTodos.setSelected(true);
            recargarTabla();
        }).setVisible(true);
    }

    private void mostrarComprobante(MovimientoPago sel) {
        JOptionPane.showMessageDialog(this,
                "Concepto: " + sel.concepto() + "\n"
                + "Póliza / Cliente: " + sel.polizaCliente() + "\n"
                + "Canal / Forma: " + sel.canal() + " (" + sel.detalleCanal() + ")\n"
                + "Fecha: " + sel.fecha() + "\n"
                + "Monto: " + sel.monto() + "\n"
                + "Estado: " + sel.estado(),
                "Comprobante de movimiento",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void anularMovimiento(MovimientoPago sel) {
        int op = JOptionPane.showConfirmDialog(this,
                "¿Anular el movimiento \"" + sel.concepto() + "\"?",
                "Confirmar anulación",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (op == JOptionPane.YES_OPTION) {
            controlador.anular(sel);
            recargarTabla();
        }
    }

    @Override
    protected List<MovimientoPago> obtenerElementosFiltrados(String textoBusqueda) {
        return controlador.filtrar(
                tabPendientes.isSelected(),
                tabVencidos.isSelected(),
                tabAvisos.isSelected(),
                (String) cmbTipo.getSelectedItem(),
                (String) cmbEstado.getSelectedItem(),
                textoBusqueda);
    }

    @Override
    protected Object[] mapearFila(MovimientoPago m) {
        boolean esPago = "Pago / Cuota".equals(m.tipo());
        return new Object[]{
            new TextoDoble(m.concepto(), m.polizaCliente(), esPago ? iconoPago : iconoAviso),
            new TextoDoble(m.canal(), m.detalleCanal(), null),
            new TextoDoble(m.fecha(), m.detalleFecha(), null),
            m.monto(),
            m.estado(),
            ""
        };
    }
}
