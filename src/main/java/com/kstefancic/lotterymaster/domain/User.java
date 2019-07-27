package com.kstefancic.lotterymaster.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;

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

    private boolean enabled = false;

    @JsonIgnore
    private String verificationCode;

    public String getVerificationCode() {
        return verificationCode;
    }

    public void setVerificationCode(String verificationCode) {
        this.verificationCode = verificationCode;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

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
