package com.mycompany.proyectopolizasfinal.vistas.polizas;

import com.formdev.flatlaf.FlatClientProperties;
import com.mycompany.proyectopolizasfinal.componentes.JDateInput;
import com.mycompany.proyectopolizasfinal.modelo.Poliza;
import com.mycompany.proyectopolizasfinal.util.EstiloUI;
import com.mycompany.proyectopolizasfinal.util.Formatos;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;

/**
 * Modal flotante tipo wizard de 4 pasos para registrar una nueva póliza.
 */
public class PolizasWizzard extends JDialog {

    private static final String[] NOMBRES_PASOS = {
        "Cliente y bien",
        "Cobertura",
        "Vigencia y pago",
        "Confirmar"
    };

    private final Consumer<Poliza> onGuardar;
    private int pasoActual = 0;

    private final CardLayout pasosLayout = new CardLayout();
    private final JPanel panelPasos = new JPanel(pasosLayout);

    private final JLabel[] lblNumPasos = new JLabel[4];
    private final JLabel[] lblTitPasos = new JLabel[4];
    private final JLabel lblError = new JLabel(" ");

    private final JButton btnCancelar = EstiloUI.crearBotonSecundario("Cancelar", null);
    private final JButton btnAnterior = EstiloUI.crearBotonSecundario("Anterior", null);
    private final JButton btnSiguiente = EstiloUI.crearBotonPrimario("Siguiente", null);

    // Paso 1
    private final JTextField txtNumero = crearCampo("Ej. POL-2026-0145");
    private final JTextField txtCliente = crearCampo("Nombre completo del contratante");
    private final JComboBox<String> cmbTipoPoliza = crearCombo(new String[]{"Individual", "Flotilla", "Carga", "Plataforma"});
    private final JTextField txtVehiculo = crearCampo("Ej. Nissan Versa · ABC-123");

    // Paso 2
    private final JComboBox<String> cmbAseguradora = crearCombo(new String[]{"GNP", "Qualitas", "AXA", "Mapfre"});
    private final JComboBox<String> cmbPaquete = crearCombo(new String[]{"Amplia", "Limitada", "RC Básica"});
    private final JCheckBox chkDanos = new JCheckBox("Daños materiales", true);
    private final JCheckBox chkRobo = new JCheckBox("Robo total", true);
    private final JCheckBox chkRc = new JCheckBox("Responsabilidad civil", true);
    private final JCheckBox chkGastos = new JCheckBox("Gastos médicos ocupantes", true);

    // Paso 3
    private final JComboBox<String> cmbPeriodicidad = crearCombo(new String[]{"Contado", "Semestral", "Trimestral", "Mensual"});
    private final JDateInput dateInicio = new JDateInput(LocalDate.now(), true);
    private final JDateInput dateVigencia = new JDateInput(LocalDate.now().plusYears(1), true);
    private final JTextField txtPrima = crearCampo("Ej. 12450");
    private final JComboBox<String> cmbEstado = crearCombo(new String[]{"Activa", "Por Vencer", "Vencida"});

    // Paso 4 (Resumen)
    private final JLabel valResumenNumero = crearValorResumen();
    private final JLabel valResumenCliente = crearValorResumen();
    private final JLabel valResumenBien = crearValorResumen();
    private final JLabel valResumenAseguradora = crearValorResumen();
    private final JLabel valResumenCoberturas = crearValorResumen();
    private final JLabel valResumenVigencia = crearValorResumen();
    private final JLabel valResumenPrima = crearValorResumen();
    private final JLabel valResumenEstado = crearValorResumen();

    public PolizasWizzard() {
        this(null, null);
    }

    public PolizasWizzard(Window owner, Consumer<Poliza> onGuardar) {
        super(owner, "Nueva Póliza", ModalityType.APPLICATION_MODAL);
        this.onGuardar = onGuardar;

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
        root.setPreferredSize(new Dimension(620, 535 - (38 - top)));

        root.add(crearEncabezado(), BorderLayout.NORTH);
        root.add(crearContenedorPasos(), BorderLayout.CENTER);
        root.add(crearPie(), BorderLayout.SOUTH);

        setContentPane(root);
        pack();
        setLocationRelativeTo(owner);

        cmbPeriodicidad.addActionListener(_ -> actualizarVigenciaPorPeriodicidad());
        cmbPaquete.addActionListener(_ -> sincronizarCoberturasPorPaquete());
        btnCancelar.addActionListener(_ -> dispose());
        btnAnterior.addActionListener(_ -> irAPaso(pasoActual - 1));
        btnSiguiente.addActionListener(_ -> avanzarOGuardar());

        actualizarEstadoWizard();
    }

