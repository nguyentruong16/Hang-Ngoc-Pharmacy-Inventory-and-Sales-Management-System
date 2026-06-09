package com.example.project.controller;

import com.example.project.dto.response.ComboResponse;
import com.example.project.service.ComboService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/combos")
public class ComboController {
    private final ComboService comboService;

    public ComboController(ComboService comboService) {
        this.comboService = comboService;
    }

    @GetMapping
    public List<ComboResponse> getAll() {
        return comboService.getAll();
    }
}