package com.hcc.services;

import com.hcc.converter.Converter;
import com.hcc.entities.Assignment;
import com.hcc.entities.User;
import com.hcc.enums.AssignmentEnum;
import com.hcc.enums.AssignmentStatusEnum;
import com.hcc.exceptions.assignmentexceptions.*;

import com.hcc.models.AssignmentModel;
import com.hcc.repositories.AssignmentRepository;
import com.hcc.repositories.UserRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Service
public class AssignmentService  {


    @Autowired
    private final AssignmentRepository assignmentRepository;
    private static final String GITHUB_URL_REGEX = "^(https?://)?(www\\.)?github\\.com/.+";
    private static final Logger logger = LoggerFactory.getLogger(AssignmentService.class);

    public AssignmentService(AssignmentRepository assignmentRepository) {
        this.assignmentRepository = assignmentRepository;

    }

    @Transactional
    public AssignmentModel createAssignment(Integer assignmentNumber, User creator) {
        Long creatorId = creator.getId();
        logger.debug("Attempting to create assignment {} for user {}", assignmentNumber, creatorId);

        if (assignmentRepository.existsByAssignmentNumberAndUserId(assignmentNumber, creatorId)) {
            logger.error("Assignment {} already exists for user {}", assignmentNumber, creatorId);
            throw new AssignmentAlreadySubmittedException("Assignment " + assignmentNumber + " already exists for user " + creatorId);
        }

        Assignment assignment = Assignment.builder()
                .assignmentNumber(assignmentNumber)
                .user(creator)
                .assignmentType(AssignmentEnum.fromAssignmentNumber(assignmentNumber))
                .status(AssignmentStatusEnum.PENDING_SUBMISSION)
                .createdAt(Instant.now())
                .build();

        Assignment savedAssignment = assignmentRepository.save(assignment);
        logger.info("Created assignment ID {} for user {}", savedAssignment.getId(), creatorId);
        return Converter.toAssignmentModel(savedAssignment);
    }

    public AssignmentModel getAssignmentByIdAndUserId(Long id, Long userId) {
        logger.debug("Fetching assignment ID {} for user {}", id, userId);

        return assignmentRepository.findByIdAndUserId(id, userId)
                .map(assignment -> {
                    logger.debug("Found assignment ID {} for user {}", id, userId);
                    return Converter.toAssignmentModel(assignment);
                })
                .orElseThrow(() -> {
                    logger.warn("Assignment ID {} not found for user {}", id, userId);
                    return new AssignmentNotFoundException(
                            "Assignment %d not found for user %d".formatted(id, userId));
                });
    }

    public AssignmentModel getAssignmentById(Long assignmentId) {
        return assignmentRepository.findById(assignmentId)
                .map(Converter::toAssignmentModel)
                .orElseThrow(() -> new AssignmentNotFoundException("Assignment not found"));
    }

    public boolean existsAssignmentByNumber(Integer number, Long userId) {
        if (number == null) {
            throw new InvalidAssignmentNumberException("Assignment number must not be null");
        }
        return assignmentRepository.existsByAssignmentNumberAndUserId(number, userId);
    }



    public List<AssignmentModel> getAssignmentsByStatusAndUserId(String status, Long userId) {
        logger.debug("Fetching assignments for user {} with status: {}", userId, status);

        List<AssignmentModel> assignments;

        if (status == null || status.isEmpty()) {
            assignments = assignmentRepository.findAllByUserIdOrderByCreatedAtDesc(userId).stream()
                    .map(Converter::toAssignmentModel)
                    .collect(Collectors.toList());;
            logger.info("Fetched all {} assignments for user {}", assignments.size(), userId);
        } else {
            AssignmentStatusEnum statusEnum = parseAssignmentStatus(status);
            assignments = assignmentRepository.findAllByStatusAndUserId(statusEnum, userId)
                    .stream()
                    .map(Converter::toAssignmentModel)
                    .collect(Collectors.toList());
            ;
            logger.info("Fetched {} {} assignments for user {}", assignments.size(), statusEnum, userId);
        }

        return assignments;
    }

    public List<AssignmentModel> getAssignmentsByStatusAndReviewerId(String status, Long reviewerId) {
        logger.debug("Fetching assignments for reviewer {} with status: {}", reviewerId, status);

        List<Assignment> assignments;

        if (status == null || status.isEmpty()) {
            assignments = assignmentRepository.findAllByCodeReviewerIdOrderByReviewedAtDesc(reviewerId);
            logger.info("Fetched all {} assignments for reviewer {}", assignments.size(), reviewerId);
        } else {
            AssignmentStatusEnum statusEnum = parseAssignmentStatus(status);
            assignments = assignmentRepository
                    .findAllByStatusAndCodeReviewerIdOrderByReviewedAtDesc(statusEnum, reviewerId);
            logger.info("Fetched {} {} assignments for reviewer {}", assignments.size(), statusEnum, reviewerId);
        }

        return assignments.stream()
                .map(Converter::toAssignmentModel)
                .collect(Collectors.toList());
    }

