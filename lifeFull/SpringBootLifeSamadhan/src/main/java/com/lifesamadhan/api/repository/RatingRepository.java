package com.lifesamadhan.api.repository;

import com.lifesamadhan.api.model.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {
    List<Rating> findByAssignmentId(Long assignmentId);

    @org.springframework.data.jpa.repository.Query("SELECT AVG(r.stars) FROM Rating r JOIN ServiceAssignment sa ON r.assignmentId = sa.id WHERE sa.providerId = :providerId")
    Double getAverageRatingForProvider(Long providerId);

    @org.springframework.data.jpa.repository.Query("SELECT COUNT(r) FROM Rating r JOIN ServiceAssignment sa ON r.assignmentId = sa.id WHERE sa.providerId = :providerId")
    Long getReviewCountForProvider(Long providerId);
}