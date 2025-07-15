package com.hcc.services;

import com.hcc.converter.Converter;
import com.hcc.entities.Assignment;
import com.hcc.entities.User;
import com.hcc.enums.AssignmentStatusEnum;
import com.hcc.models.AssignmentModel;
import com.hcc.repositories.AssignmentRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class AssignmentClaimingService {


    private final ConcurrentMap<Long, ReentrantLock> assignmentLocks = new ConcurrentHashMap<>();
    private static final Logger logger = LoggerFactory.getLogger(AssignmentClaimingService.class);
    private final AssignmentRepository assignmentRepository;

    /**
     * Constructor for Dependency Injection. Spring will automatically inject
     * AssignmentRepository and UserService instances.
     *
     * @param assignmentRepository The JPA repository for Assignment entities.
     */
    @Autowired
    public AssignmentClaimingService(AssignmentRepository assignmentRepository) {

        this.assignmentRepository = assignmentRepository;

    }
    public Optional<AssignmentModel> claimSpecificAssignment(Long assignmentId, User reviewer) {

        logger.info("Reviewer {} attempting to claim specific assignment {}", reviewer.getId(), assignmentId);

        return assignmentRepository.findById(assignmentId)
                .map(assignment -> processSpecificAssignment(assignment, reviewer))
                .orElseGet(() -> {
                    logger.info("Assignment {} not found", assignmentId);
                    return Optional.empty();
                });
    }

    private Optional<AssignmentModel> processSpecificAssignment(Assignment assignment, User reviewer) {
        if (assignment.getStatus() != AssignmentStatusEnum.SUBMITTED) {
            logger.info("Assignment {} not SUBMITTED (current status: {})",
                    assignment.getId(), assignment.getStatus());
            return Optional.empty();
        }

        return handleAssignmentClaimInternal(assignment, reviewer)
                .map(Converter::toAssignmentModel
                );
    }
    /**
     * INTERNAL METHOD: Handles the core logic for claiming a specific assignment,
     * including per-assignment locking, re-fetching current state, updating status and reviewer,
     * and persisting changes to the database.
     * This method ensures thread-safety for individual assignment updates.
     *
     * @param assignment The assignment entity (potentially stale) taken from the queue.
     * @param reviewer The user (reviewer) who will be assigned to this assignment.
     * @return The updated and saved Assignment entity if successful, {@code null} otherwise.
     */
    private Optional<Assignment> handleAssignmentClaimInternal(Assignment assignment, User reviewer) {
        final Long assignmentId = assignment.getId();
        final Long reviewerId = reviewer.getId();

        ReentrantLock lock = assignmentLocks.computeIfAbsent(assignmentId, id -> new ReentrantLock());

        logger.debug("Attempting to acquire lock for assignment {} by reviewer {}", assignmentId, reviewerId);

        try {
            if (!lock.tryLock(5, TimeUnit.SECONDS)) {
                logger.warn("Failed to acquire lock for assignment {} (timeout)", assignmentId);
                return Optional.empty();
            }

            try {
                logger.debug("Lock acquired for assignment {} by reviewer {}", assignmentId, reviewerId);
                return assignmentRepository.findById(assignmentId)
                        .filter(a -> a.getStatus() == AssignmentStatusEnum.SUBMITTED)
                        .map(a -> processValidAssignment(a, reviewer))
                        .orElseGet(() -> {
                            logger.info("Assignment {} not SUBMITTED or not found", assignmentId);
                            return Optional.empty();
                        });
            } finally {
                lock.unlock();
                assignmentLocks.remove(assignmentId);
                logger.debug("Lock released for assignment {} by reviewer {}", assignmentId, reviewerId);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warn("Interrupted while acquiring lock for assignment {}", assignmentId, e);

            return Optional.empty();
        } catch (Exception e) {
            logger.error("Error processing assignment {}", assignmentId, e);
            return Optional.empty();
        }

    }

    private Optional<Assignment> processValidAssignment(Assignment assignment, User reviewer) {
        assignment.setStatus(AssignmentStatusEnum.IN_REVIEW);
        assignment.setCodeReviewer(reviewer);
        assignment.setReviewedAt(Instant.now());
        Assignment savedAssignment = assignmentRepository.save(assignment);

        logger.info("Assignment {} claimed by reviewer {}, status updated to IN_REVIEW",
                assignment.getId(), reviewer.getId());


        return Optional.of(savedAssignment);
    }


}
