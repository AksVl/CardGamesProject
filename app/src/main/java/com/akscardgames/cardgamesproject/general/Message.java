package com.akscardgames.cardgamesproject.general;

public class Message {
    private final String author;
    private final String message;
    private final long time;

    public String getAuthor() {
        return author;
    }

    public String getMessage() {
        return message;
    }

    public long getTime() {
        return time;
    }

    public Message(String author, String text, long time) {
        this.author = author;
        this.message = text;
        this.time = time;
    }
}
