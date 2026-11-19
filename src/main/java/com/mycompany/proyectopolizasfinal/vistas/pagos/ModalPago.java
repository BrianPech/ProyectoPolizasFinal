package com.mycompany.proyectopolizasfinal.vistas.pagos;

import com.mycompany.proyectopolizasfinal.componentes.JDateInput;
import com.mycompany.proyectopolizasfinal.componentes.ModalFormularioBase;
import com.mycompany.proyectopolizasfinal.modelo.MovimientoPago;
import com.mycompany.proyectopolizasfinal.util.Formatos;
import java.awt.Window;
import java.time.LocalDate;
import java.util.function.Consumer;
import javax.swing.JComboBox;
import javax.swing.JTextField;

/**
 * Modal flotante para registrar o liquidar una cuota en `pago` según `db_scheme.md`.
 */
public class ModalPago extends ModalFormularioBase {

    private final Consumer<MovimientoPago> onGuardar;

    private final JTextField txtPoliza = crearCampoTexto("Ej. POL-2026-0142 · Juan Pérez");
    private final JTextField txtCuota = crearCampoTexto("Ej. 1");
    private final JTextField txtMonto = crearCampoTexto("Ej. 6225.00");
    private final JDateInput dateVencimiento = new JDateInput(LocalDate.now().plusDays(15), true);
    private final JDateInput datePago = new JDateInput(LocalDate.now(), true);
    private final JComboBox<String> cmbFormaPago = crearCombo(new String[]{"Transferencia", "Tarjeta", "Efectivo"});
    private final JTextField txtRecibo = crearCampoTexto("Folio único (ej. REC-0842, máx. 30)");
    private final JComboBox<String> cmbEstado = crearCombo(new String[]{"Pagado", "Pendiente", "Vencido", "Cancelado"});

    public ModalPago(Window owner, String polizaInicial, String montoInicial, Consumer<MovimientoPago> onGuardar) {
        super(owner,
                "Registrar Pago",
                "Registrar pago de cuota",
                "Tabla pago (cuota, monto, forma_pago, recibo y estado)",
                "credit-card.svg",
                560,
                525);
        this.onGuardar = onGuardar;

        agregarCampo(0, "Póliza / Cliente *", txtPoliza);
        agregarCampo(1, "Número de cuota *", txtCuota);
        agregarCampo(2, "Monto (MXN) *", txtMonto);
        agregarCampo(3, "Fecha límite cuota *", dateVencimiento);
        agregarCampo(4, "Fecha de pago", datePago);
        agregarCampo(5, "Forma de pago *", cmbFormaPago);
        agregarCampo(6, "Número de recibo", txtRecibo);
        agregarCampo(7, "Estado", cmbEstado);

        if (polizaInicial != null && !polizaInicial.isBlank()) {
            txtPoliza.setText(polizaInicial);
        }
        if (montoInicial != null && !montoInicial.isBlank()) {
            txtMonto.setText(montoInicial.replace("$", "").replace(",", "").trim());
        }
        btnGuardar.setText("Guardar Pago");
        finalizarLayout(owner);
    }

    @Override
    protected boolean validarYGuardar() {
        limpiarErrores(txtPoliza, txtCuota, txtMonto, txtRecibo);
        dateVencimiento.setError(false);

        if (estaVacio(txtPoliza)) {
            return mostrarError("Indica la póliza o cliente del pago.", txtPoliza);
        }
        int cuota;
        try {
            cuota = Integer.parseInt(txtCuota.getText().trim());
        } catch (NumberFormatException ex) {
            cuota = 0;
        }
        if (cuota <= 0) {
            return mostrarError("Ingresa un número de cuota válido mayor a 0.", txtCuota);
        }
        double monto = Formatos.parsearMontoCaptura(txtMonto.getText());
        if (monto <= 0) {
            return mostrarError("Ingresa un monto válido mayor a 0.", txtMonto);
        }
        if (!dateVencimiento.isValidDate() || dateVencimiento.getDate() == null) {
            dateVencimiento.setError(true);
            return mostrarError("Selecciona una fecha límite de cuota válida.", null);
        }
        String recibo = txtRecibo.getText().trim();
        if (recibo.length() > 30) {
            return mostrarError("El número de recibo no debe exceder 30 caracteres.", txtRecibo);
        }

        String estado = (String) cmbEstado.getSelectedItem();
        if ("Pagado".equals(estado) && recibo.isEmpty()) {
            return mostrarError("Ingresa el número de recibo para un pago liquidado.", txtRecibo);
        }

        String detalleCanal = recibo.isEmpty() ? "Sin recibo" : "Recibo #" + recibo;
        String detalleFecha = "Pagado".equals(estado) ? "Liquidado" : "Cuota programada";

        if (onGuardar != null) {
            onGuardar.accept(new MovimientoPago(
                    0,
                    "Cuota " + cuota + " · Pago registrado",
                    txtPoliza.getText().trim(),
                    "Pago / Cuota",
                    (String) cmbFormaPago.getSelectedItem(),
                    detalleCanal,
                    Formatos.formatearFechaCorta(dateVencimiento.getDate()),
                    detalleFecha,
                    Formatos.formatearMoneda(monto),
                    estado
            ));
        }
        return true;
    }
}
