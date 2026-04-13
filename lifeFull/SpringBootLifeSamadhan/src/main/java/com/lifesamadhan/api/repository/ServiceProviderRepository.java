package com.lifesamadhan.api.repository;

import com.lifesamadhan.api.model.ServiceProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ServiceProviderRepository extends JpaRepository<ServiceProvider, Long> {
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = { "user" })
    java.util.Optional<ServiceProvider> findById(Long id);

    Optional<ServiceProvider> findByUserId(Long userId);
}