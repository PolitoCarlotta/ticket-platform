package org.ticket.ticket_platform.security;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.ticket.ticket_platform.model.Role;
import org.ticket.ticket_platform.model.User;

public class DatabaseUserDetails implements UserDetails {

    private String email;

    private String password;

    private Set<GrantedAuthority> authorities;

    public DatabaseUserDetails(User user) {
        this.email = user.getEmail();
        this.password = user.getPassword();
        this.authorities = new HashSet<>();
        for(Role role : user.getRoles()) {
            SimpleGrantedAuthority sGA = new SimpleGrantedAuthority(role.getName());
            this.authorities.add(sGA);
        }
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities(){
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }
       

}
