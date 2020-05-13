package com.rwtcompany.chitchat.model;

public class Message {
    private String uid;
    private String message;
    private boolean isSelf;
    private String name;
    private String imageUrl;

    public Message() {
    }

    public Message(String uid, String message, boolean isSelf) {
        this.uid = uid;
        this.message = message;
        this.isSelf = isSelf;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSelf() {
        return isSelf;
    }

    public void setSelf(boolean self) {
        isSelf = self;
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
}