    private List<AssignmentModel> getAssignmentsByUserId(Long userId) {
        return assignmentRepository.findAllByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(Converter::toAssignmentModel)
                .collect(Collectors.toList());
    }


    public List<AssignmentModel> getAssignmentsByStatus(String status) {
        logger.debug("Fetching assignments by status: {}", status);

        List<Assignment> assignments;

        if (status.isEmpty()) {
            assignments = assignmentRepository.findAll();
            logger.info("Fetched all assignments (count: {})", assignments.size());
        } else {
            AssignmentStatusEnum statusEnum = parseAssignmentStatus(status);
            assignments = assignmentRepository.findAllByStatus(statusEnum);
            logger.info("Fetched {} assignments with status: {}", assignments.size(), statusEnum);
        }

        return assignments.stream()
                .map(Converter::toAssignmentModel)
                .collect(Collectors.toList());
    }

    // Helper method for status parsing
    private AssignmentStatusEnum parseAssignmentStatus(String status) {
        try {
            return AssignmentStatusEnum.fromStatus(status);
        } catch (InvalidAssignmentStatusException e) {
            logger.warn("Invalid status parameter provided: {}", status);
            return null;
        }
    }
    //-------------------------------------------------------------------------------



    public AssignmentModel submitOrEditAssignment(Long assignmentId, String branchName, String githubUrl, Long userId) {
        logger.debug("Submitting assignment ID {} by user {}", assignmentId, userId);

        if (githubUrl == null || !githubUrl.matches(GITHUB_URL_REGEX)) {
            throw new InvalidGithubUrlException("Invalid GitHub URL");
        }
        if (branchName == null || branchName.trim().isEmpty()) {
            throw new IllegalArgumentException("Branch can not be empty");
        }

        Assignment assignment = assignmentRepository.findByIdAndUserId(assignmentId, userId)
                .filter(assign -> {
                    boolean isValidStatus = assign.getStatus().equals(AssignmentStatusEnum.PENDING_SUBMISSION) ||
                            assign.getStatus().equals(AssignmentStatusEnum.NEEDS_UPDATE);
                    if (!isValidStatus) {
                        logger.warn("Invalid status change attempted by user {} for assignment {}", userId, assignmentId);
                        throw new InvalidStatusChangeException("This assignment cannot be submitted");
                    }
                    return true;
                })
                .map(assign -> {
                    AssignmentStatusEnum newStatus = assign.getStatus().equals(AssignmentStatusEnum.PENDING_SUBMISSION)
                            ? AssignmentStatusEnum.SUBMITTED
                            : AssignmentStatusEnum.RESUBMITTED;
                    assign.setBranch(branchName);
                    assign.setGithubUrl(githubUrl);
                    assign.setStatus(newStatus);
                    logger.info("Assignment ID {} status updated to {}", assignmentId, newStatus);
                    return assign;
                })
                .orElseThrow(() -> {
                    logger.error("Assignment ID {} not found for user {}", assignmentId, userId);
                    return new AssignmentNotFoundException("Assignment not found with ID for this user: " + assignmentId);
                });

        return Converter.toAssignmentModel(assignmentRepository.save(assignment));
    }
    @Transactional
    public AssignmentModel getAssignmentByIdAndReviewer(Long assignmentId, Long reviewerId) {
        logger.debug("Fetching assignment ID {} for reviewer {}", assignmentId, reviewerId);

        return assignmentRepository.findByIdAndCodeReviewerId(assignmentId, reviewerId)
                .map(assignment -> {
                    logger.debug("Found assignment ID {} under reviewer {}", assignmentId, reviewerId);
                    return Converter.toAssignmentModel(assignment);
                })
                .orElseThrow(() -> {
                    logger.warn("Assignment ID {} not found or not assigned to reviewer {}", assignmentId, reviewerId);
                    return new AssignmentNotFoundException(
                            "Assignment %d not found or not under your review".formatted(assignmentId));
                });
    }



    public AssignmentModel completeReview(Long assignmentId, String reviewVideoUrl) {
        logger.debug("Completing review for assignment ID: {}", assignmentId);

        validateReviewVideoUrl(reviewVideoUrl);

        return assignmentRepository.findById(assignmentId)
                .map(assignment -> {

                    if (assignment.getStatus() != AssignmentStatusEnum.IN_REVIEW) {
                        logger.warn("Invalid status change attempt from {} to COMPLETED for assignment {}",
                                assignment.getStatus(), assignmentId);
                        throw new InvalidStatusChangeException(
                                String.format("Cannot complete assignment in %s state",
                                        assignment.getStatus()));
                    }

                    assignment.setStatus(AssignmentStatusEnum.COMPLETED);
                    assignment.setReviewVideoUrl(reviewVideoUrl);
                    assignment.setReviewedAt(Instant.now());

                    Assignment savedAssignment = assignmentRepository.save(assignment);
                    logger.info("Completed review for assignment ID: {}", assignmentId);

                    return Converter.toAssignmentModel(savedAssignment);
                })
                .orElseThrow(() -> {
                    logger.error("Assignment not found with ID: {}", assignmentId);
                    return new AssignmentNotFoundException(
                            String.format("Assignment %d not found", assignmentId));
                });
    }


