package com.mycompany.proyectopolizasfinal.vistas.polizas;

import com.formdev.flatlaf.FlatClientProperties;
import com.mycompany.proyectopolizasfinal.componentes.BarraTabs;
import com.mycompany.proyectopolizasfinal.componentes.tabla.CeldaTextoSimpleRenderer;
import com.mycompany.proyectopolizasfinal.componentes.tabla.TablaOperativa;
import com.mycompany.proyectopolizasfinal.controlador.ControladorPagos;
import com.mycompany.proyectopolizasfinal.controlador.ControladorPolizas;
import com.mycompany.proyectopolizasfinal.modelo.Poliza;
import com.mycompany.proyectopolizasfinal.util.EstiloUI;
import com.mycompany.proyectopolizasfinal.vistas.pagos.ModalPago;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

/**
 * Vista de detalle de una póliza con resumen general, coberturas, pagos e historial.
 */
public class PanelDetallePoliza extends JPanel {

    private final ControladorPolizas controlador;
    private final ControladorPagos controladorPagos;
    private final Runnable onVolver;

    private final JLabel lblNumero = new JLabel("POL-0000-0000");
    private final JLabel lblEstadoBadge = new JLabel("Activa");
    private final JLabel lblSubtitulo = new JLabel("-");

    private final JLabel valCliente = crearValorDato();
    private final JLabel valBien = crearValorDato();
    private final JLabel valAseguradora = crearValorDato();
    private final JLabel valPaquete = crearValorDato();
    private final JLabel valVigencia = crearValorDato();
    private final JLabel valDetalleVigencia = crearValorDato();
    private final JLabel valPrima = crearValorDato();
    private final JLabel valRamo = crearValorDato();

    private final CardLayout tabsLayout = new CardLayout();
    private final JPanel panelTabsContenido = new JPanel(tabsLayout);

    private final DefaultTableModel modeloCoberturas = crearModelo(new String[]{"Cobertura", "Suma asegurada", "Deducible (%)", "Prima cobertura"});
    private final DefaultTableModel modeloPagos = crearModelo(new String[]{"Concepto", "Fecha límite", "Monto", "Estado"});
    private final DefaultTableModel modeloHistorial = crearModelo(new String[]{"Fecha", "Evento", "Detalle", "Responsable"});

    public PanelDetallePoliza(ControladorPolizas controlador, ControladorPagos controladorPagos, Runnable onVolver) {
        this.controlador = controlador;
        this.controladorPagos = controladorPagos;
        this.onVolver = onVolver;
        setOpaque(false);
        setLayout(new BorderLayout(0, 14));
        setBorder(BorderFactory.createEmptyBorder(20, 22, 20, 22));

        add(crearEncabezado(), BorderLayout.NORTH);
        add(crearCuerpo(), BorderLayout.CENTER);
    }

    public void mostrarPoliza(Poliza p) {
        if (p == null) {
            return;
        }
        lblNumero.setText(p.numero());
        lblEstadoBadge.setText(p.estado());
        lblEstadoBadge.setForeground(EstiloUI.colorEstado(p.estado()));
        lblSubtitulo.setText(p.cliente() + " · " + p.vehiculo());

        valCliente.setText(p.cliente());
        valBien.setText(p.vehiculo());
        valAseguradora.setText(p.aseguradora());
        valPaquete.setText(p.tipo());
        valVigencia.setText(p.vigencia());
        valDetalleVigencia.setText(p.detalleVigencia());
        valPrima.setText(p.prima());
        valRamo.setText(p.esRenovacion() ? "Renovación" : "Emisión nueva");

        poblarModelo(modeloCoberturas, controlador.coberturasDe(p));
        poblarModelo(modeloPagos, controlador.pagosDe(p));
        poblarModelo(modeloHistorial, controlador.historialDe(p));
    }

    private static void poblarModelo(DefaultTableModel modelo, List<Object[]> filas) {
        modelo.setRowCount(0);
        for (Object[] fila : filas) {
            modelo.addRow(fila);
        }
    }

