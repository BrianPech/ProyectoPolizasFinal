package com.mycompany.proyectopolizasfinal.vistas.clientes;

import com.formdev.flatlaf.FlatClientProperties;
import com.mycompany.proyectopolizasfinal.componentes.ModalFormularioBase;
import com.mycompany.proyectopolizasfinal.modelo.Cliente;
import java.awt.Window;
import java.util.function.Consumer;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JTextField;

/**
 * Modal flotante para crear o editar un registro de `cliente` según `db_scheme.md`.
 */
public class ModalCliente extends ModalFormularioBase {

    private final Consumer<Cliente> onGuardar;
    private final Cliente existente;

    private final JComboBox<String> cmbTipo = crearCombo(new String[]{"Fisica", "Moral"});
    private final JTextField txtNombre = crearCampoTexto("Nombre o Razón Social (máx. 100)");
    private final JTextField txtApellidos = crearCampoTexto("Apellidos (opcional si es Moral, máx. 100)");
    private final JTextField txtRfc = crearCampoTexto("12 o 13 caracteres (ej. PEMJ880412H19)");
    private final JTextField txtTelefono = crearCampoTexto("Ej. 55 4120 8831 (máx. 20)");
    private final JTextField txtCorreo = crearCampoTexto("correo@dominio.com (máx. 100)");
    private final JTextField txtDireccion = crearCampoTexto("Calle, número, colonia y ciudad");
    private final JCheckBox chkActivo = new JCheckBox("Cliente activo", true);

    public ModalCliente(Window owner, Cliente existente, Consumer<Cliente> onGuardar) {
        super(owner,
                existente == null ? "Nuevo Cliente" : "Editar Cliente",
                existente == null ? "Registrar cliente" : "Editar cliente",
                "Tabla cliente (tipo_persona, RFC, contacto, dirección, activo)",
                "user.svg",
                560,
                515);
        this.existente = existente;
        this.onGuardar = onGuardar;

        agregarCampo(0, "Tipo persona *", cmbTipo);
        agregarCampo(1, "Nombre / Razón social *", txtNombre);
        agregarCampo(2, "Apellidos", txtApellidos);
        agregarCampo(3, "RFC *", txtRfc);
        agregarCampo(4, "Teléfono *", txtTelefono);
        agregarCampo(5, "Correo *", txtCorreo);
        agregarCampo(6, "Dirección *", txtDireccion);

        chkActivo.setOpaque(false);
        chkActivo.setFocusPainted(false);
        chkActivo.putClientProperty(FlatClientProperties.STYLE, "font: -1;");
        agregarCampo(7, "Activo", chkActivo);

        cmbTipo.addActionListener(_ -> actualizarEstadoApellidos());
        if (existente != null) {
            cargarDatos(existente);
            btnGuardar.setText("Guardar cambios");
        } else {
            btnGuardar.setText("Registrar Cliente");
        }
        actualizarEstadoApellidos();
        finalizarLayout(owner);
    }

    private void cargarDatos(Cliente c) {
        cmbTipo.setSelectedItem(c.tipoPersona());
        txtNombre.setText(c.nombre());
        txtApellidos.setText(c.apellidos() != null ? c.apellidos() : "");
        txtRfc.setText(c.rfc());
        txtTelefono.setText(c.telefono());
        txtCorreo.setText(c.correo());
        txtDireccion.setText(c.direccion() != null ? c.direccion() : "");
        chkActivo.setSelected(!"Inactivo".equalsIgnoreCase(c.estatus()));
    }

    private void actualizarEstadoApellidos() {
        boolean fisica = "Fisica".equals(cmbTipo.getSelectedItem());
        txtApellidos.setEnabled(fisica);
        if (!fisica) {
            txtApellidos.setText("");
        }
    }

    @Override
    protected boolean validarYGuardar() {
        limpiarErrores(txtNombre, txtApellidos, txtRfc, txtTelefono, txtCorreo, txtDireccion);
        String nombre = txtNombre.getText().trim();
        if (nombre.isEmpty() || nombre.length() > 100) {
            return mostrarError("Ingresa un nombre válido (1 a 100 caracteres).", txtNombre);
        }
        String apellidos = txtApellidos.getText().trim();
        if (apellidos.length() > 100) {
            return mostrarError("Los apellidos no deben exceder 100 caracteres.", txtApellidos);
        }
        String rfc = txtRfc.getText().trim().toUpperCase();
        if (rfc.length() < 12 || rfc.length() > 13) {
            return mostrarError("El RFC debe tener 12 o 13 caracteres.", txtRfc);
        }
        String tel = txtTelefono.getText().trim();
        if (tel.isEmpty() || tel.length() > 20) {
            return mostrarError("Ingresa un teléfono válido (máx. 20 caracteres).", txtTelefono);
        }
        String correo = txtCorreo.getText().trim();
        if (correo.isEmpty() || correo.length() > 100 || !correo.contains("@")) {
            return mostrarError("Ingresa un correo válido (máx. 100 caracteres).", txtCorreo);
        }
        String direccion = txtDireccion.getText().trim();
        if (direccion.isEmpty()) {
            return mostrarError("Ingresa la dirección del cliente.", txtDireccion);
        }

        int polizas = existente != null ? existente.polizasActivas() : 0;
        String resumen = existente != null ? existente.resumenPolizas() : "Sin pólizas";
        String prima = existente != null ? existente.primaAnual() : "$0";
        String estatus;
        if (!chkActivo.isSelected()) {
            estatus = "Inactivo";
        } else if (existente != null && !"Inactivo".equalsIgnoreCase(existente.estatus())) {
            estatus = existente.estatus();
        } else {
            estatus = "Al corriente";
        }

        if (onGuardar != null) {
            onGuardar.accept(new Cliente(
                    existente != null ? existente.id() : 0,
                    nombre,
                    "Fisica".equals(cmbTipo.getSelectedItem()) ? apellidos : "",
                    rfc,
                    (String) cmbTipo.getSelectedItem(),
                    tel,
                    correo,
                    direccion,
                    polizas,
                    resumen,
                    prima,
                    estatus
            ));
        }
        return true;
    }
}
