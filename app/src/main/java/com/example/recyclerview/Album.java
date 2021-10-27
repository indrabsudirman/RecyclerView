package com.example.recyclerview;

public class Album {

    private long albumId;
    private int imageAlbum;
    private String titleAlbum;
    private String descAlbum;
    private String duration;
    private int albumDownload;

    //Default constructor
    public Album() {
    }

    public Album(int albumId, int imageUrl, String titleAlbum, String descAlbum, String duration, int albumDownload) {
        this.albumId = albumId;
        this.imageAlbum = imageUrl;
        this.titleAlbum = titleAlbum;
        this.descAlbum = descAlbum;
        this.duration = duration;
        this.albumDownload = albumDownload;
    }

    public long getAlbumId() {
        return albumId;
    }

    public void setAlbumId(int albumId) {
        this.albumId = albumId;
    }

    public int getImageAlbum() {
        return imageAlbum;
    }

    public void setImageAlbum(int imageAlbum) {
        this.imageAlbum = imageAlbum;
    }

    public String getTitleAlbum() {
        return titleAlbum;
    }

    public void setTitleAlbum(String titleAlbum) {
        this.titleAlbum = titleAlbum;
    }

    public String getDescAlbum() {
        return descAlbum;
    }

    public void setDescAlbum(String descAlbum) {
        this.descAlbum = descAlbum;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public int getAlbumDownload() {
        return albumDownload;
    }

    public void setAlbumDownload(int albumDownload) {
        this.albumDownload = albumDownload;
    }
}
