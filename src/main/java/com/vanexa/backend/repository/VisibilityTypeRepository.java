
package com.vanexa.backend.repository;

import com.vanexa.backend.entity.VisibilityType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VisibilityTypeRepository
        extends JpaRepository<VisibilityType, Long> {

    Optional<VisibilityType> findByName(String name);

    boolean existsByName(String name);
}
