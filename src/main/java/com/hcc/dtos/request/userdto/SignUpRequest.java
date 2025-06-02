package com.hcc.dtos.request.userdto;



public class SignUpRequest {

    @NotBlan
    private String userName;

    private String password;
    private String role;


    public SignUpRequest() {}


    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
