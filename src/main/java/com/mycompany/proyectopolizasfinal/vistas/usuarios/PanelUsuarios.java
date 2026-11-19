package com.mycompany.proyectopolizasfinal.vistas.usuarios;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.mycompany.proyectopolizasfinal.componentes.PanelListadoBase;
import com.mycompany.proyectopolizasfinal.componentes.tabla.CeldaConIconoRenderer;
import com.mycompany.proyectopolizasfinal.componentes.tabla.CeldaDobleLineaRenderer;
import com.mycompany.proyectopolizasfinal.componentes.tabla.CeldaTextoSimpleRenderer;
import com.mycompany.proyectopolizasfinal.componentes.tabla.TablaOperativa;
import com.mycompany.proyectopolizasfinal.componentes.tabla.TextoDoble;
import com.mycompany.proyectopolizasfinal.controlador.ControladorUsuarios;
import com.mycompany.proyectopolizasfinal.modelo.Usuario;
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
 * Vista de administración de Usuarios, roles de acceso y actividad reciente.
 */
public class PanelUsuarios extends PanelListadoBase<Usuario> {

    private final ControladorUsuarios controlador;
    private final JToggleButton tabTodos;
    private final JToggleButton tabAdmins;
    private final JToggleButton tabAgentes;
    private final JToggleButton tabInactivos;
    private final JComboBox<String> cmbRol;
    private final JComboBox<String> cmbEstado;

    private final FlatSVGIcon iconoAdmin = EstiloUI.cargarIcono("user-cog.svg", 15, "Label.foreground");
    private final FlatSVGIcon iconoUsuario = EstiloUI.cargarIcono("user.svg", 15, "Label.foreground");

    public PanelUsuarios() {
        this(new ControladorUsuarios(Repositorios.defecto().usuarios()));
    }

    public PanelUsuarios(ControladorUsuarios controlador) {
        super("Usuarios",
                "Gestión de cuentas de acceso, roles y estado",
                "Buscar por nombre, usuario o rol...",
                new String[]{"Usuario", "Rol", "Permisos", "Pólizas", "Estado", ""},
                56,
                3, 4);
        this.controlador = controlador;

        agregarBotonPrimario("Nuevo Usuario", "user-cog.svg", () -> abrirModalUsuario(null, false));

        tabTodos = agregarTab("Todos", true);
        tabAdmins = agregarTab("Administradores", false);
        tabAgentes = agregarTab("Agentes de Ventas", false);
        tabInactivos = agregarTab("Inactivos", false);

        cmbRol = agregarComboFiltro(new String[]{"Todos los roles", "Administrador", "Agente de Ventas"}, 148);
        cmbEstado = agregarComboFiltro(new String[]{"Estado", "Activo", "Inactivo"}, 132);

        setAnchoMinimoMenu(220);
        agregarItemMenu("Editar rol y datos", "user-cog.svg", u -> abrirModalUsuario(u, false));
        agregarItemMenu("Restablecer contraseña", "lock.svg", u -> abrirModalUsuario(u, true));
        agregarItemMenu("Ver actividad reciente", "eye.svg", this::mostrarActividad);
        agregarSeparadorMenu();
        agregarItemMenuDestructivo("Desactivar usuario", "shield-off.svg", this::alternarEstado);

        sorter.setComparator(0, TablaOperativa.COMPARADOR_TEXTO_DOBLE);
        sorter.setComparator(1, TablaOperativa.COMPARADOR_TEXTO_DOBLE);
        sorter.setComparator(2, TablaOperativa.COMPARADOR_TEXTO_DOBLE);
        sorter.setComparator(3, Comparator.comparingInt(v -> Formatos.parsearEntero(String.valueOf(v))));
        sorter.setComparator(4, String.CASE_INSENSITIVE_ORDER);

        tabla.configurarColumna(0, 215, new CeldaConIconoRenderer());
        tabla.configurarColumna(1, 140, new CeldaDobleLineaRenderer());
        tabla.configurarColumna(2, 130, new CeldaDobleLineaRenderer());
        tabla.configurarColumna(3, 75, new CeldaTextoSimpleRenderer(SwingConstants.RIGHT, true, false));
        tabla.configurarColumna(4, 80, new CeldaTextoSimpleRenderer(SwingConstants.RIGHT, true, true));
        tabla.configurarColumnaAccion(5, 38, 42);

        recargarTabla();
    }

    private void abrirModalUsuario(Usuario existente, boolean enfocarPassword) {
        new ModalUsuario(
                SwingUtilities.getWindowAncestor(this),
                existente,
                username -> controlador.existeUsername(username, existente),
                enfocarPassword,
                (guardado, password) -> {
                    if (existente == null) {
                        controlador.guardar(guardado, password);
                        tabTodos.setSelected(true);
                    } else {
                        controlador.actualizar(guardado, password);
                    }
                    recargarTabla();
                }).setVisible(true);
    }

    private void mostrarActividad(Usuario sel) {
        JOptionPane.showMessageDialog(this,
                "Usuario: " + sel.nombre() + " (@" + sel.username() + ")\n"
                + "Rol: " + sel.rol() + "\n"
                + "Permisos: " + sel.descripcionRol() + "\n"
                + "Estado: " + sel.estado() + "\n"
                + "Pólizas gestionadas: " + sel.polizasGestionadas(),
                "Actividad del usuario",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void alternarEstado(Usuario sel) {
        boolean activo = "Activo".equalsIgnoreCase(sel.estado());
        int op = JOptionPane.showConfirmDialog(this,
                (activo ? "¿Desactivar" : "¿Reactivar") + " al usuario @" + sel.username() + "?",
                "Confirmar cambio de estado",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (op == JOptionPane.YES_OPTION) {
            controlador.alternarEstado(sel);
            recargarTabla();
        }
    }

    @Override
    protected List<Usuario> obtenerElementosFiltrados(String textoBusqueda) {
        return controlador.filtrar(
                tabAdmins.isSelected(),
                tabAgentes.isSelected(),
                tabInactivos.isSelected(),
                (String) cmbRol.getSelectedItem(),
                (String) cmbEstado.getSelectedItem(),
                textoBusqueda);
    }

    @Override
    protected Object[] mapearFila(Usuario u) {
        boolean esAdmin = "Administrador".equalsIgnoreCase(u.rol());
        return new Object[]{
            new TextoDoble(u.nombre(), "@" + u.username(), esAdmin ? iconoAdmin : iconoUsuario),
            new TextoDoble(u.rol(), "Acceso " + u.estado().toLowerCase(), null),
            new TextoDoble(u.descripcionRol(), "Tabla rol / usuario", null),
            String.valueOf(u.polizasGestionadas()),
            u.estado(),
            ""
        };
    }
}
