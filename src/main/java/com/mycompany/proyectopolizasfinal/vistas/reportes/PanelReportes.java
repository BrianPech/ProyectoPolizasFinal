package com.mycompany.proyectopolizasfinal.vistas.reportes;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.mycompany.proyectopolizasfinal.componentes.JDateInput;
import com.mycompany.proyectopolizasfinal.componentes.PanelListadoBase;
import com.mycompany.proyectopolizasfinal.componentes.tabla.CeldaConIconoRenderer;
import com.mycompany.proyectopolizasfinal.componentes.tabla.CeldaDobleLineaRenderer;
import com.mycompany.proyectopolizasfinal.componentes.tabla.CeldaTextoSimpleRenderer;
import com.mycompany.proyectopolizasfinal.componentes.tabla.TablaOperativa;
import com.mycompany.proyectopolizasfinal.componentes.tabla.TextoDoble;
import com.mycompany.proyectopolizasfinal.controlador.ControladorReportes;
import com.mycompany.proyectopolizasfinal.controlador.ControladorSesion;
import com.mycompany.proyectopolizasfinal.modelo.Reporte;
import com.mycompany.proyectopolizasfinal.modelo.Usuario;
import com.mycompany.proyectopolizasfinal.repositorio.Repositorios;
import com.mycompany.proyectopolizasfinal.util.EstiloUI;
import com.mycompany.proyectopolizasfinal.util.Formatos;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

/**
 * Vista de Reportes alineada a la tabla `reporte` (generación con filtros e historial de auditoría).
 */
public class PanelReportes extends PanelListadoBase<Reporte> {

    private final ControladorReportes controlador;
    private final ControladorSesion controladorSesion;

    private final JComboBox<String> cmbTipoGenerar;
    private final JComboBox<String> cmbAsegGenerar;
    private final JDateInput dateDesde;
    private final JDateInput dateHasta;

    private final JToggleButton tabTodos;
    private final JToggleButton tabOperacion;
    private final JToggleButton tabCobranza;
    private final JToggleButton tabCancelaciones;
    private final JComboBox<String> cmbTipoFiltro;
    private final JComboBox<String> cmbUsuarioFiltro;

    private final FlatSVGIcon iconoReporte = EstiloUI.cargarIcono("chart-column.svg", 15, "Label.foreground");

    public PanelReportes() {
        this(new ControladorReportes(Repositorios.defecto().reportes()), null);
    }

    public PanelReportes(ControladorReportes controlador, ControladorSesion controladorSesion) {
        super("Reportes",
                "Generación con filtros e historial de auditoría (tabla reporte)",
                "Buscar por folio, tipo, usuario o parámetros...",
                new String[]{"Reporte", "Generado por", "Parámetros / Filtros", "Registros", "Estado", ""},
                56,
                3, 4);
        this.controlador = controlador;
        this.controladorSesion = controladorSesion;

        agregarBotonPrimario("Generar Reporte", "chart-column.svg", this::generarNuevoReporte);

        JPanel cardParametros = EstiloUI.crearTarjeta(new GridBagLayout(), 12, 14);
        GridBagConstraints gbcP = new GridBagConstraints();
        gbcP.gridy = 0;
        gbcP.fill = GridBagConstraints.HORIZONTAL;

        cmbTipoGenerar = new JComboBox<>(new String[]{
            "Pólizas Vigentes", "Vencimientos y Renovaciones", "Cobranza y Pagos", "Cancelaciones"
        });
        cmbTipoGenerar.putClientProperty(FlatClientProperties.STYLE, "arc: 10;");
        cmbTipoGenerar.setPreferredSize(new Dimension(190, 34));

        cmbAsegGenerar = new JComboBox<>(new String[]{"Todas las aseguradoras", "GNP", "Quálitas", "AXA", "Mapfre"});
        cmbAsegGenerar.putClientProperty(FlatClientProperties.STYLE, "arc: 10;");
        cmbAsegGenerar.setPreferredSize(new Dimension(165, 34));

        dateDesde = new JDateInput(LocalDate.now().minusMonths(1), true);
        dateHasta = new JDateInput(LocalDate.now(), true);

        agregarCampoParametro(cardParametros, gbcP, 0, 1.0, "Tipo de reporte", cmbTipoGenerar, 10);
        agregarCampoParametro(cardParametros, gbcP, 1, 0.85, "Aseguradora", cmbAsegGenerar, 10);
        agregarCampoParametro(cardParametros, gbcP, 2, 0.75, "Desde", dateDesde, 10);
        agregarCampoParametro(cardParametros, gbcP, 3, 0.75, "Hasta", dateHasta, 0);
        setBloqueExtraSuperior(cardParametros);

        tabTodos = agregarTab("Todos", true);
        tabOperacion = agregarTab("Operación", false);
        tabCobranza = agregarTab("Cobranza", false);
        tabCancelaciones = agregarTab("Cancelaciones", false);

        cmbTipoFiltro = agregarComboFiltro(new String[]{
            "Todos los tipos", "Pólizas Vigentes", "Vencimientos y Renovaciones", "Cobranza y Pagos", "Cancelaciones"
        }, 148);
        cmbUsuarioFiltro = agregarComboFiltro(new String[]{"Usuario"}, 132);

        setAnchoMinimoMenu(205);
        agregarItemMenu("Ver vista previa", "eye.svg", this::abrirVistaPrevia);
        agregarItemMenu("Exportar / Enviar", "send.svg",
                r -> GeneradorPdfReportes.exportarPdf(this, r, controlador.filasParaTipo(r.tipo())));
        agregarSeparadorMenu();
        agregarItemMenuDestructivo("Eliminar del historial", "shield-off.svg", this::eliminarReporte);

        setOnDobleClick(this::abrirVistaPrevia);

        sorter.setComparator(0, TablaOperativa.COMPARADOR_TEXTO_DOBLE);
        sorter.setComparator(1, TablaOperativa.COMPARADOR_TEXTO_DOBLE);
        sorter.setComparator(2, TablaOperativa.COMPARADOR_TEXTO_DOBLE);
        sorter.setComparator(3, Comparator.comparingInt(v -> Formatos.parsearEntero(String.valueOf(v))));
        sorter.setComparator(4, String.CASE_INSENSITIVE_ORDER);

        tabla.configurarColumna(0, 210, new CeldaConIconoRenderer());
        tabla.configurarColumna(1, 130, new CeldaDobleLineaRenderer());
        tabla.configurarColumna(2, 190, new CeldaDobleLineaRenderer());
        tabla.configurarColumna(3, 70, new CeldaTextoSimpleRenderer(SwingConstants.RIGHT, true, false));
        tabla.configurarColumna(4, 80, new CeldaTextoSimpleRenderer(SwingConstants.RIGHT, true, true));
        tabla.configurarColumnaAccion(5, 38, 42);

        recargarTabla();
    }

