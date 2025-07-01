package com.hcc.controllers;

import com.hcc.dtos.request.assignmentdto.CreateAssignmentRequest;
import com.hcc.dtos.request.assignmentdto.ReviewAssignmentRequestDto;
import com.hcc.dtos.request.assignmentdto.SubmitAssignmentRequestDto;
import com.hcc.entities.User;
import com.hcc.enums.AssignmentEnum;
import com.hcc.models.AssignmentModel;
import com.hcc.services.AssignmentClaimingService;
import com.hcc.services.AssignmentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("api")
public class AssignmentController {

    @Autowired
    private final AssignmentService assignmentService;
    @Autowired
    private final AssignmentClaimingService assignmentClaimingService;


    public AssignmentController(AssignmentService assignmentService, AssignmentClaimingService assignmentClaimingService) {
        this.assignmentService = assignmentService;
        this.assignmentClaimingService = assignmentClaimingService;
    }

    @PostMapping("/assignments")
    public ResponseEntity<AssignmentModel> createAssignment(
            @Valid @RequestBody CreateAssignmentRequest request,
            @AuthenticationPrincipal User currentUser) {

        AssignmentModel createdAssignment = assignmentService.createAssignment(
                request.getAssignmentNumber(),
                currentUser
        );

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdAssignment.getId())
                .toUri();

        return ResponseEntity.created(location).body(createdAssignment);
    }

    @PostMapping("/user/assignments/{assignmentId}/submit")
    public ResponseEntity<?> submitAssignment(@Valid @RequestBody SubmitAssignmentRequestDto requestDto,
                                              @PathVariable Long assignmentId,
                                              @AuthenticationPrincipal User user) {

        AssignmentModel assignmentModel = assignmentService.submitOrEditAssignment(assignmentId,
                                                                    requestDto.branch(),
                                                                    requestDto.githubUrl(),
                                                                    user.getId());
        return ResponseEntity.ok(assignmentModel);
    }

    @GetMapping("/user/assignments/{assignmentNumber}/status")
    public ResponseEntity<?> existsAssignmentByNumber(@PathVariable  Integer assignmentNumber,
                                                      @AuthenticationPrincipal User user ) {
//        boolean doesExit = assignmentService.existsAssignmentByNumber(assignmentNumber, user.getId());
//        if (!doesExit) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Assignment not found");
//        }

        return ResponseEntity.ok(assignmentService.existsAssignmentByNumber(assignmentNumber, user.getId()));
    }

    @GetMapping("/assignments/enums")
    public ResponseEntity<?> getAssignmentEnumList() {
        List<AssignmentEnum>  enumArray = assignmentService.getAssignmentListUsingStreams();

        return ResponseEntity.ok(enumArray);
    }

    @GetMapping("/user/assignments")
    public ResponseEntity<?> findAssignmentsByUser(@AuthenticationPrincipal User user,
                                                  @RequestParam(required = false) String status) {
        List<AssignmentModel> assignmentModels = assignmentService.getAssignmentsByStatusAndUserId(status, user.getId());
        return ResponseEntity.ok(assignmentModels);

    }
    @GetMapping("/reviewer/assignments")
    public ResponseEntity<?> findAssignmentsByReviewer(@AuthenticationPrincipal User reviewer,
                                                       @RequestParam(required = false) String status) {
        List<AssignmentModel> assignmentModels = assignmentService.getAssignmentsByStatusAndReviewerId(status, reviewer.getId());
        return ResponseEntity.ok(assignmentModels);
    }


    @GetMapping("/assignments")
    public ResponseEntity<?> findOrFilterAllAssignments(@RequestParam(required = false) String status) {
        return ResponseEntity.ok(assignmentService.getAssignmentsByStatus(status)) ;
    }


    @GetMapping("/user/assignments/{assignmentId}")
    public ResponseEntity<?> getAnAssignment(@PathVariable Long assignmentId, @AuthenticationPrincipal User user) {
        AssignmentModel assignmentModels = assignmentService.getAssignmentByIdAndUserId(assignmentId, user.getId());
        return ResponseEntity.ok(assignmentModels);


    }

   @GetMapping("reviewer/assignments/{assignmentId}")
    public ResponseEntity<?> findAssignmentByReviewer(@PathVariable Long assignmentId,
                                                      @AuthenticationPrincipal User reviewer) {
        AssignmentModel assignmentModel = assignmentService.getAssignmentByIdAndReviewer(assignmentId, reviewer.getId())
;
        return ResponseEntity.ok(assignmentModel);
    }
    @GetMapping("assignments/{id}/claim")
    public ResponseEntity<AssignmentModel> claimSpecificAssignmentByReviewer(
            @PathVariable Long id,
            @AuthenticationPrincipal User reviewer) {

        if (reviewer == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        return assignmentClaimingService.claimSpecificAssignment(id, reviewer)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.CONFLICT).body(null));
    }

    @PutMapping("/reviewer/assignments/{assignmentId}/complete")
    public ResponseEntity<?> completeReview(@Valid @RequestBody ReviewAssignmentRequestDto requestDto,
                                            @PathVariable Long assignmentId ) {
        return ResponseEntity.ok(assignmentService
                .completeReview(assignmentId, requestDto.getReviewVideoUrl()));

    }

    @PutMapping("/reviewer/assignments/{assignmentId}/resubmit")
    public ResponseEntity<?> requestReSubmission(@Valid @RequestBody ReviewAssignmentRequestDto requestDto, @PathVariable Long assignmentId) {
        return ResponseEntity.ok(assignmentService.requestResubmission(assignmentId, requestDto.getReviewVideoUrl()));

    }


    @GetMapping("/reviewer/assignments/claim-reclaim")
    public  ResponseEntity<?> getClaimedAndUnclaimedAssignments(@AuthenticationPrincipal User reviewer) {
        return ResponseEntity.ok(assignmentService.getClaimedAndUnclaimedAssignmentsForReviewer(reviewer.getId()));

    }

    @PutMapping("/reviewer/assignments/{assignmentId}/reclaim")
    public ResponseEntity<?> reclaimAssignment(@PathVariable Long assignmentId,
                                               @AuthenticationPrincipal User reviewer) {
        return ResponseEntity.ok(assignmentService.reclaimAnAssignment(assignmentId, reviewer.getId()));

    }

    @GetMapping("/assignments/{assignmentId}")
    public ResponseEntity<?> getAssignmentById(@PathVariable Long assignmentId) {
        return ResponseEntity.ok(assignmentService.getAssignmentById(assignmentId));
    }
}
