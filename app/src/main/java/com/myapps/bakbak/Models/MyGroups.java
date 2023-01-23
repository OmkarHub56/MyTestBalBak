package com.myapps.bakbak.Models;

public class MyGroups {
    String group_name,group_id,group_profile_pic;

    public MyGroups(String group_name, String group_id, String group_profile_pic) {
        this.group_name = group_name;
        this.group_id = group_id;
        this.group_profile_pic = group_profile_pic;
    }

    // for firebase
    public MyGroups() {
    }

    public String getGroup_name() {
        return group_name;
    }

    public void setGroup_name(String group_name) {
        this.group_name = group_name;
    }

    public String getGroup_id() {
        return group_id;
    }

    public void setGroup_id(String group_id) {
        this.group_id = group_id;
    }

    public String getGroup_profile_pic() {
        return group_profile_pic;
    }

    public void setGroup_profile_pic(String group_profile_pic) {
        this.group_profile_pic = group_profile_pic;
    }
}
