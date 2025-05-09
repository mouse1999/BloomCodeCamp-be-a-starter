package com.hcc.interfaces;
import com.hcc.entities.User;

import java.util.List;

public interface AssignmentServiceInterface<T> {

    // Create
    T createAssignment(T assignmentDTO, String creatorId);

    // Read
    T getAssignmentById(Long id);
    List<T> getAllAssignments();
    List<T> getAssignmentsByUserId(String userId);
    List<T> getAssignmentsByStatus(String status);

    //status management

    T submitAssignment(Long assignmentId, String githubUrl, String studentId);
    T startReview(Long assignmentId, String reviewerId);
    T completeReview(Long assignmentId, String reviewVideoUrl, String reviewerId);
    T requestResubmission(Long assignmentId, String feedback, String reviewerId);
}
