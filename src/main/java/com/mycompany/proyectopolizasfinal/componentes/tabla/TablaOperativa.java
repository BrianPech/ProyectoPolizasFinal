package com.mycompany.proyectopolizasfinal.componentes.tabla;

import com.formdev.flatlaf.FlatClientProperties;
import com.mycompany.proyectopolizasfinal.util.EstiloUI;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.Comparator;
import java.util.function.IntConsumer;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

/**
 * Tabla estilizada con soporte integrado de hover por fila, menú contextual por celda de acción y doble clic.
 */
public class TablaOperativa extends JTable {

    public static final Comparator<Object> COMPARADOR_TEXTO_DOBLE = Comparator.comparing(
            v -> v instanceof TextoDoble td ? td.principal() : String.valueOf(v),
            String.CASE_INSENSITIVE_ORDER);

    private int filaHover = -1;
    private boolean hoverBotonAccion;
    private int filaMenuActual = -1;
    private long ultimoCierreMenuMs;

    private int columnaAccion = -1;
    private JPopupMenu menuAcciones;
    private int anchoMinimoMenu = 200;
    private IntConsumer onDobleClick;
    private String tituloVacio = "Sin registros";
    private String subtituloVacio = "Aún no hay elementos para mostrar.";

    public TablaOperativa(TableModel modelo, int alturaFila, boolean mostrarCabecera, int... colsCabeceraDerecha) {
        super(modelo);
        setOpaque(false);
        setFillsViewportHeight(true);
        setShowGrid(false);
        setIntercellSpacing(new Dimension(0, 0));
        setRowHeight(alturaFila);
        setFocusable(false);
        setRowSelectionAllowed(false);
        setCellSelectionEnabled(false);

        if (!mostrarCabecera) {
            setTableHeader(null);
        } else {
            JTableHeader header = getTableHeader();
            header.setReorderingAllowed(false);
            header.setResizingAllowed(false);
            header.setBackground(EstiloUI.colorOrDefault("Card.background", new Color(0x171717)));
            header.putClientProperty(FlatClientProperties.STYLE,
                    "height: 34; separatorColor: #00000000; bottomSeparatorColor: $Component.borderColor;");
            header.setDefaultRenderer(new CabeceraTablaRenderer(colsCabeceraDerecha));
            header.addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    int col = header.columnAtPoint(e.getPoint());
                    boolean ordenable = col >= 0 && col != columnaAccion && getRowSorter() != null;
                    header.setCursor(Cursor.getPredefinedCursor(
                            ordenable ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
                }
            });
        }

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int fila = rowAtPoint(e.getPoint());
                int col = columnAtPoint(e.getPoint());
                boolean sobreBtn = fila >= 0 && columnaAccion >= 0 && col == columnaAccion;
                if (fila != filaHover || sobreBtn != hoverBotonAccion) {
                    filaHover = fila;
                    hoverBotonAccion = sobreBtn;
                    setCursor(Cursor.getPredefinedCursor(sobreBtn ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
                    repaint();
                }
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                limpiarHover();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                int fila = rowAtPoint(e.getPoint());
                int col = columnAtPoint(e.getPoint());
                if (fila < 0 || !SwingUtilities.isLeftMouseButton(e)) {
                    return;
                }
                if (columnaAccion >= 0 && col == columnaAccion) {
                    alternarMenuFila(fila);
                } else if (e.getClickCount() == 2 && onDobleClick != null) {
                    onDobleClick.accept(fila);
                }
            }
        });
    }

    public void vincularMenu(int columnaAccion, JPopupMenu menu, int anchoMinimo) {
        this.columnaAccion = columnaAccion;
        this.menuAcciones = menu;
        this.anchoMinimoMenu = anchoMinimo;
        if (menu == null) {
            return;
        }
        menu.addPopupMenuListener(new PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {}

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                ultimoCierreMenuMs = System.currentTimeMillis();
                repaint();
            }

            @Override
            public void popupMenuCanceled(PopupMenuEvent e) {
                ultimoCierreMenuMs = System.currentTimeMillis();
                repaint();
            }
        });
    }

    public void setAnchoMinimoMenu(int anchoMinimo) {
        this.anchoMinimoMenu = anchoMinimo;
    }

    public void setOnDobleClick(IntConsumer onDobleClick) {
        this.onDobleClick = onDobleClick;
    }

    public void setMensajeVacio(String titulo, String subtitulo) {
        this.tituloVacio = titulo;
        this.subtituloVacio = subtitulo;
        repaint();
    }

    public String getTituloVacio() {
        return tituloVacio;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (getRowCount() == 0 && tituloVacio != null && !tituloVacio.isBlank()) {
            pintarEstadoVacio(g);
        }
    }

    private void pintarEstadoVacio(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        Font base = getFont() != null ? getFont() : UIManager.getFont("Label.font");
        if (base == null) {
            base = new Font(Font.SANS_SERIF, Font.PLAIN, 13);
        }
        Font fTitulo = base.deriveFont(Font.BOLD, base.getSize2D() + 1f);
        Font fSub = base.deriveFont(Font.PLAIN, Math.max(11f, base.getSize2D() - 1f));

        FontMetrics fmTit = g2.getFontMetrics(fTitulo);
        FontMetrics fmSub = g2.getFontMetrics(fSub);
        boolean tieneSub = subtituloVacio != null && !subtituloVacio.isBlank();
        int altoTotal = fmTit.getHeight() + (tieneSub ? 4 + fmSub.getHeight() : 0);
        int yBase = Math.max(fmTit.getAscent() + 12, (getHeight() - altoTotal) / 2 + fmTit.getAscent());

        g2.setFont(fTitulo);
        g2.setColor(EstiloUI.colorOrDefault("Label.foreground", Color.WHITE));
        int xTit = (getWidth() - fmTit.stringWidth(tituloVacio)) / 2;
        g2.drawString(tituloVacio, xTit, yBase);

        if (tieneSub) {
            g2.setFont(fSub);
            g2.setColor(EstiloUI.colorOrDefault("Label.disabledForeground", Color.GRAY));
            int xSub = (getWidth() - fmSub.stringWidth(subtituloVacio)) / 2;
            g2.drawString(subtituloVacio, xSub, yBase + 4 + fmSub.getHeight());
        }
        g2.dispose();
    }

    public void configurarColumna(int col, int prefWidth, TableCellRenderer renderer) {
        getColumnModel().getColumn(col).setPreferredWidth(prefWidth);
        getColumnModel().getColumn(col).setCellRenderer(renderer);
    }

    public void configurarColumnaAccion(int col, int prefWidth, int maxWidth) {
        var cm = getColumnModel().getColumn(col);
        cm.setPreferredWidth(prefWidth);
        cm.setMaxWidth(maxWidth);
        cm.setCellRenderer(new CeldaAccionRenderer());
    }

    public void limpiarHover() {
        if (filaHover != -1 || hoverBotonAccion) {
            filaHover = -1;
            hoverBotonAccion = false;
            setCursor(Cursor.getDefaultCursor());
            repaint();
        }
    }

    public int getFilaHover() {
        return filaHover;
    }

    public int getFilaMenuActual() {
        return filaMenuActual;
    }

    public boolean isAccionActiva(int row) {
        return (row == filaHover && hoverBotonAccion)
                || (menuAcciones != null && menuAcciones.isVisible() && row == filaMenuActual);
    }

    private void alternarMenuFila(int fila) {
        if (menuAcciones == null) {
            return;
        }
        if (menuAcciones.isVisible() && fila == filaMenuActual) {
            menuAcciones.setVisible(false);
            return;
        }
        if (fila == filaMenuActual && System.currentTimeMillis() - ultimoCierreMenuMs < 200) {
            return;
        }
        filaMenuActual = fila;
        menuAcciones.pack();
        Dimension pref = menuAcciones.getPreferredSize();
        int width = Math.max(pref.width, anchoMinimoMenu);
        menuAcciones.setPopupSize(width, pref.height);
        Rectangle cellRect = getCellRect(fila, columnaAccion, true);
        int x = cellRect.x + cellRect.width - width;
        int y = cellRect.y + cellRect.height - 6;
        menuAcciones.show(this, x, y);
        repaint();
    }
}
