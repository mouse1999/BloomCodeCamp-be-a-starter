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

import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

@Service // Marks this class as a Spring service component
public class AssignmentClaimingService {

    private final BlockingQueue<Assignment> unclaimedAssignments;

    private final ConcurrentMap<Long, ReentrantLock> assignmentLocks = new ConcurrentHashMap<>();
    private static final Logger logger = LoggerFactory.getLogger(AssignmentClaimingService.class);
    private final AssignmentRepository assignmentRepository;

    /**
     * Constructor for Dependency Injection. Spring will automatically inject
     * AssignmentRepository and UserService instances.
     *
     * @param assignmentRepository The JPA repository for Assignment entities.
     */
    @Autowired // Ensures Spring uses this constructor for dependency injection
    public AssignmentClaimingService(AssignmentRepository assignmentRepository) {

        this.unclaimedAssignments = new LinkedBlockingQueue<>();

        this.assignmentRepository = assignmentRepository;

    }

    /**
     * This method is annotated with @PostConstruct, meaning it will be executed
     * automatically by Spring after the AssignmentClaimingService bean has been
     * initialized and all its dependencies have been injected.
     * This is the ideal place to load assignments from the database on application startup.
     */
    @PostConstruct
    private void initializeServiceOnStartup() {
        loadUnclaimedAssignmentsFromDatabase();
    }

    /**
     * Helper method to add an assignment to the internal queue.
     * Used by loadUnclaimedAssignmentsFromDatabase.
     * @param item The assignment to add.
     */
    private void addAssignmentToQueueInternal(Assignment item) {
        try {
            unclaimedAssignments.put(item);
        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();
            throw new RuntimeException("Failed to add assignment to queue during startup", e);
        }
    }

    /**
     * Fetches all SUBMITTED assignments from the database and loads them into
     * the unclaimedAssignments queue. This ensures persistence across shutdowns/restarts.
     * This method is typically called once on application startup.
     */
    private void loadUnclaimedAssignmentsFromDatabase() {
        try {
            List<Assignment> submittedAssignments = assignmentRepository.findAllByStatus(AssignmentStatusEnum.SUBMITTED);

            if (submittedAssignments.isEmpty()) {
                logger.info("No SUBMITTED assignments found to load");
                return;
            }

            logger.info("Loading {} SUBMITTED assignments into queue", submittedAssignments.size());
            submittedAssignments.forEach(this::addAssignmentToQueueInternal);
            logger.info("Queue loaded successfully. Current size: {}", unclaimedAssignments.size());

        } catch (Exception e) {
            logger.error("Failed to load unclaimed assignments from database", e);
        }
    }

    /**
     * PUBLIC API: Allows a reviewer to claim the next available assignment from the queue.
     * This method is designed to be called by an external mechanism, e.g., a REST controller
     * when a reviewer requests to claim an assignment.
     *
     * @param reviewer The user (reviewer) attempting to claim an assignment.
     * @return The claimed Assignment object with updated status and assigned reviewer,
     * or {@code null} if no assignment could be claimed or processed.
     */
    public Optional<Assignment> claimNextAvailableAssignment(User reviewer) {

        try {
            return Optional.ofNullable(unclaimedAssignments.poll(100, TimeUnit.MILLISECONDS))
                    .map(assignment -> {
                        try {
                            return handleAssignmentClaimInternal(assignment, reviewer).orElse(null);
                        } catch (Exception e) {
                            logger.error("Failed to process assignment {} for reviewer {}",
                                    assignment.getId(), reviewer.getId(), e);
                            unclaimedAssignments.offer(assignment); // Requeue on failure
                            return null;
                        }
                    });

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warn("Claiming process interrupted for reviewer {}", reviewer.getId(), e);
            return Optional.empty();
        }
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
                .map(updatedAssignment -> {
                    boolean removed = unclaimedAssignments.remove(assignment);
                    logger.debug("Assignment {} {} from unclaimed queue",
                            assignment.getId(), removed ? "removed" : "not found");
                    return Converter.toAssignmentModel(updatedAssignment);
                });
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
                unclaimedAssignments.offer(assignment);
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
            unclaimedAssignments.offer(assignment);
            return Optional.empty();
        } catch (Exception e) {
            logger.error("Error processing assignment {}", assignmentId, e);
            unclaimedAssignments.offer(assignment);
            return Optional.empty();
        }

    }

    private Optional<Assignment> processValidAssignment(Assignment assignment, User reviewer) {
        assignment.setStatus(AssignmentStatusEnum.IN_REVIEW);
        assignment.setCodeReviewer(reviewer);
        Assignment savedAssignment = assignmentRepository.save(assignment);

        logger.info("Assignment {} claimed by reviewer {}, status updated to IN_REVIEW",
                assignment.getId(), reviewer.getId());


        return Optional.of(savedAssignment);
    }

    /**
     * Adds an assignment to the unclaimed queue if its status is SUBMITTED.
     * This method is typically called by other services (e.g., when a learner submits a new assignment).
     * @param assignment The assignment submitted by a learner.
     */
    public void addUnclaimedAssignment(Assignment assignment) {
        if (assignment.getStatus() == AssignmentStatusEnum.SUBMITTED) {
            unclaimedAssignments.offer(assignment); // offer() is non-blocking and preferred for producers
            System.out.println("Assignment " + assignment.getId() + " added to unclaimed queue by addUnclaimedAssignment method.");
        } else {
            System.out.println("Assignment " + assignment.getId() + " is not in SUBMITTED status. Not adding to queue.");
        }
    }

}
