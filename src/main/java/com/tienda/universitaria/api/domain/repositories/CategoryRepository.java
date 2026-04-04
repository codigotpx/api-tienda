package com.tienda.universitaria.api.domain.repositories;

import com.tienda.universitaria.api.domain.entities.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {

    boolean existsByName(String name);

    Optional<Category> findByName(String name);
}
