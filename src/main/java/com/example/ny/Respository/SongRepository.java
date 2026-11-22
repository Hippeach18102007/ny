package com.example.ny.Respository;

import com.example.ny.Model.Song;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SongRepository extends JpaRepository<Song, Long> {
    List<Song> findAllByOrderByCreatedAtDesc();
}