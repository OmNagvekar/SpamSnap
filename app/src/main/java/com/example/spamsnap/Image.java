package com.example.spamsnap;

public class Image {
    String imagepath;
    String imagename;
    boolean select;

    public Image(String imagepath, String imagename) {
        this.imagepath = imagepath;
        this.imagename = imagename;
        this.select=false;
    }


}
