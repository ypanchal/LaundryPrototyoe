package com.laundry.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "app_user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Phone number doubles as the username for both admin and customers.
    @Column(unique = true, nullable = false)
    private String phone;

    // NOTE: plain text for prototype simplicity only.
    // Before going live, hash this (e.g. BCryptPasswordEncoder) - see README.
    @JsonIgnore
    @Column(nullable = false)
    private String password;

    private String name;

    @Enumerated(EnumType.STRING)
    private Role role;

    public User() {}

    public User(String phone, String password, String name, Role role) {
        this.phone = phone;
        this.password = password;
        this.name = name;
        this.role = role;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
}
