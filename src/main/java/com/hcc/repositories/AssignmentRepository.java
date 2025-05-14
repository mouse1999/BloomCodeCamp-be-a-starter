package com.hcc.repositories;

import com.hcc.entities.Assignment;
import com.hcc.enums.AssignmentStatusEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
    boolean existsByAssignmentNumberAndUserId(Integer assignmentNumber, Long userId);
    List<Assignment> findByStatusAndUserId(AssignmentStatusEnum statusEnum, Long userId);
    List<Assignment> findAllByStatus(AssignmentStatusEnum statusEnum);
    List<Assignment> findByStatusOrderByCreatedAtDesc(AssignmentStatusEnum status);
    Assignment findByAssignmentNumberAndUserId(Integer assignmentNumber, Long userId);
}
