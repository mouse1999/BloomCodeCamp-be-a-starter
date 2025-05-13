package com.hcc.dtos.request.assignmentdto;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

public class EditAssignmentRequestDto {
    @NotBlank(message = "GitHub URL is required")
    @Pattern(
            regexp = "^(https?://)?(www\\.)?github\\.com/.+",
            message = "Must be a valid GitHub URL"
    )
    private final String githubUrl;

    @NotBlank(message = "Branch name is required")
    private final String branch;

    @NotNull(message = "Assignment number is required")
    @Min(value = 1, message = "Number must be ≥ 1")
    private final Integer assignmentNumber;
    @NotNull(message = "user Id is required")
    private final Long userId;

    public EditAssignmentRequestDto(
            String githubUrl,
            String branch,
            Integer assignmentNumber, Long userId
    ) {
        this.githubUrl = githubUrl;
        this.branch = branch;
        this.assignmentNumber = assignmentNumber;
        this.userId = userId;
    }

    public String getGithubUrl() {
        return githubUrl;
    }

    public Long getUserId() {
        return userId;
    }

    public String getBranch() {
        return branch;
    }

    public Integer getAssignmentNumber() {
        return assignmentNumber;
    }


}
