package com.hcc.dtos.request.assignmentdto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record SubmitAssignmentRequestDto(@NotBlank(message = "GitHub URL is required") @Pattern(
        regexp = "^(https?://)?(www\\.)?github\\.com/.+",
        message = "Must be a valid GitHub URL"
) String githubUrl, @NotBlank(message = "Branch name is required") String branch) {
    public SubmitAssignmentRequestDto(
            String githubUrl,
            String branch
    ) {
        this.githubUrl = githubUrl;
        this.branch = branch;

    }

    @Override
    public String githubUrl() {
        return githubUrl;
    }


    @Override
    public String branch() {
        return branch;
    }

}
