package com.example.ny.Model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Entity
@Table(name = "letters")
public class Letter {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, length = 20000)
    private String content;

    private String imageFile; // tên file ảnh trong thư mục uploads

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getImageFile() { return imageFile; }
    public void setImageFile(String imageFile) { this.imageFile = imageFile; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public String getCreatedAtText() { return createdAt.format(FMT); }
}