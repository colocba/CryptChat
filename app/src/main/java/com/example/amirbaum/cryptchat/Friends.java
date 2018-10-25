package com.example.amirbaum.cryptchat;

/**
 * Created by amirbaum on 09/10/2018.
 */

class Friends {
    public String date;
    public String friends_name;

    public Friends() {}

    public Friends(String date, String friends_name) {

        this.date = date;
        this.friends_name = friends_name;
    }

    public String getStatus() {
        return date;
    }

    public void setStatus(String date) {
        this.date = date;
    }

    public String getFriends_name() {
        return friends_name;
    }

    public void setFriends_name(String friends_name) {
        this.friends_name = friends_name;
    }
}
