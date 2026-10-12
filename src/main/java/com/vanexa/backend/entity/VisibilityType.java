
package com.vanexa.backend.entity;

import jakarta.persistence.*;

@Entity
@Table(
        name = "visibility_types",
        uniqueConstraints = @UniqueConstraint(columnNames = "name")
)
public class VisibilityType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 30, unique = true)
    private String name;

    public VisibilityType() {
    }

    public VisibilityType(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
