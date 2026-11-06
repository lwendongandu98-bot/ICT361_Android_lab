package com.example.studentlabgroupmanagement.data.remote; // Adjust package as needed

public class LoginResponse {
    private String message;
    private String token;
    private UserDto user;

    public String getMessage() { return message; }
    public String getToken() { return token; }
    public UserDto getUser() { return user; }

    public static class UserDto {
        private int id;
        private String name;
        private String role;

        public int getId() { return id; }
        public String getName() { return name; }
        public String role() { return role; }
    }
}