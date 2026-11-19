package com.mycompany.proyectopolizasfinal.modelo;

/**
 * Modelo de vehículo asegurado.
 */
public record Vehiculo(
        int id,
        String marca,
        String modelo,
        String placas,
        String color,
        String vin,
        String uso,
        String poliza,
        String aseguradoraCliente,
        int anio,
        String estatus) {

    public String marcaModelo() {
        return (marca == null || marca.isBlank()) ? modelo : marca + " " + modelo;
    }

    public String placasColor() {
        return (color == null || color.isBlank()) ? placas : placas + " · " + color;
    }

    public Vehiculo conId(int nuevoId) {
        return new Vehiculo(nuevoId, marca, modelo, placas, color, vin, uso,
                poliza, aseguradoraCliente, anio, estatus);
    }

    public Vehiculo dadoDeBaja() {
        return new Vehiculo(id, marca, modelo, placas, color, vin, uso,
                "Sin póliza activa", "Baja de unidad", anio, "Sin póliza");
    }
}
