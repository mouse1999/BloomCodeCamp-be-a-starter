package com.hcc.entities;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "authoritiesTable")
public class Authority {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private final Long id;

    @Column(nullable = false, unique = true)
    private final String authority;

    private final User user;

    // Required by JPA (protected to prevent direct instantiation)
    protected Authority() {
        this.id = null;
        this.authority = null;
        this.user = null;
    }

    // Private constructor used by builder
    private Authority(Builder builder) {
        this.id = null;
        this.authority = builder.authority;
        this.user = builder.user;
    }

    // Getters (no setters for immutability)
    public Long getId() {
        return id;
    }

    public String getAuthority() {
        return authority;
    }

    public User getUser() {
        return user;
    }

    // equals() and hashCode()
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Authority authority = (Authority) o;
        return Objects.equals(id, authority.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Authority{" +
                "id=" + id +
                ", authority='" + authority + '\'' +
                '}'; // Exclude user to avoid circular references
    }

    // Builder pattern implementation
    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String authority;
        private User user;

        private Builder() {}

        public Builder authority(String authority) {
            this.authority = authority;
            return this;
        }

        public Builder user(User user) {
            this.user = user;
            return this;
        }

        public Authority build() {
            return new Authority(this);
        }
    }
}
