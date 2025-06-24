package com.hcc.service;


import com.hcc.converter.Converter;
import com.hcc.entities.Assignment;
import com.hcc.entities.User;
import com.hcc.enums.AssignmentEnum;
import com.hcc.enums.AssignmentStatusEnum;
import com.hcc.exceptions.UserNotFoundException;
import com.hcc.exceptions.assignmentexceptions.*;
import com.hcc.exceptions.userexceptions.InvalidUserAttributeException;
import com.hcc.models.AssignmentModel;
import com.hcc.repositories.AssignmentRepository;
import com.hcc.repositories.UserRepository;
import com.hcc.services.AssignmentService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AssignmentServiceTest {

    @Mock
    private AssignmentRepository assignmentRepository;

    @InjectMocks
    private AssignmentService assignmentService;

    private final User testUser = User.builder()
            .id(1L)
            .userName("testuser")
            .password("password")
            .build();

    private final Assignment testAssignment = Assignment.builder()
            .id(1L)
            .user(testUser)
            .assignmentNumber(1)
            .status(AssignmentStatusEnum.PENDING_SUBMISSION)
            .assignmentType(AssignmentEnum.ASSIGNMENT_1)
            .createdAt(Instant.now())
            .build();

    @Test
    void createAssignment_validInput_returnsAssignmentModel() {
        // Given
        Integer assignmentNumber = 1;

        when(assignmentRepository.existsByAssignmentNumberAndUserId(assignmentNumber, testUser.getId()))
                .thenReturn(false);
        when(assignmentRepository.save(any(Assignment.class)))
                .thenReturn(testAssignment);

        // When
        AssignmentModel result = assignmentService.createAssignment(assignmentNumber, testUser);

        // Then
        assertNotNull(result);
        assertEquals(testAssignment.getId(), result.getId());
        assertEquals(testAssignment.getAssignmentNumber(), result.getAssignmentNumber());
        assertEquals(testAssignment.getStatus().getStatus(), result.getStatus());
        assertNotNull(result.getCreatedAt());
    }

    @Test
    void getAssignmentByIdAndUserId_validIds_returnsAssignmentModel() {
        // Given
        Long assignmentId = 1L;
        Long userId = 1L;

        when(assignmentRepository.findByIdAndUserId(assignmentId, userId))
                .thenReturn(Optional.of(testAssignment));

        // When
        AssignmentModel result = assignmentService.getAssignmentByIdAndUserId(assignmentId, userId);

        // Then
        assertNotNull(result);
        assertEquals(testAssignment.getId(), result.getId());
        assertEquals(Converter.toAssignmentModel(testAssignment).getLearnerName(), result.getLearnerName());
    }

    @Test
    void submitOrEditAssignment_validSubmission_returnsUpdatedAssignmentModel() {
        // Given
        Long assignmentId = 1L;
        String branch = "main";
        String githubUrl = "https://github.com/user/repo";

        Assignment submittedAssignment = Assignment.builder()
                .id(assignmentId)
                .user(testUser)
                .branch(branch)
                .githubUrl(githubUrl)
                .status(AssignmentStatusEnum.SUBMITTED)
                .build();

        when(assignmentRepository.findByIdAndUserId(assignmentId, testUser.getId()))
                .thenReturn(Optional.of(testAssignment));
        when(assignmentRepository.save(any(Assignment.class)))
                .thenReturn(submittedAssignment);

        // When
        AssignmentModel result = assignmentService.submitOrEditAssignment(
                assignmentId, branch, githubUrl, testUser.getId());

        // Then
        assertEquals(AssignmentStatusEnum.SUBMITTED.getStatus(), result.getStatus());
        assertEquals(branch, result.getBranch());
        assertEquals(githubUrl, result.getGithubUrl());
    }

    @Test
    void completeReview_validReview_returnsCompletedAssignmentModel() {
        // Given
        Long assignmentId = 1L;
        String reviewVideoUrl = "https://example.com/review.mp4";

        Assignment completedAssignment = Assignment.builder()
                .id(assignmentId)
                .status(AssignmentStatusEnum.COMPLETED)
                .reviewVideoUrl(reviewVideoUrl)
                .reviewedAt(Instant.now())
                .build();

        when(assignmentRepository.findById(assignmentId))
                .thenReturn(Optional.of(testAssignment));
        when(assignmentRepository.save(any(Assignment.class)))
                .thenReturn(completedAssignment);

        // When
        AssignmentModel result = assignmentService.completeReview(assignmentId, reviewVideoUrl);

        // Then
        assertEquals(AssignmentStatusEnum.COMPLETED.getStatus(), result.getStatus());
        assertEquals(reviewVideoUrl, result.getReviewVideoUrl());
        assertNotNull(result.getReviewedAt());
    }

    @Test
    void getAssignmentsByStatusAndUserId_withStatus_returnsAssignmentModelList() {
        // Given
        String status = "submitted";
        Long userId = 1L;
        Integer assignmentNumber = 3;

        Assignment submittedAssignment = Assignment.builder()
                .status(AssignmentStatusEnum.SUBMITTED)
                .user(testUser)
                .assignmentNumber(assignmentNumber)
                .build();

        when(assignmentRepository.findAllByStatusAndUserId(
                AssignmentStatusEnum.SUBMITTED, userId))
                .thenReturn(List.of(submittedAssignment));

        // When
        List<AssignmentModel> result = assignmentService.getAssignmentsByStatusAndUserId(status, userId);

        // Then
        assertEquals(1, result.size());
        assertEquals(AssignmentStatusEnum.SUBMITTED.getStatus(), result.get(0).getStatus());
        assertEquals(Converter.toAssignmentModel(submittedAssignment).getLearnerName(), result.get(0).getLearnerName());
    }

    @Test
    void reclaimAnAssignment_validReclaim_returnsAssignmentModel() {
        // Given
        Long assignmentId = 1L;
        Long reviewerId = 2L;

        Assignment reclaimedAssignment = Assignment.builder()
                .id(assignmentId)
                .codeReviewer(User.builder().id(reviewerId).build())
                .status(AssignmentStatusEnum.IN_REVIEW)
                .build();

        when(assignmentRepository.findByIdAndCodeReviewerId(assignmentId, reviewerId))
                .thenReturn(Optional.of(testAssignment));
        when(assignmentRepository.save(any(Assignment.class)))
                .thenReturn(reclaimedAssignment);

        // When
        AssignmentModel result = assignmentService.reclaimAnAssignment(assignmentId, reviewerId);

        // Then
        assertEquals(AssignmentStatusEnum.IN_REVIEW.getStatus(), result.getStatus());
        assertNotNull(result.getCodeReviewerName());
    }

    @Test
    void getAssignmentListUsingStreams_returnsAssignmentEnumList() {
        // When
        List<AssignmentEnum> result = assignmentService.getAssignmentListUsingStreams();

        // Then
        assertFalse(result.isEmpty());
        assertEquals(AssignmentEnum.values().length, result.size());
    }
}