package com.laundry.dto;

import com.laundry.model.Role;

public class LoginResponse {
    private String token;
    private Role role;
    private String name;
    private String phone;

    public LoginResponse(String token, Role role, String name, String phone) {
        this.token = token;
        this.role = role;
        this.name = name;
        this.phone = phone;
    }

    public String getToken() { return token; }
    public Role getRole() { return role; }
    public String getName() { return name; }
    public String getPhone() { return phone; }
}
