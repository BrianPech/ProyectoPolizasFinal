package com.mycompany.proyectopolizasfinal.vistas.inicio;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.mycompany.proyectopolizasfinal.componentes.tabla.CeldaConIconoRenderer;
import com.mycompany.proyectopolizasfinal.componentes.tabla.CeldaTextoSimpleRenderer;
import com.mycompany.proyectopolizasfinal.componentes.tabla.TablaOperativa;
import com.mycompany.proyectopolizasfinal.componentes.tabla.TextoDoble;
import com.mycompany.proyectopolizasfinal.controlador.ControladorInicio;
import com.mycompany.proyectopolizasfinal.modelo.Poliza;
import com.mycompany.proyectopolizasfinal.repositorio.Repositorios;
import com.mycompany.proyectopolizasfinal.util.EstiloUI;
import com.mycompany.proyectopolizasfinal.vistas.pagos.ModalAviso;
import com.mycompany.proyectopolizasfinal.vistas.pagos.ModalPago;
import com.mycompany.proyectopolizasfinal.vistas.polizas.ModalCancelacion;
import com.mycompany.proyectopolizasfinal.vistas.polizas.PolizasWizzard;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

/**
 * Vista de resumen operativo para la sección Inicio dentro del Dashboard.
 */
public class PanelInicio extends JPanel {

    private final ControladorInicio controlador;
    private final Consumer<Poliza> onVerDetalle;

    private final JButton btnNuevaPoliza;
    private final JButton btnRegistrarPago;
    private final DefaultTableModel modeloVencimientos;
    private final TablaOperativa tablaVencimientos;
    private final List<Poliza> polizasVencimiento = new ArrayList<>();

    private final JLabel lblKpiActivasValor = crearLabelKpiValor();
    private final JLabel lblKpiActivasDetalle = crearLabelKpiDetalle();
    private final JLabel lblKpiPorVencerValor = crearLabelKpiValor();
    private final JLabel lblKpiPorVencerDetalle = crearLabelKpiDetalle();
    private final JLabel lblKpiPendValor = crearLabelKpiValor();
    private final JLabel lblKpiPendDetalle = crearLabelKpiDetalle();
    private final JLabel lblKpiCobradoValor = crearLabelKpiValor();
    private final JLabel lblKpiCobradoDetalle = crearLabelKpiDetalle();

    private final JPanel listaPendientes = new JPanel();
    private final JPopupMenu menuAccionesFila;
    private final JMenuItem itemVerDetalle;
    private final JMenuItem itemRegistrarPagoFila;
    private final JMenuItem itemEnviarAviso;
    private final JMenuItem itemCancelarPoliza;

    private final FlatSVGIcon iconoAuto = EstiloUI.cargarIcono("car.svg", 15, "Label.foreground");
    private final FlatSVGIcon iconoPoliza = EstiloUI.cargarIcono("shield-check.svg", 15, "Label.foreground");

    public PanelInicio() {
        this(new ControladorInicio(Repositorios.defecto().polizas(), Repositorios.defecto().movimientos()), null);
    }

    public PanelInicio(Consumer<Poliza> onVerDetalle) {
        this(new ControladorInicio(Repositorios.defecto().polizas(), Repositorios.defecto().movimientos()), onVerDetalle);
    }