    private JPanel crearEncabezado() {
        JPanel norte = new JPanel();
        norte.setLayout(new BoxLayout(norte, BoxLayout.Y_AXIS));
        norte.setOpaque(false);

        JPanel tituloRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        tituloRow.setOpaque(false);
        tituloRow.setAlignmentX(LEFT_ALIGNMENT);

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
        JLabel ico = new JLabel(EstiloUI.cargarIcono("shield-check.svg", 17, "Button.default.foreground"));
        ico.setHorizontalAlignment(SwingConstants.CENTER);
        badge.add(ico, BorderLayout.CENTER);

        JPanel textos = new JPanel();
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        textos.setOpaque(false);
        JLabel lblTitulo = new JLabel("Registrar nueva póliza");
        lblTitulo.putClientProperty(FlatClientProperties.STYLE, "font: bold +5;");
        JLabel lblSub = new JLabel("Completa los 4 pasos para dar de alta la póliza y sus coberturas");
        lblSub.putClientProperty(FlatClientProperties.STYLE, "foreground: $Label.disabledForeground; font: -1;");
        textos.add(lblTitulo);
        textos.add(Box.createVerticalStrut(2));
        textos.add(lblSub);

        tituloRow.add(badge);
        tituloRow.add(textos);

        JPanel stepper = new JPanel(new GridLayout(1, 4, 8, 0));
        stepper.setOpaque(false);
        stepper.setAlignmentX(LEFT_ALIGNMENT);
        stepper.setBorder(BorderFactory.createEmptyBorder(14, 0, 2, 0));

        for (int i = 0; i < NOMBRES_PASOS.length; i++) {
            JPanel item = new JPanel(new FlowLayout(FlowLayout.LEFT, 7, 0));
            item.setOpaque(false);

            final int idx = i;
            JLabel num = new JLabel(String.valueOf(i + 1), SwingConstants.CENTER) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    boolean activo = idx == pasoActual;
                    boolean hecho = idx < pasoActual;
                    Color fill = (activo || hecho)
                            ? EstiloUI.colorOrDefault("Button.default.background", new Color(0x193CB8))
                            : EstiloUI.colorOrDefault("Button.background", new Color(0x27272A));
                    g2.setColor(fill);
                    g2.fillOval(0, 0, getWidth(), getHeight());
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            num.setPreferredSize(new Dimension(22, 22));
            num.putClientProperty(FlatClientProperties.STYLE, "font: bold -2;");

            JLabel tit = new JLabel(NOMBRES_PASOS[i]);
            tit.putClientProperty(FlatClientProperties.STYLE, "font: -1;");

            lblNumPasos[i] = num;
            lblTitPasos[i] = tit;
            item.add(num);
            item.add(tit);
            stepper.add(item);
        }

        norte.add(tituloRow);
        norte.add(stepper);
        return norte;
    }

    private JPanel crearContenedorPasos() {
        JPanel tarjeta = EstiloUI.crearTarjeta(new BorderLayout(0, 10), 18, 20);

        panelPasos.setOpaque(false);
        panelPasos.add(crearPaso1(), "0");
        panelPasos.add(crearPaso2(), "1");
        panelPasos.add(crearPaso3(), "2");
        panelPasos.add(crearPaso4(), "3");

        lblError.setForeground(EstiloUI.colorDestructivo());
        lblError.putClientProperty(FlatClientProperties.STYLE, "font: -1;");

        tarjeta.add(panelPasos, BorderLayout.CENTER);
        tarjeta.add(lblError, BorderLayout.SOUTH);
        return tarjeta;
    }

    private JPanel crearPaso1() {
        JPanel p = crearGridFormulario();
        GridBagConstraints gbc = crearGbc();
        agregarFila(p, gbc, 0, "Número de póliza *", txtNumero, 0.36, 10);
        agregarFila(p, gbc, 1, "Cliente / Contratante *", txtCliente, 0.36, 10);
        agregarFila(p, gbc, 2, "Tipo de póliza", cmbTipoPoliza, 0.36, 10);
        agregarFila(p, gbc, 3, "Vehículo asegurado *", txtVehiculo, 0.36, 10);
        agregarRellenoVertical(p, gbc, 4);
        return p;
    }

