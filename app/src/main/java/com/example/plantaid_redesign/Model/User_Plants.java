package com.example.plantaid_redesign.Model;

public class User_Plants {
    public String image;
    public String key;
    public String user_key;
    public String c_plantName;
    public String s_plantName;

    public User_Plants() {

    }

    public User_Plants(String c_plantName, String s_plantName, String image, String key, String user_key) {
        this.user_key = user_key;
        this.key = key;
        this.image = image;
        this.c_plantName = c_plantName;
        this.s_plantName = s_plantName;
    }

    public String getUser_key() {
        return user_key;
    }

    public void setUser_key(String user_key) {
        this.user_key = user_key;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getC_plantName() {
        return c_plantName;
    }

    public void setC_plantName(String c_plantName) {
        this.c_plantName = c_plantName;
    }

    public String getS_plantName() {
        return s_plantName;
    }

    public void setS_plantName(String s_plantName) {
        this.s_plantName = s_plantName;
    }


    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
