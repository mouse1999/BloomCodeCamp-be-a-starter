package com.hcc.models;

import java.util.Objects;

public class AssignmentModel {


    private Long id;
    private String createdAt;
    private String status;
    private Integer assignmentNumber;
    private String githubUrl;

    private String branch; // Default to nullable in DB

    private String reviewVideoUrl;

    private String reviewedAt; // Nullable until review

    private String assignmentType;
    private String learnerName;

    private String codeReviewerName;

    // JPA-required no-arg constructor

    public AssignmentModel() {
    }

    private AssignmentModel(Builder builder) {
        this.status = builder.status;
        this.id = builder.id;
        this.assignmentNumber = builder.assignmentNumber;
        this.githubUrl = builder.githubUrl;
        this.branch = builder.branch;
        this.reviewVideoUrl = builder.reviewVideoUrl;
        this.learnerName = builder.learnerName;
        this.codeReviewerName = builder.codeReviewerName;
        this.assignmentType = builder.assignmentType;
        this.createdAt = builder.createdAt;
        this.reviewedAt = builder.reviewedAt;
    }


    public Long getId() { return id; }
    public String getCreatedAt() { return createdAt; }
    public String getStatus() { return status; }
    public String getAssignmentType() { return assignmentType; }
    public Integer getAssignmentNumber() { return assignmentNumber; }
    public String getGithubUrl() { return githubUrl; }
    public String getBranch() { return branch; }
    public String getReviewVideoUrl() { return reviewVideoUrl; }
    public String getReviewedAt() { return reviewedAt; }
    public String getLearnerName() { return learnerName; }
    public String getCodeReviewerName() { return codeReviewerName; }


    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public void setStatus(String status) { this.status = status; }
    public void setAssignmentNumber(Integer assignmentNumber) { this.assignmentNumber = Objects.requireNonNull(assignmentNumber); }
    public void setGithubUrl(String githubUrl) { this.githubUrl = githubUrl; }
    public void setBranch(String branch) { this.branch = branch; }
    public void setReviewVideoUrl(String reviewVideoUrl) { this.reviewVideoUrl = reviewVideoUrl; }
    public void setReviewedAt(String reviewedAt) { this.reviewedAt = reviewedAt; }
    public void setLearnerName(String learnerName) { this.learnerName = learnerName; }
    public void setCodeReviewerName(String codeReviewer) { this.codeReviewerName = codeReviewerName; }
    public void setAssignmentType(String assignmentType) { this.assignmentType = assignmentType; } // Renamed setter



    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Long id;
        private String status;
        private String assignmentType; // Renamed
        private Integer assignmentNumber;
        private String githubUrl;
        private String branch;
        private String reviewVideoUrl;
        private String learnerName;
        private String codeReviewerName;
        private String createdAt;
        private String reviewedAt;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }


        public Builder status(String status) {
            this.status = status;
            return this;
        }

        public Builder assignmentType(String assignmentType) { // Renamed method
            this.assignmentType = assignmentType;
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

        public Builder reviewVideoUrl(String reviewVideoUrl) {
            this.reviewVideoUrl = reviewVideoUrl;
            return this;
        }

        public Builder learner(String userName) {
            this.learnerName = userName;
            return this;
        }

        public Builder codeReviewer(String codeReviewerName) {
            this.codeReviewerName = codeReviewerName;
            return this;
        }
        public Builder createdAt(String createdAt) {
            this.createdAt = createdAt;
            return this;
        }
        public Builder reviewedAt(String reviewedAt) {
            this.reviewedAt = reviewedAt;
            return this;
        }

        public AssignmentModel build() {

            return new AssignmentModel(this);
        }
    }
}

