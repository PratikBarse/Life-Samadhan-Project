package com.lifesamadhan.api.repository;

import com.lifesamadhan.api.model.ProviderSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProviderSkillRepository extends JpaRepository<ProviderSkill, Long> {
    List<ProviderSkill> findByProviderId(Long providerId);

    List<ProviderSkill> findByServiceId(Long serviceId);

    List<ProviderSkill> findByStatus(String status);

    List<ProviderSkill> findByServiceIdAndStatus(Long serviceId, String status);
}