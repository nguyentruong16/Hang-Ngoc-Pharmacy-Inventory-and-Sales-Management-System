package com.example.project.repository;

import com.example.project.entity.Stockoutdetail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockoutdetailRepository extends JpaRepository<Stockoutdetail, Integer> {
}