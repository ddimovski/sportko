package com.dimovski.sportko.db.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;


@Entity(tableName = "users")
public class User {

    @PrimaryKey
    @NonNull
    private String email;
    private String username;

    public User() {

    }
    public User(String email) {
        this.email = email;
        this.username = "test";
    }

    public User(String email, String username) {
        this.email = email;
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }


}
