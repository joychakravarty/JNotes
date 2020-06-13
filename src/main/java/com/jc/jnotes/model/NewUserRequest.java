package com.jc.jnotes.model;

public class NewUserRequest {
    
    public NewUserRequest() {
        
    }
    
    public NewUserRequest(String userId, String userSecret) {
        this.userId = userId;
        this.userSecret = userSecret;
    }

    private String userId;
    private String userSecret;
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserSecret() {
        return userSecret;
    }

    public void setUserSecret(String userSecret) {
        this.userSecret = userSecret;
    }

}
