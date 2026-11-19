package com.mycompany.proyectopolizasfinal.vistas.catalogos;

import com.formdev.flatlaf.FlatClientProperties;
import com.mycompany.proyectopolizasfinal.componentes.ModalFormularioBase;
import com.mycompany.proyectopolizasfinal.modelo.CatalogoItem;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.util.function.Consumer;
import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 * Modal flotante para crear o editar registros de los 4 catálogos maestros
 * (`compania_aseguradora`, `tipo_poliza`, `cobertura`, `marca`).
 */
public class ModalCatalogo extends ModalFormularioBase {

    private final Consumer<CatalogoItem> onGuardar;
    private final CatalogoItem existente;

    private final JComboBox<String> cmbCatalogo = crearCombo(new String[]{
        "Aseguradoras", "Tipos de póliza", "Coberturas", "Marcas"
    });
    private final JCheckBox chkActiva = new JCheckBox("Registro activo", true);

    private final CardLayout cardsLayout = new CardLayout();
    private final JPanel panelCards = new JPanel(cardsLayout);

    // 1. compania_aseguradora: nombre(100), rfc(13), telefono(20), correo(100), contacto(100)
    private final JTextField txtAsegNombre = crearCampoTexto("Razón social o nombre comercial (máx. 100)");
    private final JTextField txtAsegRfc = crearCampoTexto("12 o 13 caracteres (ej. GNP921124EC7)");
    private final JTextField txtAsegTelefono = crearCampoTexto("Ej. 800 400 9000 (máx. 20)");
    private final JTextField txtAsegCorreo = crearCampoTexto("correo@aseguradora.com");
    private final JTextField txtAsegContacto = crearCampoTexto("Nombre del contacto o ejecutivo");

    // 2. tipo_poliza: nombre_tipo(50), descripcion(200)
    private final JTextField txtTipoNombre = crearCampoTexto("Ej. Individual, Flotilla, Carga, Plataforma");
    private final JTextArea txtTipoDesc = crearAreaTexto();
    private final JScrollPane scrollTipoDesc = envolverArea(txtTipoDesc);

    // 3. cobertura: nombre(80), descripcion(text)
    private final JTextField txtCobNombre = crearCampoTexto("Ej. RC, Daños Materiales, Robo Total");
    private final JTextArea txtCobDesc = crearAreaTexto();
    private final JScrollPane scrollCobDesc = envolverArea(txtCobDesc);

    // 4. marca: nombre_marca(50), pais_origen(50, nullable)
    private final JTextField txtMarcaNombre = crearCampoTexto("Ej. Nissan, Toyota, Ford");
    private final JTextField txtMarcaPais = crearCampoTexto("Opcional (ej. Japón, EE. UU.)");

    public ModalCatalogo(Window owner, String catalogoInicial, CatalogoItem existente,
            Consumer<CatalogoItem> onGuardar) {
        super(owner,
                existente == null ? "Nuevo Registro de Catálogo" : "Editar Registro de Catálogo",
                existente == null ? "Nuevo registro maestro" : "Editar registro maestro",
                "Campos y restricciones alineados al esquema de catálogos",
                "layers.svg",
                560,
                495);
        this.existente = existente;
        this.onGuardar = onGuardar;

        agregarCampo(0, "Catálogo *", cmbCatalogo);

        panelCards.setOpaque(false);
        panelCards.add(crearFormAseguradora(), "Aseguradoras");
        panelCards.add(crearFormTipoPoliza(), "Tipos de póliza");
        panelCards.add(crearFormCobertura(), "Coberturas");
        panelCards.add(crearFormMarca(), "Marcas");

        GridBagConstraints gbcCards = new GridBagConstraints();
        gbcCards.gridx = 0;
        gbcCards.gridy = 1;
        gbcCards.gridwidth = 2;
        gbcCards.weightx = 1.0;
        gbcCards.fill = GridBagConstraints.HORIZONTAL;
        panelCampos.add(panelCards, gbcCards);

        chkActiva.setOpaque(false);
        chkActiva.setFocusPainted(false);
        chkActiva.putClientProperty(FlatClientProperties.STYLE, "font: -1;");
        agregarCampo(2, "Activa", chkActiva);

        cmbCatalogo.addActionListener(_ -> cambiarFormulario());

        if (existente != null) {
            cmbCatalogo.setSelectedItem(existente.catalogo());
            cmbCatalogo.setEnabled(false);
            chkActiva.setSelected(!"Inactivo".equalsIgnoreCase(existente.estado()));
            cargarDatosExistentes(existente);
            btnGuardar.setText("Guardar cambios");
        } else {
            if (catalogoInicial != null) {
                cmbCatalogo.setSelectedItem(catalogoInicial);
            }
            btnGuardar.setText("Guardar Registro");
        }
        cambiarFormulario();
        finalizarLayout(owner);
    }

