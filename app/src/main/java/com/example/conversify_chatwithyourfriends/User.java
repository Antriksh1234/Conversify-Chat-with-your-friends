package com.example.conversify_chatwithyourfriends;

public class User {
    public String name = "";
    public String mobile = "";
    public String status = "";
    private String about = "";
    public User(){
        //Do nothing
    }

    public User(String name, String mobile,String status,String about)
    {
        this.mobile = mobile;
        this.name = name;
        this.status = status;
        this.about = about;
    }

    public String getMobile() {
        return mobile;
    }

    public String getName() {
        return name;
    }

    public String getAbout() {
        return about;
    }

    public String getStatus() {
        return status;
    }
}
