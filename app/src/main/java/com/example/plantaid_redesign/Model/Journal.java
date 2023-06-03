package com.example.plantaid_redesign.Model;

public class Journal {
    public String id;
    public String title;
    public String content;
    public String photo;
    public String like;
    public String date;
    public String time;


    public Journal() {
    }

    public Journal(String id, String title, String content, String photo, String like, String date, String time) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.photo = photo;
        this.like = like;
        this.date = date;
        this.time = time;
    }

    public Journal(String content, String photo, String date, String time, String like) {
        this.content = content;
        this.photo = photo;
        this.date = date;
        this.time = time;
        this.like = like;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getLike() {
        return like;
    }

    public void setLike(String like) {
        this.like = like;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
