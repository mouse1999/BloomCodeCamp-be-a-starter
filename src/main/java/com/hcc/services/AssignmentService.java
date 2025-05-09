package com.hcc.services;

import com.hcc.dtos.AssignmentDto;
import com.hcc.interfaces.AssignmentServiceInterface;
import com.hcc.repositories.AssignmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AssignmentService implements AssignmentServiceInterface<AssignmentDto> {


    @Autowired
    private final AssignmentRepository assignmentRepository;
    public AssignmentService(AssignmentRepository assignmentRepository) {
        this.assignmentRepository = assignmentRepository;
    }

    @Override
    public AssignmentDto createAssignment(AssignmentDto assignmentDTO, String creatorId) {
        return null;
    }

    @Override
    public AssignmentDto getAssignmentById(Long id) {
        return null;
    }

    @Override
    public List<AssignmentDto> getAllAssignments() {
        return null;
    }

    @Override
    public List<AssignmentDto> getAssignmentsByUserId(String userId) {
        return null;
    }

    @Override
    public List<AssignmentDto> getAssignmentsByStatus(String status) {
        return null;
    }

    @Override
    public AssignmentDto submitAssignment(Long assignmentId, String githubUrl, String studentId) {
        return null;
    }

    @Override
    public AssignmentDto startReview(Long assignmentId, String reviewerId) {
        return null;
    }

    @Override
    public AssignmentDto completeReview(Long assignmentId, String reviewVideoUrl, String reviewerId) {
        return null;
    }

    @Override
    public AssignmentDto requestResubmission(Long assignmentId, String feedback, String reviewerId) {
        return null;
    }
}
