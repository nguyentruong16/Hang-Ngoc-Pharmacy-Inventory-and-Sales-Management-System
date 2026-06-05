package com.example.project.repository;

import com.example.project.entity.Employeenote;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeenoteRepository extends JpaRepository<Employeenote, Integer> {
}