    public PanelInicio(ControladorInicio controlador, Consumer<Poliza> onVerDetalle) {
        this.controlador = controlador;
        this.onVerDetalle = onVerDetalle;
        setOpaque(false);
        setLayout(new BorderLayout(0, 16));
        setBorder(BorderFactory.createEmptyBorder(20, 22, 20, 22));

        // 1. Encabezado superior + acciones rápidas
        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setOpaque(false);

        JPanel textosHeader = new JPanel();
        textosHeader.setLayout(new BoxLayout(textosHeader, BoxLayout.Y_AXIS));
        textosHeader.setOpaque(false);

        JLabel lblTitulo = new JLabel("Inicio");
        lblTitulo.putClientProperty(FlatClientProperties.STYLE, "font: bold +7;");
        JLabel lblSubtitulo = new JLabel("Resumen operativo de pólizas, cobranza y vencimientos");
        lblSubtitulo.putClientProperty(FlatClientProperties.STYLE, "foreground: $Label.disabledForeground; font: -1;");

        textosHeader.add(lblTitulo);
        textosHeader.add(Box.createVerticalStrut(2));
        textosHeader.add(lblSubtitulo);

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        acciones.setOpaque(false);

        btnRegistrarPago = EstiloUI.crearBotonSecundario("Registrar Pago", "credit-card.svg");
        btnRegistrarPago.addActionListener(_ -> abrirModalPago(null, null));

        btnNuevaPoliza = EstiloUI.crearBotonPrimario("Nueva Póliza", "shield-check.svg");
        btnNuevaPoliza.addActionListener(_ -> new PolizasWizzard(
                SwingUtilities.getWindowAncestor(this),
                nueva -> {
                    controlador.registrarPoliza(nueva);
                    refrescar();
                }).setVisible(true));

        acciones.add(btnRegistrarPago);
        acciones.add(btnNuevaPoliza);

        header.add(textosHeader, BorderLayout.WEST);
        header.add(acciones, BorderLayout.EAST);

        // 2. Fila de 4 tarjetas KPI
        JPanel filaKpis = new JPanel(new GridLayout(1, 4, 12, 0));
        filaKpis.setOpaque(false);
        filaKpis.add(crearTarjetaKpi("Pólizas activas", lblKpiActivasValor, lblKpiActivasDetalle, "shield-check.svg"));
        filaKpis.add(crearTarjetaKpi("Por vencer", lblKpiPorVencerValor, lblKpiPorVencerDetalle, "car.svg"));
        filaKpis.add(crearTarjetaKpi("Pagos pend.", lblKpiPendValor, lblKpiPendDetalle, "credit-card.svg"));
        filaKpis.add(crearTarjetaKpi("Cobrado (reg.)", lblKpiCobradoValor, lblKpiCobradoDetalle, "chart-column.svg"));

        JPanel bloqueSuperior = new JPanel(new BorderLayout(0, 14));
        bloqueSuperior.setOpaque(false);
        bloqueSuperior.add(header, BorderLayout.NORTH);
        bloqueSuperior.add(filaKpis, BorderLayout.CENTER);

        // 3. Cuerpo principal en 2 columnas: tabla de vencimientos (64%) + pendientes (36%)
        JPanel cuerpo = new JPanel(new GridBagLayout());
        cuerpo.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;

        modeloVencimientos = new DefaultTableModel(new String[]{"Póliza", "Vence", "Estado", ""}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tablaVencimientos = crearTablaVencimientos(modeloVencimientos);

        menuAccionesFila = new JPopupMenu();
        menuAccionesFila.putClientProperty(FlatClientProperties.STYLE, "borderInsets: 6,4,6,4;");
        itemVerDetalle = EstiloUI.crearItemMenu("Ver detalles", "eye.svg", "MenuItem.foreground");
        itemVerDetalle.addActionListener(_ -> abrirDetalleFila(tablaVencimientos.getFilaMenuActual()));
        itemRegistrarPagoFila = EstiloUI.crearItemMenu("Registrar pago", "credit-card.svg", "MenuItem.foreground");
        itemRegistrarPagoFila.addActionListener(_ -> {
            Poliza p = polizaEnFila(tablaVencimientos.getFilaMenuActual());
            if (p != null) {
                abrirModalPago(p.numero() + " · " + p.cliente(), p.prima());
            }
        });
        itemEnviarAviso = EstiloUI.crearItemMenu("Enviar aviso", "send.svg", "MenuItem.foreground");
        itemEnviarAviso.addActionListener(_ -> new ModalAviso(
                SwingUtilities.getWindowAncestor(this),
                polizaTextoFilaActual(),
                aviso -> {
                    controlador.registrarMovimiento(aviso);
                    refrescar();
                }).setVisible(true));
        itemCancelarPoliza = EstiloUI.crearItemMenuDestructivo("Cancelar póliza", "shield-off.svg");
        itemCancelarPoliza.addActionListener(_ -> new ModalCancelacion(
                SwingUtilities.getWindowAncestor(this), polizaTextoFilaActual(), null).setVisible(true));

        menuAccionesFila.add(itemVerDetalle);
        menuAccionesFila.add(itemRegistrarPagoFila);
        menuAccionesFila.add(itemEnviarAviso);
        menuAccionesFila.addSeparator();
        menuAccionesFila.add(itemCancelarPoliza);

        tablaVencimientos.vincularMenu(3, menuAccionesFila, 192);
        tablaVencimientos.setOnDobleClick(this::abrirDetalleFila);

        JPanel cardTabla = crearTarjetaVencimientos(tablaVencimientos);
        gbc.gridx = 0;
        gbc.weightx = 0.64;
        gbc.insets = new Insets(0, 0, 0, 12);
        cuerpo.add(cardTabla, gbc);

        JPanel cardPendientes = crearTarjetaPendientes();
        gbc.gridx = 1;
        gbc.weightx = 0.36;
        gbc.insets = new Insets(0, 0, 0, 0);
        cuerpo.add(cardPendientes, gbc);

        add(bloqueSuperior, BorderLayout.NORTH);
        add(cuerpo, BorderLayout.CENTER);

        refrescar();
    }

    public final void refrescar() {
        ControladorInicio.ResumenKpis kpis = controlador.obtenerKpis();
        lblKpiActivasValor.setText(String.valueOf(kpis.polizasActivas()));
        lblKpiActivasDetalle.setText(kpis.renovaciones() + " renovaciones");
        lblKpiPorVencerValor.setText(String.valueOf(kpis.porVencer()));
        lblKpiPorVencerDetalle.setText("Próx. 15 días");
        lblKpiPendValor.setText(kpis.montoPendiente());
        lblKpiPendDetalle.setText(kpis.cuotasPendientes() + " cuotas abiertas");
        lblKpiCobradoValor.setText(kpis.montoCobrado());
        lblKpiCobradoDetalle.setText("Pagos liquidados");

        modeloVencimientos.setRowCount(0);
        polizasVencimiento.clear();
        for (Poliza p : controlador.proximosVencimientos(5)) {
            polizasVencimiento.add(p);
            modeloVencimientos.addRow(new Object[]{
                new TextoDoble(p.cliente(), p.numero() + " · " + p.vehiculo(), p.esAuto() ? iconoAuto : iconoPoliza),
                p.vigencia(),
                p.estado(),
                ""
            });
        }
        tablaVencimientos.limpiarHover();

        listaPendientes.removeAll();
        List<ControladorInicio.ItemPendiente> pendientes = controlador.pendientesDelDia(4);
        if (pendientes.isEmpty()) {
            listaPendientes.add(Box.createVerticalGlue());
            JLabel lblVacio = new JLabel("Sin registros", SwingConstants.CENTER);
            lblVacio.setAlignmentX(CENTER_ALIGNMENT);
            lblVacio.putClientProperty(FlatClientProperties.STYLE, "font: bold; foreground: $Label.foreground;");
            JLabel lblSubVacio = new JLabel("No hay cobranza ni avisos pendientes.", SwingConstants.CENTER);
            lblSubVacio.setAlignmentX(CENTER_ALIGNMENT);
            lblSubVacio.putClientProperty(FlatClientProperties.STYLE, "font: -1; foreground: $Label.disabledForeground;");
            listaPendientes.add(lblVacio);
            listaPendientes.add(Box.createVerticalStrut(3));
            listaPendientes.add(lblSubVacio);
            listaPendientes.add(Box.createVerticalGlue());
        } else {
            for (int i = 0; i < pendientes.size(); i++) {
                if (i > 0) {
                    agregarSeparador(listaPendientes);
                }
                ControladorInicio.ItemPendiente item = pendientes.get(i);
                agregarItemPendiente(listaPendientes, item.titulo(), item.detalle(), item.etiqueta(), item.critico());
            }
            listaPendientes.add(Box.createVerticalGlue());
        }
        listaPendientes.revalidate();
        listaPendientes.repaint();
    }

    private void abrirModalPago(String poliza, String monto) {
        new ModalPago(SwingUtilities.getWindowAncestor(this), poliza, monto, pago -> {
            controlador.registrarMovimiento(pago);
            refrescar();
        }).setVisible(true);
    }

    private Poliza polizaEnFila(int fila) {
        return (fila >= 0 && fila < polizasVencimiento.size()) ? polizasVencimiento.get(fila) : null;
    }

    private void abrirDetalleFila(int fila) {
        Poliza sel = polizaEnFila(fila);
        if (onVerDetalle != null && sel != null) {
            onVerDetalle.accept(sel);
        }
    }

    private String polizaTextoFilaActual() {
        Poliza sel = polizaEnFila(tablaVencimientos.getFilaMenuActual());
        return sel != null ? sel.numero() + " · " + sel.cliente() : "";
    }

    private JPanel crearTarjetaKpi(String titulo, JLabel lblValor, JLabel lblDetalle, String icono) {
        JPanel card = EstiloUI.crearTarjeta(new BorderLayout(0, 6), 12, 14);

        JPanel filaTop = new JPanel(new BorderLayout());
        filaTop.setOpaque(false);

        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.putClientProperty(FlatClientProperties.STYLE, "foreground: $Label.disabledForeground; font: bold -1;");
        JLabel lblIcono = new JLabel(EstiloUI.cargarIcono(icono, 15, "Label.disabledForeground"));

        filaTop.add(lblTitulo, BorderLayout.WEST);
        filaTop.add(lblIcono, BorderLayout.EAST);

        JPanel cuerpoTexto = new JPanel();
        cuerpoTexto.setLayout(new BoxLayout(cuerpoTexto, BoxLayout.Y_AXIS));
        cuerpoTexto.setOpaque(false);
        cuerpoTexto.add(lblValor);
        cuerpoTexto.add(Box.createVerticalStrut(2));
        cuerpoTexto.add(lblDetalle);

        card.add(filaTop, BorderLayout.NORTH);
        card.add(cuerpoTexto, BorderLayout.CENTER);
        return card;
    }

    private static JLabel crearLabelKpiValor() {
        JLabel lbl = new JLabel("0");
        lbl.putClientProperty(FlatClientProperties.STYLE, "font: bold +8;");
        return lbl;
    }

    private static JLabel crearLabelKpiDetalle() {
        JLabel lbl = new JLabel("-");
        lbl.putClientProperty(FlatClientProperties.STYLE, "foreground: $Label.disabledForeground; font: -2;");
        return lbl;
    }

    private TablaOperativa crearTablaVencimientos(DefaultTableModel modelo) {
        TablaOperativa tabla = new TablaOperativa(modelo, 48, false);
        tabla.configurarColumna(0, 186, new CeldaConIconoRenderer());
        tabla.configurarColumna(1, 78, new CeldaTextoSimpleRenderer(SwingConstants.LEFT, false, false, "Label.disabledForeground"));
        tabla.configurarColumna(2, 76, new CeldaTextoSimpleRenderer(SwingConstants.RIGHT, true, true));
        tabla.configurarColumnaAccion(3, 34, 34);
        return tabla;
    }

    private JPanel crearTarjetaVencimientos(TablaOperativa tabla) {
        JPanel card = EstiloUI.crearTarjeta(new BorderLayout(0, 10), 14, 16);

        JPanel encabezado = new JPanel();
        encabezado.setLayout(new BoxLayout(encabezado, BoxLayout.Y_AXIS));
        encabezado.setOpaque(false);

        JLabel lblTitulo = new JLabel("Próximos vencimientos");
        lblTitulo.putClientProperty(FlatClientProperties.STYLE, "font: bold +1;");
        JLabel lblSub = new JLabel("Pólizas con aviso o renovación próxima");
        lblSub.putClientProperty(FlatClientProperties.STYLE, "foreground: $Label.disabledForeground; font: -2;");

        encabezado.add(lblTitulo);
        encabezado.add(Box.createVerticalStrut(2));
        encabezado.add(lblSub);

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);

        card.add(encabezado, BorderLayout.NORTH);
        card.add(scroll, BorderLayout.CENTER);
        return card;
    }

