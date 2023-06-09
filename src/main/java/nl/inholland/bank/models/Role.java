package nl.inholland.bank.models;

import org.springframework.security.core.GrantedAuthority;

public enum Role implements GrantedAuthority {
    CUSTOMER,
    EMPLOYEE,
    ADMIN;

    @Override
    public String getAuthority() {
        return name();
    }
}
