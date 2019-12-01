package com.limitless.setnews;

public class Users {
    private String name;
    private String phone;
    private String profile_image;
    private String user_id;
    private String department;


    public Users (){}

    public Users(String name, String phone, String profile_image, String user_id, String department) {
        this.name = name;
        this.phone = phone;
        this.profile_image = profile_image;
        this.user_id = user_id;
        this.department = department;

    }

    public String getName() {
        return name;
    }

    public void setName(String name) { this.name = name; }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getProfile_image() {
        return profile_image;
    }

    public void setProfile_image(String profile_image) {
        this.profile_image = profile_image;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getDepartment(){return department; }

    public void setDepartment(String department){this.department = department; }


}
