package com.example.project.controller;

import com.example.project.entity.Purchaserequisition;
import com.example.project.service.PurchaserequisitionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/purchase-requisitions")
public class PurchaserequisitionController {
    private final PurchaserequisitionService purchaserequisitionService;

    public PurchaserequisitionController(PurchaserequisitionService purchaserequisitionService) {
        this.purchaserequisitionService = purchaserequisitionService;
    }

    @GetMapping
    public List<Purchaserequisition> getAll() {
        return purchaserequisitionService.getAll();
    }
}
