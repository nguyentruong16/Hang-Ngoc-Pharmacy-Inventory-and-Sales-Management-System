package com.example.project.controller;

import com.example.project.entity.Combocomponent;
import com.example.project.service.CombocomponentService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/combo-components")
public class CombocomponentController {
    private final CombocomponentService combocomponentService;

    public CombocomponentController(CombocomponentService combocomponentService) {
        this.combocomponentService = combocomponentService;
    }

    @GetMapping
    public List<Combocomponent> getAll() {
        return combocomponentService.getAll();
    }
}