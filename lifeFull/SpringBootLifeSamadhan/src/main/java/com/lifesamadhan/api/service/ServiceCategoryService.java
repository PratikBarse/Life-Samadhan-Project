package com.lifesamadhan.api.service;

import com.lifesamadhan.api.model.ServiceCategory;
import com.lifesamadhan.api.repository.ServiceCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ServiceCategoryService {
    
    private final ServiceCategoryRepository serviceCategoryRepository;
    
    public List<ServiceCategory> getAllCategories() {
        return serviceCategoryRepository.findAll();
    }
    
    public Optional<ServiceCategory> getCategoryById(Long id) {
        return serviceCategoryRepository.findById(id);
    }
    
    public List<ServiceCategory> getCategoriesByStatus(String status) {
        return serviceCategoryRepository.findByStatus(status);
    }
    
    public ServiceCategory createCategory(ServiceCategory category) {
        return serviceCategoryRepository.save(category);
    }
    
    public ServiceCategory updateCategory(Long id, ServiceCategory category) {
        category.setId(id);
        return serviceCategoryRepository.save(category);
    }
    
    public void deleteCategory(Long id) {
        serviceCategoryRepository.deleteById(id);
    }
    
    public boolean existsById(Long id) {
        return serviceCategoryRepository.existsById(id);
    }
}