package com.hcc.converter;


import com.hcc.entities.Assignment;
import com.hcc.entities.User;
import com.hcc.models.AssignmentModel;
import com.hcc.models.UserModel;
import org.springframework.security.core.GrantedAuthority;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.stream.Collectors;

public class Converter {
    public static com.hcc.models.AssignmentModel toAssignmentModel(Assignment assignment) {
        return AssignmentModel.builder()
                .id(assignment.getId())
                .codeReviewer(assignment.getCodeReviewer() == null ? null : assignment.getCodeReviewer().getUsername())
                .assignmentNumber(assignment.getAssignmentNumber())
                .assignmentType(assignment.getAssignmentType().getAssignmentName())
                .status(assignment.getStatus().getStatus())
                .githubUrl(assignment.getGithubUrl())
                .createdAt(formatInstant(assignment.getCreatedAt()))
                .reviewVideoUrl(assignment.getReviewVideoUrl())
                .branch(assignment.getBranch())
                .reviewedAt(formatInstant(assignment.getReviewedAt()))
                .learner(assignment.getUser().getUsername())
                .build();
    }


    public static UserModel toUserModel(User user) {

        return UserModel.builder()
                .userName(user.getUsername())
                .authorities(user.getAuthorities().stream().map(item -> (GrantedAuthority) item)
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toList()))
                .cohortStartDate(user.getCohortStartDate())
                .build();
    }

    public static User toUser(UserModel userModel) {
        return null;
    }

    public static String formatInstant(Instant instant) {
        if (instant == null) {
            return null;
        }
        LocalDate date = instant.atZone(ZoneId.systemDefault()).toLocalDate();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        return date.format(formatter);
    }
}
