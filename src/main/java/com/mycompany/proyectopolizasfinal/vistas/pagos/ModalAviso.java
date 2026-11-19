package com.mycompany.proyectopolizasfinal.vistas.pagos;

import com.mycompany.proyectopolizasfinal.componentes.ModalFormularioBase;
import com.mycompany.proyectopolizasfinal.modelo.MovimientoPago;
import com.mycompany.proyectopolizasfinal.util.Formatos;
import java.awt.Window;
import java.time.LocalDate;
import java.util.function.Consumer;
import javax.swing.JComboBox;
import javax.swing.JTextField;

/**
 * Modal flotante para registrar o enviar un aviso en `aviso_vencimiento` según `db_scheme.md`.
 */
public class ModalAviso extends ModalFormularioBase {

    private final Consumer<MovimientoPago> onGuardar;

    private final JTextField txtPoliza = crearCampoTexto("Ej. POL-2026-0139 · María López");
    private final JComboBox<String> cmbTipoAviso = crearCombo(new String[]{"Renovacion_Poliza", "Cobro_Cuota"});
    private final JComboBox<String> cmbMedio = crearCombo(new String[]{"Correo", "Notificación en Pantalla"});
    private final JTextField txtDias = crearCampoTexto("Ej. 15");
    private final JComboBox<String> cmbEstado = crearCombo(new String[]{"Enviado", "Pendiente", "Contactado", "Renovado"});

    public ModalAviso(Window owner, String polizaInicial, Consumer<MovimientoPago> onGuardar) {
        super(owner,
                "Enviar Aviso",
                "Registrar aviso de vencimiento",
                "Tabla aviso_vencimiento (tipo_aviso, medio_envio, días y estado)",
                "send.svg",
                540,
                420);
        this.onGuardar = onGuardar;

        agregarCampo(0, "Póliza / Cliente *", txtPoliza);
        agregarCampo(1, "Tipo de aviso *", cmbTipoAviso);
        agregarCampo(2, "Medio de envío *", cmbMedio);
        agregarCampo(3, "Días restantes *", txtDias);
        agregarCampo(4, "Estado", cmbEstado);

        if (polizaInicial != null && !polizaInicial.isBlank()) {
            txtPoliza.setText(polizaInicial);
        }
        btnGuardar.setText("Registrar Aviso");
        finalizarLayout(owner);
    }

    @Override
    protected boolean validarYGuardar() {
        limpiarErrores(txtPoliza, txtDias);
        if (estaVacio(txtPoliza)) {
            return mostrarError("Indica la póliza o cliente destinatario.", txtPoliza);
        }
        int dias;
        try {
            dias = Integer.parseInt(txtDias.getText().trim());
        } catch (NumberFormatException ex) {
            return mostrarError("Ingresa un número válido de días restantes.", txtDias);
        }

        String tipo = (String) cmbTipoAviso.getSelectedItem();
        String concepto = "Cobro_Cuota".equals(tipo) ? "Aviso de cobro de cuota" : "Aviso de renovación";

        if (onGuardar != null) {
            onGuardar.accept(new MovimientoPago(
                    0,
                    concepto,
                    txtPoliza.getText().trim(),
                    "Aviso",
                    (String) cmbMedio.getSelectedItem(),
                    "Generado hoy",
                    Formatos.formatearFechaCorta(LocalDate.now()),
                    "Faltan " + dias + " días",
                    "—",
                    (String) cmbEstado.getSelectedItem()
            ));
        }
        return true;
    }
}
