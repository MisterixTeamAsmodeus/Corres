package com.itschool.buzuverov.corres_chat.Model;

import com.itschool.buzuverov.corres_chat.Adapter.AdapterUpdater.UpdaterAdapterInterface;

import java.util.Comparator;

public class Chat implements UpdaterAdapterInterface {

    private String lastMessage = "";
    private String lastMessageType = "";
    private String lastMessageTime = "";

    private String statusMessages = "";
    private String fromMessages = "";
    private int notCheckMessage = 0;
    private User user = new User();

    public static final Comparator<Chat> TIME_COMPARATOR = new Comparator<Chat>() {
        @Override
        public int compare(Chat o1, Chat o2) {
            return Long.compare(Long.parseLong(o2.getLastMessageTime()), Long.parseLong(o1.getLastMessageTime()));
        }
    };

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public String getLastMessageTime() {
        return lastMessageTime;
    }

    public void setLastMessageTime(String lastMessageTime) {
        this.lastMessageTime = lastMessageTime;
    }

    public String getName() {
        return user.getName();
    }

    public void setName(String name) {
        user.setName(name);
    }

    public String getImagePath() {
        return user.getImagePath();
    }

    public void setImagePath(String imagePath) {
        user.setImagePath(imagePath);
    }

    public String getId() {
        return user.getId();
    }

    public void setId(String id) {
        user.setId(id);
    }

    public String getOnline() {
        return user.getOnline();
    }

    public void setOnline(String online) {
        user.setOnline(online);
    }

    public String getStatusMessages() {
        return statusMessages;
    }

    public void setStatusMessages(String statusMessages) {
        this.statusMessages = statusMessages;
    }

    public String getFromMessages() {
        return fromMessages;
    }

    public void setFromMessages(String fromMessages) {
        this.fromMessages = fromMessages;
    }

    public String getLastMessageType() {
        return lastMessageType;
    }

    public void setLastMessageType(String lastMessageType) {
        this.lastMessageType = lastMessageType;
    }

    public int getNotCheckMessage() {
        return notCheckMessage;
    }

    public void setNotCheckMessage(int notCheckMessage) {
        this.notCheckMessage = notCheckMessage;
    }

    public int getDataToCheck() {
        return (getName() + getImagePath() + getStatusMessages() + getOnline() + getLastMessage() + getLastMessageType() + getLastMessageTime() + getFromMessages() + getNotCheckMessage()).hashCode();
    }
}
