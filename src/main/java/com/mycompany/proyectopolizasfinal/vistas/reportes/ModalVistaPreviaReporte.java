package com.mycompany.proyectopolizasfinal.vistas.reportes;

import com.formdev.flatlaf.FlatClientProperties;
import com.mycompany.proyectopolizasfinal.modelo.Reporte;
import com.mycompany.proyectopolizasfinal.util.EstiloUI;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.awt.print.PrinterException;
import java.text.MessageFormat;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

/**
 * Visor de vista previa tipo JasperViewer con opciones directas de Imprimir y Descargar PDF.
 */
public class ModalVistaPreviaReporte extends JDialog {

    private final JTable tablaHoja;

    public ModalVistaPreviaReporte(Window owner, Reporte reporte, List<String[]> filas) {
        super(owner, "Vista Previa · " + reporte.folio(), ModalityType.APPLICATION_MODAL);

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        EstiloUI.configurarBarraTituloVentana(this);

        Color fondo = EstiloUI.colorOrDefault("Panel.background", new Color(0x0A0A0A));
        getContentPane().setBackground(fondo);
        getRootPane().setBackground(fondo);
        setBackground(fondo);

        getRootPane().registerKeyboardAction(
                _ -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        int top = EstiloUI.margenSuperiorVentana(38, 18);
        JPanel root = new JPanel(new BorderLayout(0, 12));
        root.setOpaque(false);
        root.setBorder(BorderFactory.createEmptyBorder(top, 22, 18, 22));
        root.setPreferredSize(new Dimension(780, 600));

        List<String[]> filasSeguro = filas != null ? filas : List.of();
        tablaHoja = crearTablaHoja(reporte, filasSeguro);

        root.add(crearToolbar(reporte, filasSeguro), BorderLayout.NORTH);
        root.add(crearHojaDocumento(reporte), BorderLayout.CENTER);

        setContentPane(root);
        pack();
        setLocationRelativeTo(owner);
    }

    private JPanel crearToolbar(Reporte r, List<String[]> filas) {
        JPanel bar = new JPanel(new BorderLayout(12, 0));
        bar.setOpaque(false);

        JPanel textos = new JPanel();
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        textos.setOpaque(false);
        JLabel lblTit = new JLabel(r.tipo() + " · " + r.folio());
        lblTit.putClientProperty(FlatClientProperties.STYLE, "font: bold +4;");
        JLabel lblSub = new JLabel("Vista previa lista para imprimir o exportar a PDF");
        lblSub.putClientProperty(FlatClientProperties.STYLE, "foreground: $Label.disabledForeground; font: -1;");
        textos.add(lblTit);
        textos.add(Box.createVerticalStrut(2));
        textos.add(lblSub);

        JButton btnImprimir = new JButton("Imprimir");
        btnImprimir.setFocusPainted(false);
        btnImprimir.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnImprimir.putClientProperty(FlatClientProperties.STYLE, "margin: 6,14,6,14; font: -1;");
        btnImprimir.addActionListener(_ -> imprimirReporte(r));

        JButton btnPdf = new JButton("Descargar PDF");
        btnPdf.setIcon(EstiloUI.cargarIcono("chart-column.svg", 14, "Button.default.foreground"));
        btnPdf.setIconTextGap(6);
        btnPdf.setFocusPainted(false);
        btnPdf.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnPdf.putClientProperty(FlatClientProperties.STYLE,
                "margin: 6,14,6,14; font: bold -1; background: $Button.default.background; foreground: $Button.default.foreground; borderWidth: 0;");
        btnPdf.addActionListener(_ -> GeneradorPdfReportes.exportarPdf(this, r, filas));

        JButton btnCerrar = new JButton("Cerrar");
        btnCerrar.setFocusPainted(false);
        btnCerrar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnCerrar.putClientProperty(FlatClientProperties.STYLE, "margin: 6,12,6,12; font: -1;");
        btnCerrar.addActionListener(_ -> dispose());

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        acciones.setOpaque(false);
        acciones.add(btnCerrar);
        acciones.add(btnImprimir);
        acciones.add(btnPdf);

        bar.add(textos, BorderLayout.WEST);
        bar.add(acciones, BorderLayout.EAST);
        return bar;
    }

    private JScrollPane crearHojaDocumento(Reporte r) {
        JPanel hoja = new JPanel(new BorderLayout(0, 14));
        hoja.setBackground(Color.WHITE);
        hoja.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xD1D5DB), 1),
                BorderFactory.createEmptyBorder(28, 32, 28, 32)));

        JPanel cabeceraHoja = new JPanel();
        cabeceraHoja.setLayout(new BoxLayout(cabeceraHoja, BoxLayout.Y_AXIS));
        cabeceraHoja.setOpaque(false);

        JLabel tit = new JLabel("Sistema de Gestión de Pólizas · " + r.tipo());
        tit.setForeground(new Color(0x111827));
        tit.putClientProperty(FlatClientProperties.STYLE, "font: bold +5;");

        JLabel meta = new JLabel("Folio: " + r.folio() + "   |   Generado: " + r.fechaGeneracion()
                + "   |   Usuario: " + r.usuarioNombre() + " (@" + r.username() + ")");
        meta.setForeground(new Color(0x6B7280));
        meta.putClientProperty(FlatClientProperties.STYLE, "font: -1;");

        JLabel filtros = new JLabel("Periodo: " + r.rangoFechas() + "   |   Filtros: " + r.parametrosFiltros());
        filtros.setForeground(new Color(0x374151));
        filtros.putClientProperty(FlatClientProperties.STYLE, "font: -1;");

        cabeceraHoja.add(tit);
        cabeceraHoja.add(Box.createVerticalStrut(4));
        cabeceraHoja.add(meta);
        cabeceraHoja.add(Box.createVerticalStrut(3));
        cabeceraHoja.add(filtros);

        JScrollPane scrollTabla = new JScrollPane(tablaHoja);
        scrollTabla.setBorder(BorderFactory.createLineBorder(new Color(0xE5E7EB)));
        scrollTabla.getViewport().setBackground(Color.WHITE);

        JLabel pie = new JLabel("Total de registros en vista previa: " + tablaHoja.getRowCount()
                + " (referencia BD: " + r.registros() + ")", SwingConstants.RIGHT);
        pie.setForeground(new Color(0x6B7280));
        pie.putClientProperty(FlatClientProperties.STYLE, "font: -1;");

        hoja.add(cabeceraHoja, BorderLayout.NORTH);
        hoja.add(scrollTabla, BorderLayout.CENTER);
        hoja.add(pie, BorderLayout.SOUTH);

        JScrollPane visorScroll = new JScrollPane(hoja);
        visorScroll.setBorder(BorderFactory.createEmptyBorder());
        visorScroll.setOpaque(false);
        visorScroll.getViewport().setOpaque(false);
        return visorScroll;
    }

    private static JTable crearTablaHoja(Reporte r, List<String[]> filas) {
        String[] cols = GeneradorPdfReportes.columnasPorTipo(r.tipo());
        DefaultTableModel modelo = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        for (String[] fila : filas) {
            modelo.addRow(fila);
        }

        JTable tabla = new JTable(modelo);
        tabla.setBackground(Color.WHITE);
        tabla.setForeground(new Color(0x1F2937));
        tabla.setGridColor(new Color(0xE5E7EB));
        tabla.setShowGrid(true);
        tabla.setRowHeight(32);
        tabla.setFocusable(false);
        tabla.setRowSelectionAllowed(false);

        JTableHeader header = tabla.getTableHeader();
        header.setBackground(new Color(0x193CB8));
        header.setForeground(Color.WHITE);
        header.setReorderingAllowed(false);
        header.putClientProperty(FlatClientProperties.STYLE, "height: 32; font: bold -1;");
        return tabla;
    }

    private void imprimirReporte(Reporte r) {
        try {
            boolean ok = tablaHoja.print(
                    JTable.PrintMode.FIT_WIDTH,
                    new MessageFormat("Sistema de Pólizas - " + r.tipo() + " (" + r.folio() + ")"),
                    new MessageFormat("Página {0}"));
            if (ok) {
                JOptionPane.showMessageDialog(this, "Trabajo enviado a impresión.", "Impresión", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (PrinterException ex) {
            JOptionPane.showMessageDialog(this, "No se pudo imprimir: " + ex.getMessage(), "Error de impresión", JOptionPane.ERROR_MESSAGE);
        }
    }
}
