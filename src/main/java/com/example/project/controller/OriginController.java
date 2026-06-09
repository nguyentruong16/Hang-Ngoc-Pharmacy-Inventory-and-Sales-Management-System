package com.example.project.controller;

import com.example.project.dto.response.OriginResponse;
import com.example.project.service.OriginService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/origins")
public class OriginController {
    private final OriginService originService;

    public OriginController(OriginService originService) {
        this.originService = originService;
    }

    @GetMapping
    public List<OriginResponse> getAll() {
        return originService.getAll();
    }
}