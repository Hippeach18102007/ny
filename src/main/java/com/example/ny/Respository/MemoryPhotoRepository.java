package com.example.ny.Respository;

import com.example.ny.Model.MemoryPhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MemoryPhotoRepository extends JpaRepository<MemoryPhoto, Long> {
    List<MemoryPhoto> findAllByOrderByCreatedAtDesc();
}