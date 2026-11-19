package com.mycompany.proyectopolizasfinal.componentes.tabla;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.mycompany.proyectopolizasfinal.util.EstiloUI;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.table.TableCellRenderer;

/**
 * Renderer del botón de menú de acciones (ellipsis) por fila.
 */
public class CeldaAccionRenderer extends JLabel implements TableCellRenderer {

    private final FlatSVGIcon iconoNormal;
    private final FlatSVGIcon iconoActivo;
    private int rowActual = -1;
    private JTable tablaActual;
    private boolean activo;

    public CeldaAccionRenderer() {
        this.iconoNormal = EstiloUI.cargarIcono("ellipsis.svg", 16, "Label.disabledForeground");
        this.iconoActivo = EstiloUI.cargarIcono("ellipsis.svg", 16, "Label.foreground");
        setOpaque(false);
        setHorizontalAlignment(SwingConstants.CENTER);
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (tablaActual != null) {
            int hoverRow = tablaActual instanceof TablaOperativa t ? t.getFilaHover() : -1;
            EstiloUI.pintarFondoYDivisor(g, this, tablaActual, rowActual, hoverRow);
        }
        if (activo) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color bg = UIManager.getColor("Button.background");
            g2.setColor(bg != null ? bg : new Color(0x27272A));
            int size = 28;
            g2.fill(new RoundRectangle2D.Float(
                    (getWidth() - size) / 2f, (getHeight() - size) / 2f, size, size, 8f, 8f));
            g2.dispose();
        }
        super.paintComponent(g);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {
        this.tablaActual = table;
        this.rowActual = row;
        this.activo = table instanceof TablaOperativa t && t.isAccionActiva(row);
        setIcon(activo ? iconoActivo : iconoNormal);
        return this;
    }
}
