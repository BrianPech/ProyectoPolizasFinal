package com.mycompany.proyectopolizasfinal.controlador;

import com.mycompany.proyectopolizasfinal.modelo.MovimientoPago;
import com.mycompany.proyectopolizasfinal.repositorio.RepositorioCrud;
import java.util.List;

/**
 * Controlador del módulo de Pagos y Avisos.
 */
public class ControladorPagos extends ControladorListado<MovimientoPago> {

    public ControladorPagos(RepositorioCrud<MovimientoPago> repositorio) {
        super(repositorio);
    }

    public List<MovimientoPago> filtrar(boolean soloPendientes, boolean soloVencidos, boolean soloAvisos,
            String tipoFiltro, String estadoFiltro, String texto) {
        return filtrar(m -> {
            if (soloPendientes && !("Pago / Cuota".equals(m.tipo()) && "Pendiente".equalsIgnoreCase(m.estado()))) {
                return false;
            }
            if (soloVencidos && !"Vencido".equalsIgnoreCase(m.estado())) {
                return false;
            }
            if (soloAvisos && !"Aviso".equals(m.tipo())) {
                return false;
            }
            if (tipoFiltro != null && !"Todos los tipos".equals(tipoFiltro) && !tipoFiltro.equals(m.tipo())) {
                return false;
            }
            if (estadoFiltro != null && !"Estado".equals(estadoFiltro)
                    && !estadoFiltro.equalsIgnoreCase(m.estado())) {
                return false;
            }
            return contieneTexto(texto, m.concepto(), m.polizaCliente(), m.canal(), m.detalleCanal());
        });
    }

    public MovimientoPago anular(MovimientoPago movimiento) {
        return actualizar(movimiento.conEstado("Cancelado"));
    }
}
