package com.example.project.controller;

import com.example.project.entity.Employeenote;
import com.example.project.service.EmployeenoteService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/employee-notes")
public class EmployeenoteController {
    private final EmployeenoteService employeenoteService;

    public EmployeenoteController(EmployeenoteService employeenoteService) {
        this.employeenoteService = employeenoteService;
    }

    @GetMapping
    public List<Employeenote> getAll() {
        return employeenoteService.getAll();
    }
}
