package com.example.amirbaum.cryptchat;

/**
 * Created by amirbaum on 09/10/2018.
 */

class Friends {
    public String date;

    public Friends() {}

    public Friends(String date) {
        this.date = date;
    }

    public String getStatus() {
        return date;
    }

    public void setStatus(String date) {
        this.date = date;
    }
}