    private JPanel crearPaso2() {
        JPanel p = crearGridFormulario();
        GridBagConstraints gbc = crearGbc();
        agregarFila(p, gbc, 0, "Aseguradora", cmbAseguradora, 0.36, 10);
        agregarFila(p, gbc, 1, "Paquete de cobertura", cmbPaquete, 0.36, 10);

        gbc.gridy = 2;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(10, 0, 6, 0);
        JLabel lblCob = new JLabel("Coberturas incluidas en la póliza");
        lblCob.putClientProperty(FlatClientProperties.STYLE, "font: bold -1;");
        p.add(lblCob, gbc);

        JPanel gridCob = new JPanel(new GridLayout(2, 2, 10, 8));
        gridCob.setOpaque(false);
        for (JCheckBox chk : new JCheckBox[]{chkDanos, chkRobo, chkRc, chkGastos}) {
            chk.setOpaque(false);
            chk.setFocusPainted(false);
            chk.putClientProperty(FlatClientProperties.STYLE, "font: -1;");
            gridCob.add(chk);
        }

        gbc.gridy = 3;
        gbc.insets = new Insets(0, 0, 0, 0);
        p.add(gridCob, gbc);
        agregarRellenoVertical(p, gbc, 4);
        return p;
    }

    private JPanel crearPaso3() {
        JPanel p = crearGridFormulario();
        GridBagConstraints gbc = crearGbc();
        agregarFila(p, gbc, 0, "Forma de pago / Periodicidad", cmbPeriodicidad, 0.36, 10);
        agregarFila(p, gbc, 1, "Fecha de inicio *", dateInicio, 0.36, 10);
        agregarFila(p, gbc, 2, "Fecha de vencimiento *", dateVigencia, 0.36, 10);
        agregarFila(p, gbc, 3, "Prima total (MXN) *", txtPrima, 0.36, 10);
        agregarFila(p, gbc, 4, "Estado inicial", cmbEstado, 0.36, 10);
        agregarRellenoVertical(p, gbc, 5);
        return p;
    }

    private JPanel crearPaso4() {
        JPanel p = crearGridFormulario();
        GridBagConstraints gbc = crearGbc();

        JLabel lblIntro = new JLabel("Revisa los datos antes de registrar la póliza:");
        lblIntro.putClientProperty(FlatClientProperties.STYLE, "foreground: $Label.disabledForeground; font: -1;");
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 0, 10, 0);
        p.add(lblIntro, gbc);

