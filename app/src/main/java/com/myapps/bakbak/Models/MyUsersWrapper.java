package com.myapps.bakbak.Models;

public class MyUsersWrapper {

    MyUsers user;
    String lastMessageText,lastMessageTime,lastMessageUser;
    int newMessageCount;

    public int getNewMessageCount() {
        return newMessageCount;
    }

    public void setNewMessageCount(int lastMessageCount) {
        this.newMessageCount = lastMessageCount;
    }

    public MyUsersWrapper(MyUsers user){
        this.user=user;
    }

    public MyUsers getUser() {
        return user;
    }

    public void setUser(MyUsers user) {
        this.user = user;
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
}
