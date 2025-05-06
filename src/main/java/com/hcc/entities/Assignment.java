package com.hcc.entities;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "assignmentsTable")
public class Assignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private final Long id;

    @Column(nullable = false)
    private final String status;

    @Column(nullable = false)
    private final Integer number;

    @Column(name = "github_url")
    private final String githubUrl;

    private final String branch;

    @Column(name = "review_video_url")
    private final String reviewVideoUrl;

    private final User user;

    // Required by JPA (protected to prevent direct use
    protected Assignment() {
        this.id = null;
        this.status = null;
        this.number = null;
        this.githubUrl = null;
        this.branch = null;
        this.reviewVideoUrl = null;
        this.user = null;
    }

    // Main constructor (all fields except id)
    public Assignment(Builder builder) {
        this.id = null; // Let JPA generate the ID
        this.status = builder.status;
        this.number = builder.number;
        this.githubUrl = builder.githubUrl;
        this.branch = builder.branch;
        this.reviewVideoUrl = builder.reviewVideoUrl;
        this.user = builder.user;
    }

    // Getters only (no setters)
    public Long getId() {
        return id;
    }

    public String getStatus() {
        return status;
    }

    public Integer getNumber() {
        return number;
    }

    public String getGithubUrl() {
        return githubUrl;
    }

    public String getBranch() {
        return branch;
    }

    public String getReviewVideoUrl() {
        return reviewVideoUrl;
    }

    public User getUser() {
        return user;
    }

    // equals() and hashCode()
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Assignment that = (Assignment) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    // toString()
    @Override
    public String toString() {
        return "Assignment{" +
                "id=" + id +
                ", status='" + status + '\'' +
                ", number=" + number +
                ", githubUrl='" + githubUrl + '\'' +
                ", branch='" + branch + '\'' +
                ", reviewVideoUrl='" + reviewVideoUrl + '\'' +
                '}';
    }

    // Builder pattern for updates
    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Long id;
        private String status;
        private Integer number;
        private String githubUrl;
        private String branch;
        private String reviewVideoUrl;
        private User user;

        private Builder() {}

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder status(String status) {
            this.status = status;
            return this;
        }

        public Builder number(Integer number) {
            this.number = number;
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

        public Assignment build() {
            return new Assignment(this);
        }
    }
}