        gbc.gridwidth = 1;
        agregarFila(p, gbc, 1, "Póliza", valResumenNumero, 0.34, 7);
        agregarFila(p, gbc, 2, "Cliente", valResumenCliente, 0.34, 7);
        agregarFila(p, gbc, 3, "Bien asegurado", valResumenBien, 0.34, 7);
        agregarFila(p, gbc, 4, "Aseguradora / Tipo", valResumenAseguradora, 0.34, 7);
        agregarFila(p, gbc, 5, "Coberturas", valResumenCoberturas, 0.34, 7);
        agregarFila(p, gbc, 6, "Vigencia y pago", valResumenVigencia, 0.34, 7);
        agregarFila(p, gbc, 7, "Prima total", valResumenPrima, 0.34, 7);
        agregarFila(p, gbc, 8, "Estado", valResumenEstado, 0.34, 7);
        agregarRellenoVertical(p, gbc, 9);
        return p;
    }

    private JPanel crearPie() {
        JPanel pie = new JPanel(new BorderLayout());
        pie.setOpaque(false);

        JPanel derecha = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        derecha.setOpaque(false);
        derecha.add(btnAnterior);
        derecha.add(btnSiguiente);

        pie.add(btnCancelar, BorderLayout.WEST);
        pie.add(derecha, BorderLayout.EAST);
        return pie;
    }

    private void sincronizarCoberturasPorPaquete() {
        String paquete = (String) cmbPaquete.getSelectedItem();
        chkDanos.setSelected("Amplia".equals(paquete));
        chkRobo.setSelected("Amplia".equals(paquete) || "Limitada".equals(paquete));
        chkRc.setSelected(true);
        chkGastos.setSelected("Amplia".equals(paquete) || "Limitada".equals(paquete));
    }

    private void avanzarOGuardar() {
        if (!validarPasoActual()) {
            return;
        }
        if (pasoActual < 3) {
            if (pasoActual == 2) {
                poblarResumen();
            }
            irAPaso(pasoActual + 1);
        } else {
            if (onGuardar != null) {
                onGuardar.accept(construirResultado());
            }
            dispose();
        }
    }

    private void irAPaso(int paso) {
        if (paso < 0 || paso > 3) {
            return;
        }
        pasoActual = paso;
        limpiarError();
        pasosLayout.show(panelPasos, String.valueOf(pasoActual));
        actualizarEstadoWizard();
    }

    private void actualizarEstadoWizard() {
        Color fgActivo = EstiloUI.colorOrDefault("Label.foreground", Color.WHITE);
        Color fgInactivo = EstiloUI.colorOrDefault("Label.disabledForeground", Color.GRAY);
        Color fgPrimary = EstiloUI.colorOrDefault("Button.default.foreground", Color.WHITE);

        for (int i = 0; i < NOMBRES_PASOS.length; i++) {
            boolean activo = i == pasoActual;
            boolean hecho = i < pasoActual;
            lblNumPasos[i].setText(hecho ? "✓" : String.valueOf(i + 1));
            lblNumPasos[i].setForeground((activo || hecho) ? fgPrimary : fgInactivo);
            lblNumPasos[i].repaint();
            lblTitPasos[i].setForeground(activo ? fgActivo : fgInactivo);
            lblTitPasos[i].putClientProperty(FlatClientProperties.STYLE, activo ? "font: bold -1;" : "font: -1;");
        }

        btnAnterior.setVisible(pasoActual > 0);
        btnSiguiente.setText(pasoActual == 3 ? "Registrar Póliza" : "Siguiente");
    }

    private boolean validarPasoActual() {
        limpiarError();
        if (pasoActual == 0) {
            String num = txtNumero.getText().trim();
            if (num.isEmpty() || num.length() > 30) {
                return mostrarError("Ingresa el número de póliza (1 a 30 caracteres).", txtNumero);
            }
            if (estaVacio(txtCliente)) {
                return mostrarError("Ingresa el nombre del cliente o contratante.", txtCliente);
            }
            if (estaVacio(txtVehiculo)) {
                return mostrarError("Describe el vehículo o bien asegurado.", txtVehiculo);
            }
        } else if (pasoActual == 1) {
            if (!chkDanos.isSelected() && !chkRobo.isSelected() && !chkRc.isSelected() && !chkGastos.isSelected()) {
                return mostrarError("Selecciona al menos una cobertura para la póliza.", null);
            }
        } else if (pasoActual == 2) {
            if (!dateInicio.isValidDate() || dateInicio.getDate() == null) {
                dateInicio.setError(true);
                dateInicio.requestFocusInWindow();
                return mostrarError("Selecciona una fecha de inicio válida.", null);
            }
            if (!dateVigencia.isValidDate() || dateVigencia.getDate() == null) {
                dateVigencia.setError(true);
                dateVigencia.requestFocusInWindow();
                return mostrarError("Selecciona una fecha de vencimiento válida.", null);
            }
            if (!dateVigencia.getDate().isAfter(dateInicio.getDate())) {
                dateVigencia.setError(true);
                return mostrarError("La fecha de vencimiento debe ser posterior a la de inicio.", null);
            }
            if (Formatos.parsearMontoCaptura(txtPrima.getText()) <= 0) {
                return mostrarError("Ingresa una prima válida mayor a 0.", txtPrima);
            }
        }
        return true;
    }

    private void limpiarError() {
        lblError.setText(" ");
        dateInicio.setError(false);
        dateVigencia.setError(false);
        for (JTextField f : new JTextField[]{txtNumero, txtCliente, txtVehiculo, txtPrima}) {
            f.putClientProperty(FlatClientProperties.OUTLINE, null);
        }
    }

    private boolean mostrarError(String mensaje, JTextField campo) {
        lblError.setText(mensaje);
        if (campo != null) {
            campo.putClientProperty(FlatClientProperties.OUTLINE, "error");
            campo.requestFocusInWindow();
        }
        return false;
    }

    private void poblarResumen() {
        Poliza d = construirResultado();
        valResumenNumero.setText(d.numero());
        valResumenCliente.setText(d.cliente());
        valResumenBien.setText(d.vehiculo());
        valResumenAseguradora.setText(d.aseguradora() + " · " + d.tipo());
        valResumenCoberturas.setText(resumenCoberturas());
        valResumenVigencia.setText(d.vigencia() + " · " + d.detalleVigencia());
        valResumenPrima.setText(d.prima());
        valResumenEstado.setText(d.estado());
    }

    private Poliza construirResultado() {
        double monto = Formatos.parsearMontoCaptura(txtPrima.getText());
        return new Poliza(
                0,
                txtNumero.getText().trim(),
                txtCliente.getText().trim(),
                txtVehiculo.getText().trim(),
                (String) cmbAseguradora.getSelectedItem(),
                (String) cmbTipoPoliza.getSelectedItem(),
                Formatos.formatearFechaCorta(dateVigencia.getDate()),
                (String) cmbPeriodicidad.getSelectedItem(),
                Formatos.formatearMoneda(monto),
                (String) cmbEstado.getSelectedItem(),
                true,
                false,
                false
        );
    }

    private void actualizarVigenciaPorPeriodicidad() {
        LocalDate base = dateInicio.getDate() != null ? dateInicio.getDate() : LocalDate.now();
        String p = (String) cmbPeriodicidad.getSelectedItem();
        int meses = switch (p) {
            case "Semestral" -> 6;
            case "Trimestral" -> 3;
            case "Mensual" -> 1;
            default -> 12;
        };
        dateVigencia.setDate(base.plusMonths(meses));
    }

    private String resumenCoberturas() {
        List<String> lista = new ArrayList<>();
        if (chkDanos.isSelected()) lista.add("Daños");
        if (chkRobo.isSelected()) lista.add("Robo");
        if (chkRc.isSelected()) lista.add("RC");
        if (chkGastos.isSelected()) lista.add("Gastos Méd.");
        return String.join(", ", lista);
    }

    private static boolean estaVacio(JTextField field) {
        return field.getText() == null || field.getText().trim().isEmpty();
    }

    private static JTextField crearCampo(String placeholder) {
        JTextField tf = new JTextField();
        tf.setPreferredSize(new Dimension(0, 34));
        tf.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, placeholder);
        tf.putClientProperty(FlatClientProperties.STYLE, "arc: 10; margin: 4,10,4,10;");
        return tf;
    }

    private static JComboBox<String> crearCombo(String[] items) {
        JComboBox<String> cmb = new JComboBox<>(items);
        cmb.setPreferredSize(new Dimension(0, 34));
        cmb.putClientProperty(FlatClientProperties.STYLE, "arc: 10;");
        return cmb;
    }

    private static JLabel crearValorResumen() {
        JLabel lbl = new JLabel("-");
        lbl.putClientProperty(FlatClientProperties.STYLE, "font: bold -1;");
        return lbl;
    }

    private static JPanel crearGridFormulario() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        return p;
    }

    private static GridBagConstraints crearGbc() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        return gbc;
    }

    private static void agregarFila(JPanel p, GridBagConstraints gbc, int fila, String etiqueta,
            JComponent valor, double pesoLabel, int bottomGap) {
        gbc.gridx = 0;
        gbc.gridy = fila;
        gbc.weightx = pesoLabel;
        gbc.insets = new Insets(0, 0, bottomGap, 12);
        JLabel lbl = new JLabel(etiqueta);
        lbl.putClientProperty(FlatClientProperties.STYLE, "foreground: $Label.disabledForeground; font: -1;");
        p.add(lbl, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0 - pesoLabel;
        gbc.insets = new Insets(0, 0, bottomGap, 0);
        p.add(valor, gbc);
    }

    private static void agregarRellenoVertical(JPanel p, GridBagConstraints gbc, int fila) {
        gbc.gridx = 0;
        gbc.gridy = fila;
        gbc.gridwidth = 2;
        gbc.weighty = 1.0;
        p.add(Box.createVerticalGlue(), gbc);
    }
}
