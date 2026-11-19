package com.mycompany.proyectopolizasfinal.componentes.tabla;

import com.formdev.flatlaf.FlatClientProperties;
import com.mycompany.proyectopolizasfinal.util.EstiloUI;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 * Renderer de celda de texto plano o estado semántico coloreado.
 */
public class CeldaTextoSimpleRenderer extends JLabel implements TableCellRenderer {

    private final boolean colorearEstado;
    private final String colorBaseKey;
    private int rowActual = -1;
    private JTable tablaActual;

    public CeldaTextoSimpleRenderer(int alineacion, boolean negrita, boolean colorearEstado) {
        this(alineacion, negrita, colorearEstado, "Label.foreground");
    }

    public CeldaTextoSimpleRenderer(int alineacion, boolean negrita, boolean colorearEstado, String colorBaseKey) {
        this.colorearEstado = colorearEstado;
        this.colorBaseKey = colorBaseKey;
        setOpaque(false);
        setHorizontalAlignment(alineacion);
        setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 6));
        putClientProperty(FlatClientProperties.STYLE, negrita ? "font: bold -1;" : "font: -1;");
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
        String texto = value != null ? value.toString() : "";
        setText(texto);
        setForeground(colorearEstado
                ? EstiloUI.colorEstado(texto)
                : EstiloUI.colorOrDefault(colorBaseKey, Color.WHITE));
        return this;
    }
}