    private void generarNuevoReporte() {
        LocalDate desde = dateDesde.getDate();
        LocalDate hasta = dateHasta.getDate();
        if (desde == null || hasta == null) {
            dateDesde.setError(desde == null);
            dateHasta.setError(hasta == null);
            return;
        }
        dateDesde.setError(false);
        dateHasta.setError(false);

        Usuario autor = controladorSesion != null ? controladorSesion.getUsuarioActual() : null;
        Reporte nuevo = controlador.generar(
                (String) cmbTipoGenerar.getSelectedItem(),
                (String) cmbAsegGenerar.getSelectedItem(),
                desde, hasta, autor);
        tabTodos.setSelected(true);
        recargarTabla();
        abrirVistaPrevia(nuevo);
    }

    private void abrirVistaPrevia(Reporte sel) {
        new ModalVistaPreviaReporte(
                SwingUtilities.getWindowAncestor(this),
                sel,
                controlador.filasParaTipo(sel.tipo())).setVisible(true);
    }

    private void eliminarReporte(Reporte sel) {
        int op = JOptionPane.showConfirmDialog(this,
                "¿Eliminar el reporte " + sel.folio() + " del historial?",
                "Confirmar eliminación",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (op == JOptionPane.YES_OPTION) {
            controlador.eliminar(sel.id());
            recargarTabla();
        }
    }

    private static void agregarCampoParametro(JPanel parent, GridBagConstraints gbc, int col, double weightx,
            String etiqueta, Component comp, int rightGap) {
        JPanel bloque = new JPanel();
        bloque.setLayout(new BoxLayout(bloque, BoxLayout.Y_AXIS));
        bloque.setOpaque(false);

        JLabel lbl = new JLabel(etiqueta);
        lbl.putClientProperty(FlatClientProperties.STYLE, "foreground: $Label.disabledForeground; font: -2;");
        lbl.setAlignmentX(LEFT_ALIGNMENT);
        if (comp instanceof JComponent jc) {
            jc.setAlignmentX(LEFT_ALIGNMENT);
        }

        bloque.add(lbl);
        bloque.add(Box.createVerticalStrut(4));
        bloque.add(comp);

        gbc.gridx = col;
        gbc.weightx = weightx;
        gbc.insets = new Insets(0, 0, 0, rightGap);
        parent.add(bloque, gbc);
    }

    @Override
    protected List<Reporte> obtenerElementosFiltrados(String textoBusqueda) {
        return controlador.filtrar(
                tabOperacion.isSelected(),
                tabCobranza.isSelected(),
                tabCancelaciones.isSelected(),
                (String) cmbTipoFiltro.getSelectedItem(),
                (String) cmbUsuarioFiltro.getSelectedItem(),
                textoBusqueda);
    }

    @Override
    protected Object[] mapearFila(Reporte r) {
        return new Object[]{
            new TextoDoble(r.tipo(), r.folio() + " · " + r.fechaGeneracion(), iconoReporte),
            new TextoDoble(r.usuarioNombre(), "@" + r.username(), null),
            new TextoDoble(r.rangoFechas(), r.parametrosFiltros(), null),
            String.valueOf(r.registros()),
            r.estado(),
            ""
        };
    }
}
