package com.example.myapplication.Models;

public class Message {

    private String from, message, type, time, date, readStatus, mid;

    public Message() {
    }

    public Message(String from, String message, String type, String time, String date, String readStatus, String mid) {
        this.from = from;
        this.message = message;
        this.type = type;
        this.time = time;
        this.date = date;
        this.readStatus = readStatus;
        this.mid = mid;
    }

    public String getMid() {
        return mid;
    }

    public void setMid(String mid) {
        this.mid = mid;
    }

    public String getReadStatus() {
        return readStatus;
    }

    public void setReadStatus(String readStatus) {
        this.readStatus = readStatus;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}