    private JPanel crearTarjetaPendientes() {
        JPanel card = EstiloUI.crearTarjeta(new BorderLayout(0, 10), 14, 16);

        JPanel encabezado = new JPanel();
        encabezado.setLayout(new BoxLayout(encabezado, BoxLayout.Y_AXIS));
        encabezado.setOpaque(false);

        JLabel lblTitulo = new JLabel("Pendientes del día");
        lblTitulo.putClientProperty(FlatClientProperties.STYLE, "font: bold +1;");
        JLabel lblSub = new JLabel("Cobranza y avisos activos");
        lblSub.putClientProperty(FlatClientProperties.STYLE, "foreground: $Label.disabledForeground; font: -2;");

        encabezado.add(lblTitulo);
        encabezado.add(Box.createVerticalStrut(2));
        encabezado.add(lblSub);

        listaPendientes.setLayout(new BoxLayout(listaPendientes, BoxLayout.Y_AXIS));
        listaPendientes.setOpaque(false);

        card.add(encabezado, BorderLayout.NORTH);
        card.add(listaPendientes, BorderLayout.CENTER);
        return card;
    }

    private void agregarItemPendiente(JPanel contenedor, String titulo, String detalle, String etiqueta, boolean critico) {
        JPanel fila = new JPanel(new BorderLayout(8, 0));
        fila.setOpaque(false);
        fila.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        fila.setBorder(BorderFactory.createEmptyBorder(6, 0, 6, 0));

        JPanel textos = new JPanel();
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        textos.setOpaque(false);

        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.putClientProperty(FlatClientProperties.STYLE, "font: bold -1;");
        JLabel lblDetalle = new JLabel(detalle);
        lblDetalle.putClientProperty(FlatClientProperties.STYLE, "foreground: $Label.disabledForeground; font: -2;");

        textos.add(lblTitulo);
        textos.add(Box.createVerticalStrut(1));
        textos.add(lblDetalle);

        JLabel badge = new JLabel(etiqueta);
        badge.setHorizontalAlignment(SwingConstants.CENTER);
        badge.putClientProperty(FlatClientProperties.STYLE, "font: bold -2;");
        badge.setForeground(critico
                ? EstiloUI.colorOrDefault("Component.error.borderColor", new Color(0xFF6467))
                : EstiloUI.colorOrDefault("SideBar.primary", new Color(0x2B7FFF)));

        fila.add(textos, BorderLayout.CENTER);
        fila.add(badge, BorderLayout.EAST);
        contenedor.add(fila);
    }

    private static void agregarSeparador(JPanel contenedor) {
        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        contenedor.add(sep);
    }

    public JButton getBtnNuevaPoliza() {
        return btnNuevaPoliza;
    }

    public JButton getBtnRegistrarPago() {
        return btnRegistrarPago;
    }

    public TablaOperativa getTablaVencimientos() {
        return tablaVencimientos;
    }

    public JPopupMenu getMenuAccionesFila() {
        return menuAccionesFila;
    }

    public JMenuItem getItemVerDetalle() {
        return itemVerDetalle;
    }

    public JMenuItem getItemRegistrarPagoFila() {
        return itemRegistrarPagoFila;
    }

    public JMenuItem getItemEnviarAviso() {
        return itemEnviarAviso;
    }

    public JMenuItem getItemCancelarPoliza() {
        return itemCancelarPoliza;
    }
}
