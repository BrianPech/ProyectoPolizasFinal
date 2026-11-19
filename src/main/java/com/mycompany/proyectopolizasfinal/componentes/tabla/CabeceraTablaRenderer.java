package com.mycompany.proyectopolizasfinal.componentes.tabla;

import com.formdev.flatlaf.FlatClientProperties;
import com.mycompany.proyectopolizasfinal.util.EstiloUI;
import java.awt.Color;
import java.awt.Component;
import java.util.HashSet;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.table.TableCellRenderer;

/**
 * Renderer de cabecera de tabla con soporte para indicadores de ordenamiento.
 */
public class CabeceraTablaRenderer extends JLabel implements TableCellRenderer {

    private final Set<Integer> columnasDerecha = new HashSet<>();

    public CabeceraTablaRenderer(int... columnasDerecha) {
        for (int col : columnasDerecha) {
            this.columnasDerecha.add(col);
        }
        setOpaque(true);
        setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 6));
        setIconTextGap(6);
        setHorizontalTextPosition(SwingConstants.LEADING);
        putClientProperty(FlatClientProperties.STYLE, "font: bold -2;");
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {
        setBackground(EstiloUI.colorOrDefault("Card.background", new Color(0x171717)));
        setText(value != null ? value.toString() : "");
        setHorizontalAlignment(columnasDerecha.contains(column) ? SwingConstants.RIGHT : SwingConstants.LEFT);

        SortOrder orden = SortOrder.UNSORTED;
        RowSorter<?> sorter = table != null ? table.getRowSorter() : null;
        if (sorter != null) {
            int modelCol = table.convertColumnIndexToModel(column);
            for (RowSorter.SortKey key : sorter.getSortKeys()) {
                if (key.getColumn() == modelCol) {
                    orden = key.getSortOrder();
                    break;
                }
            }
        }

        Icon iconoOrden = null;
        if (orden == SortOrder.ASCENDING) {
            iconoOrden = UIManager.getIcon("Table.ascendingSortIcon");
        } else if (orden == SortOrder.DESCENDING) {
            iconoOrden = UIManager.getIcon("Table.descendingSortIcon");
        }
        setIcon(iconoOrden);
        setForeground(orden == SortOrder.UNSORTED
                ? EstiloUI.colorOrDefault("Label.disabledForeground", Color.GRAY)
                : EstiloUI.colorOrDefault("Label.foreground", Color.WHITE));
        return this;
    }
}
