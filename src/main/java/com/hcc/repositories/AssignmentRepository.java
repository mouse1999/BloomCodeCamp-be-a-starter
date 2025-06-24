package com.hcc.repositories;

import com.hcc.entities.Assignment;
import com.hcc.enums.AssignmentStatusEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
    boolean existsByAssignmentNumberAndUserId(Integer assignmentNumber, Long userId);
//    List<Assignment> findByStatusAndUserId(AssignmentStatusEnum statusEnum, Long userId);
    List<Assignment> findAllByStatusAndUserId(AssignmentStatusEnum statusEnum, Long userId);
    List<Assignment> findAllByStatus(AssignmentStatusEnum statusEnum);

    List<Assignment> findByStatusOrderByCreatedAtDesc(AssignmentStatusEnum status);
    List<Assignment> findAllByStatusAndCodeReviewerIdOrderByReviewedAtDesc(AssignmentStatusEnum statusEnum, Long userId);
    List<Assignment> findAllByStatusInAndCodeReviewerIdOrderByReviewedAtDesc(List<AssignmentStatusEnum> status, Long reviewerId);
    List<Assignment> findAllByCodeReviewerIdOrderByReviewedAtDesc(Long reviewerId);

    Optional<Assignment>  findByIdAndUserId(Long assignmentId, Long userId);
    List<Assignment> findAllByUserIdOrderByCreatedAtDesc(Long userId);
    Optional<Assignment> findByIdAndCodeReviewerId(Long assignmentId, Long reviewerId);

}
