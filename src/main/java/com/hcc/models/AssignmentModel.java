package com.hcc.models;


import com.hcc.entities.User;
import com.hcc.enums.AssignmentStatusEnum;

import javax.persistence.*;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

public class AssignmentModel {


    private final Instant createdAt;  // Auto-set on creation

    private AssignmentStatusEnum status;

    private final Integer assignmentNumber;  // Required

    private String githubUrl;

    private String branch;

    private String reviewVideoUrl;

    private Instant reviewedAt;  // Nullable until review

    private final User user;

    private User codeReviewer;

    // JPA-required constructor
    public AssignmentModel() {

        assignmentNumber = null;
        createdAt = null;
        user = null;
    }

    // Builder constructor
    private AssignmentModel(Builder builder) {

        this.status = builder.status;
        this.assignmentNumber = builder.assignmentNumber;
        this.githubUrl = builder.githubUrl;
        this.branch = builder.branch;
        this.reviewVideoUrl = builder.reviewVideoUrl;
        this.user = builder.user;
        this.codeReviewer = builder.codeReviewer;
        this.createdAt = null;
    }



    public Instant getCreatedAt() {
        return createdAt;
    }

    public AssignmentStatusEnum getStatus() {
        return status;
    }

    public Integer getAssignmentNumber() {
        return assignmentNumber;
    }

    public Optional<String> getGithubUrl() {
        return Optional.ofNullable(githubUrl);
    }

    public Optional<String> getBranch() {
        return Optional.of(branch);
    }

    public Optional<String> getReviewVideoUrl() {
        return Optional.ofNullable(reviewVideoUrl);
    }

    public Optional<Instant> getReviewedAt() {
        return Optional.ofNullable(reviewedAt);
    }

    public User getUser() {
        return user;
    }

    public Optional<User> getCodeReviewer() {
        return Optional.ofNullable(codeReviewer);
    }


    public void setStatus(AssignmentStatusEnum status) {
        this.status = status;
    }

    public void setGithubUrl(String githubUrl) {
        this.githubUrl = githubUrl;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public void setReviewDetails(String videoUrl, Instant reviewedAt) {
        this.reviewVideoUrl = videoUrl;
        this.reviewedAt = reviewedAt;
    }

    public void setCodeReviewer(User codeReviewer) {
        this.codeReviewer = codeReviewer;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private AssignmentStatusEnum status;
        private Integer assignmentNumber;
        private String githubUrl;
        private String branch;
        private String reviewVideoUrl;
        private User user;
        private User codeReviewer;

        public Builder status(AssignmentStatusEnum status) {
            this.status = status;
            return this;
        }

        public Builder assignmentNumber(Integer assignmentNumber) {
            this.assignmentNumber = (assignmentNumber);
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

        public Builder user(User user) {
            this.user = user;
            return this;
        }

        public Builder codeReviewer(User codeReviewer) {
            this.codeReviewer = codeReviewer;
            return this;
        }

        public AssignmentModel build() {
            return new AssignmentModel(this);
        }
    }
}