    @Transactional
    public AssignmentModel requestResubmission(Long assignmentId, String reviewVideoUrl) {
        if (assignmentId == null) {
            throw new IllegalArgumentException("assignment ID is null");

        }

        validateReviewVideoUrl(reviewVideoUrl);

        logger.debug("Requesting resubmission for assignment ID: {}", assignmentId);

        return assignmentRepository.findById(assignmentId)
                .map(assignment -> {
                    validateStatusTransition(assignment, AssignmentStatusEnum.NEEDS_UPDATE);

                    updateAssignmentForResubmission(assignment, reviewVideoUrl);

                    Assignment savedAssignment = assignmentRepository.save(assignment);
                    logger.info("Requested resubmission for assignment ID: {}", assignmentId);

                    return Converter.toAssignmentModel(savedAssignment);
                })
                .orElseThrow(() -> {
                    logger.error("Assignment not found with ID: {}", assignmentId);
                    return new AssignmentNotFoundException("Assignment %d not found".formatted(assignmentId));
                });
    }

    // --- Helper Methods ---
    private void validateReviewVideoUrl(String reviewVideoUrl) {
        if (reviewVideoUrl == null || reviewVideoUrl.isBlank()) {
            throw new IllegalArgumentException("Review video URL must be provided");
        }

        if (!reviewVideoUrl.matches("^https?://.+")) {
            throw new IllegalArgumentException("Invalid video URL format");
        }
    }

    private void validateStatusTransition(Assignment assignment,
                                          AssignmentStatusEnum targetStatus) {
        if (assignment.getStatus() != AssignmentStatusEnum.IN_REVIEW) {
            logger.warn("Invalid status transition from {} to {} for assignment {}",
                    assignment.getStatus(), targetStatus, assignment.getId());
            throw new InvalidStatusChangeException(
                    "Cannot transition assignment from %s to %s"
                            .formatted(assignment.getStatus(), targetStatus));
        }
    }
    private void updateAssignmentForResubmission(Assignment assignment,
                                                 String reviewVideoUrl) {
        assignment.setStatus(AssignmentStatusEnum.NEEDS_UPDATE);
        assignment.setReviewVideoUrl(reviewVideoUrl);
    }
    //--------------------------------------------------------------------------------

    public List<AssignmentModel> getClaimedAndUnclaimedAssignmentsForReviewer(Long reviewerId) {
        if (reviewerId == null) {
            throw new IllegalArgumentException("reviewer ID is null");

        }

        Stream<Assignment> submittedAssignmentsStream = assignmentRepository
                .findAllByStatus(AssignmentStatusEnum.SUBMITTED)
                .stream();

        Stream<Assignment> resubmittedAssignmentsStream = assignmentRepository
                .findAllByStatusAndCodeReviewerIdOrderByReviewedAtDesc(AssignmentStatusEnum.RESUBMITTED, reviewerId)
                .stream();

        Stream<Assignment> combinedStream = Stream.concat(submittedAssignmentsStream, resubmittedAssignmentsStream);

        return combinedStream
                .map(Converter::toAssignmentModel)
                .collect(Collectors.toList());

    }

    @Transactional
    public AssignmentModel reclaimAnAssignment(Long assignmentId, Long reviewerId) {

        logger.debug("Attempting to reclaim assignment ID {} by reviewer {}", assignmentId, reviewerId);

        return assignmentRepository.findByIdAndCodeReviewerId(assignmentId, reviewerId)
                .map(assignment -> {
                    validateReclaimEligibility(assignment);
                    updateAssignmentForReclaim(assignment);

                    Assignment savedAssignment = assignmentRepository.save(assignment);
                    logger.info("Reclaimed assignment ID {} by reviewer {}", assignmentId, reviewerId);

                    return Converter.toAssignmentModel(savedAssignment);
                })
                .orElseThrow(() -> {
                    logger.error("Assignment {} not found or not assigned to reviewer {}", assignmentId, reviewerId);
                    return new AssignmentNotFoundException(
                            "Assignment %d not found or not under reviewer %d".formatted(assignmentId, reviewerId));
                });
    }

    // --- Helper Methods -----------------------------------------
    private void validateReclaimEligibility(Assignment assignment) {
        if (assignment.getStatus() != AssignmentStatusEnum.RESUBMITTED) {
            logger.warn("Invalid reclaim attempt from status {} for assignment {}",
                    assignment.getStatus(), assignment.getId());
            throw new InvalidStatusChangeException(
                    "Cannot reclaim assignment in %s status".formatted(assignment.getStatus()));
        }
    }

    private void updateAssignmentForReclaim(Assignment assignment) {
        assignment.setStatus(AssignmentStatusEnum.IN_REVIEW);
        //i can set updated date here
    }

    //-------------------------------------------------------------------



    public List<AssignmentEnum> getAssignmentListUsingStreams() {
        return Arrays.stream(AssignmentEnum.values())
                .collect(Collectors.toList());

    }


}