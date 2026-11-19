package com.mycompany.proyectopolizasfinal.componentes;

import com.formdev.flatlaf.FlatClientProperties;
import com.mycompany.proyectopolizasfinal.util.EstiloUI;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.event.KeyEvent;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;

/**
 * Base reutilizable para modales flotantes CRUD con estilo macOS + shadcn/ui.
 */
public abstract class ModalFormularioBase extends JDialog {

    protected final JPanel panelCampos = new JPanel(new GridBagLayout());
    private final JLabel lblError = new JLabel(" ");
    protected final JButton btnCancelar = new JButton("Cancelar");
    protected final JButton btnGuardar = new JButton("Guardar");

    protected ModalFormularioBase(Window owner, String tituloVentana, String tituloHeader,
            String subtitulo, String icono, int ancho, int alto) {
        super(owner, tituloVentana, ModalityType.APPLICATION_MODAL);

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);
        EstiloUI.configurarBarraTituloVentana(this);

        Color colorFondo = EstiloUI.colorOrDefault("Panel.background", new Color(0x0A0A0A));
        getContentPane().setBackground(colorFondo);
        getRootPane().setBackground(colorFondo);
        setBackground(colorFondo);

        getRootPane().registerKeyboardAction(
                _ -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        int top = EstiloUI.margenSuperiorVentana(38, 18);
        JPanel root = new JPanel(new BorderLayout(0, 14));
        root.setOpaque(false);
        root.setBorder(BorderFactory.createEmptyBorder(top, 22, 18, 22));
        root.setPreferredSize(new Dimension(ancho, alto - (38 - top)));

        root.add(crearEncabezado(tituloHeader, subtitulo, icono), BorderLayout.NORTH);
        root.add(crearCuerpo(), BorderLayout.CENTER);
        root.add(crearPie(), BorderLayout.SOUTH);

        setContentPane(root);
    }

    protected final void finalizarLayout(Window owner) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 99;
        gbc.gridwidth = 2;
        gbc.weighty = 1.0;
        panelCampos.add(Box.createVerticalGlue(), gbc);

        pack();
        setLocationRelativeTo(owner);
    }

    private JPanel crearEncabezado(String titulo, String subtitulo, String icono) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        row.setOpaque(false);

        JPanel badge = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(EstiloUI.colorOrDefault("Button.default.background", new Color(0x193CB8)));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        badge.setOpaque(false);
        badge.setPreferredSize(new Dimension(34, 34));
        JLabel ico = new JLabel(EstiloUI.cargarIcono(icono, 17, "Button.default.foreground"));
        ico.setHorizontalAlignment(SwingConstants.CENTER);
        badge.add(ico, BorderLayout.CENTER);

        JPanel textos = new JPanel();
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        textos.setOpaque(false);
        JLabel lblTit = new JLabel(titulo);
        lblTit.putClientProperty(FlatClientProperties.STYLE, "font: bold +5;");
        JLabel lblSub = new JLabel(subtitulo);
        lblSub.putClientProperty(FlatClientProperties.STYLE, "foreground: $Label.disabledForeground; font: -1;");
        textos.add(lblTit);
        textos.add(Box.createVerticalStrut(2));
        textos.add(lblSub);

        row.add(badge);
        row.add(textos);
        return row;
    }

    private JPanel crearCuerpo() {
        JPanel tarjeta = EstiloUI.crearTarjeta(new BorderLayout(0, 10), 18, 20);
        panelCampos.setOpaque(false);

        lblError.setForeground(EstiloUI.colorDestructivo());
        lblError.putClientProperty(FlatClientProperties.STYLE, "font: -1;");

        tarjeta.add(panelCampos, BorderLayout.CENTER);
        tarjeta.add(lblError, BorderLayout.SOUTH);
        return tarjeta;
    }

    private JPanel crearPie() {
        JPanel pie = new JPanel(new BorderLayout());
        pie.setOpaque(false);

        btnCancelar.setFocusPainted(false);
        btnCancelar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnCancelar.putClientProperty(FlatClientProperties.STYLE, "margin: 7,14,7,14; font: -1;");
        btnCancelar.addActionListener(_ -> dispose());

        btnGuardar.setFocusPainted(false);
        btnGuardar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnGuardar.putClientProperty(FlatClientProperties.STYLE,
                "margin: 7,16,7,16; font: bold -1; background: $Button.default.background; foreground: $Button.default.foreground; borderWidth: 0;");
        btnGuardar.addActionListener(_ -> {
            if (validarYGuardar()) {
                dispose();
            }
        });

        pie.add(btnCancelar, BorderLayout.WEST);
        pie.add(btnGuardar, BorderLayout.EAST);
        return pie;
    }

    protected abstract boolean validarYGuardar();

    protected void agregarCampo(int fila, String etiqueta, JComponent campo) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridy = fila;

        gbc.gridx = 0;
        gbc.weightx = 0.35;
        gbc.insets = new Insets(0, 0, 10, 12);
        JLabel lbl = new JLabel(etiqueta);
        lbl.putClientProperty(FlatClientProperties.STYLE, "foreground: $Label.disabledForeground; font: -1;");
        panelCampos.add(lbl, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.65;
        gbc.insets = new Insets(0, 0, 10, 0);
        panelCampos.add(campo, gbc);
    }

    protected boolean mostrarError(String mensaje, JComponent campo) {
        lblError.setText(mensaje);
        if (campo != null) {
            campo.putClientProperty(FlatClientProperties.OUTLINE, "error");
            campo.requestFocusInWindow();
        }
        return false;
    }

    protected void limpiarErrores(JComponent... campos) {
        lblError.setText(" ");
        for (JComponent c : campos) {
            c.putClientProperty(FlatClientProperties.OUTLINE, null);
        }
    }

    protected static JTextField crearCampoTexto(String placeholder) {
        JTextField tf = new JTextField();
        tf.setPreferredSize(new Dimension(0, 34));
        tf.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, placeholder);
        tf.putClientProperty(FlatClientProperties.STYLE, "arc: 10; margin: 4,10,4,10;");
        return tf;
    }

    protected static JPasswordField crearCampoPassword(String placeholder) {
        JPasswordField pf = new JPasswordField();
        pf.setPreferredSize(new Dimension(0, 34));
        pf.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, placeholder);
        pf.putClientProperty(FlatClientProperties.STYLE, "arc: 10; margin: 4,10,4,10;");
        return pf;
    }

    protected static JComboBox<String> crearCombo(String[] items) {
        JComboBox<String> cmb = new JComboBox<>(items);
        cmb.setPreferredSize(new Dimension(0, 34));
        cmb.putClientProperty(FlatClientProperties.STYLE, "arc: 10;");
        return cmb;
    }

    protected static boolean estaVacio(JTextField tf) {
        return tf.getText() == null || tf.getText().trim().isEmpty();
    }
}
