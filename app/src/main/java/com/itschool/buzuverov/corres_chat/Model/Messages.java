package com.itschool.buzuverov.corres_chat.Model;

import com.itschool.buzuverov.corres_chat.Adapter.AdapterUpdater.UpdaterAdapterInterface;

public class Messages implements UpdaterAdapterInterface {

    private String
            from,
            message,
            type,
            time,
            id,
            status,
            uri;

    public Messages(){}

    public Messages(String message, String type) {
        this.message = message;
        this.type = type;
    }

    public Messages(String from, String message, String type, String time, String id, String status) {
        this.from = from;
        this.message = message;
        this.type = type;
        this.time = time;
        this.id = id;
        this.status = status;
    }

    public int getDataToCheck(){
        return (message + status + time).hashCode();
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }
}
