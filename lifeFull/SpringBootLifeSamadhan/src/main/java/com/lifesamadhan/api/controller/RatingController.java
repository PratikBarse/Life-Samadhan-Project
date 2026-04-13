package com.lifesamadhan.api.controller;

import com.lifesamadhan.api.model.Rating;
import com.lifesamadhan.api.service.RatingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/rating")
@RequiredArgsConstructor
public class RatingController {
    
    private final RatingService ratingService;
    
    @PostMapping("/submit")
    public ResponseEntity<?> submitRating(@Valid @RequestBody Rating rating) {
        try {
            Rating savedRating = ratingService.submitRating(rating);
            return ResponseEntity.ok(savedRating);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}