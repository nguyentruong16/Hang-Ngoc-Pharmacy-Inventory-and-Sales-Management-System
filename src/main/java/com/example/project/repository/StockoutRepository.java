package com.example.project.repository;

import com.example.project.entity.Stockout;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockoutRepository extends JpaRepository<Stockout, Integer> {
}