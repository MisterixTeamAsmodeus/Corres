package com.itschool.buzuverov.corres_chat.Model;

import com.itschool.buzuverov.corres_chat.Adapter.AdapterUpdater.UpdaterAdapterInterface;

import java.util.Comparator;

public class User implements UpdaterAdapterInterface {

    public static final Comparator<User> NAME_COMPARATOR = new Comparator<User>() {
                @Override
                public int compare(User o1, User o2) {
                    return o1.getName().compareTo(o2.getName());
                }
            };

    private String name = "";
    private String status = "";
    private String imagePath = "";
    private String id = "";
    private String email = "";
    private String online = "";
    private String token = "";
    private int requestCount = 0;

    public User(String name, String status, String image, String id) {
        this.name = name;
        this.status = status;
        this.imagePath = image;
        this.id = id;
    }

    public User(){ }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOnline() {
        return online;
    }

    public void setOnline(String online) {
        this.online = online;
    }

    public int getRequestCount() {
        return requestCount;
    }

    public void setRequestCount(int requestCount) {
        this.requestCount = requestCount;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public int getDataToCheck() {
        return (name+imagePath+online).hashCode();
    }
}
