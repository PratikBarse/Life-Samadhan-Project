package com.lifesamadhan.api.scheduler;

import com.lifesamadhan.api.model.ServiceAssignment;
import com.lifesamadhan.api.repository.ServiceAssignmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class AssignmentCleanupScheduler {

    private final ServiceAssignmentRepository serviceAssignmentRepository;

    @Scheduled(fixedRate = 60000) 
    @Transactional
    public void cancelStaleAssignments() {
        
        
        LocalDateTime cutoffTime = LocalDateTime.now(java.time.ZoneId.of("Asia/Kolkata")).minusMinutes(30);

        List<ServiceAssignment> staleAssignments = serviceAssignmentRepository
                .findByStatusAndAssignedAtBefore("ASSIGNED", cutoffTime);

        if (!staleAssignments.isEmpty()) {
            log.info("Found {} stale assignments to cancel", staleAssignments.size());

            for (ServiceAssignment assignment : staleAssignments) {
                log.info("Auto-cancelling assignment ID: {} (Assigned At: {})",
                        assignment.getId(), assignment.getAssignedAt());

                assignment.setStatus("CANCELLED");
                
            }

            serviceAssignmentRepository.saveAll(staleAssignments);
        }
    }
}
