package org.uniquindio.proyectoavanzadacompuparts.domain.service;

import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Build;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Compra;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Componente;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.ReservaPreventa;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.UnidadInventario;
import org.uniquindio.proyectoavanzadacompuparts.domain.exception.ReglaDominioException;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.EstadoBuild;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.EstadoCompra;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.ItemCompra;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.uniquindio.proyectoavanzadacompuparts.domain.exception.ReglaDominioException.exigir;

/**
 * Servicio de dominio para operaciones de compra que cruzan agregados.
 * <p>
 * Encapsula la lógica de compra, confirmación, cancelación y reembolso
 * coordinando Build, Compra, Componente, ReservaPreventa y UnidadInventario.
 * <p>
 * Implementa "verificar todo, luego aplicar todo" para atomicidad.
 */
public final class CompraService {

    private final Clock clock;

    public CompraService(Clock clock) {
        this.clock = clock;
    }

    // =========================================================================
    // COMPRAS
    // =========================================================================

    /**
     * Realiza una compra a partir de un Build.
     * <p>
     * Verifica todo antes de mutar:
     * - Build existe, está LISTO_PARA_COMPRA y tiene stock en todos sus componentes
     * Luego reserva unidades, crea items y la compra PENDIENTE.
     */
    public Compra realizarDeBuild(Build build, String compradorId) {
        // --- VERIFICAR TODO ---
        build.validarListoParaComprar();

        List<ItemCompra> items = new ArrayList<>();
        List<Componente> componentesReservados = new ArrayList<>();

        for (Componente componente : build.getComponentes()) {
            UnidadInventario unidad = componente.reservarPrimeraUnidadDisponible();
            items.add(new ItemCompra(
                    unidad.getId(),
                    componente.getId(),
                    unidad.getNumeroSerie(),
                    componente.getPrecio()
            ));
        }

        // --- APLICAR TODO ---
        Compra compra = Compra.realizar(
                UUID.randomUUID().toString(),
                build.getId(),
                compradorId,
                items
        );

        return compra;
    }

    /**
     * Realiza una compra directa de un Componente (sin Build).
     */
    public Compra realizarDeComponente(Componente componente, String compradorId) {
        // --- VERIFICAR TODO ---
        UnidadInventario unidad = componente.reservarPrimeraUnidadDisponible();
        exigir(unidad != null, "El componente no tiene stock disponible");

        ItemCompra item = new ItemCompra(
                unidad.getId(),
                componente.getId(),
                unidad.getNumeroSerie(),
                componente.getPrecio()
        );

        Compra compra = Compra.realizar(
                UUID.randomUUID().toString(),
                null, // sin Build
                compradorId,
                List.of(item)
        );

        return compra;
    }

    /**
     * Realiza una compra a partir de una reserva de preventa convertida.
     */
    public Compra realizarDeReserva(ReservaPreventa reserva, Componente componente, String compradorId) {
        UnidadInventario unidad = componente.unidadPorId(reserva.getUnidadId());
        unidad.validarParaComprarReservaPreventa();
        unidad.validarReservaPreventaCoincide(reserva.getId());

        ItemCompra item = new ItemCompra(
                unidad.getId(),
                componente.getId(),
                unidad.getNumeroSerie(),
                componente.getPrecio()
        );

        Compra compra = Compra.realizar(
                UUID.randomUUID().toString(),
                null, // sin Build
                compradorId,
                List.of(item)
        );

        reserva.iniciarCompra();
        return compra;
    }

    // =========================================================================
    // CONFIRMACIÓN / CANCELACIÓN / REEMBOLSO
    // =========================================================================

