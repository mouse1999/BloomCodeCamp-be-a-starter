package com.hcc.dtos.response.assignmentdto;


import java.util.Optional;

public class AssignmentResponseDto {
    private final Long assignmentId;
    private final String status;
    private final Integer assignmentNumber;
    private final String assignmentName;
    private final String githubUrl;
    private final String branch;
    private final String createdAt;
    private final String reviewVideoUrl;  // Nullable until review
    private final  String reviewedAt;     // Nullable until review

    // All-args constructor (use builder in practice)
    private AssignmentResponseDto(Builder builder) {
        this.assignmentId = builder.assignmentId;
        this.status = builder.status;
        this.assignmentNumber = builder.assignmentNumber;
        this.githubUrl = builder.githubUrl;
        this.branch = builder.branch;
        this.createdAt = builder.createdAt;
        this.reviewVideoUrl = builder.reviewVideoUrl;
        this.reviewedAt = builder.reviewedAt;
        this.assignmentName = builder.assignmentName;
    }

    // === Getters (with Optional for nullable fields) ===

    public Long getAssignmentId() {
        return assignmentId;
    }

    public Optional<String> getStatus() { return Optional.ofNullable(status); }

    public Integer getAssignmentNumber() {
        return assignmentNumber;
    }

    public String getAssignmentName() {
        return assignmentName;
    }

    public Optional<String> getGithubUrl() {
        return Optional.ofNullable(githubUrl); }
    public Optional<String> getBranch() { return Optional.ofNullable(branch); }
    public String getCreatedAt() { return createdAt; }

    public Optional<String> getReviewVideoUrl() {
        return Optional.ofNullable(reviewVideoUrl);
    }
    public Optional<String> getReviewedAt() {
        return Optional.ofNullable(reviewedAt);
    }

    // === Builder Pattern (Recommended) ===
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long assignmentId;
        private String status;
        private Integer assignmentNumber;
        private String githubUrl;
        private String assignmentName;
        private String branch;
        private String createdAt;
        private String reviewVideoUrl;
        private String reviewedAt;

        public Builder assignmentId(Long assignmentId) {
            this.assignmentId = assignmentId;
            return this;
        }
        public Builder status(String status) {
            this.status = status;
            return this;
        }
        public Builder assignmentNumber(Integer assignmentNumber) {
            this.assignmentNumber = assignmentNumber;
            return this;
        }
        public Builder githubUrl(String githubUrl) {
            this.githubUrl = githubUrl;
            return this;
        }
        public Builder branch(String branch) {
            this.branch = branch;
            return this;
        }
        public Builder assignmentName(String assignmentName) {
            this.assignmentName = assignmentName;
            return this;
        }
        public Builder createdAt(String createdAt) {
            this.createdAt = createdAt;
            return this;
        }
        public Builder reviewVideoUrl(String reviewVideoUrl) {
            this.reviewVideoUrl = reviewVideoUrl;
            return this;
        }
        public Builder reviewedAt(String reviewedAt) {
            this.reviewedAt = reviewedAt;
            return this;
        }

        public AssignmentResponseDto build() {
            return new AssignmentResponseDto(this);
        }
    }
}