package org.ticket.ticket_platform.model;

import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
public class User {

    @Id
    private Integer id;

    @Email
    @NotNull
    @NotBlank
    @Column(unique=true, nullable=false)
    private String email;

    @NotNull
    @NotBlank
    @Column(nullable=false)
    private String password;

    @NotBlank
    @NotNull
    private String name;

    @Column(nullable=false)
    private boolean flag = false;
  

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name="user_role",
        joinColumns= @JoinColumn(name="user_id"),
        inverseJoinColumns= @JoinColumn(name="role_id")
    )
    private List<Role> roles;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isFlag() {
        return flag;
    }

    public void setFlag(boolean flag) {
        this.flag = flag;
    }

    public List<Role> getRoles() {
        return roles;
    }

    public void setRoles(List<Role> roles) {
        this.roles = roles;
    }


    
}