    private JPanel crearEncabezado() {
        JPanel header = new JPanel(new BorderLayout(12, 8));
        header.setOpaque(false);

        JButton btnVolver = new JButton("← Volver a Pólizas");
        btnVolver.setFocusPainted(false);
        btnVolver.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnVolver.putClientProperty(FlatClientProperties.STYLE, "margin: 5,12,5,12; font: -1;");
        btnVolver.addActionListener(_ -> {
            if (onVolver != null) {
                onVolver.run();
            }
        });

        JPanel filaVolver = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        filaVolver.setOpaque(false);
        filaVolver.add(btnVolver);

        JPanel filaTitulo = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filaTitulo.setOpaque(false);
        lblNumero.putClientProperty(FlatClientProperties.STYLE, "font: bold +7;");

        lblEstadoBadge.putClientProperty(FlatClientProperties.STYLE, "font: bold -2;");
        lblEstadoBadge.setBorder(BorderFactory.createEmptyBorder(3, 10, 3, 10));
        JPanel badgeEstado = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(EstiloUI.colorOrDefault("Button.background", new Color(0x27272A)));
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 999f, 999f));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        badgeEstado.setOpaque(false);
        badgeEstado.add(lblEstadoBadge, BorderLayout.CENTER);

        filaTitulo.add(lblNumero);
        filaTitulo.add(badgeEstado);

        lblSubtitulo.putClientProperty(FlatClientProperties.STYLE, "foreground: $Label.disabledForeground; font: -1;");

        JPanel izquierda = new JPanel();
        izquierda.setLayout(new BoxLayout(izquierda, BoxLayout.Y_AXIS));
        izquierda.setOpaque(false);
        filaVolver.setAlignmentX(LEFT_ALIGNMENT);
        filaTitulo.setAlignmentX(LEFT_ALIGNMENT);
        lblSubtitulo.setAlignmentX(LEFT_ALIGNMENT);
        izquierda.add(filaVolver);
        izquierda.add(Box.createVerticalStrut(8));
        izquierda.add(filaTitulo);
        izquierda.add(Box.createVerticalStrut(2));
        izquierda.add(lblSubtitulo);

        JButton btnPago = EstiloUI.crearBotonSecundario("Registrar pago", "credit-card.svg");
        btnPago.addActionListener(_ -> new ModalPago(
                SwingUtilities.getWindowAncestor(this),
                lblNumero.getText() + " · " + valCliente.getText(),
                valPrima.getText(),
                pago -> {
                    if (controladorPagos != null) {
                        controladorPagos.guardar(pago);
                    }
                }).setVisible(true));

        JButton btnRenovar = EstiloUI.crearBotonPrimario("Renovar", "shield-check.svg");
        btnRenovar.addActionListener(_ -> new PolizasWizzard(
                SwingUtilities.getWindowAncestor(this),
                controlador::guardar).setVisible(true));

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        acciones.setOpaque(false);
        acciones.add(btnPago);
        acciones.add(btnRenovar);

        header.add(izquierda, BorderLayout.WEST);
        header.add(acciones, BorderLayout.EAST);
        return header;
    }

    private JPanel crearCuerpo() {
        JPanel cuerpo = new JPanel(new BorderLayout(0, 14));
        cuerpo.setOpaque(false);

        JPanel resumenCard = EstiloUI.crearTarjeta(new GridLayout(2, 4, 16, 12), 16, 18);
        resumenCard.add(crearBloqueDato("Contratante", valCliente));
        resumenCard.add(crearBloqueDato("Vehículo", valBien));
        resumenCard.add(crearBloqueDato("Aseguradora", valAseguradora));
        resumenCard.add(crearBloqueDato("Tipo de póliza", valPaquete));
        resumenCard.add(crearBloqueDato("Vencimiento / Estado", valVigencia));
        resumenCard.add(crearBloqueDato("Periodicidad / Detalle", valDetalleVigencia));
        resumenCard.add(crearBloqueDato("Prima total", valPrima));
        resumenCard.add(crearBloqueDato("Origen", valRamo));

        JPanel seccionInferior = new JPanel(new BorderLayout(0, 10));
        seccionInferior.setOpaque(false);

        BarraTabs barraTabs = new BarraTabs();
        barraTabs.agregarTab("Coberturas", true, () -> tabsLayout.show(panelTabsContenido, "coberturas"));
        barraTabs.agregarTab("Pagos y cuotas", false, () -> tabsLayout.show(panelTabsContenido, "pagos"));
        barraTabs.agregarTab("Historial", false, () -> tabsLayout.show(panelTabsContenido, "historial"));

        panelTabsContenido.setOpaque(false);
        panelTabsContenido.add(envolverTabla(modeloCoberturas), "coberturas");
        panelTabsContenido.add(envolverTabla(modeloPagos), "pagos");
        panelTabsContenido.add(envolverTabla(modeloHistorial), "historial");

        JPanel cardTabla = EstiloUI.crearTarjeta(new BorderLayout(), 6, 10);
        cardTabla.add(panelTabsContenido, BorderLayout.CENTER);

        seccionInferior.add(barraTabs, BorderLayout.NORTH);
        seccionInferior.add(cardTabla, BorderLayout.CENTER);

        cuerpo.add(resumenCard, BorderLayout.NORTH);
        cuerpo.add(seccionInferior, BorderLayout.CENTER);
        return cuerpo;
    }

    private JScrollPane envolverTabla(DefaultTableModel modelo) {
        TablaOperativa tabla = new TablaOperativa(modelo, 44, true);
        CeldaTextoSimpleRenderer renderer = new CeldaTextoSimpleRenderer(SwingConstants.LEFT, false, false);
        CeldaTextoSimpleRenderer rendererEstado = new CeldaTextoSimpleRenderer(SwingConstants.LEFT, true, true);
        for (int i = 0; i < modelo.getColumnCount(); i++) {
            tabla.getColumnModel().getColumn(i).setCellRenderer(
                    i == modelo.getColumnCount() - 1 ? rendererEstado : renderer);
        }

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        return scroll;
    }

    private static JPanel crearBloqueDato(String etiqueta, JLabel valor) {
        JPanel bloque = new JPanel();
        bloque.setLayout(new BoxLayout(bloque, BoxLayout.Y_AXIS));
        bloque.setOpaque(false);
        JLabel lbl = new JLabel(etiqueta);
        lbl.putClientProperty(FlatClientProperties.STYLE, "foreground: $Label.disabledForeground; font: -2;");
        bloque.add(lbl);
        bloque.add(Box.createVerticalStrut(3));
        bloque.add(valor);
        return bloque;
    }

    private static JLabel crearValorDato() {
        JLabel lbl = new JLabel("-");
        lbl.putClientProperty(FlatClientProperties.STYLE, "font: bold -1;");
        return lbl;
    }

    private static DefaultTableModel crearModelo(String[] cols) {
        return new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }
}
