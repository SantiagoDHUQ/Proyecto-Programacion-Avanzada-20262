package org.uniquindio.proyectoavanzadacompuparts.infrastructure.persistence.jpa.dto;

import java.math.BigDecimal;

/**
 * DTO para proyectar los resultados de reportes de ventas agrupados por vendedor.
 */
public record VentasPorVendedorDTO(
        String vendedorId,
        BigDecimal totalVentas
) {}

