package com.example.myapplication.models;

import java.util.List;

public class Movie {
    private String id;
    private String title;
    private String description;
    private String posterUrl;
    private int duration; // minutes
    private List<String> genres;

    public Movie() {}

    public Movie(String id, String title, String description, String posterUrl, int duration, List<String> genres) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.posterUrl = posterUrl;
        this.duration = duration;
        this.genres = genres;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getPosterUrl() { return posterUrl; }
    public void setPosterUrl(String posterUrl) { this.posterUrl = posterUrl; }
    public int getDuration() { return duration; }
    public void setDuration(int duration) { this.duration = duration; }
    public List<String> getGenres() { return genres; }
    public void setGenres(List<String> genres) { this.genres = genres; }
}