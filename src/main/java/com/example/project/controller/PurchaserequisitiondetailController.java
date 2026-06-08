package com.example.project.controller;

import com.example.project.entity.Purchaserequisitiondetail;
import com.example.project.service.PurchaserequisitiondetailService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/purchase-requisition-details")
public class PurchaserequisitiondetailController {
    private final PurchaserequisitiondetailService purchaserequisitiondetailService;

    public PurchaserequisitiondetailController(PurchaserequisitiondetailService purchaserequisitiondetailService) {
        this.purchaserequisitiondetailService = purchaserequisitiondetailService;
    }

    @GetMapping
    public List<Purchaserequisitiondetail> getAll() {
        return purchaserequisitiondetailService.getAll();
    }
}