package com.hcc.entities;

import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "users")
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cohort_start_date")
    private Instant cohortStartDate;

    @Column(name = "username", nullable = false, unique = true)
    private String userName;

    @Column(name = "password", nullable = false, updatable = false)
    private String password;


    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Authority> authorities;

    protected User() {
        this.authorities = new ArrayList<>();
    }

    private User(Builder builder) {
        this();
        this.cohortStartDate = builder.cohortStartDate;
        this.userName = builder.userName;
        this.password = builder.password;
        if (builder.authorities != null) {
            this.authorities.addAll(builder.authorities);
        }
    }

    public Long getId() { return id; }
    public Instant getCohortStartDate() { return cohortStartDate; }
    public String getUserName() { return userName; }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.authorities;
    }

    @Override
    public String getPassword() { return password; }

    @Override
    public String getUsername() {
        return this.userName;
    }

    public void setCohortStartDate(Instant cohortStartDate) { this.cohortStartDate = cohortStartDate; }
    public void setUserName(String userName) { this.userName = userName; }
    public void setPassword(String password) { this.password = password; }
    public void setAuthorities(List<Authority> authorities) {
        this.authorities.clear();
        if (authorities != null) {
            this.authorities.addAll(authorities);
        }
    }


    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {

        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public static Builder builder() {
        return new Builder();
    }

    // Builder class
    public static class Builder {
        private Instant cohortStartDate;
        private String userName;
        private String password;
        private List<Authority> authorities;

        private Builder() {}

        public Builder cohortStartDate(Instant cohortStartDate) {
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
            this.authorities = new ArrayList<>(authorities); // Defensive copy
            return this;
        }

        public User build() {
            // Validate required fields
            if (userName == null || userName.trim().isEmpty()) {
                throw new IllegalStateException("Username is required.");
            }
            if (password == null || password.trim().isEmpty()) {
                throw new IllegalStateException("Password is required.");
            }

            if (this.authorities == null || this.authorities.isEmpty()) {
                this.authorities = new ArrayList<>();
            }
            return new User(this);
        }
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        if (id == null || user.id == null) {
            return false;
        }
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {

        return id == null ? 31 : Objects.hash(id);
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", userName='" + userName + '\'' +
                ", cohortStartDate=" + cohortStartDate +
                ", authoritiesSize=" + (authorities != null ? authorities.size() : 0) +
                '}';
    }
}