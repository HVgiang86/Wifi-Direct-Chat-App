package com.example.wifidirectchatapp.model;

public class Message {
    private String sender;
    private String content;
    private boolean isServer;
    private boolean isFile;

    public Message(String content, boolean isServer, boolean isFile) {
        this.content = content;
        this.isServer = isServer;
        this.isFile = isFile;
    }

    public Message(String sender, String content, boolean isServer, boolean isFile) {
        this.sender = sender;
        this.content = content;
        this.isServer = isServer;
        this.isFile = isFile;
    }

    public Message() {
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isServer() {
        return isServer;
    }

    public void setServer(boolean server) {
        isServer = server;
    }

    public boolean isFile() {
        return isFile;
    }

    public void setFile(boolean file) {
        isFile = file;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }
}
