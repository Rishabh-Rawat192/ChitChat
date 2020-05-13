package com.rwtcompany.chitchat.model;

public class User {
    private String name;
    private String imageUrl;
    private String uid;
    private String active;
    public User(){

    }

    public User(String name, String imageUrl, String uid, String active) {
        this.name = name;
        this.imageUrl = imageUrl;
        this.uid = uid;
        this.active = active;
    }

    public String getActive() {
        return active;
    }

    public void setActive(String active) {
        this.active = active;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
