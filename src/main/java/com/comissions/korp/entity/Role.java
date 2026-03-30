package com.comissions.korp.entity;

import com.comissions.korp.entity.ENUM.Roles;
import jakarta.persistence.*;

@Entity(name = "roles" )
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "role_id")
    private Integer id;

    @Enumerated(EnumType.STRING)
    private Roles role;

    public Role() {
    }

    public Role(Integer id, Roles role) {
        this.id = id;
        this.role = role;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Roles getRole() {
        return role;
    }

    public void setRole(Roles role) {
        this.role = role;
    }

}
