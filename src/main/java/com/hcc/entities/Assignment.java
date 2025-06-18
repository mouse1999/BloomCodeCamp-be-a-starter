package com.hcc.entities;

import com.hcc.enums.AssignmentEnum;
import com.hcc.enums.AssignmentStatusEnum;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;


import java.util.Objects;


import java.time.Instant;

@Entity
@Table(name = "assignments_table")
public class Assignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private Instant createdAt;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AssignmentStatusEnum status;

    @Column(nullable = false)
    private Integer assignmentNumber;

    @Column(name = "github_url")
    private String githubUrl;

    private String branch; // Default to nullable in DB

    @Column(name = "review_video_url")
    private String reviewVideoUrl;

    private Instant reviewedAt; // Nullable unt
    // il review

    @Enumerated(EnumType.STRING)
    @Column(name = "assignment_type")
    private AssignmentEnum assignmentType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id")
    private User codeReviewer;

    // JPA-required no-arg constructor

    public Assignment() {
        this.createdAt = Instant.now();
        this.status = AssignmentStatusEnum.PENDING_SUBMISSION;
    }

    private Assignment(Builder builder) {
        this();// initializes createdAt and default status
        this.status = builder.status != null ? builder.status : AssignmentStatusEnum.PENDING_SUBMISSION; // Ensure status from builder
        this.assignmentNumber = builder.assignmentNumber;
        this.githubUrl = builder.githubUrl;
        this.branch = builder.branch;
        this.reviewVideoUrl = builder.reviewVideoUrl;
        this.user = builder.user;
        this.codeReviewer = builder.codeReviewer;
        this.assignmentType = builder.assignmentType;
        this.createdAt = builder.createdAt;
    }


    public Long getId() { return id; }
    public Instant getCreatedAt() { return createdAt; }
    public AssignmentStatusEnum getStatus() { return status; }
    public AssignmentEnum getAssignmentType() { return assignmentType; }
    public Integer getAssignmentNumber() { return assignmentNumber; }
    public String getGithubUrl() { return githubUrl; }
    public String getBranch() { return branch; }
    public String getReviewVideoUrl() { return reviewVideoUrl; }
    public Instant getReviewedAt() { return reviewedAt; }
    public User getUser() { return user; }
    public User getCodeReviewer() { return codeReviewer; }


    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public void setStatus(AssignmentStatusEnum status) { this.status = Objects.requireNonNull(status); }
    public void setAssignmentNumber(Integer assignmentNumber) { this.assignmentNumber = Objects.requireNonNull(assignmentNumber); }
    public void setGithubUrl(String githubUrl) { this.githubUrl = githubUrl; }
    public void setBranch(String branch) { this.branch = branch; }
    public void setReviewVideoUrl(String reviewVideoUrl) { this.reviewVideoUrl = reviewVideoUrl; }
    public void setReviewedAt(Instant reviewedAt) { this.reviewedAt = reviewedAt; }
    public void setUser(User user) { this.user = user; }
    public void setCodeReviewer(User codeReviewer) { this.codeReviewer = codeReviewer; }
    public void setAssignmentType(AssignmentEnum assignmentType) { this.assignmentType = assignmentType; } // Renamed setter


    public void updateReviewDetails(String videoUrl, Instant reviewedAt, User reviewer) {
        this.reviewVideoUrl = videoUrl;
        this.reviewedAt = reviewedAt;
        this.codeReviewer = reviewer;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private AssignmentStatusEnum status;
        private AssignmentEnum assignmentType; // Renamed
        private Integer assignmentNumber;
        private String githubUrl;
        private String branch;
        private String reviewVideoUrl;
        private Instant createdAt;
        private User user;
        private User codeReviewer;

        // Ensure mandatory fields are handled appropriately (e.g., in a required constructor)
        // or through validation after build().
        // For builder, usually all fields are optional initially, then validated in build().

        public Builder status(AssignmentStatusEnum status) {
            this.status = status;
            return this;
        }

        public Builder assignmentType(AssignmentEnum assignmentType) { // Renamed method
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

        public Builder user(User user) {
            this.user = user;
            return this;
        }

        public Builder codeReviewer(User codeReviewer) {
            this.codeReviewer = codeReviewer;
            return this;
        }

        public Builder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Assignment build() {

            if (this.assignmentNumber == null) {
                throw new IllegalStateException("Assignment number must be set.");
            }
            if (this.user == null) {
                throw new IllegalStateException("User must be set.");
            }
            if (this.status == null) {
                this.status = AssignmentStatusEnum.PENDING_SUBMISSION;
            }

            return new Assignment(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (id == null) return false; // If this entity is not persisted, it cannot be equal to another entity by ID
        if (o == null || getClass() != o.getClass()) return false;
        Assignment that = (Assignment) o;
        return Objects.equals(id, that.id); // Only compare by ID for persisted entities
    }

    @Override
    public int hashCode() {
        // Important: Return a constant (e.g., 31) for transient entities or use ID only
        return id == null ? 31 : Objects.hash(id); // Use ID for persisted entities
    }

    @Override
    public String toString() {
        return "Assignment{" +
                "id=" + id +
                ", createdAt=" + createdAt +
                ", status=" + status +
                ", assignmentNumber=" + assignmentNumber +
                ", githubUrl='" + githubUrl + '\'' +
                ", branch='" + branch + '\'' +
                ", reviewVideoUrl='" + reviewVideoUrl + '\'' +
                ", reviewedAt=" + reviewedAt +
                ", assignmentType=" + assignmentType + // Renamed
                ", user=" + (user != null ? user.getId() : "null") + // Avoid fetching full user object
                ", codeReviewer=" + (codeReviewer != null ? codeReviewer.getId() : "null") +
                '}';
    }
}
