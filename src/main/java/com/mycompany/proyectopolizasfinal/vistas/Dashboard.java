package com.mycompany.proyectopolizasfinal.vistas;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.formdev.flatlaf.ui.FlatLineBorder;
import com.mycompany.proyectopolizasfinal.ProyectoPolizasFinal;
import com.mycompany.proyectopolizasfinal.componentes.UserProfile;
import com.mycompany.proyectopolizasfinal.controlador.ControladorCatalogos;
import com.mycompany.proyectopolizasfinal.controlador.ControladorClientes;
import com.mycompany.proyectopolizasfinal.controlador.ControladorInicio;
import com.mycompany.proyectopolizasfinal.controlador.ControladorPagos;
import com.mycompany.proyectopolizasfinal.controlador.ControladorPolizas;
import com.mycompany.proyectopolizasfinal.controlador.ControladorReportes;
import com.mycompany.proyectopolizasfinal.controlador.ControladorSesion;
import com.mycompany.proyectopolizasfinal.controlador.ControladorUsuarios;
import com.mycompany.proyectopolizasfinal.controlador.ControladorVehiculos;
import com.mycompany.proyectopolizasfinal.modelo.Usuario;
import com.mycompany.proyectopolizasfinal.repositorio.Repositorios;
import com.mycompany.proyectopolizasfinal.util.EstiloUI;
import com.mycompany.proyectopolizasfinal.vistas.catalogos.PanelCatalogos;
import com.mycompany.proyectopolizasfinal.vistas.clientes.PanelClientes;
import com.mycompany.proyectopolizasfinal.vistas.inicio.PanelInicio;
import com.mycompany.proyectopolizasfinal.vistas.pagos.PanelPagosAvisos;
import com.mycompany.proyectopolizasfinal.vistas.polizas.PanelDetallePoliza;
import com.mycompany.proyectopolizasfinal.vistas.polizas.PanelPolizas;
import com.mycompany.proyectopolizasfinal.vistas.reportes.PanelReportes;
import com.mycompany.proyectopolizasfinal.vistas.usuarios.PanelUsuarios;
import com.mycompany.proyectopolizasfinal.vistas.vehiculos.PanelVehiculos;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Dashboard extends JFrame {

    private ButtonGroup grupoNavegacion;
    private JPanel panelSidebar;
    private JPanel panelSidebarHeader;
    private JPanel panelHeader;
    private final CardLayout vistasLayout = new CardLayout();
    private final JPanel panelContenido = new JPanel(vistasLayout);
    private UserProfile userProfile;

    private final PanelInicio panelInicio;
    private final PanelPolizas panelPolizas;
    private final PanelPagosAvisos panelPagosAvisos;

    public Dashboard() {
        this(Repositorios.defecto(), null);
    }

    public Dashboard(Repositorios repositorios, ControladorSesion controladorSesion) {
        super("Proyecto Pólizas - Dashboard");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        EstiloUI.configurarBarraTituloVentana(this);

        Color colorSidebar = UIManager.getColor("SideBar.background");
        Color colorInset = UIManager.getColor("SideBar.inset.background");
        if (colorInset == null) {
            colorInset = UIManager.getColor("Panel.background");
        }
        Color colorBorder = UIManager.getColor("SideBar.borderColor");

        getContentPane().setBackground(colorSidebar);
        getRootPane().setBackground(colorSidebar);
        setBackground(colorSidebar);

        JPanel panelTop = new JPanel(new BorderLayout());
        panelTop.setOpaque(false);
        panelTop.setBorder(BorderFactory.createEmptyBorder(
                EstiloUI.margenSuperiorVentana(28, 12), 0, 10, 14));

        panelSidebarHeader = new JPanel();
        panelSidebarHeader.setPreferredSize(new Dimension(220, 36));
        panelSidebarHeader.setOpaque(false);
        panelSidebarHeader.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 15));
        panelSidebarHeader.setLayout(new BoxLayout(panelSidebarHeader, BoxLayout.X_AXIS));

        JPanel badgeMarca = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                Color bg = UIManager.getColor("SideBar.primary");
                if (bg == null) {
                    bg = new Color(0x2B7FFF);
                }
                Color fg = UIManager.getColor("SideBar.primaryForeground");
                if (fg == null) {
                    fg = Color.WHITE;
                }

                g2.setColor(bg);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));

                g2.setColor(fg);
                Font base = getFont() != null ? getFont() : UIManager.getFont("Label.font");
                g2.setFont((base != null ? base : new Font(Font.SANS_SERIF, Font.BOLD, 14)).deriveFont(Font.BOLD, 14f));
                FontMetrics fm = g2.getFontMetrics();
                String letra = "P";
                int x = Math.round((getWidth() - fm.stringWidth(letra)) / 2f);
                int y = Math.round((getHeight() - fm.getHeight()) / 2f + fm.getAscent());
                g2.drawString(letra, x, y);
                g2.dispose();
            }
        };
        badgeMarca.setOpaque(false);
        Dimension badgeSize = new Dimension(28, 28);
        badgeMarca.setPreferredSize(badgeSize);
        badgeMarca.setMinimumSize(badgeSize);
        badgeMarca.setMaximumSize(badgeSize);
        badgeMarca.setAlignmentY(CENTER_ALIGNMENT);

        JLabel lblMarca = new JLabel("Pólizas");
        lblMarca.putClientProperty(FlatClientProperties.STYLE, "font: bold +2;");
        Color fgSidebar = UIManager.getColor("SideBar.foreground");
        if (fgSidebar != null) {
            lblMarca.setForeground(fgSidebar);
        }
        lblMarca.setAlignmentY(CENTER_ALIGNMENT);

        panelSidebarHeader.add(badgeMarca);
        panelSidebarHeader.add(Box.createHorizontalStrut(10));
        panelSidebarHeader.add(lblMarca);

        panelHeader = new JPanel(new BorderLayout());
        panelHeader.setBackground(colorSidebar);
        panelHeader.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 2));

        Usuario activo = controladorSesion != null ? controladorSesion.getUsuarioActual() : null;
        String nombrePerfil = activo != null ? activo.nombre() : "Usuario";
        String correoPerfil = activo != null ? "@" + activo.username() : "usuario@polizas.com";
        String rolPerfil = activo != null ? activo.rol() : "Administrador";

        userProfile = new UserProfile(nombrePerfil, correoPerfil, rolPerfil, () -> {
            if (controladorSesion != null) {
                controladorSesion.cerrarSesion();
            }
            new Login(repositorios).setVisible(true);
            dispose();
        });
        panelHeader.add(userProfile, BorderLayout.EAST);

        panelTop.add(panelSidebarHeader, BorderLayout.WEST);
        panelTop.add(panelHeader, BorderLayout.CENTER);

        panelSidebar = new JPanel();
        panelSidebar.setPreferredSize(new Dimension(220, 0));
        panelSidebar.setOpaque(false);
        panelSidebar.setBorder(BorderFactory.createEmptyBorder(0, 15, 15, 15));
        panelSidebar.setLayout(new BoxLayout(panelSidebar, BoxLayout.Y_AXIS));

        JPanel contenedorContenido = new JPanel(new BorderLayout());
        contenedorContenido.setOpaque(false);
        contenedorContenido.setBorder(BorderFactory.createEmptyBorder(0, 6, 14, 14));

        int insetArc = UIManager.getInt("SideBar.inset.arc");
        if (insetArc <= 0) {
            insetArc = 24;
        }

        panelContenido.setBackground(colorInset);
        panelContenido.putClientProperty(FlatClientProperties.STYLE, "arc: " + insetArc + ";");
        if (colorBorder != null) {
            panelContenido.setBorder(new FlatLineBorder(new Insets(1, 1, 1, 1), colorBorder, 1f, insetArc));
        }

        ControladorPolizas ctrlPolizas = new ControladorPolizas(repositorios.polizas(), repositorios.movimientos());
        ControladorPagos ctrlPagos = new ControladorPagos(repositorios.movimientos());
        ControladorInicio ctrlInicio = new ControladorInicio(repositorios.polizas(), repositorios.movimientos());

        final String[] origenDetalle = {"Pólizas"};
        PanelDetallePoliza panelDetallePoliza = new PanelDetallePoliza(
                ctrlPolizas, ctrlPagos, () -> vistasLayout.show(panelContenido, origenDetalle[0]));
        panelInicio = new PanelInicio(ctrlInicio, poliza -> {
            origenDetalle[0] = "Inicio";
            panelDetallePoliza.mostrarPoliza(poliza);
            vistasLayout.show(panelContenido, "DetallePoliza");
        });
        panelPolizas = new PanelPolizas(ctrlPolizas, ctrlPagos, poliza -> {
            origenDetalle[0] = "Pólizas";
            panelDetallePoliza.mostrarPoliza(poliza);
            vistasLayout.show(panelContenido, "DetallePoliza");
        });
        panelPagosAvisos = new PanelPagosAvisos(ctrlPagos);

        panelContenido.add(panelInicio, "Inicio");
        panelContenido.add(panelPolizas, "Pólizas");
        panelContenido.add(panelDetallePoliza, "DetallePoliza");
        panelContenido.add(new PanelClientes(new ControladorClientes(repositorios.clientes())), "Clientes");
        panelContenido.add(new PanelVehiculos(new ControladorVehiculos(repositorios.vehiculos())), "Vehículos");
        panelContenido.add(panelPagosAvisos, "Pagos y Avisos");
        panelContenido.add(new PanelReportes(
                new ControladorReportes(repositorios.reportes(), repositorios.polizas(), repositorios.movimientos()),
                controladorSesion), "Reportes");
        panelContenido.add(new PanelCatalogos(new ControladorCatalogos(repositorios.catalogos())), "Catálogos");
        panelContenido.add(new PanelUsuarios(new ControladorUsuarios(repositorios.usuarios())), "Usuarios");

        grupoNavegacion = new ButtonGroup();
        agregarGrupoSidebar("Operación", new OpcionSidebar[]{
            new OpcionSidebar("Inicio", "layout-dashboard.svg"),
            new OpcionSidebar("Pólizas", "shield-check.svg"),
            new OpcionSidebar("Clientes", "users.svg"),
            new OpcionSidebar("Vehículos", "car.svg"),
            new OpcionSidebar("Pagos y Avisos", "credit-card.svg")
        }, true, true);
        agregarGrupoSidebar("Análisis", new OpcionSidebar[]{
            new OpcionSidebar("Reportes", "chart-column.svg")
        }, false, false);
        agregarGrupoSidebar("Administración", new OpcionSidebar[]{
            new OpcionSidebar("Catálogos", "layers.svg"),
            new OpcionSidebar("Usuarios", "user-cog.svg")
        }, false, false);

        panelSidebar.add(Box.createVerticalGlue());

        contenedorContenido.add(panelContenido, BorderLayout.CENTER);

        add(panelTop, BorderLayout.NORTH);
        add(panelSidebar, BorderLayout.WEST);
        add(contenedorContenido, BorderLayout.CENTER);

        setLocationRelativeTo(null);
    }

    private record OpcionSidebar(String titulo, String icono) {}

    private void agregarGrupoSidebar(String titulo, OpcionSidebar[] opciones, boolean esPrimero, boolean seleccionarPrimero) {
        JLabel lblGrupo = new JLabel(titulo);
        lblGrupo.setAlignmentX(0.0f);
        lblGrupo.setBorder(BorderFactory.createEmptyBorder(esPrimero ? 0 : 10, 14, 4, 14));
        lblGrupo.putClientProperty(FlatClientProperties.STYLE, "font: bold -2; foreground: $Label.disabledForeground;");
        panelSidebar.add(lblGrupo);

        for (int i = 0; i < opciones.length; i++) {
            OpcionSidebar opcion = opciones[i];
            JToggleButton btn = new JToggleButton(opcion.titulo());
            btn.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_BORDERLESS);
            btn.putClientProperty(FlatClientProperties.STYLE,
                    "margin: 7,14,7,14; font: bold; " +
                    "foreground: $SideBar.button.foreground; " +
                    "selectedBackground: $SideBar.button.selectedBackground; selectedForeground: $SideBar.button.selectedForeground; " +
                    "toolbar.selectedBackground: $SideBar.button.selectedBackground; toolbar.selectedForeground: $SideBar.button.selectedForeground; " +
                    "hoverBackground: $SideBar.button.hoverBackground; hoverForeground: $SideBar.button.hoverForeground;");
            btn.setHorizontalAlignment(SwingConstants.LEFT);
            btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
            btn.setAlignmentX(0.0f);
            btn.setFocusPainted(false);
            btn.setIconTextGap(10);

            FlatSVGIcon iconoSelected = EstiloUI.cargarIcono(opcion.icono(), 16, "SideBar.button.selectedForeground");
            btn.setIcon(EstiloUI.cargarIcono(opcion.icono(), 16, "SideBar.button.foreground"));
            btn.setSelectedIcon(iconoSelected);
            btn.setRolloverIcon(EstiloUI.cargarIcono(opcion.icono(), 16, "SideBar.button.hoverForeground"));
            btn.setRolloverSelectedIcon(iconoSelected);

            if (seleccionarPrimero && i == 0) {
                btn.setSelected(true);
            }
            btn.addActionListener(_ -> mostrarVista(opcion.titulo()));

            grupoNavegacion.add(btn);
            panelSidebar.add(btn);
            panelSidebar.add(Box.createVerticalStrut(4));
        }
    }

    private void mostrarVista(String nombre) {
        switch (nombre) {
            case "Inicio" -> panelInicio.refrescar();
            case "Pólizas" -> panelPolizas.recargarTabla();
            case "Pagos y Avisos" -> panelPagosAvisos.recargarTabla();
            default -> {}
        }
        vistasLayout.show(panelContenido, nombre);
    }

    public ButtonGroup getGrupoNavegacion() {
        return grupoNavegacion;
    }

    public JPanel getPanelSidebar() {
        return panelSidebar;
    }

    public JPanel getPanelSidebarHeader() {
        return panelSidebarHeader;
    }

    public JPanel getPanelHeader() {
        return panelHeader;
    }

    public UserProfile getUserProfile() {
        return userProfile;
    }

    public static void main(String[] args) {
        ProyectoPolizasFinal.instalarFuentesModernas();
        EstiloUI.inicializarTema();
        SwingUtilities.invokeLater(() -> new Dashboard().setVisible(true));
    }
}
