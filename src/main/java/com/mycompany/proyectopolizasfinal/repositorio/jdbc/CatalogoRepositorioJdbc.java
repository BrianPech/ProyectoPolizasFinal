package com.mycompany.proyectopolizasfinal.repositorio.jdbc;

import com.mycompany.proyectopolizasfinal.modelo.CatalogoItem;
import com.mycompany.proyectopolizasfinal.repositorio.PersistenciaException;
import com.mycompany.proyectopolizasfinal.repositorio.RepositorioCrud;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Une los cuatro catálogos ({@code compania_aseguradora}, {@code tipo_poliza}, {@code cobertura},
 * {@code marca}) en un solo repositorio de {@link CatalogoItem}.
 * <p>
 * Como cada tabla tiene su propio AUTO_INCREMENT, el id del item se codifica como
 * {@code idReal * 10 + codigoTabla} para que no choquen entre catálogos.
 */
public class CatalogoRepositorioJdbc extends BaseRepositorioJdbc implements RepositorioCrud<CatalogoItem> {

    public static final String ASEGURADORAS = "Aseguradoras";
    public static final String TIPOS = "Tipos de póliza";
    public static final String COBERTURAS = "Coberturas";
    public static final String MARCAS = "Marcas";

    private static final int COD_ASEG = 1;
    private static final int COD_TIPO = 2;
    private static final int COD_COB = 3;
    private static final int COD_MARCA = 4;

    private static final Pattern RFC_TEL = Pattern.compile("^RFC\\s+(\\S+)\\s*·\\s*(.*)$");
    private static final String PREFIJO_CONTACTO = "Contacto: ";
    private static final String PREFIJO_PAIS = "País de origen: ";

    public CatalogoRepositorioJdbc(ConexionBD conexionBD) {
        super(conexionBD);
    }

    @Override
    public List<CatalogoItem> listar() {
        List<CatalogoItem> todos = new ArrayList<>();
        todos.addAll(consultarLista(SQL_ASEG + " ORDER BY a.nombre", this::mapearAseguradora));
        todos.addAll(consultarLista(SQL_TIPO + " ORDER BY t.nombre_tipo", this::mapearTipo));
        todos.addAll(consultarLista(SQL_COB + " ORDER BY c.nombre", this::mapearCobertura));
        todos.addAll(consultarLista(SQL_MARCA + " ORDER BY m.nombre_marca", this::mapearMarca));
        return todos;
    }

    @Override
    public Optional<CatalogoItem> buscarPorId(int id) {
        int real = id / 10;
        return switch (id % 10) {
            case COD_ASEG -> consultarUno(SQL_ASEG + " WHERE a.id_aseguradora = ?", this::mapearAseguradora, real);
            case COD_TIPO -> consultarUno(SQL_TIPO + " WHERE t.id_tipo_poliza = ?", this::mapearTipo, real);
            case COD_COB -> consultarUno(SQL_COB + " WHERE c.id_cobertura = ?", this::mapearCobertura, real);
            case COD_MARCA -> consultarUno(SQL_MARCA + " WHERE m.id_marca = ?", this::mapearMarca, real);
            default -> Optional.empty();
        };
    }

    @Override
    public CatalogoItem guardar(CatalogoItem item) {
        boolean activa = activa(item);
        int id = switch (catalogo(item)) {
            case ASEGURADORAS -> {
                String[] rfcTel = rfcYTelefono(item);
                yield ejecutarInsertConId("""
                        INSERT INTO compania_aseguradora (nombre, rfc, telefono, correo, contacto, activa)
                        VALUES (?, ?, ?, ?, ?, ?)
                        """, item.nombre(), rfcTel[0], rfcTel[1], item.detalleRegla(), contacto(item), activa)
                        * 10 + COD_ASEG;
            }
            case TIPOS -> ejecutarInsertConId(
                    "INSERT INTO tipo_poliza (nombre_tipo, descripcion, activa) VALUES (?, ?, ?)",
                    item.nombre(), item.claveDetalle(), activa) * 10 + COD_TIPO;
            case COBERTURAS -> ejecutarInsertConId(
                    "INSERT INTO cobertura (nombre, descripcion, activa) VALUES (?, ?, ?)",
                    item.nombre(), item.claveDetalle(), activa) * 10 + COD_COB;
            default -> ejecutarInsertConId(
                    "INSERT INTO marca (nombre_marca, pais_origen, activa) VALUES (?, ?, ?)",
                    item.nombre(), pais(item), activa) * 10 + COD_MARCA;
        };
        return buscarPorId(id).orElse(item.conId(id));
    }

    @Override
    public CatalogoItem actualizar(CatalogoItem item) {
        int real = item.id() / 10;
        boolean activa = activa(item);
        int filas = switch (item.id() % 10) {
            case COD_ASEG -> {
                String[] rfcTel = rfcYTelefono(item);
                yield ejecutarUpdate("""
                        UPDATE compania_aseguradora
                        SET nombre = ?, rfc = ?, telefono = ?, correo = ?, contacto = ?, activa = ?
                        WHERE id_aseguradora = ?
                        """, item.nombre(), rfcTel[0], rfcTel[1], item.detalleRegla(), contacto(item), activa, real);
            }
            case COD_TIPO -> ejecutarUpdate(
                    "UPDATE tipo_poliza SET nombre_tipo = ?, descripcion = ?, activa = ? WHERE id_tipo_poliza = ?",
                    item.nombre(), item.claveDetalle(), activa, real);
            case COD_COB -> ejecutarUpdate(
                    "UPDATE cobertura SET nombre = ?, descripcion = ?, activa = ? WHERE id_cobertura = ?",
                    item.nombre(), item.claveDetalle(), activa, real);
            case COD_MARCA -> ejecutarUpdate(
                    "UPDATE marca SET nombre_marca = ?, pais_origen = ?, activa = ? WHERE id_marca = ?",
                    item.nombre(), pais(item), activa, real);
            default -> 0;
        };
        if (filas == 0) {
            throw new PersistenciaException("No se encontró el registro de catálogo con id=" + item.id());
        }
        return buscarPorId(item.id()).orElse(item);
    }

