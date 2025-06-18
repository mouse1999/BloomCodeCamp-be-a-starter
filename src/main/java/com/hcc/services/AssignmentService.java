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
    private static final Logger logger = LoggerFactory.getLogger(AssignmentService.class);

    public AssignmentService(AssignmentRepository assignmentRepository) {
        this.assignmentRepository = assignmentRepository;

    }



    @Transactional // Ensures atomicity of the operation
    public AssignmentModel createAssignment(Integer assignmentNumber, User creator) {

        Long creatorId = creator.getId();

        if (assignmentRepository.existsByAssignmentNumberAndUserId(assignmentNumber, creatorId)) {
            logger.error("Assignment {} already submitted by user {}", assignmentNumber, creatorId);
            throw new AssignmentAlreadySubmittedException(
                    "Assignment " + assignmentNumber + " already exists for user " + creatorId
            );
        }


        Assignment assignment = Assignment.builder()
                .assignmentNumber(assignmentNumber)
                .user(creator)
                .assignmentType(AssignmentEnum.fromAssignmentNumber(assignmentNumber))
                .status(AssignmentStatusEnum.PENDING_SUBMISSION)
                .createdAt(Instant.now())
                .build();

        logger.debug("Creating assignment: {}", assignment);
        return Converter.toAssignmentModel(assignmentRepository.save(assignment));
    }

    public AssignmentModel getAssignmentByIdAndUserId(Long id, Long userId) {

        return Optional.ofNullable(id)
                .flatMap(assignmentId->assignmentRepository.findByIdAndUserId(assignmentId, userId))
                .map(Converter::toAssignmentModel)
                .orElseThrow(() -> id == null
                ? new InvalidAssignmentIdException("assignment Id must not be null") :
                        new AssignmentNotFoundException("Assignment with this Id is not found"));
    }

    public boolean existsAssignmentByNumber(Integer number, Long userId) {

        return Optional.ofNullable(number)
                .map((n)->assignmentRepository.existsByAssignmentNumberAndUserId(n, userId) )
                .orElseThrow(() -> number == null
                        ? new InvalidAssignmentNumberException("This assignment number does not exist") :
                        new AssignmentNotFoundException("This assignment is not yet") );
    }




    public List<AssignmentModel> getAssignmentsByStatusAndUserId(String status, Long userId) {

        if (status == null || status.trim().isEmpty()) {
            return getAssignmentsByUserId(userId);

        }else {
            return assignmentRepository.findAllByStatusAndUserId(AssignmentStatusEnum.fromStatus(status), userId)
                    .stream()
                    .map(Converter::toAssignmentModel)
                    .collect(Collectors.toList());
        }

    }

    public List<AssignmentModel> getAssignmentsByStatusAndReviewerId(String status, Long reviewerId) {
        if (status == null || status.trim().isEmpty()) {
            return assignmentRepository.findAllByCodeReviewerIdOrderByReviewedAtDesc(reviewerId).stream()
                    .map(Converter::toAssignmentModel)
                    .collect(Collectors.toList());
        }else {
            return assignmentRepository.findAllByStatusAndCodeReviewerIdOrderByReviewedAtDesc(AssignmentStatusEnum.fromStatus(status), reviewerId)
                    .stream()
                    .map(Converter::toAssignmentModel)
                    .collect(Collectors.toList());

        }


    }

    private List<AssignmentModel> getAssignmentsByUserId(Long userId) {
        return assignmentRepository.findAllByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(Converter::toAssignmentModel)
                .collect(Collectors.toList());
    }


    public List<AssignmentModel> getAssignmentsByStatus(String status) {

        if (status == null || status.trim().isEmpty()) {
            return assignmentRepository.findAll().stream()
                    .map(Converter::toAssignmentModel)
                    .collect(Collectors.toList());
        }else {
            return assignmentRepository.findAllByStatus(AssignmentStatusEnum.fromStatus(status)).stream()
                    .map(Converter::toAssignmentModel)
                    .collect(Collectors.toList());
        }
    }



    @Transactional
    public AssignmentModel submitAssignment(Long assignmentId, String branchName, String githubUrl, Long userId) {


        Optional.ofNullable(branchName)
                .filter(name -> !name.isEmpty())
                .orElseThrow(() -> new InvalidBranchNameException("Branch name cannot be empty"));

        Optional.ofNullable(githubUrl)
                .filter(url -> url.matches("^(https?://)?(www\\.)?github\\.com/.+"))
                .orElseThrow(() -> new InvalidGithubUrlException("Invalid GitHub URL: " + githubUrl));


        Assignment assignment = assignmentRepository.findByIdAndUserId(assignmentId, userId)
                .filter(assign -> {
                    if (assign.getStatus().equals(AssignmentStatusEnum.PENDING_SUBMISSION)) {
                        return true;
                    }else {
                        throw new InvalidStatusChangeException("This assignment cannot be submitted");
                    }

                })
                .map(assign -> {
                    assign.setBranch(branchName);
                    assign.setGithubUrl(githubUrl);
                    assign.setStatus(AssignmentStatusEnum.SUBMITTED);

                    return assign;
                })
                .orElseThrow(() -> new AssignmentNotFoundException("Assignment not found with ID for this user: " + assignmentId));

        return Converter.toAssignmentModel(assignmentRepository.save(assignment));
    }

    public AssignmentModel startReview(Long assignmentId, User reviewer) {

        Assignment foundAssignment = assignmentRepository.findById(assignmentId)
                .filter(assignment -> {
                    if (assignment.getStatus().equals(AssignmentStatusEnum.SUBMITTED)) {
                        return true;
                    }else {
                        throw new InvalidStatusChangeException("This assignment cannot be submitted ");
                    }
                })
                .map(assignment -> {
                    assignment.setStatus(AssignmentStatusEnum.IN_REVIEW);
                    assignment.setCodeReviewer(reviewer);
                    return assignment;
                })
                .orElseThrow(()-> new AssignmentNotFoundException("Assignment is not found"));

        return Converter.toAssignmentModel(assignmentRepository.save(foundAssignment));
    }


    public Assignment completeReview(Long assignmentId, String reviewVideoUrl, String reviewerId) {
        return null;
    }


    public Assignment requestResubmission(Long assignmentId, String feedback, String reviewerId) {
        return null;
    }


    public List<AssignmentEnum> getAssignmentListUsingStreams() {
        return Arrays.stream(AssignmentEnum.values())
                .collect(Collectors.toList());

    }


}