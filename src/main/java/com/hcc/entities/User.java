package com.hcc.entities;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;


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

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private final List<Authority> authorities;


    private User(Builder builder) {
        this.id = null;
        this.cohortStartDate = builder.cohortStartDate;
        this.userName = builder.userName;
        this.password = builder.password;

        authorities = builder.authorities;
    }
    // JPA requires a no-args constructor (protected)
    protected User() {
        this.id = null;
        this.cohortStartDate = null;
        this.userName = null;
        this.password = null;
        authorities = null;
    }

    // Getters
    public Long getId() { return id; }
    public Date getCohortStartDate() { return cohortStartDate; }
    public String getUserName() { return userName; }
    public String getPassword() { return password; }

    public List<Authority> getAuthorities() {
        return new ArrayList<>(Objects.requireNonNull(authorities));
    }

    // Static builder method
    public static Builder builder() {
        return new Builder();
    }

    // Builder class
    public static class Builder {

        private Date cohortStartDate;
        private String userName;
        private String password;
        private List<Authority> authorities;


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
        public Builder authorities(List<Authority> authorities) {
            this.authorities = new ArrayList<>(authorities);
            return this;
        }


        public User build() {
            // Validate required fields
            if (userName == null || password == null) {
                //TODO
                throw new IllegalStateException("Username and password are required"); // i will have to create a custom Exception
            }

            return new User(this);
        }
    }


}