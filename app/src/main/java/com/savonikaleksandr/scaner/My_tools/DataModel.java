package com.savonikaleksandr.scaner.My_tools;


public class DataModel {

    String id;
    String title;
    String text;
    String object;
    String image_qr;
    String image_obj;

    public DataModel(String id, String name, String subtitle, String object, String image_obj, String image_qr) {
        this.id = id;
        this.title = name;
        this.text = subtitle;
        this.object = object;
        this.image_qr = image_qr;
        this.image_obj = image_obj;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getImage_obj() {
        return image_obj;
    }

    public void setImage_obj(String image_obj) {
        this.image_obj = image_obj;
    }

    public String getImage_qr() {
        return image_qr;
    }

    public void setImage_qr(String image_qr) {
        this.image_qr = image_qr;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getObject() {
        return object;
    }

    public void setObject(String object) {
        this.object = object;
    }
}