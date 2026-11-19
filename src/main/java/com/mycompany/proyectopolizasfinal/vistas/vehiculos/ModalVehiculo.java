package com.mycompany.proyectopolizasfinal.vistas.vehiculos;

import com.formdev.flatlaf.FlatClientProperties;
import com.mycompany.proyectopolizasfinal.componentes.ModalFormularioBase;
import com.mycompany.proyectopolizasfinal.modelo.Vehiculo;
import java.awt.Window;
import java.time.LocalDate;
import java.util.function.Consumer;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JTextField;

/**
 * Modal flotante para crear o editar un registro de `vehiculo` (FK `marca`).
 */
public class ModalVehiculo extends ModalFormularioBase {

    private final Consumer<Vehiculo> onGuardar;
    private final Vehiculo existente;

    private final JComboBox<String> cmbMarca = crearCombo(new String[]{
        "Nissan", "Honda", "Mazda", "Toyota", "Volkswagen", "Ford", "Kia", "Chevrolet"
    });
    private final JTextField txtModelo = crearCampoTexto("Ej. Versa, Civic, Corolla");
    private final JTextField txtAnio = crearCampoTexto("Ej. 2024");
    private final JTextField txtPlacas = crearCampoTexto("Ej. ABC-123");
    private final JTextField txtVin = crearCampoTexto("17 caracteres alfanuméricos");
    private final JTextField txtColor = crearCampoTexto("Ej. Gris Plata");
    private final JComboBox<String> cmbUso = crearCombo(new String[]{"Particular", "Comercial"});
    private final JCheckBox chkActivo = new JCheckBox("Vehículo activo", true);

    public ModalVehiculo(Window owner, Vehiculo existente, Consumer<Vehiculo> onGuardar) {
        super(owner,
                existente == null ? "Nuevo Vehículo" : "Editar Vehículo",
                existente == null ? "Registrar vehículo" : "Editar vehículo",
                "Tabla vehiculo (id_marca, placas, VIN, modelo, año, color, uso)",
                "car.svg",
                550,
                510);
        this.existente = existente;
        this.onGuardar = onGuardar;

        agregarCampo(0, "Marca *", cmbMarca);
        agregarCampo(1, "Modelo *", txtModelo);
        agregarCampo(2, "Año *", txtAnio);
        agregarCampo(3, "Placas *", txtPlacas);
        agregarCampo(4, "Número de serie (VIN) *", txtVin);
        agregarCampo(5, "Color *", txtColor);
        agregarCampo(6, "Uso *", cmbUso);

        chkActivo.setOpaque(false);
        chkActivo.setFocusPainted(false);
        chkActivo.putClientProperty(FlatClientProperties.STYLE, "font: -1;");
        agregarCampo(7, "Activo", chkActivo);

        if (existente != null) {
            cargarDatos(existente);
            btnGuardar.setText("Guardar cambios");
        } else {
            txtAnio.setText(String.valueOf(LocalDate.now().getYear()));
            btnGuardar.setText("Registrar Vehículo");
        }
        finalizarLayout(owner);
    }

    private void cargarDatos(Vehiculo v) {
        cmbMarca.setSelectedItem(v.marca());
        txtModelo.setText(v.modelo());
        txtAnio.setText(String.valueOf(v.anio()));
        txtPlacas.setText(v.placas());
        txtColor.setText(v.color());
        txtVin.setText(v.vin());
        cmbUso.setSelectedItem(v.uso());
        chkActivo.setSelected(!"Baja de unidad".equalsIgnoreCase(v.aseguradoraCliente()));
    }

    @Override
    protected boolean validarYGuardar() {
        limpiarErrores(txtModelo, txtAnio, txtPlacas, txtVin, txtColor);
        String modelo = txtModelo.getText().trim();
        if (modelo.isEmpty() || modelo.length() > 50) {
            return mostrarError("Ingresa un modelo válido (1 a 50 caracteres).", txtModelo);
        }
        int anio;
        try {
            anio = Integer.parseInt(txtAnio.getText().trim());
        } catch (NumberFormatException ex) {
            anio = -1;
        }
        int maxAnio = LocalDate.now().getYear() + 1;
        if (anio < 1970 || anio > maxAnio) {
            return mostrarError("Ingresa un año válido (1970 - " + maxAnio + ").", txtAnio);
        }
        String placas = txtPlacas.getText().trim().toUpperCase();
        if (placas.isEmpty() || placas.length() > 10) {
            return mostrarError("Ingresa placas válidas (1 a 10 caracteres).", txtPlacas);
        }
        String vin = txtVin.getText().trim().toUpperCase();
        if (vin.length() < 10 || vin.length() > 17) {
            return mostrarError("Ingresa un VIN válido (10 a 17 caracteres).", txtVin);
        }
        String color = txtColor.getText().trim();
        if (color.isEmpty() || color.length() > 30) {
            return mostrarError("Ingresa un color válido (1 a 30 caracteres).", txtColor);
        }

        String poliza = existente != null ? existente.poliza() : "Sin póliza activa";
        String asegCli = chkActivo.isSelected()
                ? (existente != null && !"Baja de unidad".equalsIgnoreCase(existente.aseguradoraCliente())
                        ? existente.aseguradoraCliente() : "Disponible para asignar")
                : "Baja de unidad";
        String estatus = chkActivo.isSelected()
                ? (existente != null ? existente.estatus() : "Sin póliza")
                : "Sin póliza";

        if (onGuardar != null) {
            onGuardar.accept(new Vehiculo(
                    existente != null ? existente.id() : 0,
                    (String) cmbMarca.getSelectedItem(),
                    modelo,
                    placas,
                    color,
                    vin,
                    (String) cmbUso.getSelectedItem(),
                    poliza,
                    asegCli,
                    anio,
                    estatus
            ));
        }
        return true;
    }
}
