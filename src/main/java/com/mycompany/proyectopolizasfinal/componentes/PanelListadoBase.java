package com.mycompany.proyectopolizasfinal.componentes;

import com.formdev.flatlaf.FlatClientProperties;
import com.mycompany.proyectopolizasfinal.componentes.tabla.TablaOperativa;
import com.mycompany.proyectopolizasfinal.util.EstiloUI;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

/**
 * Base reutilizable para vistas de listado MVC: renderiza tabla, filtros, pestañas
 * y menú contextual delegando consultas y mutaciones al controlador de cada módulo.
 */
public abstract class PanelListadoBase<T> extends JPanel {

    protected final List<T> visibles = new ArrayList<>();

    private final JPanel panelBotonesHeader = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
    private final JPanel bloqueSuperior = new JPanel(new BorderLayout(0, 12));
    private final JPanel centroSuperior = new JPanel(new BorderLayout(0, 12));
    private final BarraTabs barraTabs = new BarraTabs();
    private final JPanel filaFiltros = new JPanel(new GridBagLayout());
    private int siguienteColumnaFiltro = 1;

    protected final JTextField txtBuscar = new JTextField();
    protected final DefaultTableModel modeloTabla;
    protected final TablaOperativa tabla;
    protected final TableRowSorter<DefaultTableModel> sorter;
    protected final JPopupMenu menuAccionesFila = new JPopupMenu();

    protected PanelListadoBase(
            String titulo,
            String subtitulo,
            String placeholderBusqueda,
            String[] columnas,
            int alturaFila,
            int... colsCabeceraDerecha) {
        setOpaque(false);
        setLayout(new BorderLayout(0, 14));
        setBorder(BorderFactory.createEmptyBorder(20, 22, 20, 22));

        // 1. Header
        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setOpaque(false);

        JPanel textosHeader = new JPanel();
        textosHeader.setLayout(new BoxLayout(textosHeader, BoxLayout.Y_AXIS));
        textosHeader.setOpaque(false);

        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.putClientProperty(FlatClientProperties.STYLE, "font: bold +7;");
        JLabel lblSubtitulo = new JLabel(subtitulo);
        lblSubtitulo.putClientProperty(FlatClientProperties.STYLE, "foreground: $Label.disabledForeground; font: -1;");

        textosHeader.add(lblTitulo);
        textosHeader.add(Box.createVerticalStrut(2));
        textosHeader.add(lblSubtitulo);

        panelBotonesHeader.setOpaque(false);
        header.add(textosHeader, BorderLayout.WEST);
        header.add(panelBotonesHeader, BorderLayout.EAST);

        // 2. Barra de controles (tabs + búsqueda + combos)
        filaFiltros.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.gridx = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 0, 0);

