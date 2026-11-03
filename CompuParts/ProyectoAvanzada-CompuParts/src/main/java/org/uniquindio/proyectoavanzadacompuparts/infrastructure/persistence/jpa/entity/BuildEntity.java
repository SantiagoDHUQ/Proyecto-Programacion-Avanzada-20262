package org.uniquindio.proyectoavanzadacompuparts.infrastructure.persistence.jpa.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.EstadoBuild;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "builds")
@Getter
@Setter
@NoArgsConstructor
public class BuildEntity {

    @Id
    private UUID id;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "build_componentes", joinColumns = @JoinColumn(name = "build_id"))
    @Column(name = "componente_id")
    private List<String> componenteIds = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EstadoBuild estado;
}
