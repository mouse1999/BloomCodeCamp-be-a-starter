package com.hcc.services;

import com.hcc.entities.Assignment;
import com.hcc.entities.User;
import com.hcc.enums.AssignmentEnum;
import com.hcc.enums.AssignmentStatusEnum;
import com.hcc.exceptions.UsernameNotFoundException;
import com.hcc.exceptions.assignmentexceptions.*;
import com.hcc.exceptions.userexceptions.InvalidUserAttributeException;
import com.hcc.interfaces.AssignmentServiceInterface;
import com.hcc.repositories.AssignmentRepository;
import com.hcc.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.naming.directory.InvalidAttributeValueException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
public class AssignmentService implements AssignmentServiceInterface<Assignment> {


    @Autowired
    private final AssignmentRepository assignmentRepository;
    private final UserRepository userRepository;
    private static final Logger logger = LoggerFactory.getLogger(AssignmentService.class);

    public AssignmentService(AssignmentRepository assignmentRepository, UserRepository userRepository) {
        this.assignmentRepository = assignmentRepository;
        this.userRepository = userRepository;
    }


    @Override
    public Assignment createAssignment(Integer assignmentNumber, Long creatorId) {

        Optional.ofNullable(creatorId)
                .orElseThrow(() -> new InvalidUserAttributeException("User ID must not be null"));


        User user = userRepository.findById(creatorId)
                .orElseThrow(() -> {
                    logger.error("User id {} not valid", creatorId);
                    return new UsernameNotFoundException("User not found with id: " + creatorId);
                });


        Optional.of(assignmentNumber)
                .filter(num -> !assignmentRepository.existsByAssignmentNumberAndUserId(num, creatorId))
                .orElseThrow(() -> {
                    logger.error("Assignment already submitted by user {}", creatorId);
                    return new AssignmentAlreadySubmittedException(
                            "Assignment " + assignmentNumber + " already exists for user " + creatorId
                    );
                });

        // Build and save assignment
        Assignment assignment = Assignment.builder()
                .assignmentNumber(assignmentNumber)
                .user(user)
                .assignmentEnum(AssignmentEnum.fromAssignmentNumber(assignmentNumber))
                .status(AssignmentStatusEnum.PENDING_SUBMISSION)
                .build();

        logger.debug("Assignment created: {}", assignment);
        return assignmentRepository.save(assignment);
    }
    @Override
    public Assignment getAssignmentById(Long id) {

        return Optional.ofNullable(id)
                .flatMap(assignmentRepository::findById)
                .orElseThrow(() -> id == null
                ? new InvalidAssignmentIdException("") :
                        new AssignmentNotFoundException("") );
    }

    @Override
    public List<Assignment> getAllAssignments() {
        return  new ArrayList<>(assignmentRepository.findAll());
    }

    @Override
    public List<Assignment> getAssignmentsByUserId(Long userId) {
        return null;
    }

    @Override
    public List<Assignment> getAssignmentsByStatusAndUserId(String status, Long userId) {
        return null;
    }

    @Override
    public List<Assignment> getAssignmentsByStatus(String status) {
        return null;
    }

    @Override
    public List<Assignment> getAssignmentsByStatusAndUserId(Integer statusStep, Long userId) {

        AssignmentStatusEnum status = Optional.ofNullable(statusStep)
                .map(AssignmentStatusEnum::fromStatusStep)
                .orElseThrow(() ->
                        new InvalidStatusStepAttributeException("Status step cannot be null"));


        return Optional.ofNullable(userId)
                .filter(userRepository::existsById)
                .map(id -> assignmentRepository.findByStatusAndUserId(status, id))
                .orElseThrow(() ->
                        userId == null
                        ? new InvalidUserAttributeException("User ID cannot be null")
                        : new UsernameNotFoundException("User not found for this Id " + userId));
    }

    @Override
    public List<Assignment> getAssignmentsByStatus(Integer statusStep) {
        AssignmentStatusEnum status = Optional.ofNullable(statusStep)
                .map(AssignmentStatusEnum::fromStatusStep)
                .orElseThrow(() -> statusStep == null?
                        new InvalidStatusStepAttributeException("Status step cannot be null")
                        : new InvalidAssignmentStatusException(""));

        return assignmentRepository.findAllByStatus(status);
    }

    @Override
    public Assignment submitAssignment(Long assignmentId, String branchName, String githubUrl, String studentId) {
        return null;
    }

    @Override
    public Assignment startReview(Long assignmentId, Long reviewerId) {
        return null;
    }

    @Override
    public Assignment completeReview(Long assignmentId, String reviewVideoUrl, String reviewerId) {
        return null;
    }

    @Override
    public Assignment requestResubmission(Long assignmentId, String feedback, String reviewerId) {
        return null;
    }

    @Override
    public AssignmentEnum[] getAssignmentList() {
        return  AssignmentEnum.values();
    }
}
