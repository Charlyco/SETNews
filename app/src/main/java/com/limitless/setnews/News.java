package com.limitless.setnews;

import android.widget.EditText;

import java.io.Serializable;

public class News implements Serializable {
    private String id;
    private String title;
    private String details;
    private String imageUrl;
    private String imageName;

    public News(){}

    public News(String title, String details, String imageUrl, String imageName) {
        this.setId(id);
        this.setTitle(title);
        this.setDetails(details);
        this.setImageUrl(imageUrl);
        this.setImageName(imageName);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }
}
