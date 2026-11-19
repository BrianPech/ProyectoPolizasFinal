package com.mycompany.proyectopolizasfinal.util;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import com.formdev.flatlaf.util.SystemInfo;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JTable;
import javax.swing.RootPaneContainer;
import javax.swing.UIManager;

/**
 * Utilidades compartidas de tema, íconos, tarjetas y colores de estado.
 */
public final class EstiloUI {

    private static final Set<String> ESTADOS_ERROR = Set.of(
            "inactivo", "sin póliza", "vencido", "vencida", "cancelada", "cancelado", "urgente");
    private static final Set<String> ESTADOS_ALERTA = Set.of(
            "con adeudo", "por vencer", "pendiente");

    private EstiloUI() {}

    public static boolean esMac() {
        return SystemInfo.isMacOS;
    }

    /**
     * Inicializa el tema FlatLaf de forma condicional según el sistema operativo
     * (FlatMacDarkLaf + apariencia de sistema en macOS; FlatDarkLaf en Windows/Linux).
     */
    public static void inicializarTema() {
        if (SystemInfo.isMacOS) {
            System.setProperty("apple.awt.application.appearance", "system");
        }
        FlatLaf.registerCustomDefaultsSource("com.mycompany.proyectopolizasfinal.theme");
        if (SystemInfo.isMacOS) {
            FlatMacDarkLaf.setup();
        } else {
            FlatDarkLaf.setup();
        }
    }

    /**
     * Aplica barra de título transparente y contenido a ventana completa solo en macOS.
     */
    public static void configurarBarraTituloVentana(RootPaneContainer ventana) {
        if (ventana != null && SystemInfo.isMacFullWindowContentSupported) {
            JRootPane root = ventana.getRootPane();
            root.putClientProperty("apple.awt.fullWindowContent", true);
            root.putClientProperty("apple.awt.transparentTitleBar", true);
        }
    }

    /**
     * Devuelve el margen superior adecuado según si la barra de título se superpone al contenido (macOS) o no.
     */
    public static int margenSuperiorVentana(int topMac, int topOtros) {
        return SystemInfo.isMacFullWindowContentSupported ? topMac : topOtros;
    }

    /** Carga un ícono SVG del paquete de íconos y lo tiñe dinámicamente según la clave del tema. */
    public static FlatSVGIcon cargarIcono(String archivo, int tamano, String colorKey) {
        FlatSVGIcon icon = new FlatSVGIcon("com/mycompany/proyectopolizasfinal/icons/" + archivo, tamano, tamano);
        icon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> {
            Color target = UIManager.getColor(colorKey);
            return target != null ? target : c;
        }));
        return icon;
    }

    public static Color colorOrDefault(String key, Color fallback) {
        Color c = UIManager.getColor(key);
        return c != null ? c : fallback;
    }

    public static Color colorDestructivo() {
        return colorOrDefault("UserProfile.destructiveForeground",
                colorOrDefault("Component.error.borderColor", new Color(0xFF6467)));
    }

    public static Color colorEstado(String estado) {
        if (estado == null) {
            return colorOk();
        }
        String norm = estado.trim().toLowerCase();
        if (ESTADOS_ERROR.contains(norm)) {
            return colorOrDefault("Component.error.borderColor", new Color(0xFF6467));
        }
        if (ESTADOS_ALERTA.contains(norm)) {
            return colorOrDefault("SideBar.primary", new Color(0x2B7FFF));
        }
        return colorOk();
    }

    private static Color colorOk() {
        return FlatLaf.isLafDark() ? new Color(0x34D399) : new Color(0x16A34A);
    }

    /** Crea una tarjeta redondeada con fondo Card.background y borde sutil Component.borderColor. */
    public static JPanel crearTarjeta(LayoutManager layout, int paddingV, int paddingH) {
        JPanel card = new JPanel(layout) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = UIManager.getColor("Card.background");
                Color bd = UIManager.getColor("Component.borderColor");
                RoundRectangle2D.Float rect = new RoundRectangle2D.Float(
                        0.5f, 0.5f, getWidth() - 1f, getHeight() - 1f, 16f, 16f);
                if (bg != null) {
                    g2.setColor(bg);
                    g2.fill(rect);
                }
                if (bd != null) {
                    g2.setColor(bd);
                    g2.setStroke(new BasicStroke(1f));
                    g2.draw(rect);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(paddingV, paddingH, paddingV, paddingH));
        return card;
    }

    public static JButton crearBotonPrimario(String texto, String icono) {
        JButton btn = new JButton(texto);
        if (icono != null) {
            btn.setIcon(cargarIcono(icono, 14, "Button.default.foreground"));
            btn.setIconTextGap(6);
        }
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.putClientProperty(FlatClientProperties.STYLE,
                "margin: 6,12,6,12; font: bold -1; background: $Button.default.background; "
                + "foreground: $Button.default.foreground; borderWidth: 0;");
        return btn;
    }

    public static JButton crearBotonSecundario(String texto, String icono) {
        JButton btn = new JButton(texto);
        if (icono != null) {
            btn.setIcon(cargarIcono(icono, 14, "Button.foreground"));
            btn.setIconTextGap(6);
        }
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.putClientProperty(FlatClientProperties.STYLE, "margin: 6,12,6,12; font: bold -1;");
        return btn;
    }

    public static JMenuItem crearItemMenu(String texto, String icono, String colorKey) {
        JMenuItem item = new JMenuItem(texto, cargarIcono(icono, 16, colorKey));
        item.setIconTextGap(10);
        item.putClientProperty(FlatClientProperties.STYLE, "margin: 7,14,7,14; font: +1;");
        return item;
    }

    public static JMenuItem crearItemMenuDestructivo(String texto, String icono) {
        JMenuItem item = crearItemMenu(texto, icono, "UserProfile.destructiveForeground");
        item.setForeground(colorDestructivo());
        return item;
    }

    public static void pintarFondoYDivisor(Graphics g, Component c, JTable table, int row, int hoverRow) {
        Graphics2D g2 = (Graphics2D) g.create();
        if (row == hoverRow) {
            boolean dark = FlatLaf.isLafDark();
            g2.setColor(dark ? new Color(255, 255, 255, 10) : new Color(0, 0, 0, 8));
            g2.fillRect(0, 0, c.getWidth(), c.getHeight());
        }
        if (row < table.getRowCount() - 1) {
            Color bd = UIManager.getColor("Component.borderColor");
            g2.setColor(bd != null ? bd : new Color(255, 255, 255, 26));
            g2.fillRect(0, c.getHeight() - 1, c.getWidth(), 1);
        }
        g2.dispose();
    }
}
