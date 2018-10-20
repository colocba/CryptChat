package com.example.amirbaum.cryptchat;

/**
 * Created by amirbaum on 10/10/2018.
 */

public class Messages {

    private String rsa_encrypted_message, aes_encrypted_message, id, type, from;
    private boolean seen;
    private long time;

    public Messages() {}

    public Messages(String rsa_encrypted_message, String aes_encrypted_message, String id, String type, String from, boolean seen, long time) {
        this.rsa_encrypted_message = rsa_encrypted_message;
        this.aes_encrypted_message = aes_encrypted_message;
        this.id = id;
        this.type = type;
        this.from = from;
        this.seen = seen;
        this.time = time;
    }

    public String getRsa_encrypted_message() {
        return rsa_encrypted_message;
    }

    public void setRsa_encrypted_message(String rsa_encrypted_message) {
        this.rsa_encrypted_message = rsa_encrypted_message;
    }

    public String getAes_encrypted_message() {
        return aes_encrypted_message;
    }

    public void setAes_encrypted_message(String aes_encrypted_message) {
        this.aes_encrypted_message = aes_encrypted_message;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