    @Override
    public void eliminar(int id) {
        int real = id / 10;
        switch (id % 10) {
            case COD_ASEG -> ejecutarUpdate("DELETE FROM compania_aseguradora WHERE id_aseguradora = ?", real);
            case COD_TIPO -> ejecutarUpdate("DELETE FROM tipo_poliza WHERE id_tipo_poliza = ?", real);
            case COD_COB -> ejecutarUpdate("DELETE FROM cobertura WHERE id_cobertura = ?", real);
            case COD_MARCA -> ejecutarUpdate("DELETE FROM marca WHERE id_marca = ?", real);
            default -> { }
        }
    }

    // ------------------------------------------------------------------ SQL + mapeo

    private static final String SQL_ASEG = """
            SELECT a.*, (SELECT COUNT(*) FROM poliza p WHERE p.id_aseguradora = a.id_aseguradora) AS usos
            FROM compania_aseguradora a
            """;
    private static final String SQL_TIPO = """
            SELECT t.*, (SELECT COUNT(*) FROM poliza p WHERE p.id_tipo_poliza = t.id_tipo_poliza) AS usos
            FROM tipo_poliza t
            """;
    private static final String SQL_COB = """
            SELECT c.*, (SELECT COUNT(*) FROM poliza_cobertura pc WHERE pc.id_cobertura = c.id_cobertura) AS usos
            FROM cobertura c
            """;
    private static final String SQL_MARCA = """
            SELECT m.*, (SELECT COUNT(*) FROM vehiculo v WHERE v.id_marca = m.id_marca) AS usos
            FROM marca m
            """;

    private CatalogoItem mapearAseguradora(ResultSet rs) throws SQLException {
        return new CatalogoItem(
                rs.getInt("id_aseguradora") * 10 + COD_ASEG,
                rs.getString("nombre"),
                "RFC " + rs.getString("rfc") + " · " + rs.getString("telefono"),
                ASEGURADORAS,
                "Compañía emisora",
                PREFIJO_CONTACTO + rs.getString("contacto"),
                rs.getString("correo"),
                rs.getInt("usos"),
                estado(rs.getBoolean("activa")));
    }

    private CatalogoItem mapearTipo(ResultSet rs) throws SQLException {
        return new CatalogoItem(
                rs.getInt("id_tipo_poliza") * 10 + COD_TIPO,
                rs.getString("nombre_tipo"),
                rs.getString("descripcion"),
                TIPOS,
                "Modalidad",
                "Catálogo tipo_poliza",
                "Póliza vehicular",
                rs.getInt("usos"),
                estado(rs.getBoolean("activa")));
    }

    private CatalogoItem mapearCobertura(ResultSet rs) throws SQLException {
        return new CatalogoItem(
                rs.getInt("id_cobertura") * 10 + COD_COB,
                rs.getString("nombre"),
                rs.getString("descripcion"),
                COBERTURAS,
                "Cobertura base",
                "En poliza_cobertura",
                "Catálogo cobertura",
                rs.getInt("usos"),
                estado(rs.getBoolean("activa")));
    }

    private CatalogoItem mapearMarca(ResultSet rs) throws SQLException {
        String pais = rs.getString("pais_origen");
        return new CatalogoItem(
                rs.getInt("id_marca") * 10 + COD_MARCA,
                rs.getString("nombre_marca"),
                PREFIJO_PAIS + (pais == null ? "No especificado" : pais),
                MARCAS,
                "Automotriz",
                "Catálogo vehicular",
                "Tabla marca",
                rs.getInt("usos"),
                estado(rs.getBoolean("activa")));
    }

    // ------------------------------------------------------------------ helpers de los textos del modal

    private static String catalogo(CatalogoItem item) {
        String c = item.catalogo() == null ? "" : item.catalogo();
        if (c.equalsIgnoreCase(ASEGURADORAS)) return ASEGURADORAS;
        if (c.equalsIgnoreCase(TIPOS)) return TIPOS;
        if (c.equalsIgnoreCase(COBERTURAS)) return COBERTURAS;
        return MARCAS;
    }

    private static boolean activa(CatalogoItem item) {
        return !"Inactivo".equalsIgnoreCase(item.estado());
    }

    private static String estado(boolean activa) {
        return activa ? "Activo" : "Inactivo";
    }

    /** "RFC ABC123456XYZ · 5512345678" → [rfc, teléfono]. */
    private static String[] rfcYTelefono(CatalogoItem item) {
        Matcher m = RFC_TEL.matcher(item.claveDetalle() == null ? "" : item.claveDetalle().trim());
        if (!m.matches()) {
            throw new PersistenciaException("No se pudo leer el RFC y teléfono de la aseguradora.");
        }
        return new String[]{m.group(1).trim(), m.group(2).trim()};
    }

    private static String contacto(CatalogoItem item) {
        String r = item.regla() == null ? "" : item.regla().trim();
        return r.startsWith(PREFIJO_CONTACTO) ? r.substring(PREFIJO_CONTACTO.length()).trim() : r;
    }

    private static String pais(CatalogoItem item) {
        String d = item.claveDetalle() == null ? "" : item.claveDetalle().trim();
        String pais = d.startsWith(PREFIJO_PAIS) ? d.substring(PREFIJO_PAIS.length()).trim() : d;
        return pais.isEmpty() || "No especificado".equalsIgnoreCase(pais) ? null : pais;
    }
}
