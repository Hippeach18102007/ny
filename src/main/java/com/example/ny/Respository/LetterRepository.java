package com.example.ny.Respository;

import com.example.ny.Model.Letter;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LetterRepository extends JpaRepository<Letter, Long> {
    List<Letter> findAllByOrderByCreatedAtDesc();
}