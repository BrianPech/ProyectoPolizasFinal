package com.mycompany.proyectopolizasfinal.vistas.polizas;

import com.formdev.flatlaf.FlatClientProperties;
import com.mycompany.proyectopolizasfinal.componentes.ModalFormularioBase;
import com.mycompany.proyectopolizasfinal.modelo.Cancelacion;
import com.mycompany.proyectopolizasfinal.util.Formatos;
import java.awt.Dimension;
import java.awt.Window;
import java.util.function.Consumer;
import javax.swing.JComboBox;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 * Modal flotante para solicitar/resolver cancelación en `cancelacion_poliza` según `db_scheme.md`.
 */
public class ModalCancelacion extends ModalFormularioBase {

    private final Consumer<Cancelacion> onGuardar;

    private final JTextField txtPoliza = crearCampoTexto("");
    private final JTextArea txtMotivo = new JTextArea(3, 20);
    private final JScrollPane scrollMotivo;
    private final JTextField txtDevolucion = crearCampoTexto("0.00");
    private final JComboBox<String> cmbEstado = crearCombo(new String[]{"Pendiente", "Aprobada", "Rechazada"});

    public ModalCancelacion(Window owner, String polizaTexto, Consumer<Cancelacion> onGuardar) {
        super(owner,
                "Solicitar Cancelación",
                "Solicitud de cancelación",
                "Tabla cancelacion_poliza (motivo, devolucion_prima y estado_solicitud)",
                "shield-off.svg",
                540,
                420);
        this.onGuardar = onGuardar;

        txtPoliza.setText(polizaTexto != null ? polizaTexto : "");
        txtPoliza.setEditable(false);

        txtMotivo.setLineWrap(true);
        txtMotivo.setWrapStyleWord(true);
        txtMotivo.putClientProperty(FlatClientProperties.STYLE, "font: -1;");
        scrollMotivo = new JScrollPane(txtMotivo);
        scrollMotivo.setPreferredSize(new Dimension(0, 76));
        scrollMotivo.putClientProperty(FlatClientProperties.STYLE, "arc: 10;");

        txtDevolucion.setText("0");

        agregarCampo(0, "Póliza", txtPoliza);
        agregarCampo(1, "Motivo *", scrollMotivo);
        agregarCampo(2, "Devolución prima (MXN)", txtDevolucion);
        agregarCampo(3, "Estado solicitud", cmbEstado);

        btnGuardar.setText("Confirmar Solicitud");
        finalizarLayout(owner);
    }

    @Override
    protected boolean validarYGuardar() {
        limpiarErrores(scrollMotivo, txtDevolucion);
        String motivo = txtMotivo.getText().trim();
        if (motivo.isEmpty() || motivo.length() > 255) {
            return mostrarError("Ingresa el motivo de cancelación (1 a 255 caracteres).", scrollMotivo);
        }
        String raw = txtDevolucion.getText().replace("$", "").replace(",", "").trim();
        double devolucion = raw.isEmpty() ? 0 : Formatos.parsearMontoCaptura(raw);
        if (devolucion < 0) {
            return mostrarError("Ingresa un monto de devolución válido no negativo.", txtDevolucion);
        }

        if (onGuardar != null) {
            onGuardar.accept(new Cancelacion(
                    motivo, Formatos.formatearMoneda(devolucion), (String) cmbEstado.getSelectedItem()));
        }
        return true;
    }
}
