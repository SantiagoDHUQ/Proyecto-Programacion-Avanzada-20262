package org.uniquindio.proyectoavanzadacompuparts.domain.factory;

import java.math.BigDecimal;

/** Datos de entrada del dominio para registrar un Componente (valores crudos: texto y números). */
public record DatosComponente(String id, String nombre, String categoria, String socket,
                              int wattajeRequerido, String tipoMemoria,
                              BigDecimal precio, String moneda, Integer duracionGarantia) {
}