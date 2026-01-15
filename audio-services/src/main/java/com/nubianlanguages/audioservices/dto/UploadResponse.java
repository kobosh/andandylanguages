package com.nubianlanguages.audioservices.dto;


public class UploadResponse {
    private String objectName;
    private String url;

    public UploadResponse(String objectName, String url) {
        this.objectName = objectName;
        this.url = url;
    }

    public String getObjectName() { return objectName; }
    public String getUrl() { return url; }
}
