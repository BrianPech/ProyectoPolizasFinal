package com.mycompany.proyectopolizasfinal.vistas.usuarios;

import com.formdev.flatlaf.FlatClientProperties;
import com.mycompany.proyectopolizasfinal.componentes.ModalFormularioBase;
import com.mycompany.proyectopolizasfinal.modelo.Usuario;
import java.awt.Window;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.Predicate;
import javax.swing.JComboBox;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

/**
 * Modal flotante para crear o editar una cuenta en `usuario` (`id_rol`, `nombre`, `usuario`, `contrasena_hash`, `estado`).
 */
public class ModalUsuario extends ModalFormularioBase {

    private final Consumer<Usuario> onGuardar;
    private final Usuario existente;
    private final Predicate<String> usuarioDuplicado;

    private final JTextField txtNombre = crearCampoTexto("Nombre completo (máx. 100)");
    private final JTextField txtUsername = crearCampoTexto("Login único sin espacios (máx. 50)");
    private final JComboBox<String> cmbRol = crearCombo(new String[]{"Agente de Ventas", "Administrador"});
    private final JPasswordField txtPassword = crearCampoPassword("Mínimo 6 caracteres");
    private final JPasswordField txtConfirmar = crearCampoPassword("Repite la contraseña");
    private final JComboBox<String> cmbEstado = crearCombo(new String[]{"Activo", "Inactivo"});

    public ModalUsuario(Window owner, Usuario existente,
            Predicate<String> usuarioDuplicado, boolean enfocarPassword,
            Consumer<Usuario> onGuardar) {
        super(owner,
                existente == null ? "Nuevo Usuario" : (enfocarPassword ? "Restablecer Contraseña" : "Editar Usuario"),
                existente == null ? "Registrar usuario" : (enfocarPassword ? "Restablecer contraseña" : "Editar usuario y rol"),
                "Tabla usuario + rol (Administrador, Agente de Ventas)",
                enfocarPassword ? "lock.svg" : "user-cog.svg",
                540,
                475);
        this.existente = existente;
        this.usuarioDuplicado = usuarioDuplicado;
        this.onGuardar = onGuardar;

        agregarCampo(0, "Nombre *", txtNombre);
        agregarCampo(1, "Usuario (login) *", txtUsername);
        agregarCampo(2, "Rol *", cmbRol);
        agregarCampo(3, existente == null ? "Contraseña *" : "Nueva contraseña", txtPassword);
        agregarCampo(4, existente == null ? "Confirmar *" : "Confirmar nueva", txtConfirmar);
        agregarCampo(5, "Estado", cmbEstado);

        if (existente != null) {
            txtNombre.setText(existente.nombre());
            txtUsername.setText(existente.username());
            cmbRol.setSelectedItem(existente.rol());
            cmbEstado.setSelectedItem(existente.estado());
            String hint = "Dejar vacío para conservar actual";
            txtPassword.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, hint);
            txtConfirmar.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, hint);
            btnGuardar.setText("Guardar cambios");
            if (enfocarPassword) {
                txtPassword.requestFocusInWindow();
            }
        } else {
            btnGuardar.setText("Crear Usuario");
        }
        finalizarLayout(owner);
    }

    @Override
    protected boolean validarYGuardar() {
        limpiarErrores(txtNombre, txtUsername, txtPassword, txtConfirmar);
        String nombre = txtNombre.getText().trim();
        if (nombre.isEmpty() || nombre.length() > 100) {
            return mostrarError("Ingresa un nombre válido (1 a 100 caracteres).", txtNombre);
        }
        String user = txtUsername.getText().trim().toLowerCase();
        if (user.length() < 3 || user.length() > 50 || user.contains(" ")) {
            return mostrarError("El usuario debe tener 3 a 50 caracteres y sin espacios.", txtUsername);
        }
        if (usuarioDuplicado != null && usuarioDuplicado.test(user)) {
            return mostrarError("El usuario @" + user + " ya existe.", txtUsername);
        }

        char[] pass = txtPassword.getPassword();
        char[] confirm = txtConfirmar.getPassword();
        boolean capturaPass = (existente == null) || pass.length > 0 || confirm.length > 0;
        if (capturaPass) {
            if (pass.length < 6) {
                return mostrarError("La contraseña debe tener al menos 6 caracteres.", txtPassword);
            }
            if (!Arrays.equals(pass, confirm)) {
                return mostrarError("Las contraseñas no coinciden.", txtConfirmar);
            }
        }
        Arrays.fill(pass, '\0');
        Arrays.fill(confirm, '\0');

        String rol = (String) cmbRol.getSelectedItem();
        String descRol = "Administrador".equals(rol)
                ? "Control total, catálogos y autorizaciones"
                : "Emisión, renovaciones y cobros";
        int polizas = existente != null ? existente.polizasGestionadas() : 0;

        if (onGuardar != null) {
            onGuardar.accept(new Usuario(
                    existente != null ? existente.id() : 0,
                    nombre,
                    user,
                    rol,
                    descRol,
                    polizas,
                    (String) cmbEstado.getSelectedItem()
            ));
        }
        return true;
    }
}
