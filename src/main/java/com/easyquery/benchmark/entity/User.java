package com.easyquery.benchmark.entity;

import com.easy.query.core.annotation.Column;
import com.easy.query.core.annotation.EntityProxy;
import com.easy.query.core.annotation.Table;
import com.easy.query.core.proxy.ProxyEntityAvailable;
import com.easyquery.benchmark.entity.proxy.UserProxy;

@Table("t_user")
@EntityProxy
public class User implements ProxyEntityAvailable<User, UserProxy> {
    
    @Column(primaryKey = true)
    private String id;
    
    @Column
    private String username;
    
    @Column
    private String email;
    
    @Column
    private Integer age;
    
    @Column
    private String phone;
    
    @Column
    private String address;

    public User() {
    }

    public User(String id, String username, String email, Integer age, String phone, String address) {
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

