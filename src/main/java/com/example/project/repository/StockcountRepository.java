package com.example.project.repository;

import com.example.project.entity.Stockcount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockcountRepository extends JpaRepository<Stockcount, Integer> {
}