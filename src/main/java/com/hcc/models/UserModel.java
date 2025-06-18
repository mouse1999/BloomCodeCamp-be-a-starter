package com.hcc.models;



import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class UserModel {
    private Instant cohortStartDate;
    private String userName;
    private List<String> authorities;

    private UserModel(Builder builder) {
        this.cohortStartDate = builder.cohortStartDate;
        this.userName = builder.userName;
        this.authorities = builder.authorities;
    }


    public Instant getCohortStartDate() { return cohortStartDate; }
    public String getUserName() { return userName; }



    public void setCohortStartDate(Instant cohortStartDate) {
        this.cohortStartDate = cohortStartDate;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public static Builder builder() {
        return new Builder();
    }

    public List<String> getAuthorities() {
        return authorities;
    }

    public void setAuthorities(List<String> authorities) {
        this.authorities = authorities;
    }

    public static class Builder {

        private Instant cohortStartDate;
        private String userName;
        private List<String> authorities;

        private Builder() {}

        public Builder cohortStartDate(Instant cohortStartDate) {
            this.cohortStartDate = cohortStartDate;
            return this;
        }

        public Builder userName(String userName) {
            this.userName = userName;
            return this;
        }



        public Builder authorities(List<String> authorities) {
            this.authorities = new ArrayList<>(authorities);
            return this;
        }

        public UserModel build() {
            return new UserModel(this);
        }
    }



}