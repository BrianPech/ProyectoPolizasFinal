package com.mycompany.proyectopolizasfinal;

import com.mycompany.proyectopolizasfinal.util.EstiloUI;
import com.mycompany.proyectopolizasfinal.vistas.Login;
import javax.swing.SwingUtilities;

public class ProyectoPolizasFinal {

    public static void main(String[] args) {
        instalarFuentesModernas();
        EstiloUI.inicializarTema();

        SwingUtilities.invokeLater(() -> new Login().setVisible(true));
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
