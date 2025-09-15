package com.gridscale.nextpush.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LoginResponse {
    
    @JsonProperty("token")
    private String token;
    
    @JsonProperty("userId")
    private Long userId;
    
    @JsonProperty("userName")
    private String userName;
    
    @JsonProperty("email")
    private String email;
    
    @JsonProperty("userType")
    private String userType;
    
    @JsonProperty("message")
    private String message;
    
    // Default constructor
    public LoginResponse() {}
    
    // Constructor for successful login
    public LoginResponse(String token, Long userId, String userName, String email, String userType) {
        this.token = token;
        this.userId = userId;
        this.userName = userName;
        this.email = email;
        this.userType = userType;
        this.message = "Login successful";
    }
    
    // Constructor for error response
    public LoginResponse(String message) {
        this.message = message;
    }
    
    // Getters and Setters
    public String getToken() {
        return token;
    }
    
    public void setToken(String token) {
        this.token = token;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public String getUserName() {
        return userName;
    }
    
    public void setUserName(String userName) {
        this.userName = userName;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getUserType() {
        return userType;
    }
    
    public void setUserType(String userType) {
        this.userType = userType;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
}