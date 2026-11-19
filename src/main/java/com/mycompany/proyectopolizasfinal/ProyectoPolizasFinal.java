package com.mycompany.proyectopolizasfinal;

import com.mycompany.proyectopolizasfinal.repositorio.PersistenciaException;
import com.mycompany.proyectopolizasfinal.repositorio.Repositorios;
import com.mycompany.proyectopolizasfinal.util.EstiloUI;
import com.mycompany.proyectopolizasfinal.vistas.Login;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class ProyectoPolizasFinal {

    public static void main(String[] args) {
        instalarFuentesModernas();
        EstiloUI.inicializarTema();

        instalarManejadorErrores();

        SwingUtilities.invokeLater(() -> {
            Login login = new Login();
            login.setVisible(true);
            Repositorios.errorConexion().ifPresent(error -> JOptionPane.showMessageDialog(login,
                    "No se pudo conectar a la base de datos MySQL.\n"
                    + "La aplicación funcionará con datos en memoria (se pierden al cerrar).\n\n"
                    + "Detalle: " + error + "\n\n"
                    + "Revisa que MySQL esté encendido en XAMPP y el archivo db.properties.",
                    "Sin conexión a la base de datos", JOptionPane.WARNING_MESSAGE));
        });
    }

    /**
     * Muestra en un diálogo los errores de base de datos (cliente inexistente, RFC duplicado, etc.)
     * en lugar de que solo aparezcan en la consola.
     */
    private static void instalarManejadorErrores() {
        Thread.setDefaultUncaughtExceptionHandler((hilo, error) -> {
            error.printStackTrace();
            Throwable causa = error;
            while (causa != null && !(causa instanceof PersistenciaException)) {
                causa = causa.getCause();
            }
            String mensaje = causa != null ? causa.getMessage()
                    : "Ocurrió un error inesperado: " + error;
            Runnable mostrar = () -> JOptionPane.showMessageDialog(null, mensaje,
                    "Error al guardar", JOptionPane.ERROR_MESSAGE);
            if (SwingUtilities.isEventDispatchThread()) {
                mostrar.run();
            } else {
                SwingUtilities.invokeLater(mostrar);
            }
        });
    }

    public static void instalarFuentesModernas() {
        if (EstiloUI.esMac()) {
            System.setProperty("apple.awt.application.appearance", "system");
        }
        try {
            Class<?> interFontClass = Class.forName("com.formdev.flatlaf.fonts.inter.FlatInterFont");
            interFontClass.getMethod("install").invoke(null);
        } catch (Throwable ignored) {
            try {
                Class<?> robotoFontClass = Class.forName("com.formdev.flatlaf.fonts.roboto.FlatRobotoFont");
                robotoFontClass.getMethod("install").invoke(null);
            } catch (Throwable ignored2) {}
        }
    }
}
