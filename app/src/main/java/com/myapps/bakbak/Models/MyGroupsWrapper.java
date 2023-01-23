package com.myapps.bakbak.Models;

public class MyGroupsWrapper {
    MyGroups group;
    String lastMessageText,lastMessageTime,lastMessageUser;
    int newMessageCount;

    public MyGroupsWrapper(MyGroups group) {
        this.group = group;
    }

    public MyGroups getGroup() {
        return group;
    }

    public void setGroup(MyGroups group) {
        this.group = group;
    }

    public String getLastMessageText() {
        return lastMessageText;
    }

    public void setLastMessageText(String lastMessageText) {
        this.lastMessageText = lastMessageText;
    }

    public String getLastMessageTime() {
        return lastMessageTime;
    }

    public void setLastMessageTime(String lastMessageTime) {
        this.lastMessageTime = lastMessageTime;
    }

    public String getLastMessageUser() {
        return lastMessageUser;
    }

    public void setLastMessageUser(String lastMessageUser) {
        this.lastMessageUser = lastMessageUser;
    }

    public int getNewMessageCount() {
        return newMessageCount;
    }

    public void setNewMessageCount(int newMessageCount) {
        this.newMessageCount = newMessageCount;
    }
}
