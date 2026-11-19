package com.mycompany.proyectopolizasfinal.componentes;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.FlatLaf;
import com.mycompany.proyectopolizasfinal.util.EstiloUI;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

/**
 * Componente Avatar circular con borde primario y menú desplegable para el perfil de usuario.
 */
public class UserProfile extends JPanel {

    private static final int AVATAR_SIZE = 36;
    private static final int MENU_AVATAR_SIZE = 42;
    private static final float BORDER_WIDTH = 2.0f;

    private final String iniciales;
    private final JPopupMenu menu;
    private final JMenuItem itemPerfil;
    private final JMenuItem itemConfig;
    private final JMenuItem itemCerrarSesion;
    private boolean hovered;
    private long ultimoCierreMs;

    public UserProfile(String nombre, String email, Runnable onLogout) {
        this(nombre, email, "Administrador", onLogout);
    }

    public UserProfile(String nombre, String email, String rol, Runnable onLogout) {
        this.iniciales = extraerIniciales(nombre);
        String rolTexto = (rol == null || rol.isBlank()) ? "Usuario" : rol.trim();

        setOpaque(false);
        Dimension size = new Dimension(AVATAR_SIZE, AVATAR_SIZE);
        setPreferredSize(size);
        setMinimumSize(size);
        setMaximumSize(size);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setToolTipText(nombre);

        menu = new JPopupMenu();
        menu.putClientProperty(FlatClientProperties.STYLE, "borderInsets: 6,4,6,4;");

        JPanel infoHeader = new JPanel();
        infoHeader.setLayout(new BoxLayout(infoHeader, BoxLayout.X_AXIS));
        infoHeader.setOpaque(false);
        infoHeader.setBorder(BorderFactory.createEmptyBorder(8, 14, 10, 16));

        JPanel avatarMenu = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                dibujarAvatar(g, getWidth(), getHeight(), false, 14.0f);
            }
        };
        avatarMenu.setOpaque(false);
        Dimension menuAvatarDim = new Dimension(MENU_AVATAR_SIZE, MENU_AVATAR_SIZE);
        avatarMenu.setPreferredSize(menuAvatarDim);
        avatarMenu.setMinimumSize(menuAvatarDim);
        avatarMenu.setMaximumSize(menuAvatarDim);
        avatarMenu.setAlignmentY(CENTER_ALIGNMENT);

        JPanel datosUsuario = new JPanel();
        datosUsuario.setLayout(new BoxLayout(datosUsuario, BoxLayout.Y_AXIS));
        datosUsuario.setOpaque(false);
        datosUsuario.setAlignmentY(CENTER_ALIGNMENT);

        JLabel lblNombre = new JLabel(nombre);
        lblNombre.setAlignmentX(LEFT_ALIGNMENT);
        lblNombre.putClientProperty(FlatClientProperties.STYLE, "font: bold +2;");

        JLabel lblEmail = new JLabel(email);
        lblEmail.setAlignmentX(LEFT_ALIGNMENT);
        lblEmail.putClientProperty(FlatClientProperties.STYLE, "foreground: $Label.disabledForeground; font: -1;");

        JLabel badgeRol = crearBadgeRol(rolTexto);
        badgeRol.setAlignmentX(LEFT_ALIGNMENT);

        datosUsuario.add(lblNombre);
        datosUsuario.add(Box.createVerticalStrut(2));
        datosUsuario.add(lblEmail);
        datosUsuario.add(Box.createVerticalStrut(6));
        datosUsuario.add(badgeRol);

        infoHeader.add(avatarMenu);
        infoHeader.add(Box.createHorizontalStrut(12));
        infoHeader.add(datosUsuario);
        menu.add(infoHeader);
        menu.addSeparator();

        itemPerfil = crearItemMenu("Mi perfil", "user.svg", "MenuItem.foreground");
        itemConfig = crearItemMenu("Configuración", "settings.svg", "MenuItem.foreground");
        menu.add(itemPerfil);
        menu.add(itemConfig);
        menu.addSeparator();

        itemCerrarSesion = crearItemMenu("Cerrar sesión", "log-out.svg", "UserProfile.destructiveForeground");
        Color destructive = colorOrDefault("UserProfile.destructiveForeground",
                colorOrDefault("Component.error.borderColor", new Color(0xFF6467)));
        itemCerrarSesion.setForeground(destructive);
        if (onLogout != null) {
            itemCerrarSesion.addActionListener(e -> onLogout.run());
        }
        menu.add(itemCerrarSesion);

        menu.addPopupMenuListener(new PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                ultimoCierreMs = System.currentTimeMillis();
            }

            @Override
            public void popupMenuCanceled(PopupMenuEvent e) {
                ultimoCierreMs = System.currentTimeMillis();
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                hovered = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hovered = false;
                repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    alternarMenu();
                }
            }
        });
    }

    private JLabel crearBadgeRol(String rolTexto) {
        JLabel badge = new JLabel(rolTexto) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                Color primary = colorPrimario();
                boolean dark = FlatLaf.isLafDark();
                Color fill = new Color(primary.getRed(), primary.getGreen(), primary.getBlue(), dark ? 48 : 30);
                Color stroke = new Color(primary.getRed(), primary.getGreen(), primary.getBlue(), dark ? 190 : 150);

                float pad = 0.75f;
                float w = getWidth() - pad * 2f;
                float h = getHeight() - pad * 2f;
                RoundRectangle2D.Float pill = new RoundRectangle2D.Float(pad, pad, w, h, h, h);

                g2.setColor(fill);
                g2.fill(pill);
                g2.setColor(stroke);
                g2.setStroke(new BasicStroke(1.1f));
                g2.draw(pill);
                g2.dispose();

                super.paintComponent(g);
            }
        };
        badge.setOpaque(false);
        badge.setBorder(BorderFactory.createEmptyBorder(1, 8, 1, 8));
        Font base = UIManager.getFont("Label.font");
        badge.setFont((base != null ? base : new Font(Font.SANS_SERIF, Font.BOLD, 10)).deriveFont(Font.BOLD, 10.5f));
        badge.setForeground(FlatLaf.isLafDark()
                ? colorOrDefault("Label.foreground", Color.WHITE)
                : colorPrimario());
        return badge;
    }

    private static JMenuItem crearItemMenu(String texto, String icono, String colorKey) {
        return EstiloUI.crearItemMenu(texto, icono, colorKey);
    }

    private void alternarMenu() {
        if (menu.isVisible()) {
            menu.setVisible(false);
            return;
        }
        if (System.currentTimeMillis() - ultimoCierreMs < 200) {
            return;
        }
        mostrarMenu();
    }

    private void mostrarMenu() {
        menu.pack();
        Dimension pref = menu.getPreferredSize();
        int width = Math.max(pref.width, 236);
        menu.setPopupSize(width, pref.height);
        int x = getWidth() - width;
        menu.show(this, x, getHeight() + 6);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        dibujarAvatar(g, getWidth(), getHeight(), hovered, 12.5f);
    }

    private void dibujarAvatar(Graphics g, int width, int height, boolean isHovered, float fontSize) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        float pad = BORDER_WIDTH / 2f + 0.5f;
        float diametro = Math.min(width, height) - pad * 2f;
        Ellipse2D.Float circulo = new Ellipse2D.Float(pad, pad, diametro, diametro);

        Color bg = isHovered
                ? colorOrDefault("UserProfile.hoverBackground", new Color(0x27272A))
                : colorOrDefault("UserProfile.background", new Color(0x262626));
        g2.setColor(bg);
        g2.fill(circulo);

        g2.setColor(colorPrimario());
        g2.setStroke(new BasicStroke(BORDER_WIDTH));
        g2.draw(circulo);

        Color fg = colorOrDefault("UserProfile.foreground",
                colorOrDefault("Label.foreground", Color.WHITE));
        g2.setColor(fg);
        Font base = getFont() != null ? getFont() : UIManager.getFont("Label.font");
        Font fontIniciales = (base != null ? base : new Font(Font.SANS_SERIF, Font.BOLD, 13))
                .deriveFont(Font.BOLD, fontSize);
        g2.setFont(fontIniciales);
        FontMetrics fm = g2.getFontMetrics();
        int textX = Math.round((width - fm.stringWidth(iniciales)) / 2f);
        int textY = Math.round((height - fm.getHeight()) / 2f + fm.getAscent());
        g2.drawString(iniciales, textX, textY);

        g2.dispose();
    }

    private static Color colorPrimario() {
        return colorOrDefault("UserProfile.borderColor",
                colorOrDefault("SideBar.primary",
                        colorOrDefault("Button.default.background", new Color(0x2B7FFF))));
    }

    private static Color colorOrDefault(String key, Color fallback) {
        Color c = UIManager.getColor(key);
        return c != null ? c : fallback;
    }

    private static String extraerIniciales(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            return "U";
        }
        String[] partes = nombre.trim().split("\\s+");
        if (partes.length == 1) {
            return partes[0].substring(0, Math.min(2, partes[0].length())).toUpperCase();
        }
        return ("" + partes[0].charAt(0) + partes[partes.length - 1].charAt(0)).toUpperCase();
    }

    public JPopupMenu getMenu() {
        return menu;
    }

    public JMenuItem getItemPerfil() {
        return itemPerfil;
    }

    public JMenuItem getItemConfig() {
        return itemConfig;
    }

    public JMenuItem getItemCerrarSesion() {
        return itemCerrarSesion;
    }
}
