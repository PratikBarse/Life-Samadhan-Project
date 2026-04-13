package com.lifesamadhan.api.repository;

import com.lifesamadhan.api.model.ProviderType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProviderTypeRepository extends JpaRepository<ProviderType, Long> {
    Optional<ProviderType> findByName(String name);

    List<ProviderType> findByStatus(String status);
}