        txtBuscar.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, placeholderBusqueda);
        txtBuscar.putClientProperty(FlatClientProperties.STYLE, "arc: 12; margin: 4,10,4,10;");
        txtBuscar.setPreferredSize(new Dimension(0, 34));
        txtBuscar.setMinimumSize(new Dimension(120, 34));
        txtBuscar.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { recargarTabla(); }
            @Override
            public void removeUpdate(DocumentEvent e) { recargarTabla(); }
            @Override
            public void changedUpdate(DocumentEvent e) { recargarTabla(); }
        });
        filaFiltros.add(txtBuscar, gbc);

        JPanel barraControles = new JPanel(new BorderLayout(0, 10));
        barraControles.setOpaque(false);
        barraControles.add(barraTabs, BorderLayout.NORTH);
        barraControles.add(filaFiltros, BorderLayout.CENTER);

        centroSuperior.setOpaque(false);
        centroSuperior.add(barraControles, BorderLayout.SOUTH);

        bloqueSuperior.setOpaque(false);
        bloqueSuperior.add(header, BorderLayout.NORTH);
        bloqueSuperior.add(centroSuperior, BorderLayout.CENTER);

        // 3. Tabla + menú contextual
        menuAccionesFila.putClientProperty(FlatClientProperties.STYLE, "borderInsets: 6,4,6,4;");

        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tabla = new TablaOperativa(modeloTabla, alturaFila, true, colsCabeceraDerecha);
        sorter = new TableRowSorter<>(modeloTabla);
        int colAccion = columnas.length - 1;
        sorter.setSortable(colAccion, false);
        tabla.setRowSorter(sorter);
        tabla.vincularMenu(colAccion, menuAccionesFila, 205);

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        if (scroll.getColumnHeader() != null) {
            scroll.getColumnHeader().setOpaque(false);
        }

        JPanel cardTabla = EstiloUI.crearTarjeta(new BorderLayout(), 6, 10);
        cardTabla.add(scroll, BorderLayout.CENTER);

        add(bloqueSuperior, BorderLayout.NORTH);
        add(cardTabla, BorderLayout.CENTER);
    }

    protected final void setBloqueExtraSuperior(JComponent comp) {
        centroSuperior.add(comp, BorderLayout.CENTER);
    }

    protected final JButton agregarBotonPrimario(String texto, String icono, Runnable accion) {
        JButton btn = EstiloUI.crearBotonPrimario(texto, icono);
        if (accion != null) {
            btn.addActionListener(_ -> accion.run());
        }
        panelBotonesHeader.add(btn);
        return btn;
    }

    protected final JButton agregarBotonSecundario(String texto, String icono, Runnable accion) {
        JButton btn = EstiloUI.crearBotonSecundario(texto, icono);
        if (accion != null) {
            btn.addActionListener(_ -> accion.run());
        }
        panelBotonesHeader.add(btn);
        return btn;
    }

    protected final JToggleButton agregarTab(String texto, boolean seleccionado) {
        return barraTabs.agregarTab(texto, seleccionado, this::recargarTabla);
    }

    protected final JComboBox<String> agregarComboFiltro(String[] items, int ancho) {
        JComboBox<String> cmb = new JComboBox<>(items);
        cmb.putClientProperty(FlatClientProperties.STYLE, "arc: 12;");
        Dimension dim = new Dimension(ancho, 34);
        cmb.setPreferredSize(dim);
        cmb.setMinimumSize(dim);
        cmb.addActionListener(_ -> recargarTabla());

        int count = filaFiltros.getComponentCount();
        if (count > 0 && filaFiltros.getLayout() instanceof GridBagLayout gbl) {
            var prev = filaFiltros.getComponent(count - 1);
            GridBagConstraints prevGbc = gbl.getConstraints(prev);
            prevGbc.insets = new Insets(0, 0, 0, 8);
            gbl.setConstraints(prev, prevGbc);
        }

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.gridx = siguienteColumnaFiltro++;
        gbc.weightx = 0.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 0, 0);
        filaFiltros.add(cmb, gbc);
        return cmb;
    }

    protected final JMenuItem agregarItemMenu(String texto, String icono, Consumer<T> accion) {
        JMenuItem item = EstiloUI.crearItemMenu(texto, icono, "MenuItem.foreground");
        item.addActionListener(_ -> {
            T sel = elementoSeleccionado();
            if (sel != null && accion != null) {
                accion.accept(sel);
            }
        });
        menuAccionesFila.add(item);
        return item;
    }

    protected final JMenuItem agregarItemMenuDestructivo(String texto, String icono, Consumer<T> accion) {
        JMenuItem item = EstiloUI.crearItemMenuDestructivo(texto, icono);
        item.addActionListener(_ -> {
            T sel = elementoSeleccionado();
            if (sel != null && accion != null) {
                accion.accept(sel);
            }
        });
        menuAccionesFila.add(item);
        return item;
    }

    protected final void agregarSeparadorMenu() {
        menuAccionesFila.addSeparator();
    }

    protected final void setAnchoMinimoMenu(int ancho) {
        tabla.setAnchoMinimoMenu(ancho);
    }

    protected final void setOnDobleClick(Consumer<T> accion) {
        tabla.setOnDobleClick(viewRow -> {
            T item = elementoEnFila(viewRow);
            if (item != null && accion != null) {
                accion.accept(item);
            }
        });
    }

    protected final T elementoSeleccionado() {
        return elementoEnFila(tabla.getFilaMenuActual());
    }

    protected final T elementoEnFila(int viewRow) {
        if (viewRow < 0 || viewRow >= tabla.getRowCount()) {
            return null;
        }
        int modelRow = tabla.convertRowIndexToModel(viewRow);
        return (modelRow >= 0 && modelRow < visibles.size()) ? visibles.get(modelRow) : null;
    }

    protected void antesDeRecargar() {}

    public final void recargarTabla() {
        antesDeRecargar();
        modeloTabla.setRowCount(0);
        visibles.clear();
        String texto = txtBuscar.getText() != null ? txtBuscar.getText().trim() : "";

        for (T item : obtenerElementosFiltrados(texto)) {
            visibles.add(item);
            modeloTabla.addRow(mapearFila(item));
        }
        tabla.limpiarHover();
    }

    protected abstract List<T> obtenerElementosFiltrados(String textoBusqueda);

    protected abstract Object[] mapearFila(T item);

    public JTextField getTxtBuscar() {
        return txtBuscar;
    }

    public TablaOperativa getTabla() {
        return tabla;
    }

    public JPopupMenu getMenuAccionesFila() {
        return menuAccionesFila;
    }
}
