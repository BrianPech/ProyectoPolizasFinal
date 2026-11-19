package com.mycompany.proyectopolizasfinal.util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.util.Locale;
import java.util.Map;

/**
 * Utilidades de parseo y formato de montos, enteros y fechas.
 */
public final class Formatos {

    private static final String[] MESES_CORTO = {
        "Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"
    };

    private static final Map<String, String> MESES_ORDEN = Map.ofEntries(
            Map.entry("ene", "01"), Map.entry("feb", "02"), Map.entry("mar", "03"),
            Map.entry("abr", "04"), Map.entry("may", "05"), Map.entry("jun", "06"),
            Map.entry("jul", "07"), Map.entry("ago", "08"), Map.entry("sep", "09"),
            Map.entry("oct", "10"), Map.entry("nov", "11"), Map.entry("dic", "12"));

    private Formatos() {}

    public static int parsearEntero(String texto) {
        if (texto == null) {
            return 0;
        }
        String digits = texto.replaceAll("[^0-9]", "");
        if (digits.isEmpty()) {
            return 0;
        }
        try {
            return Integer.parseInt(digits);
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    /** Para ordenar columnas monetarias (retorna 0.0 si no es numérico). */
    public static double parsearMonto(Object valor) {
        if (valor == null) {
            return 0.0;
        }
        String limpio = valor.toString().replaceAll("[^0-9.]", "");
        if (limpio.isEmpty()) {
            return 0.0;
        }
        try {
            return Double.parseDouble(limpio);
        } catch (NumberFormatException ex) {
            return 0.0;
        }
    }

    /** Para validar captura de montos en formularios (retorna -1 si es inválido o vacío). */
    public static double parsearMontoCaptura(String raw) {
        if (raw == null) {
            return -1;
        }
        String limpio = raw.replace("$", "").replace(",", "").trim();
        if (limpio.isEmpty()) {
            return -1;
        }
        try {
            return Double.parseDouble(limpio);
        } catch (NumberFormatException ex) {
            return -1;
        }
    }

    public static String formatearMoneda(double monto) {
        if (monto <= 0) {
            return "$0";
        }
        DecimalFormat df = new DecimalFormat("#,##0", DecimalFormatSymbols.getInstance(Locale.US));
        return "$" + df.format(Math.round(monto));
    }

    public static String formatearFechaCorta(LocalDate fecha) {
        if (fecha == null) {
            return "-";
        }
        return String.format("%02d %s %d",
                fecha.getDayOfMonth(),
                MESES_CORTO[fecha.getMonthValue() - 1],
                fecha.getYear());
    }

    /** Inverso de {@link #formatearFechaCorta(LocalDate)}: "05 Mar 2026" → 2026-03-05 (null si no se reconoce). */
    public static LocalDate parsearFechaCorta(String texto) {
        String clave = claveOrdenFecha(texto);
        if (clave == null || !clave.matches("\\d{4}-\\d{2}-\\d{2}")) {
            return null;
        }
        try {
            return LocalDate.parse(clave);
        } catch (java.time.format.DateTimeParseException ex) {
            return null;
        }
    }

    public static String claveOrdenFecha(String texto) {
        if (texto == null) {
            return "";
        }
        String[] partes = texto.trim().split("\\s+");
        if (partes.length == 3) {
            String mes = MESES_ORDEN.get(partes[1].toLowerCase());
            if (mes != null) {
                String dia = partes[0].length() == 1 ? "0" + partes[0] : partes[0];
                return partes[2] + "-" + mes + "-" + dia;
            }
        }
        return texto.toLowerCase();
    }
}
