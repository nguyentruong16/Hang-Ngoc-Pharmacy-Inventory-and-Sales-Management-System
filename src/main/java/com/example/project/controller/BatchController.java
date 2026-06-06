package com.example.project.controller;

import com.example.project.entity.Batch;
import com.example.project.service.BatchService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/batches")
public class BatchController {
    private final BatchService batchService;

    public BatchController(BatchService batchService) {
        this.batchService = batchService;
    }

    @GetMapping
    public List<Batch> getAll() {
        return batchService.getAll();
    }
}