    private JPanel crearFormAseguradora() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        agregarFila(p, 0, "Nombre *", txtAsegNombre);
        agregarFila(p, 1, "RFC *", txtAsegRfc);
        agregarFila(p, 2, "Teléfono *", txtAsegTelefono);
        agregarFila(p, 3, "Correo *", txtAsegCorreo);
        agregarFila(p, 4, "Contacto *", txtAsegContacto);
        return p;
    }

    private JPanel crearFormTipoPoliza() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        agregarFila(p, 0, "Nombre tipo *", txtTipoNombre);
        agregarFila(p, 1, "Descripción *", scrollTipoDesc);
        agregarRelleno(p, 2);
        return p;
    }

    private JPanel crearFormCobertura() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        agregarFila(p, 0, "Nombre *", txtCobNombre);
        agregarFila(p, 1, "Descripción *", scrollCobDesc);
        agregarRelleno(p, 2);
        return p;
    }

    private JPanel crearFormMarca() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        agregarFila(p, 0, "Nombre marca *", txtMarcaNombre);
        agregarFila(p, 1, "País de origen", txtMarcaPais);
        agregarRelleno(p, 2);
        return p;
    }

    private static JTextArea crearAreaTexto() {
        JTextArea ta = new JTextArea(3, 20);
        ta.setLineWrap(true);
        ta.setWrapStyleWord(true);
        ta.putClientProperty(FlatClientProperties.STYLE, "font: -1;");
        return ta;
    }

    private static JScrollPane envolverArea(JTextArea ta) {
        JScrollPane sp = new JScrollPane(ta);
        sp.setPreferredSize(new Dimension(0, 76));
        sp.putClientProperty(FlatClientProperties.STYLE, "arc: 10;");
        return sp;
    }

    private static void agregarFila(JPanel p, int fila, String etiqueta, JComponent campo) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridy = fila;

        gbc.gridx = 0;
        gbc.weightx = 0.35;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = new Insets(campo instanceof JScrollPane ? 6 : 0, 0, 10, 12);
        JLabel lbl = new JLabel(etiqueta);
        lbl.putClientProperty(FlatClientProperties.STYLE, "foreground: $Label.disabledForeground; font: -1;");
        p.add(lbl, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.65;
        gbc.insets = new Insets(0, 0, 10, 0);
        p.add(campo, gbc);
    }

    private static void agregarRelleno(JPanel p, int fila) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = fila;
        gbc.gridwidth = 2;
        gbc.weighty = 1.0;
        p.add(Box.createVerticalGlue(), gbc);
    }

    private void cambiarFormulario() {
        String cat = (String) cmbCatalogo.getSelectedItem();
        cardsLayout.show(panelCards, cat);
        limpiarTodosLosErrores();
    }

    private void cargarDatosExistentes(CatalogoItem item) {
        switch (item.catalogo()) {
            case "Aseguradoras" -> {
                txtAsegNombre.setText(item.nombre());
                String[] partes = item.claveDetalle().split("·", 2);
                txtAsegRfc.setText(partes[0].replace("RFC", "").trim());
                txtAsegTelefono.setText(partes.length > 1 ? partes[1].trim() : "");
                txtAsegContacto.setText(item.regla().replace("Contacto:", "").trim());
                txtAsegCorreo.setText(item.detalleRegla());
            }
            case "Tipos de póliza" -> {
                txtTipoNombre.setText(item.nombre());
                txtTipoDesc.setText(item.claveDetalle());
            }
            case "Coberturas" -> {
                txtCobNombre.setText(item.nombre());
                txtCobDesc.setText(item.claveDetalle());
            }
            default -> {
                txtMarcaNombre.setText(item.nombre());
                String pais = item.claveDetalle().replace("País de origen:", "").trim();
                txtMarcaPais.setText("No especificado".equals(pais) ? "" : pais);
            }
        }
    }

    private void limpiarTodosLosErrores() {
        limpiarErrores(
                txtAsegNombre, txtAsegRfc, txtAsegTelefono, txtAsegCorreo, txtAsegContacto,
                txtTipoNombre, scrollTipoDesc,
                txtCobNombre, scrollCobDesc,
                txtMarcaNombre, txtMarcaPais
        );
    }

    @Override
    protected boolean validarYGuardar() {
        limpiarTodosLosErrores();
        int id = existente != null ? existente.id() : 0;
        String cat = (String) cmbCatalogo.getSelectedItem();
        String estado = chkActiva.isSelected() ? "Activo" : "Inactivo";
        int usos = existente != null ? existente.usos() : 0;

        CatalogoItem resultado;
        switch (cat) {
            case "Aseguradoras" -> {
                String nombre = txtAsegNombre.getText().trim();
                if (nombre.isEmpty() || nombre.length() > 100) {
                    return mostrarError("Ingresa un nombre válido (1 a 100 caracteres).", txtAsegNombre);
                }
                String rfc = txtAsegRfc.getText().trim().toUpperCase();
                if (rfc.length() < 12 || rfc.length() > 13) {
                    return mostrarError("El RFC debe tener 12 o 13 caracteres.", txtAsegRfc);
                }
                String tel = txtAsegTelefono.getText().trim();
                if (tel.isEmpty() || tel.length() > 20) {
                    return mostrarError("Ingresa un teléfono válido (máx. 20 caracteres).", txtAsegTelefono);
                }
                String correo = txtAsegCorreo.getText().trim();
                if (correo.isEmpty() || correo.length() > 100 || !correo.contains("@")) {
                    return mostrarError("Ingresa un correo válido (máx. 100 caracteres).", txtAsegCorreo);
                }
                String contacto = txtAsegContacto.getText().trim();
                if (contacto.isEmpty() || contacto.length() > 100) {
                    return mostrarError("Ingresa el contacto (1 a 100 caracteres).", txtAsegContacto);
                }
                resultado = new CatalogoItem(
                        id,
                        nombre,
                        "RFC " + rfc + " · " + tel,
                        "Aseguradoras",
                        "Compañía emisora",
                        "Contacto: " + contacto,
                        correo,
                        usos,
                        estado
                );
            }
            case "Tipos de póliza" -> {
                String nombreTipo = txtTipoNombre.getText().trim();
                if (nombreTipo.isEmpty() || nombreTipo.length() > 50) {
                    return mostrarError("Ingresa el nombre del tipo (máx. 50 caracteres).", txtTipoNombre);
                }
                String desc = txtTipoDesc.getText().trim();
                if (desc.isEmpty() || desc.length() > 200) {
                    return mostrarError("Ingresa la descripción (1 a 200 caracteres).", scrollTipoDesc);
                }
                resultado = new CatalogoItem(
                        id,
                        nombreTipo,
                        desc,
                        "Tipos de póliza",
                        "Modalidad",
                        "Catálogo tipo_poliza",
                        existente != null ? existente.detalleRegla() : "Póliza vehicular",
                        usos,
                        estado
                );
            }
            case "Coberturas" -> {
                String nombreCob = txtCobNombre.getText().trim();
                if (nombreCob.isEmpty() || nombreCob.length() > 80) {
                    return mostrarError("Ingresa el nombre de la cobertura (máx. 80 caracteres).", txtCobNombre);
                }
                String desc = txtCobDesc.getText().trim();
                if (desc.isEmpty()) {
                    return mostrarError("Ingresa la descripción de la cobertura.", scrollCobDesc);
                }
                resultado = new CatalogoItem(
                        id,
                        nombreCob,
                        desc,
                        "Coberturas",
                        "Cobertura base",
                        "En poliza_cobertura",
                        existente != null ? existente.detalleRegla() : "Catálogo cobertura",
                        usos,
                        estado
                );
            }
            default -> {
                String nombreMarca = txtMarcaNombre.getText().trim();
                if (nombreMarca.isEmpty() || nombreMarca.length() > 50) {
                    return mostrarError("Ingresa el nombre de la marca (máx. 50 caracteres).", txtMarcaNombre);
                }
                String pais = txtMarcaPais.getText().trim();
                if (pais.length() > 50) {
                    return mostrarError("El país de origen no debe exceder 50 caracteres.", txtMarcaPais);
                }
                resultado = new CatalogoItem(
                        id,
                        nombreMarca,
                        "País de origen: " + (pais.isEmpty() ? "No especificado" : pais),
                        "Marcas",
                        "Automotriz",
                        "Catálogo vehicular",
                        existente != null ? existente.detalleRegla() : "Tabla marca",
                        usos,
                        estado
                );
            }
        }

        if (onGuardar != null) {
            onGuardar.accept(resultado);
        }
        return true;
    }
}
