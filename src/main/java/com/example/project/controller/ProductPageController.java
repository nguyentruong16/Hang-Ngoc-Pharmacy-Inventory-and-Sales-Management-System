package com.example.project.controller;

import com.example.project.dto.response.ProductDetailResponse;
import com.example.project.dto.response.ProductRowResponse;
import com.example.project.service.ProductService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

/**
 * Thymeleaf page controller for the Product List screen. Serves the same screen under each
 * role prefix (Owner / Chief Pharmacist / Pharmacist), following the pattern established by
 * {@code StockOutPageController}. Replaces the former placeholder mappings for these routes.
 */
@Controller
public class ProductPageController {

    private final ProductService productService;

    public ProductPageController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping({
            "/owner/products",
            "/chief-pharmacist/products",
            "/pharmacist/products"
    })
    public String listProducts(@RequestParam(name = "keyword", required = false) String keyword,
                               @RequestParam(name = "typeId", required = false) Integer typeId,
                               @RequestParam(name = "producerId", required = false) Integer producerId,
                               @RequestParam(name = "stockStatus", required = false) String stockStatus,
                               @RequestParam(name = "page", defaultValue = "0") int page,
                               @RequestParam(name = "size", defaultValue = "10") int size,
                               HttpServletRequest request,
                               Model model) {
        if (page < 0) {
            page = 0;
        }
        if (size <= 0) {
            size = 10;
        }

        Page<ProductRowResponse> productPage =
                productService.searchProducts(keyword, typeId, producerId, stockStatus, PageRequest.of(page, size));

        model.addAttribute("productPage", productPage);
        model.addAttribute("products", productPage.getContent());
        model.addAttribute("stats", productService.getStats());

        model.addAttribute("types", productService.listTypes());
        model.addAttribute("producers", productService.listProducers());

        model.addAttribute("keyword", keyword);
        model.addAttribute("filterTypeId", typeId);
        model.addAttribute("filterProducerId", producerId);
        model.addAttribute("filterStockStatus", stockStatus);

        model.addAttribute("currentPage", productPage.getNumber());
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("pageSize", size);
        model.addAttribute("totalItems", productPage.getTotalElements());

        model.addAttribute("basePath", resolveBasePath(request));

        return "product/list";
    }

    @GetMapping({
            "/owner/products/{productId}",
            "/chief-pharmacist/products/{productId}",
            "/pharmacist/products/{productId}"
    })
    public String productDetail(@PathVariable Integer productId,
                                HttpServletRequest request,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        String basePath = resolveBasePath(request);

        Optional<ProductDetailResponse> detail = productService.getProductDetail(productId);
        if (detail.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy hàng hóa");
            return "redirect:" + basePath;
        }

        ProductDetailResponse product = detail.get();
        model.addAttribute("product", product);
        model.addAttribute("basePath", basePath);
        model.addAttribute("canViewRecentHistory", product.isCanViewRecentHistory());

        return "product/detail";
    }

    private String resolveBasePath(HttpServletRequest request) {
        String uri = request.getRequestURI();

        if (uri.startsWith("/owner/products")) {
            return "/owner/products";
        }
        if (uri.startsWith("/pharmacist/products")) {
            return "/pharmacist/products";
        }
        return "/chief-pharmacist/products";
    }
}
