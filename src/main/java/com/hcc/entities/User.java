package com.hcc.entities;

import javax.persistence.*;
import java.util.Date;


@Entity
@Table(name = "UserTable")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private final Long id;

    @Column(name = "cohort_start_date")
    private final Date cohortStartDate;

    @Column(name = "username", nullable = false, unique = true)
    private final String userName;

    @Column(name = "password", nullable = false, updatable = false)
    private final String password;


    // Private constructor - only the Builder can create instances
    private User(Builder builder) {
        this.id = null;
        this.cohortStartDate = builder.cohortStartDate;
        this.userName = builder.userName;
        this.password = builder.password;

    }
    // JPA requires a no-args constructor (protected)
    protected User() {
        this.id = null;
        this.cohortStartDate = null;
        this.userName = null;
        this.password = null;

    }

    // Getters
    public Long getId() { return id; }
    public Date getCohortStartDate() { return cohortStartDate; }
    public String getUserName() { return userName; }
    public String getPassword() { return password; }

    // Static builder method
    public static Builder builder() {
        return new Builder();
    }

    // Builder class
    public static class Builder {

        private Date cohortStartDate;
        private String userName;
        private String password;


        private Builder() {}

        public Builder cohortStartDate(Date cohortStartDate) {
            this.cohortStartDate = cohortStartDate;
            return this;
        }

        public Builder userName(String userName) {
            this.userName = userName;
            return this;
        }

        public Builder password(String password) {
            this.password = password;
            return this;
        }


        public User build() {
            // Validate required fields
            if (userName == null || password == null) {
                throw new IllegalStateException("Username and password are required"); // i will have to create a custom Exception
            }

            return new User(this);
        }
    }


}