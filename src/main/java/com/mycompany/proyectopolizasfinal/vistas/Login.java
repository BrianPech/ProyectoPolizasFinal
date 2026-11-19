package com.mycompany.proyectopolizasfinal.vistas;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.mycompany.proyectopolizasfinal.ProyectoPolizasFinal;
import com.mycompany.proyectopolizasfinal.controlador.ControladorSesion;
import com.mycompany.proyectopolizasfinal.repositorio.Repositorios;
import com.mycompany.proyectopolizasfinal.util.EstiloUI;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import java.util.Optional;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Login extends JFrame {

    private final Repositorios repositorios;
    private final ControladorSesion controladorSesion;

    private JTextField txtEmail;
    private JPasswordField txtPassword;
    private JCheckBox chkRecordar;
    private JLabel lblError;
    private JButton btnLogin;

    public Login() {
        this(Repositorios.defecto());
    }

    public Login(Repositorios repositorios) {
        super("Iniciar Sesión");
        this.repositorios = repositorios;
        this.controladorSesion = new ControladorSesion(repositorios.usuarios());

        setSize(420, EstiloUI.margenSuperiorVentana(560, 545));
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        EstiloUI.configurarBarraTituloVentana(this);

        Color colorFondo = UIManager.getColor("Panel.background");
        Color colorCard = UIManager.getColor("Card.background");

        getContentPane().setBackground(colorFondo);
        getRootPane().setBackground(colorFondo);
        setBackground(colorFondo);

        JPanel contenedor = new JPanel(new BorderLayout());
        contenedor.setOpaque(false);
        contenedor.setBorder(BorderFactory.createEmptyBorder(
                EstiloUI.margenSuperiorVentana(40, 24), 25, 25, 25));

        int cardArc = UIManager.getInt("Component.arc");
        if (cardArc <= 0) {
            cardArc = 20;
        }

        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(colorCard);
        card.putClientProperty(FlatClientProperties.STYLE, "arc: " + cardArc + ";");
        card.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.weightx = 1.0;

        JLabel badgeMarca = new JLabel(
                EstiloUI.cargarIcono("shield-check.svg", 22, "Button.default.foreground"),
                SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = UIManager.getColor("Button.default.background");
                g2.setColor(bg != null ? bg : new Color(0x2B7FFF));
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 14, 14));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        badgeMarca.setPreferredSize(new Dimension(44, 44));
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(0, 0, 12, 0);
        card.add(badgeMarca, gbc);

        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.LINE_START;

        JLabel lblTitulo = new JLabel("Bienvenido", SwingConstants.CENTER);
        lblTitulo.putClientProperty(FlatClientProperties.STYLE, "font: bold +8");
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 4, 0);
        card.add(lblTitulo, gbc);

        JLabel lblSubtitulo = new JLabel("Ingresa tus credenciales para continuar", SwingConstants.CENTER);
        lblSubtitulo.putClientProperty(FlatClientProperties.STYLE, "foreground: $Label.disabledForeground; font: -1");
        gbc.gridy = 2;
        gbc.insets = new Insets(0, 0, 22, 0);
        card.add(lblSubtitulo, gbc);

        JLabel lblEmail = new JLabel("Usuario o Correo");
        lblEmail.putClientProperty(FlatClientProperties.STYLE, "font: bold;");
        gbc.gridy = 3;
        gbc.insets = new Insets(0, 0, 6, 0);
        card.add(lblEmail, gbc);

        txtEmail = new JTextField();
        txtEmail.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Usuario o correo");
        txtEmail.putClientProperty(FlatClientProperties.TEXT_FIELD_LEADING_ICON,
                EstiloUI.cargarIcono("mail.svg", 16, "Label.disabledForeground"));
        txtEmail.putClientProperty(FlatClientProperties.STYLE, "arc: 10; margin: 4,8,4,8;");
        txtEmail.setPreferredSize(new Dimension(0, 38));
        gbc.gridy = 4;
        gbc.insets = new Insets(0, 0, 16, 0);
        card.add(txtEmail, gbc);

        JLabel lblPassword = new JLabel("Contraseña");
        lblPassword.putClientProperty(FlatClientProperties.STYLE, "font: bold;");
        gbc.gridy = 5;
        gbc.insets = new Insets(0, 0, 6, 0);
        card.add(lblPassword, gbc);

        txtPassword = new JPasswordField();
        txtPassword.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "••••••••");
        txtPassword.putClientProperty(FlatClientProperties.TEXT_FIELD_LEADING_ICON,
                EstiloUI.cargarIcono("lock.svg", 16, "Label.disabledForeground"));
        txtPassword.putClientProperty(FlatClientProperties.TEXT_FIELD_TRAILING_COMPONENT,
                crearBotonVerPassword(txtPassword));
        txtPassword.putClientProperty(FlatClientProperties.STYLE, "arc: 10; margin: 4,8,4,8;");
        txtPassword.setPreferredSize(new Dimension(0, 38));
        gbc.gridy = 6;
        gbc.insets = new Insets(0, 0, 12, 0);
        card.add(txtPassword, gbc);

        chkRecordar = new JCheckBox("Recordar sesión");
        chkRecordar.setOpaque(false);
        chkRecordar.setFocusPainted(false);
        gbc.gridy = 7;
        gbc.insets = new Insets(0, 0, 8, 0);
        card.add(chkRecordar, gbc);

        lblError = new JLabel(" ");
        lblError.setForeground(EstiloUI.colorDestructivo());
        lblError.putClientProperty(FlatClientProperties.STYLE, "font: -1;");
        gbc.gridy = 8;
        gbc.insets = new Insets(0, 0, 10, 0);
        card.add(lblError, gbc);

        btnLogin = new JButton("Iniciar Sesión");
        btnLogin.setIcon(EstiloUI.cargarIcono("log-in.svg", 16, "Button.default.foreground"));
        btnLogin.setIconTextGap(8);
        btnLogin.putClientProperty(FlatClientProperties.STYLE,
                "margin: 8,0,8,0; font: bold +1; background: $Button.default.background; foreground: $Button.default.foreground; borderWidth: 0;");
        btnLogin.setPreferredSize(new Dimension(0, 40));
        getRootPane().setDefaultButton(btnLogin);
        btnLogin.addActionListener(_ -> intentarLogin());
        gbc.gridy = 9;
        gbc.insets = new Insets(0, 0, 0, 0);
        card.add(btnLogin, gbc);

        contenedor.add(card, BorderLayout.CENTER);
        add(contenedor);

        setLocationRelativeTo(null);
    }

    private void intentarLogin() {
        lblError.setText(" ");
        txtEmail.putClientProperty(FlatClientProperties.OUTLINE, null);
        txtPassword.putClientProperty(FlatClientProperties.OUTLINE, null);

        char[] pass = txtPassword.getPassword();
        Optional<String> error = controladorSesion.iniciarSesion(txtEmail.getText(), pass);
        java.util.Arrays.fill(pass, '\0');

        if (error.isPresent()) {
            lblError.setText(error.get());
            return;
        }
        new Dashboard(repositorios, controladorSesion).setVisible(true);
        dispose();
    }

    private static JToggleButton crearBotonVerPassword(JPasswordField campo) {
        char echoPorDefecto = campo.getEchoChar() != 0 ? campo.getEchoChar() : '\u2022';
        FlatSVGIcon ojoVisible = EstiloUI.cargarIcono("eye.svg", 16, "Label.disabledForeground");
        FlatSVGIcon ojoVisibleHover = EstiloUI.cargarIcono("eye.svg", 16, "Label.foreground");
        FlatSVGIcon ojoOculto = EstiloUI.cargarIcono("eye-off.svg", 16, "Label.disabledForeground");
        FlatSVGIcon ojoOcultoHover = EstiloUI.cargarIcono("eye-off.svg", 16, "Label.foreground");

        JToggleButton btn = new JToggleButton(ojoVisible);
        btn.setSelectedIcon(ojoOculto);
        btn.setRolloverIcon(ojoVisibleHover);
        btn.setRolloverSelectedIcon(ojoOcultoHover);
        btn.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_BORDERLESS);
        btn.putClientProperty(FlatClientProperties.STYLE, "margin: 0,6,0,6;");
        btn.setFocusable(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setToolTipText("Mostrar contraseña");
        btn.addActionListener(_ -> {
            boolean visible = btn.isSelected();
            campo.setEchoChar(visible ? (char) 0 : echoPorDefecto);
            btn.setToolTipText(visible ? "Ocultar contraseña" : "Mostrar contraseña");
        });
        return btn;
    }

    public JTextField getTxtEmail() {
        return txtEmail;
    }

    public JPasswordField getTxtPassword() {
        return txtPassword;
    }

    public JCheckBox getChkRecordar() {
        return chkRecordar;
    }

    public JButton getBtnLogin() {
        return btnLogin;
    }

    public static void main(String[] args) {
        ProyectoPolizasFinal.instalarFuentesModernas();
        EstiloUI.inicializarTema();
        SwingUtilities.invokeLater(() -> new Login().setVisible(true));
    }
}