    /**
     * Confirma una compra PENDIENTE.
     * <p>
     * Verifica todo antes de mutar:
     * - Compra está PENDIENTE
     * - Todas las unidades están RESERVADAS
     * - Si hay Build asociado, está LISTO_PARA_COMPRA
     * Luego vende las unidades, marca la compra COMPLETADA, marca Build COMPRADO,
     * convierte reservas EN_COMPRA a CONVERTIDA.
     */
    public void confirmar(Compra compra, Build build, List<Componente> componentes,
                          List<ReservaPreventa> reservasVigentes) {
        // --- VERIFICAR TODO ---
        compra.validarConfirmar();

        if (compra.getBuildId() != null) {
            exigir(build != null, "Build no encontrado para la compra");
            build.validarMarcarComoComprado();
        }

        for (ItemCompra item : compra.getItems()) {
            Componente componente = componentes.stream()
                    .filter(c -> c.getId().equals(item.componenteId()))
                    .findFirst()
                    .orElseThrow(() -> new ReglaDominioException("Componente no encontrado: " + item.componenteId()));

            UnidadInventario unidad = componente.unidadPorId(item.unidadId());
            unidad.validarEstaReservada();
        }

        // --- APLICAR TODO ---
        for (ItemCompra item : compra.getItems()) {
            Componente componente = componentes.stream()
                    .filter(c -> c.getId().equals(item.componenteId()))
                    .findFirst()
                    .orElseThrow();
            componente.venderUnidad(item.unidadId());
        }

        compra.confirmar();

        // Convertir reservas EN_COMPRA a CONVERTIDA
        for (ReservaPreventa reserva : reservasVigentes) {
            if (reserva.estaEnCompra()) {
                reserva.convertir();
            }
        }

        if (compra.getBuildId() != null && build != null) {
            build.marcarComoComprado();
        }
    }

    /**
     * Cancela una compra PENDIENTE.
     * <p>
     * Verifica que la compra esté PENDIENTE.
     * Libera las reservas de compra y reactiva reservas EN_COMPRA a ACTIVA.
     */
    public void cancelar(Compra compra, List<Componente> componentes,
                         List<ReservaPreventa> reservasVigentes) {
        // --- VERIFICAR TODO ---
        compra.validarCancelar();

        for (ItemCompra item : compra.getItems()) {
            Componente componente = componentes.stream()
                    .filter(c -> c.getId().equals(item.componenteId()))
                    .findFirst()
                    .orElseThrow(() -> new ReglaDominioException("Componente no encontrado: " + item.componenteId()));

            UnidadInventario unidad = componente.unidadPorId(item.unidadId());
            unidad.validarEstaReservada();
        }

        // --- APLICAR TODO ---
        for (ItemCompra item : compra.getItems()) {
            Componente componente = componentes.stream()
                    .filter(c -> c.getId().equals(item.componenteId()))
                    .findFirst()
                    .orElseThrow();
            componente.liberarReservaCompra(item.unidadId());
        }

        compra.cancelar();

        // Reactivar reservas EN_COMPRA a ACTIVA
        for (ReservaPreventa reserva : reservasVigentes) {
            if (reserva.estaEnCompra()) {
                reserva.volverAActiva();
            }
        }
    }

    /**
     * Reembolsa una compra COMPLETADA.
     * <p>
     * Verifica todo antes de mutar:
     * - Compra está COMPLETADA
     * - Comprador es el dueño
     * - Motivo no vacío
     * - Todas las unidades tienen RMA ACEPTADA y ninguna PENDIENTE
     * Luego descarta las unidades y marca la compra REEMBOLSADA.
     */
    public void reembolsar(Compra compra, String compradorId, String motivo,
                           List<Componente> componentes,
                           List<String> seriesConRmaPendiente,
                           List<String> seriesConRmaAceptada) {
        // --- VERIFICAR TODO ---
        compra.validarReembolso(compradorId, motivo);

        for (ItemCompra item : compra.getItems()) {
            String numeroSerie = item.numeroSerie();
            if (seriesConRmaPendiente.contains(numeroSerie)) {
                throw new ReglaDominioException("La unidad " + numeroSerie + " tiene una SolicitudRMA pendiente");
            }
            if (!seriesConRmaAceptada.contains(numeroSerie)) {
                throw new ReglaDominioException("La unidad " + numeroSerie + " no tiene una SolicitudRMA aceptada");
            }
        }

        // --- APLICAR TODO ---
        for (ItemCompra item : compra.getItems()) {
            Componente componente = componentes.stream()
                    .filter(c -> c.getId().equals(item.componenteId()))
                    .findFirst()
                    .orElseThrow(() -> new ReglaDominioException("Componente no encontrado: " + item.componenteId()));
            componente.descartarUnidad(item.unidadId());
        }

        compra.solicitarReembolso(motivo);
    }
}
