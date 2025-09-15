package com.gridscale.nextpush.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LoginRequest {
    
    @JsonProperty("email")
    private String email;
    
    @JsonProperty("password")
    private String password;
    
    // Default constructor
    public LoginRequest() {}
    
    // Constructor with parameters
    public LoginRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }
    
    // Getters and Setters
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
}