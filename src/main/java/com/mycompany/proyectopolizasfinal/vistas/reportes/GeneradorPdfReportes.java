package com.mycompany.proyectopolizasfinal.vistas.reportes;

import com.mycompany.proyectopolizasfinal.modelo.Reporte;
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dialog;
import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.Window;
import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import org.openpdf.text.Document;
import org.openpdf.text.Element;
import org.openpdf.text.Font;
import org.openpdf.text.PageSize;
import org.openpdf.text.Paragraph;
import org.openpdf.text.Phrase;
import org.openpdf.text.pdf.PdfPCell;
import org.openpdf.text.pdf.PdfPTable;
import org.openpdf.text.pdf.PdfWriter;

/**
 * Generador de reportes en PDF usando OpenPDF (`org.openpdf.text.*`).
 */
public final class GeneradorPdfReportes {

    private GeneradorPdfReportes() {}

    public static void exportarPdf(Component parent, Reporte reporte, List<String[]> filas) {
        if (reporte == null) {
            return;
        }
        Window win = parent instanceof Window w ? w : SwingUtilities.getWindowAncestor(parent);
        FileDialog fd = win instanceof Dialog d
                ? new FileDialog(d, "Guardar reporte PDF", FileDialog.SAVE)
                : new FileDialog(win instanceof Frame f ? f : null, "Guardar reporte PDF", FileDialog.SAVE);
        fd.setFile(reporte.folio() + ".pdf");
        fd.setFilenameFilter((_, name) -> name.toLowerCase().endsWith(".pdf"));
        fd.setVisible(true);

        if (fd.getFile() == null) {
            return;
        }
        File archivo = new File(fd.getDirectory(), fd.getFile());
        if (!archivo.getName().toLowerCase().endsWith(".pdf")) {
            archivo = new File(archivo.getParentFile(), archivo.getName() + ".pdf");
        }

        try {
            escribirPdf(archivo, reporte, filas != null ? filas : List.of());
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
                Desktop.getDesktop().open(archivo);
            } else {
                JOptionPane.showMessageDialog(parent,
                        "Reporte guardado en:\n" + archivo.getAbsolutePath(),
                        "PDF generado",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(parent,
                    "No se pudo generar el PDF: " + ex.getMessage(),
                    "Error al exportar",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void escribirPdf(File archivo, Reporte r, List<String[]> filas) throws Exception {
        Document doc = new Document(PageSize.LETTER, 36, 36, 42, 36);
        try (FileOutputStream fos = new FileOutputStream(archivo)) {
            PdfWriter.getInstance(doc, fos);
            doc.open();

            Font fTitulo = new Font(Font.HELVETICA, 18, Font.BOLD, new Color(0x111827));
            Font fSub = new Font(Font.HELVETICA, 10, Font.NORMAL, new Color(0x6B7280));
            Font fMeta = new Font(Font.HELVETICA, 9, Font.NORMAL, new Color(0x374151));
            Font fHead = new Font(Font.HELVETICA, 9, Font.BOLD, Color.WHITE);
            Font fCell = new Font(Font.HELVETICA, 9, Font.NORMAL, new Color(0x1F2937));

            Paragraph titulo = new Paragraph("Sistema de Gestión de Pólizas · " + r.tipo(), fTitulo);
            titulo.setSpacingAfter(4);
            doc.add(titulo);

            Paragraph sub = new Paragraph(
                    "Folio: " + r.folio() + "   |   Generado: " + r.fechaGeneracion()
                    + "   |   Usuario: " + r.usuarioNombre() + " (@" + r.username() + ")",
                    fSub);
            sub.setSpacingAfter(6);
            doc.add(sub);

            Paragraph params = new Paragraph(
                    "Periodo: " + r.rangoFechas() + "   |   Filtros: " + r.parametrosFiltros(),
                    fMeta);
            params.setSpacingAfter(14);
            doc.add(params);

            String[] columnas = columnasPorTipo(r.tipo());
            PdfPTable tabla = new PdfPTable(columnas.length);
            tabla.setWidthPercentage(100);
            tabla.setHeaderRows(1);

            for (String col : columnas) {
                PdfPCell cell = new PdfPCell(new Phrase(col, fHead));
                cell.setBackgroundColor(new Color(0x193CB8));
                cell.setPadding(7);
                cell.setBorderColor(new Color(0xD1D5DB));
                tabla.addCell(cell);
            }

            for (int i = 0; i < filas.size(); i++) {
                String[] fila = filas.get(i);
                Color bg = (i % 2 == 1) ? new Color(0xF9FAFB) : Color.WHITE;
                for (int c = 0; c < fila.length; c++) {
                    PdfPCell cell = new PdfPCell(new Phrase(fila[c], fCell));
                    cell.setBackgroundColor(bg);
                    cell.setPadding(6);
                    cell.setBorderColor(new Color(0xE5E7EB));
                    if (c == fila.length - 2 && fila[c] != null && fila[c].startsWith("$")) {
                        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    }
                    tabla.addCell(cell);
                }
            }
            doc.add(tabla);

            Paragraph pie = new Paragraph(
                    "\nTotal de registros incluidos en el reporte: " + filas.size(),
                    fSub);
            doc.add(pie);
        } finally {
            if (doc.isOpen()) {
                doc.close();
            }
        }
    }

    static String[] columnasPorTipo(String tipo) {
        return switch (tipo) {
            case "Cobranza y Pagos" -> new String[]{"Cuota", "Póliza / Cliente", "Forma pago", "Monto", "Estado"};
            case "Cancelaciones" -> new String[]{"Póliza", "Cliente", "Motivo", "Devolución", "Solicitud"};
            default -> new String[]{"Póliza", "Cliente / Vehículo", "Aseguradora", "Prima", "Estado"};
        };
    }
}
