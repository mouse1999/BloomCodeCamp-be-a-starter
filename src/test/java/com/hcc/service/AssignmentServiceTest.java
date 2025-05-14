package com.hcc.service;

import com.hcc.entities.Assignment;
import com.hcc.entities.User;
import com.hcc.enums.AssignmentStatusEnum;
import com.hcc.exceptions.UserNotFoundException;
import com.hcc.exceptions.assignmentexceptions.*;
import com.hcc.exceptions.userexceptions.InvalidUserAttributeException;
import com.hcc.repositories.AssignmentRepository;
import com.hcc.repositories.UserRepository;
import com.hcc.services.AssignmentService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AssignmentServiceTest {

    @Mock
    private AssignmentRepository assignmentRepository;
    @InjectMocks
    private AssignmentService assignmentService;
    @Mock
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

    }

    @Test
    void createAssignment_validAssignmentNumberAndUserId_returnsAssignment() {
        //GIVEN
        Integer assignmentNumber = 2;
        String userName = "edward";
        String password = "user123";
        Long userId = 123L;


        User user = User.builder()
                .userName(userName)
                .password(password)
                .cohortStartDate(Date.from(Instant.now()))
                .build();

        Assignment expectedAssignment = Assignment.builder()
                .user(user)
                .assignmentNumber(assignmentNumber)
                .status(AssignmentStatusEnum.PENDING_SUBMISSION)
                .build();


        //WHEN
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(assignmentRepository.save(ArgumentMatchers.any(Assignment.class))).thenReturn(expectedAssignment);

        when(assignmentRepository.existsByAssignmentNumberAndUserId(assignmentNumber, userId))
                .thenReturn(false);

        Assignment actualAssignment = assignmentService.createAssignment(assignmentNumber, userId);

        //THEN
        assertNotNull(actualAssignment, "Assignment should not be null");
        assertEquals(assignmentNumber, actualAssignment.getAssignmentNumber(),
                "Assignment number should match");
        assertEquals(AssignmentStatusEnum.PENDING_SUBMISSION, actualAssignment.getStatus(),
                "Default status should be PENDING_SUBMISSION");
        assertNotNull(actualAssignment.getCreatedAt(),
                "Created timestamp should be set");

    }

    @Test
    void createAssignment_invalidAssignmentNumber_throwsInvalidAssignmentException() {

        Integer invalidAssignmentNumber = 20;
        Long userId = 123L;

        String userName = "edward";
        String password = "user123";


        User user = User.builder()
                .userName(userName)
                .password(password)
                .cohortStartDate(Date.from(Instant.now()))
                .build();


        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertThrows(InvalidAssignmentNumberException.class, () -> {
            assignmentService.createAssignment(invalidAssignmentNumber, userId);
        });


    }
    @Test
    void createAssignment_nullUserId_throwsException() {
        Integer assignmentNumber = 2;
        Long nullUserId = null ;

        assertThrows(InvalidUserAttributeException.class, () -> {
            assignmentService.createAssignment(assignmentNumber, nullUserId);
        });

    }
    @Test
    void createAssignment_wrongUserId_throwsUserNotFoundException() {
        //Given
        Integer assignmentNumber = 2;
        Long wrongUserId = -1L ;

        //WHEN & THEN
        when(userRepository.findById(wrongUserId)).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> {
            assignmentService.createAssignment(assignmentNumber, wrongUserId);
        });


    }
    @Test
    void createAssignment_duplicateAssignmentForSameUser_throwsAssignmentAlreadySubmittedException() {
        Integer assignmentNumber = 2;
        Long userId = 123L;
        String userName = "edward";
        String password = "user123";


        User user = User.builder()
                .userName(userName)
                .password(password)
                .cohortStartDate(Date.from(Instant.now()))
                .build();

        Assignment expectedAssignment = Assignment.builder()
                .user(user)
                .assignmentNumber(assignmentNumber)
                .status(AssignmentStatusEnum.PENDING_SUBMISSION)
                .build();


        //WHEN
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(assignmentRepository.save(ArgumentMatchers.any(Assignment.class))).thenReturn(expectedAssignment);
        when(assignmentRepository.existsByAssignmentNumberAndUserId(assignmentNumber, userId))
                .thenReturn(true);


        assertThrows(AssignmentAlreadySubmittedException.class, () -> {
            assignmentService.createAssignment(assignmentNumber, userId);
        });
    }


    @Test
    void getAssignmentById_validId_returnsAssignment() {


        //GIVEN
        Long assignmentId = 123L;
        Integer assignmentNumber = 5;
        Assignment expectedAssignment = Assignment.builder()
                .assignmentNumber(assignmentNumber)
                .build();


        //WHEN
        when(assignmentRepository.findById(assignmentId)).thenReturn(Optional.of(expectedAssignment));
        Assignment actualAssignment = assignmentService.getAssignmentById(assignmentId);

        //THEN

        Assertions.assertNotNull(actualAssignment, "assignment should not be null");
    }

    @Test
    void getAssignmentById_nullId_throwsInvalidAssignmentIdException() {

        //GIVEN
        Long assignmentId = null;

        //WHEN
        when(assignmentRepository.findById(assignmentId)).thenThrow(InvalidAssignmentIdException.class);

        //THEN
        assertThrows(InvalidAssignmentIdException.class,
                ()-> assignmentService.getAssignmentById(assignmentId));
    }

    @Test
    void getAssignmentById_nonExistentId_throwsAssignmentNotFoundException() {

        //GIVEN
        Long assignmentId = 123L;


        //WHEN & THEN
        when(assignmentRepository.findById(assignmentId)).thenReturn(Optional.empty());

        Assertions.assertThrows(AssignmentNotFoundException.class,
                ()-> assignmentService.getAssignmentById(assignmentId) );



    }

    @Test
    void getAllAssignments_returnsAssignments() {
        //GIVEN

        Assignment assignment1 = Assignment.builder()
                .build();
        Assignment assignment2 = Assignment.builder()
                .build();
        Assignment assignment3 = Assignment.builder()
                .build();

        List<Assignment> assignments = List.of(assignment1, assignment2, assignment3);


        //WHEN
        when(assignmentRepository.findAll()).thenReturn(assignments);
        List<Assignment> assignment = assignmentService.getAllAssignments();
        //THEN
        assertEquals(3, assignment.size());
    }

    @Test
    void getAllAssignments_modifyingListItem_returnsOriginalAssignments() {
        Assignment assignment1 = Assignment.builder()
                .build();
        Assignment assignment2 = Assignment.builder()
                .build();
        Assignment assignment3 = Assignment.builder()
                .build();

        List<Assignment> assignments = List.of(assignment1, assignment2, assignment3);


        //WHEN
        when(assignmentRepository.findAll()).thenReturn(assignments);
        List<Assignment> actualAssignment = assignmentService.getAllAssignments();
        //modify the return list
        actualAssignment.add(new Assignment());


        //THEN
        assertEquals(3, assignmentService.getAllAssignments().size());
    }

    @Test
    void getAssignmentsByStatusAndUserId_validStatusAndUserId_returnsAssignments() {
        //GIVEN
        Integer statusStep = 1;
        AssignmentStatusEnum stepEnum = AssignmentStatusEnum.PENDING_SUBMISSION;
        Long userId = 1L;
        Assignment mockAssignment1 = Assignment.builder().status(AssignmentStatusEnum.PENDING_SUBMISSION).build();
        Assignment mockAssignment2 = Assignment.builder().status(AssignmentStatusEnum.SUBMITTED).build();
        Assignment mockAssignment3 = Assignment.builder().status(AssignmentStatusEnum.PENDING_SUBMISSION).build();

        when(userRepository.existsById(userId)).thenReturn(true);

        when(assignmentRepository.findByStatusAndUserId(stepEnum, userId))
                .thenReturn(List.of(mockAssignment1, mockAssignment3));

        //WHEN
        List<Assignment> result = assignmentService.getAssignmentsByStatusAndUserId(statusStep, userId);

        // THEN
        assertEquals(2, result.size());
        assertEquals(statusStep, result.get(0).getStatus().getStep());
        verify(assignmentRepository).findByStatusAndUserId(stepEnum, userId);
    }
    @Test
    void getAssignmentsByStatusAndId_emptyStatus_returnsAllAssignmentsForUser() {}
    @Test
    void getAssignmentsByStatusAndId_nullStatus_returnInvalidStatusStepAttributeException() {

        //GIVEN
        Integer statusStep = null;
        Long userId = 1L;
        when(userRepository.existsById(userId)).thenReturn(true);

        //WHEN & THEN
        assertThrows(InvalidStatusStepAttributeException.class,
                ()-> assignmentService.getAssignmentsByStatusAndUserId(statusStep, userId));

    }
    @Test
    void getAssignmentsByStatusAndId_nullUserId_throwsException() {

    }
    @Test
    void getAssignmentsByStatusAndId_invalidStatus_throwsException() {}
    @Test
    void getAssignmentsByStatusAndId_validStatusAndNonExistentUserId_returnsEmptyList() {}
    @Test
    void getAssignmentsByStatusAndId_whitespaceStatus_returnsAllAssignments() {}
    @Test
    void getAssignmentsByStatusAndId_caseInsensitiveStatus_returnsAssignments() {}

    @Test
    void getAssignmentsByStatus_validStatusStep_returnsAssignments() {
        // GIVEN
        Integer statusStep = 1;
        AssignmentStatusEnum stepEnum = AssignmentStatusEnum.PENDING_SUBMISSION;

        List<Assignment> expected = List.of(Assignment.builder()
                        .status(AssignmentStatusEnum.PENDING_SUBMISSION)
                        .build(),
                Assignment.builder()
                        .status(AssignmentStatusEnum.PENDING_SUBMISSION)
                        .build()
        );
        when(assignmentRepository.findAllByStatus(stepEnum)).thenReturn(expected);

        // WHEN
        List<Assignment> result = assignmentService.getAssignmentsByStatus(statusStep);

        // THEN
        assertEquals(expected.size(), result.size());
        assertTrue(result.stream().allMatch(a ->
                a.getStatus().getStep().equals(statusStep)));
        verify(assignmentRepository).findAllByStatus(stepEnum);
    }
    @Test
    void getAssignmentsByStatus_validStatusStep_noAssignments_returnsEmptyList() {
        // GIVEN
        Integer statusStep = 2; // IN_REVIEW
        when(assignmentRepository.findAllByStatus(AssignmentStatusEnum.IN_REVIEW)).thenReturn(List.of());

        // WHEN
        List<Assignment> result = assignmentService.getAssignmentsByStatus(statusStep);

        // THEN
        assertTrue(result.isEmpty());
    }
    @Test
    void getAssignmentsByStatus_invalidStatusStep_throwsInvalidAssignmentStatusException() {
        // GIVEN
        Integer invalidStatusStep = 99;

        // WHEN & THEN
        assertThrows(InvalidAssignmentStatusException.class, () -> {
            assignmentService.getAssignmentsByStatus(invalidStatusStep);
        });
        verify(assignmentRepository, never()).findAllByStatus(any());
    }

    @Test
    void getAssignmentsByStatus_nullStatusStep_throwsInvalidStatusStepAttributeException() {
        Integer statusStep = null;
        assertThrows(InvalidStatusStepAttributeException.class, () -> {
            assignmentService.getAssignmentsByStatus(statusStep);
        });
        verify(assignmentRepository, never()).findAllByStatus(any());
    }

    @Test
    void submitAssignment_validInput_returnsAssignment() {
        // Given
        Integer assignmentNumber = 1;
        String branchName = "main";
        String githubUrl = "https://github.com/user/repo";
        Long userId = 123L;
        Long assignmentId = 234L;

        User mockUser = User.builder()
                .userName("edward")
                .password("user123")
                .build();

        Assignment expectedAssignment = Assignment.builder()
                .assignmentNumber(assignmentNumber)
                .branch(branchName)
                .githubUrl(githubUrl)
                .user(mockUser)
                .status(AssignmentStatusEnum.SUBMITTED)
                .build();

        when(userRepository.existsById(userId)).thenReturn(true);
        when(assignmentRepository.findById(assignmentId)).thenReturn(Optional.of(expectedAssignment));

        when(assignmentRepository.save(any(Assignment.class))).thenReturn(expectedAssignment);

        // When
        Assignment result = assignmentService.submitAssignment(assignmentId, branchName, githubUrl, userId);

        // Then
        assertNotNull(result);
        assertEquals(assignmentNumber, result.getAssignmentNumber(),"assignment Id must match");
        assertEquals(branchName, result.getBranch().get(),"branch name must match");
        assertEquals(githubUrl, result.getGithubUrl().get(),"githuburl must match");
        assertEquals(AssignmentStatusEnum.SUBMITTED, result.getStatus());
        verify(assignmentRepository, times(1)).save(any());
    }

    @Test
    void submitAssignment_invalidAssignmentId_throwsInvalidAssignmentIdException() {
        // Given
        Long invalidAssignmentId = null; // or null
        String branchName = "main";
        String githubUrl = "https://github.com/user/repo";
        Long userId = 123L;

        // When/Then
        assertThrows(InvalidAssignmentIdException.class, () -> {
            assignmentService.submitAssignment(invalidAssignmentId, branchName, githubUrl, userId);
        });
        verify(assignmentRepository, never()).save(any());
    }

    @Test
    void submitAssignment_nonexistentUser_throwsException() {
        // Given
        Long invalidUserId = 999L;
        String githubUrl = "https://github.com/user/repo";
        when(userRepository.existsById(invalidUserId)).thenReturn(false);

        // When/Then
        assertThrows(UserNotFoundException.class, () -> {
            assignmentService.submitAssignment(3L, "main", githubUrl, invalidUserId);
        });
        verify(assignmentRepository, never()).save(any());
    }

    @Test
    void submitAssignment_invalidGithubUrl_throwsInvalidGithubUrlException() {
        // Given
        String invalidUrl = "not-a-url";
        Long userId = 123L;

        // When/Then
        when(userRepository.existsById(userId)).thenReturn(true);
        assertThrows(InvalidGithubUrlException.class, () -> {
            assignmentService.submitAssignment(1L, "main", invalidUrl, 123L);
        });
        verify(assignmentRepository, never()).save(any());
    }

    @Test
    void submitAssignment_emptyBranchName_throwsInvalidBranchNameException() {
        // Given
        String emptyBranch = "";
        Long userId = 123L;

        // When/Then
        when(userRepository.existsById(userId)).thenReturn(true);
        assertThrows(InvalidBranchNameException.class, () -> {
            assignmentService.submitAssignment(1L, emptyBranch, "https://valid.url", userId);
        });
        verify(assignmentRepository, never()).save(any());
    }


    @Test
    @WithMockUser(password = "1234", username = "reviewer1", roles = "REVIEWER")
    void startReview_validAssignmentAndAuthenticatedReviewer_returnsUpdatedAssignment() {
        // Given
        Long assignmentId = 1L;
        Long reviewerId = 100L;
        Assignment assignment = Assignment.builder()
                .status(AssignmentStatusEnum.SUBMITTED)
                .build();

        when(assignmentRepository.findById(assignmentId))
                .thenReturn(Optional.of(assignment));

        when(userRepository.findById(reviewerId)).thenReturn(Optional.of(User.builder().userName("reviewer").password("123").build()));

        //when(any(assignment.getClass()).getStatus()). thenReturn(AssignmentStatusEnum.SUBMITTED);

        when(assignmentRepository.save(assignment)).then(inv -> inv.getArgument(0));

        // When
        Assignment result = assignmentService.startReview(assignmentId, reviewerId);

        // Then
        assertEquals(AssignmentStatusEnum.IN_REVIEW, result.getStatus());
        assertNotNull(result.getCodeReviewer().get(),"reviewer must not be null");
        verify(assignmentRepository).save(assignment);
        verify(assignmentRepository, times(1)).findById(assignmentId);
    }

    @Test
    void startReview_invalidAssignmentId_throwsAssignmentNotFoundException() {
        when(assignmentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(AssignmentNotFoundException.class,
                () -> assignmentService.startReview(999L, 100L));
    }

    @Test
    void startReview_alreadyReviewedAssignment_throwsStatusChangeException() {

        Long reviewerId = 123L;
        Long assignmentId = 1L;
        Assignment inReviewAssignment = Assignment.builder()
                .status(AssignmentStatusEnum.IN_REVIEW)
                .build();
        when(userRepository.findById(reviewerId)).thenReturn(Optional.of(User.builder().userName("reviewer").password("123").build()));

        when(assignmentRepository.findById(assignmentId))
                .thenReturn(Optional.of(inReviewAssignment));

        assertThrows(StatusChangeException.class,
                () -> assignmentService.startReview(assignmentId, reviewerId));
    }


    @Test
    @WithMockUser(username = "unauthorized", roles = "LEARNER")
    void startReview_unauthorizedUser_throwsAccessDenied() {
        // Given
        Long assignmentId = 1L;
        Long reviewerId = 100L;
        when(assignmentRepository.findById(assignmentId)).thenReturn(Optional.of(new Assignment()));
        when(userRepository.findById(reviewerId)).thenReturn(Optional.of(User.builder().userName("learner1").password("123").build())); // User exists

        // When & Then
        assertThrows(AccessDeniedException.class, () -> assignmentService.startReview(assignmentId, reviewerId));
    }



}
