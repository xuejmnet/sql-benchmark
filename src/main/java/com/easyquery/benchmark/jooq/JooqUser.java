package com.easyquery.benchmark.jooq;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class JooqUser {
    private String id;
    private String username;
    private String email;
    private Integer age;
    private String phone;
    private String address;

    public JooqUser() {
    }

    public JooqUser(String id, String username, String email, Integer age, String phone, String address) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.age = age;
        this.phone = phone;
        this.address = address;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}



