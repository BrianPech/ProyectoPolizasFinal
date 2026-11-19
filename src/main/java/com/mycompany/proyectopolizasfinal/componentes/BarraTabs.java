package com.mycompany.proyectopolizasfinal.componentes;

import com.formdev.flatlaf.FlatClientProperties;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.UIManager;

/**
 * Barra de pestañas tipo segmento redondeado con selección exclusiva.
 */
public class BarraTabs extends JPanel {

    private final JPanel contenedor;
    private final ButtonGroup grupo = new ButtonGroup();

    public BarraTabs() {
        super(new FlowLayout(FlowLayout.LEFT, 0, 0));
        setOpaque(false);

        contenedor = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 4)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = UIManager.getColor("Card.background");
                Color bd = UIManager.getColor("Component.borderColor");
                RoundRectangle2D.Float rect = new RoundRectangle2D.Float(
                        0.5f, 0.5f, getWidth() - 1f, getHeight() - 1f, 14f, 14f);
                if (bg != null) {
                    g2.setColor(bg);
                    g2.fill(rect);
                }
                if (bd != null) {
                    g2.setColor(bd);
                    g2.draw(rect);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        contenedor.setOpaque(false);
        add(contenedor);
    }

    public JToggleButton agregarTab(String texto, boolean seleccionado, Runnable onSeleccion) {
        JToggleButton tab = new JToggleButton(texto, seleccionado);
        tab.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_BORDERLESS);
        tab.putClientProperty(FlatClientProperties.STYLE,
                "margin: 5,12,5,12; font: bold -1; arc: 10; "
                + "foreground: $SideBar.button.foreground; "
                + "selectedBackground: $SideBar.button.selectedBackground; selectedForeground: $SideBar.button.selectedForeground; "
                + "hoverBackground: $SideBar.button.hoverBackground; hoverForeground: $SideBar.button.hoverForeground;");
        tab.setFocusPainted(false);
        tab.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        if (onSeleccion != null) {
            tab.addActionListener(_ -> onSeleccion.run());
        }
        grupo.add(tab);
        contenedor.add(tab);
        return tab;
    }
}
