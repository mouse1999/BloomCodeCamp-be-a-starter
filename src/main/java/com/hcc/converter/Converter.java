package com.hcc.converter;


import com.hcc.dtos.response.assignmentdto.AssignmentResponseDto;
import com.hcc.entities.Assignment;
import com.hcc.entities.Authority;
import com.hcc.entities.User;
import com.hcc.models.AssignmentModel;
import com.hcc.models.UserModel;
import org.springframework.security.core.GrantedAuthority;

import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

public class Converter {
    public static AssignmentResponseDto toAssignmentResponseDto(Assignment assignment) {
        return AssignmentResponseDto.builder()
                .assignmentId(assignment.getId())
                //.assignmentName(assignment.)
                .branch(assignment.getBranch().orElse(null))
                .createdAt(DateTimeFormatter.ISO_DATE_TIME.format(assignment.getCreatedAt()))
                .githubUrl(assignment.getGithubUrl().orElse(null))
                .reviewVideoUrl(assignment.getReviewVideoUrl().orElse(null))
                .status(assignment.getStatus().name())
                .reviewedAt(assignment.getReviewedAt().orElse(null).toString())
                .assignmentNumber(assignment.getAssignmentNumber())
                .build();
    }


    public static UserModel toUserModel(User user) {

        return UserModel.builder()
                .userName(user.getUserName())
                .authorities(user.getAuthorities().stream().map(item -> (GrantedAuthority) item)
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toList()))
                .cohortStartDate(user.getCohortStartDate())
                .build();
    }

    public static User toUser(UserModel userModel) {
        return null;
    }
}
