package com.example.project.repository;

import com.example.project.entity.Branch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface BranchRepository extends JpaRepository<Branch, Integer> {

    @Query("SELECT b FROM Branch b LEFT JOIN FETCH b.statusID ORDER BY b.id")
    List<Branch> findAllWithStatus();

    @Query("SELECT b FROM Branch b LEFT JOIN FETCH b.statusID WHERE b.id = :id")
    Optional<Branch> findByIdWithStatus(Integer id);
}
