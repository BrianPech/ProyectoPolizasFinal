package com.mycompany.proyectopolizasfinal.vistas.catalogos;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.mycompany.proyectopolizasfinal.componentes.PanelListadoBase;
import com.mycompany.proyectopolizasfinal.componentes.tabla.CeldaConIconoRenderer;
import com.mycompany.proyectopolizasfinal.componentes.tabla.CeldaDobleLineaRenderer;
import com.mycompany.proyectopolizasfinal.componentes.tabla.CeldaTextoSimpleRenderer;
import com.mycompany.proyectopolizasfinal.componentes.tabla.TablaOperativa;
import com.mycompany.proyectopolizasfinal.componentes.tabla.TextoDoble;
import com.mycompany.proyectopolizasfinal.controlador.ControladorCatalogos;
import com.mycompany.proyectopolizasfinal.modelo.CatalogoItem;
import com.mycompany.proyectopolizasfinal.repositorio.Repositorios;
import com.mycompany.proyectopolizasfinal.util.EstiloUI;
import com.mycompany.proyectopolizasfinal.util.Formatos;
import java.util.Comparator;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

/**
 * Vista de Catálogos maestros (aseguradoras, tipos de póliza, coberturas y marcas).
 */
public class PanelCatalogos extends PanelListadoBase<CatalogoItem> {

    private final ControladorCatalogos controlador;
    private final JToggleButton tabTodos;
    private final JToggleButton tabAseguradoras;
    private final JToggleButton tabTiposPoliza;
    private final JToggleButton tabCoberturas;
    private final JToggleButton tabMarcas;
    private final JComboBox<String> cmbCatalogo;
    private final JComboBox<String> cmbEstado;

    private final FlatSVGIcon iconoCapas = EstiloUI.cargarIcono("layers.svg", 15, "Label.foreground");
    private final FlatSVGIcon iconoEscudo = EstiloUI.cargarIcono("shield-check.svg", 15, "Label.foreground");

    public PanelCatalogos() {
        this(new ControladorCatalogos(Repositorios.defecto().catalogos()));
    }

    public PanelCatalogos(ControladorCatalogos controlador) {
        super("Catálogos",
                "Catálogos maestros de aseguradoras, tipos de póliza, coberturas y marcas",
                "Buscar por clave, nombre o regla...",
                new String[]{"Registro", "Catálogo", "Regla / Valor", "Pólizas", "Estado", ""},
                56,
                3, 4);
        this.controlador = controlador;

        agregarBotonPrimario("Nuevo Registro", "layers.svg", () -> abrirModalCatalogo(null));

        tabTodos = agregarTab("Todos", true);
        tabAseguradoras = agregarTab("Aseguradoras", false);
        tabTiposPoliza = agregarTab("Tipos de póliza", false);
        tabCoberturas = agregarTab("Coberturas", false);
        tabMarcas = agregarTab("Marcas", false);

        cmbCatalogo = agregarComboFiltro(new String[]{"Todos los catálogos", "Aseguradoras", "Tipos de póliza", "Coberturas", "Marcas"}, 148);
        cmbEstado = agregarComboFiltro(new String[]{"Estado", "Activo", "Inactivo"}, 132);

        setAnchoMinimoMenu(195);
        agregarItemMenu("Ver detalle", "eye.svg", this::abrirModalCatalogo);
        agregarItemMenu("Editar registro", "settings.svg", this::abrirModalCatalogo);
        agregarSeparadorMenu();
        agregarItemMenuDestructivo("Desactivar", "shield-off.svg", this::desactivarItem);

        sorter.setComparator(0, TablaOperativa.COMPARADOR_TEXTO_DOBLE);
        sorter.setComparator(1, TablaOperativa.COMPARADOR_TEXTO_DOBLE);
        sorter.setComparator(2, TablaOperativa.COMPARADOR_TEXTO_DOBLE);
        sorter.setComparator(3, Comparator.comparingInt(v -> Formatos.parsearEntero(String.valueOf(v))));
        sorter.setComparator(4, String.CASE_INSENSITIVE_ORDER);

        tabla.configurarColumna(0, 220, new CeldaConIconoRenderer());
        tabla.configurarColumna(1, 115, new CeldaDobleLineaRenderer());
        tabla.configurarColumna(2, 150, new CeldaDobleLineaRenderer());
        tabla.configurarColumna(3, 70, new CeldaTextoSimpleRenderer(SwingConstants.RIGHT, true, false));
        tabla.configurarColumna(4, 80, new CeldaTextoSimpleRenderer(SwingConstants.RIGHT, true, true));
        tabla.configurarColumnaAccion(5, 38, 42);

        recargarTabla();
    }

    private String catalogoTabActual() {
        if (tabAseguradoras.isSelected()) return "Aseguradoras";
        if (tabTiposPoliza.isSelected()) return "Tipos de póliza";
        if (tabCoberturas.isSelected()) return "Coberturas";
        if (tabMarcas.isSelected()) return "Marcas";
        return null;
    }

    private void abrirModalCatalogo(CatalogoItem existente) {
        String inicial = catalogoTabActual();
        new ModalCatalogo(
                SwingUtilities.getWindowAncestor(this),
                inicial != null ? inicial : "Aseguradoras",
                existente,
                guardado -> {
                    if (existente == null) {
                        controlador.guardar(guardado);
                        tabTodos.setSelected(true);
                    } else {
                        controlador.actualizar(guardado);
                    }
                    recargarTabla();
                }).setVisible(true);
    }

    private void desactivarItem(CatalogoItem sel) {
        int op = JOptionPane.showConfirmDialog(this,
                "¿Desactivar \"" + sel.nombre() + "\" de " + sel.catalogo() + "?",
                "Confirmar baja",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (op == JOptionPane.YES_OPTION) {
            controlador.desactivar(sel);
            recargarTabla();
        }
    }

    @Override
    protected List<CatalogoItem> obtenerElementosFiltrados(String textoBusqueda) {
        return controlador.filtrar(
                catalogoTabActual(),
                (String) cmbCatalogo.getSelectedItem(),
                (String) cmbEstado.getSelectedItem(),
                textoBusqueda);
    }

    @Override
    protected Object[] mapearFila(CatalogoItem item) {
        boolean esEscudo = "Aseguradoras".equalsIgnoreCase(item.catalogo())
                || "Coberturas".equalsIgnoreCase(item.catalogo());
        return new Object[]{
            new TextoDoble(item.nombre(), item.claveDetalle(), esEscudo ? iconoEscudo : iconoCapas),
            new TextoDoble(item.catalogo(), item.subtipo(), null),
            new TextoDoble(item.regla(), item.detalleRegla(), null),
            String.valueOf(item.usos()),
            item.estado(),
            ""
        };
    }
}
