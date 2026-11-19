package com.mycompany.proyectopolizasfinal.componentes.tabla;

import com.formdev.flatlaf.extras.FlatSVGIcon;

/**
 * Valor de celda compuesto por texto principal, subtítulo e ícono opcional.
 */
public record TextoDoble(String principal, String secundario, FlatSVGIcon icono) {}
