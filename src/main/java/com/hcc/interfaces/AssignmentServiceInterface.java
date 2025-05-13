package com.hcc.interfaces;
import com.hcc.entities.User;
import com.hcc.enums.AssignmentEnum;

import java.util.List;

public interface AssignmentServiceInterface<T> {

    // Create
    T createAssignment(Integer assignmentNumber,  Long creatorId);

    // Read
    T getAssignmentById(Long id);
    List<T> getAllAssignments();
    List<T> getAssignmentsByUserId(Long userId);
    List<T> getAssignmentsByStatusAndUserId(String status, Long userId);
    List<T> getAssignmentsByStatus(String status);
    List<T> getAssignmentsByStatusAndUserId(Integer statusStep, Long userId);
    List<T> getAssignmentsByStatus(Integer statusStep);


    //status management

    T submitAssignment(Long assignmentId, String branchName, String githubUrl, String studentId);
    T startReview(Long assignmentId, Long reviewerId); //when this is called by any reviewer, the queued assignment is removed from the list and handled specifically by the reviewer
    T completeReview(Long assignmentId, String reviewVideoUrl, String reviewerId);
    T requestResubmission(Long assignmentId, String feedback, String reviewerId);
    AssignmentEnum[] getAssignmentList();

}
