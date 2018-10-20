package com.example.amirbaum.cryptchat;

import com.firebase.ui.auth.data.model.User;

/**
 * Created by amirbaum on 06/10/2018.
 */

public class Users {
    public String name;
    public String picture;
    public String status;
    public String thumb_image;

    public Users() {}

    public Users(String name, String picture, String status, String thum_image) {
        this.name = name;
        this.picture = picture;
        this.status = status;
        this.thumb_image = thum_image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getThum_image() {
        return thumb_image;
    }

    public void setThum_image(String thum_image) {
        this.thumb_image = thum_image;
    }
}
