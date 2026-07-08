package com.example.project.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ProcurementPlanCreateRequest {

    private String note;

    @Valid
    @NotEmpty(message = "Dự trù mua hàng phải có ít nhất một sản phẩm")
    private List<ProcurementPlanDetailCreateRequest> details = new ArrayList<>();
}
