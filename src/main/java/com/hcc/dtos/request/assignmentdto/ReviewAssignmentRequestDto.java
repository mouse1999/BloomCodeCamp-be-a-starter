package com.hcc.dtos.request.assignmentdto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ReviewAssignmentRequestDto {
    @NotNull(message = "Review url cannot be null")
    private String reviewVideoUrl;

    public ReviewAssignmentRequestDto() {

    }

    public String getReviewVideoUrl() {
        return reviewVideoUrl;
    }

    public void setReviewVideoUrl(String reviewVideoUrl) {
        this.reviewVideoUrl = reviewVideoUrl;
    }
}
