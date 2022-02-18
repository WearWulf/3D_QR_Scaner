package com.savonikaleksandr.scaner.My_tools;

import java.io.Serializable;

public class ItemSave implements Serializable {
    private String id, title, subtitle, uriObject, uriObjImag, uriQrcode;


    public ItemSave() {

    }

    public ItemSave(String id, String title, String subtitle, String uriObject, String uriObjImag, String uriQrcode) {
        this.id = id;
        this.title = title;
        this.subtitle = subtitle;
        this.uriObject = uriObject;
        this.uriObjImag = uriObjImag;
        this.uriQrcode = uriQrcode;
    }

    public String getUriQrcode() {
        return uriQrcode;
    }

    public void setUriQrcode(String uriQrcode) {
        this.uriQrcode = uriQrcode;
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

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public String getUriObject() {
        return uriObject;
    }

    public void setUriObject(String uriObject) {
        this.uriObject = uriObject;
    }

    public String getUriObjImag() {
        return uriObjImag;
    }

    public void setUriObjImag(String uriObjImag) {
        this.uriObjImag = uriObjImag;
    }
}
