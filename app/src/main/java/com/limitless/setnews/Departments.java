package com.limitless.setnews;

import java.io.Serializable;

public class Departments implements Serializable {
    private String id;
    private String name;
    private String imageUrl;



    private String details;

    public Departments(){}

    public Departments(String name, String imageUrl, String details){
        this.setId(id);
        this.setName(name);
        this.setImageUrl(imageUrl);
        this.setDetails(details);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }
}
