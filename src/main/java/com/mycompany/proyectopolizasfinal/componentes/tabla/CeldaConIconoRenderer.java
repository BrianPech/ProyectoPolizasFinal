package com.mycompany.proyectopolizasfinal.componentes.tabla;

import com.formdev.flatlaf.FlatClientProperties;
import com.mycompany.proyectopolizasfinal.util.EstiloUI;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.table.TableCellRenderer;

/**
 * Renderer de celda principal con badge de ícono redondeado y doble línea de texto.
 */
public class CeldaConIconoRenderer extends JPanel implements TableCellRenderer {

    private final JLabel lblIcono;
    private final JLabel lblPrincipal;
    private final JLabel lblSecundario;
    private int rowActual = -1;
    private JTable tablaActual;

    public CeldaConIconoRenderer() {
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(8, 6, 8, 6));

        JPanel badge = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = UIManager.getColor("Button.background");
                g2.setColor(bg != null ? bg : new Color(0x27272A));
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10f, 10f));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        badge.setOpaque(false);
        Dimension dimBadge = new Dimension(32, 32);
        badge.setPreferredSize(dimBadge);
        badge.setMinimumSize(dimBadge);
        badge.setMaximumSize(dimBadge);
        badge.setAlignmentY(CENTER_ALIGNMENT);

        lblIcono = new JLabel();
        lblIcono.setHorizontalAlignment(SwingConstants.CENTER);
        badge.add(lblIcono, BorderLayout.CENTER);

        JPanel textos = new JPanel();
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        textos.setOpaque(false);
        textos.setAlignmentY(CENTER_ALIGNMENT);

        lblPrincipal = new JLabel();
        lblPrincipal.putClientProperty(FlatClientProperties.STYLE, "font: bold -1;");
        lblSecundario = new JLabel();
        lblSecundario.putClientProperty(FlatClientProperties.STYLE, "foreground: $Label.disabledForeground; font: -2;");

        textos.add(lblPrincipal);
        textos.add(Box.createVerticalStrut(1));
        textos.add(lblSecundario);

        add(badge);
        add(Box.createHorizontalStrut(10));
        add(textos);
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
            lblIcono.setIcon(td.icono());
            lblPrincipal.setText(td.principal());
            lblSecundario.setText(td.secundario());
        }
        return this;
    }
}
