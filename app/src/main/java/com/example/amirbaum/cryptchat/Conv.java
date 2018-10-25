package com.example.amirbaum.cryptchat;

/**
 * Created by amirbaum on 10/10/2018.
 */

public class Conv {
    public boolean seen;
    public long timestamp;
    public String other_user_name;

    public Conv(boolean seen, long timestamp, String other_user_name) {
        this.seen = seen;
        this.timestamp = timestamp;
        this.other_user_name = other_user_name;
    }

    public Conv() {}

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getOther_user_name() {
        return other_user_name;
    }

    public void setOther_user_name(String other_user_name) {
        this.other_user_name = other_user_name;
    }
}
