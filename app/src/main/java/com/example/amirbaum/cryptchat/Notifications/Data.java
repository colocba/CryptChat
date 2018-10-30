package com.example.amirbaum.cryptchat.Notifications;

/**
 * Created by amirbaum on 30/10/2018.
 */

public class Data {
    private String user;
    private int icon;
    private String body;
    private String title;
    private String sent;
    private String from_name;
    private String to_name;
    private boolean isImage;

    public Data() {
    }

    public Data(String user, int icon, String body, String title, String sent, String from_name, String to_name, boolean isImage) {
        this.user = user;
        this.icon = icon;
        this.body = body;
        this.title = title;
        this.sent = sent;
        this.from_name = from_name;
        this.to_name = to_name;
        this.isImage = isImage;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSent() {
        return sent;
    }

    public void setSent(String sent) {
        this.sent = sent;
    }

    public String getFrom_name() {
        return from_name;
    }

    public void setFrom_name(String from_name) {
        this.from_name = from_name;
    }

    public String getTo_name() {
        return to_name;
    }

    public void setTo_name(String to_name) {
        this.to_name = to_name;
    }

    public boolean isImage() {
        return isImage;
    }

    public void setImage(boolean image) {
        isImage = image;
    }
}
