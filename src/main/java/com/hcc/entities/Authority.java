package com.hcc.entities;

import org.springframework.security.core.GrantedAuthority;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "authorities")
public class Authority implements GrantedAuthority {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String authority;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    protected Authority() {

    }


    private Authority(Builder builder) {
        this.authority = builder.authority;
        this.user = builder.user;
    }

    public Long getId() {
        return id;
    }

    @Override
    public String getAuthority() {
        return authority;
    }

    public User getUser() {
        return user;
    }

    public void setAuthority(String authority) { this.authority = authority; }
    public void setUser(User user) { this.user = user; }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Authority that = (Authority) o;

        if (id == null) {
            return false;
        }
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return id == null ? 31 : Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Authority{" +
                "id=" + id +
                ", authority='" + authority + '\'' +
                '}';
    }

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
            if (this.authority == null || this.authority.trim().isEmpty()) {
                throw new IllegalStateException("Authority string cannot be null or empty.");
            }
            if (this.user == null) {
                throw new IllegalStateException("User cannot be null for an Authority.");
            }
            return new Authority(this);
        }
    }
}