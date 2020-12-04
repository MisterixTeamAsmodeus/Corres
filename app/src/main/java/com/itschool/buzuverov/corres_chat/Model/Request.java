package com.itschool.buzuverov.corres_chat.Model;

import com.itschool.buzuverov.corres_chat.Adapter.AdapterUpdater.UpdaterAdapterInterface;

public class Request implements UpdaterAdapterInterface {

    private User user = new User();
    private String type;

    public Request(){ }

    public String getName() {
        return user.getName();
    }

    public void setName(String name) {
        user.setName(name);
    }

    public String getStatus() {
        return user.getStatus();
    }

    public void setStatus(String status) {
        user.setStatus(status);
    }

    public String getImage() {
        return user.getImagePath();
    }

    public void setImage(String image) {
        user.setImagePath(image);
    }

    public String getId() {
        return user.getId();
    }

    public void setId(String id) {
        user.setId(id);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getToken() {
        return user.getToken();
    }

    public void setToken(String token) {
        user.setToken(token);
    }

    public int getDataToCheck() {
        return (getName() + getStatus() + getImage() + type).hashCode();
    }
}
