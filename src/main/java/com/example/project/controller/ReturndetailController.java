package com.example.project.controller;

import com.example.project.dto.response.ReturndetailResponse;
import com.example.project.service.ReturndetailService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/return-details")
public class ReturndetailController {
    private final ReturndetailService returndetailService;

    public ReturndetailController(ReturndetailService returndetailService) {
        this.returndetailService = returndetailService;
    }

    @GetMapping
    public List<ReturndetailResponse> getAll() {
        return returndetailService.getAll();
    }
}