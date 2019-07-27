package com.kstefancic.lotterymaster.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Entity
@Table(name = "users")
public class User {

    @Id
    @Column(nullable = false, updatable = false, length = 100, unique = true)
    private String id;
    @NotBlank
    @Email
    @Column(nullable = false, updatable = false, unique = true, length = 100)
    private String email;
    @NotBlank
    @Column(nullable = false)
    private String password;
    private int generatesLeft = 2;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getGeneratesLeft() {
        return generatesLeft;
    }

    public void setGeneratesLeft(int generatesLeft) {
        this.generatesLeft = generatesLeft;
    }
}
