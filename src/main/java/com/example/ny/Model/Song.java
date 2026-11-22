package com.example.ny.Model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Song {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String artist;
    private String audioUrl; // Có thể là link YouTube, link MP3 ngoài, hoặc tên file upload
    private boolean isLocalFile; // true nếu là file .mp3 tự upload lên server
    private LocalDateTime createdAt = LocalDateTime.now();

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getArtist() { return artist; }
    public void setArtist(String artist) { this.artist = artist; }
    public String getAudioUrl() { return audioUrl; }
    public void setAudioUrl(String audioUrl) { this.audioUrl = audioUrl; }
    public boolean isLocalFile() { return isLocalFile; }
    public void setLocalFile(boolean localFile) { isLocalFile = localFile; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}