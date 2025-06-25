package com.hcc.service;


import com.hcc.converter.Converter;
import com.hcc.dtos.request.assignmentdto.EditAssignmentRequestDto;
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

import java.time.Duration;
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

    private final User testLearner = User.builder()
            .id(1L)
            .userName("testLearner")
            .password("password")
            .build();

    private final User testReviewer = User.builder()
            .id(1L)
            .userName("testReviewer")
            .password("password")
            .build();

    private final Assignment testAssignment = Assignment.builder()
            .id(1L)
            .user(testLearner)
            .assignmentNumber(1)
            .status(AssignmentStatusEnum.PENDING_SUBMISSION)
            .assignmentType(AssignmentEnum.ASSIGNMENT_1)
            .createdAt(Instant.now())
            .build();
    private final Long assignmentId = 1L;
    private final String validReviewVideoUrl = "https://example.com/review.mp4";
    private final String invalidReviewVideoUrl = "invalid-url";


    @Test
    void createAssignment_validInput_returnsAssignmentModel() {
        // Given
        Integer assignmentNumber = 1;

        when(assignmentRepository.existsByAssignmentNumberAndUserId(assignmentNumber, testLearner.getId()))
                .thenReturn(false);
        when(assignmentRepository.save(any(Assignment.class)))
                .thenReturn(testAssignment);

        // When
        AssignmentModel result = assignmentService.createAssignment(assignmentNumber, testLearner);

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
        Integer assignmentNumber = 2;

        EditAssignmentRequestDto request = new EditAssignmentRequestDto(githubUrl, branch, assignmentNumber, testLearner.getId());

        Assignment submittedAssignment = Assignment.builder()
                .id(assignmentId)
                .user(testLearner)
                .assignmentNumber(2)
                .branch(branch)
                .assignmentType(AssignmentEnum.ASSIGNMENT_1)
                .githubUrl(githubUrl)
                .status(AssignmentStatusEnum.SUBMITTED)
                .build();

        when(assignmentRepository.findByIdAndUserId(assignmentId, testLearner.getId()))
                .thenReturn(Optional.of(testAssignment));
        when(assignmentRepository.save(any(Assignment.class)))
                .thenReturn(submittedAssignment);

        // When
        AssignmentModel result = assignmentService.submitOrEditAssignment(
                assignmentId, request.getBranch(), request.getGithubUrl(), testLearner.getId());

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
        String branch = "main";
        String githubUrl = "https://github.com/user/repo";

        testAssignment.setStatus(AssignmentStatusEnum.IN_REVIEW);


        Assignment completedAssignment = Assignment.builder()
                .id(assignmentId)
                .user(testLearner)
                .assignmentNumber(2)
                .branch(branch)
                .assignmentType(AssignmentEnum.ASSIGNMENT_1)
                .githubUrl(githubUrl)
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
        String status = "Submitted";
        Integer assignmentNumber = 3;
        String branch = "main";
        String githubUrl = "https://github.com/user/repo";
        Long assignmentId = 1L;



        Assignment submittedAssignment = Assignment.builder()
                .id(assignmentId)
                .user(testLearner)
                .assignmentNumber(2)
                .branch(branch)
                .assignmentType(AssignmentEnum.ASSIGNMENT_1)
                .githubUrl(githubUrl)
                .status(AssignmentStatusEnum.SUBMITTED)
                .assignmentNumber(assignmentNumber)
                .build();

        when(assignmentRepository.findAllByStatusAndUserId(
                AssignmentStatusEnum.SUBMITTED, testLearner.getId()))
                .thenReturn(List.of(submittedAssignment));

        // When
        List<AssignmentModel> result = assignmentService.getAssignmentsByStatusAndUserId(status, testLearner.getId());

        // Then
        assertEquals(1, result.size());
        assertEquals(AssignmentStatusEnum.SUBMITTED.getStatus(), result.get(0).getStatus());
        assertEquals(Converter.toAssignmentModel(submittedAssignment).getLearnerName(), result.get(0).getLearnerName());
    }


    @Test
    void getAssignmentsByStatusAndUserId_withOutStatus_returnsAssignmentModelList() {
        // Given
        String status = null;
        Integer assignmentNumber = 3;
        String branch = "main";
        String githubUrl = "https://github.com/user/repo";
        Long assignmentId = 1L;



        Assignment submittedAssignment = Assignment.builder()
                .id(assignmentId)
                .user(testLearner)
                .assignmentNumber(2)
                .branch(branch)
                .assignmentType(AssignmentEnum.ASSIGNMENT_1)
                .githubUrl(githubUrl)
                .status(AssignmentStatusEnum.SUBMITTED)
                .assignmentNumber(assignmentNumber)
                .build();


        when(assignmentRepository.findAllByUserIdOrderByCreatedAtDesc(testLearner.getId()))
                .thenReturn(List.of(submittedAssignment));

        // When
        List<AssignmentModel> result = assignmentService.getAssignmentsByStatusAndUserId(status, testLearner.getId());

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
        Integer assignmentNumber = 3;
        String branch = "main";
        String githubUrl = "https://github.com/user/repo";

        Assignment reclaimedAssignment = Assignment.builder()
                .id(assignmentId)
                .user(testLearner)
                .branch(branch)
                .assignmentType(AssignmentEnum.ASSIGNMENT_1)
                .githubUrl(githubUrl)
                .assignmentNumber(assignmentNumber)
                .codeReviewer(testReviewer)
                .status(AssignmentStatusEnum.IN_REVIEW)
                .build();

        testAssignment.setStatus(AssignmentStatusEnum.RESUBMITTED);

        when(assignmentRepository.findByIdAndCodeReviewerId(assignmentId, testReviewer.getId()))
                .thenReturn(Optional.of(testAssignment));
        when(assignmentRepository.save(any(Assignment.class)))
                .thenReturn(reclaimedAssignment);

        // When
        AssignmentModel result = assignmentService.reclaimAnAssignment(assignmentId, testReviewer.getId());

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



    @Test
    void getClaimedAndUnclaimedAssignmentsForReviewer_returnsBothSubmittedAndResubmitted() {
        // Given

        Integer assignmentNumber = 3;
        String branch = "main";
        String githubUrl = "https://github.com/user/repo";

        Assignment submittedAssignment = Assignment.builder()
                .id(1L)
                .user(testLearner)
                .branch(branch)
                .assignmentType(AssignmentEnum.ASSIGNMENT_1)
                .githubUrl(githubUrl)
                .assignmentNumber(assignmentNumber)
                .status(AssignmentStatusEnum.SUBMITTED)
                .build();

        Assignment resubmittedAssignment = Assignment.builder()
                .id(2L)
                .user(testLearner)
                .branch(branch)
                .assignmentType(AssignmentEnum.ASSIGNMENT_1)
                .githubUrl(githubUrl)
                .assignmentNumber(assignmentNumber)
                .status(AssignmentStatusEnum.RESUBMITTED)
                .codeReviewer(testReviewer)
                .build();

        List<Assignment> mockAssignments = List.of(submittedAssignment, resubmittedAssignment);

        when(assignmentRepository.findAllByStatusInAndCodeReviewerIdOrderByReviewedAtDesc(
                List.of(AssignmentStatusEnum.SUBMITTED, AssignmentStatusEnum.RESUBMITTED),
                testReviewer.getId()
        )).thenReturn(mockAssignments);

        // When
        List<AssignmentModel> result = assignmentService.getClaimedAndUnclaimedAssignmentsForReviewer(testReviewer.getId());

        // Then
        assertEquals(2, result.size());

        // Verify conversion and status
        assertEquals(AssignmentStatusEnum.SUBMITTED.getStatus(), result.get(0).getStatus());
        assertEquals(AssignmentStatusEnum.RESUBMITTED.getStatus(), result.get(1).getStatus());

        // Verify reviewer assignment
        assertEquals(testReviewer.getUsername(), result.get(1).getCodeReviewerName());
//        assertEquals(reviewerId, result.get(1).getCodeReviewer().getId());

        verify(assignmentRepository).findAllByStatusInAndCodeReviewerIdOrderByReviewedAtDesc(
                List.of(AssignmentStatusEnum.SUBMITTED, AssignmentStatusEnum.RESUBMITTED),
                testReviewer.getId()
        );
    }

    @Test
    void getClaimedAndUnclaimedAssignmentsForReviewer_emptyResult_returnsEmptyList() {
        // Given
        when(assignmentRepository.findAllByStatusInAndCodeReviewerIdOrderByReviewedAtDesc(
                anyList(),
                eq(testReviewer.getId())
        )).thenReturn(List.of());

        // When
        List<AssignmentModel> result = assignmentService.getClaimedAndUnclaimedAssignmentsForReviewer(testReviewer.getId());

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void getClaimedAndUnclaimedAssignmentsForReviewer_nullReviewerId_throwsException() {
        // When/Then
        assertThrows(IllegalArgumentException.class, () -> {
            assignmentService.getClaimedAndUnclaimedAssignmentsForReviewer(null);
        });

        verify(assignmentRepository, never()).findAllByStatusInAndCodeReviewerIdOrderByReviewedAtDesc(anyList(), any());
    }

    @Test
    void getClaimedAndUnclaimedAssignmentsForReviewer_ordersByReviewedAtDesc() {
        // Given
        Instant now = Instant.now();

        Assignment olderAssignment = Assignment.builder()
                .id(1L)
                .status(AssignmentStatusEnum.SUBMITTED)
                .codeReviewer(testReviewer)
                .assignmentType(AssignmentEnum.ASSIGNMENT_1)
                .user(testLearner)
                .assignmentNumber(2)
                .reviewedAt(now)
                .build();

        Assignment newerAssignment = Assignment.builder()
                .id(2L)
                .status(AssignmentStatusEnum.RESUBMITTED)
                .codeReviewer(testReviewer)
                .assignmentType(AssignmentEnum.ASSIGNMENT_2)
                .user(testLearner)
                .assignmentNumber(4)
                .reviewedAt(now)
                .build();

        //  Repository should return in descending order
        List<Assignment> mockAssignments = List.of(newerAssignment, olderAssignment);

        when(assignmentRepository.findAllByStatusInAndCodeReviewerIdOrderByReviewedAtDesc(anyList(), eq(testReviewer.getId())))
                .thenReturn(mockAssignments);

        // When
        List<AssignmentModel> result = assignmentService.getClaimedAndUnclaimedAssignmentsForReviewer(testReviewer.getId());

        // Then
        assertEquals(2, result.size());
        assertEquals(newerAssignment.getId(), result.get(0).getId());
        assertEquals(olderAssignment.getId(), result.get(1).getId());
    }


    @Test
    void requestResubmission_validRequest_returnsUpdatedAssignmentModel() {
        // Given
//         Long assignmentId = 1L;
//        String validReviewVideoUrl = "https://example.com/review.mp4";


        Instant now = Instant.now();
        Assignment inReviewAssignment = Assignment.builder()
                .id(assignmentId)
                .codeReviewer(testReviewer)
                .assignmentType(AssignmentEnum.ASSIGNMENT_1)
                .user(testLearner)
                .assignmentNumber(2)
                .reviewedAt(now)
                .status(AssignmentStatusEnum.IN_REVIEW)
                .build();

        Assignment updatedAssignment = Assignment.builder()
                .id(assignmentId)
                .assignmentNumber(2)
                .user(testLearner)
                .assignmentType(AssignmentEnum.ASSIGNMENT_1)
                .status(AssignmentStatusEnum.NEEDS_UPDATE)
                .reviewVideoUrl(validReviewVideoUrl)
                .build();

        when(assignmentRepository.findById(assignmentId))
                .thenReturn(Optional.of(inReviewAssignment));
        when(assignmentRepository.save(any(Assignment.class)))
                .thenReturn(updatedAssignment);

        // When
        AssignmentModel result = assignmentService.requestResubmission(assignmentId, validReviewVideoUrl);

        // Then
        assertEquals(AssignmentStatusEnum.NEEDS_UPDATE.getStatus(), result.getStatus());
        assertEquals(validReviewVideoUrl, result.getReviewVideoUrl());

        verify(assignmentRepository).findById(assignmentId);
        verify(assignmentRepository).save(inReviewAssignment);
//        verify(logger).info("Requested resubmission for assignment ID: {}", assignmentId);
    }

    @Test
    void requestResubmission_invalidVideoUrl_throwsException() {
        // When/Then
//        Long assignmentId = 1L;
//        String invalidReviewVideoUrl = "invalid-url";
        assertThrows(IllegalArgumentException.class, () -> {
            assignmentService.requestResubmission(assignmentId, invalidReviewVideoUrl);
        });

        verify(assignmentRepository, never()).findById(any());
        verify(assignmentRepository, never()).save(any());
    }

    @Test
    void requestResubmission_assignmentNotFound_throwsException() {
//        // Given
//        Long assignmentId = 1L;
//        String validReviewVideoUrl = "https://example.com/review.mp4";
        when(assignmentRepository.findById(assignmentId))
                .thenReturn(Optional.empty());

        // When/Then
        assertThrows(AssignmentNotFoundException.class, () -> {
            assignmentService.requestResubmission(assignmentId, validReviewVideoUrl);
        });

//        verify(logger).error("Assignment not found with ID: {}", assignmentId);
    }

    @Test
    void requestResubmission_invalidStatus_throwsStatusChangeException() {
        // Given

        Assignment completedAssignment = Assignment.builder()
                .id(assignmentId)
                .assignmentNumber(1)
                .user(testLearner)
                .assignmentType(AssignmentEnum.ASSIGNMENT_1)
                .status(AssignmentStatusEnum.COMPLETED)
                .build();

        when(assignmentRepository.findById(assignmentId))
                .thenReturn(Optional.of(completedAssignment));

        // When/Then
        assertThrows(InvalidStatusChangeException.class, () -> {
            assignmentService.requestResubmission(assignmentId, validReviewVideoUrl);
        });

//        verify(logger).warn("Invalid status transition from {} to {} for assignment {}",
//                completedAssignment.getStatus(),
//                AssignmentStatusEnum.NEEDS_UPDATE,
//                completedAssignment.getId());
    }

    @Test
    void requestResubmission_nullAssignmentId_throwsException() {
        // When/Then
        assertThrows(IllegalArgumentException.class, () -> {
            assignmentService.requestResubmission(null, validReviewVideoUrl);
        });

        verify(assignmentRepository, never()).findById(any());
    }

    @Test
    void requestResubmission_nullVideoUrl_throwsException() {
        // When/Then
        assertThrows(IllegalArgumentException.class, () -> {
            assignmentService.requestResubmission(assignmentId, null);
        });

        verify(assignmentRepository, never()).findById(any());
    }

    @Test
    void requestResubmission_emptyVideoUrl_throwsException() {
        // When/Then
        assertThrows(IllegalArgumentException.class, () -> {
            assignmentService.requestResubmission(assignmentId, "");
        });

        verify(assignmentRepository, never()).findById(any());
    }
}