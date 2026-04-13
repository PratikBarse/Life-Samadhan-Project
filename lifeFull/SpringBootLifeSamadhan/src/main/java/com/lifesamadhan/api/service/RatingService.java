package com.lifesamadhan.api.service;

import com.lifesamadhan.api.model.Rating;
import com.lifesamadhan.api.model.ServiceAssignment;
import com.lifesamadhan.api.repository.RatingRepository;
import com.lifesamadhan.api.repository.ServiceAssignmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RatingService {
    
    private final RatingRepository ratingRepository;
    private final ServiceAssignmentRepository serviceAssignmentRepository;
    
    public Rating submitRating(Rating rating) {
        ServiceAssignment assignment = serviceAssignmentRepository.findById(rating.getAssignmentId())
                .orElseThrow(() -> new RuntimeException("Invalid Assignment"));
        
        if (!"COMPLETED".equals(assignment.getStatus())) {
            throw new RuntimeException("Rating allowed only after service completion");
        }
        
        
        boolean alreadyRated = ratingRepository.findByAssignmentId(rating.getAssignmentId())
                .stream().findAny().isPresent();
        
        if (alreadyRated) {
            throw new RuntimeException("Rating already submitted for this service");
        }
        
        return ratingRepository.save(rating);
    }
}