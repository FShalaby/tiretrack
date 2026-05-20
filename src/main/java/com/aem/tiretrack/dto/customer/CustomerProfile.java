package com.aem.tiretrack.dto.customer;

import com.aem.tiretrack.enums.UserRole;
import com.aem.tiretrack.model.User;

public class CustomerProfile {
    private final Long id;
    private final String fullName;
    private final String email;
    private final String phone;
    private final UserRole role;

    public CustomerProfile(User user) {
        this.id = user.getId();
        this.fullName = user.getFullName();
        this.email = user.getEmail();
        this.phone = user.getPhone();
        this.role = user.getRole();
    }

    public Long getId() { return id; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public UserRole getRole() { return role; }
}
