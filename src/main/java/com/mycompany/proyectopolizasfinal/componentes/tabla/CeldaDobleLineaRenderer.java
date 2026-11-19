package com.mycompany.proyectopolizasfinal.componentes.tabla;

import com.formdev.flatlaf.FlatClientProperties;
import com.mycompany.proyectopolizasfinal.util.EstiloUI;
import java.awt.Component;
import java.awt.Graphics;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 * Renderer de celda con texto principal y subtítulo secundario.
 */
public class CeldaDobleLineaRenderer extends JPanel implements TableCellRenderer {

    private final JLabel lblPrincipal;
    private final JLabel lblSecundario;
    private int rowActual = -1;
    private JTable tablaActual;

    public CeldaDobleLineaRenderer() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(8, 6, 8, 6));

        lblPrincipal = new JLabel();
        lblPrincipal.putClientProperty(FlatClientProperties.STYLE, "font: -1;");
        lblSecundario = new JLabel();
        lblSecundario.putClientProperty(FlatClientProperties.STYLE, "foreground: $Label.disabledForeground; font: -2;");

        add(Box.createVerticalGlue());
        add(lblPrincipal);
        add(Box.createVerticalStrut(1));
        add(lblSecundario);
        add(Box.createVerticalGlue());
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (tablaActual != null) {
            int hoverRow = tablaActual instanceof TablaOperativa t ? t.getFilaHover() : -1;
            EstiloUI.pintarFondoYDivisor(g, this, tablaActual, rowActual, hoverRow);
        }
        super.paintComponent(g);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {
        this.tablaActual = table;
        this.rowActual = row;
        if (value instanceof TextoDoble td) {
            lblPrincipal.setText(td.principal());
            lblSecundario.setText(td.secundario());
        }
        return this;
    }
}
